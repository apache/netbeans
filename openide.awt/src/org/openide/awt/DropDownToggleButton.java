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

package org.openide.awt;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 * JToggleButton with a small arrow that displays popup menu when clicked.
 *
 * @author S. Aubrecht
 * @since 6.11
 */
class DropDownToggleButton extends JToggleButton {
    
    private boolean mouseInButton = false;
    private boolean mouseInArrowArea = false;
    
    private Map<String,Icon> regIcons = new HashMap<String,Icon>( 5 );
    private Map<String,Icon> arrowIcons = new HashMap<String,Icon>( 5 );
    
    private static final String ICON_NORMAL = "normal"; //NOI18N
    private static final String ICON_PRESSED = "pressed"; //NOI18N
    private static final String ICON_ROLLOVER = "rollover"; //NOI18N
    private static final String ICON_ROLLOVER_SELECTED = "rolloverSelected"; //NOI18N
    private static final String ICON_SELECTED = "selected"; //NOI18N
    private static final String ICON_DISABLED = "disabled"; //NOI18N
    private static final String ICON_DISABLED_SELECTED = "disabledSelected"; //NOI18N
    
    private static final String ICON_ROLLOVER_LINE = "rolloverLine"; //NOI18N
    private static final String ICON_ROLLOVER_SELECTED_LINE = "rolloverSelectedLine"; //NOI18N
    
    private PopupMenuListener menuListener;
    
    /** Creates a new instance of DropDownToggleButton */
    public DropDownToggleButton( Icon icon, JPopupMenu popup ) {
        Parameters.notNull("icon", icon); //NOI18N
        
        putClientProperty( DropDownButtonFactory.PROP_DROP_DOWN_MENU, popup );
        
        setIcon( icon );
        
        resetIcons();
        
        addPropertyChangeListener(  DropDownButtonFactory.PROP_DROP_DOWN_MENU,new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent e ) {
                resetIcons();
            }
        });
        
        addMouseMotionListener( new MouseMotionAdapter() {
            @Override
            public void mouseMoved( MouseEvent e ) {
                if( null != getPopupMenu() ) {
                    mouseInArrowArea = isInArrowArea( e.getPoint() );
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }
        });
        
        addMouseListener( new MouseAdapter() {
            private boolean popupMenuOperation = false;
            
            @Override
            public void mousePressed( MouseEvent e ) {
                popupMenuOperation = false;
                JPopupMenu menu = getPopupMenu();
                if ( menu != null && getModel() instanceof Model ) {
                    Model model = (Model) getModel();
                    if ( !model._isPressed() ) {
                        if( isInArrowArea( e.getPoint() ) && menu.getComponentCount() > 0 ) {
                            model._press();
                            menu.addPopupMenuListener( getMenuListener() );
                            menu.show( DropDownToggleButton.this, 0, getHeight() );
                            popupMenuOperation = true;
                        }
                    } else {
                        model._release();
                        menu.removePopupMenuListener( getMenuListener() );
                        popupMenuOperation = true;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // If we done something with the popup menu, we should consume
                // the event, otherwise the button's action will be triggered.
                if (popupMenuOperation) {
                    popupMenuOperation = false;
                    e.consume();
                }
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                mouseInButton = true;
                if( hasPopupMenu() ) {
                    mouseInArrowArea = isInArrowArea( e.getPoint() );
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                mouseInButton = false;
                mouseInArrowArea = false;
                if( hasPopupMenu() ) {
                    updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
                }
            }
        });
        
        setModel( new Model() );
    }
    
    private PopupMenuListener getMenuListener() {
        if( null == menuListener ) {
            menuListener = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // If inside the button let the button's mouse listener
                    // deal with the state. The popup menu will be hidden and
                    // we should not show it again.
                    if ( !mouseInButton ) {
                        if( getModel() instanceof Model ) {
                            ((Model)getModel())._release();
                        }
                        JPopupMenu menu = getPopupMenu();
                        if( null != menu ) {
                            menu.removePopupMenuListener( this );
                        }
                    }
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            };
        }
        return menuListener;
    }
        
    private void updateRollover( Icon rollover, Icon rolloverSelected ) {
        super.setRolloverIcon( rollover );
        super.setRolloverSelectedIcon( rolloverSelected );
    }
    
    private void resetIcons() {
        Icon icon = regIcons.get( ICON_NORMAL );
        if( null != icon )
            setIcon( icon );
        
        icon = regIcons.get( ICON_PRESSED );
        if( null != icon )
            setPressedIcon( icon );
        
        icon = regIcons.get( ICON_ROLLOVER );
        if( null != icon )
            setRolloverIcon( icon );
        
        icon = regIcons.get( ICON_ROLLOVER_SELECTED );
        if( null != icon )
            setRolloverSelectedIcon( icon );
        
        icon = regIcons.get( ICON_SELECTED );
        if( null != icon )
            setSelectedIcon( icon );
        
        icon = regIcons.get( ICON_DISABLED );
        if( null != icon )
            setDisabledIcon( icon );
        
        icon = regIcons.get( ICON_DISABLED_SELECTED );
        if( null != icon )
            setDisabledSelectedIcon( icon );
    }
    
    private Icon _getRolloverIcon() {
        Icon icon = null;
        icon = arrowIcons.get( mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE );
        if( null == icon ) {
            Icon orig = regIcons.get( ICON_ROLLOVER );
            if( null == orig )
                orig = regIcons.get( ICON_NORMAL );
            icon = new IconWithArrow( orig, !mouseInArrowArea );
            arrowIcons.put( mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE, icon );
        }
        return icon;
    }
    
    private Icon _getRolloverSelectedIcon() {
        Icon icon = null;
        icon = arrowIcons.get( mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE );
        if( null == icon ) {
            Icon orig = regIcons.get( ICON_ROLLOVER_SELECTED );
            if( null == orig )
                orig = regIcons.get( ICON_ROLLOVER );
            if( null == orig )
                orig = regIcons.get( ICON_NORMAL );
            icon = new IconWithArrow( orig, !mouseInArrowArea );
            arrowIcons.put( mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE, icon );
        }
        return icon;
    }
    
    JPopupMenu getPopupMenu() {
        Object menu = getClientProperty( DropDownButtonFactory.PROP_DROP_DOWN_MENU );
        if( menu instanceof JPopupMenu ) {
            return (JPopupMenu)menu;
        }
        return null;
    }
    
    boolean hasPopupMenu() {
        return null != getPopupMenu();
    }
    
    private boolean isInArrowArea( Point p ) {
        return p.getLocation().x >= getWidth() - IconWithArrow.getArrowAreaWidth() - getInsets().right;
    }

    @Override
    public void setIcon(Icon icon) {
        assert null != icon;
        Icon arrow = updateIcons( icon, ICON_NORMAL );
        arrowIcons.remove( ICON_ROLLOVER_LINE );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        arrowIcons.remove( ICON_ROLLOVER );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED );
        super.setIcon( hasPopupMenu() ? arrow : icon );
    }

    private Icon updateIcons( Icon orig, String iconType ) {
        Icon arrow = null;
        if( null == orig ) {
            regIcons.remove( iconType );
            arrowIcons.remove( iconType );
        } else {
            regIcons.put( iconType, orig );
            arrow = new ImageIcon(ImageUtilities.icon2Image(new IconWithArrow( orig, false )));
            arrowIcons.put( iconType, arrow );
        }
        return arrow;
    }
    
    @Override
    public void setPressedIcon(Icon icon) {
        Icon arrow = updateIcons( icon, ICON_PRESSED );
        super.setPressedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setSelectedIcon(Icon icon) {
        Icon arrow = updateIcons( icon, ICON_SELECTED );
        super.setSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setRolloverIcon(Icon icon) {
        Icon arrow = updateIcons( icon, ICON_ROLLOVER );
        arrowIcons.remove( ICON_ROLLOVER_LINE );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        super.setRolloverIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setRolloverSelectedIcon(Icon icon) {
        Icon arrow = updateIcons( icon, ICON_ROLLOVER_SELECTED );
        arrowIcons.remove( ICON_ROLLOVER_SELECTED_LINE );
        super.setRolloverSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setDisabledIcon(Icon icon) {
        //TODO use 'disabled' arrow icon
        Icon arrow = updateIcons( icon, ICON_DISABLED );
        super.setDisabledIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setDisabledSelectedIcon(Icon icon) {
        //TODO use 'disabled' arrow icon
        Icon arrow = updateIcons( icon, ICON_DISABLED_SELECTED );
        super.setDisabledSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setText( String text ) {
        //does nothing
        Logger.getLogger(DropDownToggleButton.class.getName()).log(Level.FINER, "DropDownToggleButton cannot display text."); //NOI18N
    }

    @Override
    public String getText() {
        return null;
    }
    
    private class Model extends JToggleButton.ToggleButtonModel {
        private boolean _pressed = false;
        
        @Override
        public void setPressed(boolean b) {
            if( mouseInArrowArea || _pressed )
                return;
            super.setPressed( b );
        }
    
        public void _press() {
            if((isPressed()) || !isEnabled()) {
                return;
            }

            stateMask |= PRESSED + ARMED;

            fireStateChanged();
            _pressed = true;
        }
        
        public void _release() {
            _pressed = false;
            mouseInArrowArea = false;
            setArmed( false );
            setPressed( false );
            setRollover( false );
        }

        public boolean _isPressed() {
            return _pressed;
        }
        
        @Override
        protected void fireStateChanged() {
            if( _pressed )
                return;
            super.fireStateChanged();
        }

        @Override
        public void setArmed(boolean b) {
            if( _pressed )
                return;
            super.setArmed(b);
        }

        @Override
        public void setEnabled(boolean b) {
            if( _pressed )
                return;
            super.setEnabled(b);
        }

        @Override
        public void setSelected(boolean b) {
            if( _pressed )
                return;
            super.setSelected(b);
        }

        @Override
        public void setRollover(boolean b) {
            if( _pressed )
                return;
            super.setRollover(b);
        }
    }
}
