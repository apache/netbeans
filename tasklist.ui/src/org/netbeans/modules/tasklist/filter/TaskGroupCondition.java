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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.tasklist.trampoline.TaskGroup;
import org.netbeans.modules.tasklist.ui.checklist.CheckList;
import org.openide.util.NbBundle;

/**
 * "Task Group is" - condition
 *
 * @author tl
 */
class TaskGroupCondition extends FilterCondition {
    /**
     * Creates an array of filter conditions for the specified property
     *
     * @param index index of the property
     */
    public static TaskGroupCondition[] createConditions() {
        return new TaskGroupCondition[] {
            new TaskGroupCondition()
        };
    };
    
    
    private boolean[] groupState;
    private TaskGroup[] groups;
    
    /**
     * Creates a new instance
     *
     * @param prop index of a property
     */
    public TaskGroupCondition() {
        List<TaskGroup> groupList = new ArrayList<TaskGroup>( TaskGroup.getGroups() );
        groups = groupList.toArray( new TaskGroup[groupList.size()] );
        groupState = new boolean[groups.length];
        Arrays.fill(groupState, true);
    }
    
        
    public TaskGroupCondition(final TaskGroupCondition rhs) {
        super(rhs);
        this.groups = new TaskGroup[ rhs.groups.length ];
        this.groupState = new boolean[ rhs.groupState.length ];
        assert this.groups.length == this.groupState.length;
        for( int i=0; i<groups.length; i++ ) {
            groups[i] = rhs.groups[i];
            groupState[i] = rhs.groupState[i];
        }
    }
    
    public Object clone() {
        return new TaskGroupCondition(this);
    }
    
    public boolean isTrue(Object o1) {
        TaskGroup g = (TaskGroup) o1;
        for( int i=0; i<groups.length; i++ ) {
            if( groups[i].equals( g ) ) {
                return groupState[i];
            }
        }
        return true; //new or unknown Groups are always visible
    }
    
    public JComponent createConstantComponent() {
        String[] descs = new String[groups.length];
        for (int i = 0; i < descs.length; i++) {
            descs[i] = groups[i].getDescription();
        }
        CheckList list = new CheckList(
                groupState, groups, descs
        );
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            UIManager.getBorder("TextField.border"), // NOI18N
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        panel.add(list, BorderLayout.CENTER);
        panel.setToolTipText(Util.getString("group_desc")); //NOI18N

        list.getAccessibleContext().setAccessibleName(Util.getString("LBL_PriorityCheckList"));
        list.getAccessibleContext().setAccessibleDescription(Util.getString("LBL_PriorityCheckList"));
        
        list.getModel().addListDataListener( new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
            }
            
            public void intervalRemoved(ListDataEvent e) {
            }
            
            public void contentsChanged(ListDataEvent e) {
                boolean atLeastOneGroupSelected = false;
                for( int i=0; i<groupState.length; i++ ) {
                    if( groupState[i] ) {
                        atLeastOneGroupSelected = true;
                        break;
                    }
                }
                panel.putClientProperty( FilterCondition.PROP_VALUE_VALID, new Boolean( atLeastOneGroupSelected ) );
            }
        });
        
        return panel;
    }
    
    public void getConstantFrom(JComponent cmp) {
        // Nothing to do. The array of booleans will not be cloned in
        // DefaultCheckListModel
    }

    protected String getDisplayName() {
      return NbBundle.getMessage(TaskGroupCondition.class, "IsOneOf"); //NOI18N
    }

    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        for( int i=0; i<groups.length; i++ ) {
            String groupName = groups[i].getName();
            groupState[i] = prefs.getBoolean( prefix+"_enabled_"+groupName, true ); //NOI18N
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        for( int i=0; i<groups.length; i++ ) {
            String groupName = groups[i].getName();
            prefs.putBoolean( prefix+"_enabled_"+groupName, groupState[i] ); //NOI18N
        }
    }
}
