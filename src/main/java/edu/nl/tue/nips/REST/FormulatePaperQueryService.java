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

import edu.nl.tue.nips.basic.Paper;
import edu.nl.tue.nips.lucene.VSR.MyVSRSearch;
import edu.nl.tue.nips.lucene.booleanIR.MyBooleanQuery;
import edu.nl.tue.nips.lucene.fuzzy.MyFuzzySearch;
import edu.nl.tue.nips.lucene.parametric.ParametricSearch;

/*
 * the paper query is composed of 2 parts
 * one if the parametric search as filter
 * the other one is the boolean/VSR/fuzzy search
 * we combine the results in the end
 */
@Path("/getpaper")
public class FormulatePaperQueryService
{

	@GET
	@Path("/para")
	@Produces(MediaType.APPLICATION_JSON)
	public Paper[] getPaperJson(@Context UriInfo ui) throws IOException, ParseException
	{
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

		Map<String, String> filterParams = new HashMap<String, String>();
		if (queryParams.getFirst("author") != null)
		{
			filterParams.put("author", queryParams.getFirst("author"));
		}
		if (queryParams.getFirst("bYear") != null)
		{
			filterParams.put("bYear", queryParams.getFirst("bYear"));
		}
		if (queryParams.getFirst("eYear") != null)
		{
			filterParams.put("eYear", queryParams.getFirst("eYear"));
		}
		if (queryParams.getFirst("topic") != null)
		{
			filterParams.put("topic", queryParams.getFirst("topic"));
		}
		if (queryParams.getFirst("mix") != null)
		{
			filterParams.put("mix", queryParams.getFirst("mix"));
		}
		else
		{
			filterParams.put("mix", "and");
		}

		List<Integer> filter = new ParametricSearch(filterParams).compute();

		List<Paper> papers = new ArrayList<Paper>();

		//if there is no concrete search, we can only use the results of parametric search
		if(queryParams.getFirst("searchType")==null)
		{
			for(Integer i :filter)
			{
				papers.add(new Paper(i, "", 0f));
			}
			return papers.toArray(new Paper[0]);
		}
		//boolean query
		if (queryParams.getFirst("searchType").equals("boolean"))
		{
			Map<String, List<String>> params = new HashMap<String, List<String>>();
			params.put("or", queryParams.get("or"));
			params.put("no", queryParams.get("no"));
			params.put("must", queryParams.get("must"));
			MyBooleanQuery query = new MyBooleanQuery(params);
			papers.addAll(query.compute());
		}
		//vector space retrieval
		else if (queryParams.getFirst("searchType").equals("VSR"))
		{
			String sentence = queryParams.getFirst("sentence");
			MyVSRSearch vs = new MyVSRSearch(sentence);
			papers.addAll(vs.compute());
		}
		//fuzzy search
		else if (queryParams.getFirst("searchType").equals("fuzzy"))
		{
			String sentence = queryParams.getFirst("sentence");
			MyFuzzySearch fs = new MyFuzzySearch(sentence);
			papers.addAll(fs.compute());
		}

		//filter the final results
		if(filterParams.size()==1)
			return papers.toArray(new Paper[0]);
		else
		{
			List<Paper> result = new ArrayList<Paper>();
			for (Paper p : papers)
			{
				if(filter.contains(p.getId()))
					result.add(p);
			}
			return result.toArray(new Paper[0]);
		}
	}

}
