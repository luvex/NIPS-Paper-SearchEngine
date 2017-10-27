package edu.nl.tue.nips.basic;

import javax.xml.bind.annotation.XmlRootElement;
/*
 * class to represent paper, then it will be serialized and returned to user request
 */
@XmlRootElement
public class Paper
{
	int id;
	String title;
	float score;

	public float getScore()
	{
		return score;
	}

	public void setScore(float score)
	{
		this.score = score;
	}

	public Paper(int id, String title, float score)
	{
		super();
		this.id = id;
		this.title = title;
		this.score = score;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getPaperName()
	{
		return title;
	}

	public void setPaperName(String title)
	{
		this.title = title;
	}

	public Paper()
	{
		// TODO Auto-generated constructor stub
	}

}
