/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.testng;

import java.util.Map;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.CreateTestParam;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  Marian Petras
 */
public abstract class TestNGPluginTrampoline {

    /** the trampoline singleton, defined by {@link TestNGPlugin} */
    public static TestNGPluginTrampoline DEFAULT;

    /**
     * Provokes initialization of class TestNGPlugin.
     */
    {
        Class c = TestNGPlugin.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Used by {@link TestNGPlugin}. */
    public TestNGPluginTrampoline() {}
    
    /**
     */
    public abstract boolean createTestActionCalled(
            TestNGPlugin plugin,
            FileObject[] filesToTest);
    
    /**
     * Returns a specification of a Java element or file representing test
     * for the given source Java element or file.
     *
     * @param  sourceLocation  specification of a Java element or file
     * @return  specification of a corresponding test Java element or file,
     *          or {@code null} if no corresponding test Java file is available
     */
    public abstract Location getTestLocation(
            TestNGPlugin plugin,
            Location sourceLocation);
    
    /**
     * Returns a specification of a Java element or file that is tested
     * by the given test Java element or test file.
     *
     * @param  testLocation  specification of a Java element or file
     * @return  specification of a Java element or file that is tested
     *          by the given Java element or file.
     */
    public abstract Location getTestedLocation(
            TestNGPlugin plugin,
            Location testLocation);
    
    /**
     * Determines whether the given plugin is capable of creating tests
     * for the given files at the moment.
     * The default implementation returns {@code true}.
     *
     * @param  plugin  plugin to be queried
     * @param  fileObjects  {@code FileObject}s for which the tests are about
     *                      to be created
     * @return  {@code true} if the given plugin is able of creating tests
     *          for the given {@code FileObject}s, {@code false} otherwise
     * @see  #createTests
     */
    public abstract boolean canCreateTests(
            TestNGPlugin plugin,
            FileObject... fileObjects);

    /**
     * Creates test classes for given source classes.
     *
     * @param  filesToTest  source files for which test classes should be
     *                      created
     * @param  targetRoot   root folder of the target source root
     * @param  params  parameters of creating test class
     *                 - each key is an {@code Integer} whose value is equal
     *                 to some of the constants defined in the class;
     *                 the value is either
     *                 a {@code String} (for key with value {@code CLASS_NAME})
     *                 or a {@code Boolean} (for other keys)
     * @return  created test files
     */
    public abstract FileObject[] createTests(
            TestNGPlugin plugin,
            FileObject[] filesToTest,
            FileObject targetRoot,
            Map<CreateTestParam, Object> params);

}
