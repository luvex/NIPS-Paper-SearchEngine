package edu.nl.tue.nips.REST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;

import edu.nl.tue.nips.basic.Author;
import edu.nl.tue.nips.basic.Result;
import edu.nl.tue.nips.lucene.VSR.MyVSRSearch;
import edu.nl.tue.nips.lucene.booleanIR.MyBooleanQuery;
import edu.nl.tue.nips.lucene.fuzzy.MyFuzzySearch;
import edu.nl.tue.nips.lucene.query.author.AuthorSearch;

/*
 * api of author query, the user should at most 4 parameters to formulate a query
 */
@Path("/getauthor")
public class FormulateAuthorQueryService
{

	@GET
	@Path("/para")
	@Produces(MediaType.APPLICATION_JSON)
	public Author[] getPaperJson(@Context UriInfo ui) throws IOException, ParseException
	{
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		Map<String, String> params = new HashMap<String, String>();

		if (queryParams.getFirst("name") != null)
		{
			params.put("name", queryParams.getFirst("name"));
		}
		if (queryParams.getFirst("inst") != null)
		{
			params.put("inst", queryParams.getFirst("inst"));
		}
		if (queryParams.getFirst("topic") != null)
		{
			String topic = "";
			for (String s : queryParams.get("topic"))
				topic += (s + ",");
			params.put("topic", topic.substring(0, topic.length() - 1));
		}
		if (queryParams.getFirst("mix") != null)
		{
			params.put("mix", queryParams.getFirst("mix"));
		}
		else
		{
			params.put("mix", "and");
		}

		return new AuthorSearch(params).compute();
	}

}
