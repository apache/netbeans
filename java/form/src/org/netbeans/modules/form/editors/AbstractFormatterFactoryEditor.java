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
package org.netbeans.modules.form.editors;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDesignValueAdapter;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Property editor for JFormattedTextField's format (formatter factory).
 *
 * @author Jan Stola
 */
public class AbstractFormatterFactoryEditor extends PropertyEditorSupport
        implements XMLPropertyEditor, NamedPropertyEditor, PropertyChangeListener, FormAwareEditor {
    /** Format selector used by the editor. */
    private FormatSelector selector;
    /** Property being edited. */
    private FormProperty property;
    
    /**
     * Returns custom property editor (form selector).
     * 
     * @return custom property editor. 
     */
    @Override
    public Component getCustomEditor() {
        if (selector == null) {
            selector = new FormatSelector();
            selector.addPropertyChangeListener(this);
        }
        Object value = getValue();
        if (value instanceof FormFormatter) {
            FormFormatter formatter = (FormFormatter)value;
            selector.setFormat(formatter.getFormat());
        } else {
            selector.setFormat(new FormatSelector.FormatInfo(FormatSelector.FormatInfo.NUMBER, FormatSelector.FormatInfo.DEFAULT, null));
            propertyChange(null);
        }
        return selector.getSelectorPanel();
    }

    /**
     * Returns Java code that corresponds to the selected formatter factory.
     * 
     * @return Java code that corresponds to the selected formatter factory.
     */
    @Override
    public String getJavaInitializationString() {
        Object value = getValue();
        if (!(value instanceof FormFormatter)) {
            return super.getJavaInitializationString();
        }
        FormFormatter formatter = (FormFormatter)value;
        FormatSelector.FormatInfo formatInfo = formatter.getFormat();
        int type = formatInfo.getType();
        int subtype = formatInfo.getSubtype();
        String format = formatInfo.getFormat();
        if (format != null) {
            format = "\"" + format.replace("\"", "\\\"") + "\""; // NOI18N
        }
        String code = null;
        if (type == FormatSelector.FormatInfo.MASK) {
            code = "new javax.swing.text.MaskFormatter(" + format + ")"; // NOI18N
        } else if (type == FormatSelector.FormatInfo.DATE) {
            switch (subtype) {
                case FormatSelector.FormatInfo.NONE:
                    code = "new java.text.SimpleDateFormat(" + format + ")"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.DEFAULT:
                    code = ""; // NOI18N
                    break;
                case FormatSelector.FormatInfo.SHORT:
                    code = "java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.MEDIUM:
                    code = "java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.LONG:
                    code = "java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.FULL:
                    code = "java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL)"; // NOI18N
                    break;
                default:
                    assert false;
                    break;
            }
            code = "new javax.swing.text.DateFormatter(" + code + ")"; // NOI18N
        } else if (type == FormatSelector.FormatInfo.TIME) {
            switch (subtype) {
                case FormatSelector.FormatInfo.NONE:
                    code = "new java.text.SimpleDateFormat(" + format + ")"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.DEFAULT:
                    code = "java.text.DateFormat.getTimeInstance()"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.SHORT:
                    code = "java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.MEDIUM:
                    code = "java.text.DateFormat.getTimeInstance(java.text.DateFormat.MEDIUM)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.LONG:
                    code = "java.text.DateFormat.getTimeInstance(java.text.DateFormat.LONG)"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.FULL:
                    code = "java.text.DateFormat.getTimeInstance(java.text.DateFormat.FULL)"; // NOI18N
                    break;
                default:
                    assert false;
                    break;
            }
            code = "new javax.swing.text.DateFormatter(" + code + ")"; // NOI18N
        } else if (type == FormatSelector.FormatInfo.NUMBER) {
            switch (subtype) {
                case FormatSelector.FormatInfo.NONE:
                    code = "new java.text.DecimalFormat(" + format + ")"; // NOI18N
                    break;
                case FormatSelector.FormatInfo.DEFAULT:
                    code = ""; // NOI18N
                    break;
                case FormatSelector.FormatInfo.INTEGER:
                    code = "java.text.NumberFormat.getIntegerInstance()"; // NOI18N
                    break;
                default:
                    assert false;
                    break;                        
            }
            code = "new javax.swing.text.NumberFormatter(" + code + ")"; // NOI18N
        } else if (type == FormatSelector.FormatInfo.PERCENT) {
            if (subtype == FormatSelector.FormatInfo.DEFAULT) {
                code = "java.text.NumberFormat.getPercentInstance()"; // NOI18N
            } else {
                code = "new java.text.DecimalFormat(" + format + ")"; // NOI18N
            }
            code = "new javax.swing.text.NumberFormatter(" + code + ")"; // NOI18N
        } else if (type == FormatSelector.FormatInfo.CURRENCY) {
            if (subtype == FormatSelector.FormatInfo.DEFAULT) {
                code = "java.text.NumberFormat.getCurrencyInstance()"; // NOI18N
            } else {
                code = "new java.text.DecimalFormat(" + format + ")"; // NOI18N
            }
            code = "new javax.swing.text.NumberFormatter(" + code + ")"; // NOI18N            
        }
        return "new javax.swing.text.DefaultFormatterFactory(" + code + ")"; // NOI18N
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {
        String msg = NbBundle.getMessage(AbstractFormatterFactoryEditor.class, "MSG_AbstractFormatterFactory"); // NOI18N
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, rectangle.x, rectangle.y + (rectangle.height - fm.getHeight())/2 + fm.getAscent());
    }

    /** Pre-code for mask format. */
    private static final String PRE_MASK_CODE = "try {"; // NOI18N
    /** Post-code for mask format. */
    private static final String POST_MASK_CODE = "} catch (java.text.ParseException ex) {\nex.printStackTrace();\n}"; // NOI18N
    
    /**
     * Implementation of property change listener that listens on changes 
     * in selected format.
     * 
     * @param evt property change event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setValue(new FormFormatter(selector.getFormat()));
    }

    @Override
    public void setValue(Object value) {
        updatePrePostCode(value, getValue());
        super.setValue(value);
    }

    /**
     * Updates pre/post code of the edited property.
     * 
     * @param newValue new value of the property.
     * @param oldValue old value of the property.
     */
    private void updatePrePostCode(Object newValue, Object oldValue) {
        if ((newValue instanceof FormFormatter)
                && (((FormFormatter)newValue).getFormat().getType() == FormatSelector.FormatInfo.MASK)) {
            property.setPreCode(PRE_MASK_CODE);
            property.setPostCode(POST_MASK_CODE);
        } else {
            // simple heuristic to avoid modification of real user code
            if (((oldValue instanceof FormFormatter)
                    && (((FormFormatter)oldValue).getFormat().getType() == FormatSelector.FormatInfo.MASK))
                    // Make sure restoreDefaultValue works on mask format before changing this
                    || ((property.getPostCode() != null)
                    && (property.getPreCode().trim().equals(PRE_MASK_CODE)))) {
                property.setPreCode(null);
                property.setPostCode(null);
            }
        }
    }

    /**
     * Determines whether custom property editor is supported.
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    // Implementation of XMLPropertyEditor
    
    /** Tag representing the format. */
    private static final String XML_FORMAT = "Format"; // NOI18N
    /** Attribute describing type of format. */
    private static final String ATTR_TYPE = "type"; // NOI18N
    /** Attribute describing subtype of format. */
    private static final String ATTR_SUBTYPE = "subtype"; // NOI18N
    /** Attribute describing format's pattern. */
    private static final String ATTR_FORMAT = "format"; // NOI18N
    
    @Override
    public void readFromXML(Node element) throws IOException {
        NamedNodeMap attributes = element.getAttributes();
        String typeTxt = attributes.getNamedItem(ATTR_TYPE).getNodeValue();
        String subtypeTxt = attributes.getNamedItem(ATTR_SUBTYPE).getNodeValue();
        String format = null;
        Node node = attributes.getNamedItem(ATTR_FORMAT);
        if (node != null) {
            format = node.getNodeValue();
        }
        int type = Integer.parseInt(typeTxt);
        int subtype = Integer.parseInt(subtypeTxt);
        setValue(new FormFormatter(new FormatSelector.FormatInfo(type, subtype, format)));
    }

    @Override
    public Node storeToXML(Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_FORMAT);
        Object value = getValue();
        FormFormatter formatter = (FormFormatter)value;
        FormatSelector.FormatInfo format = formatter.getFormat();
        el.setAttribute(ATTR_TYPE, "" + format.getType()); // NOI18N
        el.setAttribute(ATTR_SUBTYPE, "" + format.getSubtype()); // NOI18N
        if (format.isCustomizable()) {
            el.setAttribute(ATTR_FORMAT, format.getFormat());
        }
        return el;
    }

    /**
     * Returns display name of the property editor.
     * 
     * @return display name of the property editor.
     */
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CTL_AbstractFormatterFactoryEditor_DisplayName"); // NOI18N
    }

    @Override
    public void setContext(FormModel formModel, FormProperty property) {
        this.property = property;
        // [Why pre/post code was updated here? It breaks after loading - commented out to fix bug 237371.]
//        try {
//            updatePrePostCode(null, property.getValue());
//        } catch (Exception ex) {
//            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
//        }
    }

    /**
     * Raise form version to 6.0 - this editor is available since NB 6.0.
     */
    @Override
    public void updateFormVersionLevel() {
        property.getPropertyContext().getFormModel()
                .raiseVersionLevel(FormModel.FormVersion.NB60, FormModel.FormVersion.NB60);
    }

    /**
     * Form wrapper around formatter.
     */
    static class FormFormatter extends FormDesignValueAdapter {
        /** Information about wrapped format. */
        private FormatSelector.FormatInfo format;

        /**
         * Creates new <code>FormFormatter</code>.
         * 
         * @param format information about wrapped format.
         */
        FormFormatter(FormatSelector.FormatInfo format) {
            this.format = format;
        }
        
        /**
         * Returns information about wrapped format.
         * 
         * @return information about wrapped format.
         */
        public FormatSelector.FormatInfo getFormat() {
            return format;
        }

        /**
         * Returns design value corresponding to this formatter.
         * 
         * @return design value corresponding to this formatter. 
         */
        @Override
        public Object getDesignValue() {
            JFormattedTextField.AbstractFormatter value = null;
            int type = format.getType();
            if (type == FormatSelector.FormatInfo.MASK) {
                try {
                    value = new MaskFormatter(format.getFormat());
                } catch (ParseException pex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, pex.getMessage(), pex);
                    value = new MaskFormatter();
                }
            } else if (type == FormatSelector.FormatInfo.DATE) {
                DateFormat dateFormat = null;
                switch (format.getSubtype()) {
                    case FormatSelector.FormatInfo.NONE:
                        dateFormat = new SimpleDateFormat(format.getFormat());
                        break;
                    case FormatSelector.FormatInfo.DEFAULT:
                        dateFormat = DateFormat.getDateInstance();
                        break;
                    case FormatSelector.FormatInfo.SHORT:
                        dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
                        break;
                    case FormatSelector.FormatInfo.MEDIUM:
                        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
                        break;
                    case FormatSelector.FormatInfo.LONG:
                        dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
                        break;
                    case FormatSelector.FormatInfo.FULL:
                        dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
                        break;
                    default:
                        assert false;
                        break;
                }
                value = new DateFormatter(dateFormat);
            } else if (type == FormatSelector.FormatInfo.TIME) {
                DateFormat timeFormat = null;
                switch (format.getSubtype()) {
                    case FormatSelector.FormatInfo.NONE:
                        timeFormat = new SimpleDateFormat(format.getFormat());
                        break;
                    case FormatSelector.FormatInfo.DEFAULT:
                        timeFormat = DateFormat.getTimeInstance();
                        break;
                    case FormatSelector.FormatInfo.SHORT:
                        timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
                        break;
                    case FormatSelector.FormatInfo.MEDIUM:
                        timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                        break;
                    case FormatSelector.FormatInfo.LONG:
                        timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);
                        break;
                    case FormatSelector.FormatInfo.FULL:
                        timeFormat = DateFormat.getTimeInstance(DateFormat.FULL);
                        break;
                    default:
                        assert false;
                        break;
                }
                value = new DateFormatter(timeFormat);
            } else if (type == FormatSelector.FormatInfo.NUMBER) {
                NumberFormat numberFormat = null;
                switch (format.getSubtype()) {
                    case FormatSelector.FormatInfo.NONE:
                        numberFormat = new DecimalFormat(format.getFormat());
                        break;
                    case FormatSelector.FormatInfo.DEFAULT:
                        numberFormat = NumberFormat.getInstance();
                        break;
                    case FormatSelector.FormatInfo.INTEGER:
                        numberFormat = NumberFormat.getIntegerInstance();
                        break;
                    default:
                        assert false;
                        break;                        
                }
                value = new NumberFormatter(numberFormat);
            } else if (type == FormatSelector.FormatInfo.PERCENT) {
                NumberFormat percentFormat;
                if (format.getSubtype() == FormatSelector.FormatInfo.DEFAULT) {
                    percentFormat = NumberFormat.getPercentInstance();
                } else {
                    percentFormat = new DecimalFormat(format.getFormat());
                }
                value = new NumberFormatter(percentFormat);
            } else if (type == FormatSelector.FormatInfo.CURRENCY) {
                NumberFormat currencyFormat;
                if (format.getSubtype() == FormatSelector.FormatInfo.DEFAULT) {
                    currencyFormat = NumberFormat.getCurrencyInstance();
                } else {
                    currencyFormat = new DecimalFormat(format.getFormat());
                }
                value = new NumberFormatter(currencyFormat);
            } else {
                assert false;
            }
            return new DefaultFormatterFactory(value);
        }
        
        @Override
        public Object copy(FormProperty targetFormProperty) {
            return new FormFormatter(format);
        }

    }

}
