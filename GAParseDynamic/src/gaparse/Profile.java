package gaparse;

import java.util.Objects;

/**
 *
 * @author ARazumovskiy
 */
public class Profile {
    boolean fix;
    boolean cross;
    boolean mutate;
    double cp;
    double mp;
    double fr;
    double wr;
    double nc;
    String ncfg;
    String experiment;
    int cspd;
    
    long hash;
    
    //Profile:fix=true;c=true;m=true;cp=0.2;mp=0.76;fr=0.1;wr=0.1;nc=0.1;ncfg=fully;exp=Experiment2_4;cspd=1;hash=1108524259
    public Profile(String s) {
        s = s.substring(s.indexOf(":"));
        String[] str = s.split(";");
        for (String paramValue : str) {
            String[] vals = paramValue.split("=");
            switch(vals[0]) {
                case "fix": fix = Boolean.valueOf(vals[1]); break;
                case "c": cross = Boolean.valueOf(vals[1]); break;
                case "m": mutate = Boolean.valueOf(vals[1]); break;
                case "cp": cp = Double.valueOf(vals[1]); break;
                case "mp": mp = Double.valueOf(vals[1]); break;
                case "fr": fr = Double.valueOf(vals[1]); break;
                case "wr": wr = Double.valueOf(vals[1]); break;
                case "nc": nc = Double.valueOf(vals[1]); break;
                case "ncfg": ncfg = vals[1]; break;
                case "exp": experiment = vals[1]; break;
                case "cspd": cspd = Integer.valueOf(vals[1]); break;
                case "hash": hash = Long.valueOf(vals[1]); break;
            }
        }
    }
    
    @Override public int hashCode() {
        int hash = 3;
        //hash = 53 * hash + (this.fix ? 1 : 0);
        hash = 53 * hash + (this.cross ? 1 : 0);
        hash = 53 * hash + (this.mutate ? 1 : 0);
        //hash = 53 * hash + (int) (Double.doubleToLongBits(this.cp) ^ (Double.doubleToLongBits(this.cp) >>> 32));
        //hash = 53 * hash + (int) (Double.doubleToLongBits(this.mp) ^ (Double.doubleToLongBits(this.mp) >>> 32));
        //hash = 53 * hash + (int) (Double.doubleToLongBits(this.fr) ^ (Double.doubleToLongBits(this.fr) >>> 32));
        //hash = 53 * hash + (int) (Double.doubleToLongBits(this.wr) ^ (Double.doubleToLongBits(this.wr) >>> 32));
        //hash = 53 * hash + Objects.hashCode(this.ncfg);
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.nc) ^ (Double.doubleToLongBits(this.nc) >>> 32));
        hash = 53 * hash + Objects.hashCode(this.experiment);
        hash = 53 * hash + this.cspd;
        return hash;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Profile other = (Profile) obj;
        //if (this.fix != other.fix) return false;
        if (this.cross != other.cross) return false;
        if (this.mutate != other.mutate) return false;
        //if (Double.doubleToLongBits(this.cp) != Double.doubleToLongBits(other.cp)) return false;
        //if (Double.doubleToLongBits(this.mp) != Double.doubleToLongBits(other.mp)) return false;
        //if (Double.doubleToLongBits(this.fr) != Double.doubleToLongBits(other.fr)) return false;
        //if (Double.doubleToLongBits(this.wr) != Double.doubleToLongBits(other.wr)) return false;
        //if (!Objects.equals(this.ncfg, other.ncfg)) return false;
        if (Double.doubleToLongBits(this.nc) != Double.doubleToLongBits(other.nc)) return false;
        if (!Objects.equals(this.experiment, other.experiment)) return false;
        return this.cspd == other.cspd;
    }
}
