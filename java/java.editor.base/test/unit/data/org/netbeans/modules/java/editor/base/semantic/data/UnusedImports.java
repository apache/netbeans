package test;
import java.beans.*; import static java.lang.Math.min; import static java.lang.Math.max; import static java.lang.System.*; import java.lang.reflect.*;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import javax.swing.JTable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import java.util.Date;

public class UnusedImports<E, F> extends JTable implements Serializable {
    
    Entry e;
    
    private List list;
    
    private static final List l = Collections.EMPTY_LIST;
    
    public static void main(String[] args) throws BadLocationException {
        Set<Iterator> l;
        Collections.<Date>emptyList();
        
        Constructor c;
        max(1, 2);
    }
    
}
