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

package org.netbeans.modules.cnd.testrunner.ui;

import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestNodeAction;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Base class for actions associated with a test method node.
 *
 */
abstract class BaseTestMethodNodeAction extends TestNodeAction {

    private static final Logger LOGGER = Logger.getLogger(BaseTestMethodNodeAction.class.getName());

    protected final Testcase testcase;
    protected final Project project;
    protected final String name;

    public BaseTestMethodNodeAction(Testcase testcase, Project project, String name) {
        this.testcase = testcase;
        this.project = project;
        this.name = name;
    }

    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return name;
        }
        return super.getValue(key);
    }

    protected String getTestMethod() {
        return testcase.getClassName() + "/" + testcase.getName(); //NOI18N
    }

//    protected FileObject getTestSourceRoot() {
//        PythonProject baseProject = project.getLookup().lookup(PythonProject.class);
//        // need to use test source roots, not source roots -- see the comments in #135680
//        FileObject[] testRoots = baseProject.getTestSourceRootFiles();
//        // if there are not test roots, return the project root -- works in rails projects
//        return 0 == testRoots.length ? project.getProjectDirectory() : testRoots[0];
//    }
//
//    protected TestRunner getTestRunner(TestRunner.TestType testType) {
//        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
//        for (TestRunner each : testRunners) {
//            if (each.supports(testType)) {
//                return each;
//            }
//        }
//        return null;
//    }
}
