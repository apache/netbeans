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

package org.netbeans.spi.tasklist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.Action;
import org.netbeans.junit.*;
import org.netbeans.modules.tasklist.trampoline.TaskGroupFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;



/** 
 * Tests for Task class.
 * 
 * @author S. Aubrecht
 */
public class TaskTest extends NbTestCase {

    public static final String TASK_GROUP_NAME = "nb-tasklist-unittest";
    public static final String TASK_GROUP_DISPLAY_NAME = "unitTestGroupLabel";
    public static final String TASK_GROUP_DESCRIPTION = "unitTestGroupDescription";
    
    static {
        String[] layers = new String[] {"org/netbeans/spi/tasklist/resources/mf-layer.xml"};//NOI18N
        IDEInitializer.setup(layers,new Object[0]);
    }
    
    public TaskTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        assertNotNull( "make sure we have a task group ready for testing", 
                TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME ) );
    }

    public void testGetters() throws FileStateInvalidException {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = FileUtil.getConfigRoot();
        
        Task t = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getFile() );
        assertNull( t.getURL() );
        assertNull( t.getActions() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertNull( t.getDefaultAction() );
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        t = Task.create(resource, TASK_GROUP_NAME, description, al );
        
        assertEquals( description, t.getDescription() );
        assertEquals( -1, t.getLine() );
        assertEquals( resource, t.getFile() );
        assertNull( t.getURL() );
        assertNull( t.getActions() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertEquals( al, t.getDefaultAction() );

        URL url = resource.getURL();

        t = Task.create(url, TASK_GROUP_NAME, description );

        assertEquals( description, t.getDescription() );
        assertEquals( -1, t.getLine() );
        assertEquals( url, t.getURL() );
        assertNull( t.getFile() );
        assertNull( t.getActions() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertNull( t.getDefaultAction() );

        Action[] actions = new Action[1];
        t = Task.create(url, TASK_GROUP_NAME, description, al, actions );

        assertEquals( description, t.getDescription() );
        assertEquals( -1, t.getLine() );
        assertEquals( url, t.getURL() );
        assertNull( t.getFile() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
        assertEquals( al, t.getDefaultAction() );
        assertSame( actions, t.getActions() );
    }

    public void testNullValues() {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = FileUtil.getConfigRoot();

        try {
            Task.create((FileObject)null, TASK_GROUP_NAME, description, lineNo );
            fail( "resource cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }

        try {
            Task.create((URL)null, TASK_GROUP_NAME, description );
            fail( "resource cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            Task.create(resource, null, description, lineNo );
            fail( "group name cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
        
        try {
            Task.create(resource, TASK_GROUP_NAME, null, lineNo );
            fail( "description cannot be null" );
        } catch( AssertionError e ) {
            //that's what we want
        }
    }

    public void testNegativeLineNumberAllowed() throws FileStateInvalidException {
        String description = "task description";
        int lineNo = -1;
        FileObject resource = FileUtil.getConfigRoot();
        
        Task t = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getFile() );
        assertEquals( TaskGroupFactory.getDefault().getGroup( TASK_GROUP_NAME), t.getGroup() );
    }

    public void testUnknownTaskGroup() throws FileStateInvalidException {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = FileUtil.getConfigRoot();
        
        Task t = Task.create(resource, "unknown task group name", description, lineNo );
        
        assertEquals( description, t.getDescription() );
        assertEquals( lineNo, t.getLine() );
        assertEquals( resource, t.getFile() );
        assertEquals( TaskGroupFactory.getDefault().getDefaultGroup(), t.getGroup() );
    }

    public void testEquals() throws FileStateInvalidException {
        String description = "task description";
        int lineNo = 123;
        FileObject resource = FileUtil.getConfigRoot();

        Task t1 = Task.create(resource, TASK_GROUP_NAME, description, lineNo );
        Task t2 = Task.create(resource, TASK_GROUP_NAME, description, lineNo );

        assertEquals( t1, t2 );
        assertEquals( t1.hashCode(), t2.hashCode() );

        URL url = FileUtil.getConfigRoot().getURL();

        t1 = Task.create(url, TASK_GROUP_NAME, description );
        t2 = Task.create(url, TASK_GROUP_NAME, description );

        assertEquals( t1, t2 );
        assertEquals( t1.hashCode(), t2.hashCode() );
    }
}

