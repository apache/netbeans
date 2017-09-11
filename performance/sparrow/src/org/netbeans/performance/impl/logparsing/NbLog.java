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
