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
 
