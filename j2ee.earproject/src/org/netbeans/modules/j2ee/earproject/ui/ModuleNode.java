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

package org.netbeans.modules.j2ee.earproject.ui;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.actions.OpenModuleProjectAction;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Represents one node in the <em>J2EE Modules</em> node in the EAR project's
 * logical view.
 *
 * @author vkraemer
 * @author Ludovic Champenois
 */
public final class ModuleNode extends AbstractNode implements Node.Cookie {
    
    /** Package-private for unit tests <strong>only</strong>. */
    static final String MODULE_NODE_NAME = "module.node"; // NOI18N
    
    private final FileObject projectDirectory;
    private final ClassPathSupport.Item key;
    private ClassPathSupport cs;
    private final UpdateHelper updateHelper;
    private EarProject project;
    boolean isWAR;
    
    public ModuleNode(final ClassPathSupport.Item key, final FileObject projectDirectory, 
            EarProject project, UpdateHelper updateHelper, ClassPathSupport cs) {
        super(Children.LEAF);
        assert key.getType() == ClassPathSupport.Item.TYPE_ARTIFACT;
        this.key = key;
        this.projectDirectory = projectDirectory;
        setName(ModuleNode.MODULE_NODE_NAME);
        String value = project.evaluator().evaluate(key.getReference());
        isWAR = value != null && value.endsWith(".war"); // NOI18N
        String dispName = EarProjectProperties.getCompletePathInArchive(project, key);
        setDisplayName(dispName);
        setShortDescription(NbBundle.getMessage(ModuleNode.class, "HINT_ModuleNode"));
        this.project = project;
        this.updateHelper = updateHelper;
        this.cs = cs;
        getCookieSet().add(this);
    }
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
                SystemAction.get(OpenModuleProjectAction.class),
                SystemAction.get(RemoveAction.class)
            };
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenModuleProjectAction.class);
    }
    
    @Override
    public Image getIcon(int type) {
        // XXX the "algorithm" based on the ant property name - in the case of
        // application client; is little odd. Also the rest is rather unclear.
        if (isWAR) { // NOI18N
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/WebModuleNode.gif");//NOI18N
        } else if (key.getReference().indexOf("j2ee-module-car") > 0) { //NOI18N
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/CarModuleNodeIcon.gif");//NOI18N
        } else {
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/earproject/ui/resources/EjbModuleNodeIcon.gif");//NOI18N
        }
    }
    
    @Override
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    void removeFromJarContent() {
        EarProjectProperties.removeJ2eeSubprojects(project, new Project[]{key.getArtifact().getProject()});
    }
    
    public ClassPathSupport.Item getVCPI() {
        return key;
    }
    
    // Handle copying and cutting specially:
    @Override
    public boolean canCopy() {
        return false;
    }
  
}
