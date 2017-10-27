package edu.nl.tue.nips.basic;

/*
 * class to represent the author, will be serialized to json and returned to user request
 */
public class Author implements Comparable<Author>
{
	public float getScore()
	{
		return score;
	}

	public void setScore(float score)
	{
		this.score = score;
	}

	int id;
	float score;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}



	public Author(int id, float score)
	{
		super();
		this.id = id;
		this.score = score;
	}

	public Author()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Author o)
	{
		// TODO Auto-generated method stub
		if (score > o.score)
			return -1;
		if (score < o.score)
			return 1;
		return 0;
	}

}
