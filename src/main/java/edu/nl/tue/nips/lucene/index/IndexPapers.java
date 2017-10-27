package edu.nl.tue.nips.lucene.index;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.sqlite.JDBC;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/*
 * class to index the papers
 */
public class IndexPapers
{
	Connection conn;

	public IndexPapers()
	{
		connect();
	}

	//initialize the sqlite connection
	private void connect()
	{
		// SQLite connection string
		String url = "jdbc:sqlite:iresource//nips.db";
		try
		{
			Class.forName("org.sqlite.JDBC");
			this.conn = DriverManager.getConnection(url);
		}
		catch (SQLException | ClassNotFoundException e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Index all text files under a directory.
	 * 
	 * @throws IOException
	 */
	public boolean run() throws IOException
	{
		String indexPath = "iresource//paperindex";
		boolean create = true;

		Directory dir = FSDirectory.open(Paths.get(indexPath));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		if (create)
		{
			// Create a new index in the directory, removing any
			// previously indexed documents: iwc.setOpenMode(OpenMode.CREATE);
		}
		else
		{
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}
		IndexWriter writer = new IndexWriter(dir, iwc);

		String sql = "SELECT id, year, paper_text,title FROM papers";
		try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(sql))
		{

			// loop through the result set
			while (rs.next())
			{
				int id = rs.getInt("id");
				int year = rs.getInt("year");
				String paper_text = rs.getString("paper_text");
				String authors = "";
				String title = rs.getString("title");
				int topicid = -1;

				String authorsql = "select authors.name from authors, paper_authors where paper_authors.author_id=authors.id and paper_authors.paper_id="
						+ id;
				Statement stmt2 = this.conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(authorsql);
				while (rs2.next())
				{
					authors += (rs2.getString("name") + " ");
				}
				rs2.close();
				stmt2.close();
				String topicsql = "select topic_id from paper_topic where paper_id=" + id;
				Statement stmt3 = this.conn.createStatement();
				ResultSet rs3 = stmt3.executeQuery(topicsql);
				while (rs3.next())
				{
					topicid = rs3.getInt("topic_id");
				}

				indexDoc(writer, id, year, paper_text, authors, topicid, title);
			}

			rs.close();
			stmt.close();
			
			writer.forceMerge(1);
			writer.close();
			return true;
		}
		catch (SQLException e)
		{
			System.out.println(e.getMessage());
		}
		return false;
	}

	/** Indexes a single document */
	static void indexDoc(IndexWriter writer, int id, int year, String content, String authors, int topic_id,
			String title) throws IOException
	{

		// make a new, empty document
		Document doc = new Document();

		// add fields to the document
		doc.add(new TextField("id", "" + id, Field.Store.YES));
		doc.add(new TextField("topicid", "" + topic_id, Field.Store.YES));
		Field authorid = new TextField("name", authors, Field.Store.YES);		
		doc.add(authorid);
		Field yearid = new TextField("years", "" + year, Field.Store.YES);
		doc.add(yearid);
		doc.add(new TextField("contents", content, Field.Store.NO));
		doc.add(new TextField("title", title, Field.Store.YES));

		if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
		{
			// New index, so we just add the document (no old document can
			// be there):
			writer.addDocument(doc);
		}
		else
		{
			// Existing index (an old copy of this document may have been
			// indexed) so
			// we use updateDocument instead to replace the old one matching
			// the exact
			// path, if present:
			writer.updateDocument(new Term("id", "" + id), doc);
		}

	}
}
