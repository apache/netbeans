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
 * Reporter.java
 *
 * Created on May 12, 2005, 10:26 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.j2ee.lib;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author jungi
 */
public class Reporter {
    
    private static final boolean CREATE_GOLDENFILES = Boolean.getBoolean("org.netbeans.test.j2ee.goldenfiles");
    //boolean CREATE_GOLDENFILES=true;
    
    private static Map reporters;
    
    private NbTestCase tc;
    
    static {
        reporters = new HashMap();
    }
    
    /** directory, where the golden and .diff files resides
     */
    private File classPathWorkDir;
    /** test will generate this file
     */
    private File refFile;
    private File logFile;
    private File mFile;
    
    private PrintStream log = null;
    private PrintStream ref = null;
    private PrintStream golden = null;
    //private PrintStream mf = null;
    
    /** Creates a new instance of Reporter */
    private Reporter(NbTestCase testCase) {
        tc = testCase;
        try {
            //logs and refs
            refFile = new File(tc.getWorkDir(), tc.getName() + ".ref");
            logFile = new File(tc.getWorkDir(), tc.getName() + ".log");
            mFile = new File(tc.getWorkDir(), tc.getName() + ".mf");
            ref = new PrintStream(new BufferedOutputStream(new FileOutputStream(refFile)));
            if (CREATE_GOLDENFILES) { //generates golden files
                File f;
                //generate goldenfile name
                f = tc.getDataDir();
                ArrayList names = new ArrayList();
                names.add("goldenfiles");
                while (!f.getName().equals("test")) {
                    if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                        names.add(f.getName());
                    }
                    f = f.getParentFile();
                }
                for (int i=names.size()-1;i > -1;i--) {
                    f = new File(f,(String)(names.get(i)));
                }
                f = new File(f, tc.getClass().getName().replace('.', File.separatorChar));
                f = new File(f, tc.getName()+".pass");
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                golden = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));
                log("Passive mode: generate golden file into "+f.getAbsolutePath());
            }
            //logFileStructure(classPathWorkDir);
        } catch (Exception e) {
            e.printStackTrace(getLogStream());
            //tc.assertTrue(e.toString(), false);
        }
    }
    
    public static final Reporter getReporter(NbTestCase testCase) {
        String name = testCase.toString();
        Object retVal = reporters.get(name);
        if (retVal == null) {
            retVal = new Reporter(testCase);
            reporters.put(name, retVal);
        }
        return (Reporter) retVal;
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
            ex.printStackTrace(getLogStream());
        }
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
    
    public void logFileStructure(File file) {
        File[] files=file.listFiles();
        for (int i=0;i < files.length;i++) {
            if (files[i].isDirectory()) {
                logFileStructure(files[i]);
            } else {
                log(files[i].getAbsolutePath());
                log(files[i]);
            }
        }
    }
    
    public void log(String s) {
        getLogStream().println(s);
    }
    
    public void log(Object o) {
        getLogStream().println(o);
    }
    
    public PrintStream getLogStream() {
        if (log == null) {
            try {
                log = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)));
            } catch (Exception ex) {
                ex.printStackTrace();
                tc.assertTrue(ex.toString(), false);
            }
        }
        return log;
    }
    
    public void log(File file) {
        try {
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine()) != null) {
                log(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace(getLogStream());
        }
    }
    
    /** sets the PrintStreams
     */
    public void close() {
        ref.close();
        if (log != null) {
            log.close();
        }
        if (CREATE_GOLDENFILES && golden != null) {
            golden.close();
            //assertTrue("Passive mode", false);
        }/* else {
            try {
                assertFile("Golden file differs ", refFile, getGoldenFile(), getWorkDir(), new LineDiff());
            } catch (Exception ex) {
                ex.printStackTrace();
                assertTrue(ex.toString(), false);
            }
        }
          */
    }
}


