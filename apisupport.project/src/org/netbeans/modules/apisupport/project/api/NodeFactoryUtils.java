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

package org.netbeans.modules.apisupport.project.api;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.layers.LayerNode;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

// XXX still a fair amount of duplicated code in ImportantFilesNodeFactory in apisupport.ant and maven.apisupport

/**
 * Utility class for exposing creation of layer, services or other 
 * special purpose nodes.
 * @author mkleint
 */
public final class NodeFactoryUtils {
    
    /** Creates a new instance of NodeFactoryUtils */
    private NodeFactoryUtils() { }
    
    /**
     * creates a Node for displaying the layer content.
     * @param project 
     * @return node or null.
     */
    public static Node createLayersNode(Project project) {
        NbModuleProvider prv = project.getLookup().lookup(NbModuleProvider.class);
        if (prv != null && prv.getManifestFile() != null) {
            return createLayerNode(project);
        }
        return null;
    }
    
    private static Node createLayerNode(Project prj) {
        LayerHandle handle = LayerHandle.forProject(prj);
        if (handle != null && handle.getLayerFile() != null) {
            return new SpecialFileNode(new LayerNode(handle), null);
}
        // XXX consider also displaying node for generated-layer.xml where applicable
        return null;
    }

    public static Node createSpecialFileNode(Node orig, String displayName) {
        return new SpecialFileNode(orig, displayName);
    }

    /**
     * Node to represent some special file in a project.
     * Mostly just a wrapper around the normal data node.
     */
    private static final class SpecialFileNode extends FilterNode {

        private final String displayName;

        public SpecialFileNode(Node orig, String displayName) {
            super(orig);
            this.displayName = displayName;
        }

        public @Override String getDisplayName() {
            if (displayName != null) {
                return displayName;
            } else {
                return super.getDisplayName();
            }
        }

        public @Override boolean canRename() {
            return false;
        }

        public @Override boolean canDestroy() {
            return false;
        }

        public @Override boolean canCut() {
            return false;
        }

        public @Override String getHtmlDisplayName() {
            String result = null;
            DataObject dob = getLookup().lookup(DataObject.class);
            if (dob != null) {
                Set<FileObject> files = dob.files();
                result = computeAnnotatedHtmlDisplayName(getDisplayName(), files);
            }
            return result;
        }

    }

    /**
     * Annotates <code>htmlDisplayName</code>, if it is needed, and returns the
     * result; <code>null</code> otherwise.
     */
    public static String computeAnnotatedHtmlDisplayName(
            final String htmlDisplayName, final Set<? extends FileObject> files) {

        String result = null;
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = (FileObject) files.iterator().next();
                StatusDecorator stat = fo.getFileSystem().getDecorator();
                String annotated = stat.annotateNameHtml(htmlDisplayName, files);
                // Make sure the super string was really modified (XXX why?)
                if (annotated != null && !htmlDisplayName.equals(annotated)) {
                    result = annotated;
                }
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return result;
    }

}
