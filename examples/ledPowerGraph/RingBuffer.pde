
import java.util.*;
 
public class RingBuffer {
    private float[][] buffer; 
    private final int len; // buffer length
    private int nItems = 0;  // items currently stored
    private int head = 0;
 
    public RingBuffer(int sz) {
        len = sz;
        buffer = new float[sz][2];
    }
    
    void reset() {
      nItems = 0;
      head = 0;
    }
    
    int size() {
      return nItems;
    }
    
    void setAt(int i, float curr,float avg) {
      buffer[i][0] = curr;
      buffer[i][1] = avg;
    }
    
    void add(float curr,float avg) {
       setAt(head,curr,avg);       
       head = (head + 1) % len;
       nItems = min(len,nItems + 1);
    }
    
    // get item at ring buffer relative index
    // (0 is the current tail, len - 1 is the current head)
    void getAt(int i, float[] val) {
      int n = (getTailIndex() + i) % len;
      try {
        val[0] = buffer[n][0];
        val[1] = buffer[n][1];
      }
      catch (Exception e) {
        println("nItems, head ", nItems,head);
        println("Tail ",getTailIndex());
      }
    }
       
    int getTailIndex() {
      return (nItems < len) ? 0 : (nItems - 1 + head) % len;
    }       
}
