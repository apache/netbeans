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
                        Iterator it = files.iterator();
                        while (it.hasNext()) {
                            FileObject f = (FileObject) it.next();
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
