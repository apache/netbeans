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

import java.util.List;
import org.netbeans.junit.*;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class TaskGroupFactoryTest extends NbTestCase {
    
    public static final String TASK_GROUP_NAME_A = "nb-tasklist-unittestA";
    public static final String TASK_GROUP_DISPLAY_NAME_A = "unitTestGroupLabelA";
    public static final String TASK_GROUP_DESCRIPTION_A = "unitTestGroupDescriptionA";
    
    public static final String TASK_GROUP_NAME_B = "nb-tasklist-unittestB";
    public static final String TASK_GROUP_DISPLAY_NAME_B = "unitTestGroupLabelB";
    public static final String TASK_GROUP_DESCRIPTION_B = "unitTestGroupDescriptionB";
    
    public static final String TASK_GROUP_NAME_C = "nb-tasklist-unittestC";
    public static final String TASK_GROUP_DISPLAY_NAME_C = "unitTestGroupLabelC";
    public static final String TASK_GROUP_DESCRIPTION_C = "unitTestGroupDescriptionC";
    
    static {
        String[] layers = new String[] {"org/netbeans/modules/tasklist/trampoline/resources/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers,new Object[0]);
    }

    public TaskGroupFactoryTest (String name) {
        super (name);
    }

    public void testGetGroup() {
        TaskGroupFactory factory = TaskGroupFactory.getDefault();
        
        List<? extends TaskGroup> groups = factory.getGroups();
        
        assertEquals( 2, groups.size() );
        
        TaskGroup tgA = groups.get( 0 );
        assertEquals( TASK_GROUP_NAME_A, tgA.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_A, tgA.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_A, tgA.getDescription());
        
        TaskGroup tgB = groups.get( 1 );
        assertEquals( TASK_GROUP_NAME_B, tgB.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_B, tgB.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_B, tgB.getDescription());
        
        assertFalse( tgA.equals( tgB ) );
        
        TaskGroup group = factory.getGroup( TASK_GROUP_NAME_A );
        assertNotNull( group );
        assertEquals( TASK_GROUP_NAME_A, group.getName());
        
        group = factory.getGroup( TASK_GROUP_NAME_B );
        assertNotNull( group );
        
        group = factory.getGroup( "unknown group name" );
        assertNull( group );
        
        assertNotNull( factory.getDefaultGroup() );
        
        try {
            factory.getGroup( null );
            fail( "null group name is not acceptable" );
        } catch( AssertionError e ) {
            //expected
        }
    }

    public void testCreate() {
        TaskGroup group = TaskGroupFactory.create( TASK_GROUP_NAME_C, 
                "org.netbeans.modules.tasklist.trampoline.resources.Bundle", 
                "LBL_unittest_groupC", 
                "HINT_unittest_groupC", 
                "ICON_unittestgroupC");

        assertNotNull( group );
        assertEquals( TASK_GROUP_NAME_C, group.getName());
        assertEquals( TASK_GROUP_DISPLAY_NAME_C, group.getDisplayName());
        assertEquals( TASK_GROUP_DESCRIPTION_C, group.getDescription());
    }
}

