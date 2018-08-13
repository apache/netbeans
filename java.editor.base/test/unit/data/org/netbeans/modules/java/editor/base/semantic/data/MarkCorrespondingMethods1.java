package test;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Set;

public class MarkCorrespondingMethods1 extends AbstractList<String> implements CharSequence {
    
    public MarkCorrespondingMethods1() {
    }
    
    public String get(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean add(String arg0) {
        return super.add(arg0);
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public int length() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char charAt(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CharSequence subSequence(int arg0, int arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class Inner extends AbstractList<String> implements CharSequence {
        
        public String get(int arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int size() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public boolean add(String arg0) {
            return super.add(arg0);
        }
        
        public boolean add(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int length() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public char charAt(int arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public CharSequence subSequence(int arg0, int arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static class Map extends AbstractMap<String, String> implements Runnable, CharSequence {

        public Set<Entry<String, String>> entrySet() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int length() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public char charAt(int arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public CharSequence subSequence(int arg0, int arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
    public static abstract class X implements CharSequence {
        public @Override int hashCode() {
            return 0;
        }
    }
    
    public static interface Int {
        public int hashCode();
    }
    
    public static class Y implements Int {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
    
}
