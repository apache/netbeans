/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.test.java;

import java.io.*;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.diff.LineDiff;


/** LogTestCase
 * @author Jan Becicka
 */
public class LogTestCase extends JavaTestCase {

    /**
     * state - true - testing
     *       - false - generating goldenfiles
     */
    public static boolean CREATE_GOLDENFILES=false;

    static {
        if (System.getProperty("create.goldenfiles") != null && System.getProperty("create.goldenfiles").equals("true")) {
            CREATE_GOLDENFILES=true;
        }
    }
    
    /** directory, where the golden and .diff files resides
     */
    protected File classPathWorkDir;
    /** test will generate this file
     */
    protected File refFile;
    
    protected PrintWriter log = null;
    protected PrintWriter ref = null;
    protected PrintWriter golden = null;
    
    public LogTestCase(java.lang.String testName) {
        super(testName);
    }
    
    /** sets the PrintWriters
     */
    protected void setUp() {
        prepareProject();
        try {
            //logs and refs
            refFile = new File(getWorkDir(), getName() + ".ref");
            File logFile = new File(getWorkDir(), getName() + ".log");
            ref = new PrintWriter(new BufferedWriter(new FileWriter(refFile)));
            log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
            openDefaultProject();
            if (CREATE_GOLDENFILES) { //generates golden files
                File f;
                //generate goldenfile name
                f=getDataDir();
                ArrayList names=new ArrayList();
                names.add("goldenfiles");
                while (!f.getName().equals("test")) {
                    if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                        names.add(f.getName());
                    }
                    f=f.getParentFile();
                }
                for (int i=names.size()-1;i > -1;i--) {
                    f=new File(f,(String)(names.get(i)));
                }
                f=new File(f, getClass().getName().replace('.', File.separatorChar));
                f=new File(f, getName()+".pass");
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                golden=new PrintWriter(new BufferedWriter(new FileWriter(f)));
                log("Passive mode: generate golden file into "+f.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        }
    }
    
    public void prepareProject() {//default - override for another projects
        classPathWorkDir=new File(getDataDir(), "projects.default.src".replace('.', File.separatorChar));
    }
    
    public void log(String s) {
        log.println(s);
    }
    
    public void log(Object o) {
        log.println(o);
    }
    
    public void ref(String s) {
        ref.println(s);
        if (CREATE_GOLDENFILES) {
            golden.println(s);
        }
    }
    
    public void ref(Object o) {
        ref.println(o.toString());
        if (CREATE_GOLDENFILES) {
            golden.println(o.toString());
        }
    }
    
    public void ref(File file) {
        try {
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine()) != null) {
                ref(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    /** sets the PrintWriters
     */
    protected void tearDown() {
        ref.close();
        log.close();
        if (CREATE_GOLDENFILES && golden != null) {
            golden.close();
            assertTrue("Passive mode", false);
        } else {
            try {
                assertFile("Golden file differs ", refFile, getGoldenFile(), getWorkDir(), new LineDiff());
            } catch (Exception ex) {
                ex.printStackTrace();
                assertTrue(ex.toString(), false);
            }
        }
    }
}

