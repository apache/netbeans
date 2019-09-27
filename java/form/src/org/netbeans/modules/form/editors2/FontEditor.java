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

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.openide.awt.Mnemonics;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.CodeVariable;

/**
 * Font property editor that wraps a default property editor for fonts plus it
 * adds the ability to specify relative changes to the font.
 * As ResourceWrapperEditor subclass it also allows to wrap the font in a
 * ResourceValue (store as a resource). This "resource wrapping" is only used
 * for absolute fonts (relative changes can't be resources).
 *
 * @author Jan Stola, Tomas Pavek
 */
public class FontEditor extends ResourceWrapperEditor implements XMLPropertyEditor {
    
    public FontEditor() {
        super(FormPropertyEditorManager.findBasicEditor(Font.class));
    }

    @Override
    public PropertyEditor getDelegatedPropertyEditor() {
        // hack for saving: this is not only a wrapper of FontEditor for
        // absolute fonts, but also a complete property editor for relative
        // fonts - in which case it returns itself as the property editor
        // responsible for saving
        Object value = getValue();
        if (!(value instanceof NbFont))
            return super.getDelegatedPropertyEditor();
        else
            return this;
    }

    @Override
    public void updateFormVersionLevel() {
        if (getValue() instanceof NbFont) {
            formModel.raiseVersionLevel(FormModel.FormVersion.NB60_PRE, FormModel.FormVersion.NB60);
        } else {
            super.updateFormVersionLevel();
        }
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        super.attachEnv(env);
        FeatureDescriptor prop = env.getFeatureDescriptor();
        if (prop != null) {
            prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        }
    }

    @Override
    public Object getUnwrappedValue() {
        // NbFont can't be stored in the delegate FontEditor, so we can't use
        // delegateEditor.getValue() to get the unwrapped value
        Object value = getValue();
        return value instanceof ResourceValue ?
            ((ResourceValue)value).getDesignValue() : value;
    }

    @Override
    protected void setValueToDelegate(Object value) {
        if (value instanceof ResourceValue)
            value = ((ResourceValue)value).getValue();
        if (value instanceof NbFont)
            value = ((NbFont)value).getDesignValue();
        delegateEditor.setValue(value);
    }

    @Override
    public String getJavaInitializationString() {
        String exp;
        Object value = getValue();
        if (value instanceof NbFont) {
            NbFont propertyValue = (NbFont) value;
            RADProperty property = (RADProperty) this.property;
            RADComponent comp = property.getRADComponent();
            CodeVariable var = comp.getCodeExpression().getVariable();
            String varName = (var == null) ? null : var.getName();
            String readMethod = property.getPropertyDescriptor().getReadMethod().getName();
            String getter = readMethod + "()"; // NOI18N
            if (varName != null) {
                getter = varName + '.' + getter;
            }
            exp = getter + ".deriveFont("; // NOI18N
            boolean styleChanged = (propertyValue.italic != null) || (propertyValue.bold != null);
            if (styleChanged) {
                String styleExp = null;
                if (propertyValue.italic != null)  {
                    styleExp = getter + ".getStyle()"; // NOI18N
                    if (Boolean.TRUE.equals(propertyValue.italic)) {
                        styleExp += " | "; // NOI18N
                    } else{
                        styleExp += " & ~"; // NOI18N
                    }
                    styleExp += "java.awt.Font.ITALIC"; // NOI18N
                }
                if (styleExp == null) {
                    styleExp = getter + ".getStyle()"; // NOI18N
                } else {
                    styleExp = "(" + styleExp + ")"; // NOI18N
                }
                if (propertyValue.bold != null)  {
                    if (Boolean.TRUE.equals(propertyValue.bold)) {
                        styleExp += " | "; // NOI18N
                    } else{
                        styleExp += " & ~"; // NOI18N
                    }
                    styleExp += "java.awt.Font.BOLD"; // NOI18N
                }
                exp += styleExp;
            }
            if (propertyValue.absoluteSize) {
                exp += styleChanged ? ", " : "(float)"; // NOI18N
                exp += propertyValue.size + ")"; // NOI18N
            } else {
                if (propertyValue.size == 0) {
                    if (styleChanged) {
                        exp += ')';
                    } else {
                        exp = getter;
                    }
                } else {
                    if (styleChanged) {
                        exp += ", "; // NOI18N
                    }
                    exp += getter + ".getSize()"; // NOI18N
                    if (propertyValue.size > 0) {
                        exp += '+';
                    }
                    exp += propertyValue.size;
                    if (!styleChanged) exp += "f"; // NOI18N
                    exp += ")"; // NOI18N
                }
            }
        } else if (value instanceof Font) {
            // plain font - let the default editor handle the code
            // add NOI18N comment - font name is a string
            exp = "*/\n\\1NOI18N*/\n\\0" + delegateEditor.getJavaInitializationString(); // NOI18N
            // */\n\\1 is a special code mark for line comment
            // */\n\\0 is a special code mark to indicate that a real code follows
        } else { // neither NbFont nor Font - let ResourceWrapperEditor handle it
            exp = super.getJavaInitializationString();
        }
        return exp;
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        return value instanceof NbFont ? ((NbFont)value).getDescription() : super.getAsText();
    }

    @Override
    protected Component createCustomEditorGUI(final Component resourcePanelGUI) {
        Object value = getUnwrappedValue();
        boolean absolute = !(value instanceof NbFont);

        final Component absoluteComp = absolute ? createAbsolutePanel(resourcePanelGUI) : null;
        final JCheckBox switchBox = new JCheckBox();
        switchBox.setVisible((this.property instanceof RADProperty) && (this.property.getDefaultValue() != null));
        Mnemonics.setLocalizedText(switchBox, NbBundle.getMessage(FontEditor.class, "CTL_DeriveFont")); // NOI18N
        switchBox.setSelected(!absolute);
        final RelativeFontPanel relativeComp = new RelativeFontPanel();
        Component pane = absolute ? absoluteComp : relativeComp;
        if (!absolute)
            relativeComp.updateFromPropertyValue();

        final JPanel editor = new JPanel();
        final GroupLayout layout = new GroupLayout(editor);
        editor.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(switchBox))
            .addComponent(pane));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(switchBox)
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pane));

        switchBox.addItemListener(new ItemListener() {
            private Component absoluteInLayout = absoluteComp;
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (switchBox.isSelected()) {
                    layout.replace(absoluteInLayout, relativeComp);
                    convertToRelative();
                    relativeComp.updateFromPropertyValue();
                } else {
                    absoluteInLayout = createAbsolutePanel(resourcePanelGUI);
                    layout.replace(relativeComp, absoluteInLayout);
                    convertToAbsolute();
                }
                editor.revalidate();
                editor.repaint();
            }
        });

        return editor;
    }

    private Component createAbsolutePanel(Component resourcePanelGUI) {
        Component fontEditor = delegateEditor.getCustomEditor();
        if (resourcePanelGUI == null)
            return fontEditor;

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);

        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(fontEditor).addComponent(resourcePanelGUI));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(fontEditor).addComponent(resourcePanelGUI));
        return panel;
    }

    private void convertToRelative() {
        Object value = getUnwrappedValue();
        if (!(value instanceof Font)) {
            return;
        }
        Font font = (Font) value;
        NbFont propertyValue = new NbFont();
        propertyValue.property = property;

        Font defaultFont = (Font)property.getDefaultValue();
        if (propertyValue.absoluteSize) {
            propertyValue.size = font.getSize();
        } else {
            if (defaultFont == null) return;
            propertyValue.size = font.getSize() - defaultFont.getSize();
        }
        if (defaultFont == null) return;
        int absoluteStyle = font.getStyle();
        int defaultStyle = defaultFont.getStyle();
        boolean aItalic = ((absoluteStyle & Font.ITALIC) != 0);
        boolean dItalic = ((defaultStyle & Font.ITALIC) != 0);
        if (aItalic && !dItalic) {
            propertyValue.italic = Boolean.TRUE;
        }
        if (!aItalic && dItalic) {
            propertyValue.italic = Boolean.FALSE;
        }
        if ((propertyValue.italic != null) && (aItalic == dItalic)
            && (aItalic != propertyValue.italic.booleanValue())) {
            propertyValue.italic = null;
        }
        boolean aBold = ((absoluteStyle & Font.BOLD) != 0);
        boolean dBold = ((defaultStyle & Font.BOLD) != 0);
        if (aBold && !dBold) {
            propertyValue.bold = Boolean.TRUE;
        }
        if (!aBold && dBold) {
            propertyValue.bold = Boolean.FALSE;
        }
        if ((propertyValue.bold != null) && (aBold == dBold)
            && (aBold != propertyValue.bold.booleanValue())) {
            propertyValue.bold = null;
        }
        setValue(propertyValue);
    }

    private void convertToAbsolute() {
        Object value = getUnwrappedValue();
        if (value instanceof NbFont) {
            setValue(((NbFont)value).getDesignValue());
        }
    }

    // XMLPropertyEditor implementation
    /** Root of the XML representation of the font. */
    public static final String XML_FONT_ROOT = "FontInfo"; // NOI18N
    /** Element with information about the font. */
    public static final String XML_FONT = "Font"; // NOI18N
    /** Determines whether the font is relative. */
    public static final String ATTR_RELATIVE = "relative"; // NOI18N
    /** Determines whether the font size is relative to the default value. */
    public static final String ATTR_RELATIVE_SIZE = "relativeSize"; // NOI18N
    /** Attribute for the font size. */
    public static final String ATTR_SIZE = "size"; // NOI18N
    /** Attribute for the change of italic. */
    public static final String ATTR_ITALIC_CHANGE = "italic"; // NOI18N
    /** Attribute for the change of thickness. */
    public static final String ATTR_BOLD_CHANGE = "bold"; // NOI18N
    /** Name of the component this value belongs to. */
    public static final String ATTR_COMP_NAME = "component"; // NOI18N
    /** Name of the property this value belongs to. */
    public static final String ATTR_PROP_NAME = "property"; // NOI18N

    @Override
    public void readFromXML(Node element) throws IOException {
        if (!XML_FONT_ROOT.equals(element.getNodeName())) {
            // Backward compatibility with the default FontEditor from core
            ((XMLPropertyEditor)delegateEditor).readFromXML(element);
            setValue(delegateEditor.getValue());
            return;
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        boolean relative = Boolean.valueOf(attributes.getNamedItem(ATTR_RELATIVE).getNodeValue()).booleanValue();
        org.w3c.dom.NodeList subnodes = element.getChildNodes();
        for (int i=0; i<subnodes.getLength(); i++){
            org.w3c.dom.Node subnode = subnodes.item(i);
            if (subnode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                if (relative) {
                    if (!XML_FONT.equals(subnode.getNodeName())) {
                        throw new java.io.IOException();
                    }
                    NbFont propertyValue = new NbFont();
                    propertyValue.property = property;
                    attributes = subnode.getAttributes();
                    propertyValue.absoluteSize = !Boolean.valueOf(attributes.getNamedItem(ATTR_RELATIVE_SIZE).getNodeValue()).booleanValue();
                    propertyValue.size = Integer.parseInt(attributes.getNamedItem(ATTR_SIZE).getNodeValue());
                    org.w3c.dom.Node italicChange = attributes.getNamedItem(ATTR_ITALIC_CHANGE);
                    if (italicChange != null) {
                        propertyValue.italic = Boolean.valueOf(italicChange.getNodeValue());
                    }
                    org.w3c.dom.Node boldChange = attributes.getNamedItem(ATTR_BOLD_CHANGE);
                    if (boldChange != null) {
                        propertyValue.bold = Boolean.valueOf(boldChange.getNodeValue());
                    }
                    setValue(propertyValue);
                } else {
                    ((XMLPropertyEditor)delegateEditor).readFromXML(subnode);
                }
                break;
            }
        }
    }

    @Override
    public Node storeToXML(Document doc) {
        Object value = getUnwrappedValue();
        org.w3c.dom.Element el = doc.createElement(XML_FONT_ROOT);
        el.setAttribute(ATTR_RELATIVE, Boolean.TRUE.toString());
        if (!(value instanceof NbFont)) {// || propertyValue.absolute) {
            org.w3c.dom.Node absNode = ((XMLPropertyEditor)delegateEditor).storeToXML(doc);
            el.appendChild(absNode);
        } else {
            NbFont propertyValue = (NbFont) value;
            org.w3c.dom.Element subel = doc.createElement(XML_FONT);
            el.appendChild(subel);
            subel.setAttribute(ATTR_RELATIVE_SIZE, Boolean.toString(!propertyValue.absoluteSize));
            subel.setAttribute(ATTR_SIZE, Integer.toString(propertyValue.size));
            if (propertyValue.italic != null) {
                subel.setAttribute(ATTR_ITALIC_CHANGE, propertyValue.italic.toString());
            }
            if (propertyValue.bold != null) {
                subel.setAttribute(ATTR_BOLD_CHANGE, propertyValue.bold.toString());
            }
            subel.setAttribute(ATTR_COMP_NAME, ((RADProperty)property).getRADComponent().getName());
            subel.setAttribute(ATTR_PROP_NAME, property.getName());
        }
        return el;
    }

    static class NbFont extends FormDesignValueAdapter {
        /**
         * Describes the relative change of italic.
         * <code>true = add italic, false = remove italic, null = leave it as it is</code>
         */
        Boolean italic;
        /**
         * Describes the relative change of bold.
         * <code>true = add bold, false = remove false, null = leave it as it is</code>
         */
        Boolean bold;
        /**
         * Determines whether the change of the font size is relative or absolute.
         */
        boolean absoluteSize;
        /**
         * Describes the change of the font size.
         * <code>size = (absoluteSize) ? absolute size : relative change</code>
         */
        int size;
        /**
         * Property that contains this value.
         */
        FormProperty property;

        @Override
        public Object getDesignValue() {
            Font value = defaultValue(property);
            if (value != null) {
                int origStyle = value.getStyle();
                int style = origStyle;
                if (italic != null) {
                    if (italic.booleanValue()) {
                        style |= Font.ITALIC;
                    } else {
                        style &= ~Font.ITALIC;
                    }
                }
                if (bold != null) {
                    if (bold.booleanValue()) {
                        style |= Font.BOLD;
                    } else {
                        style &= ~Font.BOLD;
                    }
                }
                int origSize = value.getSize();
                int newSize = (absoluteSize) ? size : (size + origSize);
                if ((style != origStyle) || (origSize != newSize)) {
                    value = value.deriveFont(style, newSize);
                }
            }
            return value;
        }

        private Font defaultValue(FormProperty property) {
            if ((property instanceof RADProperty) && FormLAF.getUsePreviewDefaults()) {
                RADProperty radProp = (RADProperty)property;
                PropertyDescriptor propDesc = radProp.getPropertyDescriptor();
                java.lang.reflect.Method readMethod = propDesc.getReadMethod();
                if (readMethod != null) {
                    try {
                        Class<?> clazz = radProp.getRADComponent().getBeanClass();
                        Object beanInstance = BeanSupport.createBeanInstance(clazz);
                        return (Font)readMethod.invoke(beanInstance, new Object [0]);
                    } catch (Exception e) {
                    }
                }
            }
            return (Font)property.getDefaultValue();
        }

        @Override
        public String getDescription() {
            ResourceBundle bundle = NbBundle.getBundle(FontEditor.class);
            String description;
            description = Integer.toString(size);
            if (!absoluteSize && (size > 0)) {
                description = '+' + description;
            }
            if (italic != null) {
                description += " " + (italic.booleanValue() ? '+' : '-') + bundle.getString("CTL_FontStyleItalic"); // NOI18N
            }
            if (bold != null) {
                description += " " + (bold.booleanValue() ? '+' : '-') + bundle.getString("CTL_FontStyleBold"); // NOI18N
            }
            if (description.charAt(0) == '0') description = description.substring(Math.min(2, description.length()));
            return description;
        }

        @Override
        public FormDesignValue copy(FormProperty targetFormProperty) {
            NbFont copy = copy();
            copy.property = targetFormProperty;
            return copy;
        }

        NbFont copy() {
            NbFont newValue = new NbFont();
            newValue.italic = italic;
            newValue.bold = bold;
            newValue.absoluteSize = absoluteSize;
            newValue.size = size;
            newValue.property = property;
            return newValue;
        }
    }

    /**
     * Panel used to configure the relative change.
     */
    private class RelativeFontPanel extends JPanel {
        private JRadioButton absoluteChoice;
        private JSpinner absoluteSize;
        private JRadioButton addBoldChoice;
        private JRadioButton addItalicChoice;
        private JCheckBox italicCheckBox;
        private JRadioButton relativeChoice;
        private JSpinner relativeSize;
        private JRadioButton removeBoldChoice;
        private JRadioButton removeItalicChoice;
        private JCheckBox thicknessCheckBox;

        private boolean ignoreUpdates;

        RelativeFontPanel() {
            initComponents();
        }

        void updateFromPropertyValue() {
            NbFont propertyValue = (NbFont) getUnwrappedValue();
            if (propertyValue == null) return;

            ignoreUpdates = true;
            boolean changeItalic = (propertyValue.italic != null);
            italicCheckBox.setSelected(changeItalic);
            addItalicChoice.setEnabled(changeItalic);
            removeItalicChoice.setEnabled(changeItalic);
            if (!changeItalic) {
                addItalicChoice.setSelected(true);
            } else if (Boolean.TRUE.equals(propertyValue.italic)) {
                addItalicChoice.setSelected(true);
            } else if (Boolean.FALSE.equals(propertyValue.italic)) {
                removeItalicChoice.setSelected(true);
            }
            boolean changeBold = (propertyValue.bold != null);
            thicknessCheckBox.setSelected(changeBold);
            addBoldChoice.setEnabled(changeBold);
            removeBoldChoice.setEnabled(changeBold);
            if (!changeBold) {
                addBoldChoice.setSelected(true);
            } else if (Boolean.TRUE.equals(propertyValue.bold)) {
                addBoldChoice.setSelected(true);
            } else if (Boolean.FALSE.equals(propertyValue.bold)) {
                removeBoldChoice.setSelected(true);
            }
            absoluteSize.setEnabled(propertyValue.absoluteSize);
            relativeSize.setEnabled(!propertyValue.absoluteSize);
            if (propertyValue.absoluteSize) {
                absoluteSize.setValue(new Integer(propertyValue.size));
                absoluteChoice.setSelected(true);
                synchronizeSizeControls(propertyValue);
            } else {
                relativeSize.setValue(new Integer(propertyValue.size));
                relativeChoice.setSelected(true);
                synchronizeSizeControls(propertyValue);
            }
            ignoreUpdates = false;
        }

        private void synchronizeSizeControls(NbFont propertyValue) {
            if (propertyValue.absoluteSize) {
                Font defaultFont = (Font)property.getDefaultValue();
                if (defaultFont != null) {
                    relativeSize.setValue(new Integer(propertyValue.size - defaultFont.getSize()));
                }
            } else {
                Font font = (Font)propertyValue.getDesignValue();
                absoluteSize.setValue(new Integer(font == null ? 12 : font.getSize()));   
            }
        }

        private void initComponents() {
            relativeSize = new JSpinner(new SpinnerNumberModel(0, Short.MIN_VALUE, Short.MAX_VALUE, 1));
            relativeSize.setEditor(new JSpinner.NumberEditor(relativeSize, "+#;-#")); // NOI18N
            absoluteSize = new JSpinner(new SpinnerNumberModel(12, 1, Short.MAX_VALUE, 1));

            ResourceBundle bundle = NbBundle.getBundle(FontEditor.class);
            JLabel fontSizeLabel = new JLabel(bundle.getString("CTL_FontSize")); // NOI18N
            JLabel fontStyleLabel = new JLabel(bundle.getString("CTL_FontStyle")); // NOI18N

            absoluteChoice = new JRadioButton();
            Mnemonics.setLocalizedText(absoluteChoice, bundle.getString("CTL_AbsoluteFontSize")); // NOI18N

            relativeChoice = new JRadioButton();
            Mnemonics.setLocalizedText(relativeChoice, bundle.getString("CTL_RelativeFontSize")); // NOI18N

            italicCheckBox = new JCheckBox();
            Mnemonics.setLocalizedText(italicCheckBox, bundle.getString("CTL_ChangeItalic")); // NOI18N

            addItalicChoice = new JRadioButton();
            Mnemonics.setLocalizedText(addItalicChoice, bundle.getString("CTL_AddItalic")); // NOI18N

            removeItalicChoice = new JRadioButton();
            Mnemonics.setLocalizedText(removeItalicChoice, bundle.getString("CTL_RemoveItalic")); // NOI18N

            thicknessCheckBox = new JCheckBox();
            Mnemonics.setLocalizedText(thicknessCheckBox, bundle.getString("CTL_ChangeBold")); // NOI18N

            addBoldChoice = new JRadioButton();
            Mnemonics.setLocalizedText(addBoldChoice, bundle.getString("CTL_AddBold")); // NOI18N

            removeBoldChoice = new JRadioButton();
            Mnemonics.setLocalizedText(removeBoldChoice, bundle.getString("CTL_RemoveBold")); // NOI18N

            // Listener
            Listener listener = new Listener();
            relativeChoice.addItemListener(listener);
            thicknessCheckBox.addItemListener(listener);
            italicCheckBox.addItemListener(listener);
            relativeSize.addChangeListener(listener);
            absoluteSize.addChangeListener(listener);
            addItalicChoice.addItemListener(listener);
            addBoldChoice.addItemListener(listener);

            // Radio button groups
            ButtonGroup italicGroup = new ButtonGroup();
            italicGroup.add(addItalicChoice);
            italicGroup.add(removeItalicChoice);

            ButtonGroup thicknessGroup = new ButtonGroup();
            thicknessGroup.add(addBoldChoice);
            thicknessGroup.add(removeBoldChoice);

            ButtonGroup fontSizeGroup = new ButtonGroup();
            fontSizeGroup.add(absoluteChoice);
            fontSizeGroup.add(relativeChoice);

            // Eliminate redundant borders
            Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
            Insets emptyInsets = new Insets(0, 0, 0, 0);

            absoluteChoice.setBorder(emptyBorder);
            absoluteChoice.setMargin(emptyInsets);
            relativeChoice.setBorder(emptyBorder);
            relativeChoice.setMargin(emptyInsets);
            italicCheckBox.setBorder(emptyBorder);
            italicCheckBox.setMargin(emptyInsets);
            addItalicChoice.setBorder(emptyBorder);
            addItalicChoice.setMargin(emptyInsets);
            removeItalicChoice.setBorder(emptyBorder);
            removeItalicChoice.setMargin(emptyInsets);
            thicknessCheckBox.setBorder(emptyBorder);
            thicknessCheckBox.setMargin(emptyInsets);
            addBoldChoice.setBorder(emptyBorder);
            addBoldChoice.setMargin(emptyInsets);
            removeBoldChoice.setBorder(emptyBorder);
            removeBoldChoice.setMargin(emptyInsets);

            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup()
                    .addComponent(fontSizeLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                            .addComponent(relativeChoice)
                            .addComponent(absoluteChoice))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup()
                            .addComponent(relativeSize, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                            .addComponent(absoluteSize, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup()
                    .addComponent(fontStyleLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                            .addComponent(italicCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17)
                                .addGroup(layout.createParallelGroup()
                                    .addComponent(addItalicChoice)
                                    .addComponent(removeItalicChoice))))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup()
                            .addComponent(thicknessCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17)
                                .addGroup(layout.createParallelGroup()
                                    .addComponent(removeBoldChoice)
                                    .addComponent(addBoldChoice))))))
                    .addContainerGap());
            layout.setVerticalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fontSizeLabel)
                    .addComponent(fontStyleLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(relativeChoice)
                    .addComponent(relativeSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(italicCheckBox)
                    .addComponent(thicknessCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(absoluteChoice)
                    .addComponent(absoluteSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(addItalicChoice)
                    .addComponent(addBoldChoice))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(removeItalicChoice)
                    .addComponent(removeBoldChoice))
                .addContainerGap(260, 260));
        }

        private class Listener implements ItemListener, ChangeListener {
            // called when some of the checkboxes/radiobuttons is selected/unselected
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ignoreUpdates) return;
                ignoreUpdates = true;
                NbFont propertyValue = ((NbFont)getUnwrappedValue()).copy();
                Object source = e.getSource();
                if (source == relativeChoice) {
                    boolean relative = relativeChoice.isSelected();
                    relativeSize.setEnabled(relative);
                    absoluteSize.setEnabled(!relative);
                    propertyValue.absoluteSize = !relative;
                    propertyValue.size = ((Number)((relative ? relativeSize : absoluteSize).getValue())).intValue();
                } else if (source == italicCheckBox) {
                    boolean changeItalic = italicCheckBox.isSelected();
                    addItalicChoice.setEnabled(changeItalic);
                    removeItalicChoice.setEnabled(changeItalic);
                    propertyValue.italic = changeItalic ? Boolean.valueOf(addItalicChoice.isSelected()) : null;
                } else if (source == thicknessCheckBox) {
                    boolean changeBold = thicknessCheckBox.isSelected();
                    addBoldChoice.setEnabled(changeBold);
                    removeBoldChoice.setEnabled(changeBold);
                    propertyValue.bold = changeBold ? Boolean.valueOf(addBoldChoice.isSelected()) : null;
                } else if (source == addBoldChoice) {
                    propertyValue.bold = Boolean.valueOf(addBoldChoice.isSelected());
                } else if (source == addItalicChoice) {
                    propertyValue.italic = Boolean.valueOf(addItalicChoice.isSelected());
                }
                ignoreUpdates = false;
                setValue(propertyValue);
            }

            // called when the size of the font is changed in one of the spinners
            @Override
            public void stateChanged(ChangeEvent e) {
                if (ignoreUpdates) return;
                ignoreUpdates = true;
                NbFont propertyValue = ((NbFont)getUnwrappedValue()).copy();
                Object source = e.getSource();
                if (source == relativeSize) {
                    propertyValue.size = ((Number)relativeSize.getValue()).intValue();
                    synchronizeSizeControls(propertyValue);
                } else if(source == absoluteSize) {
                    propertyValue.size = ((Number)absoluteSize.getValue()).intValue();
                    synchronizeSizeControls(propertyValue);
                }
                ignoreUpdates = false;
                setValue(propertyValue);
            }
        }
    }
    
}
