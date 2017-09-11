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
package org.netbeans.modules.maven.output;

import junit.framework.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.maven.api.output.OutputVisitor;

/**
 * testing test output processing
 * @author  Milos Kleint
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
    
    public void testFailfafeSeparateTestOuput() {
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
    
}
