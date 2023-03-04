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

package org.netbeans.performance.enterprise.scanclasspath;


import java.util.ArrayList;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.LoggingScanClasspath;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;


/**
 * Test provide measured time of scanning classpth for some classpath roots.
 * We measure classpath scanning during openieng HugeApplication project.
 *
 * @author  mmirilovic@netbeans.org
 */
public class EPScanClasspath extends PerformanceTestCase {

    // list of classpath roots we are interesting to measure
    protected static ArrayList<String> reportCPR = new ArrayList<String> ();

    // measure whole classpath scan time together
    protected static long wholeClasspathScan = 0;
    
    static {
        reportCPR.clear();
        reportCPR.add("rt.jar");                // JDK/jre/lib/rt.jar    
        reportCPR.add("src/java");              // HugeApp/src/java/
        reportCPR.add("webservices-rt.jar");    // appserver/lib/webservices-rt.jar
        reportCPR.add("webservices-tools.jar"); // appserver/lib/webservices-tools.jar
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     */
    public EPScanClasspath(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of WebScanClasspath
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public EPScanClasspath(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        
//        suite.addTest(new EnterpriseSetupTest("openReservationPartnerServicesProject"));
//        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceProject"));
//        suite.addTest(new EnterpriseSetupTest("openTravelReservationServiceApplicationProject"));
//
//        suite.addTest(new EPScanClasspath("measureClassPathScan"));
//
//        
//        suite.addTest(new ScanClasspath("openJEditProject"));
        
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
        reportPerformance("Scanning Enteprise Project Classpath", wholeClasspathScan, "ms", 1);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        return null;
    }
}
