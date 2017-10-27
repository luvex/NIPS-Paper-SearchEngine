package edu.nl.tue.nips.lucene.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/*
 * class to index the authors
 */
public class IndexAuthor
{
	Connection conn;

	public IndexAuthor()
	{
		// TODO Auto-generated constructor stub
		connect();
	}

	//initialize sqlite connection string
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

	public boolean run() throws IOException
	{

		String indexPath = "iresource//authorindex";
		boolean create = true;

		Directory dir = FSDirectory.open(Paths.get(indexPath));
		
		//define the standard analyzer, which will be used for tokenizing
		Analyzer analyzer = new StandardAnalyzer();
		//initialize index writer
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		if (create)
		{
			// Create a new index in the directory, removing any
			// previously indexed documents:
			iwc.setOpenMode(OpenMode.CREATE);
		}
		else
		{
			// Add new documents to an existing index:
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}
		IndexWriter writer = new IndexWriter(dir, iwc);

		//retrieve the authors from sqlite database
		String sql = "SELECT id, name FROM authors";
		try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(sql))
		{

			// loop through the result set
			while (rs.next())
			{
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String institution = "";
				String topicid = "";

				String authorsql = "select institutions.name from author_institution,institutions where author_institution.institution_id = institutions.id and author_institution.author_id="
						+ id;
				Statement stmt2 = this.conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(authorsql);
				while (rs2.next())
				{
					institution = rs2.getString("name");
				}
				rs2.close();
				stmt2.close();

				String topicsql = "select topic_id from paper_topic where paper_id in ( select paper_id from paper_authors    where author_id = "
						+ id + ")";
				Statement stmt3 = this.conn.createStatement();
				ResultSet rs3 = stmt3.executeQuery(topicsql);
				HashMap<Integer, Integer> map = new HashMap<>();
				while (rs3.next())
				{
					int tempid = rs3.getInt("topic_id");
					if (map.containsKey(tempid))
					{
						int temp = map.get(tempid);
						map.put(tempid, temp + 1);
					}
					else
					{
						map.put(tempid, 1);
					}
				}
				Collection<Integer> count = map.values();

				int maxCount = Collections.max(count);

				for (Map.Entry<Integer, Integer> entry : map.entrySet())
				{
					if (maxCount == entry.getValue())
					{
						topicid += (entry.getKey() + " ");
					}
				}

				//index the docs with all fields
				indexDoc(writer, id, name, institution, topicid);
			}
			rs.close();
			stmt.close();

			//write index to hard disk
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

	void indexDoc(IndexWriter writer, int id, String name, String institution, String topicid) throws IOException
	{

		// make a new, empty document
		Document doc = new Document();
		// add fields to the documents
		doc.add(new TextField("id", "" + id, Field.Store.YES));
		doc.add(new TextField("name", name, Field.Store.YES));
		doc.add(new TextField("institution", institution, Field.Store.YES));
		doc.add(new TextField("topicid", topicid, Field.Store.YES));

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
