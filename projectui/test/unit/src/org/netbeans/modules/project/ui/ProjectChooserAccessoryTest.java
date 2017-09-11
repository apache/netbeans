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

package org.netbeans.modules.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport.ChangeableLookup;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectChooserAccessoryTest extends NbTestCase {
    
    public ProjectChooserAccessoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    /**The cycles in project dependencies should be handled gracefully:
     */
    public void testAddSubprojects() throws Exception {
        ChangeableLookup l1 = new ChangeableLookup();
        ChangeableLookup l2 = new ChangeableLookup();
        Project p1 = new TestProject(l1);
        Project p2 = new TestProject(l2);
        
        Set<Project> subprojects1 = new HashSet<Project>();
        Set<Project> subprojects2 = new HashSet<Project>();
        
        subprojects1.add(p2);
        subprojects2.add(p1);
        
        l1.change(new SubprojectProviderImpl(subprojects1));
        l2.change(new SubprojectProviderImpl(subprojects2));
        
        List<Project> result = new ArrayList<Project>();
        //#101227
        ProjectChooserAccessory acc = new ProjectChooserAccessory(new JFileChooser(), false);
        acc.modelUpdater.addSubprojects(p1, result, new HashMap<Project,Set<? extends Project>>());
        
        assertTrue(new HashSet<Project>(Arrays.asList(p1, p2)).equals(new HashSet<Project>(result)));
    }
    
    private final class TestProject implements Project {
        
        private Lookup l;
        
        public TestProject(Lookup l) {
            this.l = l;
        }
        
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Should not be called in this test.");
        }
        
        public Lookup getLookup() {
            return l;
        }
    }
    
    private static final class SubprojectProviderImpl implements SubprojectProvider {
        
        private Set<Project> subprojects;
        
        public SubprojectProviderImpl(Set<Project> subprojects) {
            this.subprojects = subprojects;
        }
        
        public Set<? extends Project> getSubprojects() {
            return subprojects;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
        
    }
}
