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
