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
 * GcTestSet.java
 *
 * Created on October 8, 2002, 12:40 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
import java.io.*;
/** Defines NetBeans garbage collection tests.
 *
 * @author  Tim Boudreau
 */
public class GcTestSet extends TestSet {

    public GcTestSet(String name) {
        super(name);
    }

    long start=-1;
    long end=-1;
    public LogElement[] createElementsForData(String name, String value) throws DataNotFoundException {
        if (name.equals("CONFIG")) {
            return new LogElement[] { new IdeCfg(value) };
        }
        if (name.equals("GCLOG")) {
            //XXX fixme - bugs in modules log
            //            return new LogElement[] { new GcLog (value), new ModulesLog (value) };
            GcLog log = genAveragedGcLog (value);
            return new LogElement[] { log };
        }
        if (name.equals ("TIMELOG")) {
            TimeLog tlog = genAveragedTimeLog (value);
            return new LogElement[] {tlog};
        }
        
        if (name.equals("START") || name.equals("END")) {
            if (name.equals("START"))
                start = parseDate(value);
            if (name.equals("END"))
                end = parseDate(value);
            if ((start != -1) && (end != -1)) {
                System.out.println("Session length = " + new Long(end-start).toString());
                LogElement[] result = new LogElement[] {
                    new NameValueLogElement("SessionLength", new Long(end-start)),
                    new NameValueLogElement("SessionStart", new Date(start)),
                    new NameValueLogElement("SessionEnd", new Date(end))
                };
                start = -1;
                end = -1;
                return result;
            } else {
                return null;
            }
        }
        if (name.equals("LOGFILE")) {
            return new LogElement[] { new NbLog(value) };
        }
        return new LogElement[] { new NameValueLogElement(name, value) };
    }
    
    public void createRun (FolderAggregation ada, String runinfo) {
        //cover the case where an exception aborts a run after one
        //or the other of these variables is set
        start=-1;
        end=-1;
        super.createRun(ada, runinfo);
    }
    
    private static final long parseDate(String date) {
        try {
            return new java.text.SimpleDateFormat(AbstractLogFile.NBLOG_DATEFORMAT).parse(date).getTime();
        } catch (java.text.ParseException pe) {
            pe.printStackTrace();
            throw new RuntimeException("Failed to parse start or end date from logged data.");
        }
    }
    
    private GcLog genAveragedGcLog (String logPrefix) {
        File f = new File (logPrefix + "_0");
        int index = 0;
        ArrayList al=new ArrayList();
        String currfile;
        while (f.exists()) {
            currfile = logPrefix + "_" + Integer.toString(index);
            al.add (new GcLog (currfile));
            index++;
            f = new File (logPrefix + "_" + Integer.toString (index));
        }
        GcLog[] logs = new GcLog[al.size()];
        logs = (GcLog[]) al.toArray(logs);
        return new GcLog (logs);
    }
    
    private TimeLog genAveragedTimeLog (String logPrefix) {
        File f = new File (logPrefix + "_0");
        int index = 0;
        ArrayList al=new ArrayList();
        String currfile;
        while (f.exists()) {
            currfile = logPrefix + "_" + Integer.toString(index);
            al.add (new TimeLog (currfile));
            index++;
            f = new File (logPrefix + "_" + Integer.toString (index));
        }
        TimeLog[] logs = new TimeLog[al.size()];
        logs = (TimeLog[]) al.toArray(logs);
        return new TimeLog (logs);
    }
}
 
