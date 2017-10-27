package edu.nl.tue.nips.lucene.VSR;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.nl.tue.nips.basic.Paper;

/*
 * implementation of vector space retrieval
 */
public class MyVSRSearch
{
	String sentence;

	public MyVSRSearch(String sentence)
	{
		// TODO Auto-generated constructor stub
		this.sentence = sentence;
	}

	public List<Paper> compute() throws ParseException, IOException
	{
		//read index
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("iresource//paperindex")));
		IndexSearcher searcher = new IndexSearcher(reader);

		//initialize the query parser
		String field = "contents";
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser(field, analyzer);
		Query query = parser.parse(sentence);

		//calculate the results
		TopDocs results = searcher.search(query, 6560);
		ScoreDoc[] hits = results.scoreDocs;
		List<Paper> papers= new ArrayList<Paper>();
		
		//formulate the result array
		for (ScoreDoc scoreDoc : hits)
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
