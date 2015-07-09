package gaparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Andrew
 */
public class GAParse {
    private final static Logger log = Logger.getLogger(GAParse.class.getSimpleName());
    private static String path = "";
    private static Map<Integer, Stat> stats = new HashMap<>();
    private static Double bestFitness = 0d;
    private static String bestFile = "";
    //private final static String strPattern = "(\\d)(.)(\\d+)( )(\\d){1,3}(.?)";
    //private final static String fitGen = "(\\d.\\d+)( ){1}(\\d+)";
    public static Pattern fitGeneneration = Pattern.compile("(\\d.\\d+)( ){1}(\\d+)");  // "0.43493720518041723 3"
    public static Pattern finalFitness = Pattern.compile("(^Fitness)(\\d)( = )(.*)()"); // "Fitness2 = 0.7878027422306279"
    public static Pattern flow = Pattern.compile("(^Fitness flow )(\\d )(.*)()"); // Fitness flow 4 40% 4 stat update = 0.5597242201790585
    public static Pattern best = Pattern.compile("(^Fitness best )(\\d )(.*)()"); // Fitness best 1 = 0.44003893124571014
    public static Pattern evo = Pattern.compile("(----Evolution_)(\\d )(.*)()"); // [genetic.Jgap.evoCycleAdd(Jgap.java:147)]:----Evolution_1
    private static FinalFitness ff = new FinalFitness();
    
    public static void main(String[] args) {
//        String q = "0.43493720518041723 3";
//        Matcher matcher = fitGeneneration.matcher(q);
//        if (matcher.find( )) {
//            for (int i = 0; i < matcher.groupCount(); i++) {
//                System.out.println("Group " + i + ":" + matcher.group(i));
//            }
//        }
//        else System.out.println("not found");
//        System.exit(0);
        
//        String q = "Fitness1 = 0.7166359805765001";
//        Pattern pattern = Pattern.compile("(^Fitness)(\\d)( = )(.*)()");
//        Matcher matcher = pattern.matcher(q);
//        if (matcher.find( )) {
//            for (int i = 0; i < matcher.groupCount(); i++) {
//                System.out.println("Group " + i + ":" + matcher.group(i));
//            }
//            
//            int fitNum = Integer.valueOf(matcher.group(2));
//            double fitness = Double.valueOf(matcher.group(4));
//        }
//        else System.out.println("not found");
//        System.exit(0);
        
        // set settings for log
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
        // set path
        if (args.length > 1) path = args[1];
        // лучше называть каталог с результатами осмысленно :)
        File cfgFile = new File(args[0]);
        PropertyConfigurator.configure(cfgFile.getAbsolutePath());
        
        File[] files = getFilesInDir(path);
        for (File file : files) {
            // среднее считать - за сколько поколений в среднем достигается максимальнй фитнес - проще в расчетах писать и пихать в файл
            try {
                read(file.getAbsolutePath());
                System.out.println("Stats: " + stats.size());
            }catch (IOException ex) { log.info(ex);  }
        }
        
        System.out.println("bestFitness: " + bestFitness);
        System.out.println("bestFile:\n" + bestFile + " : "); //NetBeans link to file
        
        //for (Integer id : stats.keySet()) {
        for (int i = 0; i < stats.size(); i++) {
            Stat stat = stats.get(i);
            //System.out.println(stat);
            //System.out.println(id + ". SumFit: " + stat.sumFit() + ". SumUrg: " + stat.sumUrgent());
            //System.out.println(id + ". AvgFit: " + stat.avgFit() + ". avgUrg: " + stat.avgUrgent());
            log.info(stat.avgFit() + " " + stat.id());
        }
        
        System.out.println("FinalFitness");
        System.out.println("Validation1: " + ff.validation1());
        System.out.println("Validation2: " + ff.validation2());
        if (ff.valid()) {
            log.info("Diff1: " + ff.diff());
            Map<String, Double> m = ff.diff2();
            for (String s : m.keySet()) {
                log.info(s + ": " + m.get(s));
                System.out.println(s + ": " + m.get(s));
            }
            ff.diffPositive();
            
        }
        
        System.out.println(ff.csv());
        System.out.println(ff);
    }
    
    public static File[] getFilesInDir(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".log");
                //return fileName.matches(pattern);
            }
        });
        
        return files;
    }
    
    // Чтение локального файла построчно
    public static void read(String filename) throws IOException {
        ff.add(filename);
        BufferedReader in = new BufferedReader(new FileReader(filename));
        while (in.ready()) {
            String s = in.readLine();
            Matcher fitGenMatch = fitGeneneration.matcher(s);
            Matcher finalFitnessMatch = finalFitness.matcher(s);
            if (fitGenMatch.find()) {
                //System.out.println(s);
                int id = Integer.valueOf(fitGenMatch.group(3));
                double fit = Double.valueOf(fitGenMatch.group(1));

                if (fit > bestFitness) {
                    bestFitness = fit;
                    bestFile = filename;
                }

                if (stats.containsKey(id)) 
                    stats.get(id).add(fit);
                else 
                    stats.put(id, new Stat(id, fit));
            }
            
            if (finalFitnessMatch.find()) {
                //System.out.println(s);
                //Group 0:Fitness2 = 0.7878027422306279
                //Group 1:Fitness
                //Group 2:2
                //Group 3: = 
                //Group 4:0.7878027422306279
                int fitNum = Integer.valueOf(finalFitnessMatch.group(2));
                double fitness = Double.valueOf(finalFitnessMatch.group(4));
                ff.add(fitNum, fitness);
            }
        }
        in.close();
    }
}
