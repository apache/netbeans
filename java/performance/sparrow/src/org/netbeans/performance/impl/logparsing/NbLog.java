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
 * NbLog.java
 *
 * Created on October 8, 2002, 4:43 PM
 */

package org.netbeans.performance.impl.logparsing;
import java.util.*;
import org.netbeans.performance.spi.*;
import java.io.*;
/** Object wrapper for NetBeans ide.log file, which parses
 * and stores data from the log file in LogElement objects.
 *
 * @author  Tim Boudreau
 */
public class NbLog extends AbstractLogFile {
    private static final String NB_LOG="sysinfo";
    /** Creates a new instance of NbLog */
    public NbLog(String filename) throws DataNotFoundException {
        super (filename);
        name = NB_LOG;
    }

    protected void parse() throws ParseException {
        String s=null;
        try {
            s = stringFromFile(getFileName());
        } catch (IOException ioe) {
            throw new ParseException ("IOException reading log file.", getFileName(), ioe);
        }
        //map the system info
        String lookfor = "Product Version";
        int startidx = s.indexOf(lookfor);
        int endidx = s.indexOf("CLASSPATH");
        endidx = s.lastIndexOf('\n', endidx);
        String logsection = s.substring(startidx, endidx);
        parseLogSysinfo(logsection);
    }
    
    /* Parses out the section of the log containing system
     * info and stores it in the aggregation.   
     */
    private void parseLogSysinfo(String s) {
        StringTokenizer sk = new StringTokenizer(s, "\n");
        String curr;
        while (sk.hasMoreTokens()) {
            curr=sk.nextToken().trim();
            if ((!(curr.startsWith("["))) && (!(curr.startsWith("---"))) && (curr.indexOf('=') != -1)){
                List elements = parseElements(curr);
                Iterator i = elements.iterator();
                LogElement le;
                while (i.hasNext()) {
                    le = (LogElement) i.next();
                    addElement (le);
                }
            }
        }
    }

    
    /**Utility method for deriving a list of log elements from a single
     * text line.  NetBeans compresses some information in its logfile,
     * along the lines of <code>Home dir; User dir = /dir1;/dir2</code>.
     * This will conveniently return a list of log elements for all of
     * the entries contained in a line.
     */
    public static List parseElements (String line) {
        ArrayList al = new ArrayList (2);
        StringTokenizer ske = new StringTokenizer(line, "=");
        if (ske.hasMoreElements()) {
            String initialkey = ske.nextToken().trim();
            String initialvalue = ske.nextToken().trim();
            while (ske.hasMoreElements()) {
                initialvalue += "=" + ske.nextToken();
            }
            if ((initialkey.indexOf (';') != -1) && (initialvalue.indexOf (';') != -1)) {
                String[] keys = Utils.splitString (initialkey, ";");
                String[] values = Utils.splitString (initialvalue, ";");
                for (int i=0; i < keys.length; i++) {
                    al.add (new NameValueLogElement(keys[i].trim(), values[i].trim()));
                }
            }  else {
                al.add (new NameValueLogElement(initialkey, initialvalue));
            }
        }
        return (List) al;
    }    
    
    /**Test execution for debugging */
    public static void main (String[] args) {
        NbLog lg = new NbLog ("/home/tb97952/.netbeans/3.4/system/ide.log");
        Iterator i = lg.iterator();
        System.out.println("Printing the stuff:");
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
}
