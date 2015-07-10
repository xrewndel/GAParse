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
    private static Double bestUrgent = 0d;
    private static String bestFile = "";
    
    public static void main(String[] args) {
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
        System.out.println("bestFile   : " + bestFile);
        System.out.println("bestUrgent : " + bestUrgent);
        
        for (Integer id : stats.keySet()) {
            Stat stat = stats.get(id);
            //System.out.println(stat);
            //System.out.println(id + ". SumFit: " + stat.sumFit() + ". SumUrg: " + stat.sumUrgent());
            //System.out.println(id + ". AvgFit: " + stat.avgFit() + ". avgUrg: " + stat.avgUrgent());
            log.info(stat.avgFit() + " " + stat.avgUrgent() + " " + stat.id());
        }
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
        BufferedReader in = new BufferedReader(new FileReader(filename));
        while (in.ready()) {
            //String s = in.readLine();
            String[] data = in.readLine().split(" ");
            int id = Integer.valueOf(data[2]);
            double fit = Double.valueOf(data[0]);
            double urg = Double.valueOf(data[1]);
            
            if (fit > bestFitness) {
                bestFitness = fit;
                bestFile = filename;
            }
            if (urg > bestUrgent) bestUrgent = urg;
            
            if (stats.containsKey(id)) 
                stats.get(id).add(fit, urg);
            else 
                stats.put(id, new Stat(id, fit, urg));
        }
        in.close();
    }
}
