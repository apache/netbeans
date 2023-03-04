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

package org.openide.awt;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;

/**
 * JButton with a small arrow that displays popup menu when clicked.
 *
 * @author S. Aubrecht
 * @since 6.11
 */
class DropDownButton extends JButton {

    private boolean mouseInButton = false;
    private boolean mouseInArrowArea = false;
    private boolean popupClosingInProgress = false;

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

    /** Creates a new instance of MenuToggleButton */
    public DropDownButton( Icon icon, JPopupMenu popup ) {
        Parameters.notNull("icon", icon); //NOI18N

        putClientProperty( DropDownButtonFactory.PROP_DROP_DOWN_MENU, popup );

        setIcon( icon );
        setDisabledIcon(ImageUtilities.createDisabledIcon(icon));

        resetIcons();

        addPropertyChangeListener(DropDownButtonFactory.PROP_DROP_DOWN_MENU, (PropertyChangeEvent e) -> {
            resetIcons();
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
                if (popupClosingInProgress) {
                    return;
                }
                popupMenuOperation = false;
                JPopupMenu menu = getPopupMenu();
                if ( menu != null && getModel() instanceof Model ) {
                    Model model = (Model) getModel();
                    if ( !model._isPressed() ) {
                        if( isInArrowArea( e.getPoint() ) && menu.getComponentCount() > 0 &&
                            model.isEnabled() )
                        {
                            model._press();
                            menu.addPopupMenuListener( getMenuListener() );
                            menu.show( DropDownButton.this, 0, getHeight() );
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
                    if( getModel() instanceof Model ) {
                        ((Model)getModel())._release();
                    }
                    JPopupMenu menu = getPopupMenu();
                    if( null != menu ) {
                        menu.removePopupMenuListener( this );
                    }
                    /* If the popup was closed by a mouse click inside the button area, the button
                    may also receive a mousePressed event, although this seems not to be guaranteed.
                    Ignore any such button press while the popup is closing, to avoid interpreting
                    the press as a click to open the menu again. */
                    popupClosingInProgress = true;
                    SwingUtilities.invokeLater(() -> {
                        popupClosingInProgress = false;
                    });
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
            icon = new IconWithArrow( orig, !mouseInArrowArea, false );
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
            icon = new IconWithArrow( orig, !mouseInArrowArea, false );
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
        /* If no one is listening for button presses, treat the entire button as a dropdown menu
        trigger. This also means we do not paint the IconWithArrow.paintRollOver separator. */
        if (getActionListeners().length == 0) {
            return true;
        }
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
        updateRollover( _getRolloverIcon(), _getRolloverSelectedIcon() );
    }

    private Icon updateIcons( Icon orig, String iconType ) {
        Icon arrow = null;
        if( null == orig ) {
            regIcons.remove( iconType );
            arrowIcons.remove( iconType );
        } else {
            regIcons.put( iconType, orig );
            arrow = new IconWithArrow( orig, false,
                iconType.equals(ICON_DISABLED) || iconType.equals(ICON_DISABLED_SELECTED));
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
        Icon arrow = updateIcons( icon, ICON_DISABLED );
        super.setDisabledIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setDisabledSelectedIcon(Icon icon) {
        Icon arrow = updateIcons( icon, ICON_DISABLED_SELECTED );
        super.setDisabledSelectedIcon( hasPopupMenu() ? arrow : icon );
    }

    @Override
    public void setText( String text ) {
        //does nothing
        Logger.getLogger(DropDownButton.class.getName()).log(Level.FINER, "DropDownButton cannot display text."); //NOI18N
    }

    @Override
    public String getText() {
        return null;
    }

    private class Model extends DefaultButtonModel {
        private boolean _pressed = false;

        @Override
        public void setPressed(boolean b) {
            if( _pressed || b && mouseInArrowArea)
                return;
            super.setPressed( b );
        }

        public void _press() {
            if((_pressed && isPressed()) || !isEnabled()) {
                return;
            }

            stateMask |= PRESSED + ARMED;

            fireStateChanged();
            _pressed = true;
        }

        public void _release() {
            _pressed = false;
            setArmed( false );
            setPressed( false );
            setRollover( false );
            setSelected( false );
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
