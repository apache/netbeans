/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * ModulesLog.java
 *
 * Created on October 8, 2002, 4:43 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
/**Wrapper class for the listing of active modules in NetBeans' output
 *
 * @author  Tim Boudreau
 */
public class ModulesLog extends AbstractLogFile {
    private static final String MOD_LOG="activeModules";

    public static final String GC_SECONDS="Seconds spent in GC";
    public static final String GC_TOTAL="Number of GC cycles";
    public static final String GC_FULLTOTAL="Number of GC cycles";
    public static final String GC_MINORTOTAL="Number of minor GC cycles";
    public static final String GC_TOTALGARBAGE="Total Kb garbage collected";
    public static final String GC_HGES="Heap growth events";
    public static final String GC_AVGCOLLECTEDPERGC="Average Kb collected per GC";
    public static final String GC_SECSMAJOR="Seconds in MAJOR GC cycles";
    public static final String GC_SECSMINOR="Seconds in minor GC cycles";
    public static final String GC_AVGSECSMAJOR="Avg. seconds per MAJOR GC";
    public static final String GC_AVGSECSMINOR="Avg. seconds per minor GC";
    
    
    /** Creates a new instance of GcLog using the
     * specified file.  */
    public ModulesLog(String filename) {
        super (filename);
        name = MOD_LOG;
    }
    
        
    /**Parse out all of the garbage collection entries from the
     * log file, and build some name-value statistics about them.
     */
    protected void parse() throws ParseException {
        String s;
        try {
            s=getFullText();
        } catch (java.io.IOException ioe) {
            throw new ParseException ("Exception getting logfile to parse " + getFileName(), ioe);
        }
        
        String lookfor = "Turning on modules:";
        /*
        int startidx = s.indexOf(lookfor + lookfor.length());
        startidx = s.indexOf('\n', startidx);
        int endidx = s.indexOf("[GC", startidx);
        int end2 = s.indexOf("[Full", startidx);
        if (end2 != -1) endidx = Math.min(endidx, end2);
        if (endidx==-1) {
            endidx=s.length()-1;
        } else {
            endidx=s.lastIndexOf('\n', endidx);
        }
         */
        int startidx = s.indexOf(lookfor + lookfor.length());
        startidx = s.indexOf('\n', startidx);
        
        String moduleSection = s.substring(startidx);
        
        StringTokenizer sk = new StringTokenizer(moduleSection, "\n");
        String curr;
        while (sk.hasMoreTokens()) {
            curr = sk.nextToken().trim();
            if ((!(curr.startsWith ("["))) && (!(curr.startsWith ("Turn")))) {
                addElement (new ModuleEntry (curr));
            }
        }
    }
    

    /**Test execution for debugging */
    public static void main (String[] args) {
        ModulesLog lg = new ModulesLog ("/space/nbsrc/performance/gc/report/vanilla/gclog");
        Iterator i = lg.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
}
