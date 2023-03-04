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

package org.netbeans.modules.tasklist.trampoline;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Factory to create new instances of a TaskGroup.
 * TaskGroups definitions are read from XML layers.
 * 
 * @author S. Aubrecht
 */
public final class TaskGroupFactory {
    static final String ATTR_GROUP_NAME = "groupName"; //NOI18N
    static final String ATTR_BUNDLE_NAME = "localizingBundle"; //NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "diplayNameKey"; //NOI18N
    static final String ATTR_DESCRIPTION_KEY = "descriptionKey"; //NOI18N
    static final String ATTR_ICON_KEY = "iconKey"; //NOI18N
    
    private static final String GROUP_LIST_PATH = "TaskList/Groups"; //NOI18N
    
    private static TaskGroupFactory theInstance;
    
    private Lookup.Result<TaskGroup> lookupRes;
    
    private Map<String, TaskGroup> name2group;
    private List<TaskGroup> groups;
    
    private static TaskGroup defaultGroup;
    
    /** Creates a new instance of TaskTypeFactory */
    private TaskGroupFactory() {
    }
    
    /**
     * Creates a new TaskGroup
     * 
     * @param attrs TaskGroup's attributes
     */
    public static TaskGroup create( Map<String,String> attrs ) {
        String groupName = attrs.get( ATTR_GROUP_NAME ); 
        String bundleName = attrs.get( ATTR_BUNDLE_NAME ); 
        String displayNameKey = attrs.get( ATTR_DISPLAY_NAME_KEY ); 
        String descriptionKey = attrs.get( ATTR_DESCRIPTION_KEY ); 
        String iconKey = attrs.get( ATTR_ICON_KEY ); 
        return create( groupName, bundleName, displayNameKey, descriptionKey, iconKey );
    }
    
    /**
     * Creates a new TaskGroup
     * 
     * @param groupName Group's id
     * @param bundleName Resource bundle name
     * @param displayNameKey Bundle key for display name
     * @param descriptionKey Bundle key for description
     * @param iconKey Bundle key for group's icon
     * @return New TaskGroup
     */
    public static TaskGroup create( String groupName, String bundleName, String displayNameKey, String descriptionKey, String iconKey ) {
        ResourceBundle bundle = NbBundle.getBundle( bundleName );
        String displayName = bundle.getString( displayNameKey );
        String description = bundle.getString( descriptionKey );
        String iconPath = bundle.getString( iconKey );
        Image icon = ImageUtilities.loadImage( iconPath );
        
        return new TaskGroup( groupName, displayName, description, icon );
    }
    
    /**
     * @return The one and only instance of this class.
     */
    public static TaskGroupFactory getDefault() {
        if( null == theInstance ) {
            theInstance = new TaskGroupFactory();
        }
        return theInstance;
    }
    
    /**
     * @return The default group to be used for unknown group names.
     */
    public TaskGroup getDefaultGroup() {
        if( null == defaultGroup ) {
            ResourceBundle bundle = NbBundle.getBundle( TaskGroupFactory.class );
            defaultGroup = new TaskGroup( "nb-unknown-group", //NOI18N
                    bundle.getString( "LBL_UnknownGroup" ), //NOI18N
                    bundle.getString( "HINT_UnknownGroup" ), //NOI18N
                    ImageUtilities.loadImage("org/netbeans/modules/tasklist/trampoline/unknown.gif")); //NOI18N
        }
        return defaultGroup;
    }
    
    private void initGroups() {
        synchronized( this ) {
            if( null == name2group ) {
                if( null == lookupRes ) {
                    lookupRes = initLookup();
                    lookupRes.addLookupListener( new LookupListener() {
                        public void resultChanged(LookupEvent ev) {
                            synchronized( TaskGroupFactory.this ) {
                                name2group = null;
                                groups = null;
                            }
                        }
                    });
                }
                int index = 0;
                groups = new ArrayList<TaskGroup>( lookupRes.allInstances() );
                name2group = new HashMap<String,TaskGroup>(groups.size());
                for( TaskGroup tg : groups) {
                    name2group.put( tg.getName(), tg );
                    tg.setIndex( index++ );
                }
            }
        }
    }
    
    /**
     * 
     * @param groupName Group's unique name/id
     * @return TaskGroup for the given name or null if such a group does not exist.
     */
    public TaskGroup getGroup( String groupName ) {
        assert null != groupName;
        synchronized( this ) {
            initGroups();
            return name2group.get( groupName );
        }
    }
    
    /**
     * @return List of all available groups
     */
    public List<? extends TaskGroup> getGroups() {
        synchronized( this ) {
            initGroups();
            return groups;
        }
    }
    
    private Lookup.Result<TaskGroup> initLookup() {
        Lookup lkp = Lookups.forPath( GROUP_LIST_PATH );
        Lookup.Template<TaskGroup> template = new Lookup.Template<TaskGroup>( TaskGroup.class );
        Lookup.Result<TaskGroup> res = lkp.lookup( template );
        return res;
    }
}
