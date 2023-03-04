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

package org.netbeans.performance.mobility.scanclasspath;

import java.util.ArrayList;
import org.netbeans.jemmy.operators.ComponentOperator;
//import org.netbeans.performance.mobility.setup.MobilitySetup;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.LoggingScanClasspath;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;


/**
 * Test provide measured time of scanning classpth for some classpath roots.
 * We measure classpath scanning during openieng HugeApplication project.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MPScanClasspath extends PerformanceTestCase {

    // list of classpath roots we are interesting to measure
    protected static ArrayList<String> reportCPR = new ArrayList<String> ();

    // measure whole classpath scan time together
    protected static long wholeClasspathScan = 0;
    
    static {
        reportCPR.clear();
        reportCPR.add("midpapi20.jar");                
        reportCPR.add("nb_midp_components.jar");              
        reportCPR.add("MobileApplicationVisualMIDlet/src");              
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     */
    public MPScanClasspath(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public MPScanClasspath(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
//        suite.addTest(new MobilitySetup("openMobilityMIDletProject"));
//        suite.addTest(new MPScanClasspath("measureClassPathScan"));
        
        return suite;
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
    public void measureClassPathScan() {
        LoggingScanClasspath.printMeasuredValues(getLog());
        
        for (LoggingScanClasspath.PerformanceScanData data : LoggingScanClasspath.getData()) {
            // report only if we want to report it
            if(reportCPR.contains((Object)data.getName())){
                if(data.getValue() > 0)
                    reportPerformance("Scanning " + data.getName(), data.getValue(), "ms", 1);
                else
                    fail("Measured value ["+data.getValue()+"] is not > 0 !");
            }
            
            // measure whole classpath scan
            wholeClasspathScan = wholeClasspathScan + data.getValue();
        }
        reportPerformance("Scanning Mobility Project Classpath", wholeClasspathScan, "ms", 1);
    }

    @Override
    public void prepare() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ComponentOperator open() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
