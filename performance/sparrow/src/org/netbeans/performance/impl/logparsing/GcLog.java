/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
            
            addElement (new NameValueLogElement (GC_SECONDS, new Float(totalGcTime)));
            addElement (new NameValueLogElement (GC_TOTAL, new Integer(gcs.size())));
            addElement (new NameValueLogElement (GC_FULLTOTAL, new Integer(fullGcCount)));
            addElement (new NameValueLogElement (GC_MINORTOTAL, new Integer(gcs.size() - fullGcCount)));
            addElement (new NameValueLogElement (GC_TOTALGARBAGE, new Long(totalCollected)));
            addElement (new NameValueLogElement (GC_HGES, new Integer(heapChangeCount)));
            addElement (new NameValueLogElement (GC_AVGCOLLECTEDPERGC, new Long(totalCollected / gcs.size())));
            addElement (new NameValueLogElement (GC_SECSMAJOR, new Float(totalMajorGcTime)));
            addElement (new NameValueLogElement (GC_SECSMINOR, new Float(totalMinorGcTime)));
            if (fullGcCount != 0) {
                addElement (new NameValueLogElement (GC_AVGMILLISMAJOR, new Float((totalMajorGcTime / fullGcCount) * 1000)));
            } else {
                addElement (new NameValueLogElement (GC_AVGMILLISMAJOR, new Float (0)));
            }
            addElement (new NameValueLogElement (GC_AVGMILLISMINOR, new Float((totalMinorGcTime / (gcs.size() - fullGcCount))*1000)));
            
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
