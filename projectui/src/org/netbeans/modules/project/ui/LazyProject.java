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
package org.netbeans.modules.project.ui;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Dummy project that shows a wait node while the real project list is 
 * loaded
 *
 * @author Tim Boudreau, Jaroslav Tulach
 */
final class LazyProject implements
Project, ProjectInformation, LogicalViewProvider, RecommendedTemplates {
    URL url;
    String displayName;
    ExtIcon icon;

    public LazyProject(URL url, String displayName, ExtIcon icon) {
        super();
        this.url = url;
        this.displayName = displayName;
        this.icon = icon;
    }

    @Override
    public FileObject getProjectDirectory() {
        FileObject fo = URLMapper.findFileObject(url);
        if (fo == null) {
            OpenProjectList.LOGGER.warning("Project dir with " + url + " not found!");
            fo = FileUtil.createMemoryFileSystem().getRoot();
        }
        return fo;
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(this);
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Icon getIcon() {
        return icon.getIcon();
    }

    @Override
    public Project getProject() {
        return this;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public Node createLogicalView() {
        return new ProjNode(Lookups.singleton(this));
    }

    @Override
    public Node findPath(Node root, Object target) {
        return null;
    }

    @Override
    public String[] getRecommendedTypes() {
        return new String[] { "simple-files" }; // NOI18N
    }
    
    
    private final class ProjNode extends AbstractNode {
        public ProjNode(Lookup lookup) {
            super(new ProjCh(), lookup);
            
            setName(url.toExternalForm());
            setDisplayName(displayName);
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.icon2Image(icon.getIcon());
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action getPreferredAction() {
            OpenProjectList.preferredProject(LazyProject.this);
            return super.getPreferredAction();
        }

        @Override
        public boolean hasCustomizer() {
            return false;
        }

        @Override
        public Action[] getActions(boolean context) {
            OpenProjectList.preferredProject(LazyProject.this);
            return new Action[] { 
                SystemAction.get(LazyProjectInitializing.class),
                CommonProjectActions.closeProjectAction()
            };
        }
    } // end of ProjNode
    private final class ProjCh extends Children.Array {
        @Override
        protected Collection<Node> initCollection() {
            AbstractNode n = new AbstractNode(Children.LEAF);
            n.setName("init"); // NOI18N
            n.setDisplayName(NbBundle.getMessage(ProjCh.class, "MSG_ProjChInit")); 
            n.setIconBaseWithExtension("org/netbeans/modules/project/ui/resources/wait.gif");
            return Collections.singletonList((Node)n);
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            OpenProjectList.preferredProject(LazyProject.this);
        }
    }
}
