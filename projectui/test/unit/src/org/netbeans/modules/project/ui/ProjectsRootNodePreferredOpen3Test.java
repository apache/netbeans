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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.modules.project.ui.actions.TestSupport.TestProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectsRootNodePreferredOpen3Test extends ProjectsRootNodePreferredOpenTest {
    private CharSequence log;
    
    public ProjectsRootNodePreferredOpen3Test(String testName) {
        super(testName);
    }            
    
    @Override
    Lookup createLookup(TestProject project, Object instance) {
        return Lookups.fixed(instance, project, new MyTestProjectAdditions(project));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log = Log.enable("", Level.WARNING);
    }

    private static final class MyTestProjectAdditions
    implements LogicalViewProvider {
        private TestProject project;
        public MyTestProjectAdditions(TestProject project) {
            this.project = project;
        }
        
        public boolean canSearch() {
            return false;
        }

        public Iterator<DataObject> objectsToSearch() {
            return Collections.<DataObject>emptyList().iterator();
        }

        public Node createLogicalView() {
            return new AbstractNode(Children.LEAF, Lookups.singleton(project));
        }

        public Node findPath(Node root, Object target) {
            return null;
        }
    }
    
}
