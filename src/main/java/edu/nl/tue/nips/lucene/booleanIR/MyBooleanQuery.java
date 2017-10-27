package edu.nl.tue.nips.lucene.booleanIR;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import edu.nl.tue.nips.basic.Paper;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/*
 * the implementation of boolean query
 */
public class MyBooleanQuery
{
	List<BooleanClause> booleanClauses = new ArrayList<BooleanClause>();

	/*
	 * intialize all the boolean query terms
	 */
	public MyBooleanQuery( Map<String, List<String>> params)
	{
		super();
		// TODO Auto-generated constructor stub
		if (params.get("or") != null)
			for (String s : params.get("or"))
			{
				booleanClauses.add(new BooleanClause(new TermQuery(new Term("contents", s)),
						BooleanClause.Occur.SHOULD));
			}
		if (params.get("no") != null)
			for (String s : params.get("no"))
			{
				booleanClauses.add(new BooleanClause(new TermQuery(new Term("contents", s)),
						BooleanClause.Occur.MUST_NOT));
			}
		if (params.get("must") != null)
			for (String s : params.get("must"))
			{
				booleanClauses.add(new BooleanClause(new TermQuery(new Term("contents", s)),
						BooleanClause.Occur.MUST));
			}
	}

	public List<Paper> compute() throws IOException
	{
		//read index from file system
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("iresource//paperindex")));
		IndexSearcher searcher = new IndexSearcher(reader);

		//formulate the boolean query
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (BooleanClause booleanClause : booleanClauses)
		{
			builder.add(booleanClause);
		}
		BooleanQuery query = builder.build();
		
		//compute the related docs with rankings
		TopDocs hits = searcher.search(query, 6560);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		List<Paper> papers= new ArrayList<Paper>();
		for (ScoreDoc scoreDoc : scoreDocs)
		{
			Paper p = new Paper();
			p.setId(Integer.parseInt(searcher.doc(scoreDoc.doc).get("id")));
			p.setScore(scoreDoc.score);
			p.setPaperName(searcher.doc(scoreDoc.doc).get("title"));
			papers.add(p);
		}
		return papers;
	}
}
