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

package org.netbeans.performance.results;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.xml.parsers.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.types.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Radim Kubacki
 */
public class AnalyzeResultsTask extends Task {

    /** File containing reference results. */
    private File refFile;

    /** Use last testrun result as a reference.
     *  If set to false then the oldest run will be used.
     */
    private boolean refLast = false;
    
    public void setrefFile(File f) {
        refFile = f;
    }
    
    
    private List<FileSet> resultsReports = new LinkedList<FileSet>();
    public void addFileset(FileSet fs) {
        resultsReports.add(fs);
    }
    
    public void execute() throws BuildException {
        // TODO code here what the task actually does:
        
        // To log something:
        // log("Some message");
        // log("Serious message", Project.MSG_WARN);
        log("AnalyzeResultsTask running", Project.MSG_VERBOSE);

        checkArgs();
        try {
            log("Processing reference results from "+refFile, Project.MSG_VERBOSE);
            Set<File> refFiles = Collections.singleton(refFile);
            
            Set<File> newFiles = new TreeSet<File>();
            
            for (FileSet fs: resultsReports) {
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                File basedir = ds.getBasedir();
                String[] files = ds.getIncludedFiles();
                for (int i = 0; i<files.length; i++) {
                    log("Processing "+files[i], Project.MSG_VERBOSE);
                    File srcFile = new File(basedir, files[i]);
                    newFiles.add(srcFile);
                }
            }
            // process them...
            Set<File> xmls = ReportUtils.doCompare(refFiles, newFiles);
            // PENDING now generate html

            for (File xml: xmls) {
                log("Generated XML: "+xml, Project.MSG_INFO);
                XSLTProcess xslt = (XSLTProcess)project.createTask("style");
                xslt.setIn(xml);
                File outFile = new File(xml.getParentFile().getParentFile(), "htmlresults"+File.separator+"testresults-performance.html");
                xslt.setOut(outFile);
                xslt.setStyle("reports/style-html.xsl");
                xslt.init();
                xslt.execute();
                log("Generated HTML: "+outFile, Project.MSG_INFO);
            }
        }
        catch (Exception ex) {
            throw new BuildException ("Error during result comparing.", ex);
        }
    }

    /*
    private void printTTest(final Map refCases, final Map newCases) {
        Iterator it2 = newCases.keySet().iterator();
        while (it2.hasNext()) {
            TestCaseResults res = (TestCaseResults)it2.next();
            Object o = refCases.get(res);
            if (o != null) {
                TestCaseResults refResult = (TestCaseResults)o;
                if (refResult.getCount() <= 2 || res.getCount() <= 2) {
                    log("ttest skipped for "+refResult.getName()+" "+refResult.getOrder(), Project.MSG_VERBOSE);
                    continue;
                }
                log("Compare "+refResult.getName()+" "+refResult.getOrder(), Project.MSG_VERBOSE);
                log("\t"+refResult.getCount()+" against "+res.getCount()+" values", Project.MSG_VERBOSE);
                TestCaseResults.TTestValue tt = TestCaseResults.ttest(res, refResult);
                res.setTTest(tt);
            }
        }
    }
     */
    
    private void checkArgs() throws BuildException {
        if (refFile == null && resultsReports.isEmpty()) {
            // try to find last performance results and use them as a reference
            // look for one of results/testrun_??????-??????/xmlresults/testrun-performance.xml
            File dir = new File (getProject().getBaseDir(), "results");
            if (dir.isDirectory()) {
                File [] testrunDirs = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                         return name.startsWith("testrun_") && name.length() == 21;
                    }
                });
                Arrays.sort(testrunDirs);
                int refIdx = refLast? testrunDirs.length-1: 0;
                if (testrunDirs.length > 0) {
                    refFile = new File(testrunDirs[refIdx], "xmlresults"+File.separator+"testrun-performance.xml");
                    log("Using default reference file "+refFile, Project.MSG_VERBOSE);
                }
                for (int i = 0; i < testrunDirs.length; i++) {
                    if (i == refIdx) {
                        continue;
                    }
                    File runFile = new File(testrunDirs[i], "xmlresults"+File.separator+"testrun-performance.xml");
                    log("Adding files for comparision: "+runFile, Project.MSG_VERBOSE);
                    FileSet fs = new FileSet();
                    fs.setFile(runFile);
                    addFileset(fs);
                }
            }
            // find results to compare with

        }
        if (refFile == null || !refFile.exists() || !refFile.isFile()) {
            throw new BuildException ("Missing reference file. refFile attribute must be set.");
        }
//        if (destFile == null) {
//            throw new BuildException ("Missing destination file. destFile attribute must be set.");
//        }
    }
    
}
