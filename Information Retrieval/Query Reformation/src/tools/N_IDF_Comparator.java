package tools;

import java.util.Comparator;

import containers.Triple;

public class N_IDF_Comparator implements Comparator<Triple> {
	@Override
	// THIS SHIT IS IN REVERSE ORDER SO YOU CAN SORT FROM HIGHEST SCORE TO LOWEST SCORE
	public int compare(Triple t1, Triple t2) {
        if (t1.n_idf() > t2.n_idf()) {
            return -1;
        }
        if (t1.n_idf() < t2.n_idf()) {
            return 1;
        }
		return 0;
	}
}
