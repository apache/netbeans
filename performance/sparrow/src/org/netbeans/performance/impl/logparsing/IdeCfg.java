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
import org.netbeans.performance.spi.*;
import java.util.*;
import java.io.*;
/**Wrapper class for a JDK garbage collection log.
 *
 * @author  Tim Boudreau
 */
public class IdeCfg extends AbstractLogFile {
    private static final String IDE_CFG="javaconfig";

    /** Creates a new instance of GcLog using the
     * specified file.  */
    public IdeCfg(String filename) throws DataNotFoundException {
        super (filename);
        name = IDE_CFG;
    }

    /**Parse out all of the garbage collection entries from the
     * log file, and build some name-value statistics about them.
     */
    protected void parse() throws ParseException {
        String s;
        try {
            s=getFullText();
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                throw new DataNotFoundException ("Could not find log file.", ioe);
            } else {
                throw new ParseException ("Error getting log file text.", new File (getFileName()));
            }
        }
        StringTokenizer sk = new StringTokenizer(s);
        ArrayList switches = new ArrayList(10);
        while (sk.hasMoreElements()) {
            String el =sk.nextToken().trim();
            if (el.startsWith("-J")) {
                if (!(el.equals("-J-verbose:gc"))) {
                    if (!(el.startsWith("-J-D"))) {
                        JavaLineswitch curr = new JavaLineswitch(el.substring(2));
                        addElement (curr); 
                    }
                }
            }
        }
    }
    
    /**Test execution for debugging */
    public static void main (String[] args) {
        IdeCfg lg = new IdeCfg ("/home/tb97952/nb34/netbeans/bin/ide.cfg"); //("/space/nbsrc/performance/gc/report/vanilla/gclog");
        Iterator i = lg.iterator();
        System.out.println("Printing the stuff:");
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
}
