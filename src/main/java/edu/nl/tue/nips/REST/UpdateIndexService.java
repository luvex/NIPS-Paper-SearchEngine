package edu.nl.tue.nips.REST;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;

import edu.nl.tue.nips.lucene.index.IndexAuthor;
import edu.nl.tue.nips.lucene.index.IndexPapers;

@Path("/update")
public class UpdateIndexService
{


	@GET
	@Path("/para")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean[] getPaperJson(@Context UriInfo ui) throws IOException, ParseException
	{
		//update the index, the return is 2 boolean value
		//which represents if the indexing is successful respectively
		//for paper and author
		boolean[] b = new boolean[2];
		IndexPapers index = new IndexPapers();
		b[0]=  index.run();
		b[1]= new IndexAuthor().run();
		return b;
	}

}
