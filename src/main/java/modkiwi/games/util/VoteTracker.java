package modkiwi.games.util;

import modkiwi.util.Logger;
import modkiwi.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
			return this == latestVotes.get(voter.toLowerCase());
		}

        public boolean isLocked()
        {
            return isLatest() && VoteTracker.this.isLocked(voter);
        }

		@Override
		public int compareTo(Vote v)
		{
			return Integer.compare(index, v.index);
		}

		@Override
		public String toString()
		{
            if (isLocked())
				return "r{" + voter + "* (" + index + ")}r";
            else if (isLatest())
				return voter + " (" + index + ")";
			else
				return "[-]" + voter + " (" + index + ")[/-]";
		}

        public void replace(String oldName, String newName)
        {
            if (voter.equals(oldName))
            {
                LOGGER.finest("replacing voter '%s' in vote %d with '%s'", oldName, index, newName);
                voter = newName;
            }
        }
	}

	public class VoteOption implements Comparable<VoteOption>
	{
		private int votee;
		private int numVotes, numLockedVotes;
		private int lastVote;
		private PriorityQueue<Vote> votes;

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
                if (v.isLocked())
                    numLockedVotes++;
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

        public String getTarget()
        {
            return getVotee(votee);
        }

        public boolean isMajority(boolean countEqual)
        {
            if (voters() == null)
                return false;

            int total = voters().size();

            if (numVotes > total / 2)
                return true;

            if (countEqual && numVotes == total / 2)
                return true;

            return false;
        }

        public boolean isLockedMajority(boolean countEqual)
        {
            if (voters() == null)
                return false;

            int total = voters().size();

            if (numLockedVotes > total / 2)
                return true;

            if (countEqual && numLockedVotes * 2 == total)
                return true;

            return false;
        }
	}

    private static final Logger LOGGER = new Logger(VoteTracker.class);

	private Map<String, Vote> latestVotes;
    private Set<String> lockedVotes;
	private List<Vote> votes;
	private int nextVote;

	public VoteTracker()
	{
		latestVotes = new HashMap<String, Vote>();
        lockedVotes = new HashSet<String>();
		votes = new LinkedList<Vote>();
		nextVote = 1;
	}

	public abstract String getVotee(int index);

	public boolean vote(String voter, int votee)
	{
        if (isLocked(voter))
            return false;

		Vote v = new Vote(voter, votee);
		votes.add(v);
		latestVotes.put(voter.toLowerCase(), v);
        return true;
	}

    public void unvote(String voter)
    {
        latestVotes.remove(voter.toLowerCase());
    }

    public void lock(String voter)
    {
        lockedVotes.add(voter.toLowerCase());
    }

    public void unlock(String voter)
    {
        lockedVotes.remove(voter.toLowerCase());
    }

    public boolean isLocked(String voter)
    {
        return lockedVotes.contains(voter.toLowerCase());
    }

    public boolean isVoting(String voter)
    {
        return latestVotes.get(voter.toLowerCase()) != null;
    }

	public void reset()
	{
		nextVote = 1;
		latestVotes.clear();
        lockedVotes.clear();
		votes.clear();
	}

	public List<String> voters()
	{
		return null;
	}

    public List<VoteOption> getVoteList()
    {
		Map<Integer, VoteOption> tally = new HashMap<Integer, VoteOption>();
		for (Vote v : votes)
		{
			if (tally.get(v.votee) == null)
				tally.put(v.votee, new VoteOption(v));
			else
				tally.get(v.votee).addVote(v);
		}

		List<VoteOption> list = new ArrayList<VoteOption>(tally.values());
		Collections.sort(list);

        return list;
    }

	public CharSequence getVotes()
	{
		StringBuilder output = new StringBuilder();

        List<VoteOption> list = getVoteList();

		output.append("[u]Vote Tally:[/u]");

		for (VoteOption vo : list)
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
				if (isVoting(li.next()))
					li.remove();
			}

			Collections.sort(nonVoters);

			output.append("\nNot voting: " + Utils.join(nonVoters, ", "));
		}

		return output;
	}

    public VoteOption getLL()
    {
        List<VoteOption> list = getVoteList();
        if (list == null || list.isEmpty())
            return null;
        return list.get(0);
    }

    public void replace(String oldName, String newName)
    {
        LOGGER.finer("replacing %s with %s", oldName, newName);
        for (Vote v : votes)
            v.replace(oldName, newName);

        Vote v = latestVotes.get(oldName.toLowerCase());
        if (v != null)
        {
            latestVotes.put(newName.toLowerCase(), v);
            latestVotes.remove(oldName.toLowerCase());

            if (isLocked(oldName))
            {
                unlock(oldName);
                lock(newName);
            }
        }
    }
}
