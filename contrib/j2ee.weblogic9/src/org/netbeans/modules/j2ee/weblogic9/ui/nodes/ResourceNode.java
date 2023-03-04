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
