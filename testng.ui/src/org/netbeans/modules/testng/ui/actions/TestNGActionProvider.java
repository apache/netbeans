/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.testng.ui.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lukas
 */
@ServiceProvider(service = ActionProvider.class)
public class TestNGActionProvider implements ActionProvider {

    private static final Logger LOGGER = Logger.getLogger(TestNGActionProvider.class.getName());
    private static final String[] SUPPORTED_ACTIONS = {
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD
    };

    public TestNGActionProvider() {
    }

    public String[] getSupportedActions() {
        return SUPPORTED_ACTIONS;
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return context.lookup(SingleMethod.class) != null;
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        TestNGSupport.Action action = SingleMethod.COMMAND_RUN_SINGLE_METHOD.equals(command)
                ? TestNGSupport.Action.RUN_TESTMETHOD
                : TestNGSupport.Action.DEBUG_TESTMETHOD;
        SingleMethod sm = context.lookup(SingleMethod.class);
        FileObject fo = sm.getFile();

        TestClassInfoTask task = new TestClassInfoTask(0);
        JavaSource js = JavaSource.forFileObject(fo);
        try {
            js.runUserActionTask(task, true);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        Project p = FileOwnerQuery.getOwner(fo);
        TestNGSupportImplementation.TestExecutor exec = TestNGSupport.findTestNGSupport(p).createExecutor(p);
        TestConfig conf = TestNGUtils.getTestConfig(fo, false, task.getPackageName(), task.getClassName(), sm.getMethodName());
        try {
            exec.execute(action, conf);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
