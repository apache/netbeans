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
package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Image;
import java.beans.IntrospectionException;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.actions.JAXBRefreshAction;
import org.netbeans.modules.xml.jaxb.actions.JAXBWizardOpenXSDIntoEditorAction;
import org.netbeans.modules.xml.jaxb.ui.JAXBWizardSchemaNode.JAXBWizardSchemaNodeChildren;
import org.netbeans.modules.xml.jaxb.spi.JAXBWizModuleConstants;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author gpatil
 */
public class JAXBBindingSupportFileNode extends AbstractNode {
    private static Action[] actions = null;    
    private FileObject xsdFileObject;
    private String origLocation ;
    private Boolean origLocationType ;
    private FileObject localSchemaRoot;
    private Node nodeDelegate;
    private FileChangeListener fcl;
    
    public JAXBBindingSupportFileNode(Project prj, FileObject xsdFile, 
            FileObject locSchemaRoot, Boolean origLocType, 
            String origLocation) throws IntrospectionException {
        super( Children.LEAF, createLookup( xsdFile, prj ) );
        
        this.xsdFileObject = xsdFile;
        this.origLocation = origLocation;
        this.origLocationType = origLocType;
        this.localSchemaRoot = locSchemaRoot;
        
        this.setValue(JAXBWizModuleConstants.ORIG_LOCATION, this.origLocation);
        this.setValue(JAXBWizModuleConstants.ORIG_LOCATION_TYPE, this.origLocationType);
        this.setValue(JAXBWizModuleConstants.LOC_SCHEMA_ROOT, this.localSchemaRoot);
        this.fcl =  new FileChangeListener() {
            public void fileAttributeChanged(FileAttributeEvent fae) {
            }
            
            public void fileChanged(FileEvent fileAttributeEvent) {
            }
            
            public void fileDataCreated(FileEvent fileAttributeEvent) {
            }
            
            public void fileDeleted(FileEvent fileAttributeEvent) {
                Node parent = JAXBBindingSupportFileNode.this.getParentNode();
                if (parent != null) {
                    JAXBWizardSchemaNodeChildren xsdChildren =
                            (JAXBWizardSchemaNodeChildren)getChildren();
                    xsdChildren.refreshChildren();
                    JAXBBindingSupportFileNode.this.fireNodeDestroyed();
                }
            }
            
            public void fileFolderCreated(FileEvent fe) {
            }
            
            public void fileRenamed(FileRenameEvent frenameEvent) {
            }
        } ;
        
        FileChangeListener weakFcl = FileUtil.weakFileChangeListener(fcl, 
                xsdFileObject);
        xsdFileObject.addFileChangeListener(weakFcl);
        initActions();
        this.setShortDescription(xsdFile.getPath());
    }
    
    public void setNodeDelegate(Node delegate){
        this.nodeDelegate = delegate;
    }
    
    private static Lookup createLookup(FileObject xsdFileObject, 
                                        Project prj) {
        return Lookups.fixed( new Object[] {
            xsdFileObject,
            prj
        } );
    }

    public String getName() {
        return xsdFileObject.getName();
    }
    
    public String getDisplayName() {
        return xsdFileObject.getNameExt();
    }
    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
                SystemAction.get(JAXBWizardOpenXSDIntoEditorAction.class),
                SystemAction.get(JAXBRefreshAction.class)
            };
        }
    }
    
    public Action[] getActions(boolean b) {
        return actions;
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(JAXBWizardOpenXSDIntoEditorAction.class);
    }
    
    public Image getIcon(int type) {
        if (this.nodeDelegate != null){
            return this.nodeDelegate.getIcon(type);
        }
        return ImageUtilities.loadImage(
                "org/netbeans/modules/xml/jaxb/resources/XML_file.png" );//NOI18N
    }
    
    public Image getOpenedIcon(int type) {
        if (this.nodeDelegate != null){
            return this.nodeDelegate.getOpenedIcon(type);
        }
        
        return ImageUtilities.loadImage(
                "org/netbeans/modules/xml/jaxb/resources/XML_file.png" );//NOI18N  
    }
 }
