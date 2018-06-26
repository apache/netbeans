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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class JsfSupportImplTest extends TestBaseForTestProject {

    public JsfSupportImplTest(String testName) {
        super(testName);
    }

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JsfSupportImplTest("testELErrorReporting"));
        return suite;
    }

    public void testJsfSupportProviderInGlobalLookup() {
        JsfSupportProvider instance = Lookup.getDefault().lookup(JsfSupportProvider.class);
        assertNotNull(instance);
        assertTrue(instance instanceof JsfSupportProviderImpl);
    }

    public void testGetJsfSupportInstance() throws Exception {
        FileObject file = getWorkFile("testWebProject/web/index.xhtml");
        assertNotNull(file);
        JsfSupportImpl instance = JsfSupportImpl.findFor(file);
        assertNotNull(instance);
    }

    public void testIsTheJsfSupportInstanceCached() throws Exception{
        FileObject file = getWorkFile("testWebProject/web/index.xhtml");
        assertNotNull(file);
        JsfSupportImpl instance1 = JsfSupportImpl.findFor(file);
        assertNotNull(instance1);
        JsfSupportImpl instance2 = JsfSupportImpl.findFor(file);
        assertNotNull(instance2);

        assertSame(instance1, instance2);

    }

}
