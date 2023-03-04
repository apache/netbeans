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
package org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.openide.awt.CloseButtonFactory;
import org.openide.util.NbBundle;

/**
 * Component to paint tab header.
 *
 * @author S. Aubrecht
 */
public class CloseableTabComponent extends JPanel {

    private final NBTabbedPane parent;
    private final JLabel lblTitle = new JLabel();
    private final JButton closeButton;

    public CloseableTabComponent(Icon icon, String title, boolean closeable, String tooltip, final NBTabbedPane parent, MouseListener controller) {
        super(new BorderLayout( 2, 0 ) );

        lblTitle.setText( title );
        if( parent.getType() == TabbedType.EDITOR )
            lblTitle.setIcon( icon );
        add(lblTitle, BorderLayout.CENTER);
        lblTitle.setToolTipText( tooltip );

        addMouseListener(controller);
        lblTitle.addMouseListener( controller);
        if( closeable ) {
            closeButton = CloseButtonFactory.createBigCloseButton();
            add(closeButton, BorderLayout.EAST);
            closeButton.addMouseListener( controller );
            if( parent.getType() == TabbedType.EDITOR )
                closeButton.setToolTipText( NbBundle.getMessage(CloseableTabComponent.class, "BtnClose_Tooltip") );
        } else {
            closeButton = null;
        }
        this.parent = parent;
        setOpaque(false);
    }

    public void setIcon(Icon icon) {
        if( parent.getType() == TabbedType.EDITOR )
            lblTitle.setIcon( icon );
    }

    public void setTitle(String title) {
        lblTitle.setText( title );
    }

    public void setTooltip(String tooltip) {
        lblTitle.setToolTipText( tooltip );
    }

    boolean isInCloseButton( MouseEvent e ) {
        return null != closeButton && e.getComponent() == closeButton;
    }
}
