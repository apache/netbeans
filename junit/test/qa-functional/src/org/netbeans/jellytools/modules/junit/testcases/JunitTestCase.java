/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
