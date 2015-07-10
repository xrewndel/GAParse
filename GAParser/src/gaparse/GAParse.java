package gaparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Andrew
 */
public class GAParse {
    private final static Logger log = Logger.getLogger(GAParse.class.getSimpleName());
    private static String path = "";
    private static List<Stat> stats = new LinkedList<>();
    private final static String pattern = "ga-(\\d){4}-(\\d){2}-(\\d){2}(.*log)"; //ga-2014-10-24 17-18-21-cross_10-mutate_10.log
    
    public static void main(String[] args) {
        int cross = -1;
        int mutate = -1;
        int fr = -1;
        int wr = -1;
        Double bestFitness = 0d;
        int bestfr = fr;
        int bestwr = wr;
        String bestFile = "";
        boolean oldType = false;
        // set settings for log
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
        // set path
        if (args.length > 1) path = args[1];
        // лучше называть каталог с результатами осмысленно :)
        //String dir = path.substring(path.lastIndexOf("\\") + 1).replace("_", "");
        //System.setProperty("mydir", dir);
        File cfgFile = new File(args[0]);
        PropertyConfigurator.configure(cfgFile.getAbsolutePath());
        
        File[] files = getFilesInDir(path);
        for (File file : files) {
            log.info("File: " + file.getName());
            String[] t = file.getName().substring(23).replaceAll(".log", "").split("-");
            for (String t1 : t) {
                String[] q = t1.split("_");
                switch(q[0]) {
                    case "c": cross = Integer.valueOf(q[1]);        break;
                    case "cross": cross = Integer.valueOf(q[1]);    break;
                    case "m": mutate = Integer.valueOf(q[1]);       break;
                    case "mutate": mutate = Integer.valueOf(q[1]);  break;
                    case "fr": fr = Integer.valueOf(q[1]);          break;
                    case "wr": wr = Integer.valueOf(q[1]);          break;
                    default: System.out.println("Param " + q[0] + " is unknown");
                }
            }
            log.debug("Cross: " + cross);
            log.debug("Mutate: " + mutate);
            log.debug("Free rate: " + fr);
            log.debug("Waste rate: " + wr);
            
            // среднее считать - за сколько поколений в среднем достигается максимальнй фитнес - проще в расчетах писать и пихать в файл
            try {
                Map map = read(file.getAbsolutePath());
                log.debug("Map: " + map);
                List<Double> fitness = (List<Double>) map.get("fitness");
                log.info("Fitness: " + fitness);
                if (fr == -1) {
                    oldType = true;
                    double d = (double) map.get("fr");
                    fr = (int) (d * 100);
                    log.info("Free rate 2: " + fr);
                    double d2 = (double) map.get("wr");
                    wr = (int) (d2 * 100);
                    log.info("Waste rate 2: " + wr);
                } 
                
                for (Double fit : fitness) {
                    if (fit > bestFitness) {
                        bestFitness = fit;
                        bestFile = file.getName();
                        bestfr = fr;
                        bestwr = wr;
                    }
                }
                
                Stat stat = new Stat(cross, mutate, fr, wr);
                if (!stats.contains(stat)) { 
                    stat.add((List<Integer>)map.get("gen"), (List<Integer>)map.get("time"), (List<Double>)map.get("fitness"));
                    stats.add(stat); }
                else {
                    for (Stat st : stats) {
                        if (st.equals(stat)) {
                            st.add((List<Integer>)map.get("gen"), (List<Integer>)map.get("time"), (List<Double>)map.get("fitness"));
                        }
                    }
                }
                if (oldType) { fr = -1; wr = -1; }
            }catch (IOException ex) { log.info(ex);  }
            if (oldType) { fr = -1; wr = -1; }
        }
        
        Map<String, Double> mapGen = new HashMap<>();
        for (Stat st : stats) {
            log.debug("Stat: " + st.toString());
            log.debug("Sum: " + st.sumGen());
            log.debug("Avg: " + st.avgGen());
            mapGen.put(st.simpleid(), st.avgGen());
        }
        Map<String, Double> sortedMapGen = sortByComparator(mapGen);
        
        Map<String, Double> mapTime = new HashMap<>();
        for (Stat st : stats) mapTime.put(st.id(), st.avgTime());
        Map<String, Double> sortedMapTime = sortByComparator(mapTime);
        
        Map<String, Double> mapFitness = new HashMap<>();
        for (Stat st : stats) mapFitness.put(st.id(), st.avgFitness());
        Map<String, Double> sortedMapFitness = sortByComparator(mapFitness);
        
        log.debug("Map: " + mapGen);
        log.debug("");
        log.debug("Sorted by generation (from best to worst): "); printMap(sortedMapGen);
        log.debug("");
        log.debug("Sorted by time, sec (from best to worst): "); printMap(sortedMapTime);
        log.debug("");
        log.debug("Sorted by fitness (from worst to best): "); printMap(sortedMapFitness);
        log.info("Parsed files: " + files.length);
        log.info("Best fitness: " + bestFitness + ". File: " + bestFile);
    }
    
    public static File[] getFilesInDir(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override public boolean accept(File directory, String fileName) {
                //return fileName.endsWith(".log");
                return fileName.matches(pattern);
            }
        });
        
        return files;
    }
    
    private static Map sortByComparator(Map unsortMap) {
        List list = new LinkedList(unsortMap.entrySet());
 
        // sort list based on comparator
        Collections.sort(list, new Comparator() {
            @Override public int compare(Object o1, Object o2) {
                    return ((Comparable) ((Map.Entry) (o1)).getValue())
                           .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // put sorted list into map again
        //LinkedHashMap make sure order in which keys were inserted
        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    public static void printMap(Map<String, Double> map){
        log.debug("Cross|Mutation|FreeRate|WasteRate|Fitness");
        for (String s : map.keySet()) { log.debug(s + "       | " + map.get(s)); }
    }
    
    // Чтение локального файла построчно
    public static Map read(String filename) throws IOException {
        List<Integer> generationIdx = new ArrayList<>();
        List<Integer> time = new ArrayList<>();
        List<Double> fitness = new ArrayList<>();
        double fr = -1d;
        double wr = -1d;
        double bestFitness = 0d;
        int generation = 0;

        BufferedReader in = new BufferedReader(new FileReader(filename));
        while (in.ready()) {
            String s = in.readLine();
            //if (s.startsWith("2."))  generationIdx.add(Integer.valueOf(s.substring(s.indexOf(" ")).trim()));
            // парсим время
            if (s.startsWith("Exec time:")) {
                String[] minSec = s.substring(11).trim().replaceAll("m", "").replaceAll("sec", "").split(" ");
                int total = Integer.valueOf(minSec[0]) * 60 + Integer.valueOf(minSec[1]);
                time.add(total);
            }
            
            // парсим пары значений фитнес поколение и выбираем запоминаем лучий фитнес и поколение на котором он появился
            if (s.length() > 2 && s.charAt(1) == '.') {
                String[] arr = s.split(" ");
                double fit = Double.valueOf(arr[0]);
                int gen = Integer.valueOf(arr[1]);
                if (fit > bestFitness) {
                    bestFitness = fit;
                    generation = gen;
                }
            }
            //if (s.startsWith("Fitness"))  fitness.add(Double.valueOf(s.substring(s.indexOf("=") + 1).trim()));
            if (s.startsWith("Mutate free rate")) fr = Double.valueOf(s.substring(s.indexOf(":") + 1).trim());
            if (s.startsWith("Mutate waste rate")) wr = Double.valueOf(s.substring(s.indexOf(":") + 1).trim());
        }
        in.close();
        generationIdx.add(generation);
        fitness.add(bestFitness);

        Map map = new HashMap<>();
        map.put("gen", generationIdx);
        map.put("time", time);
        map.put("fitness", fitness);
        map.put("fr", fr);
        map.put("wr", wr);
        return map;
    }
    
    public static String arrToStr(String[] arr) {
        StringBuilder sb = new StringBuilder();
        if (arr.length % 2 == 0)
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i % 2 == 0) sb.append("\n");
                else sb.append(": ");
            }
        else 
            for (String arr1 : arr) {  sb.append(arr1).append(" "); }
        return sb.toString();
    }
}
