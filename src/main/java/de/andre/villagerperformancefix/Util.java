package de.andre.villagerperformancefix;

import java.util.Collections;
import java.util.List;

public class Util {
    public static Main main = null;
    public static <T> void reArrange(List<T> list, int from, int to){
        if(from != to){
            if(from > to)
                reArrange(list,from -1, to);
            else
                reArrange(list,from +1, to);

            Collections.swap(list, from, to);
        }
    }
}
