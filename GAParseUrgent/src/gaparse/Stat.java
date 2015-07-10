package gaparse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Stat {
    private final int id;
    private List<Double> fitness = new ArrayList<>();
    private List<Double> urgent = new ArrayList<>();

    public Stat(int cr, double fit, double urg) { id = cr; add(fit, urg); }
    public void add(double fit, double urg) { fitness.add(fit); urgent.add(urg); }
    public double sumFit() { 
        double sum = 0d;
        for (Double i : fitness) { sum += i; }
        return sum;
    }
    public double sumUrgent() { 
        double sum = 0d;
        for (Double i : urgent) { sum += i; }
        return sum;
    }
    
    public double avgFit() { return (double)sumFit() / fitness.size(); }
    public double avgUrgent() { return (double)sumUrgent() / urgent.size(); }
    public int size() { return fitness.size(); }

    @Override public String toString() { return id + ": " + fitness + "(" + urgent + ")"; }
    public int id() { return id; }

    @Override public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.id;
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Stat other = (Stat) obj;
        return this.id == other.id;
    }
}
