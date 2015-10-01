package modkiwi.games.util;

import modkiwi.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;

public abstract class VoteTracker
{
	private class Vote implements Comparable<Vote>
	{
		String voter;
		int votee;
		int index;

		public Vote(String voter, int votee)
		{
			this.voter = voter;
			this.votee = votee;
			this.index = nextVote++;
		}

		public boolean isLatest()
		{
			return this == latestVotes.get(voter);
		}

		@Override
		public int compareTo(Vote v)
		{
			return Integer.compare(index, v.index);
		}

		@Override
		public String toString()
		{
			if (isLatest())
				return voter + " (" + index + ")";
			else
				return "[-]" + voter + " (" + index + ")[/-]";
		}
	}

	private class VoteOption implements Comparable<VoteOption>
	{
		int votee;
		int numVotes;
		int lastVote;
		PriorityQueue<Vote> votes;

		public VoteOption(Vote v)
		{
			votee = v.votee;
			numVotes = 0;
			lastVote = 0;
			this.votes = new PriorityQueue<Vote>();
			addVote(v);
		}

		public void addVote(Vote v)
		{
			if (v.isLatest())
			{
				numVotes++;
				lastVote = Math.max(lastVote, v.index);
			}

			votes.add(v);
		}

		@Override
		public int compareTo(VoteOption vo)
		{
			if (this.numVotes > vo.numVotes)
				return -1;
			else if (this.numVotes < vo.numVotes)
				return 1;
			else if (this.lastVote < vo.lastVote)
				return -1;
			else if (this.lastVote > vo.lastVote)
				return 1;
			else
				return 0;
		}
	}

	private Map<String, Vote> latestVotes;
	private List<Vote> votes;
	private int nextVote;

	public VoteTracker()
	{
		latestVotes = new HashMap<String, Vote>();
		votes = new LinkedList<Vote>();
		nextVote = 1;
	}

	public abstract String getVotee(int index);

	public void vote(String voter, int votee)
	{
		Vote v = new Vote(voter, votee);
		votes.add(v);
		latestVotes.put(voter, v);
	}

	public void reset()
	{
		nextVote = 1;
		latestVotes.clear();
		votes.clear();
	}

	public List<String> voters()
	{
		return null;
	}

	public CharSequence getVotes()
	{
		Map<Integer, VoteOption> tally = new HashMap<Integer, VoteOption>();
		for (Vote v : votes)
		{
			if (tally.get(v.votee) == null)
				tally.put(v.votee, new VoteOption(v));
			else
				tally.get(v.votee).addVote(v);
		}

		StringBuilder output = new StringBuilder();

		List<VoteOption> votees = new ArrayList<VoteOption>(tally.values());
		Collections.sort(votees);

		output.append("[u]Vote Tally:[/u]");

		for (VoteOption vo : votees)
		{
			output.append("\n");
			output.append(getVotee(vo.votee));
			output.append(" - " + vo.numVotes + " - ");
			output.append(Utils.join(vo.votes, ", "));
		}

		List<String> nonVoters = voters();
		if (nonVoters != null)
		{
			ListIterator<String> li = nonVoters.listIterator(0);
			while (li.hasNext())
			{
				if (latestVotes.get(li.next()) != null)
					li.remove();
			}

			Collections.sort(nonVoters);

			output.append("\nNot voting: " + Utils.join(nonVoters, ", "));
		}

		return output;
	}
}
