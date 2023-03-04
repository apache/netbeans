/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * AbstractDataAggregation.java
 *
 * Created on October 8, 2002, 4:51 PM
 */

package org.netbeans.performance.spi;
import org.netbeans.performance.spi.html.*;
import java.util.*;
import java.io.*;
/** Convenience implementation of a data aggregation (a class
 * wrapping something such as a log file).  Data Aggregations
 * may contain other data aggregations;  the iterator() method
 * provides a way to iterate the elements of an aggregation and
 * all of its contained aggregations - the iterator().next() method
 * should never return an instance of AbstractDataAggregation, but
 * rather, LogElement objects representing entities inside of
 * itself or its contained aggregations.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractDataAggregation extends AbstractLogElement implements DataAggregation, Serializable {
    protected HashSet elements = new HashSet(20);

    //XXX FIXME - doing this by brute force for now.
    //Better would be to at least keep some hashtable,
    //and iterate children recursively.
    public LogElement findElement(String path) {
        /*
        System.out.println(toString() + "SEARCHING");
        Iterator ii = elements.iterator();
        while (ii.hasNext()) {
            System.out.println(ii.next());
        }
        System.out.println("DONE");
         */
        LogElement result = null;
        for (Iterator i = iterator(); i.hasNext();) {
            result = (LogElement) i.next();
            if (result.getPath().equals(path))
                return result;
        }
        return null;
    }
    
    /*
    public LogElement findElement(String path) {
        String myPath = getPath();
        LogElement result = null;
        if (myPath.startsWith (path)) {
            String firstChildName = pathFirstElement (path);
            LogElement next = findChild (firstChildName);
        }
        return result;
    }
     
    private final LogElement findChild (String name) {
        Iterator i = elements.iterator();
        if (i
    }
     
    private final String pathFirstElement (String path) {
        if (path.length() == 0) return null;
        int idx = path.indexOf ("/");
        if (idx == -1) return path;
        return path.substring (0, idx);
    }
     */
    
    /*
    public Iterator iterator() {
        Iterator result = new AggregateIterator();
        return result;
    } 
    */
    
    public List findElements(ElementFilter ef) {
        List result = new ArrayList();
        LogElement curr;
        for (Iterator i = iterator(); i.hasNext();) {
            curr = (LogElement) i.next();
            if (ef.accept(curr)) result.add(curr);
        }
        return result;
    }
    
    boolean hasNonAggregationElements=false;
    /** Method for adding elements to the underlying lists.
     *Throws IllegalArgumentException if an element is added to itself.
     *<I>Does not check for indirect recursive adds.</I>
     */
    protected void addElement(LogElement el) {
        if (el == this) {
            throw new IllegalArgumentException("Cannot add a DataAggregation to itself");
        }
        /*if (elements.contains (el)) 
            throw new IllegalArgumentException ("Already contain " + el); */
        if (el.getParent() != null) 
            throw new IllegalArgumentException (el + " already has a parent " + el.getParent());
        hasNonAggregationElements = hasNonAggregationElements || (!(el instanceof DataAggregation));
        elements.add(el);
        if (el instanceof AbstractLogElement) {
            ((AbstractLogElement) el).addNotify(this);
        }
    }
    
    public DataAggregation[] getChildAggregations() {
        checkParsed();
        Collection results;
        if (hasNonAggregationElements) {
            results = new ArrayList();
            Object curr=null;
            for (Iterator i=elements.iterator(); i.hasNext(); curr=i.next()) {
                if (curr instanceof DataAggregation) {
                    results.add(curr);
                }
            }
        } else {
            results = elements;
        }
        DataAggregation[] result = new DataAggregation[results.size()];
        result = (DataAggregation[]) results.toArray(result);
        return result;
    }
    
    public void writeToFile(String filename) throws java.io.IOException {
        File f = new File(filename);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(this);
        System.out.println("Serialized data written to: " + f);
    }
    
    public List getAllElements() {
        return Collections.unmodifiableList(new ArrayList(elements));
    }

    public LogElement[] query(String search) {
        checkParsed();
        ArrayList results=new ArrayList();
        LogElement curr=null;
        for (Iterator i = iterator(); i.hasNext();) {
            curr = (LogElement) i.next();
            if (matchesQueryString(search, curr.getPath()))
                results.add(curr);
        }
        LogElement[] result = new LogElement[results.size()];
        result = (LogElement[]) results.toArray(result);
        return result;
    }
    
    public LogElement[] query(String search, ElementFilter ef) {
        checkParsed();
        LogElement curr=null;
        ArrayList results=new ArrayList();
        for (Iterator i = iterator(); i.hasNext(); curr = (LogElement) i.next()) {
            if (matchesQueryString(search, curr.getPath()) && ef.accept(curr))
                results.add(curr);
        }
        LogElement[] result = new LogElement[results.size()];
        result = (LogElement[]) results.toArray(result);
        return result;
    }
    
    private static final boolean matchesQueryString(String s, String path) {
        StringTokenizer tk = new StringTokenizer(s, "*", false);
        String curr;
        int position=0;
        boolean result=true;
        boolean wildCardEnd = s.endsWith ("*");
        while (tk.hasMoreElements() && result) {
            curr = tk.nextToken();
            position = path.indexOf (curr, position);
            result = position != -1;
            boolean finished = (!(tk.hasMoreElements()));
            if (result && finished) {
                result = (position + curr.length() == path.length()) || 
                 (path.length() >= position + curr.length() &&
                 wildCardEnd);
            }
        }
        return result;
    }
    
    public LogElement findChild(String name) {
        checkParsed();
        AbstractLogElement curr;
        for (Iterator i=elements.iterator(); i.hasNext();) {
            curr = (AbstractLogElement) i.next();
            if (curr.name.equals(name)) return curr;
        }
        return null;
    }
  
    public Iterator iterator () {
        return new AggregateIterator();
    }
    
    public HTML toHTML () {
        checkParsed();
        HTMLTable result = new HTMLTable(name, 3, HTML.SINGLE_ROW);
        Iterator i = getAllElements().iterator();
        LogElement curr;
        while (i.hasNext()) {
            curr = (LogElement) i.next();
            result.add (curr.toHTML()); 
        }
        return result;
    }
    
    /*
    public String toString() {
        StringBuffer sb=new StringBuffer(elements.size() * 30);
        Iterator i = iterator();
        while (i.hasNext()) {
            sb.append (i.next());
            sb.append ("\n");
        }
    }
     */
    
    /**A hierarchy flattening iterator that, whenever it encounters
     * an instance of AbstractDataAggregation as the next object, defers
     * to that objects iterator until it has completed iterating.
     */
      class AggregateIterator implements Iterator {
        Stack stk = new Stack();
        Iterator curr=null;
        public AggregateIterator () {
            curr = elements.iterator();
        }
        
        private void nextIterator() {
            if (!stk.isEmpty()) {
                curr=(Iterator)stk.pop();
            } else {
                curr = null;
            }
        }
        
        public boolean hasNext() {
            if (curr == null) return false;
            boolean result = curr.hasNext();
            if (!result) {
                nextIterator();
                result = hasNext();
            }
            return result;
        }
        

        public Object next() {
            if (curr == null) return null;
            Object o = null;
            if (hasNext()) {
                o = curr.next();
            }
            if (o instanceof AbstractDataAggregation) {
                AbstractDataAggregation ada = (AbstractDataAggregation) o;
                if (ada instanceof AbstractLogFile) ada.checkParsed();
                Iterator it = ada.elements.iterator();
                if (curr.hasNext()) {
                    stk.push(it);
                } else {
                    curr = it;
                }
            }
            return o;
        }
        

        public void remove() {
            throw new UnsupportedOperationException("Cannot modify logged data");
        }
    }
  
}
