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
package org.netbeans.modules.maven.output;

import junit.framework.*;
import org.netbeans.modules.maven.api.output.OutputVisitor;

/**
 * testing test output processing
 *
 * @author Milos Kleint
 */
public class TestOutputListenerProviderTest extends TestCase {

    private TestOutputListenerProvider provider;

    public TestOutputListenerProviderTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        provider = new TestOutputListenerProvider();
    }

    public void testSeparateTestOuput() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#surefire:test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Surefire report directory: /home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Setting reports dir: /home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target2/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target2/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Running org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderTest", visitor);
        assertEquals(provider.runningTestClass, "org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("[surefire] Running org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderTest", visitor);
        assertEquals(provider.runningTestClass, "org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.038 sec", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Running org.codehaus.mevenide.netbeans.execute.OutputHandlerTest", visitor);
        assertEquals(provider.runningTestClass, "org.codehaus.mevenide.netbeans.execute.OutputHandlerTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec <<< FAILURE!        ", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec \r\r\t\n\r\n<<< FAILURE!        ", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec <<< FAILURE! - in org.milos.FooTest", visitor);
        assertNotNull(visitor.getOutputListener());

        //behaviour on windows...
        visitor.resetVisitor();
        provider.processLine("[surefire] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec", visitor);
        assertNull(visitor.getOutputListener());
        assertTrue(visitor.isLineSkipped());
        visitor.resetVisitor();
        provider.processLine(" <<< FAILURE !!", visitor);
        assertNotNull(visitor.getOutputListener());
        assertEquals(visitor.getLine(), "[surefire] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec <<< FAILURE !!");
        assertFalse(visitor.isLineSkipped());
        visitor.resetVisitor();

        provider.sequenceFail("mojo-execute#surefire:test", visitor);

    }

    public void testFailsafeSeparateTestOuput() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Failsafe report directory: /home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target/failsafe-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/home/mkleint/src/mevenide/mevenide2/netbeans/nb-project/target/failsafe-reports");
        visitor.resetVisitor();

        provider.processLine("Running org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderIT", visitor);
        assertEquals(provider.runningTestClass, "org.codehaus.mevenide.netbeans.output.JavaOutputListenerProviderIT");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.038 sec", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Running org.codehaus.mevenide.netbeans.execute.OutputHandlerIT", visitor);
        assertEquals(provider.runningTestClass, "org.codehaus.mevenide.netbeans.execute.OutputHandlerIT");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec <<< FAILURE!        ", visitor);
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.057 sec \r\r\t\n\r\n<<< FAILURE!        ", visitor);
        assertNotNull(visitor.getOutputListener());

        provider.sequenceFail("mojo-execute#surefire:test", visitor);

    }

    /**
     * Test based on the output generated by Surefire 2.18.1. 
     */
    public void testSurefireTestOuput_2_18_1() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("Surefire report directory: /target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Running com.example.SampleTest", visitor);
        assertEquals(provider.runningTestClass, "com.example.SampleTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Failed tests.
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec <<< FAILURE! - in com.example.SampleTest", visitor);
        // there must be a listener to navigate to test reports
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Successful tests.
        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0", visitor);
        // there must be a listener to navigate to test reports
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.sequenceFail("mojo-execute#surefire:test", visitor);
    }

    /**
     * Test based on the output generated by Surefire 2.18.1 with "Print Maven output logging level" enabled
     */
    public void testSurefireTestOuput_2_18_1_withLogging() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.processLine("[INFO] Surefire report directory: /target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Running com.example.SampleTest", visitor);
        assertEquals(provider.runningTestClass, "com.example.SampleTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Failed tests.
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec <<< FAILURE! - in com.example.SampleTest", visitor);
        // there must be a listener to navigate to test reports
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Successful tests.
        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0", visitor);
        // there must be a listener to navigate to test reports
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.sequenceFail("mojo-execute#surefire:test", visitor);
    }
    
    /**
     * Test based on the output generated by Surefire 2.19.1 
     */
    public void testSurefireTestOuput_2_19_1() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        
        // Surefire report directory: /target/surefire-reports is not generated by default only with mvn -e
        provider.processLine("Surefire report directory: /target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Running com.example.SampleTest", visitor);
        assertEquals(provider.runningTestClass, "com.example.SampleTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Failed tests.
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec <<< FAILURE! - in com.example.SampleTest", visitor);
        // there must be a listener to navigate to test reports
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Successful tests.
        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0", visitor);
        // there must be a listener to navigate to test reports
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.sequenceFail("mojo-execute#surefire:test", visitor);
    }

    /**
     * Test based on the output generated by Surefire 2.19.1 with logging turned on
     */
    public void testSurefireTestOuput_2_19_1_withLogging() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        
        // Surefire report directory: /target/surefire-reports is not generated by default only with mvn -e
        provider.processLine("[INFO] Surefire report directory: /target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("Running com.example.SampleTest", visitor);
        assertEquals(provider.runningTestClass, "com.example.SampleTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Failed tests.
        provider.processLine("Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec <<< FAILURE! - in com.example.SampleTest", visitor);
        // there must be a listener to navigate to test reports
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Successful tests.
        provider.processLine("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0", visitor);
        // there must be a listener to navigate to test reports
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.sequenceFail("mojo-execute#surefire:test", visitor);
    }

    /**
     * Test based on the output generated by Surefire 2.22.1 as well with 3.0.0-M3 with logging turned on
     */
    public void testSurefireTestOuput_2_22_later_withLogging() {
        OutputVisitor visitor = new OutputVisitor();
        visitor.resetVisitor();
        provider.sequenceStart("mojo-execute#failsafe:integration-test", visitor);
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        
        // Surefire report directory: /target/surefire-reports is not generated by default only with mvn -e
        provider.processLine("[INFO] Surefire report directory: /target/surefire-reports", visitor);
        assertNull(visitor.getOutputListener());
        assertEquals(provider.outputDir, "/target/surefire-reports");
        visitor.resetVisitor();

        provider.processLine("[INFO] Running com.example.SampleTest", visitor);
        assertEquals(provider.runningTestClass, "com.example.SampleTest");
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Failed tests.
        provider.processLine("[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec <<< FAILURE! - in com.example.SampleTest", visitor);
        // there must be a listener to navigate to test reports
        assertNotNull(visitor.getOutputListener());
        visitor.resetVisitor();

        // Successful tests.
        provider.processLine("[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0", visitor);
        // there must be a listener to navigate to test reports
        assertNull(visitor.getOutputListener());
        visitor.resetVisitor();
        provider.sequenceFail("mojo-execute#surefire:test", visitor);
    }
}
