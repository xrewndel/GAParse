package gaparse;

import static gaparse.GAParse.cleanLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;

/**
 *
 * @author Andrew
 */
public class Flow {
    public Map<Integer, List<Data>> flow = new TreeMap<>();
    Map<String, List<Data>> dataByFile = new TreeMap<>();
    public Map<Integer, List<Double>> best = new TreeMap<>();
    public Set<Integer> procs = new TreeSet<>();
    private String currFile;
    
    public enum PROC {
        e2_1 (new ArrayList<>(Arrays.asList(100,50))),
        e2_2 (new ArrayList<>(Arrays.asList(100,66,33))),
        e2_3 (new ArrayList<>(Arrays.asList(100,75,25,50))),
        e2_4 (new ArrayList<>(Arrays.asList(100,80,60,40,20))),
        e2_5 (new ArrayList<>(Arrays.asList(100,80,64,48,32,16)));
        
        private final List<Integer> proc; PROC(List<Integer> p) { proc = p; }
        public List<Integer> $() { return proc; }
        
        
    }
    
    public Flow() {}
    public void addFlow(Matcher m) {
        Data data = new Data(m.group(2), m.group(4), m.group(5), m.group(7), currFile);
        if (!flow.containsKey(data.flowNum())) flow.put(data.flowNum(), new ArrayList<Data>());
        flow.get(data.flowNum()).add(data);
        
        procs.add(data.proc());
        
        if (!dataByFile.containsKey(currFile)) dataByFile.put(currFile, new ArrayList<Data>());
        dataByFile.get(currFile).add(data);
    }
    
    public void addBest(Matcher m) { 
        int flowNum = Integer.valueOf(m.group(2));
        if (!best.containsKey(flowNum)) best.put(flowNum, new ArrayList<Double>());
        best.get(flowNum).add(Double.valueOf(m.group(4)));
    }
    
    public int flowSize() { return flow.size(); }
    public int bestSize() { return best.size(); }
    public void setFile(String file) { currFile = file; }
    
    public void clean() {
        Map<String, List<Data>> cleanData = new TreeMap<>();
        List<Integer> flowNums = new ArrayList<>(flow.keySet());
        //int flowDiff = flowNums.get(1) - flowNums.get(0);
        cleanLog.debug("flowNums: " + flowNums);
        
        List<Integer> procNums = new ArrayList<>(procs);
        cleanLog.debug("procNums: " + procNums);
        //int procDiff = procNums.get(1) - procNums.get(0);
        
        for (String file : dataByFile.keySet()) {
            cleanData.put(file, new ArrayList<Data>());
            
            for (int i = 0, j = procNums.size() - 1; i < flowNums.size(); i++, j--) {
                cleanLog.debug("flowNums: " + flowNums.get(i));
                cleanLog.debug("proc: " + procNums.get(j));
                for (Data d : dataByFile.get(file)) {
                    if (d.flowNum() == flowNums.get(i) && d.proc() == procNums.get(j)) {
                        cleanData.get(file).add(d);
                        //break;
                    }
                }
            }
        }
        
        for (String file : cleanData.keySet()) {
            cleanLog.debug(file);
            for (Data d : cleanData.get(file)) {
                cleanLog.debug(d.asLog());
            }
        }
    }
    
    // flow : <proc : sum>
    public Map<Integer, Map<Integer, Double>> avgFlow() {
        Map<Integer, Map<Integer, Double>> res = new TreeMap<>();
        // proc : procSize
        Map<Integer, Integer> proc_DataNum = new TreeMap<>(); // кол-во data с определенным proc
        
        for (Integer flowNum : flow.keySet()) {
            if (!res.containsKey(flowNum)) res.put(flowNum, new TreeMap<Integer, Double>());
            for (Data d : flow.get(flowNum)) {
                if (!res.get(flowNum).containsKey(d.proc())) 
                    res.get(flowNum).put(d.proc(), 0d);
                
                if (!proc_DataNum.containsKey(d.proc())) proc_DataNum.put(d.proc(), 0);
                int procNum = proc_DataNum.get(d.proc());
                procNum++;
                proc_DataNum.put(d.proc(), procNum);
                
                double sum = res.get(flowNum).get(d.proc());
                sum += d.fitness();
                res.get(flowNum).put(d.proc(), sum);
            }
        }
        
        System.out.println("res: " + res);
        System.out.println("proc_DataNum: " + proc_DataNum);
        
        // calc avg
        // flow : <proc : sum>
        Map<Integer, Map<Integer, Double>> avg = new TreeMap<>();
        for (Integer flowNum : res.keySet()) {
            if (!avg.containsKey(flowNum)) avg.put(flowNum, new TreeMap<Integer, Double>());
            for (Integer proc : res.get(flowNum).keySet()) {
                
                double average = res.get(flowNum).get(proc) / proc_DataNum.get(proc);
                System.out.println("avg: " + average);
                Map<Integer, Double> avgVal = new TreeMap<>();
                avgVal.put(proc, average);
                avg.get(flowNum).putAll(avgVal);
            }
        }
        
        return avg;
    }
    
    public Map<Integer, Double> sumBest() { 
        Map<Integer, Double> res = new HashMap<>();
        for (Integer flowNum : best.keySet()) {
            double sum = 0d;
            for (Double d : best.get(flowNum)) sum += d;
            
            res.put(flowNum, sum);
        }
        
        return res;
    }
    
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
    
}

class Data {
    private int flowNum; 
    private int proc;
    private int evoCycle;
    private double fitness;
    private String file;
    
    Data(int n, int p, int e, double fit, String fl) { flowNum = n; proc = p; evoCycle = e; fitness = fit; file = fl;}
    Data(String n, String p, String e, String fit, String fl) { 
        flowNum = Integer.valueOf(n); 
        proc = Integer.valueOf(p); 
        evoCycle = Integer.valueOf(e); 
        fitness = Double.valueOf(fit); 
        file = fl;
    }
    
    public int flowNum() { return flowNum; }
    public double fitness() { return fitness; }
    public int proc() { return proc; }
    public String file() { return file; }
    
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
    
    public String asLog() {
        return "Fitness flow " + flowNum + " " + proc + "% " + evoCycle + " stat update = " + fitness;
    }
}
