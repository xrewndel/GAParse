package gaparse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Andrew
 */
public class Flow {
//    private int num; 
//    private double proc;
//    private int evoCycle;
//    private double fitness;
    private Map<Integer, List<Data>> data = new HashMap<>();
    
    //1 100% 5 stat update = 0.29171835907708427
    
    public Flow() {}
    public void add(String filename) {
        if (currFile.isEmpty())
            fileFitness.put(filename, new HashMap<Integer, Double>());
        else {
            validateCurrFile();
            fileFitness.put(filename, new HashMap<Integer, Double>());
        }
        currFile = filename;
    }
    
    private void validateCurrFile() {
        if (fileFitness.containsKey(currFile))
            if (fileFitness.get(currFile).size() < 2) {// удаляем файл если предыдущий не заполнен до конца (расчет прервался и не был окончен)
                fileFitness.remove(currFile);
                System.out.println(currFile + ": not ended");
            }
    }
    
    public void add(int fitNum, double fitVal) { 
        //Map<Integer, List<Double>> m = fileFitness.get(currFile);
        //m.put(fitNum, Arrays.asList(fitVal));
        //fileFitness.get(currFile).put(fitNum, Arrays.asList(fitVal)); // проверить 
        if (!Double.isInfinite(fitVal)) {
            fileFitness.get(currFile).put(fitNum, fitVal); // проверить 

            if (fit.containsKey(fitNum)) 
                fit.get(fitNum).add(fitVal);
            else
                fit.put(fitNum, new LinkedList(Arrays.asList(fitVal)));
        }
        //for (String s : fileFitness.keySet()) System.out.println(s + ": " + fileFitness.get(s));
        //for (Integer i: fit.keySet()) System.out.println(i + ": " + fit.get(i));
    }
    
    // сверяем размеры списков
    public boolean validation1() {
        int prevSize = -1;
        
        for (Integer fitNum : fit.keySet()) {
            if (prevSize == -1)
                prevSize = fit.get(fitNum).size();
            
            if (prevSize != fit.get(fitNum).size()) 
                return false;
            
            prevSize = fit.get(fitNum).size();
        }
        
        return true;
    }
    
    // сверяем размеры списков
    public boolean validation2() {
        validateCurrFile();
        for (String file : fileFitness.keySet()) {
            if (fileFitness.get(file).size() < 2) {
                System.out.println(file + ": " + fileFitness.get(file));
                return false;
            }
        }
        
        return true;
    }
    
    public Map<Integer, Double> sumFit() { 
        Map<Integer, Double> res = new HashMap<>();
        for (Integer fitNum : fit.keySet()) {
            double sum = 0d;
            for (Double d : fit.get(fitNum)) 
                sum += d;
            
            res.put(fitNum, sum / fit.get(fitNum).size());
        }
        
        return res;
    }
    
    public Map<String, Double> diff() { 
        Map<String, Double> res = new HashMap<>();
        int prevFitNum = -1;
        Map<Integer, Double> sum = sumFit();
        Set<Integer> sort = new TreeSet<>(sum.keySet());
        for (Integer fitNum : sort) {
            if (prevFitNum == -1) {
                prevFitNum = fitNum;
                continue;
            }
            
            double d = sum.get(fitNum) - sum.get(prevFitNum);
            res.put(fitNum + "-" + prevFitNum, d);
        }
        
        return res; 
    }
    
    public Map<String, Double> diff2() { 
        Map<String, Double> res = new HashMap<>();
        double maxDiff = 0;
        String maxDiffFile = "";
        double sum = 0d;
        double sumPositive = 0d;
        for (String file : fileFitness.keySet()) {
            int prevFitNum = -1;
            Set<Integer> sort = new TreeSet<>(fileFitness.get(file).keySet());
            for (Integer fitNum : sort) {
                if (prevFitNum == -1) {
                    prevFitNum = fitNum;
                    continue;
                }
                double d = fileFitness.get(file).get(fitNum) - fileFitness.get(file).get(prevFitNum);
                if (d < 0) System.out.println(file + ": "  + d);
                if (d > 0) sumPositive += d;
                if (d > maxDiff) {
                    maxDiff = d;
                    maxDiffFile = file;
                }
                
                sum += fileFitness.get(file).get(fitNum) - fileFitness.get(file).get(prevFitNum);
            }
        }
        
        System.out.println("fileFitness size: " + sum / fileFitness.size());
        System.out.println("fileFitness positive: " + sumPositive / fileFitness.size());
        System.out.println("fileFitness: " + fileFitness.size());
        
        for (Integer i : fit.keySet()) {
            System.out.println("fit" + i +": " + fit.get(i).size());
            System.out.println("fit" + i +": " + sum / fit.get(i).size());
            System.out.println("fit" + i +" positive: " + sumPositive / fit.get(i).size());
        }
        
        
        res.put(maxDiffFile, maxDiff);
        res.put("Total diff", sum);

        return res; 
    }
    
    public Map<String, Double> diffPositive() { 
        Map<String, Double> res = new HashMap<>();
        int prevFitNum = -1;
        Map<Integer, Double> sum = sumFit();
        Set<Integer> sort = new TreeSet<>(sum.keySet());
        for (Integer fitNum : sort) {
            if (prevFitNum == -1) {
                prevFitNum = fitNum;
                continue;
            }
            
            double d = sum.get(fitNum) - sum.get(prevFitNum);
            if (d < 0) System.out.println("d: "  + d);
            res.put(fitNum + "-" + prevFitNum, d);
        }
        
        return res; 
    }
    
    public boolean valid () { return validation1() && validation2(); }
    
    public String csv() {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (String s : fileFitness.keySet()) {
            for (Integer i : fileFitness.get(s).keySet()) 
                sb.append(fileFitness.get(s).get(i)).append(";");
            
            sb.append(idx).append("\n");
            idx++;
        }
        
        return sb.toString();
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : fileFitness.keySet()) sb.append(s).append(": ").append(fileFitness.get(s)).append("\n");
        for (Integer i: fit.keySet()) sb.append(i).append(": ").append(fit.get(i)).append("\n");
        
        return sb.toString();
    }
}

class Data {
    private int num; 
    private double proc;
    private int evoCycle;
    private double fitness;
    
    Data(int n, double p, int e, double f) { num = n; proc = p; evoCycle = e; fitness = f; }
    
    @Override public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.num;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.proc) ^ (Double.doubleToLongBits(this.proc) >>> 32));
        hash = 97 * hash + this.evoCycle;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.fitness) ^ (Double.doubleToLongBits(this.fitness) >>> 32));
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Data other = (Data) obj;
        if (this.num != other.num) return false;
        if (Double.doubleToLongBits(this.proc) != Double.doubleToLongBits(other.proc)) return false;
        if (this.evoCycle != other.evoCycle) return false;
        return Double.doubleToLongBits(this.fitness) == Double.doubleToLongBits(other.fitness);
    }
}
