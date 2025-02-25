/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.tasklist.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JToggleButton;
import javax.swing.ToolTipManager;
import org.netbeans.modules.tasklist.impl.Accessor;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class ScopeButton extends JToggleButton implements PropertyChangeListener {
    
    private TaskManagerImpl tm;
    private TaskScanningScope scope;
    
    /** Creates a new instance of ScopeButton */
    public ScopeButton( TaskManagerImpl tm, TaskScanningScope scope ) {
        this.tm = tm;
        this.scope = scope;
        setText( null );
        setIcon( ImageUtilities.image2Icon( Accessor.getIcon( scope ) ) );
        ToolTipManager.sharedInstance().registerComponent(this);
        setFocusable( false );
    }

    @Override
    public String getToolTipText() {
        return null == scope ? null : Accessor.getDescription( scope );
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        tm.addPropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        tm.removePropertyChangeListener( TaskManagerImpl.PROP_SCOPE, this );
    }
    
    @Override
    protected void fireActionPerformed( ActionEvent event ) {
//        if( isSelected() ) {
//            return;
//        }
        super.fireActionPerformed( event );
        switchScope();
    }
    
    private void switchScope() {
        if( scope.equals( tm.getScope() ) ) {
            setSelected( true );
            return;
        }
        tm.observe( scope, tm.getFilter() );
        setSelected( true );
        Settings.getDefault().setActiveScanningScope( scope );
    }

    public void propertyChange( PropertyChangeEvent e ) {
        setSelected( scope.equals( tm.getScope() ) );
    }
}
