/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.io.CharConversionException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.Util;
import static org.netbeans.modules.apisupport.project.layers.Bundle.*;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.ImageDecorator;
import org.openide.filesystems.StatusDecorator;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;

/**
 * Displays two views of a layer.
 * @author Jesse Glick
 */
public final class LayerNode extends FilterNode implements Node.Cookie {
    
    private final boolean specialDisplayName;

    public LayerNode(LayerHandle handle) {
        this(getDataNode(handle), handle, true);
    }
    
    LayerNode(Node delegate, LayerHandle handle, boolean specialDisplayName) {
        super(delegate, Children.create(new LayerChildren(handle), true));
        this.specialDisplayName = specialDisplayName;
    }
    
    private static Node getDataNode(LayerHandle handle) {
        FileObject layer = handle.getLayerFile();
        try {
            return DataObject.find(layer).getNodeDelegate();
        } catch (DataObjectNotFoundException e) {
            assert false : e;
            return Node.EMPTY;
        }
    }
    
    private static final class LayerChildren extends ChildFactory<DataObject> {

        private final LayerHandle handle;
        
        LayerChildren(LayerHandle handle) {
            this.handle = handle;
        }

        @Messages({"LBL_this_layer=<this layer>", "LBL_this_layer_in_context=<this layer in context>"})
        @Override protected boolean createKeys(List<DataObject> keys) {
            handle.setAutosave(true);
            FileObject layer = handle.getLayerFile();
            if (layer == null) { // #180872, #212541
                return true;
            }
            Project p = FileOwnerQuery.getOwner(layer);
            if (p == null) { // #175861: inside JAR etc.
                return true;
            }
            FileSystem layerfs = handle.layer(false);
            try {
                if (layerfs != null) {
                    keys.add(DataObject.find(badge(layerfs, handle.getLayerFile(), LBL_this_layer(), null).getRoot()));
                }
                LayerHandle h = LayerHandle.forProject(p);
                if (layer.equals(h.getLayerFile())) {
                    h.setAutosave(true); // #135376
                    keys.add(DataObject.find(badge(LayerUtils.getEffectiveSystemFilesystem(p), handle.getLayerFile(), LBL_this_layer_in_context(), handle.layer(false)).getRoot()));
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
            return true;
        }

        @Override protected Node createNodeForKey(DataObject key) {
            return new LayerFilterNode(key.getNodeDelegate());
        }

    }
    
    /**
     * Add badging support to the plain layer.
     */
    private static FileSystem badge(final @NonNull FileSystem base, final @NonNull FileObject layer, final @NonNull String rootLabel, final @NullAllowed FileSystem highlighted) {
        class BadgingMergedFileSystem extends LayerFileSystem {
            BadgingMergedFileSystem() {
                super(new FileSystem[] {base});
                setPropagateMasks(true);
                status.addFileStatusListener(new FileStatusListener() {
                    @Override public void annotationChanged(FileStatusEvent ev) {
                        fireFileStatusChanged(ev);
                    }
                });
                // XXX loc/branding suffix?
                addFileChangeListener(new FileChangeListener() { // #65564
                    private void fire() {
                        fireFileStatusChanged(new FileStatusEvent(BadgingMergedFileSystem.this, true, true));
                    }
                    @Override public void fileAttributeChanged(FileAttributeEvent fe) {
                        fire();
                    }
                    @Override public void fileChanged(FileEvent fe) {
                        fire();
                    }
                    @Override public void fileDataCreated(FileEvent fe) {
                        fire();
                    }
                    @Override public void fileDeleted(FileEvent fe) {
                        fire();
                    }
                    @Override public void fileFolderCreated(FileEvent fe) {
                        fire();
                    }
                    @Override public void fileRenamed(FileRenameEvent fe) {
                        fire();
                    }
                });
            }
            
            class Dec implements StatusDecorator, ImageDecorator {
                @Override public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                    String nonHtmlLabel = status.annotateName(name, files);
                    if (files.size() == 1 && ((FileObject) files.iterator().next()).isRoot()) {
                        nonHtmlLabel = rootLabel;
                    }
                    String htmlLabel;
                    try {
                        htmlLabel = XMLUtil.toElementContent(nonHtmlLabel);
                    } catch (CharConversionException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        htmlLabel = nonHtmlLabel;
                    }
                    boolean deleted = false;
                    for( FileObject fo : files ) {
                        if( fo.getNameExt().endsWith(LayerUtil.HIDDEN) ) {
                            deleted = true;
                            break;
                        }
                    }
                    if (highlighted != null) {
                        // Boldface resources which do come from this project.
                        boolean local = false;
                        for (FileObject f : files) {
                            if (!f.isRoot() && highlighted.findResource(f.getPath()) != null) {
                                local = true;
                                break;
                            }
                        }
                        if (local) {
                            htmlLabel = "<b>" + htmlLabel + "</b>"; // NOI18N
                        }
                    }
                    if( deleted ) {
                        htmlLabel = "<s>" + htmlLabel + "</s>"; //NOI18N
                    }
                    return htmlLabel;
                }
                @Override public String annotateName(String name, Set<? extends FileObject> files) {
                    // Complex to explain why this is even called, but it is.
                    // Weird b/c hacks in the way DataNode.getHtmlDisplayName works.
                    return name;
                }
                @Override public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
                    return status.annotateIcon(icon, iconType, files);
                }
            }
            
            @Override
            public StatusDecorator getDecorator() {
                return new Dec();
            }
            @Override
            public String getDisplayName() {
                return FileUtil.getFileDisplayName(layer);
            }
            
            public SystemAction[] getActions(Set<FileObject> foSet) {
                return new SystemAction[] {
                    SystemAction.get(PickNameAction.class),
                    SystemAction.get(PickIconAction.class),
                    SystemAction.get(OpenLayerFilesAction.class),
                };
            }
        }
        return new BadgingMergedFileSystem();
        /* XXX loc/branding suffix possibilities:
        Matcher m = Pattern.compile("(.*" + "/)?[^_/.]+(_[^/.]+)?(\\.[^/]+)?").matcher(u);
        assert m.matches() : u;
        suffix = m.group(2);
        if (suffix == null) {
            suffix = "";
        }
        status.setSuffix(suffix);
         */
    }
    
    
    @Messages("LayerNode_label=XML Layer")
    @Override
    public String getDisplayName() {
        if (specialDisplayName) {
            return LayerNode_label();
        } else {
            return super.getDisplayName();
        }
    }

    public @Override String getHtmlDisplayName() { // #193262
        if (specialDisplayName) {
            return null;
        } else {
            return getOriginal().getHtmlDisplayName();
        }
    }
    
}
