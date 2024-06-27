package tp1;

import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.util.Chrono;

public class Collision {
    int k;            // Number of locations.
    int m;            // Number of items.
    double lambda;    // Theoretical expectation of C (asymptotic).
    boolean[] used;   // Locations already used.
    public Collision (int k, int m) {
        this.k = k;
        this.m = m;
        lambda = (double) m * m / (2.0 * k);
        used = new boolean[k];
    }
    // Generates and returns the number of collisions.
    public int generateC (RandomStream stream) {
        int C = 0;
        for (int i = 0; i < k; i++) used[i] = false;
        for (int j = 0; j < m; j++) {
            int loc = stream.nextInt (0, k-1);
            if (used[loc]) C++;
            else used[loc] = true;
        }
        return C;
    }
    // Performs n indep. runs using stream and collects statistics in statC.
    public void simulateRuns (int n, RandomStream stream,
                              Tally statC) {
        statC.init();
        for (int i=0; i<n; i++)
            statC.add(generateC (stream));
        statC.setConfidenceIntervalStudent();
        System.out.println (statC.report (0.95, 3));
        System.out.println (" Theoretical mean: "
                + lambda);
    }
    public static void main (String[] args) {
        Tally statC = new Tally ("Statistics on collisions");
        //TallyStore statCC=new TallyStore("Statistics on collisions");
        Collision col = new Collision (10000, 500);
        Chrono timer = new Chrono();
        col.simulateRuns (1000000, new MRG32k3a(), statC);
        System.out.println("Total CPU time:      " + timer.format() + "\n");
        statC.setConfidenceIntervalStudent();
        System.out.println(statC.report(0.95, 3));
        System.out.println ("Theoretical mean: lambda = " + col.lambda + "\n");

        // double data[]=statC.getArray();
        // HistogramChart hist= new HistogramChart("Distribution"," Nbre Collision",
        //		       "Frequence",data);
        // hist.view(600, 600);
    }
}

