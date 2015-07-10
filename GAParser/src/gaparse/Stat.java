package gaparse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Stat {
    private int cross;
    private int mutate;
    private int freeRate;
    private int wasteRate;
    private List<Integer> generation = new ArrayList<>();
    private List<Integer> time = new ArrayList<>();
    private List<Double> fitness = new ArrayList<>();

    public Stat(int cr, int mut, int fr, int wr) { cross = cr; mutate = mut;  freeRate = fr; wasteRate = wr; }
//    public Stat(int cr, int mut, int gen) {
//        cross = cr;
//        mutate = mut;
//        generation.add(gen);
//    }

    public void add(List<Integer> g, List<Integer> t, List<Double> f) { generation.addAll(g); time.addAll(t); fitness.addAll(f); }
    public int sumGen() { 
        int sum = 0;
        for (Integer i : generation) { sum += i; }
        return sum;
    }
    public int sumTime() { 
        int sum = 0;
        for (Integer i : time) { sum += i; }
        return sum;
    }
    
    public double sumFitness() { 
        double sum = 0d;
        for (Double i : fitness) { sum += i; }
        return sum;
    }

    public double avgGen() { return (double)sumGen() / generation.size(); }
    public double avgTime() { return (double)sumTime() / time.size(); }
    public double avgFitness() { return (double)sumFitness() / fitness.size(); }
    public int size() { return generation.size(); }

    @Override public String toString() { return simpleid() + ": " + generation.size() + "(" + generation + ")"; }
    public String simpleid() { return cross + "|" + mutate + "|" + freeRate + "|" + wasteRate; }
    public String id() { return cross + "   |" + mutate + "       |" + freeRate + "      |" + wasteRate; }

    @Override public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.cross;
        hash = 17 * hash + this.mutate;
        hash = 17 * hash + this.freeRate;
        hash = 17 * hash + this.wasteRate;
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Stat other = (Stat) obj;
        if (this.cross != other.cross) { return false; }
        if (this.mutate != other.mutate) { return false; }
        if (this.freeRate != other.freeRate) { return false; }
        if (this.wasteRate != other.wasteRate) { return false; }
        return true;
    }
}
