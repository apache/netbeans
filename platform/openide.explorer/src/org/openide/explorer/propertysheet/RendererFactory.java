/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * RendererFactory.java
 *
 * Created on 28 September 2003, 17:29
 */
package org.openide.explorer.propertysheet;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import org.openide.awt.GraphicsUtils;
import org.openide.awt.HtmlRenderer;
import org.openide.nodes.Node.Property;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/** 
 * Factory for renderers which can display properties.  With the exception
 * of the string renderer (which, if tableUI is passed to the constructor,
 * is a simple JLabel), the renderers are subclasses of the various
 * InplaceEditor implementations in this package, which are subclassed in
 * order to suppress all property change events and to call <code>clear()</code>
 * after any call to <code>paint()</code>.  What this means is that once a
 * renderer is fetched to paint a property, paint may be called <strong>
 * exactly once</strong>, after which it will have unconfigured itself (this
 * is to avoid memory leaks due to held references).  Whenever it is necessary
 * to repaint, the user of this factory class <strong>must</strong> once
 * again fetch a renderer (this also ensures that the renderer used will
 * always reflect the current state of the property - no caching of possibly
 * stale state information is possible).
 *
 * @author  Tim Boudreau
 */
final class RendererFactory {
    
    /**
     * Do not try to support huge strings (longer than this constant) as HTML.
     * Unfortunately, this is a hidden constant from the client's point of view.
     */
    private static final int MAX_HTML_LENGTH = 1048576;

    private StringRenderer stringRenderer;
    private CheckboxRenderer checkboxRenderer;
    private ComboboxRenderer comboboxRenderer;
    private RadioRenderer radioRenderer;
    private TextFieldRenderer textFieldRenderer;
    private ButtonPanel buttonPanel;
    private IconPanel iconPanel;
    private final ReusablePropertyModel mdl;
    private final ReusablePropertyEnv env;
    private PropertyChangeListener activeThemeListener;
    private boolean tableUI;
    private boolean suppressButton;
    private int radioButtonMax = -1;
    private boolean useRadioBoolean = PropUtils.forceRadioButtons;
    private boolean useLabels;

    /** Creates a new instance of RendererFactory */
    public RendererFactory(boolean tableUI, ReusablePropertyEnv env, ReusablePropertyModel mdl) {
        this.tableUI = tableUI;
        this.env = env;
        this.mdl = mdl;
        assert mdl != null && env != null;

        
        //reset renderers when windows theme is changing (classic <-> xp)
        ToolkitPropertyChangeListenerProxy tp = new ToolkitPropertyChangeListenerProxy();
        activeThemeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                stringRenderer = null;
                checkboxRenderer = null;
                comboboxRenderer = null;
                radioRenderer = null;
                textFieldRenderer = null;
                buttonPanel = null;
                iconPanel = null;
            }
        };
        tp.addPropertyChangeListener(WeakListeners.propertyChange(activeThemeListener, tp));

    }

    private static class ToolkitPropertyChangeListenerProxy implements PropertyChangeListener {

        private PropertyChangeListener l;

        public ToolkitPropertyChangeListenerProxy() {
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            this.l = l;
            Toolkit.getDefaultToolkit().addPropertyChangeListener(
                    "win.xpstyle.themeActive",   //NOI18N
                    this);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            if (this.l == l) {
                Toolkit.getDefaultToolkit().removePropertyChangeListener(
                        "win.xpstyle.themeActive",   //NOI18N
                        this);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            l.propertyChange(evt);
        }
    }

    public void setRadioButtonMax(int i) {
        radioButtonMax = i;
    }

    public void setSuppressButton(boolean val) {
        suppressButton = val;
    }

    void setUseRadioBoolean(boolean val) {
        useRadioBoolean = val;
    }

    /** Set whether or not radio and checkbox editors should show the property
     * name */
    void setUseLabels(boolean val) {
        useLabels = val;
    }

    /** Get a renderer component appropriate to a given property */
    public JComponent getRenderer(Property prop) {
        mdl.setProperty(prop);
        env.reset();

        PropertyEditor editor = preparePropertyEditor(mdl, env);

        if (editor instanceof ExceptionPropertyEditor) {
            return getExceptionRenderer((Exception) editor.getValue());
        }

        JComponent result = null;

        try {
            if (editor.isPaintable()) {
                result = prepareString(editor, env);
            } else {
                Class c = mdl.getPropertyType();

                if ((c == Boolean.class) || (c == boolean.class)) {
                    //Special handling for hinting for org.netbeans.beaninfo.BoolEditor
                    boolean useRadioRenderer = useRadioBoolean ||
                        (env.getFeatureDescriptor().getValue("stringValues") != null); //NOI18N

                    if (useRadioRenderer) {
                        result = prepareRadioButtons(editor, env);
                    } else {
                        result = prepareCheckbox(editor, env);
                    }
                } else if (editor.getTags() != null) {
                    String[] s = editor.getTags();
                    boolean editAsText = Boolean.TRUE.equals(prop.getValue("canEditAsText"));

                    if ((s.length <= radioButtonMax) && !editAsText) {
                        result = prepareRadioButtons(editor, env);
                    } else {
                        result = prepareCombobox(editor, env);
                    }
                } else {
                    result = prepareString(editor, env);
                }
            }

            if ((result != radioRenderer) && (result != textFieldRenderer)) {
                if ((result != checkboxRenderer) && tableUI && !(result instanceof JComboBox)) {
                    result.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
                } else if ((result instanceof JComboBox) && tableUI) {
                    result.setBorder(BorderFactory.createEmptyBorder());
                } else if (!(result instanceof JComboBox) && (!(result instanceof JCheckBox))) {
                    result.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
                }
            }
        } catch (Exception e) {
            result = getExceptionRenderer(e);
            Logger.getLogger(RendererFactory.class.getName()).log(Level.WARNING, null, e);
        }

        result.setEnabled(prop.canWrite());

        boolean propRequestsSuppressButton = Boolean.TRUE.equals(prop.getValue("suppressCustomEditor")); //NOI18N

        if (
            !(result instanceof JLabel) &&
                ((env.getState() == ReusablePropertyEnv.STATE_INVALID) || (prop.getValue("valueIcon") != null))
        ) { //NOI18N
            result = prepareIconPanel(editor, env, (InplaceEditor) result);
        }

        /* If we need a custom editor button, embed the resulting component in
         an instance of ButtonPanel and return that */
        if (
            editor.supportsCustomEditor() && !PropUtils.noCustomButtons && !suppressButton &&
                !propRequestsSuppressButton
        ) {
            ButtonPanel bp = buttonPanel();
            bp.setInplaceEditor((InplaceEditor) result);
            result = bp;
        }

        return result;
    }

    private IconPanel prepareIconPanel(PropertyEditor ed, PropertyEnv env, InplaceEditor inner) {
        IconPanel icp = iconPanel();
        icp.setInplaceEditor(inner);
        icp.connect(ed, env);

        return icp;
    }

    private PropertyEditor preparePropertyEditor(PropertyModel pm, PropertyEnv env) {
        PropertyEditor result;

        try {
            if (pm instanceof NodePropertyModel) {
                result = ((NodePropertyModel) pm).getPropertyEditor();
            } else if (pm instanceof ReusablePropertyModel) {
                result = ((ReusablePropertyModel) pm).getPropertyEditor();
            } else {
                Class c = pm.getPropertyEditorClass();

                if (c != null) {
                    try {
                        result = (PropertyEditor) c.getDeclaredConstructor().newInstance();

                        //Check the values first
                        Object mdlValue = pm.getValue();
                        Object edValue = result.getValue();

                        if (edValue != mdlValue) {
                            result.setValue(pm.getValue());
                        }
                    } catch (Exception e) {
                        result = new ExceptionPropertyEditor(e);
                    }
                } else {
                    result = PropUtils.getPropertyEditor(pm.getPropertyType());

                    try {
                        result.setValue(pm.getValue());
                    } catch (InvocationTargetException ite) {
                        result = new ExceptionPropertyEditor(ite);
                    }
                }
            }
        } catch (Exception e) {
            result = new ExceptionPropertyEditor(e);
        }

        if (result instanceof ExPropertyEditor) {
            ((ExPropertyEditor) result).attachEnv(env);
        }

        return result;
    }

    private JComponent getExceptionRenderer(Exception e) {
        //Anything may have gone wrong, don't rely on other infrastructure
        ExceptionRenderer lbl = new ExceptionRenderer();
        lbl.setForeground(PropUtils.getErrorColor());
        lbl.setText(e.getMessage());

        return lbl;
    }

    public JComponent getStringRenderer() {
        StringRenderer result = stringRenderer();
        result.clear();
        result.setEnabled(true);

        return result;
    }

    private JComponent prepareRadioButtons(PropertyEditor editor, PropertyEnv env) {
        RadioRenderer ren = radioRenderer();
        ren.clear();
        ren.setUseTitle(useLabels);
        ren.connect(editor, env);

        return ren.getComponent();
    }

    private JComponent prepareCombobox(PropertyEditor editor, PropertyEnv env) {
        ComboboxRenderer ren = comboboxRenderer();
        ren.clear();
        ren.setEnabled(true);
        ren.connect(editor, env);

        return ren.getComponent();
    }

    private JComponent prepareString(PropertyEditor editor, PropertyEnv env) {
        InplaceEditor ren = (tableUI || editor.isPaintable()) ? (InplaceEditor) stringRenderer()
                                                              : (InplaceEditor) textFieldRenderer();
        ren.clear();
        ren.getComponent().setEnabled(true);
        ren.connect(editor, env);

        return ren.getComponent();
    }

    private JComponent prepareCheckbox(PropertyEditor editor, PropertyEnv env) {
        CheckboxRenderer ren = checkboxRenderer();
        ren.setUseTitle(useLabels);
        ren.clear();
        ren.setEnabled(true);
        ren.connect(editor, env);

        return ren.getComponent();
    }

    private ButtonPanel buttonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new ButtonPanel();
        }

        buttonPanel.setEnabled(true);

        return buttonPanel;
    }

    private IconPanel iconPanel() {
        if (iconPanel == null) {
            iconPanel = new IconPanel();
        }

        iconPanel.setEnabled(true);

        return iconPanel;
    }

    /**
     * Lazily creates a combo box renderer
     */
    private ComboboxRenderer comboboxRenderer() {
        if (comboboxRenderer == null) {
            comboboxRenderer = new ComboboxRenderer(tableUI);

            //Mainly for debugging
            ((JComponent) comboboxRenderer).setName(
                "ComboboxRenderer for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return comboboxRenderer;
    }

    /**
     * Lazily creates a string renderer
     */
    private StringRenderer stringRenderer() {
        if (stringRenderer == null) {
            stringRenderer = new StringRenderer(tableUI);

            //Mainly for debugging
            ((JComponent) stringRenderer).setName(
                "StringRenderer for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return stringRenderer;
    }

    /**
     * Lazily creates a checkbox renderer
     */
    private CheckboxRenderer checkboxRenderer() {
        if (checkboxRenderer == null) {
            checkboxRenderer = new CheckboxRenderer();

            //Mainly for debugging
            ((JComponent) checkboxRenderer).setName(
                "CheckboxRenderer for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return checkboxRenderer;
    }

    /**
     * Lazily creates a radio button renderer
     */
    private RadioRenderer radioRenderer() {
        if (radioRenderer == null) {
            radioRenderer = new RadioRenderer(tableUI);

            //Mainly for debugging
            ((JComponent) radioRenderer).setName(
                "RadioRenderer for " + getClass().getName() + "@" + System.identityHashCode(this)
            ); //NOI18N
        }

        return radioRenderer;
    }

    /**
     * Lazily creates a text field renderer
     */
    private TextFieldRenderer textFieldRenderer() {
        if (textFieldRenderer == null) {
            textFieldRenderer = new TextFieldRenderer();
        }

        return textFieldRenderer;
    }

    /**
     * Makes the given String displayble. Probably there doesn't exists
     * perfect solution for all situation. (someone prefer display those
     * squares for undisplayable chars, someone unicode placeholders). So lets
     * try do the best compromise.
     */
    private static String makeDisplayble(String str, Font f) {
        if (null == str) {
            return str;
        }

        if (null == f) {
            f = new JLabel().getFont();
        }

        StringBuilder buf = new StringBuilder(str.length() * 6); // x -> \u1234
        final int length = str.length();
        for (int offset = 0; offset < length; ) {
            final int cp = str.codePointAt(offset);

            switch (cp) {
            // label doesn't interpret tab correctly
            case '\t':
                buf.append("        "); // NOI18N
                break;

            case '\n':
                break;

            case '\r':
                break;

            case '\b':
                buf.append("\\b");

                break; // NOI18N

            case '\f':
                buf.append("\\f");

                break; // NOI18N

            default:

                if ((null == f) || f.canDisplay(cp)) {
                    buf.appendCodePoint(cp);
                } else {
                    for (char c : Character.toChars(cp)) {
                        buf.append("\\u"); // NOI18N

                        String hex = Integer.toHexString(c);

                        for (int j = 0; j < (4 - hex.length()); j++)
                            buf.append('0');

                        buf.append(hex);
                    }
                }
            }
            offset += Character.charCount(cp);
        }

        return buf.toString();
    }

    static final class ComboboxRenderer extends ComboInplaceEditor {
        private Object item = null;
        boolean editable = false;

        public ComboboxRenderer(boolean tableUI) {
            super(tableUI);
        }

        public boolean isEditable() {
            return false;
        }

        /** Overridden to clear state after painting once */
        public void paintComponent(Graphics g) {
            setEnabled(isEnabled() && env.isEditable());

            //We may paint without a parent in PropertyPanel, so do a layout to
            //ensure there's something to paint
            doLayout(); //just in case some L&F will render directly
            super.paintComponent(g);

            //Clear cached values
            clear();
        }

        public void clear() {
            super.clear();
            item = null;
        }

        public void setSelectedItem(Object o) {
            item = o;

            if ((item == null) && (editor != null) && (editor.getTags().length > 0)) {
                item = editor.getTags()[0];
            }

            if (editable) {
                getEditor().setItem(getSelectedItem());
            }
        }

        public Object getSelectedItem() {
            return item;
        }

        public void installAncestorListener() {
            //do nothing
        }

        /** Overridden to block code in ComboInplaceEditor */
        public void processFocusEvent(FocusEvent fe) {
            //do nothing
        }

        public void processMouseEvent(MouseEvent me) {
            //do nothing
        }

        /** Overridden to do nothing */
        public void addActionListener(ActionListener ae) {
        }

        /** Overridden to do nothing */
        protected void fireActionPerformed(ActionEvent ae) {
        }

        /** Overridden to do nothing */
        protected void fireStateChanged() {
        }

        /** Overridden only fire those properties needed */
        protected void firePropertyChange(String name, Object old, Object nue) {
            //firing all changes for now - breaks text painting on OS-X
            super.firePropertyChange(name, old, nue);
        }
    }

    private static final class CheckboxRenderer extends CheckboxInplaceEditor {
        /** Overridden to clear state after painting once */
        public void paintComponent(Graphics g) {
            setEnabled(PropUtils.checkEnabled(this, editor, env));
            super.paintComponent(g);
            clear();
        }

        /** Overridden to do nothing */
        protected void fireActionPerformed(ActionEvent ae) {
        }

        /** Overridden to do nothing */
        protected void fireStateChanged() {
        }

        /** Overridden only fire those properties needed */
        protected void firePropertyChange(String name, Object old, Object nue) {
            //gtk L&F needs these, although bg and fg don't work on it in 1.4.2
            if ("foreground".equals(name) || "background".equals(name) || "font".equals(name)) { //NOI18N
                super.firePropertyChange(name, old, nue);
            }
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, boolean old, boolean nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, int old, int nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, byte old, byte nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, char old, char nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, double old, double nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, float old, float nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, short old, short nue) {
        }
    }

    /** A renderer for string properties, which can also delegate to the
     *   property editor's <code>paint()</code>method if possible. */
    private static final class StringRenderer extends JLabel implements InplaceEditor {
        private PropertyEditor editor = null;
        private PropertyEnv env = null;
        private boolean tableUI = false;
        private boolean enabled = true;
        private JLabel htmlLabel = HtmlRenderer.createLabel();
        private JLabel noHtmlLabel = new JLabel();
        private Object value = null;
        private static final boolean isGTK = "GTK".equals( UIManager.getLookAndFeel().getID() ); //NOI18N

        public StringRenderer(boolean tableUI) {
            this.tableUI = tableUI;
            setOpaque(true);
            ((HtmlRenderer.Renderer) htmlLabel).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
        }

        /** OptimizeIt shows about 12Ms overhead calling back to Component.enable(),
         * so overriding */
        @Override
        public void setEnabled(boolean val) {
            enabled = val;
        }

        @Override
        public void setText(String s) {
            if (s != null) {
                if (s.length() > 512) {
                    //IZ 44152 - Debugger producing 512K long strings, etc.
                    super.setText(makeDisplayble(s.substring(0, 512), getFont()));
                } else {
                    super.setText(makeDisplayble(s, getFont()));
                }
            } else {
                super.setText(""); //NOI18N
            }
        }

        /** OptimizeIt shows about 12Ms overhead calling back to Component.enable(),
         * so overriding */
        @Override
        public boolean isEnabled() {
            return enabled;
        }

        /** Overridden to do nothing */
        @Override
        protected void firePropertyChange(String name, Object old, Object nue) {
            //do nothing
        }

        @Override
        public void validate() {
            //do nothing
        }

        @Override
        public void invalidate() {
            //do nothing
        }

        @Override
        public void revalidate() {
            //do nothing
        }

        @Override
        public void repaint() {
            //do nothing
        }

        @Override
        public void repaint(long tm, int x, int y, int w, int h) {
            //do nothing
        }

        @Override
        public Dimension getPreferredSize() {
            if (getText().length() > 1024) {
                //IZ 44152, avoid excessive calculations when debugger
                //returns its 512K+ strings
                return new Dimension(4196, PropUtils.getMinimumPropPanelHeight());
            }

            Dimension result = super.getPreferredSize();
            result.width = Math.max(result.width, PropUtils.getMinimumPropPanelWidth());

            result.height = Math.max(result.height, PropUtils.getMinimumPropPanelHeight());

            return result;
        }

        @Override
        public void paint(Graphics g) {
            if (editor != null) {
                setEnabled(PropUtils.checkEnabled(this, editor, env));
            }

            if (editor instanceof ExceptionPropertyEditor) {
                setForeground(PropUtils.getErrorColor());
            }

            if ((editor != null) && editor.isPaintable()) {
                delegatedPaint(g);
            } else {
                String htmlDisplayValue = (env == null) ? null :
                    (String) env.getFeatureDescriptor().getValue("htmlDisplayValue"); // NOI18N
                boolean htmlValueUsed = htmlDisplayValue != null;

                JLabel lbl = htmlValueUsed ? htmlLabel : noHtmlLabel;
                String text = htmlValueUsed ? htmlDisplayValue : getText();

                if (text == null) {
                    text = ""; // NOI18N
                } else {
                    text = makeDisplayble( text, getFont() );
                }

                if (htmlValueUsed) {
                    // > MAX_HTML_LENGTH = huge strings - don't try to support this as html
                    ((HtmlRenderer.Renderer) lbl).setHtml(text.length() <= MAX_HTML_LENGTH);
                }

                lbl.setFont(getFont());
                lbl.setEnabled(isEnabled());
                lbl.setText(text); //NOI18N

                if (!htmlValueUsed) {
                    lbl.putClientProperty("html", null); // NOI18N
                }

                lbl.setIcon(getIcon());
                lbl.setIconTextGap(getIconTextGap());
                lbl.setBounds(getBounds());
                lbl.setOpaque(true);
                lbl.setBackground(getBackground());
                lbl.setForeground(getForeground());
                lbl.setBorder( getBorder() );
                if ((isGTK || "com.sun.java.swing.plaf.windows.WindowsLabelUI".equals(lbl.getUI().getClass().getName())) 
                        && ! isEnabled() && ! htmlValueUsed) {
                    // the shadow effect from the label was making a problem
                    // let's paint the text "manually" in this case
                    GraphicsUtils.configureDefaultRenderingHints(g);
                    g.setColor(lbl.getBackground());
                    g.fillRect(0, 0, lbl.getWidth(), lbl.getHeight());
                    g.setColor(lbl.getForeground());
                    Icon icon = (lbl.isEnabled()) ? lbl.getIcon() : lbl.getDisabledIcon();
                    
                    FontMetrics fm = g.getFontMetrics();
                    Insets insets = lbl.getInsets(paintViewInsets);
                    
                    paintViewR.x = insets.left;
                    paintViewR.y = insets.top;
                    paintViewR.width = lbl.getWidth() - (insets.left + insets.right);
                    paintViewR.height = lbl.getHeight() - (insets.top + insets.bottom);
                    
                    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
                    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
                    
                    String clippedText =
                            SwingUtilities.layoutCompoundLabel(
                            lbl, fm, text, icon, lbl.getVerticalAlignment(),
                            lbl.getHorizontalAlignment(),
                            lbl.getVerticalTextPosition(),
                            lbl.getHorizontalTextPosition(),
                            paintViewR, paintIconR, paintTextR, lbl.getIconTextGap());
                    
                    
                    if (icon != null) {
                        icon.paintIcon(lbl, g, paintIconR.x, paintIconR.y);
                    }
                    int textX = paintTextR.x;
                    int textY = paintTextR.y + fm.getAscent();
                    int mnemonicIndex = lbl.getDisplayedMnemonicIndex();
                    // we are here only if the property is read-only (disabled)
                    //   --> make the foreground brighter
                    Color fg = lbl.getForeground();
                    if( isGTK ) {
                        //#127522 - debugger color scheme washed out, very hard to read
                        fg = UIManager.getColor("Tree.textForeground" ); //NOI18N
                        if( null == fg )
                            fg = Color.BLACK;
                    }
                    Color changedForeground = fg.brighter();
                    if (Color.BLACK.equals(fg)) {
                        // for some unknown reason the code with brighter does
                        // not work for me!
                        changedForeground = Color.GRAY;
                    }
                    
                    g.setColor(changedForeground);
                    BasicGraphicsUtils.drawStringUnderlineCharAt(g, clippedText, mnemonicIndex,
                            textX, textY);
                } else {
                    lbl.paint(g);
                }
            }

            clear();
        }
        
        // variables for the hack from the above method:
        private static Insets paintViewInsets = new Insets(0, 0, 0, 0);
        private static Rectangle paintIconR = new Rectangle();
        private static Rectangle paintTextR = new Rectangle();
        private static Rectangle paintViewR = new Rectangle();

        private void delegatedPaint(Graphics g) {
            Color c = g.getColor();

            try {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(getForeground());

                if (!tableUI) {
                    //in the panel, give self-painting editors a lowered
                    //border so they look like something
                    Border b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
                    b.paintBorder(this, g, 0, 0, getWidth(), getHeight());
                }

                Rectangle r = getBounds();

                //XXX May be the source of Rochelle's multiple rows of error 
                //marking misalignment problem...(I do not jest)
                r.x = (getWidth() > 16) ? ((editor instanceof Boolean3WayEditor) ? 0 : 3) : 0; //align text with other renderers
                r.width -= ((getWidth() > 16) ? ((editor instanceof Boolean3WayEditor) ? 0 : 3) : 0); //align text with other renderers
                r.y = 0;
                editor.paintValue(g, r);
            } finally {
                g.setColor(c);
            }
        }

        public void clear() {
            editor = null;
            env = null;
            setIcon(null);
            setOpaque(true);
        }

        public void setValue(Object o) {
            value = o;
            setText((value instanceof String) ? (String) value : ((value != null) ? value.toString() : null));
        }

        public void connect(PropertyEditor p, PropertyEnv env) {
            editor = p;
            this.env = env;
            reset();
        }

        public JComponent getComponent() {
            return this;
        }

        public KeyStroke[] getKeyStrokes() {
            return null;
        }

        public PropertyEditor getPropertyEditor() {
            return editor;
        }

        public PropertyModel getPropertyModel() {
            return null;
        }

        public Object getValue() {
            return getText();
        }

        public void handleInitialInputEvent(java.awt.event.InputEvent e) {
            //do nothing
        }

        public boolean isKnownComponent(Component c) {
            return false;
        }

        public void removeActionListener(ActionListener al) {
            //do nothing
        }

        @Override
        public void reset() {
            setText(editor.getAsText());

            Image i = null;
            FeatureDescriptor fd;

            if (env != null) {
                if (env.getState() == PropertyEnv.STATE_INVALID) {
                    setForeground(PropUtils.getErrorColor());
                    i = ImageUtilities.loadImage("org/openide/resources/propertysheet/invalid.gif"); //NOI18N
                } else {
                    fd = env.getFeatureDescriptor();
                    Object o = fd == null ? null : fd.getValue("valueIcon"); //NOI18N

                    if (o instanceof Icon) {
                        setIcon((Icon) o);
                    } else if (o instanceof Image) {
                        i = (Image) o;
                    }
                }
            }

            if (i != null) {
                setIcon(new ImageIcon(i));
            }
        }

        public void setPropertyModel(PropertyModel pm) {
            //do nothing
        }

        public boolean supportsTextEntry() {
            return false;
        }

        /** Overridden to do nothing */
        protected void fireActionPerformed(ActionEvent ae) {
        }

        /** Overridden to do nothing */
        protected void fireStateChanged() {
        }

        public void addActionListener(ActionListener al) {
            //do nothing
        }
    }

    /** A JTextField renderer - the property sheet does not use this, but
     * the property panel does */
    private static final class TextFieldRenderer extends StringInplaceEditor {
        public void paintComponent(Graphics g) {
            setEnabled(PropUtils.checkEnabled(this, editor, env));
            super.paintComponent(g);
            clear();
        }

        /** Overridden to do nothing */
        protected void fireActionPerformed(ActionEvent ae) {
        }

        /** Overridden to do nothing */
        protected void fireStateChanged() {
        }

        /** Overridden to do nothing */
        protected void firePropertyChange(String name, Object old, Object nue) {
            //two changes we need to fire in order to be able to paint properly
            boolean fire = ("locale".equals(name)) || ("document".equals(name)); //NOI18N

            if (fire) {
                super.firePropertyChange(name, old, nue);
            }
        }
    }

    private static final class RadioRenderer extends RadioInplaceEditor {
        private boolean needLayout = true;

        public RadioRenderer(boolean tableUI) {
            super(tableUI);
        }

        public void connect(PropertyEditor pe, PropertyEnv env) {
            super.connect(pe, env);
            needLayout = true;
        }

        public void paint(Graphics g) {
            if (needLayout) {
                getLayout().layoutContainer(this);
                needLayout = false;
            }

            //            setEnabled(PropUtils.checkEnabled (this, editor, env));
            super.paint(g);
            clear();
        }

        /** Renderer version overrides this to create a subclass that won't
         * fire changes */
        protected InvRadioButton createButton() {
            return new NoEventsInvRadioButton();
        }

        /** Renderer version overrides this */
        protected void configureButton(InvRadioButton ire, String txt) {
            if (editor.getTags().length == 1) {
                ire.setEnabled(false);
            } else {
                ire.setEnabled(isEnabled());
            }

            ire.setText(txt);
            ire.setForeground(getForeground());
            ire.setBackground(getBackground());
            ire.setFont(getFont());

            if (txt.equals(editor.getAsText())) {
                ire.setSelected(true);
            } else {
                ire.setSelected(false);
            }
        }

        /** Overridden to do nothing */
        public void addContainerListener(ContainerListener cl) {
        }

        /** Overridden to do nothing */
        public void addChangeListener(ChangeListener cl) {
        }

        /** Overridden to do nothing */
        public void addComponentListener(ComponentListener l) {
        }

        /** Overridden to do nothing */
        public void addHierarchyBoundsListener(HierarchyBoundsListener hbl) {
        }

        /** Overridden to do nothing */
        public void addHierarchyListener(HierarchyListener hl) {
        }

        /** Overridden to do nothing */
        protected void firePropertyChange(String name, Object old, Object nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, boolean old, boolean nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, int old, int nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, byte old, byte nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, char old, char nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, double old, double nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, float old, float nue) {
        }

        /** Overridden to do nothing */
        public void firePropertyChange(String name, short old, short nue) {
        }

        private class NoEventsInvRadioButton extends InvRadioButton {
            /** Overridden to do nothing */
            public void addActionListener(ActionListener al) {
            }

            /** Overridden to do nothing */
            public void addContainerListener(ContainerListener cl) {
            }

            /** Overridden to do nothing */
            public void addChangeListener(ChangeListener cl) {
            }

            /** Overridden to do nothing */
            public void addComponentListener(ComponentListener l) {
            }

            /** Overridden to do nothing */
            public void addHierarchyBoundsListener(HierarchyBoundsListener hbl) {
            }

            /** Overridden to do nothing */
            public void addHierarchyListener(HierarchyListener hl) {
            }

            /** Overridden to do nothing */
            protected void fireActionPerformed(ActionEvent ae) {
            }

            /** Overridden to do nothing */
            protected void fireStateChanged() {
            }

            /** Overridden to do nothing */
            protected void firePropertyChange(String name, Object old, Object nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, boolean old, boolean nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, int old, int nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, byte old, byte nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, char old, char nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, double old, double nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, float old, float nue) {
            }

            /** Overridden to do nothing */
            public void firePropertyChange(String name, short old, short nue) {
            }
        }
    }

    private static final class ExceptionPropertyEditor implements PropertyEditor {
        Exception e;

        public ExceptionPropertyEditor(Exception e) {
            this.e = e;
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            //do nothing
        }

        public String getAsText() {
            return e.getMessage();
        }

        public java.awt.Component getCustomEditor() {
            return null;
        }

        public String getJavaInitializationString() {
            return null;
        }

        public String[] getTags() {
            return null;
        }

        public Object getValue() {
            return e;
        }

        public boolean isPaintable() {
            return false;
        }

        public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
            //do nothing
        }

        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            //do nothing
        }

        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            //do nothing
        }

        public void setValue(Object value) {
            //do nothing
        }

        public boolean supportsCustomEditor() {
            return false;
        }
    }

    /** A JLabel that implements InplaceEditor so consumers can safely cast
     * to InplaceEditor even in the case of problems */
    private class ExceptionRenderer extends JLabel implements InplaceEditor {
        public void addActionListener(ActionListener al) {
            //do nothing
        }

        @Override
        public Color getForeground() {
            return PropUtils.getErrorColor();
        }

        public void clear() {
            //do nothing
        }

        public void connect(PropertyEditor pe, org.openide.explorer.propertysheet.PropertyEnv env) {
            //do nothing
        }

        public JComponent getComponent() {
            return this;
        }

        public KeyStroke[] getKeyStrokes() {
            return null;
        }

        public PropertyEditor getPropertyEditor() {
            return null;
        }

        public org.openide.explorer.propertysheet.PropertyModel getPropertyModel() {
            return null;
        }

        public Object getValue() {
            return getText();
        }

        public boolean isKnownComponent(Component c) {
            return c == this;
        }

        public void removeActionListener(ActionListener al) {
            //do nothing
        }

        public void reset() {
            //do nothing
        }

        public void setPropertyModel(org.openide.explorer.propertysheet.PropertyModel pm) {
            //do nothing
        }

        public void setValue(Object o) {
            //do nothing
        }

        public boolean supportsTextEntry() {
            return false;
        }
    }
}
