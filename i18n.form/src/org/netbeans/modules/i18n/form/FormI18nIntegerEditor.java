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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
 * Property editor for editing <code>FormI18nInteger</code> value in form editor.
 * This editor is registered during installing i18n module
 * as editor for form properties of type <code>String</code>.
 * It provides also a capability to store such object in XML form.
 * <B>Note: </B>This class should be named FormI18nIntegerEditor, but due to forward compatibility 
 * remains that name.
 *
 * @author  Petr Jiricka
 * @see FormI18nInteger
 * @see org.netbeans.modules.form.RADComponent
 * @see org.netbeans.modules.form.RADProperty
 */
public class FormI18nIntegerEditor extends PropertyEditorSupport implements FormAwareEditor,
        NamedPropertyEditor, ExPropertyEditor, XMLPropertyEditor {

    /** Value of <code>ResourceBundleString</code> this editor is currently editing. */
    private FormI18nInteger formI18nInteger;
    
    /** <code>DataObject</code> which have <code>SourceCookie</code>, and which document contains 
     * going-to-be-internatioanlized string.
     */
    private FormDataObject sourceDataObject;

    private PropertyEnv env;

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
    
    private final ResourceBundle bundle;

 
    /** Constructor. Creates new <code>ResourceBundleStringFormEditor</code>. */
    public FormI18nIntegerEditor() {
        bundle = NbBundle.getBundle(FormI18nIntegerEditor.class);
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
        FormI18nInteger value = (FormI18nInteger)getValue();
        DataObject dataObject = value.getSupport().getResourceHolder().getResource();
        
        if (dataObject == null || value.getKey() == null) {
            return bundle.getString("TXT_InvalidValue");
        } else {

            String resourceName = org.netbeans.modules.i18n.Util.
                getResourceName(value.getSupport().getSourceDataObject().getPrimaryFile(),
                                dataObject.getPrimaryFile(),
                                '/', false);// NOI18N

            return MessageFormat.format(
                bundle.getString("TXT_Key"),
                new Object[] {
                    value.getKey(),
                    resourceName, // NOI18N
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
        return ((FormI18nInteger)getValue()).getReplaceString();
    }
    
    /** Overrides superclass method.
     * @return <code>ResourceBundlePanel</code> fed with <code>FormI18nInteger</code> value. */
    @Override
    public Component getCustomEditor() {
        return new CustomEditor(new FormI18nInteger((FormI18nInteger)getValue()), getProject(), sourceDataObject.getPrimaryFile());
    }
    
    private Project getProject() {
      return org.netbeans.modules.i18n.Util.getProjectFor(sourceDataObject);
    }


    /** Overrides superclass method. 
     * @return true since we support this feature */
    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Overrides superclass method.
     * @return <code>formI18nInteger</code> */
    @Override
    public Object getValue() {
        if(formI18nInteger == null) {
            formI18nInteger = createFormI18nInteger();

            if(I18nUtil.getOptions().getLastResource2(sourceDataObject) != null)
                formI18nInteger.getSupport().getResourceHolder().setResource(I18nUtil.getOptions().getLastResource2(sourceDataObject));
        }
        
        return formI18nInteger;
    }

    /** Overrides superclass method.
     * @param value sets the new value for this editor */
    @Override
    public void setValue(Object object) {
        if(object instanceof FormI18nInteger)
            formI18nInteger = (FormI18nInteger)object;
        else {
            formI18nInteger = createFormI18nInteger();
        
            if(I18nUtil.getOptions().getLastResource2(sourceDataObject) != null)
                formI18nInteger.getSupport().getResourceHolder().setResource(I18nUtil.getOptions().getLastResource2(sourceDataObject));
        }
    }

    /** Creates <code>FormI18nInteger</code> instance. Helper method. */
    private FormI18nInteger createFormI18nInteger() {
        // Note: Only here we can have support without possible document loading.
        return new FormI18nInteger(new FormI18nSupport.Factory().createI18nSupport(sourceDataObject));
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
        return NbBundle.getMessage(FormI18nIntegerEditor.class, "PROP_IntegerEditor_name"); // NOI18N
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
        
        FormI18nInteger value = createFormI18nInteger();

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
                            if(dataObject.getClass().equals(value.getSupport().getResourceHolder().getResourceClasses()[0])) // PENDING
                                value.getSupport().getResourceHolder().setResource(dataObject);
                        } 
                        catch (IOException e) {
                        }
                    }
                }
            }

            // Set the key property.
            if(key != null && key.length() > 0) {
                value.setKey(key);
                
                // Set value and comment.
                value.setValue(value.getSupport().getResourceHolder().getValueForKey(key));
                value.setComment(value.getSupport().getResourceHolder().getCommentForKey(key));
            }

            // Try to get identifier value.
            ((JavaI18nSupport)value.getSupport()).createIdentifier();
            
            node = namedNodes.getNamedItem(ATTR_IDENTIFIER);
            if(node != null) {
                String identifier = (node == null) ? null : node.getNodeValue();
                
                if(identifier != null)
                    ((JavaI18nSupport)value.getSupport()).setIdentifier(identifier);
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
                    
                    value.setReplaceFormat(newReplaceFormat);
                }
            } else {
                // Read was not succesful (old form or error) -> set old form replace format.
                value.setReplaceFormat((String)I18nUtil.getDefaultReplaceFormat(false));
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
            value.setArguments(parameters);
        }

        // Set the value for this editor.
        setValue(value);
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
        if (formI18nInteger.getSupport().getResourceHolder().getResource() == null) {
            bundleName = "";
        } else {
            bundleName = org.netbeans.modules.i18n.Util.
                getResourceName(formI18nInteger.getSupport().getSourceDataObject().getPrimaryFile(),
                                formI18nInteger.getSupport().getResourceHolder().getResource().getPrimaryFile(),'/', true);
            if (bundleName == null) bundleName = "";
        }


        // Set bundle and key property.    
        element.setAttribute(ATTR_BUNDLE, bundleName);
        element.setAttribute(ATTR_KEY, (formI18nInteger.getKey() == null) ? "" : formI18nInteger.getKey()); // NOI18N
        // Don't save identifier, replace the identifier argument with actual value in format.
        JavaI18nSupport support = (JavaI18nSupport)formI18nInteger.getSupport();
        if(support.getIdentifier() == null)
            support.createIdentifier();
        Map map = new HashMap(1);
        map.put("identifier", support.getIdentifier()); // NOI18N
        element.setAttribute(ATTR_REPLACE_FORMAT, formI18nInteger.getReplaceFormat() == null ? "" : MapFormat.format(formI18nInteger.getReplaceFormat(), map) ); // NOI18N

        // Append subelements corresponding to parameters.
        String[] arguments = formI18nInteger.getArguments();
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
            bundle = NbBundle.getBundle(FormI18nIntegerEditor.class);
            setI18nString(i18nString);
            env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            env.addVetoableChangeListener(this);

            HelpCtx.setHelpIDString(this, I18nUtil.HELP_ID_FORMED);
        }

        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName())
                    && isVisible()) {
                String errMsgKey = null;
                I18nString i18nString = getI18nString();
                if (i18nString == null || !(i18nString instanceof FormI18nInteger)) {
                    errMsgKey = "MSG_InvalidValue";                     //NOI18N
                } else {
                    if (i18nString.getSupport().getResourceHolder().getResource()
                            == null) {
                        errMsgKey = "MSG_BundleNotSpecified";           //NOI18N
                    } else if (i18nString.getKey() == null) {
                        errMsgKey = "MSG_KeyNotSpecified";              //NOI18N
                    } else {
                        errMsgKey = checkMnemonicIndex(i18nString.getValue());
                    }
                }
                if (errMsgKey != null) {
                    // Notify user that invalid value set.
                    throw new PropertyVetoException(bundle.getString(errMsgKey),
                                                    evt);
                }
                // Try to add new key into resource bundle first.
                i18nString.getSupport().getResourceHolder().addProperty(
                    i18nString.getKey(),
                    i18nString.getValue(),
                    i18nString.getComment(),
                    false //#19137 do not destroy existing locale values
                );
                FormI18nIntegerEditor.this.setValue(i18nString);
            }
        }

        private String checkMnemonicIndex(String value) {
            if ((value == null) || (value.length() == 0)) {
                return "MSG_MnemonicIndexNotSpecified";                 //NOI18N
            }

            if (!isNonNegativeInteger(value)) {
                return "MSG_MnemonicIndexIsInvalid";                    //NOI18N
            }

            return null;
        }

    }
    
    private static boolean isNonNegativeInteger(String value) {
        if ((value == null) || (value.length() == 0)) {
            return false;
        }

        char ch = value.charAt(0);
        if ((ch < '0') || (ch > '9')) {
            return false;
        } else if (value.length() == 1) {
            return true;
        }

        for (char c : value.toCharArray()) {
            if ((c < '0') || (c > '9')) {
                return false;
            }
        }
        return true;
    }

}
