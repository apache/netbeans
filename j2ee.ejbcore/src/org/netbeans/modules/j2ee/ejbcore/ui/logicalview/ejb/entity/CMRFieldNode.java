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
