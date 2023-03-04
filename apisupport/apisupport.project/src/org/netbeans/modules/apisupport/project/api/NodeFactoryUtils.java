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
