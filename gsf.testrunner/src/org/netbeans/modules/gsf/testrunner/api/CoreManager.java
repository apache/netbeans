/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.testrunner.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CoreManager manages communication between various loggers and {@code org.netbeans.modules.gsf.testrunner.ui.api.Manager}. 
 * This is needed so that core and UI parts of test runners can communicate.
 *
 * @author Theofanis Oikonomou
 */
public abstract class CoreManager {
    
    /**
     * Registers a communication channel between core and UI.
     * Should be placed on a {@link CoreManager} instance.
     * @since org.netbeans.modules.gsf.testrunner/2 2.0
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {

        /**
         * @return Project type, e.g. {@link org.netbeans.modules.gsf.testrunner.api.Utils#ANT_PROJECT_TYPE} or 
         * {@link org.netbeans.modules.gsf.testrunner.api.Utils#MAVEN_PROJECT_TYPE}. If it makes no difference "" should be provided
         */
        String projectType();

        /**
         *
         * @return Testing framework, e.g. {@link org.netbeans.modules.gsf.testrunner.api.Utils#JUNIT_TF} or
         * {@link org.netbeans.modules.gsf.testrunner.api.Utils#TESTNG_TF}
         */
        String testingFramework();        
    }
    
    /**
     * Registers an appropriate {@code org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory} that will take care of 
     * the creation of Nodes in the Test Results Window.
     */
    public void registerNodeFactory() {}

    /**
     * Called when a task running tests is started. Displays a message in the test results window.
     *
     * @param session the {@link TestSession} that is started
     */
    public abstract void testStarted(TestSession session);

    /**
     * Called when a task finishes running a test session.
     *
     * @param session the {@link TestSession} that is finished
     */
    public abstract void sessionFinished(TestSession session);

    /**
     * Called when a task needs to update the corresponding report of a running test session.
     * Merely invokes {@link #displayReport(TestSession, Report, boolean)} with {@code false} as the value of the last parameter.
     *
     * @param session the {@link TestSession} that is running
     * @param report the {@link Report} to be displayed
     * @see #displayReport(TestSession, Report, boolean) 
     */
    public abstract void displayReport(TestSession session, Report report);

    /**
     * Called when a task needs to update the corresponding report of a running test session.
     *
     * @param session the {@link TestSession} that is running
     * @param report the {@link Report} to be displayed
     * @param completed {@code true} if the {@link TestSession} is completed, {@code false} otherwise
     */
    public abstract void displayReport(TestSession session, Report report, boolean completed);

    /**
     * Called when a task needs to display some output of a running test session.
     *
     * @param session the {@link TestSession} for which output needs to be displayed
     * @param text the text to display
     * @param error {@code true} if this is an error, {@code false} otherwise
     */
    public abstract void displayOutput(TestSession session, String text, boolean error);

    /**
     * Called when a task needs to communicate that a test suite is running.
     *
     * @param session the {@link TestSession} that is running
     * @param suite the {@link TestSuite} that is running
     */
    public abstract void displaySuiteRunning(TestSession session, TestSuite suite);

    /**
     * Called when a task needs to communicate that a test suite is running.
     *
     * @param session the {@link TestSession} that is running
     * @param suiteName name of the running suite; or {@code null} in the case of anonymous suite
     */
    public abstract void displaySuiteRunning(TestSession session, String suiteName);
}
