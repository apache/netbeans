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
 * Gc.java
 *
 * Created on October 8, 2002, 4:14 PM
 */

package org.netbeans.performance.impl.logparsing;
import java.util.*;
import org.netbeans.performance.spi.*;
/**Represents a garbage collection event found in a log file
 * when verbose garbage collection is turned on (<code> java -verbose:gc</code>)
 *
 * @author  Tim Boudreau
 */
public class Gc extends AbstractLogElement {
    private float seconds = 0;
    private long memoryCollected = 0;
    private long initialMemory = 0;
    private long resultingMemory = 0;
    private long heap = 0;
    private boolean isFull = false;
    public static long lastHeapSize = 0;
    private long heapDelta = 0;

    public Gc (String s) {
        super (s);
    }

    protected void parse () throws ParseException {
        String s = line;
        isFull = s.startsWith("Full");
        if (s.startsWith("GC ")) {
            line = line.substring(3);
        } else {
            s=s.substring(8);
        }
        StringTokenizer sk = new StringTokenizer(s, ",", false);
        String mempart = sk.nextToken().trim();
        String timepart = sk.nextToken().trim();
        timepart = timepart.substring(0, timepart.indexOf('s') -1).trim();
        String beforePart;
        String afterPart;
        String heapSize;
        if (s.indexOf ("->") != -1) {
            sk = new StringTokenizer(mempart, "->", false);
            beforePart = sk.nextToken().trim();
            beforePart = beforePart.substring(0, beforePart.length() -1);
            afterPart = sk.nextToken().trim();
            int idx = afterPart.indexOf("(");
            heapSize = afterPart.substring(idx+1, afterPart.indexOf(")")-1); //xxx assuming always ends with "K"
            afterPart = afterPart.substring(0, idx-1); //eliminate the "K" here too
            /*
                System.out.println("beforepart " + beforePart);
                System.out.println("afterpart " + afterPart);
                System.out.println("heapsize " + heapSize);
                System.out.println("isfull " + isFull);
             */
        } else {
            beforePart = "  00";
            afterPart = "0";
            int idx = s.indexOf ("(") + 1;
            int idx2 = s.indexOf (")") -1;
            heapSize = s.substring (idx, idx2);
        }
        
        seconds = Float.parseFloat(timepart);
        
        beforePart = beforePart.substring (3);
        
        initialMemory = Long.parseLong(beforePart);
        resultingMemory = Long.parseLong(afterPart);
        heap = Long.parseLong(heapSize);
        heapDelta = heap - lastHeapSize;
        memoryCollected = initialMemory - resultingMemory;
    }
    
    public String toString() {
        return line;
    }
    
    /** Returns the GC entry or null if not a GC entry.  */
    //XXX delete this method - just cruft from copying
    public static Gc createGc(String s) {
        if ((!(s.startsWith("GC "))) && (!(s.startsWith("Full")))) {
            return null;
        } else {
            int i = s.indexOf("]");
            if (i==-1) return null;
            String initstring = s.substring(0, i);
            return new Gc(initstring);
        }
    }
    
    /** Getter for property memoryCollected.
     * @return Value of property memoryCollected.
     *
     */
    public long getMemoryCollected() {
        checkParsed();
        return memoryCollected;
    }
    
    /** Returns the amount of memory (in Kb) used
     * <i>after</i> the garbage collection event completed.*/
    public long getResultingMemory() {
        checkParsed();
        return resultingMemory;
    }
    
    /** Returns the 
     * @return the heap size    */
    public long getHeapDelta() {
        checkParsed();
        return heapDelta;
    }
    
    /** Returns the amount of memory (in Kb) used
     * <i>before</i> the garbage collection started.*/
    public long getInitialMemory () {
        checkParsed();
        return initialMemory;
    }
    
    /** Returns the duration in seconds of the garbage
     * completion event. */
    public float getSeconds () {
        checkParsed();
        return seconds;
    }
    
    /** Returns true if this was a &quot;full&quot; garbage
     * collection event (i.e. the old generation was collected) */
    public boolean isFull () {
        checkParsed();
        return isFull;
    }
    
    /** The heap size after the event completed.  If it is changed,
     * the result of heapDelta() will be non-zero. */
    public long getHeapSize() {
        checkParsed();
        return heap;
    }
    
}

