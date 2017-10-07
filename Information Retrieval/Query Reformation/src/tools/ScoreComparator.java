package tools;

import java.util.Comparator;
import containers.Score;

public class ScoreComparator implements Comparator<Score> {
	@Override
	// THIS SHIT IS IN REVERSE ORDER SO YOU CAN SORT FROM HIGHEST SCORE TO LOWEST SCORE
	public int compare(Score s1, Score s2) {
        if (s1.score() > s2.score()) {
            return -1;
        }
        if (s1.score() < s2.score()) {
            return 1;
        }
		return 0;
	}
}