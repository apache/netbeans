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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.nodes.AbstractMavenNodeList;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-maven",position=100)
public class SourcesNodeFactory implements NodeFactory {
    
    @Override
    public NodeList<?> createNodes(Project project) {
        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        return new NList(prj);
    }
    
    private static class NList extends AbstractMavenNodeList<SourceGroup> implements ChangeListener {
        private static final RequestProcessor RP = new RequestProcessor(SourcesNodeFactory.NList.class);
        private final NbMavenProjectImpl project;
        private NList(NbMavenProjectImpl prj) {
            project = prj;
        }
        
        @Override
        public List<SourceGroup> keys() {
            Sources srcs = ProjectUtils.getSources(project);
            SourceGroup[] javagroup = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            return Arrays.asList(javagroup);
        }
        
        @Messages({"# {0} - label of source group", "# {1} - project name", "ERR_WrongSG={0} is owned by project {1}, cannot be used here, see issue #138310 for details."})
        @Override
        public Node node(SourceGroup group) {
            Project owner = FileOwnerQuery.getOwner(group.getRootFolder());
            if (owner != project) {
                if (owner == null) {
                    //#152418 if project for folder is not found, just look the other way..
                    Logger.getLogger(SourcesNodeFactory.class.getName()).log(Level.INFO, "Cannot find a project owner for folder {0}", group.getRootFolder()); //NOI18N
                    return null;
                }
                AbstractNode erroNode = new AbstractNode(Children.LEAF);
                String prjText = ProjectUtils.getInformation(owner).getDisplayName();
                erroNode.setDisplayName(ERR_WrongSG(group.getDisplayName(), prjText));
                return erroNode;
            }
            return PackageView.createPackageView(group);
        }
        
        @Override
        public void addNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.addChangeListener(this);
        }
        
        @Override
        public void removeNotify() {
            Sources srcs = ProjectUtils.getSources(project);
            srcs.removeChangeListener(this);
        }

        @Override
        public void stateChanged(ChangeEvent arg0) {
            //#167372 break the stack trace chain to prevent deadlocks.
            RP.post(new Runnable() {
                @Override
                public void run() {
                    fireChange();
                }
            });
        }
    }
}
