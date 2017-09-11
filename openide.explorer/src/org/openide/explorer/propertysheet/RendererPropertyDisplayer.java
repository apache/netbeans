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
 * RendererPropertyDisplayer_java
 *
 * Created on 18 October 2003, 11:52
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.Property;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import java.beans.PropertyEditor;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;


/** A component which displays a property but cannot edit it.
 *
 * @author  Tim Boudreau
 */
final class RendererPropertyDisplayer extends JComponent implements PropertyDisplayer_Mutable, PropertyDisplayer_Inline {
    private boolean showCustomEditorButton = true;
    private boolean tableUI = false;
    private int radioButtonMax = 0;
    private boolean useLabels = true;
    private Property prop;
    private boolean radioBoolean;
    private ReusablePropertyEnv reusableEnv = new ReusablePropertyEnv(); //XXX pass from PP?
    private ReusablePropertyModel reusableModel = new ReusablePropertyModel(reusableEnv);
    boolean inGetRenderer = false;
    private Dimension prefSize = null;
    private RendererFactory rendererFactory1 = null;
    private RendererFactory rendererFactory2 = null;

    public RendererPropertyDisplayer(Property p) {
        this.prop = p;
    }

    public Component getComponent() {
        return this;
    }

    public Property getProperty() {
        return prop;
    }

    public void refresh() {
        repaint();
    }

    public void validate() {
        if (!tableUI) {
            super.validate();
        }
    }

    public boolean isValid() {
        if (tableUI) {
            return true;
        } else {
            return super.isValid();
        }
    }

    public boolean isShowing() {
        if (tableUI) {
            return true;
        } else {
            return super.isShowing();
        }
    }

    public void setProperty(Property prop) {
        if (prop == null) {
            throw new NullPointerException("Property cannot be null"); //NOI18N
        }

        if (prop != this.prop) {
            this.prop = prop;
            prefSize = null;

            if (isShowing()) {
                firePropertyChange("preferredSize", null, null); //NOI18N
            }

            repaint();
        }
    }

    public final boolean isShowCustomEditorButton() {
        boolean result = showCustomEditorButton;

        if (getProperty() != null) {
            Boolean explicit = (Boolean) getProperty().getValue("suppressCustomEditor"); //NOI18N

            if (explicit != null) {
                result = !explicit.booleanValue();
            }
        }

        return result;
    }

    public boolean isTableUI() {
        return tableUI;
    }

    public boolean isUseLabels() {
        return useLabels;
    }

    public void setUseLabels(boolean useLabels) {
        if (useLabels != this.useLabels) {
            Dimension oldPreferredSize = null;

            if (isShowing()) {
                JComponent innermost = findInnermostRenderer(getRenderer(this));

                if (innermost instanceof RadioInplaceEditor || innermost instanceof JCheckBox) {
                    oldPreferredSize = getPreferredSize();
                }
            }

            this.useLabels = useLabels;

            if (oldPreferredSize != null) {
                firePropertyChange("preferredSize", oldPreferredSize, getPreferredSize()); //NOI18N
            }
        }
    }

    public void setShowCustomEditorButton(boolean val) {
        //A property saying it doesn't want the custom editor button should
        //always override anything set on the property panel.
        if (getProperty() != null) {
            Boolean explicit = (Boolean) getProperty().getValue("suppressCustomEditor"); //NOI18N

            if (explicit != null) {
                val = explicit.booleanValue();
            }
        }

        if (showCustomEditorButton != val) {
            prefSize = null;

            Dimension oldPreferredSize = null;

            if (isShowing()) {
                oldPreferredSize = getPreferredSize();
            }

            showCustomEditorButton = val;

            if (oldPreferredSize != null) {
                firePropertyChange("preferredSize", oldPreferredSize, getPreferredSize()); //NOI18N
                repaint();
            }
        }
    }

    public void setTableUI(boolean val) {
        if (val != tableUI) {
            tableUI = val;
            repaint();
        }
    }

    public int getRadioButtonMax() {
        return radioButtonMax;
    }

    public void setRadioButtonMax(int i) {
        if (i != radioButtonMax) {
            Dimension oldPreferredSize = null;

            if (isShowing()) {
                oldPreferredSize = getPreferredSize();
            }

            int old = radioButtonMax;
            radioButtonMax = i;

            if (oldPreferredSize != null) {
                //see if the change will affect anything
                PropertyEditor ed = PropUtils.getPropertyEditor(prop);
                String[] tags = ed.getTags();

                if (tags != null) {
                    if ((tags.length >= i) != (tags.length >= old)) {
                        firePropertyChange("preferredSize", oldPreferredSize, getPreferredSize()); //NOI18N
                    }
                }
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Inline editor for property "); //NOI18N
        sb.append(getProperty().getDisplayName());
        sb.append(" = "); //NOI18N
        sb.append(getProperty());
        sb.append(" inplace editor="); //NOI18N

        if (!inGetRenderer) {
            try {
                sb.append(getRenderer(this));
            } catch (Exception e) {
                sb.append(e);
            }
        }

        return sb.toString();
    }

    public void paintComponent(Graphics g) {
        //Hack for issue 38132 - Beans are set via TTVBridge as a package
        //private property on the parent PropertyPanel (if there is one).
        //FindBeans() will locate the beans either in the model or as a 
        //property of the PropertyPanel (if an esoteric undocumented client property
        //is set on the PropertyPanel).  RendererFactory will set the env
        //value (if there is an env) to the value of ReusablePropertyEnv.NODE
        //(a performance hack to avoid creating 1 property env for each property
        //painted each time we paint).  Cool, huh?
        reusableEnv.setNode(EditorPropertyDisplayer.findBeans(this));

        JComponent comp = getRenderer(this);
        prepareRenderer(comp);
        comp.setBounds(0, 0, getWidth(), getHeight());

        if (comp instanceof InplaceEditor) {
            Component inner = findInnermostRenderer(comp);
            SwingUtilities.paintComponent(g, comp, this, 0, 0, getWidth(), getHeight());
            removeAll();
            return;
        }

        comp.paint(g);
    }

    protected void superPaintComponent(Graphics g) {
        super.paintComponent(g);
    }

    JComponent getRenderer(PropertyDisplayer_Inline inline) {
        inGetRenderer = true;

        JComponent result = rfactory(inline).getRenderer(inline.getProperty());

        if (inline.isTableUI() && null == result.getBorder() ) {
            //Actually want an empty border, not null - some components treat
            //a null border as an invitation to improvise
            result.setBorder(BorderFactory.createEmptyBorder());
        }

        inGetRenderer = false;

        return result;
    }

    protected void prepareRenderer(JComponent comp) {
        comp.setBackground(getBackground());
        comp.setForeground(getForeground());
        comp.setBounds(0, 0, getWidth(), getHeight());

        JComponent innermost;

        if ((innermost = findInnermostRenderer(comp)) instanceof JComboBox) {
            if (comp.getLayout() != null) {
                comp.getLayout().layoutContainer(comp);
            }
        }

        if (!isTableUI() && ((InplaceEditor) comp).supportsTextEntry()) {
            innermost.setBackground(PropUtils.getTextFieldBackground());
            innermost.setForeground(PropUtils.getTextFieldForeground());
        }
    }

    public boolean isTitleDisplayed() {
        if (!useLabels) {
            return false;
        }

        JComponent jc = getRenderer(this);

        if (jc instanceof InplaceEditor) {
            InplaceEditor innermost = PropUtils.findInnermostInplaceEditor((InplaceEditor) jc);

            return innermost instanceof CheckboxInplaceEditor || innermost instanceof RadioInplaceEditor;
        }

        return false;
    }

    static JComponent findInnermostRenderer(JComponent comp) {
        if (comp instanceof InplaceEditor) {
            InplaceEditor ine = (InplaceEditor) comp;

            return PropUtils.findInnermostInplaceEditor(ine).getComponent();
        } else {
            return comp;
        }
    }

    public Dimension getPreferredSize() {
        //Optimize it shows 16% of painting time is in this call.  In some
        //cases it will be called more than once, so cache the return value
        if (prefSize == null) {
            JComponent jc = getRenderer(this);
            prefSize = jc.getPreferredSize();
        }

        return prefSize;
    }

    RendererFactory rfactory(final PropertyDisplayer_Inline inline) {
        RendererFactory factory;

        if (inline.isTableUI()) {
            if (rendererFactory1 == null) {
                rendererFactory1 = new RendererFactory(
                        inline.isTableUI(), inline.getReusablePropertyEnv(),
                        inline.getReusablePropertyEnv().getReusablePropertyModel()
                    );
            }

            factory = rendererFactory1;
        } else {
            if (rendererFactory2 == null) {
                rendererFactory2 = new RendererFactory(
                        inline.isTableUI(), inline.getReusablePropertyEnv(),
                        inline.getReusablePropertyEnv().getReusablePropertyModel()
                    );
            }

            factory = rendererFactory2;
        }

        factory.setUseRadioBoolean(inline.isRadioBoolean());
        factory.setUseLabels(inline.isUseLabels());
        factory.setRadioButtonMax(inline.getRadioButtonMax());

        return factory;
    }

    public boolean isRadioBoolean() {
        return radioBoolean;
    }

    public void setRadioBoolean(boolean b) {
        radioBoolean = b;
    }

    public ReusablePropertyEnv getReusablePropertyEnv() {
        return reusableEnv;
    }

    public void firePropertyChange(String name, Object old, Object nue) {
        //do nothing
    }
}
