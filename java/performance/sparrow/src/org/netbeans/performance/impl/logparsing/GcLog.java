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
 * GcLog.java
 *
 * Created on October 8, 2002, 4:43 PM
 */

package org.netbeans.performance.impl.logparsing;

import org.netbeans.performance.spi.html.*;
import org.netbeans.performance.spi.*;
import java.util.*;

/**Wrapper class for a JDK garbage collection log.  This wrapper parses the log
 * and comes up with statistics representing data from it.  It does not hold onto
 * all of the GC events in a garbage collection log.  For a log wrapper that can
 * handle that (for example, to graph the occurance of gc events in relation to
 * some other kind of event), see GcEventLog.
 *
 * @author  Tim Boudreau
 */
public class GcLog extends AbstractLogFile {
    private static final String GC_LOG="gcinfo";

    public static final String GC_SECONDS="Seconds spent in GC";
    public static final String GC_TOTAL="Number of GC cycles";
    public static final String GC_FULLTOTAL="Number of full GC cycles";
    public static final String GC_MINORTOTAL="Number of minor GC cycles";
    public static final String GC_TOTALGARBAGE="Total Kb garbage collected";
    public static final String GC_HGES="Heap growth events";
    public static final String GC_AVGCOLLECTEDPERGC="Average Kb collected per GC";
    public static final String GC_SECSMAJOR="Seconds in MAJOR GC cycles";
    public static final String GC_SECSMINOR="Seconds in minor GC cycles";
    public static final String GC_AVGMILLISMAJOR="Average milliseconds per MAJOR GC";
    public static final String GC_AVGMILLISMINOR="Average milliseconds per minor GC";
    
    private static final String[] propNames = new String[] {
        GC_SECONDS, GC_TOTAL, GC_FULLTOTAL, GC_MINORTOTAL,
        GC_TOTALGARBAGE, GC_HGES, GC_AVGCOLLECTEDPERGC, 
        GC_SECSMAJOR, GC_SECSMINOR, GC_AVGMILLISMAJOR,
        GC_AVGMILLISMINOR 
    };
    
    /** Creates a new instance of GcLog using the
     * specified file.  */
    public GcLog(String filename) throws DataNotFoundException {
        super (filename);
        name = GC_LOG;
    }
    
    public GcLog (GcLog[] logs) throws DataNotFoundException {
        super ("Average of several runs");
        name= GC_LOG;
        parsed = true;
        if ((logs.length) == 0) throw new DataNotFoundException ("Attempted to create an averaged garbage collection log out of 0 log files.");
        buildElements (logs);
    }
    
    private void buildElements (GcLog[] logs) {
        List l;
        NameValueLogElement[][] elsByName = new NameValueLogElement[propNames.length][logs.length];
        for (int i=0; i < logs.length; i++) {
            for (int j=0; j < propNames.length; j++) {
                elsByName [j][i] = (NameValueLogElement) logs[i].findChild(propNames[j]);
            }
        }
        for (int k=0; k < propNames.length; k++) {
            addElement (new AveragedNameValueLogElement (propNames[k], elsByName[k])); 
        }
    }
    
    public static GcLog createAveragedGcLog (String[] filenames) {
        GcLog[] logs = new GcLog[filenames.length];
        for (int i=0; i < filenames.length; i++) {
            logs[i] = new GcLog (filenames[i]);
        }
        return new GcLog (logs);
    }
    
    /**Parse out all of the garbage collection entries from the
     * log file, and build some name-value statistics about them.
     */
    protected void parse() throws ParseException {
        //        System.out.println("Parsing GC log");
        String s;
        try {
            s=getFullText();
        } catch (java.io.IOException ioe) {
            throw new ParseException ("Exception reading log file.", ioe);
        }
        ArrayList gcs = new ArrayList();
        StringTokenizer tk = new StringTokenizer(s, "[", true);
        while (tk.hasMoreElements()) {
            String next = tk.nextToken();
            //            System.out.println("TOKEN: " + next);
            Gc gce = Gc.createGc(next);
            if (gce != null) { 
                gcs.add(gce);
            } else {
                //                System.out.println(next + " produced NULL");
            }
        }
        //        System.out.println("COUNT: " + gcs.size());
        if (gcs.size() > 0) {
            Iterator i = gcs.iterator();
            Gc g;
            long totalCollected=0;
            int heapChangeCount=0;
            float totalGcTime=0;
            int fullGcCount=0;
            float totalMinorGcTime=0;
            float totalMajorGcTime=0;
            
            while (i.hasNext()) {
                g = (Gc) i.next();
                totalCollected += g.getMemoryCollected();
                heapChangeCount += (g.getHeapDelta() != 0) ? 1:0;
                totalGcTime +=g.getSeconds();
                if (g.isFull()) {
                    fullGcCount++;
                    totalMajorGcTime += g.getSeconds();
                } else {
                    totalMinorGcTime += g.getSeconds();
                }
            }
            
            addElement (new NameValueLogElement (GC_SECONDS, totalGcTime));
            addElement (new NameValueLogElement (GC_TOTAL, new Integer(gcs.size())));
            addElement (new NameValueLogElement (GC_FULLTOTAL, new Integer(fullGcCount)));
            addElement (new NameValueLogElement (GC_MINORTOTAL, new Integer(gcs.size() - fullGcCount)));
            addElement (new NameValueLogElement (GC_TOTALGARBAGE, new Long(totalCollected)));
            addElement (new NameValueLogElement (GC_HGES, new Integer(heapChangeCount)));
            addElement (new NameValueLogElement (GC_AVGCOLLECTEDPERGC, new Long(totalCollected / gcs.size())));
            addElement (new NameValueLogElement (GC_SECSMAJOR, totalMajorGcTime));
            addElement (new NameValueLogElement (GC_SECSMINOR, totalMinorGcTime));
            if (fullGcCount != 0) {
                addElement (new NameValueLogElement (GC_AVGMILLISMAJOR, (float)((totalMajorGcTime / fullGcCount) * 1000)));
            } else {
                addElement (new NameValueLogElement (GC_AVGMILLISMAJOR, 0F));
            }
            addElement (new NameValueLogElement (GC_AVGMILLISMINOR, (float)((totalMinorGcTime / (gcs.size() - fullGcCount))*1000)));
            
        }
    }
    
    /**Test execution for debugging */
    public static void main (String[] args) {
//        GcLog lg = new GcLog ("/space/nbsrc/performance/gc/report/vanilla/gclog");
        GcLog lg = createAveragedGcLog (new String[] {
            "/space/nbsrc/performance/gc2/report/test17/gclog",
            "/space/nbsrc/performance/gc2/report/test17_1/gclog",
            "/space/nbsrc/performance/gc2/report/test17_2/gclog",
            "/space/nbsrc/performance/gc2/report/test17_3/gclog",
            "/space/nbsrc/performance/gc2/report/test17_4/gclog",
            "/space/nbsrc/performance/gc2/report/test17_5/gclog",
        });
            
        Iterator i = lg.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
}
