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

package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.*;
import org.openide.util.NbBundle;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.*;

import java.beans.*;

import java.text.MessageFormat;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


/** A basic property-editor-aware JCheckbox that fires changes
 *  appropriately.  Note that the property sheet
 *  implementation never instantiates an inplace editor for
 *  booleans, but toggles their state on the editing trigger.
 *  For the property renderer/property panel use case, this
 *  editor is used, since a focusable component is needed.
 *
 *  @author Tim Boudreau
 */
class CheckboxInplaceEditor extends JCheckBox implements InplaceEditor {
    protected PropertyEditor editor = null;
    protected PropertyEnv env = null;
    private boolean useTitle = false;
    private String text = null;
    private PropertyModel pm = null;

    public CheckboxInplaceEditor() {
        setActionCommand(COMMAND_SUCCESS);
    }

    public void setUseTitle(boolean val) {
        if (useTitle != val) {
            useTitle = val;
            text = null;

            if (env != null) {
                setText(env.getFeatureDescriptor().getDisplayName());
            }
        }
    }

    public void setSelected(boolean val) {
        boolean fire = val == isSelected();
        String s = getText();
        super.setSelected(val);

        if (fire) {
            firePropertyChange("text", s, getText()); //NOI18N
        }
    }

    public void connect(PropertyEditor p, PropertyEnv env) {
        text = null;

        if (editor instanceof PropUtils.NoPropertyEditorEditor) {
            //only happens in platform use case
            setSelected(false);

            return;
        }

        if (editor == p) {
            return;
        }

        editor = p;
        setSelected(Boolean.TRUE.equals(p.getValue()));
        reset();
        this.env = env;

        if (env != null) {
            if (useTitle) {
                setText(env.getFeatureDescriptor().getDisplayName());
            }
        }
    }

    public void clear() {
        editor = null;
        pm = null;
        env = null;

        //setText("");
        text = null;
        getModel().setRollover(false);
    }

    public JComponent getComponent() {
        return this;
    }

    public Object getValue() {
        return isSelected() ? Boolean.TRUE : Boolean.FALSE;
    }

    public void reset() {
        if (editor instanceof PropUtils.NoPropertyEditorEditor) {
            //only happens in platform use case
            return;
        }

        if (editor != null) {
            Object value = editor.getValue();

            if (value == null) {
                getModel().setArmed(true);
            } else if (value instanceof Boolean) {
                setSelected((Boolean) value);
            }
        }
    }

    public String getText() {
        //OptimizeIt shows 1% of drawing time can be spent in re-fetching
        //text, so cache it as a microoptimization
        if (text == null) {
            if (useTitle || (editor == null) || (editor.getTags() == null)) {
                //Initialization call in superclass constructor or we do not
                //have the standard NetBeans boolean editor with tags
                text = super.getText();
            } else if (PropUtils.noCheckboxCaption) {
                text = ""; //NOI18N
            } else {
                String prepend = NbBundle.getMessage(CheckboxInplaceEditor.class, "BOOLEAN_PREPEND"); //NOI18N
                String append = NbBundle.getMessage(CheckboxInplaceEditor.class, "BOOLEAN_APPEND"); //NOI18N
                java.text.MessageFormat mf = new MessageFormat(
                        NbBundle.getMessage(CheckboxInplaceEditor.class, "FMT_BOOLEAN")
                    ); //NOI18N

                String s;
                Boolean sel = isSelected() ? Boolean.TRUE : Boolean.FALSE;

                if (sel.equals(editor.getValue())) {
                    s = editor.getAsText();
                } else {
                    String[] tags = editor.getTags();

                    if (tags[0].equals(editor.getAsText())) {
                        s = tags[1];
                    } else {
                        s = tags[0];
                    }
                }

                text = mf.format(new String[] { prepend, s, append });
            }
        }

        return text;
    }

    public KeyStroke[] getKeyStrokes() {
        return null;
    }

    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    public void handleInitialInputEvent(InputEvent e) {
        boolean toggle = false;

        if (e instanceof MouseEvent) {
            toggle = true;
        } else if (e instanceof KeyEvent) {
            if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_SPACE) {
                toggle = true;
            }
        }

        if (toggle) {
            setSelected(!isSelected());
            fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, COMMAND_SUCCESS));
            getModel().setPressed(false); //Mainly an issue for unit tests, but can't hurt
        }
    }

    public void setValue(Object o) {
        if (o == null) {
            //platform use case
            setSelected(false);
        }

        if (Boolean.TRUE.equals(o)) {
            setSelected(true);
        } else if (Boolean.FALSE.equals(o)) {
            setSelected(false);
        }
    }

    public boolean supportsTextEntry() {
        return false;
    }

    public PropertyModel getPropertyModel() {
        return pm;
    }

    public void setPropertyModel(PropertyModel pm) {
        this.pm = pm;
    }

    public boolean isKnownComponent(java.awt.Component c) {
        return false;
    }

    /** Overridden to be able to calculate the preferred size without having
     * to be added to the AWT hierarchy */
    public Dimension getPreferredSize() {
        if (isShowing()) {
            return super.getPreferredSize();
        }

        Dimension result = PropUtils.getMinimumPanelSize();
        Graphics g = PropUtils.getScratchGraphics(this);
        g.setFont(getFont());

        String txt = getText();
        Icon i = getIcon();
        FontMetrics fm = g.getFontMetrics(getFont());
        int w = fm.stringWidth(txt);
        int h = fm.getHeight();

        if (i != null) {
            w += (i.getIconWidth() + getIconTextGap());
            h = Math.max(h, result.height);
        }

        Insets ins = getInsets();

        if (ins != null) {
            w += (ins.left + ins.right);
            h += (ins.top + ins.bottom);
        }

        w += 22; //A small fudge factor to avoid truncated text

        result.width = Math.max(result.width, w);
        result.height = Math.max(result.height, h);

        return result;
    }
}
