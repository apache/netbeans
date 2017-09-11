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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.core.multitabs.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Scrolls the content of a TabDisplayer.
 *
 * @author S. Aubrecht
 */
class ScrollAction extends AbstractAction implements ChangeListener, MouseWheelListener {

    private final JScrollPane scrollPane;
    private final boolean isHorizontal;
    private final boolean isScrollLeft;

    public ScrollAction( JScrollPane scrollPane, int orientation, boolean isScrollLeft ) {
        this.isScrollLeft = isScrollLeft;
        this.scrollPane = scrollPane;
        this.isHorizontal = orientation == JTabbedPane.TOP || orientation == JTabbedPane.BOTTOM;
        scrollPane.getViewport().addChangeListener( this );
        scrollPane.addComponentListener( new ComponentListener() {

            @Override
            public void componentResized( ComponentEvent e ) {
                updateEnabled();
            }

            @Override
            public void componentMoved( ComponentEvent e ) {
            }

            @Override
            public void componentShown( ComponentEvent e ) {
            }

            @Override
            public void componentHidden( ComponentEvent e ) {
            }
        });
        updateEnabled();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        int increment = getDefaultIncrement();

        scroll( increment );
    }

    private void updateEnabled() {
        boolean enable = true;
        Component content = scrollPane.getViewport().getView();
        if( null != content ) {
            Dimension size = content.getSize();
            Dimension viewSize = scrollPane.getViewport().getExtentSize();
            Point position = scrollPane.getViewport().getViewPosition();
            if( isHorizontal ) {
                if( isScrollLeft ) {
                    enable = position.x > 0;
                } else {
                    enable = position.x + viewSize.width < size.width;
                }
            } else {
                if( isScrollLeft ) {
                    enable = position.y > 0;
                } else {
                    enable = position.y + viewSize.height < size.height;
                }
            }
        }
        setEnabled( enable );
    }

    @Override
    public void stateChanged( ChangeEvent e ) {
        updateEnabled();
    }

    @Override
    public void mouseWheelMoved( MouseWheelEvent e ) {
        if( !isEnabled() )
            return;
        int rotation = e.getWheelRotation();
        if( (rotation < 0 && isScrollLeft)
                || (rotation > 0 && !isScrollLeft ) ) {
            int increment = getDefaultIncrement();
            increment *= Math.abs( rotation );
            scroll( increment );
            e.consume();
        }
    }

    private int getDefaultIncrement() {
        Component content = scrollPane.getViewport().getView();
        Point position = scrollPane.getViewport().getViewPosition();
        int increment = isHorizontal ? 30 : 8;
        Dimension viewSize = scrollPane.getViewport().getExtentSize();
        if( content instanceof Scrollable ) {
            increment = ((Scrollable)content).getScrollableUnitIncrement( new Rectangle( position, viewSize ),
                    isHorizontal ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL,
                    isScrollLeft ? -1 : 1 );
        }
        return increment;
    }

    private void scroll( int increment ) {
        Component content = scrollPane.getViewport().getView();
        Dimension size = content.getSize();
        Dimension viewSize = scrollPane.getViewport().getExtentSize();
        Point position = scrollPane.getViewport().getViewPosition();

        if( isHorizontal ) {
            position.x += increment;
            position.x = Math.max( position.x, 0 );
            position.x = Math.min( position.x, size.width-viewSize.width );
        } else {
            position.y += increment;
            position.y = Math.max( position.y, 0 );
            position.y = Math.min( position.y, size.height-viewSize.height );
        }

        scrollPane.getViewport().setViewPosition( position );
    }
}
