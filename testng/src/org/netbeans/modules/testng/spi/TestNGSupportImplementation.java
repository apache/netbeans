/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2008-2012 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.testng.spi;

import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lukas
 */
public abstract class TestNGSupportImplementation {

    private static final Logger LOGGER = Logger.getLogger(TestNGSupportImplementation.class.getName());

    /**
     * Check whether given project instance is supported by this implementation
     *
     * @param p project to check
     * @return true if this instance supports given project
     */
    public abstract boolean isActionSupported(Action action, Project p);

    /**
     * Check whether this implementation supports given FileObjects. Default implementation return false.
     *
     * @param activatedFOs FileoBjects to check
     * @return true if this instance supports given FileObjects, false otherwise
     */
    public boolean isSupportEnabled(FileObject[] activatedFOs) {
        return false;
    }

    /**
     * Configure project owning given FileObject
     *
     * @param createdFile FileObject for which the project should be configured
     */
    public abstract void configureProject(FileObject createdFile);

    /**
     * Create an instance of TestExecutor interface used for running
     * particular actions
     * 
     * @param p project for which the TestExecutor should be created
     * @return instance of TestExecutor
     */
    public abstract TestExecutor createExecutor(Project p);

    /**
     *
     */
    public interface TestExecutor {

        /**
         * Return true if configuration file for failed tests exists,
         * false otherwise
         *
         * @return true if configuration file for failed tests exists
         */
        boolean hasFailedTests();

        /**
         * Execute tests defined in test config
         *
         * @param config test config to run
         */
        public void execute(Action action, TestConfig config) throws IOException;
    }
}
