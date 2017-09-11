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
/*
 * InplaceEditorFactory.java
 *
 * Created on January 4, 2003, 4:52 PM
 */
package org.openide.explorer.propertysheet;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTextField;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.nodes.Node.Property;

import java.beans.PropertyEditor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.openide.util.WeakListeners;

/** Factory providing inplace editor implementations.  Provides appropriate
 *  InplaceEditor implementations, depending on the type of the property, the
 *  results of PropertyEditor.getTags(), or any hinting provided by the property
 *  editor or PropertyEnv to use a custom inplace editor implementation.
 *  Configures the editor returned and attaches it to the property in question.
  * @author  Tim Boudreau
  */
final class InplaceEditorFactory implements PropertyChangeListener {
    private InplaceEditor checkbox = null;
    private InplaceEditor text = null;
    private InplaceEditor combo = null;
    private InplaceEditor radio = null;
    private ReusablePropertyEnv reusableEnv;
    private boolean tableUI;
    int radioButtonMax = -1;
    private boolean useLabels = false;
    private boolean useRadioBoolean = PropUtils.forceRadioButtons;

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final boolean isMetal = "Metal".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    InplaceEditorFactory(boolean tableUI, ReusablePropertyEnv env) {
        this.tableUI = tableUI;
        this.reusableEnv = env;
        //reset editors when windows theme is changing (classic <-> xp)
        PropertyChangeListener weakListener = WeakListeners.propertyChange(this, Toolkit.getDefaultToolkit());
        Toolkit.getDefaultToolkit().addPropertyChangeListener( "win.xpstyle.themeActive", weakListener ); //NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        checkbox = null;
        text = null;
        combo = null;
        radio = null;
    }

    /** Set a threshold number of tags below which a radio button, not a
     * combo box editor should be used */
    void setRadioButtonMax(int i) {
        radioButtonMax = i;
    }

    /** Set whether or not radio and checkbox editors should show the property
     * name */
    void setUseLabels(boolean val) {
        useLabels = val;
    }

    void setUseRadioBoolean(boolean val) {
        useRadioBoolean = val;
    }

    /**Lazily create (or create a new instance of) the radio button editor */
    private InplaceEditor getRadioEditor(boolean newInstance) {
        RadioInplaceEditor result;

        if (newInstance) {
            result = new RadioInplaceEditor(tableUI);
        } else {
            if (radio == null) {
                radio = new RadioInplaceEditor(tableUI);

                //Mainly for debugging
                ((JComponent) radio).setName(
                    "RadioEditor for " + getClass().getName() + "@" + System.identityHashCode(this)
                ); //NOI18N
            }

            result = (RadioInplaceEditor) radio;
        }

        result.setUseTitle(useLabels);

        return result;
    }

    /**Lazily create (or create a new instance of) the combo box editor */
    private InplaceEditor getComboBoxEditor(boolean newInstance) {
        if (newInstance || isAqua || isMetal) { //#220163
            return new ComboInplaceEditor(tableUI);
        }

        if (combo == null) {
            combo = new ComboInplaceEditor(tableUI);

            //Mainly for debugging
            ((JComponent) combo).setName(
                "ComboInplaceEditor for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return combo;
    }

    /**Lazily create (or create a new instance of) the string editor */
    private InplaceEditor getStringEditor(boolean newInstance) {
        if (newInstance) {
            return new StringInplaceEditor();
        }

        if (text == null) {
            text = new StringInplaceEditor();

            //Mainly for debugging
            ((JComponent) text).setName(
                "StringEditor for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return text;
    }

    /**Lazily create (or create a new instance of) the checkbox editor */
    private InplaceEditor getCheckboxEditor(boolean newInstance) {
        CheckboxInplaceEditor result;

        if (newInstance) {
            result = new CheckboxInplaceEditor();
        } else {
            if (checkbox == null) {
                checkbox = new CheckboxInplaceEditor();

                //Mainly for debugging
                ((JComponent) checkbox).setName(
                    "CheckboxEditor for " + getClass().getName() + "@" + System.identityHashCode(this)
                ); //NOI18N
            }

            result = (CheckboxInplaceEditor) checkbox;
        }

        result.setUseTitle(useLabels);

        return (InplaceEditor) result;
    }

    /** Factory method that returns an appropriate inplace
     *  editor for an object.  Special handling is provided for
     *  instances of Node.Property which can provide hints or
     *  even their own legacy inplace editor implementation.
     *  <P>The returned instance will be connected to the
     *  object (the component provided by getComponent() will
     *  render the property object correctly with no additional
     *  intervention needed.  If <code>newInstance</code> is
     *  true, will create a new instance of the inplace editor
     *  component (for use with PropertyPanel and other cases
     *  where multiple inplace esditors can be displayed at the
     *  same time); otherwise a shared instance will be configured
     *  and returned.<P> Note that for the case of unknown object
     *  types (non Node.Property objects), the returned InplaceEditor
     *  will have no way of knowing how to update the object with
     *  a new value, and client code must listen for actions on
     *  the InplaceEditor and do this manually - the update method
     *  of the InplaceEditor will do nothing.  */
    public InplaceEditor getInplaceEditor(Property p, boolean newInstance) {
        PropertyEnv env = new PropertyEnv();
        env.setBeans(reusableEnv.getBeans());

        return getInplaceEditor(p, env, newInstance);
    }

    InplaceEditor getInplaceEditor(Property p, PropertyEnv env, boolean newInstance) {
        PropertyEditor ped = PropUtils.getPropertyEditor(p);
        InplaceEditor result = (InplaceEditor) p.getValue("inplaceEditor"); //NOI18N
        env.setFeatureDescriptor(p);
        env.setEditable(p.canWrite());

        if (ped instanceof ExPropertyEditor) {
            ExPropertyEditor epe = (ExPropertyEditor) ped;

            //configure the editor/propertyenv
            epe.attachEnv(env);

            if (result == null) {
                result = env.getInplaceEditor();
            }
        } else if (ped instanceof EnhancedPropertyEditor) {
            //handle legacy inplace custom editors
            EnhancedPropertyEditor enh = (EnhancedPropertyEditor) ped;

            if (enh.hasInPlaceCustomEditor()) {
                //Use our wrapper component to handle this
                result = new WrapperInplaceEditor(enh);
            }
        }

        //Okay, the result is null, provide one of the standard inplace editors
        if (result == null) {
            Class c = p.getValueType();

            String[] tags;
            if ((c == Boolean.class) || (c == Boolean.TYPE)) {
                if (ped instanceof PropUtils.NoPropertyEditorEditor) {
                    //platform case
                    result = getStringEditor(newInstance);
                } else {
                    boolean useRadioButtons = useRadioBoolean || (p.getValue("stringValues") != null); //NOI18N
                    result = useRadioButtons ? getRadioEditor(newInstance) : getCheckboxEditor(newInstance);
                }
            } else if ((tags = ped.getTags()) != null) {
                if (tags.length <= radioButtonMax) {
                    result = getRadioEditor(newInstance);
                } else {
                    result = getComboBoxEditor(newInstance);
                }
            } else {
                result = getStringEditor(newInstance);
            }
        }

        if (!tableUI && Boolean.FALSE.equals(p.getValue("canEditAsText"))) { //NOI18N
            result.getComponent().setEnabled(false);
        }

        result.clear(); //XXX shouldn't need to do this!
        result.setPropertyModel(new NodePropertyModel(p, env.getBeans()));
        result.connect(ped, env);

        //XXX?
        if (tableUI) {
            if( result instanceof JTextField )
                result.getComponent().setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
            else
                result.getComponent().setBorder(BorderFactory.createEmptyBorder());
        }

        return result;
    }
}
