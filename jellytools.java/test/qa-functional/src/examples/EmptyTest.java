/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package examples;

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;

/** It is just an example how test could look like. */
public class EmptyTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public EmptyTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        // run tests in particular order
        //return createModuleTest(EmptyTest.class, "test2", "test1");

        // run all tests 
        //return createModuleTest(EmptyTest.class);

        // run tests with specific configuration
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(EmptyTest.class).
                clusters(".*").
                enableModules(".*").
                failOnException(Level.INFO).
                failOnMessage(Level.SEVERE);
        conf = conf.addTest("test1");
        return NbModuleSuite.create(conf);
    }

    /** Method called before each test case. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** Method called after each test case. */
    @Override
    public void tearDown() {
    }

    /** Test case 1. */
    public void test1() {
        System.out.println("test case 1");
    }

    /** Test case 2. */
    public void test2() {
        System.out.println("test case 2");
    }
}
