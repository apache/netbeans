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
/*
 * Boolean3WayEditor.java
 *
 * Created on April 16, 2003, 7:05 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.util.*;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.*;

import java.beans.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;


/** A property editor for Boolean values which can also be null to
 *  indicate the editor represents multiple conflicting values.
 *
 * @author  Tim Boudreau
 */
final class Boolean3WayEditor implements ExPropertyEditor, InplaceEditor.Factory {
    Boolean v = null;

    /** Utility field holding list of PropertyChangeListeners. */
    private transient List<PropertyChangeListener> propertyChangeListenerList;
    private Boolean3Inplace renderer = null;

    public Boolean3WayEditor() {
    }

    public String getAsText() {
        if (v == null) {
            return NbBundle.getMessage(Boolean3WayEditor.class, "CTL_Different_Values");
        } else if (Boolean.TRUE.equals(v)) {
            return Boolean.TRUE.toString(); //XXX use hinting
        } else {
            return Boolean.FALSE.toString(); //XXX use hinting
        }
    }

    public java.awt.Component getCustomEditor() {
        return null;
    }

    public String getJavaInitializationString() {
        if (v == null) {
            return "null"; //NOI18N
        } else if (Boolean.TRUE.equals(v)) {
            return "Boolean.TRUE"; //NOI18N
        } else {
            return "Boolean.FALSE"; //NOI18N
        }
    }

    public String[] getTags() {
        return null;
    }

    public Object getValue() {
        return v;
    }

    public boolean isPaintable() {
        return true;
    }

    public void paintValue(Graphics gfx, Rectangle box) {
        if (renderer == null) {
            renderer = new Boolean3Inplace();
        }

        renderer.setSize(box.width, box.height);
        renderer.doLayout();

        Graphics g = gfx.create(box.x, box.y, box.width, box.height);
        renderer.setOpaque(false);
        renderer.paint(g);
        g.dispose();
    }

    public void setAsText(String text) {
        if (Boolean.TRUE.toString().compareToIgnoreCase(text) == 0) {
            setValue(Boolean.TRUE);
        } else {
            setValue(Boolean.FALSE);
        }
    }

    public void setValue(Object value) {
        if (v != value) {
            v = (Boolean) value;
            firePropertyChange();
        }
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     *
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListenerList == null) {
            propertyChangeListenerList = new java.util.ArrayList<PropertyChangeListener>();
        }

        propertyChangeListenerList.add(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeListenerList != null) {
            propertyChangeListenerList.remove(listener);
        }
    }

    /** Notifies all registered listeners about the event.
     *
     * @param event The event to be fired
     *
     */
    private void firePropertyChange() {
        List list;

        synchronized (this) {
            if (propertyChangeListenerList == null) {
                return;
            }

            list = (List) ((ArrayList) propertyChangeListenerList).clone();
        }

        PropertyChangeEvent event = new PropertyChangeEvent(this, null, null, null);

        for (int i = 0; i < list.size(); i++) {
            ((PropertyChangeListener) list.get(i)).propertyChange(event);
        }
    }

    /** Implementation of InplaceEditor.Factory to create an inplace editor on demand. */
    public InplaceEditor getInplaceEditor() {
        return new Boolean3Inplace();
    }

    class Boolean3Inplace extends JCheckBox implements InplaceEditor {

        private PropertyModel propertyModel = null;
        private final int NOT_SELECTED = 0;
        private final int SELECTED = 1;
        private final int DONT_CARE = 2;
        private final ButtonModel3Way model3way;

        Boolean3Inplace() {
            // Add a listener for when the mouse is pressed
            super.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    grabFocus();
                    model3way.nextState();
                }
            });
            // Reset the keyboard action map
            ActionMap map = new ActionMapUIResource();
            map.put("pressed", new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    grabFocus();
                    model3way.nextState();
                }
            });
            map.put("released", null);
            SwingUtilities.replaceUIActionMap(this, map);
            // set the model to the adapted model
            model3way = new ButtonModel3Way(getModel());
            setModel(model3way);
            setState(null == v ? DONT_CARE : (v.booleanValue() ? SELECTED : NOT_SELECTED));
        }

        /** No one may add mouse listeners, not even Swing! */
        @Override
        public void addMouseListener(MouseListener l) {
        }

        /**
         * Set the new state to either SELECTED, NOT_SELECTED or
         * DONT_CARE.  If state == null, it is treated as DONT_CARE.
         */
        public void setState(int state) {
            model3way.setState(state);
        }

        /** Return the current state, which is determined by the
         * selection status of the model. */
        public int getState() {
            return model3way.getState();
        }

        @Override
        public void setSelected(boolean b) {
            if (b) {
                setState(SELECTED);
            } else {
                setState(NOT_SELECTED);
            }
        }

        @Override
        public String getText() {
            return PropUtils.noCheckboxCaption ? "" : NbBundle.getMessage(Boolean3WayEditor.class, "CTL_Different_Values"); //NOI18N
        }

        public void clear() {
            propertyModel = null;
        }

        public void connect(PropertyEditor pe, PropertyEnv env) {
            //do nothing
        }

        public javax.swing.JComponent getComponent() {
            return this;
        }

        public javax.swing.KeyStroke[] getKeyStrokes() {
            return null;
        }

        public PropertyEditor getPropertyEditor() {
            return Boolean3WayEditor.this;
        }

        public Object getValue() {
            return getState() == DONT_CARE 
                    ? null 
                    : (getState() == SELECTED ? Boolean.TRUE : Boolean.FALSE);
        }

        public void reset() {
            setState(null == v 
                    ? DONT_CARE 
                    : (v.booleanValue() ? SELECTED : NOT_SELECTED));
        }

        public void setValue(Object o) {
            setState(null == o 
                    ? DONT_CARE 
                    : (((Boolean) o).booleanValue() ? SELECTED : NOT_SELECTED));
        }

        public boolean supportsTextEntry() {
            return false;
        }

        public void setPropertyModel(PropertyModel pm) {
            propertyModel = pm;
        }

        public PropertyModel getPropertyModel() {
            return propertyModel;
        }

        public boolean isKnownComponent(Component c) {
            return false;
        }

        private class ButtonModel3Way implements ButtonModel {

            private final ButtonModel other;

            private ButtonModel3Way(ButtonModel other) {
                this.other = other;
            }

            private void setState(int state) {
                if (state == NOT_SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(false);
                } else if (state == SELECTED) {
                    other.setArmed(false);
                    setPressed(false);
                    setSelected(true);
                } else {
                    // either "null" or DONT_CARE
                    other.setArmed(true);
                    setPressed(true);
                    setSelected(true);
                }
            }

            /**
             * The current state is embedded in the selection / armed
             * state of the model.
             *
             * We return the SELECTED state when the checkbox is selected
             * but not armed, DONT_CARE state when the checkbox is
             * selected and armed (grey) and NOT_SELECTED when the
             * checkbox is deselected.
             */
            private int getState() {
                if (isSelected() && !isArmed()) {
                    // normal black tick
                    return SELECTED;
                } else if (isSelected() && isArmed()) {
                    // don't care grey tick
                    return DONT_CARE;
                } else {
                    // normal deselected
                    return NOT_SELECTED;
                }
            }

            /** We rotate between NOT_SELECTED, SELECTED and DONT_CARE.*/
            private void nextState() {
                int current = getState();
                if (current == NOT_SELECTED) {
                    setState(SELECTED);
                } else if (current == SELECTED) {
                    setState(DONT_CARE);
                } else if (current == DONT_CARE) {
                    setState(NOT_SELECTED);
                }
            }

            /** Filter: No one may change the armed status except us. */
            public void setArmed(boolean b) {
            }

            /** We disable focusing on the component when it is not
             * enabled. */
            public void setEnabled(boolean b) {
                setFocusable(b);
                other.setEnabled(b);
            }

            /** All these methods simply delegate to the "other" model
             * that is being decorated. */
            public boolean isArmed() {
                return other.isArmed();
            }

            public boolean isSelected() {
                return other.isSelected();
            }

            public boolean isEnabled() {
                return other.isEnabled();
            }

            public boolean isPressed() {
                return other.isPressed();
            }

            public boolean isRollover() {
                return other.isRollover();
            }

            public void setSelected(boolean b) {
                other.setSelected(b);
            }

            public void setPressed(boolean b) {
                other.setPressed(b);
            }

            public void setRollover(boolean b) {
                other.setRollover(b);
            }

            public void setMnemonic(int key) {
                other.setMnemonic(key);
            }

            public int getMnemonic() {
                return other.getMnemonic();
            }

            public void setActionCommand(String s) {
                other.setActionCommand(s);
            }

            public String getActionCommand() {
                return other.getActionCommand();
            }

            public void setGroup(ButtonGroup group) {
                other.setGroup(group);
            }

            public void addActionListener(ActionListener l) {
                other.addActionListener(l);
            }

            public void removeActionListener(ActionListener l) {
                other.removeActionListener(l);
            }

            public void addItemListener(ItemListener l) {
                other.addItemListener(l);
            }

            public void removeItemListener(ItemListener l) {
                other.removeItemListener(l);
            }

            public void addChangeListener(ChangeListener l) {
                other.addChangeListener(l);
            }

            public void removeChangeListener(ChangeListener l) {
                other.removeChangeListener(l);
            }

            public Object[] getSelectedObjects() {
                return other.getSelectedObjects();
            }
        }
    }
}
