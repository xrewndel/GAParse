package gaparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 *
 * @author Andrew
 */
public class Flow {
    public Map<Integer, List<Data>> flow = new HashMap<>();
    public Map<Integer, List<Double>> best = new HashMap<>();
    
    public Flow() {}
    public void addFlow(Matcher m) {
        //Group 0:Fitness flow 2 100% 5 stat update = 0.6811394218976246
        //Group 1:Fitness flow 
        //Group 2:2
        //Group 3: 
        //Group 4:100
        //Group 5:5
        //Group 6: stat update = 
        //Group 7:0.6811394218976246
        //Data(int n, double p, int e, double f) { num = n; proc = p; evoCycle = e; fitness = f; }
        Data data = new Data(m.group(2), m.group(4), m.group(5), m.group(7));
        if (!flow.containsKey(data.flowNum())) flow.put(data.flowNum(), new ArrayList<Data>());
        flow.get(data.flowNum()).add(data);
    }
    
    //Group 0:Fitness best 5 = 0.6371098605787752
    //Group 1:Fitness best 
    //Group 2:5
    //Group 3: = 
    //Group 4:0.6371098605787752
    public void addBest(Matcher m) { 
        int flowNum = Integer.valueOf(m.group(2));
        if (!best.containsKey(flowNum)) best.put(flowNum, new ArrayList<Double>());
        best.get(flowNum).add(Double.valueOf(m.group(4)));
    }
    
    public int flowSize() { return flow.size(); }
    public int bestSize() { return best.size(); }
    
    
//    public Map<Integer, Double> sumFit() { 
//        Map<Integer, Double> res = new HashMap<>();
//        for (Integer fitNum : fit.keySet()) {
//            double sum = 0d;
//            for (Double d : fit.get(fitNum)) 
//                sum += d;
//            
//            res.put(fitNum, sum / fit.get(fitNum).size());
//        }
//        
//        return res;
//    }
    
//    public Map<String, Double> diff() { 
//        Map<String, Double> res = new HashMap<>();
//        int prevFitNum = -1;
//        Map<Integer, Double> sum = sumFit();
//        Set<Integer> sort = new TreeSet<>(sum.keySet());
//        for (Integer fitNum : sort) {
//            if (prevFitNum == -1) {
//                prevFitNum = fitNum;
//                continue;
//            }
//            
//            double d = sum.get(fitNum) - sum.get(prevFitNum);
//            res.put(fitNum + "-" + prevFitNum, d);
//        }
//        
//        return res; 
//    }
    
//    public Map<String, Double> diff2() { 
//        Map<String, Double> res = new HashMap<>();
//        double maxDiff = 0;
//        String maxDiffFile = "";
//        double sum = 0d;
//        double sumPositive = 0d;
//        for (String file : fileFitness.keySet()) {
//            int prevFitNum = -1;
//            Set<Integer> sort = new TreeSet<>(fileFitness.get(file).keySet());
//            for (Integer fitNum : sort) {
//                if (prevFitNum == -1) {
//                    prevFitNum = fitNum;
//                    continue;
//                }
//                double d = fileFitness.get(file).get(fitNum) - fileFitness.get(file).get(prevFitNum);
//                if (d < 0) System.out.println(file + ": "  + d);
//                if (d > 0) sumPositive += d;
//                if (d > maxDiff) {
//                    maxDiff = d;
//                    maxDiffFile = file;
//                }
//                
//                sum += fileFitness.get(file).get(fitNum) - fileFitness.get(file).get(prevFitNum);
//            }
//        }
//        
//        System.out.println("fileFitness size: " + sum / fileFitness.size());
//        System.out.println("fileFitness positive: " + sumPositive / fileFitness.size());
//        System.out.println("fileFitness: " + fileFitness.size());
//        
//        for (Integer i : fit.keySet()) {
//            System.out.println("fit" + i +": " + fit.get(i).size());
//            System.out.println("fit" + i +": " + sum / fit.get(i).size());
//            System.out.println("fit" + i +" positive: " + sumPositive / fit.get(i).size());
//        }
//        
//        
//        res.put(maxDiffFile, maxDiff);
//        res.put("Total diff", sum);
//
//        return res; 
//    }
//    
//    public Map<String, Double> diffPositive() { 
//        Map<String, Double> res = new HashMap<>();
//        int prevFitNum = -1;
//        Map<Integer, Double> sum = sumFit();
//        Set<Integer> sort = new TreeSet<>(sum.keySet());
//        for (Integer fitNum : sort) {
//            if (prevFitNum == -1) {
//                prevFitNum = fitNum;
//                continue;
//            }
//            
//            double d = sum.get(fitNum) - sum.get(prevFitNum);
//            if (d < 0) System.out.println("d: "  + d);
//            res.put(fitNum + "-" + prevFitNum, d);
//        }
//        
//        return res; 
//    }
//    
//    public boolean valid () { return validation1() && validation2(); }
//    
//    public String csv() {
//        StringBuilder sb = new StringBuilder();
//        int idx = 0;
//        for (String s : fileFitness.keySet()) {
//            for (Integer i : fileFitness.get(s).keySet()) 
//                sb.append(fileFitness.get(s).get(i)).append(";");
//            
//            sb.append(idx).append("\n");
//            idx++;
//        }
//        
//        return sb.toString();
//    }
    
//    @Override public String toString() {
//        StringBuilder sb = new StringBuilder();
//        for (String s : fileFitness.keySet()) sb.append(s).append(": ").append(fileFitness.get(s)).append("\n");
//        for (Integer i: fit.keySet()) sb.append(i).append(": ").append(fit.get(i)).append("\n");
//        
//        return sb.toString();
//    }
}

class Data {
    private int flowNum; 
    private int proc;
    private int evoCycle;
    private double fitness;
    
    Data(int n, int p, int e, double f) { flowNum = n; proc = p; evoCycle = e; fitness = f; }
    Data(String n, String p, String e, String f) { 
        flowNum = Integer.valueOf(n); 
        proc = Integer.valueOf(p); 
        evoCycle = Integer.valueOf(e); 
        fitness = Double.valueOf(f); 
    }
    
    public int flowNum() { return flowNum; }
    
    @Override public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.flowNum;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.proc) ^ (Double.doubleToLongBits(this.proc) >>> 32));
        hash = 97 * hash + this.evoCycle;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.fitness) ^ (Double.doubleToLongBits(this.fitness) >>> 32));
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Data other = (Data) obj;
        if (this.flowNum != other.flowNum) return false;
        if (Double.doubleToLongBits(this.proc) != Double.doubleToLongBits(other.proc)) return false;
        if (this.evoCycle != other.evoCycle) return false;
        return Double.doubleToLongBits(this.fitness) == Double.doubleToLongBits(other.fitness);
    }
    
    @Override public String toString() {
        return "Data{" + "flowNum=" + flowNum + ", proc=" + proc + ", evoCycle=" + evoCycle + ", fitness=" + fitness + '}';
    }
}
