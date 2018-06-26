/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.spi.testing.create;

import java.util.Collection;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.Parameters;

// package private class
final class EmptyTestCreatorConfiguration extends TestCreatorConfiguration {

    private final String framework;
    private final CreateTestsSupport createTestsSupport;


    private EmptyTestCreatorConfiguration(String framework, CreateTestsSupport createTestsSupport) {
        assert framework != null;
        assert createTestsSupport != null;
        this.framework = framework;
        this.createTestsSupport = createTestsSupport;
    }

    static TestCreatorConfiguration create(String framework, CreateTestsSupport createTestsSupport) {
        Parameters.notEmpty("framework", framework); // NOI18N
        Parameters.notNull("createTestsSupport", createTestsSupport); // NOI18N
        return new EmptyTestCreatorConfiguration(framework, createTestsSupport);
    }

    @Override
    public boolean canHandleProject(String framework) {
        return this.framework.equals(framework);
    }

    @Override
    public void persistConfigurationPanel(Context context) {
        // noop
    }

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        return createTestsSupport.getTestSourceRoots(createdSourceRoots, fo);
    }

    @Override
    public boolean showClassNameInfo() {
        return false;
    }

    @Override
    public boolean showClassToTestInfo() {
        return false;
    }

    @Override
    public Pair<String, String> getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium) {
        // we are hidden so noop
        return Pair.of("whatever", "donotcareTest"); // NOI18N
    }

}
