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
import org.apache.tools.ant.types.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Radim Kubacki
 */
public class ReportUtils {

    public static Set<File> doCompare(Set<File> refFiles, Set<File> newFiles) throws Exception {

        HashSet<File> outputs = new HashSet<File>();
        OutputStreamWriter ow = null;
        try {
            ResultsHandler handler = new ResultsHandler();
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            for (File refFile : refFiles) {
                p.parse(refFile, handler);
            }
            
            Map<TestResult,Collection<Integer>> refCases = handler.getCases();
            
            for (File srcFile: newFiles) {
                ResultsHandler handler2 = new ResultsHandler();
                File destFile = new File( srcFile.getParentFile(), "testreport-perf-comparision.xml");
                p.parse(srcFile, handler2);

                Map<TestResult,Collection<Integer>> newCases = handler2.getCases();
//                    printTTest(refCases, newCases);
                ow = new OutputStreamWriter(new FileOutputStream(destFile));
                ow.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testresults>\n");
                printCases(ow, newCases, refCases);
                ow.write("</testresults>\n");
                ow.close();
                outputs.add (destFile);
            }
            
        }
        catch (Exception ex) {
            throw ex;
        }
        finally {
            try { if (ow != null) ow.close(); } catch (IOException ioe) {}
        }
        return outputs;
    }

    /** Outputs the results of all cases in passed iterator.
     * When refCases is supplied it also prints the difference and result of ttest.
     */
    private static void printCases (OutputStreamWriter ow,
            Map<TestResult, Collection<Integer>> actual,
            Map<TestResult, Collection<Integer>> reference) throws IOException {
        NumberFormat f = new DecimalFormat ("#######0.00");
        NumberFormat f2 = new DecimalFormat ("#######0.0000");
        Set<TestResult> keys = actual.keySet();
        for (TestResult oneCase : keys) {
            Collection<Integer> values = actual.get(oneCase);
            TestResult.Statistics stat = TestResult.computeStatistics(values);
            ow.write("<testcase");
            ow.write(" name=\""+oneCase.getName()+"\"\n");
            ow.write(" threshold=\""+oneCase.getThreshold()+"\"");
            ow.write(" unit=\""+oneCase.getUnit()+"\"");
            ow.write(" order=\""+oneCase.getOrder()+"\"\n");
            ow.write(" average=\""+f.format(stat.getAverage())+"\"");
            ow.write(" stddev=\""+f.format(stat.getStdDev())+"\"");
            ow.write(" variance=\""+f.format(stat.getVariance())+"\">\n");
            for (int val : values) {
                ow.write("\t<result value=\""+val+"\"/>\n");
            }
            if (reference != null) {
                Collection<Integer> refValues = reference.get(oneCase);
                if (refValues != null) {
                    TestResult.Statistics refStat = TestResult.computeStatistics(refValues);
                    ow.write("<difference value=\""+
                            f.format(stat.getAverage()/refStat.getAverage()*100-100)+
                            "\" origaverage=\""+
                            refStat.getAverage()+
                            "\" origstddev=\""+
                            f.format(refStat.getStdDev())+

                            "\"/>\n");
                    
		    /*
            	TestCaseResults.TTestValue tt = oneCase.getTTest();
            	if (tt != null) {
            	    ow.write ("<ttest p=\""+f2.format(tt.getP())+"\" tvalue=\""+f.format(tt.getT())+"\" df=\""+f.format(tt.getDF())+"\">\n");
            	    ow.write (tt.getComment()+"\n");
            	    ow.write ("</ttest>\n");
            	}
		    */
                }
            }
            ow.write("</testcase>\n");
        }
    }

    private static class ResultsHandler extends DefaultHandler {
        
        private Map<TestResult, Collection<Integer>> cases;
        
        private String currSuite;
        
        public ResultsHandler () {
            cases = new TreeMap<TestResult, Collection<Integer>> ();
        }
        
        public Map<TestResult, Collection<Integer>> getCases () {
            return cases;
        }
        
        public void startElement(String namespaceURI, String localName, String qName, org.xml.sax.Attributes atts) 
        throws org.xml.sax.SAXException {
            if ("PerformanceData".equals(qName)) {
                try {
                    String name = atts.getValue("name");
                    int order = Integer.parseInt(atts.getValue("runOrder"));
                    int threshold = Integer.parseInt(atts.getValue("threshold"));
                    String unit = atts.getValue("unit");
                    int value = Integer.parseInt(atts.getValue("value"));
                    
                    TestResult oneCase = new TestResult (name, threshold, unit, order, currSuite);
                    Collection<Integer> values = cases.get(oneCase);
                    if (values == null) {
                        values = new ArrayList<Integer> ();
                        cases.put(oneCase, values);
                    }
                    values.add(value);
                }
                catch (NumberFormatException e) {
                    
                }
            }
            else if ("UnitTestSuite".equals(qName)) {
                currSuite = atts.getValue("name");
            }
        }
        
        public void endDocument() throws SAXException {
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("UnitTestSuite".equals(qName)) {
                currSuite = null;
            }
        }
        
    }
}
