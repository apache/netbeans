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
package org.netbeans.core.multitabs.impl;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 *
 * @author stan
 */
final class ControlsToolbar extends JToolBar {

    public ControlsToolbar() {
        super( JToolBar.HORIZONTAL );
    }
    
    @Override
    protected void addImpl( Component comp, Object constraints, int index ) {
        super.addImpl( comp, constraints, index ); 
        if( comp instanceof JButton ) {
            JButton btn = (JButton) comp;
            btn.setContentAreaFilled( false );
            btn.setOpaque( false );
            btn.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
            btn.setFocusable( false );
            btn.setBorderPainted( false );
            btn.setRolloverEnabled( UIManager.getBoolean("nb.multitabs.button.rollover") );
        }
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        configure();
    }
    
    private void configure() {
        setFloatable( false );
        setFocusable( false );
        setOpaque( false );
        setBorder( BorderFactory.createEmptyBorder() );
        setBorderPainted( false );
        if( TabTableUI.IS_AQUA ) {
            Color backColor = UIManager.getColor( "NbSplitPane.background" ); //NOI18N
            if( null != backColor ) {
                setBackground( backColor );
                setOpaque( true );
            }
        }
    }
}
