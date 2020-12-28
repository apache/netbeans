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

package org.netbeans.modules.python.project.spi;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * An interface for unit test runner implementations.
 * (Copied from the equivalent Ruby class in ruby.project)
 *<p/>
 * <i>A work in progress, bound to change</i>.
 * 
 * @author Erno Mononen
 */
public interface TestRunner {
    
    enum TestType {
        /**
         * Represents PyUnit tests.
         */
        PY_UNIT,

        // TODO - other test frameworks here (for Ruby we had RSPEC, AUTOTEST etc)
        //RSPEC,
        //AUTOTEST
    }

    TestRunner getInstance();
    
    /**
     * Checks whether this test runner supports running of tests of the
     * given <code>type</code>.
     * 
     * @param type the type of the tests to run.
     * @return true if this test runner supports the given <code>type</code>.
     */
    boolean supports(TestType type);
    
    /**
     * Runs the given test file, i.e runs all tests
     * in it.
     * 
     * @param testFile the file representing a unit test class.
     * @param debug specifies whether the test file should be run 
     * in the debug mode.
     */
    void runTest(FileObject testFile, boolean debug);
    
    /**
     * Runs a single test method.
     * 
     * @param testFile the file representing the unit test class
     * whose test method to run.
     * @param className the class name of the test method to run.
     * @param testMethod the name of the test method to run.
     * @param debug specifies whether the test method should be run in the 
     * debug mode.
     */
    void runSingleTest(FileObject testFile, String className, String testMethod, boolean debug);
    
    /**
     * Runs all units tests in the given project.
     * 
     * @param project the project whose unit tests to run.
     * @param debug specifies whether the tests of the project should 
     * be run in the debug mode.
     */
    void runAllTests(Project project, boolean debug);

}
