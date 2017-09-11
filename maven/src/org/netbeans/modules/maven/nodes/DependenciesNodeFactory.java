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

package org.netbeans.modules.maven.nodes;

import org.netbeans.modules.maven.DependencyType;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=500)
public class DependenciesNodeFactory implements NodeFactory {
    
    @Override public NodeList<?> createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    private static class NList extends AbstractMavenNodeList<DependenciesNode.DependenciesSet> implements PropertyChangeListener {
        private final NbMavenProjectImpl project;
        private final DependenciesNode.DependenciesSet compile;
        private final DependenciesNode.DependenciesSet runtime;
        private final DependenciesNode.DependenciesSet test;
        private final DependenciesNode.DependenciesSet noncp;
        NList(NbMavenProjectImpl prj) {
            project = prj;
            compile = new DependenciesNode.DependenciesSet(project, DependencyType.COMPILE);
            runtime = new DependenciesNode.DependenciesSet(project, DependencyType.RUNTIME);
            test = new DependenciesNode.DependenciesSet(project, DependencyType.TEST);
            noncp = new DependenciesNode.DependenciesSet(project, DependencyType.NONCP);
        }
        
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                fireChange();
            }
        }
        
        @Override public List<DependenciesNode.DependenciesSet> keys() {
            List<DependenciesNode.DependenciesSet> list = new ArrayList<DependenciesNode.DependenciesSet>();
            list.add(compile);
            if (!runtime.list(false).isEmpty()) {
                list.add(runtime);
            }
            if (!test.list(false).isEmpty()) {
                list.add(test);
            }
            if (!noncp.list(false).isEmpty()) {
                list.add(noncp);
            }
            return list;
        }
        
        @Override public Node node(DependenciesNode.DependenciesSet key) {
            return new DependenciesNode(key);
        }
        
        @Override
        public void addNotify() {
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        @Override
        public void removeNotify() {
            NbMavenProject.removePropertyChangeListener(project, this);
        }
    }
}
