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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
