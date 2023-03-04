/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        return new PreselectPropertiesAction(project, nodeName, panelName);
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
