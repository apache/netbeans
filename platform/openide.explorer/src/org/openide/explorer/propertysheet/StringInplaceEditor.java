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

package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * A JTextField implementation of the InplaceEditor interface.
 * @author Tim Boudreau
 */
class StringInplaceEditor extends JTextField implements InplaceEditor, IncrementPropertyValueSupport {

    protected PropertyEditor editor;
    protected PropertyEnv env;
    private boolean added;
    private String valFromEditor;
    private String valFromTextField;
    private PropertyModel pm;
    
    private KeyStroke[] strokes = new KeyStroke[] {
            KeyStroke.getKeyStroke(
                KeyEvent.VK_HOME, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK
            ),
            KeyStroke.getKeyStroke(
                KeyEvent.VK_END, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_DOWN_MASK
            ), KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false)
        };

    @Override
    public void clear() {
        editor = null;
        setEditable(true);
        setEnabled(true);
        setText("");
        pm = null;
        env = null;
        valFromEditor = null;
        valFromTextField = null;
    }

    @Override
    public void connect(PropertyEditor p, PropertyEnv env) {
        setActionCommand(COMMAND_SUCCESS);
        this.env = env;
        
        if(PropUtils.supportsValueIncrement( env ) ) {
            PropUtils.wrapUpDownArrowActions( this, this );
        }

        if (editor == p) {
            return;
        }

        editor = p;

        boolean editable = PropUtils.checkEnabled(this, p, env);
        setEnabled(editable);

        //Undocumented, but in NB 3.5 and earlier, getAsText() returning null for
        //paintable editors was yet another way to disable a property editor
        if ((p.getTags() == null) && (p.getAsText() == null) && p.isPaintable()) {
            editable = false;
        }

        setEditable(editable);
        reset();
        added = false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        added = true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Object getValue() {
        if ((valFromTextField != null) && valFromTextField.equals(getText())) {
            //#47430 - JTextField will strip \n's from edited text.  If no
            //change to the text field value, return what we originally got
            return null == valFromEditor ? "" : valFromEditor;
        } else {
            return getText();
        }
    }

    @Override
    public void reset() {
        String txt;
        txt = editor.getAsText();

        //don't want an editor with the text "different values" in it //NOI18N
        if (editor instanceof PropUtils.DifferentValuesEditor) {
            txt = ""; //NOI18N
        }

        valFromEditor = txt;
        //issue 26367, form editor needs ability to set a custom value
        //when editing is initiated (event handler combos, part of them
        //cleaning up their EnhancedPropertyEditors).          
        if ((getClass() == StringInplaceEditor.class) && (env != null) && (env.getFeatureDescriptor() != null)) {
            String initialEditValue = (String) env.getFeatureDescriptor().getValue("initialEditValue"); //NOI18N

            if (initialEditValue != null) {
                txt = initialEditValue;
                valFromEditor = txt;
            }
        }

        if (txt == null) {
            txt = "";
        }

        setText(txt);
        valFromTextField = getText();
        setSelectionStart(0);
        setSelectionEnd(txt.length());
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
        return strokes;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    private void handleInitialInputEvent(InputEvent e) {
        //issue 35296, select all the text
        String txt = getText();

        if (txt.length() > 0) {
            setSelectionStart(0);
            setSelectionEnd(getText().length());
        }
    }

    @Override
    public void setValue(Object o) {
        if ((null != o) && (null != editor) && editor.supportsCustomEditor()) {
            editor.setValue(o);
            String val = editor.getAsText();
            if( null == val )
                val = "";
            setText(val);
        } else {
            setText((o != null) ? o.toString() : ""); //NOI18N
        }
    }

    @Override
    public boolean supportsTextEntry() {
        return true;
    }

    @Override
    public PropertyModel getPropertyModel() {
        return pm;
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
        this.pm = pm;
    }

    @Override
    public boolean isKnownComponent(Component c) {
        return false;
    }

    @Override
    public Dimension getPreferredSize() {
        Graphics g = PropUtils.getScratchGraphics(this);
        String s = getText();

        if (s.length() > 1000) {
            //IZ 44152, debugger can return 512K+ long strings
            return new Dimension(4196, g.getFontMetrics(getFont()).getHeight());
        }

        FontMetrics fm = g.getFontMetrics(getFont());
        Dimension result = new Dimension(fm.stringWidth(s), fm.getHeight());
        result.width = Math.max(result.width, PropUtils.getMinimumPropPanelWidth());
        result.height = Math.max(result.height, PropUtils.getMinimumPropPanelHeight());

        if (getBorder() != null) {
            Insets i = getBorder().getBorderInsets(this);
            result.width += (i.right + i.left);
            result.height += (i.top + i.bottom);
        }

        return result;
    }

    @Override
    public void processMouseEvent(MouseEvent me) {
        super.processMouseEvent(me);

        if (added) {
            handleInitialInputEvent(me);
        }

        added = false;
    }

    @Override
    protected void processFocusEvent(FocusEvent fe) {
        super.processFocusEvent(fe);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        //For property panel usage, allow the editor to paint
        if ((editor != null) && !hasFocus() && editor.isPaintable()) {
            Insets ins = getInsets();
            Color c = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            } finally {
                g.setColor(c);
            }

            ins.left += PropUtils.getTextMargin();
            editor.paintValue(
                g,
                new Rectangle(
                    ins.left, ins.top, getWidth() - (ins.right + ins.left), getHeight() - (ins.top + ins.bottom)
                )
            );
        } else {
            super.paintComponent(g);
        }
    }

    @Override
    public boolean incrementValue() {
        return setNextValue( true );
    }

    @Override
    public boolean decrementValue() {
        return setNextValue( false );
    }

    private boolean setNextValue( boolean increment ) {
        if( !PropUtils.supportsValueIncrement( env ) )
            return false;

        Object nextValue = PropUtils.getNextValue( env, increment );
        if( null == nextValue )
            return true;

        setValue( nextValue );

        return PropUtils.updateProp( this );
    }

    @Override
    public boolean isIncrementEnabled() {
        return true;
    }
}
