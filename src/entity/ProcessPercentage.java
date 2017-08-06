package entity;

/**
 * Created by hw on 17/4/6.
 */
public class ProcessPercentage {

    private long fullsize;
    private long currentsize;




    private double calculatePercentage(){
        double result =  currentsize*100.0/fullsize;
        result = Math.round(result*100)/100;
        return result;
    }

    public double getPercentage(){
        return calculatePercentage();
    }

    public long getFullsize() {
        return fullsize;
    }

    public void setFullsize(long fullsize) {
        this.fullsize = fullsize;
    }

    public long getCurrentsize() {
        return currentsize;
    }

    public void setCurrentsize(long currentsize) {
        this.currentsize = currentsize;
    }
}
