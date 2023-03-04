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
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.WeakListeners;
import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;


public class CMRFieldNode extends AbstractNode implements PropertyChangeListener, OpenCookie {
    
    private static final String CMR_FIELD_ICON = "org/netbeans/modules/j2ee/ejbcore/resources/CMRFieldIcon.gif"; //NOI18N

    private final CmrField field;
    private final EntityMethodController controller;
    private final FileObject ddFile;
    
    public CMRFieldNode(CmrField field, EntityMethodController controller, FileObject ddFile) {
        super(Children.LEAF);
        this.field = field;
        this.ddFile = ddFile;
        this.controller = controller;
        field.addPropertyChangeListener(WeakListeners.propertyChange(this, field));
    }
    
    public String getDisplayName(){
        return field.getCmrFieldName();
    }
    
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(CMR_FIELD_ICON);
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
    
    public @Override <T extends Node.Cookie> T getCookie(Class<T> type) {
        if(OpenCookie.class.equals(type)) {
            return type.cast(this);
        }
        return super.getCookie(type);
    }
    
    public Action[] getActions(boolean context) {
        return new SystemAction[] {
            SystemAction.get(OpenAction.class),
                    null,
                    SystemAction.get(DeleteAction.class),
        };
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        //TODO: RETOUCHE
//        List methods = controller.getMethods(field);
//        if (!methods.isEmpty()) {
//            Method getMethod = (Method) methods.get(0);
//            OpenCookie cookie = (OpenCookie) JMIUtils.getCookie(getMethod, OpenCookie.class);
//            if(cookie != null){
//                cookie.open();
//            }
//        }
    }
}
