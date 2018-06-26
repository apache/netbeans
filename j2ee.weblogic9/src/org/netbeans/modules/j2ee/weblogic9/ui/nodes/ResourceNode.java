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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.awt.Image;

import javax.swing.Action;

import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterAction;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;


/**
 * @author ads
 *
 */
class ResourceNode extends AbstractItemNode {
    
    enum ResourceNodeType{
        RESOURCE,
        JDBC,
        JDBC_RESOURCES,
        JDBC_POOL,
        CONNECTORS,
        CONNECTOR_RESOURCES,
        CONNECTION_POOLS,
        ADMIN_OBJ_RESOURCE,
        JAVA_MAIL,
        LIBRARY,
        TUXEDO,
        WTC_SERVER,
        JOLT_CONNECTION_POOL
        ;
    }
    
    private static final String RESOURCES_ICON = 
        "org/netbeans/modules/j2ee/weblogic9/resources/resources.gif"; // NOI18N
    
    private static final String JDBC_RESOURCE_ICON = 
        "org/netbeans/modules/j2ee/weblogic9/resources/jdbc.gif"; // NOI18N
    
    private static final String CONNECTOR_ICON =
        "org/netbeans/modules/j2ee/weblogic9/resources/connector.gif"; // NOI18N
    
    private static final String JAVAMAIL_ICON =
        "org/netbeans/modules/j2ee/weblogic9/resources/javamail.gif"; // NOI18N
    
    private static final String LIBRARY_ICON =
        "org/netbeans/modules/j2ee/weblogic9/resources/libraries.gif"; // NOI18N
    

    ResourceNode( Children children , ResourceNodeType type , String name , 
            Cookie cookie) 
    {
        this( children , type , name , cookie , null );
    }
    
    ResourceNode( Children children , ResourceNodeType type , String name , 
            Cookie cookie, String tooltip ) 
    {
        super(children);
        setDisplayName(name);
        if ( tooltip != null ){
            setShortDescription( tooltip );
        }
        this.resourceType= type;
        if ( cookie != null){
            getCookieSet().add( cookie );
        }
    }
    
    ResourceNode( Children children , ResourceNodeType type , String name , 
            String tooltip) 
    {
        this( children , type , name , null , tooltip);
    }
    
    ResourceNode( Children children , ResourceNodeType type , String name ) 
    {
        this( children , type , name , null , null);
    }
    
    
    ResourceNode(ChildFactory<? extends AbstractNode> childFactory ,
            ResourceNodeType type , String name ) 
    {
        super(childFactory, name);
        setDisplayName( name);
        this.resourceType= type; 
    }
    
    /* (non-Javadoc)
     * @see org.openide.nodes.AbstractNode#getIcon(int)
     */
    @Override
    public Image getIcon( int type ) {
        switch ( resourceType ){
            case RESOURCE:
                return ImageUtilities.loadImage(RESOURCES_ICON);
            case JDBC:
                return ImageUtilities.loadImage(JDBC_RESOURCE_ICON);
            case CONNECTORS:
                return ImageUtilities.loadImage(CONNECTOR_ICON);
            case JAVA_MAIL:
                return ImageUtilities.loadImage(JAVAMAIL_ICON);
            case LIBRARY:
                return ImageUtilities.loadImage(LIBRARY_ICON);
            default:
                return getIconDelegate().getIcon(type);
                
        }
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions( context);
        UnregisterCookie cookie = getCookieSet().getCookie( UnregisterCookie.class );
        if ( cookie!= null ){
            Action[] result = new Action[ actions.length +1 ];
            System.arraycopy(actions, 0, result , 0, actions.length);
            result[ actions.length  ] = SystemAction.get(UnregisterAction.class);
            return result;
        }
        return actions;
    }
    
    private Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }
    
    private ResourceNodeType resourceType;
}
