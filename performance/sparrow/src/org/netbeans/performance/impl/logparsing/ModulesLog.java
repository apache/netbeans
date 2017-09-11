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
        
        String moduleSection = s.substring(startidx, s.length());
        
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
