package test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class GenericBoundIsClassUse<T extends List> {
    
    public static <T extends ArrayList> void test1(T t) {
    }
    
    public static <T extends HashMap & Comparator> void test1(T t) {
    }
    
}
