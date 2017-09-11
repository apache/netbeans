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
