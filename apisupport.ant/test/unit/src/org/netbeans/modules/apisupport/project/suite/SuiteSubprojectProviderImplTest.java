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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.suite;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.spi.project.SubprojectProvider;

/**
 * @author Martin Krauskopf
 */
public class SuiteSubprojectProviderImplTest extends TestBase {

    public SuiteSubprojectProviderImplTest(String testName) {
        super(testName);
    }
    
    public void testGetSubprojects() throws Exception {
        SuiteProject s = generateSuite("suite");
        SubprojectProvider spp = s.getLookup().lookup(SubprojectProvider.class);
        assertEquals("suite doesn't have any submodules", 0, spp.getSubprojects().size());
        NbModuleProject module1 = generateSuiteComponent(s, "module1");
        assertEquals("suite has one submodule", 1, spp.getSubprojects().size());
        SuiteUtils.removeModuleFromSuite(module1);
        assertEquals("suite doesn't have any submodules", 0, spp.getSubprojects().size());
        generateSuiteComponent(s, "module2");
        generateSuiteComponent(s, "module3");
        assertEquals("suite has two submodules", 2, spp.getSubprojects().size());
    }
    
    public void testChangeListener() throws Exception {
        SuiteProject s = generateSuite("suite");
        SubprojectProvider spp = s.getLookup().lookup(SubprojectProvider.class);
        SPPChangeListener l = new SPPChangeListener();
        spp.addChangeListener(l);
        NbModuleProject module1 = generateSuiteComponent(s, "module1");
        assertTrue("change was noticed", l.changed);
        assertEquals("suite has one submodule", 1, spp.getSubprojects().size());
        l.changed = false;
        SuiteUtils.removeModuleFromSuite(module1);
        assertTrue("change was noticed", l.changed);
        l.changed = false;
        assertEquals("suite doesn't have any submodules", 0, spp.getSubprojects().size());
        spp.removeChangeListener(l);
        generateSuiteComponent(s, "module2");
        assertFalse("change was noticed", l.changed);
        assertEquals("suite has one submodule", 1, spp.getSubprojects().size());
    }
    
    private final class SPPChangeListener implements ChangeListener {
        
        boolean changed;
        
        public void stateChanged(ChangeEvent e) {
            changed = true;
        }
        
    }
    
}
