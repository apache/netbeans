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
package org.netbeans.modules.web.browser.ui.picker;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.Border;

/**
 *
 * @author S. Aubrecht
 */
class RendererImpl extends DefaultListCellRenderer {

    private static final Border emptyBorder = BorderFactory.createEmptyBorder( 5, 5, 5, 5 );

    public RendererImpl() {
        setIconTextGap( 5 );
    }
    
    @Override
    public Component getListCellRendererComponent( JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
        setBorder( null );
        if( list instanceof SelectionListImpl ) {
            isSelected |= index == ((SelectionListImpl)list).getMouseOverRow();
        }
        super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus ); //To change body of generated methods, choose Tools | Templates.
        if( value instanceof ListItem ) {
            ListItem item = ( ListItem ) value;
            setText( item.getText() );
            setIcon( item.getIcon() );
        }
        Border b = getBorder();
        if( null == b ) {
            setBorder( b );
        } else {
            setBorder( BorderFactory.createCompoundBorder( b, emptyBorder ) );
        }
        return this;
    }

}
