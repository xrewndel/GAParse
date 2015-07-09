package gaparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author Andrew
 */
public class GAParse {
    public final static Logger log = Logger.getLogger(LOG.main.$());
    private static String path = "";
    private static Map<Integer, Stat> stats = new HashMap<>();
    private static Double bestFitness = 0d;
    private static String bestFile = "";
    //private final static String strPattern = "(\\d)(.)(\\d+)( )(\\d){1,3}(.?)";
    //private final static String fitGen = "(\\d.\\d+)( ){1}(\\d+)";
    public static Pattern fitGeneneration = Pattern.compile("(\\d.\\d+)( ){1}(\\d+)");  // "0.43493720518041723 3"
    public static Pattern finalFitness = Pattern.compile("(^Fitness)(\\d)( = )(.*)()"); // "Fitness2 = 0.7878027422306279"
    // Fitness flow 4 40% 4 stat update = 0.5597242201790585
    public static Pattern flowPtrn = Pattern.compile("(Fitness flow )(\\d)( )(\\d\\d\\d?)% (\\d)( stat update = )(.*)()"); 
    // Fitness best 1 = 0.44003893124571014
    public static Pattern bestPtrn = Pattern.compile("(Fitness best )(\\d)( = )(.*)()"); 
    public static Pattern evoPtrn = Pattern.compile("(----Evolution_)(\\d )(.*)()"); // [genetic.Jgap.evoCycleAdd(Jgap.java:147)]:----Evolution_1
    private static FinalFitness ff = new FinalFitness();
    private static Flow flow = new Flow();
    
    public static void main(String[] args) {
        if (args.length > 0) path = args[0]; // set path
        else {
            System.out.println("Specify dir with log");
            System.exit(1);
        }
        // лучше называть каталог с результатами осмысленно :)
        LOG.init();
        
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
        
        for (int i = 0; i < stats.size(); i++) {
            Stat stat = stats.get(i);
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
            Matcher flowMatch = flowPtrn.matcher(s);
            Matcher bestMatch = bestPtrn.matcher(s);
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
                int fitNum = Integer.valueOf(finalFitnessMatch.group(2));
                double fitness = Double.valueOf(finalFitnessMatch.group(4));
                ff.add(fitNum, fitness);
            }
            
            if (flowMatch.find()) {
                System.out.println("match flow: " + s);
                //for (int i = 0; i < flowMatch.groupCount(); i++) {
                //    System.out.println("Group " + i + ":" + flowMatch.group(i));
                //}
                //System.out.println("");
                flow.addFlow(flowMatch);
            }
            
            if (bestMatch.find()) {
                System.out.println("match best: " + s);
//                for (int i = 0; i < bestMatch.groupCount(); i++) {
//                    System.out.println("Group " + i + ":" + bestMatch.group(i));
//                }
//                System.out.println("");
                flow.addBest(bestMatch);
            }
        }
        in.close();
    }
    
    private static final String PATTERN = "%m%n";
    private static final String PATTERN1 = "%d [%c] %-5p %m%n"; // default log4j pattern
    public enum LOG {
        main    ("main",    PATTERN),
        out     ("out",     PATTERN);
        
        private final String log;  
        private final String pattern;  
        LOG(String s, String p) { log = s; pattern = p; }
        
        public String $() { return log; }
        private String pattern() { return pattern; }
        private String logFile() { return "./log/gaparseDynamic" + "-" + new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()) + ".log"; }
        private static final List<LOG> undefLog = new ArrayList<>(Arrays.asList(LOG.values()));
        static { undefLog.remove(out); }
        
        public static void init() {
            Level dbg = Level.DEBUG;
            Logger rootLogger = Logger.getRootLogger();
            if (rootLogger.getAllAppenders().hasMoreElements()) {
                System.out.println("Init log config");
                Logger.getRootLogger().getLoggerRepository().resetConfiguration();
                rootLogger.setLevel(dbg);
            }
            //ConsoleAppender console = new ConsoleAppender(); //create appender
            //console.setLayout(new PatternLayout(PATTERN)); 
            //console.setThreshold(dbg);
            //console.activateOptions();
            //rootLogger.addAppender(console);

            for (LOG log : undefLog) {
                FileAppender fa = new FileAppender();
                fa.setName(log.$());
                fa.setFile(log.logFile());
                fa.setLayout(new PatternLayout(log.pattern()));
                fa.setThreshold(dbg);
                fa.setAppend(true);
                fa.activateOptions();
                
                Logger pkgLogger = rootLogger.getLoggerRepository().getLogger(log.$());
                pkgLogger.setLevel(dbg);
                pkgLogger.addAppender(fa);
            }
        }
    }
}
