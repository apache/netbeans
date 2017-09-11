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

package org.netbeans.modules.java.api.common.project.ui;

import java.awt.Window;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.customizer.vmo.OptionsDialog;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.actions.FindAction;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;


/**
 * Misc project UI helper methods.
 * 
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class ProjectUISupport{

    private ProjectUISupport() {
    }


    /**
     * Creates a filtered node for class path root.
     * Created node decorates package nodes and file nodes under the Libraries Nodes.
     * It removes all actions from these nodes except of file node's {@link OpenAction}
     * and package node's {@link FindAction}. It also adds Show Javadoc action
     * to both file and package nodes. It also adds Remove Root action to
     * class path roots.
     *
     * @param original the original node
     * @param helper used for implementing Remove Classpath action or null if
     * the node should not have the Remove Classpath action
     * @param classPathId ant property name of classpath to which these classpath root belongs or null if
     * the node should not have the Remove Classpath action
     * @param entryId ant property name of this classpath root or null if
     * the node should not have Remove Classpath action
     * @return filter node
     */
    public static FilterNode createFilteredLibrariesNode(
            @NonNull Node original,
            @NullAllowed UpdateHelper helper,
            @NullAllowed String classPathId,
            @NullAllowed String entryId,
            @NullAllowed String webModuleElementName,
            @NullAllowed ClassPathSupport cs,
            @NullAllowed ReferenceHelper rh) {
        if (helper == null) {
            assert classPathId == null;
            assert entryId == null;
            return ActionFilterNode.forPackage(original);
        } else {
            return  ActionFilterNode.forRoot(
                    original,
                    helper,
                    classPathId,
                    entryId,
                    webModuleElementName,
                    cs,
                    rh,
                    null,
                    null, true);
        }
    }
    
    /**
     * Creates {@link SourceGroup} implementation which can be passed to
     * {@link org.netbeans.spi.java.project.support.ui.PackageView#createPackageView(SourceGroup)}.
     * @param root the classpath root
     * @param displayName the display name presented to user
     * @param icon closed icon
     * @param openIcon opened icon
     * @return
     */
    public static SourceGroup createLibrariesSourceGroup(FileObject root, String displayName, Icon icon, Icon openIcon) {
        return new LibrariesSourceGroup(root, displayName, icon, openIcon);
    }

    /**
     * Create action which opens project properties on the given panel.
     */
    public static AbstractAction createPreselectPropertiesAction(Project project, String nodeName, String panelName) {
        return new JavaSourceNodeFactory.PreselectPropertiesAction(project, nodeName, panelName);
    }

    /**
     * Opens the java VM Options customizer.
     * 
     * @param owner the customizer dialog owner
     * @param options the origin options to be preselected in the customizer
     * @return the options selected in the customizer dialog
     * @throws Exception if it isn't possible to parse the vm options.
     */
    @NonNull
    public static String showVMOptionCustomizer(Window owner, String options) throws Exception {
        return OptionsDialog.showCustomizer(owner, options != null ? options : ""); //NOI18N
    }    
}
