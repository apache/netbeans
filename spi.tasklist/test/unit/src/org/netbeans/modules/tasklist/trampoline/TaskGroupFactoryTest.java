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

