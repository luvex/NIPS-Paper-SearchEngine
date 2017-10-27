package edu.nl.tue.nips.lucene.fuzzy;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import edu.nl.tue.nips.basic.Paper;

/*
 * implementation of fuzzy search
 */
public class MyFuzzySearch
{
	String sentence;

	//initialize the query sentence
	public MyFuzzySearch(String sentence)
	{
		// TODO Auto-generated constructor stub
		this.sentence = sentence;
	}

	public List<Paper> compute() throws IOException
	{
		//read the index from local file system
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("iresource//paperindex")));
		IndexSearcher searcher = new IndexSearcher(reader);

		//formulate the fuzzy query
		Query fuzzyQuery = new FuzzyQuery(new Term("contents", sentence), 1);

		//calculate the result docs
		TopDocs hits = searcher.search(fuzzyQuery, 20);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		List<Paper> papers= new ArrayList<Paper>();
		
		//return an array of papers with attributes
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
