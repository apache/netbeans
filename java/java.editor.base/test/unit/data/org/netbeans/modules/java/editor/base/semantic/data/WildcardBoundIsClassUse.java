package test;

import java.text.Collator;
import java.util.Iterator;
import java.util.Locale;

public class WildcardBoundIsClassUse {
    
    public Iterable<? extends Iterator> test1() {
        return null;
    }
    
    public Iterable<Locale> test2() {
        return null;
    }
    
    public Iterable<? super Collator> test3() {
        return null;
    }
    
}
