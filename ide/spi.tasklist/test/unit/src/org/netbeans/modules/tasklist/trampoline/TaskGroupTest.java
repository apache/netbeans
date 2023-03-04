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
import java.awt.image.BufferedImage;
import org.netbeans.junit.*;



/** 
 * Tests for TaskGroup class.
 * 
 * @author S. Aubrecht
 */
public class TaskGroupTest extends NbTestCase {

    public TaskGroupTest (String name) {
        super (name);
    }

    public void testGetters() {
        String name = "group name";
        String displayName = "group display name";
        String description = "group description";
        Image icon = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        
        TaskGroup group = new TaskGroup( name, displayName, description, icon );
        
        assertEquals( name, group.getName() );
        assertEquals( displayName, group.getDisplayName() );
        assertEquals( description, group.getDescription() );
        assertEquals( icon, group.getIcon() );
    }

    public void testNullValues() {
        String name = "group name";
        String displayName = "group display name";
        String description = "group description";
        Image icon = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        
        try {
            new TaskGroup(null, displayName, description, icon );
            fail( "name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            new TaskGroup(name, null, description, icon );
            fail( "display name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            new TaskGroup(name, displayName, null, icon );
        } catch( AssertionError e ) {
            fail( "null description is allowed" );
        }
        
        try {
            new TaskGroup(name, displayName, description, null );
            fail( "icon cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
    }
}

