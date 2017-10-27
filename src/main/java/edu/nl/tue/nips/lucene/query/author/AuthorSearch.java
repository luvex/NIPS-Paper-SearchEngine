package edu.nl.tue.nips.lucene.query.author;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.nl.tue.nips.basic.Author;
import edu.nl.tue.nips.basic.Paper;
import edu.nl.tue.nips.basic.Result;

/*
 * implementation of author query
 * the query will consider topic as a filter, can use can decide
 * how the other 2 rules are mixed
 */
public class AuthorSearch
{
	Map<String, String> params;

	public AuthorSearch(Map<String, String> params)
	{
		// TODO Auto-generated constructor stub
		this.params = params;
	}

	public Author[] compute() throws IOException, ParseException
	{
		//read index
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("iresource//authorindex")));
		IndexSearcher searcher = new IndexSearcher(reader);

		List<Author> ids = new ArrayList<Author>();

		//query for author names
		if (this.params.containsKey("name"))
		{
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("name", analyzer);
			Query query = parser.parse(this.params.get("name"));
			TopDocs hits = searcher.search(query, 8653);
			ScoreDoc[] scoreDocs = hits.scoreDocs;
			for (ScoreDoc scoreDoc : scoreDocs)
			{
				Author author = new Author();
				author.setId(Integer.parseInt(searcher.doc(scoreDoc.doc).get("id")));
				author.setScore(scoreDoc.score);
				ids.add(author);
			}
		}
		ids.sort(null);
		
		//query for institutions
		if (this.params.containsKey("inst"))
		{
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("institution", analyzer);
			Query query = parser.parse(this.params.get("inst"));
			TopDocs hits = searcher.search(query, 8653);
			ScoreDoc[] scoreDocs = hits.scoreDocs;
			
			//the author fulfill any of the rules
			if (params.get("mix").equals("or"))
			{
				for (ScoreDoc scoreDoc : scoreDocs)
				{
					int idtemp = Integer.parseInt(searcher.doc(scoreDoc.doc).get("id"));
					float scoretemp = scoreDoc.score;
					boolean has = false;
					for (int i = 0; i < ids.size(); i++)
					{
						if (ids.get(i).getId() == idtemp)
						{
							has = true;
							ids.get(i).setScore(ids.get(i).getScore() + scoretemp);
							break;
						}
					}
					if (has)
						continue;
					else
					{
						ids.add(new Author(idtemp, scoretemp));
					}
				}
			}
			//the author fulfills all the rules
			else if (params.get("mix").equals("and"))
			{
				List<Author> instAuthors = new ArrayList<Author>();
				for (ScoreDoc scoreDoc : scoreDocs)
				{
					int idtemp = Integer.parseInt(searcher.doc(scoreDoc.doc).get("id"));
					float scoretemp = scoreDoc.score;
					instAuthors.add(new Author(idtemp, scoretemp));
				}
				List<Author> overlappedAuthors = new ArrayList<Author>();
				for (Author a : ids)
				{
					for (Author insta : instAuthors)
					{
						if (a.getId() == insta.getId())
						{
							overlappedAuthors.add(new Author(a.getId(), a.getScore() + insta.getScore()));
						}
					}
				}
				ids = overlappedAuthors;
			}
		}

		List<Integer> topicIds = null;
		if (this.params.containsKey("topic"))
		{
			topicIds = processTopic(searcher);
		}

		//if there is no filter, return the result directly
		if (topicIds == null)
		{
			ids.sort(null);
			return ids.toArray(new Author[0]);
		}
		//filter the results
		List<Author> authors = new ArrayList<Author>();
		for (Author a : ids)
		{
			if (topicIds.contains(a.getId()))
				authors.add(new Author(a.getId(), a.getScore()));
		}
		authors.sort(null);
		return authors.toArray(new Author[0]);
	}

	/*
	 * generate a list of author ids filtered by the topic
	 */
	public List<Integer> processTopic(IndexSearcher searcher) throws NumberFormatException, IOException, ParseException
	{
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("topicid", analyzer);
		Query query = parser.parse(this.params.get("topic"));

		TopDocs hits = searcher.search(query, 8653);
		List<Integer> ids = new ArrayList<Integer>();
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs)
		{
			ids.add(Integer.parseInt(searcher.doc(scoreDoc.doc).get("id")));
		}
		return ids;
	}
}
