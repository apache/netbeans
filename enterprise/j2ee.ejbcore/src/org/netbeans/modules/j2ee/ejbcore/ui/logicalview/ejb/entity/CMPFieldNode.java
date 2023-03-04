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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.WeakListeners;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


public class CMPFieldNode extends AbstractNode implements PropertyChangeListener, OpenCookie {
    private CmpField field;
    private EntityMethodController controller;
    private static final String CMP_FIELD_ICON = "org/netbeans/modules/j2ee/ejbcore/resources/CMPFieldIcon.gif"; //NOI18N
    private FileObject ddFile;
    
    public CMPFieldNode(CmpField field, EntityMethodController controller, FileObject ddFile) {
        this(field, controller, ddFile, new InstanceContent());
    }

    private CMPFieldNode(CmpField field, EntityMethodController controller, FileObject ddFile, InstanceContent instanceContent) {
        super(Children.LEAF, new AbstractLookup(instanceContent));
        instanceContent.add(this); // for enabling Open action
        
        //TODO: RETOUCHE
//        try {
//            ic.add(DataObject.find(JavaModel.getFileObject(controller.getBeanClass().getResource()))); // for enabling SafeDelete action
//        } catch (DataObjectNotFoundException ex) {
//            // ignore
//        }
//        Method getterMethod = controller.getGetterMethod(controller.getBeanClass(), field.getFieldName());
//        if (getterMethod != null) {
//            ic.add(getterMethod); // for SafeDelete refactoring to find Method element
//        }
        this.field = field;
        this.ddFile = ddFile;
        this.controller = controller;
        field.addPropertyChangeListener(WeakListeners.propertyChange(this, field));
    }
    
    public String getDisplayName(){
        return field.getFieldName();
    }
    
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(CMP_FIELD_ICON);
    }
    
    public boolean canDestroy(){
        return true;
    }
    
    public void destroy() throws IOException{
        controller.deleteField(field, ddFile);
        super.destroy();
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        fireDisplayNameChange(null,null);
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(OpenAction.class),
            null,
        //TODO: RETOUCHE
//            RefactoringActionsFactory.safeDeleteAction().createContextAwareInstance(Utilities.actionsGlobalContext())
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        try {
            DataObject ddFileDO = DataObject.find(ddFile);
            OpenCookie cookie = ddFileDO.getLookup().lookup(OpenCookie.class);
            if (cookie != null) {
                cookie.open();
            }
        } catch (DataObjectNotFoundException donf) {
            Exceptions.printStackTrace(donf);
        }
    }
    
}
