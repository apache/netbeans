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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

