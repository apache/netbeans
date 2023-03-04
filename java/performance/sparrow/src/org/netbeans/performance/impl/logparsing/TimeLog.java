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
 * TimeLog.java
 *
 * Created on October 16, 2002, 1:22 PM
 */

package org.netbeans.performance.impl.logparsing;
import java.util.*;
import org.netbeans.performance.spi.*;
import org.netbeans.performance.spi.html.*;
/** Wrapper for a simple log file representing NetBeans start
 * and finish times, in the format <BR>START=xx<BR>END=yy<BR>
 * where xx and yy are start and end times in milliseconds,
 * respectively.
 *
 * @author  Tim Boudreau
 */
public class TimeLog extends AbstractLogFile {
    /**The string to look for in the log file to indicate the start time for a session
     * (this value is written to the log file by the runner ant script) */
    public static final String START="START";
    /**The string to look for in the log file to indicate the end time for a session
     * (this value is written to the log file by the runner ant script) */
    public static final String END="END";
    /**The lookup key for the derived duration value, calculated either by subtracting
     * the logged start time from the end time, or averaged from the durations of 
     * TimeLogs passed to the averaging constructor. */
    public static final String DURATION="Duration";
    /**The string used in path elements for this type of log */
    public static final String TIME_LOG="timelog";
    
    /** Creates a new instance of TimeLog using the specified log file */
    public TimeLog(String filename) {
        super (filename);
        name=TIME_LOG;
        System.out.println("Timelog: " + filename);
        java.io.File f = new java.io.File (filename);
        if (!(f.exists())) throw new IllegalArgumentException ("Tried to create a time log from non-existent file " + filename);
    }
    
    /** Averaging constructor for TimeLog - creates a new timelog containing one
     * property - the averaged duration for all of the passed time logs. */
    public TimeLog (TimeLog[] logs) {
        super ("timelog");
        name=TIME_LOG;
        if (logs.length==0) throw new IllegalArgumentException ("Attempt to create an averaged time log from 0 individual time logs");
        NameValueLogElement[] els = new NameValueLogElement[logs.length];
            for (int i=0; i < logs.length; i++) {
                els[i] = (NameValueLogElement) logs[i].findChild(DURATION);
            }
        addElement (new AveragedNameValueLogElement (els));
        parsed = true;
    }

    protected void parse() throws ParseException {
        String s;
        try {
            s = getFullText();
        } catch (java.io.IOException ioe) {
            throw new ParseException (ioe.getMessage(), ioe);
        }
        StringTokenizer sk = new StringTokenizer (s,"^^^", false);
        long starttime=-1;
        long endtime=-1;
        long duration=-1;
        while (sk.hasMoreTokens()) {
            String curr = sk.nextToken();
            if (curr.indexOf (":") != -1) {
                String[] nameval = Utils.splitStringInTwo (curr,":");
    //            addElement (new NameValueLogElement (nameval[0], new Long(longval)));
                if (nameval[0].equals(START) || nameval[0].equals (END)) {
                    if (nameval[0].equals (START)) starttime=parseDate(nameval[1]);
                    if (nameval[0].equals (END)) endtime=parseDate(nameval[1]);
                } else {
                    if (nameval[0].equals (DURATION)) {
                        duration = Long.parseLong(nameval[1]);
                    }
                }
            } else {
                System.out.println("BOGUS LINE IN TIMELOG: " + curr);
            }
        }
        if (((starttime==-1) || (endtime==-1)) && duration == -1)
            throw new DataNotFoundException ("Start, end or duration times missing from log file");
        if (duration == -1) 
            duration = endtime-starttime;
        addElement (new NameValueLogElement (DURATION,new Long(duration)));
    }

    /**Parses a date in the format used by the ant logs into a millisecond long value.
     */
    private static final long parseDate(String date) {
        try {
            return new java.text.SimpleDateFormat(NBLOG_DATEFORMAT).parse(date).getTime();
        } catch (java.text.ParseException pe) {
            pe.printStackTrace();
            throw new RuntimeException("Failed to parse start or end date from logged data.");
        }
    }    
    
    /** Test execution for debugging */
    public static void main (String[] args) {
//        TimeLog tl = new TimeLog ("/tmp/testlog");
        TimeLog tl = new TimeLog (new TimeLog [] {
            new TimeLog ("/space/nbsrc/performance/gc2/report/test17/timelog_0"),
            new TimeLog ("/space/nbsrc/performance/gc2/report/test17/timelog_1"),
            new TimeLog ("/space/nbsrc/performance/gc2/report/test17/timelog_2"),
            new TimeLog ("/space/nbsrc/performance/gc2/report/test17/timelog_3"),
        });
        
        Iterator i = tl.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
    /** Create an HTML representation of this element's data (creates
     * a table with all of the properties).
     */
    public HTML toHTML () {
        checkParsed();
        HTMLTable result = new HTMLTable(2, HTML.SINGLE_ROW);
        Iterator i = getAllElements().iterator();
        LogElement curr;
        while (i.hasNext()) {
            curr = (LogElement) i.next();
            result.add (curr.toHTML()); 
        }
        return result;
    }
    
}
