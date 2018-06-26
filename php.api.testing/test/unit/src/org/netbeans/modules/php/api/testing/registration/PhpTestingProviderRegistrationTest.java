/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.testing.registration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class PhpTestingProviderRegistrationTest extends NbTestCase {

    private static final String CONSTRUCTOR = "constructor";
    private static final String FACTORY = "factory";


    public PhpTestingProviderRegistrationTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testRegistration() {
        MyTests.factoryCalls = 0;
        MockLookup.init();
        assertSame("No factory method should not be used yet", 0, MyTests.factoryCalls);
        Collection<? extends PhpTestingProvider> all = Lookups.forPath(PhpTesting.TESTING_PATH).lookupAll(PhpTestingProvider.class);
        assertSame("Two should be found", 2, all.size());
        // ???
        //assertSame("One factory method should be used", 1, MyTests.factoryCalls);

        Iterator<? extends PhpTestingProvider> it = all.iterator();
        assertSame(CONSTRUCTOR, it.next().getIdentifier());
        assertSame(FACTORY, it.next().getIdentifier());
    }

    //~ Inner classes

    public static final class MyTestsFactory {
        @PhpTestingProvider.Registration(position=200)
        public static MyTests getInstance() {
            MyTests.factoryCalls++;
            return new MyTests(FACTORY);
        }
    }

    /**
     *
     */
    @PhpTestingProvider.Registration(position=100)
    public static final class MyTests implements PhpTestingProvider {

        static int factoryCalls = 0;

        private final String identifier;
        private final String displayName;

        public MyTests() {
            this(CONSTRUCTOR);
        }

        MyTests(String name) {
            identifier = name;
            displayName = "display name";
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public boolean isTestFile(PhpModule phpModule, FileObject fileObj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isTestCase(PhpModule phpModule, PhpType.Method method) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void runTests(PhpModule phpModule, TestRunInfo runInfo, TestSession testSession) throws TestRunException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCoverageSupported(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locations.Line parseFileFromOutput(String line) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TestLocator getTestLocator(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
