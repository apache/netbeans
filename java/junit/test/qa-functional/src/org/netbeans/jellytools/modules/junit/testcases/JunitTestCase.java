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

package org.netbeans.jellytools.modules.junit.testcases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.util.StringFilter;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author ms159439
 */
public class JunitTestCase extends JellyTestCase {

    /** Should we create goldenfiles? */
    private static boolean CREATE_GOLDENFILES = false;

    /** Create test Dialog label */
    protected static final String CREATE_TESTS_DIALOG = Bundle.getString(
            "org.netbeans.modules.junit.Bundle", "JUnitCfgOfCreate.Title");

    /** PrintWriter used for writing goldenfiles */
    protected static PrintWriter goldenWriter = null;

    /** Error log */
    protected static PrintStream err;

    /** Standard log */
    protected static PrintStream log;

    /** Current test workdir */
    private String workDir = "/tmp";

    /** Filter used to replace author and file creation time */
    protected StringFilter filter;

    static {
        if (System.getProperty("create.goldenfiles") != null &&
                System.getProperty("create.goldenfiles").equals("true")) {
            CREATE_GOLDENFILES=true;
        }
    }

    /** Creates a new instance of JunitTestCase */
    public JunitTestCase(String testName) {
        super(testName);
    }

    /**
     * Sets up logging facilities.
     */
    public void setUp() throws IOException {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("JunitTestProject");
        new Action("Run|Set Main Project|JunitTestProject", null).perform();
        ProjectSupport.waitScanFinished();
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null,
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        }  catch (IOException e) { }
        // set up filters for author and date
        filter = new StringFilter();
        filter.addReplaceFilter("@author ", "\n", "@author Tester\n");
        filter.addReplaceFilter("Created on ", "\n", "Created on Date\n");
    }

    /**
     * Tears down logging facilities
     */
    protected void tearDown() {
        if(CREATE_GOLDENFILES) {
            File f;
            //generate goldenfile name
            f = getDataDir().getParentFile(); // junit/test directory
            ArrayList names = new ArrayList();
            names.add("goldenfiles"); //!reverse order
            names.add("data"); //!reverse order
            names.add("qa-functional"); //!reverse order
            while (!f.getName().equals("test")) {
                if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                    names.add(f.getName());
                }
                f = f.getParentFile();
            }
            for (int i = names.size()-1;i > -1;i--) {
                f = new File(f,(String)(names.get(i)));
            }
            f = new File(f, getClass().getName().replace('.', File.separatorChar));
            f = new File(f, getName()+".pass");
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            try {
                goldenWriter = new PrintWriter(new BufferedWriter(new FileWriter(f)));
                System.out.println("Class name"+ Utilities.TEST_CLASS_NAME + "Test.java");
                EditorOperator op = new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java");
                goldenWriter.print(filter.filter(op.getText())); // goldenfile creation
                goldenWriter.println(); //a newline -- i wonder why it has to be there?
                goldenWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            log("Passive mode: generate golden file into "+f.getAbsolutePath());

        } else {
//            ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
//            compareReferenceFiles();
        }
        log.close();
        err.close();
    }

}
