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

package org.netbeans.modules.form.editors2;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import java.text.MessageFormat;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.*;

import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.nodes.*;
import org.openide.explorer.view.ListView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.explorer.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.*;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.Exceptions;

/**
 * A property editor for swing border class.
 */
public final class BorderEditor extends PropertyEditorSupport
                                implements FormAwareEditor,
                                           XMLPropertyEditor,
                                           NamedPropertyEditor,
					   BeanPropertyEditor
{
    /** Icon base for unknown border node. */
    private static final String UNKNOWN_BORDER_BASE =
        "org/netbeans/modules/form/editors2/unknownBorder.gif"; // NOI18N
    /** Icon base for no border node. */
    private static final String NO_BORDER_BASE =
        "org/netbeans/modules/form/editors2/nullBorder.gif"; // NOI18N

    private static final FormProperty[] EMPTY_PROPERTIES = new FormProperty[0];
    
    // --------------
    // variables

    private Object current;

    private FormPropertyContext propertyContext;
    private BorderDesignSupport borderSupport;

    // customizer
    private BorderPanel bPanel;

    // --------------
    // init

    public BorderEditor() {
        bPanel = null;
        current = null;
    }

    // FormAwareEditor implementation
    @Override
    public void setContext(FormModel model, FormProperty property) {
        propertyContext = new FormPropertyContext.SubProperty(property);
    }

    // FormAwareEditor implementation
    @Override
    public void updateFormVersionLevel() {
    }

    // ------------------
    // main methods

    @Override
    public Object getValue() {
        return current;
    }

    @Override
    public void setValue(Object value) {
        if (current == value)
            return;

        current = value;
        borderSupport = null;

        if (value instanceof BorderDesignSupport) {
            borderSupport = (BorderDesignSupport) value;
        } else if (value != null) {
            assert (value instanceof Border);
            if (!(value instanceof javax.swing.plaf.UIResource))
                borderSupport = new BorderDesignSupport((Border)value);
        }

        if (borderSupport != null) {
            borderSupport.setPropertyContext(propertyContext);

            if (bPanel != null)
                bPanel.setValue(value);
        }
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public void setAsText(String string) {
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics g, Rectangle rectangle) {

        String valueText;
        Object value = getValue();

        if (value == null)
            valueText = getBundle().getString("LAB_NoBorder"); // NOI18N
        else if (borderSupport != null)
            valueText = "[" + borderSupport.getDisplayName() + "]"; // NOI18N
        else
            valueText = "[" + org.openide.util.Utilities.getShortClassName( // NOI18N
                            value.getClass()) + "]"; // NOI18N
 

        FontMetrics fm = g.getFontMetrics();
        g.drawString(valueText, rectangle.x,
                       rectangle.y + (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    @Override
    public String getJavaInitializationString() {
        Object value = getValue();
        if (value == null)
            return "null"; // NOI18N
        if (borderSupport != null)
            return borderSupport.getJavaInitializationString();

        // nothing to generate otherwise
        return null;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (bPanel == null)
            bPanel = new BorderPanel();

        bPanel.setValue(current);
        return bPanel;
    }

    // ------------------------------------------
    // NamedPropertyEditor implementation

    /** @return display name of the property editor */
    @Override
    public String getDisplayName() {
        return getBundle().getString("CTL_BorderEditor_DisplayName"); // NOI18N
    }

    // ----------------

    /** Update the BorderDesignSupport object according to recent changes.
     * This is needed when another border was selected or some property of
     * currently selected border was changed.
     */
    void updateBorder(Node node) {
        if (node instanceof NoBorderNode) {
            borderSupport = null;
            current = null;
        }
        else if (node instanceof UnknownBorderNode) {
            current = ((UnknownBorderNode)node).getBorder();
        }
        else {
            borderSupport = ((BorderNode)node).getBorderSupport();
            current = borderSupport;
        }
    }

    // ---------

    private static ResourceBundle getBundle() {
        return org.openide.util.NbBundle.getBundle(BorderEditor.class);
    }

    // --------------------------
    // innerclasses
    
    final class BorderPanel extends JPanel
                            implements PropertyChangeListener,
                                       VetoableChangeListener,
                                       ExplorerManager.Provider
    {
        private ExplorerManager manager = new ExplorerManager ();
        private Node selectNode = null;        
        private BorderPanel() {
            getExplorerManager().addPropertyChangeListener(this);
            getExplorerManager().addVetoableChangeListener(this);

            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(5, 5, 5, 5));            

            ResourceBundle bundle = getBundle();

            ListView listView = new ListView();
            listView.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_AvailableBorders")); // NOI18N
            
            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("LAB_AvailableBorders")); // NOI18N
            label.setLabelFor(listView);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout(0, 2));
            panel.add(label, BorderLayout.NORTH);
            panel.add(BorderLayout.CENTER, listView);

            PropertySheetView sheetView = new PropertySheetView();
            
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split.setTopComponent(panel);
            split.setBottomComponent(sheetView);
            split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI());
            split.setBorder(BorderFactory.createEmptyBorder());
            split.setDividerLocation(170);
            split.setContinuousLayout(true);
            
            add(BorderLayout.CENTER, split);
            
            getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACSD_BorderCustomEditor")); // NOI18N
        }

        @Override
        public void addNotify() {
            super.addNotify();       
            EventQueue.invokeLater(new Runnable(){
                @Override
                public void run() {
                    try {					
                        getExplorerManager().setSelectedNodes(new Node[] { selectNode });
                    } 
                    catch (PropertyVetoException e) {} // should not happen            
                }                
            });         
        }
        
        void setValue(Object border) {
            ArrayList<Node> bordersList = new ArrayList<Node>(10);
            selectNode = null;

            PaletteItem[] items = PaletteUtils.getAllItems();
            for (int i = 0; i < items.length; i++) {
                PaletteItem paletteItem = items[i];
                if (!paletteItem.isBorder())
                    continue;

                BorderDesignSupport nodeBDS = null;
                try {
                    // PENDING ClassSource should be used (and project classpath
                    // updated like in MetaComponentCreator.prepareClass)
                    // [now not needed - until custom borders are supported]
                    nodeBDS = new BorderDesignSupport(paletteItem.getComponentClass());
                }
                catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                if (nodeBDS == null)
                    continue;

                Node borderNode;
                if (borderSupport != null
                    && borderSupport.getBorderClass() == nodeBDS.getBorderClass())
                {
                    try {
                        nodeBDS.setPropertyContext(propertyContext);
                        FormUtils.copyProperties(borderSupport.getProperties(),
                                                 nodeBDS.getProperties(),
                                                 FormUtils.CHANGED_ONLY|FormUtils.DISABLE_CHANGE_FIRING|FormUtils.DONT_CLONE_VALUES);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
                    borderNode = new BorderNode(nodeBDS, paletteItem.getNode());
                    selectNode = borderNode;
                }
                else {
                    nodeBDS.setPropertyContext(propertyContext);
                    borderNode = new BorderNode(nodeBDS, paletteItem.getNode());
                }

                bordersList.add(borderNode);
            }

            final Node root = new AbstractNode(new Children.Array());
            Node noBorder = new NoBorderNode();
            if (border == null)
                selectNode = noBorder;
            root.getChildren().add(new Node[] { noBorder });

            if (selectNode == null && border instanceof BorderDesignSupport && border == borderSupport) {
                // likely a custom border no longer in palette (loaded with form)
                try {
                    BorderDesignSupport bds = new BorderDesignSupport(borderSupport, propertyContext);
                    AbstractNode dummyPaletteNode = new AbstractNode(Children.LEAF);
                    dummyPaletteNode.setDisplayName(bds.getDisplayName());
                    dummyPaletteNode.setIconBaseWithExtension(UNKNOWN_BORDER_BASE);
                    selectNode = new BorderNode(bds, dummyPaletteNode);
                    bordersList.add(selectNode);
                } catch (Exception ex) {
                    Logger.getLogger(BorderEditor.class.getName()).log(Level.INFO, "", ex);
                }
            }

            Node[] bordersArray = new Node[bordersList.size()];
            bordersList.toArray(bordersArray);
            Arrays.sort(bordersArray, new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    return n1.getDisplayName().compareTo(
                             n2.getDisplayName());
                }
            });
            root.getChildren().add(bordersArray);

            if (selectNode == null) {
                Node unknownBorder = new UnknownBorderNode(border);
                root.getChildren().add(new Node[] { unknownBorder });
                selectNode = unknownBorder;
            }	    	    
	    
	    getExplorerManager().setRootContext(root);	    	    	                
        }
        
        // track changes in nodes selection
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes = (Node[]) evt.getNewValue();
                if (nodes.length == 1)
                    updateBorder(nodes[0]);
//                else if (nodes.length == 0) {
//                    try {
//                        getExplorerManager().setSelectedNodes(new Node[] { noBorder });
//                    } 
//                    catch (PropertyVetoException e) {
//                    }
//                }
            }
        }

        // only one border can be selected
        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] nodes =(Node[]) evt.getNewValue();
                if (nodes.length != 1)
                    throw new PropertyVetoException("", evt); // NOI18N
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(360, 440);
        }
        
        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    final class BorderNode extends FilterNode implements PropertyChangeListener {

        private BorderDesignSupport nodeBorder;
        private PropertySet[] properties;

        BorderNode(BorderDesignSupport bds, Node paletteItemNode) {
            super(paletteItemNode, Children.LEAF);
            nodeBorder = bds;
        }

        @Override
        public PropertySet[] getPropertySets () {
            if (properties == null) {
                Node.Property[] props = nodeBorder.getProperties();
                Sheet.Set propSet = Sheet.createPropertiesSet();
                propSet.put(props);

                for (int i=0; i < props.length; i++)
                    if (props[i] instanceof FormProperty)
                        ((FormProperty)props[i]).addPropertyChangeListener(this);

                properties = new PropertySet[] { propSet };
            }
            return properties;
        }

        public BorderDesignSupport getBorderSupport() {
            return nodeBorder;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // update the border
            updateBorder(this);
        }
    }

    static final class NoBorderNode extends AbstractNode {
        NoBorderNode() {
            super(Children.LEAF);
            setDisplayName(getBundle().getString("LAB_NoBorder")); // NOI18N
            setIconBaseWithExtension(NO_BORDER_BASE);
        }
    }

    static final class UnknownBorderNode extends AbstractNode {
        private Object border;

        UnknownBorderNode(Object border) {
            super(Children.LEAF);
            setBorder(border);
            setIconBaseWithExtension(UNKNOWN_BORDER_BASE);
        }

        void setBorder(Object border) {
            this.border = border;
            if (border instanceof BorderDesignSupport) {
                border = ((BorderDesignSupport)border).getBorder();
            }
            String longName = border.getClass().getName();
            int dot = longName.lastIndexOf('.');
            String shortName =(dot < 0) ? longName : longName.substring(dot + 1);
            setDisplayName(new MessageFormat(
                               getBundle().getString("LAB_FMT_UnknownBorder")) // NOI18N
                    .format(new Object[] { longName, shortName }));
        }

        Object getBorder() {
            return border;
        }
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    private static final String XML_BORDER = "Border"; // NOI18N
    private static final String ATTR_INFO = "info"; // NOI18N
    private static final String PROP_NAME = "PropertyName"; // NOI18N
    private static final String XML_PROPERTY = "Property"; // NOI18N
    private static final String XML_PROPERTY_BEAN = "PropertyBean"; // NOI18N
    private static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
    private static final String ATTR_BEAN_PROPERTY_TYPE = "type"; // NOI18N
    private static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
    private static final String ATTR_PROPERTY_RES_KEY ="resourceKey"; // NOI18N
    private static final String ATTR_PROPERTY_NORES = "noResource"; // NOI18N

    /** Called to store current property value into XML subtree.
     * @param doc The XML document to store the XML in - should be used for
     *            creating nodes only
     * @return the XML DOM element representing a subtree of XML from which
               the value should be loaded
     */
    @Override
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        Object value = getValue();
        if ((value instanceof BorderDesignSupport || value instanceof Border)
             && borderSupport != null)
        {
            org.w3c.dom.Node storedNode = null;

            // we must preserve backward compatibility of storing standard
            // swing borders (for which BorderInfo classes were used sooner)
            Class<?> borderClass = borderSupport.getBorderClass();

            if (borderClass.isAssignableFrom(TitledBorder.class))
                storedNode = storeTitledBorder(doc);
            else if (borderClass.isAssignableFrom(EtchedBorder.class))
                storedNode = storeEtchedBorder(doc);
            else if (borderClass.isAssignableFrom(LineBorder.class))
                storedNode = storeLineBorder(doc);
            else if (borderClass.isAssignableFrom(EmptyBorder.class))
                storedNode = storeEmptyBorder(doc);
            else if (borderClass.isAssignableFrom(CompoundBorder.class))
                storedNode = storeCompoundBorder(doc);
            else if (SoftBevelBorder.class.isAssignableFrom(borderClass))
                storedNode = storeBevelBorder(doc, ID_BI_SOFTBEVEL);
            else if (BevelBorder.class.isAssignableFrom(borderClass))
                storedNode = storeBevelBorder(doc, ID_BI_BEVEL);
            else if (borderClass.isAssignableFrom(MatteBorder.class))
                storedNode = storeMatteBorder(doc);           
	    
            // no other way of storing to XML ...

            return storedNode;
        }

        else if (value == null) {
            return storeNullBorder(doc);
        }

        return null; // cannot be saved
    }

    /** Called to load property value from specified XML subtree.
     * If successfully loaded, the value should be available via getValue().
     * An IOException should be thrown when the value cannot be restored from
     * the specified XML element
     * @param element the XML DOM element representing a subtree of XML from
     *                which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from
                  the specified XML element
     */
    @Override
    public void readFromXML(org.w3c.dom.Node element) throws IOException {
        if ( !XML_BORDER.equals(element.getNodeName()) )
	{
            IOException ex = new IOException("Missing \"Border\" XML element"); // NOI18N
            ErrorManager.getDefault().annotate(
                ex, getBundle().getString("MSG_ERR_MissingMainElement")); // NOI18N
            throw ex;            
        }
            
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        String infoName = attributes.getNamedItem(ATTR_INFO).getNodeValue();
        if (ID_BI_NULL_BORDER.equals(infoName))
            return; // null border

        org.w3c.dom.Node readNode = null;
        org.w3c.dom.NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
            if (children.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                readNode = children.item(i);
                break;
            }
        if (readNode == null) {
            IOException ex = new IOException("Missing border data"); // NOI18N
            ErrorManager.getDefault().annotate(
                ex, getBundle().getString("MSG_ERR_MissingBorderData")); // NOI18N
            throw ex;
        }

        if (ID_BI_TITLED.equals(infoName))
            readTitledBorder(readNode);
        else if (ID_BI_ETCHED.equals(infoName))
            readEtchedBorder(readNode);
        else if (ID_BI_LINE.equals(infoName))
            readLineBorder(readNode);
        else if (ID_BI_EMPTY.equals(infoName))
            readEmptyBorder(readNode);
        else if (ID_BI_COMPOUND.equals(infoName))
            readCompoundBorder(readNode);
        else if (ID_BI_SOFTBEVEL.equals(infoName))
            readBevelBorder(readNode, SoftBevelBorder.class);
        else if (ID_BI_BEVEL.equals(infoName))
            readBevelBorder(readNode, BevelBorder.class);
        else if (ID_BI_MATTECOLOR.equals(infoName)
                 || ID_BI_MATTEICON.equals(infoName))
            readMatteBorder(readNode);
        else if (ID_BI_NULL_BORDER.equals(infoName)) { // no border
            borderSupport = null;
        }
        // no other way of reading from XML

        if (borderSupport != null) {
            borderSupport.getProperties();
        }
        current = borderSupport;
    }
    
    // ------------------------------
    // helper storing/reading methods

    private org.w3c.dom.Element createBorderInfoNode(org.w3c.dom.Document doc,
                                                     String name) {
        org.w3c.dom.Element el = doc.createElement(XML_BORDER);
        el.setAttribute(ATTR_INFO, name);
        return el;
    }

    private static void writeProperty(String propName, FormProperty prop,
                                      org.w3c.dom.Element el,
                                      org.w3c.dom.Document doc) {
        org.w3c.dom.Node valueNode = null;

        Object value;
        Object realValue;
        try {
            value = prop.getValue();
            realValue = prop.getRealValue();
        } catch (Exception ex) { // should not happen
            Exceptions.printStackTrace(ex);
            return;
        }

        boolean noResource = false; // is it explicitly a non-resource value?
        if (value instanceof ResourceValue) { // saving as resource value
            String resourceKey = ((ResourceValue)value).getKey();
            if (resourceKey != null) {
                org.w3c.dom.Element propElement = doc.createElement(XML_PROPERTY);
                propElement.setAttribute(ATTR_PROPERTY_NAME, propName);
                propElement.setAttribute(ATTR_PROPERTY_RES_KEY, resourceKey);
                el.appendChild(propElement);
                return;
            } else {
                noResource = ResourceSupport.isResourceableProperty(prop)
                        && ResourceSupport.isExcludedProperty(prop);
            }
        }

        PropertyEditor propEd = prop.getCurrentEditor();
        if (propEd instanceof ResourceWrapperEditor) {
            propEd = ((ResourceWrapperEditor)propEd).getDelegatedPropertyEditor();
        }

        if (propEd instanceof BeanPropertyEditor) {
            propEd.setValue(value);
            BeanPropertyEditor bpe = (BeanPropertyEditor) propEd;
            if (bpe.valueIsBeanProperty()) {
                // this mimics GandalfPersistenceManager.saveBeanProperty
                org.w3c.dom.Element valueEl = doc.createElement(XML_PROPERTY_BEAN);
                valueEl.setAttribute(ATTR_BEAN_PROPERTY_TYPE, realValue.getClass().getName());
                for (Node.Property p : bpe.getProperties()) {
                    FormProperty subProp = (FormProperty) p;
                    if (subProp.isChanged()) {
                        writeProperty(subProp.getName(), subProp, valueEl, doc);
                    }
                }
                valueNode = valueEl;
            }
        }

        if (valueNode == null && propEd instanceof XMLPropertyEditor) {
            propEd.setValue(value);
            valueNode = ((XMLPropertyEditor)propEd).storeToXML(doc);
        }

        if (valueNode != null) {
            el.appendChild(valueNode);
            if (valueNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                ((org.w3c.dom.Element)valueNode).setAttribute(PROP_NAME, propName);
                if (noResource) {
                    ((org.w3c.dom.Element)valueNode).setAttribute(ATTR_PROPERTY_NORES, "true"); // NOI18N
                }
            }
        } else { // i.e. no XMLPropertyEditor so expecting a basic value (primitive, String)
            org.w3c.dom.Element propElement = doc.createElement(XML_PROPERTY);
            propElement.setAttribute(ATTR_PROPERTY_NAME, propName);
            propElement.setAttribute(ATTR_PROPERTY_VALUE, value.toString());
            el.appendChild(propElement);
        }
    }

    private Object readBorderProperty(String xmlPropName,
                                      String borderPropName,
                                      BorderDesignSupport bSupport,
                                      org.w3c.dom.Node element) throws IOException {
        String resourceKey = null;
        org.w3c.dom.Element propElement = null;

        org.w3c.dom.NodeList items = element.getChildNodes();
        for (int i = 0; i < items.getLength(); i++) {
            if (items.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                propElement = (org.w3c.dom.Element) items.item(i);
                if (propElement.getNodeName().equals(XML_PROPERTY)
                        && propElement.getAttribute(ATTR_PROPERTY_NAME).equals(xmlPropName)) {
                    resourceKey = propElement.getAttribute(ATTR_PROPERTY_RES_KEY);
                    break;
                } else if (propElement.getAttribute(PROP_NAME).equals(xmlPropName)) {
                    break;
                }
                propElement = null;
            }
        }

        if (propElement != null) { // node found
            FormProperty prop = (FormProperty) bSupport.getPropertyOfName(borderPropName);
            if (prop != null) {
                return readProperty(prop, resourceKey, propElement);
            } else {
                IOException ex = new IOException("Unknown property"); // NOI18N
                ErrorManager.getDefault().annotate(
                    ex,
                    MessageFormat.format(
                        getBundle().getString("FMT_ERR_UnknownProperty"), // NOI18N
                        new Object[] { borderPropName,
                                       bSupport.getBorderClass().getName() }));
                throw ex;
            }
        }

        return null;
    }

    private Object readProperty(FormProperty prop, String resourceKey, org.w3c.dom.Element propElement) throws IOException {
        Object value = null;
        IOException lastEx = null;
        boolean valueRead = false;

        if (resourceKey != null) { // load as resource
            value = ResourceSupport.findResource(resourceKey, prop);
            try {
                prop.setValue(value);
                valueRead = true;
            } catch (Exception ex) {
                lastEx = new IOException();
                ErrorManager.getDefault().annotate(lastEx, ex);
            }
        } else if (XML_PROPERTY_BEAN.equals(propElement.getNodeName())) {
            // bean property - i.e. having sub properties
            String valueTypeStr = propElement.getAttribute(ATTR_BEAN_PROPERTY_TYPE);
            Class valueType = null;
            BeanPropertyEditor beanPropertyEditor = null;
            try {
                valueType = FormUtils.loadClass(valueTypeStr, propertyContext.getFormModel());
                for (PropertyEditor prEd : FormPropertyEditorManager.getAllEditors(prop)) {
                    if (prEd instanceof BeanPropertyEditor) { // [assuming there's just the right one BeanPropertyEditor]
                        if (prEd instanceof FormAwareEditor && propertyContext != null) {
                            ((FormAwareEditor)prEd).setContext(propertyContext.getFormModel(), prop);
                        }
                        ((BeanPropertyEditor)prEd).intializeFromType(valueType);
                        beanPropertyEditor = (BeanPropertyEditor) prEd;
                        break;
                    }
                }
            } catch (Exception ex) {
                lastEx = new IOException(ex);
            } catch (LinkageError ex) {
                lastEx = new IOException(ex);
            }
            if (beanPropertyEditor != null) {
                org.w3c.dom.NodeList children = propElement.getChildNodes();	
                Node.Property[] subProperties = beanPropertyEditor.getProperties();
                for (int i=0; i < children.getLength(); i++) {
                    org.w3c.dom.Node subNode = children.item(i);	  
                    if (subNode != null && subNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        org.w3c.dom.Element subElement = (org.w3c.dom.Element) subNode;
                        String subPropName = subElement.getAttribute(
                                XML_PROPERTY.equals(subNode.getNodeName()) ? ATTR_PROPERTY_NAME : PROP_NAME);
                        for (Node.Property p : subProperties) {
                            if (p.getName().equals(subPropName)) {
                                if (p instanceof FormProperty) {
                                    readProperty((FormProperty)p, null, subElement);
                                }
                                break;
                            }
                        }
                    }
                }
                try {
                    PropertyEditor prEd = (PropertyEditor) beanPropertyEditor;
                    value = prEd.getValue();
                    prop.setValue(value);
                    prop.setCurrentEditor(prEd);
                    if (Boolean.parseBoolean(propElement.getAttribute(ATTR_PROPERTY_NORES))) {
                        ResourceSupport.setExcludedProperty(prop, true);
                    }
                    valueRead = true;
                } catch (Exception ex) {
                    lastEx = new IOException();
                    ErrorManager.getDefault().annotate(lastEx, ex);
                }
            }
        } else if (XML_PROPERTY.equals(propElement.getNodeName())) {
            // simple property value
            try {
                String valueStr = propElement.getAttribute(ATTR_PROPERTY_VALUE);
                value = FormUtils.decodePrimitiveValue(valueStr, prop.getValueType());
                prop.setValue(value);
                if (Boolean.parseBoolean(propElement.getAttribute(ATTR_PROPERTY_NORES))) {
                    ResourceSupport.setExcludedProperty(prop, true);
                }
                valueRead = true;
            } catch (Exception ex) {
                lastEx = new IOException();
                ErrorManager.getDefault().annotate(lastEx, ex);
            }
        } else { // load via a XMLPropertyEditor
            for (PropertyEditor prEd : FormPropertyEditorManager.getAllEditors(prop)) {
                if (!(prEd instanceof XMLPropertyEditor) && prEd instanceof ResourceWrapperEditor) {
                    prEd = ((ResourceWrapperEditor)prEd).getDelegatedPropertyEditor();
                }
                if (prEd instanceof XMLPropertyEditor) {
                    try {
                        prop.getPropertyContext().initPropertyEditor(prEd, prop);
                        ((XMLPropertyEditor)prEd).readFromXML(propElement);
                        value = prEd.getValue();
                        prop.setValue(value);
                        prop.setCurrentEditor(prEd);
                        if (Boolean.parseBoolean(propElement.getAttribute(ATTR_PROPERTY_NORES))) {
                            ResourceSupport.setExcludedProperty(prop, true);
                        }
                        valueRead = true;
                        break;
                    }
                    catch (IOException ex) {
                        lastEx = ex;
                    }
                    catch (Exception ex) {
                        lastEx = new IOException();
                        ErrorManager.getDefault().annotate(lastEx, ex);
                    }
                }
            }
        }

        if (!valueRead && lastEx != null) {
            ErrorManager.getDefault().annotate(
                lastEx,
                MessageFormat.format(
                    getBundle().getString("FMT_ERR_CannotReadBorderProperty"), // NOI18N
                    new Object[] { prop.getName() }));
            throw lastEx;
        }

        return value;
    }

    // -----------------
    // No border (null)

    private static final String ID_BI_NULL_BORDER = "null"; // NOI18N

    private org.w3c.dom.Node storeNullBorder(org.w3c.dom.Document doc) {
        try {
            return createBorderInfoNode(doc, ID_BI_NULL_BORDER);
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // TitledBorder XML persistence - compatible with former TitledBorderInfo

    private static final String XML_TITLED_BORDER = "TitledBorder"; // NOI18N
    private static final String ID_BI_TITLED // "ID" of former TitledBorderInfo
        = "org.netbeans.modules.form.compat2.border.TitledBorderInfo"; // NOI18N

    private static final String ATTR_TITLE = "title"; // NOI18N
    private static final String ATTR_TITLE_X = "titleX"; // NOI18N
    private static final String ATTR_BORDER = "innerBorder"; // NOI18N
    private static final String ATTR_JUSTIFICATION = "justification"; // NOI18N
    private static final String ATTR_POSITION = "position"; // NOI18N
    private static final String ATTR_FONT = "font"; // NOI18N
    private static final String ATTR_TITLE_COLOR = "color"; // NOI18N

    private org.w3c.dom.Node storeTitledBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_TITLED_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("border"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_BORDER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("title"); // NOI18N
            if (prop != null && prop.isChanged()) {
                Object realValue = prop.getRealValue();
                el.setAttribute(ATTR_TITLE, realValue instanceof String ?
                                            (String)realValue : ""); // NOI18N

                Object value = prop.getValue();
                if (value instanceof FormDesignValue)
                    // store also FormDesignValue (for title only)
                    writeProperty(ATTR_TITLE_X, prop, el, doc);
            }

            prop = (FormProperty)borderSupport.getPropertyOfName("titleJustification"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_JUSTIFICATION, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("titlePosition"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_POSITION, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("titleFont"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_FONT, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("titleColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_TITLE_COLOR, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_TITLED);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readTitledBorder(org.w3c.dom.Node element) throws IOException {
        if (!XML_TITLED_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_TITLED_BORDER+"\" element."); // NOI18N

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(TitledBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            readBorderProperty(ATTR_BORDER, "border", borderSupport, element); // NOI18N

            // for title, first try to read FormDesignValue
            Object title = readBorderProperty(ATTR_TITLE_X, "title", borderSupport, element); // NOI18N
            if (title == null // no design value, get simple String attribute
                  && (node = attributes.getNamedItem(ATTR_TITLE)) != null
                  && (prop = (FormProperty)borderSupport
                                          .getPropertyOfName("title")) != null) // NOI18N
                prop.setValue(node.getNodeValue());

            node = attributes.getNamedItem(ATTR_JUSTIFICATION);
            if (node != null && (prop = (FormProperty)borderSupport
                             .getPropertyOfName("titleJustification")) != null) // NOI18N
                prop.setValue(new Integer(node.getNodeValue()));

            node = attributes.getNamedItem(ATTR_POSITION);
            if (node != null && (prop = (FormProperty)borderSupport
                                  .getPropertyOfName("titlePosition")) != null) // NOI18N
                prop.setValue(new Integer(node.getNodeValue()));

            readBorderProperty(ATTR_FONT, "titleFont", borderSupport, element); // NOI18N

            readBorderProperty(ATTR_TITLE_COLOR, "titleColor", borderSupport, element); // NOI18N
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // EtchedBorder XML persistence - compatible with former EtchedBorderInfo

    private static final String XML_ETCHED_BORDER = "EtchetBorder"; // NOI18N
    private static final String ID_BI_ETCHED // "ID" of former EtchedBorderInfo
        = "org.netbeans.modules.form.compat2.border.EtchedBorderInfo"; // NOI18N

    private static final String ATTR_ETCH_TYPE = "bevelType"; // NOI18N
    private static final String ATTR_HIGHLIGHT = "highlight"; // NOI18N
    private static final String ATTR_SHADOW = "shadow"; // NOI18N

    public org.w3c.dom.Node storeEtchedBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_ETCHED_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("etchType"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_ETCH_TYPE, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_ETCHED);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readEtchedBorder(org.w3c.dom.Node element) throws IOException {
        if (!XML_ETCHED_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_ETCHED_BORDER+"\" element."); // NOI18N

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(EtchedBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            node = attributes.getNamedItem(ATTR_ETCH_TYPE);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("etchType")) != null) // NOI18N
                prop.setValue(new Integer(node.getNodeValue()));

            readBorderProperty(ATTR_HIGHLIGHT, "highlightColor", borderSupport, element); // NOI18N

            readBorderProperty(ATTR_SHADOW, "shadowColor", borderSupport, element); // NOI18N
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // LineBorder XML persistence - compatible with former LineBorderInfo

    private static final String XML_LINE_BORDER = "LineBorder"; // NOI18N
    private static final String ID_BI_LINE // "ID" of former LineBorderInfo
        = "org.netbeans.modules.form.compat2.border.LineBorderInfo"; // NOI18N

    private static final String ATTR_THICKNESS = "thickness"; // NOI18N
    private static final String ATTR_LINE_COLOR = "color"; // NOI18N
    private static final String ATTR_CORNERS = "roundedCorners"; // NOI18N

    public org.w3c.dom.Node storeLineBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_LINE_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("lineColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_LINE_COLOR, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("thickness"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_THICKNESS, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("roundedCorners"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_CORNERS, prop.getRealValue().toString());

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_LINE);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readLineBorder(org.w3c.dom.Node element) throws IOException {
        if (!XML_LINE_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_LINE_BORDER+"\" element."); // NOI18N

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(LineBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            readBorderProperty(ATTR_LINE_COLOR, "lineColor", borderSupport, element); // NOI18N

            node = attributes.getNamedItem(ATTR_THICKNESS);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("thickness")) != null) // NOI18N
                prop.setValue(new Integer(node.getNodeValue()));

            node = attributes.getNamedItem(ATTR_CORNERS);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("roundedCorners")) != null) // NOI18N
                prop.setValue(Boolean.valueOf(node.getNodeValue()));
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // EmptyBorder XML persistence - compatible with former EmptyBorderInfo

    private static final String XML_EMPTY_BORDER = "EmptyBorder"; // NOI18N
    private static final String ID_BI_EMPTY // "ID" of former EmptyBorderInfo
        = "org.netbeans.modules.form.compat2.border.EmptyBorderInfo"; // NOI18N

    private static final String ATTR_TOP = "top"; // NOI18N
    private static final String ATTR_LEFT = "left"; // NOI18N
    private static final String ATTR_RIGHT = "right"; // NOI18N
    private static final String ATTR_BOTTOM = "bottom"; // NOI18N

    public org.w3c.dom.Node storeEmptyBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_EMPTY_BORDER);
            FormProperty prop = (FormProperty)borderSupport.getPropertyOfName(
                                                               "borderInsets"); // NOI18N
            Object value;
            if (prop != null && prop.isChanged()
                  && (value = prop.getRealValue()) instanceof Insets) {
                Insets insets = (Insets)value;
                el.setAttribute(ATTR_TOP, Integer.toString(insets.top));
                el.setAttribute(ATTR_LEFT, Integer.toString(insets.left));
                el.setAttribute(ATTR_BOTTOM, Integer.toString(insets.bottom));
                el.setAttribute(ATTR_RIGHT, Integer.toString(insets.right));
            }

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_EMPTY);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readEmptyBorder(org.w3c.dom.Node element) throws IOException {
        if (!XML_EMPTY_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_EMPTY_BORDER+"\" element."); // NOI18N

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            int top=1, left=1, bottom=1, right=1;

            if ((node = attributes.getNamedItem(ATTR_TOP)) != null)
                top = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_LEFT)) != null)
                left = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_BOTTOM)) != null)
                bottom = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_RIGHT)) != null)
                right = Integer.parseInt(node.getNodeValue());
            
            borderSupport = new BorderDesignSupport(EmptyBorder.class);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            if ((top != 1 || left != 1 || bottom != 1 || right != 1)
                  && (prop = (FormProperty)borderSupport
                                   .getPropertyOfName("borderInsets")) != null) // NOI18N
                prop.setValue(new Insets(top,left,bottom,right));
        } 
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // CompoundBorder XML persistence - compatible with former CompoundBorderInfo

    private static final String XML_COMPOUND_BORDER_TYPO = "CompundBorder"; // NOI18N // Issue 73244
    private static final String XML_COMPOUND_BORDER = "CompoundBorder"; // NOI18N
    private static final String ID_BI_COMPOUND // "ID" of former CompoundBorderInfo
        = "org.netbeans.modules.form.compat2.border.CompoundBorderInfo"; // NOI18N

    private static final String ATTR_OUTSIDE = "outside"; // NOI18N
    private static final String ATTR_INSIDE = "inside"; // NOI18N

    private org.w3c.dom.Node storeCompoundBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_COMPOUND_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("outsideBorder"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_OUTSIDE, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("insideBorder"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_INSIDE, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, ID_BI_COMPOUND);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readCompoundBorder(org.w3c.dom.Node element) throws IOException {
        String nodeName = element.getNodeName();
        if (!XML_COMPOUND_BORDER.equals(nodeName) && !XML_COMPOUND_BORDER_TYPO.equals(nodeName))
            throw new IOException("Invalid format: missing \""+XML_COMPOUND_BORDER+"\" element."); // NOI18N

        try {
            borderSupport = new BorderDesignSupport(CompoundBorder.class);
            borderSupport.setPropertyContext(propertyContext);

            readBorderProperty(ATTR_OUTSIDE, "outsideBorder", borderSupport, element); // NOI18N
            readBorderProperty(ATTR_INSIDE, "insideBorder", borderSupport, element); // NOI18N
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // BevelBorder & SoftBevelBorder XML persistence - compatible with former
    // BevelAbstractBorderInfo

    private static final String XML_BEVEL_BORDER = "BevelBorder"; // NOI18N
    private static final String ID_BI_BEVEL // "ID" of former BevelBorderInfo
        = "org.netbeans.modules.form.compat2.border.BevelBorderInfo"; // NOI18N
    private static final String ID_BI_SOFTBEVEL // "ID" of former SoftBevelBorderInfo
        = "org.netbeans.modules.form.compat2.border.SoftBevelBorderInfo"; // NOI18N

    private static final String ATTR_BEVEL_TYPE = "bevelType"; // NOI18N
    private static final String ATTR_HIGHLIGHT_OUTER = "highlightOuter"; // NOI18N
    private static final String ATTR_HIGHLIGHT_INNER = "highlightInner"; // NOI18N
    private static final String ATTR_SHADOW_OUTER = "shadowOuter"; // NOI18N
    private static final String ATTR_SHADOW_INNER = "shadowInner"; // NOI18N

    public org.w3c.dom.Node storeBevelBorder(org.w3c.dom.Document doc,
                                             String infoId) {
        try {
            org.w3c.dom.Element el = doc.createElement(XML_BEVEL_BORDER);
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("bevelType"); // NOI18N
            if (prop != null && prop.isChanged())
                el.setAttribute(ATTR_BEVEL_TYPE, prop.getRealValue().toString());

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightOuterColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT_OUTER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("highlightInnerColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_HIGHLIGHT_INNER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowOuterColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW_OUTER, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("shadowInnerColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_SHADOW_INNER, prop, el, doc);

            org.w3c.dom.Node nod = createBorderInfoNode(doc, infoId);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readBevelBorder(org.w3c.dom.Node element, Class borderClass)
        throws IOException
    {
        if (!XML_BEVEL_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_BEVEL_BORDER+"\" element."); // NOI18N

        try {
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;

            borderSupport = new BorderDesignSupport(borderClass);
            borderSupport.setPropertyContext(propertyContext);
            FormProperty prop;

            node = attributes.getNamedItem(ATTR_BEVEL_TYPE);
            if (node != null && (prop = (FormProperty)borderSupport
                                       .getPropertyOfName("bevelType")) != null) // NOI18N
                prop.setValue(new Integer(node.getNodeValue()));

            readBorderProperty(ATTR_HIGHLIGHT_OUTER, "highlightOuterColor", borderSupport, element); // NOI18N
            readBorderProperty(ATTR_HIGHLIGHT_INNER, "highlightInnerColor", borderSupport, element); // NOI18N
            readBorderProperty(ATTR_SHADOW_OUTER, "shadowOuterColor", borderSupport, element); // NOI18N
            readBorderProperty(ATTR_SHADOW_INNER, "shadowInnerColor", borderSupport, element); // NOI18N
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    // ------------------------------------------------------------------------
    // MatteBorder XML persistence - compatible with former
    // MatteColorBorderInfo and MatteIconBorderInfo

    private static final String XML_MATTE_COLOR_BORDER = "MatteColorBorder"; // NOI18N
    private static final String XML_MATTE_ICON_BORDER = "MatteIconBorder"; // NOI18N
    private static final String ID_BI_MATTECOLOR // "ID" of former MatteColorBorderInfo
        = "org.netbeans.modules.form.compat2.border.MatteColorBorderInfo"; // NOI18N
    private static final String ID_BI_MATTEICON // "ID" of former MatteIconBorderInfo
        = "org.netbeans.modules.form.compat2.border.MatteIconBorderInfo"; // NOI18N

    private static final String ATTR_MATTE_COLOR = "color"; // NOI18N
    private static final String ATTR_MATTE_ICON = "icon"; // NOI18N

    public org.w3c.dom.Node storeMatteBorder(org.w3c.dom.Document doc) {
        try {
            org.w3c.dom.Element el;
            String infoId;
            FormProperty prop;

            prop = (FormProperty)borderSupport.getPropertyOfName("tileIcon"); // NOI18N
            if (prop.isChanged()) {
                el = doc.createElement(XML_MATTE_ICON_BORDER);
                infoId = ID_BI_MATTEICON;
                writeProperty(ATTR_MATTE_ICON, prop, el, doc);
            }
            else {
                el = doc.createElement(XML_MATTE_COLOR_BORDER);
                infoId = ID_BI_MATTECOLOR;
            }

            prop = (FormProperty)borderSupport.getPropertyOfName("matteColor"); // NOI18N
            if (prop != null && prop.isChanged())
                writeProperty(ATTR_MATTE_COLOR, prop, el, doc);

            prop = (FormProperty)borderSupport.getPropertyOfName("borderInsets"); // NOI18N
            Object value;
            if (prop != null && prop.isChanged()
                  && (value = prop.getRealValue()) instanceof Insets) {
                Insets insets = (Insets)value;
                el.setAttribute(ATTR_TOP, Integer.toString(insets.top));
                el.setAttribute(ATTR_LEFT, Integer.toString(insets.left));
                el.setAttribute(ATTR_BOTTOM, Integer.toString(insets.bottom));
                el.setAttribute(ATTR_RIGHT, Integer.toString(insets.right));
            }

            org.w3c.dom.Node nod = createBorderInfoNode(doc, infoId);
            nod.appendChild(el);
            return nod;
        }
        catch (Exception ex) { // should not happen
            ex.printStackTrace();
        }
        return null;
    }

    public void readMatteBorder(org.w3c.dom.Node element) throws IOException {
        if (!XML_MATTE_COLOR_BORDER.equals(element.getNodeName())
              && !XML_MATTE_ICON_BORDER.equals(element.getNodeName()))
            throw new IOException("Invalid format: missing \""+XML_MATTE_COLOR_BORDER+"\" or \""+XML_MATTE_ICON_BORDER+"\" element."); // NOI18N

        try {
            borderSupport = new BorderDesignSupport(MatteBorder.class);
            borderSupport.setPropertyContext(propertyContext);

            readBorderProperty(ATTR_MATTE_ICON, "tileIcon", borderSupport, element); // NOI18N
            readBorderProperty(ATTR_MATTE_COLOR, "matteColor", borderSupport, element); // NOI18N

            org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node node;
            FormProperty prop;

            int top=1, left=1, bottom=1, right=1;

            if ((node = attributes.getNamedItem(ATTR_TOP)) != null)
                top = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_LEFT)) != null)
                left = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_BOTTOM)) != null)
                bottom = Integer.parseInt(node.getNodeValue());
            if ((node = attributes.getNamedItem(ATTR_RIGHT)) != null)
                right = Integer.parseInt(node.getNodeValue());
            
            if ((top != 1 || left != 1 || bottom != 1 || right != 1)
                  && (prop = (FormProperty)borderSupport
                                   .getPropertyOfName("borderInsets")) != null) // NOI18N
                prop.setValue(new Insets(top,left,bottom,right));
        } 
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            IOException ioex = new IOException();
            ErrorManager.getDefault().annotate(ioex, ex);
            throw ioex;
        }
    }

    @Override
    public boolean valueIsBeanProperty() {
	return !isSupportedBorder();
    }

    private boolean isSupportedBorder() {
        Object value = getValue();
	if ((value == null) || (value instanceof javax.swing.plaf.UIResource)) {
	    // supports also null value - see storeNullBorder()
	    return true;
        }
	Class<?> borderClass = borderSupport.getBorderClass();
	return borderClass.isAssignableFrom(TitledBorder.class)
            || borderClass.isAssignableFrom(EtchedBorder.class)
            || borderClass.isAssignableFrom(LineBorder.class)
            || borderClass.isAssignableFrom(EmptyBorder.class)
            || borderClass.isAssignableFrom(CompoundBorder.class)
            || SoftBevelBorder.class.isAssignableFrom(borderClass)
            || BevelBorder.class.isAssignableFrom(borderClass)
            || borderClass.isAssignableFrom(MatteBorder.class);                
    }

    @Override
    public Node.Property[] getProperties() {
        Object value = getValue();
	if ((value == null) || (value instanceof javax.swing.plaf.UIResource)) {
	    // supports also null value - see storeNullBorder()
	    return EMPTY_PROPERTIES;
	}	
	return borderSupport.getProperties();
    }

    @Override
    public void intializeFromType(Class type) throws Exception {
	borderSupport = new BorderDesignSupport(type);
	borderSupport.setPropertyContext(propertyContext);
        borderSupport.getProperties();
	current = borderSupport;	 	
    }
    
}
