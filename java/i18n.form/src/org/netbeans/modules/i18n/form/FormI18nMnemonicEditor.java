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


package org.netbeans.modules.i18n.form;


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormAwareEditor;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.NamedPropertyEditor;
import org.netbeans.modules.i18n.I18nPanel;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.java.JavaI18nSupport;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import org.openide.util.HelpCtx;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/**
 * Property editor for editing <code>FormI18nMnemonic</code> value in form editor.
 * This editor is registered during installing i18n module
 * as editor for form properties of type <code>String</code>.
 * It provides also a capability to store such object in XML form.
 * <B>Note: </B>This class should be named FormI18nMnemonicEditor, but due to forward compatibility 
 * remains that name.
 *
 * @author  Petr Jiricka
 * @see FormI18nMnemonic
 * @see org.netbeans.modules.form.RADComponent
 * @see org.netbeans.modules.form.RADProperty
 */
public class FormI18nMnemonicEditor extends PropertyEditorSupport implements FormAwareEditor,
        NamedPropertyEditor, ExPropertyEditor, XMLPropertyEditor {

    /** Value of <code>ResourceBundleString</code> this editor is currently editing. */
    private FormI18nMnemonic formI18nMnemonic;
    
    /** <code>DataObject</code> which have <code>SourceCookie</code>, and which document contains 
     * going-to-be-internatioanlized string.
     */
    private FormDataObject sourceDataObject;

    private PropertyEnv env;

    private final ResourceBundle bundle;
    
    /** Name of resource string XML element. */
    public static final String XML_RESOURCESTRING = "ResourceString"; // NOI18N
    /** Name of argument XML element (child element of resource string element). */
    public static final String XML_ARGUMENT = "Argument"; // NOI18N
    /** Name of attribute bundle of resource string XML element. */
    public static final String ATTR_BUNDLE   = "bundle"; // NOI18N
    /** Name of attribute key of resource string XML element. */
    public static final String ATTR_KEY      = "key"; // NOI18N
    /** Name of attribute identifier of resource string XML element. */
    public static final String ATTR_IDENTIFIER = "identifier"; // NOI18N
    /** Name of attribute replace format XML element. */
    public static final String ATTR_REPLACE_FORMAT = "replaceFormat"; // NOI18N
    /** Name of attribute index of argument XML element. */
    public static final String ATTR_INDEX    = "index"; // NOI18N
    /** Name of attribute java code of argument XML element. */
    public static final String ATTR_JAVACODE = "javacode"; // NOI18N

    /** Maximal index of arguments in one argument XML element. */
    private static final int MAX_INDEX       = 1000;

    /** Constructor. Creates new <code>ResourceBundleStringFormEditor</code>. */
    public FormI18nMnemonicEditor() {
        bundle = NbBundle.getBundle(FormI18nMnemonicEditor.class);
    }

    private Project getProject() {
      return org.netbeans.modules.i18n.Util.getProjectFor(sourceDataObject);
    }

    /** Overrides superclass method.
     * @return null as we don't support this feature */
    @Override
    public String[] getTags() {
        return null;
    }

    /** Sets as text. Overrides superclass method to be dummy -> don't throw
     * <code>IllegalArgumentException</code> . */
    @Override
    public void setAsText(String text) {}
        
    
    /** Overrides superclass method. 
     * @return text for the current value */
    @Override
    public String getAsText() {
        FormI18nMnemonic mnemonic = (FormI18nMnemonic)getValue();
        DataObject dataObject = mnemonic.getSupport().getResourceHolder().getResource();
        
        if (dataObject == null || mnemonic.getKey() == null) {
            return bundle.getString("TXT_InvalidValue");
        } else {
            String resourceName = org.netbeans.modules.i18n.Util.
                getResourceName(mnemonic.getSupport().getSourceDataObject().getPrimaryFile(),
                                dataObject.getPrimaryFile(),
                                '/', false);// NOI18N
            return MessageFormat.format(
                bundle.getString("TXT_Key"),
                new Object[] {
                    mnemonic.getKey(),
                    resourceName , 
                }
            );
        }            
    }

    /** Overrides superclass method. Gets string, piece of code which will replace the hardcoded
     * non-internationalized string in the source. The default form is:
     * <p>
     * <b><identifier name>.getString("<key name>")</b>
     * or if arguments for the ResoureBundleStrin are set the form:
     * <p>
     * <b>java.text.MessageFormat.format(<identifier name>getString("<key name>"), new Object[] {<code set in Parameters and Comments panel>})</b>
     */
    @Override
    public String getJavaInitializationString() {
        return ((FormI18nMnemonic)getValue()).getReplaceString();
    }
    
    /** Overrides superclass method.
     * @return <code>ResourceBundlePanel</code> fed with <code>FormI18nMnemonic</code> value. */
    @Override
    public Component getCustomEditor() {
        return new CustomEditor(new FormI18nMnemonic((FormI18nMnemonic)getValue()), getProject(), sourceDataObject.getPrimaryFile());
    }
    
    /** Overrides superclass method. 
     * @return true since we support this feature */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Overrides superclass method.
     * @return <code>formI18nMnemonic</code> */
    @Override
    public Object getValue() {
        if(formI18nMnemonic == null) {
            formI18nMnemonic = createFormI18nMnemonic();

            if(I18nUtil.getOptions().getLastResource2(sourceDataObject) != null)
                formI18nMnemonic.getSupport().getResourceHolder().setResource(I18nUtil.getOptions().getLastResource2(sourceDataObject));
        }
        
        return formI18nMnemonic;
    }

    /** Overrides superclass method.
     * @param value sets the new value for this editor */
    @Override
    public void setValue(Object object) {
        if(object instanceof FormI18nMnemonic)
            formI18nMnemonic = (FormI18nMnemonic)object;
        else {
            formI18nMnemonic = createFormI18nMnemonic();
        
            if(I18nUtil.getOptions().getLastResource2(sourceDataObject) != null)
                formI18nMnemonic.getSupport().getResourceHolder().setResource(I18nUtil.getOptions().getLastResource2(sourceDataObject));
        }
    }

    /** Creates <code>FormI18nMnemonic</code> instance. Helper method. */
    private FormI18nMnemonic createFormI18nMnemonic() {
        // Note: Only here we can have support without possible document loading.
        return new FormI18nMnemonic(new FormI18nSupport.Factory().createI18nSupport(sourceDataObject));
    }
    
    /** 
     * Implements <code>FormAwareEditor</code> method. 
     * If a property editor implements the <code>FormAwareEditor</code>
     * interface, this method is called immediately after the PropertyEditor
     * instance is created or the custom editor is obtained from getCustomEditor().
     * @param model the <code>FormModel</code> representing meta-data of current form */
    public void setContext(FormModel model, FormProperty property) {
        sourceDataObject = FormEditor.getFormEditor(model).getFormDataObject();
    }

    // FormAwareEditor impl
    public void updateFormVersionLevel() {
    }

    /**
     * Implements <code>NamePropertyEditor</code> interface method.
     * @return Display name of the property editor 
     */
    public String getDisplayName () {
        return bundle.getString("PROP_MenmonicEditor_name");
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

    /** 
     * Implements <code>XMLPropertyEditor</code> interface method.
     * Called to load property value from specified XML subtree. If succesfully loaded,
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     * @see org.w3c.dom.Node
     */
    public void readFromXML(Node domNode) throws IOException {
        if(!XML_RESOURCESTRING.equals (domNode.getNodeName ())) {
            throw new IOException ();
        }
        
        FormI18nMnemonic mnemonic = createFormI18nMnemonic();

        NamedNodeMap namedNodes = domNode.getAttributes ();
        
        try {
            Node node;
            // Retrieve bundle name.
            node = namedNodes.getNamedItem(ATTR_BUNDLE);
            String bundleName = (node == null) ? null : node.getNodeValue();

            // Retrieve key name.
            node = namedNodes.getNamedItem(ATTR_KEY);
            String key = (node == null) ? null : node.getNodeValue();

            // Set the resource bundle property.
            if(bundleName != null) {
                FileObject sourceFo = sourceDataObject.getPrimaryFile();
                if ( sourceFo != null ) {
                    FileObject fileObject = org.netbeans.modules.i18n.Util.
                        getResource(sourceFo, bundleName);

                    if(fileObject != null) {
                        try {
                            DataObject dataObject = DataObject.find(fileObject);
                            if(dataObject.getClass().equals(mnemonic.getSupport().getResourceHolder().getResourceClasses()[0])) // PENDING
                                mnemonic.getSupport().getResourceHolder().setResource(dataObject);
                        } 
                        catch (IOException e) {
                        }
                    }
                }
                 
            }

            // Set the key property.
            if(key != null && key.length() > 0) {
                mnemonic.setKey(key);
                
                // Set value and comment.
                mnemonic.setValue(mnemonic.getSupport().getResourceHolder().getValueForKey(key));
                mnemonic.setComment(mnemonic.getSupport().getResourceHolder().getCommentForKey(key));
            }

            // Try to get identifier value.
            ((JavaI18nSupport)mnemonic.getSupport()).createIdentifier();
            
            node = namedNodes.getNamedItem(ATTR_IDENTIFIER);
            if(node != null) {
                String identifier = (node == null) ? null : node.getNodeValue();
                
                if(identifier != null)
                    ((JavaI18nSupport)mnemonic.getSupport()).setIdentifier(identifier);
            }
            
            // Try to get init format string value.
            node = namedNodes.getNamedItem(ATTR_REPLACE_FORMAT);
            if(node != null) {
                String replaceFormat = node.getNodeValue();
                
                if(replaceFormat != null && replaceFormat.length() > 0) {
                    
                    // Note: This part of code is only due to use in some development builds of MessageFormat 
                    // instead of MapFormat. If somebidy used those builds the replace code is in MessageFormat 
                    // so we will convert it to MapFormat.
                    // This could be later extracted.
                    // Note if the replace form at was in the MessageFormat convert to MapFormat
                    // Don't throw away.
                    Map map = new HashMap(6);
                    map.put("0", "{identifier}"); // NOI18N
                    map.put("1", "{key}"); // NOI18N
                    map.put("2", "{bundleNameSlashes}"); // NOI18N
                    map.put("3", "{bundleNameDots}"); // NOI18N
                    map.put("4", "{sourceFileName}"); // NOI18N
                    map.put("fileName", "{sourceFileName}"); // NOI18N
                    
                    String newReplaceFormat = MapFormat.format(replaceFormat, map);
                    
                    mnemonic.setReplaceFormat(newReplaceFormat);
                }
            } else {
                // Read was not succesful (old form or error) -> set old form replace format.
                mnemonic.setReplaceFormat((String)I18nUtil.getDefaultReplaceFormat(false));
            }
        } catch(NullPointerException npe) {
            throw new IOException ();
        }

        // Retrieve the arguments.
        if(domNode instanceof Element) {
            Element domElement = (Element)domNode;
            NodeList argNodeList = domElement.getElementsByTagName(XML_ARGUMENT);

            // Find out the highest index.
            int highest = -1;
            for(int i = 0; i < argNodeList.getLength(); i++) {
                NamedNodeMap attributes = argNodeList.item(i).getAttributes();
                
                Node indexNode = attributes.getNamedItem (ATTR_INDEX);
                String indexString = (indexNode == null) ? null : indexNode.getNodeValue ();

                if(indexString != null) {
                    try {
                        int index = Integer.parseInt(indexString);
                        if (index > highest && index < MAX_INDEX)
                            highest = index;
                    } catch (Exception e) {}
                }
            }

            // Construct the array.
            String[] parameters = new String[highest + 1];

            // Fill the array.
            for (int i = 0; i < argNodeList.getLength(); i++) {
                NamedNodeMap attr = argNodeList.item(i).getAttributes ();
                
                Node indexNode = attr.getNamedItem (ATTR_INDEX);
                String indexString = (indexNode == null) ? null : indexNode.getNodeValue ();
                if (indexString != null) {
                    try {
                        int index = Integer.parseInt(indexString);
                        if (index < MAX_INDEX) {
                            Node javaCodeNode = attr.getNamedItem (ATTR_JAVACODE);
                            String javaCode = (javaCodeNode == null) ? null : javaCodeNode.getNodeValue ();
                            parameters[index] = javaCode;
                        }
                    } catch (Exception e) {}
                }
            }

            // Fill all the values in case some are missing - make it really foolproof.
            for (int i = 0; i < parameters.length; i++)
                if (parameters[i] == null)
                    parameters[i] = ""; // NOI18N

            // Set the parameters.
            mnemonic.setArguments(parameters);
        }

        // Set the value for this editor.
        setValue(mnemonic);
    }

    /**
     * Implements <code>XMLPropertyEditor</code> interface method.
     * Called to store current property value into XML subtree. The property value should be set using the
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    public Node storeToXML(Document doc) {
        Element element = doc.createElement (XML_RESOURCESTRING);

        String bundleName;
        if (formI18nMnemonic.getSupport().getResourceHolder().getResource() == null) {
            bundleName = "";
        } else {
            bundleName = org.netbeans.modules.i18n.Util.
                getResourceName(formI18nMnemonic.getSupport().getSourceDataObject().getPrimaryFile(),
                                formI18nMnemonic.getSupport().getResourceHolder().getResource().getPrimaryFile(),'/', true);
            if (bundleName == null) bundleName = "";
        }

            
        // Set bundle and key property.    
        element.setAttribute(ATTR_BUNDLE, bundleName);
        element.setAttribute(ATTR_KEY, (formI18nMnemonic.getKey() == null) ? "" : formI18nMnemonic.getKey()); // NOI18N
        // Don't save identifier, replace the identifier argument with actual value in format.
        JavaI18nSupport support = (JavaI18nSupport)formI18nMnemonic.getSupport();
        if(support.getIdentifier() == null)
            support.createIdentifier();
        Map map = new HashMap(1);
        map.put("identifier", support.getIdentifier()); // NOI18N
        element.setAttribute(ATTR_REPLACE_FORMAT, formI18nMnemonic.getReplaceFormat() == null ? "" : MapFormat.format(formI18nMnemonic.getReplaceFormat(), map) ); // NOI18N

        // Append subelements corresponding to parameters.
        String[] arguments = formI18nMnemonic.getArguments();
        for (int i = 0; i < arguments.length; i++) {
            Element childElement = doc.createElement (XML_ARGUMENT);
            childElement.setAttribute (ATTR_INDEX, "" + i); // NOI18N
            childElement.setAttribute (ATTR_JAVACODE, arguments[i]);
            try {
                element.appendChild(childElement);
            } catch (DOMException de) {}
        }

        return element;
    }

    /** Custom editor for this property editor. */
    private class CustomEditor extends I18nPanel implements VetoableChangeListener {

        private final ResourceBundle bundle;
        
        /** Constructor. */
        public CustomEditor(I18nString i18nString, Project project, FileObject file) {
            super(i18nString.getSupport().getPropertyPanel(), false, project, file);
            setI18nString(i18nString);
            bundle = NbBundle.getBundle(FormI18nMnemonicEditor.class);
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(this);

            HelpCtx.setHelpIDString(this, I18nUtil.HELP_ID_FORMED);
        }

        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                    && isVisible()) {
                I18nString i18nString = getI18nString();
                if (i18nString == null 
                    || !(i18nString instanceof FormI18nMnemonic)
                    || i18nString.getSupport().getResourceHolder().getResource() == null 
                    || i18nString.getKey() == null) {
                    // Notify user that invalid value set.
                    throw new PropertyVetoException(bundle.getString("MSG_InvalidValue"), evt); // NOI18N
                }
                // Try to add new key into resource bundle first.
                i18nString.getSupport().getResourceHolder().addProperty(
                    i18nString.getKey(),
                    i18nString.getValue(),
                    i18nString.getComment(),
                    false //#19137 do not destroy existing locale values
                );
                FormI18nMnemonicEditor.this.setValue(i18nString);
            }
        }
    }

    
}
