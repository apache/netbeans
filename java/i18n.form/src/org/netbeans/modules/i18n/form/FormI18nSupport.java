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


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;

import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADConnectionPropertyEditor.RADConnectionDesignValue;
import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.InfoPanel;
import org.netbeans.modules.i18n.java.JavaI18nFinder;
import org.netbeans.modules.i18n.java.JavaI18nString;
import org.netbeans.modules.i18n.java.JavaI18nSupport;

import org.netbeans.modules.nbform.FormEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;


/** 
 * Support for internationalizing strings in java sources with forms.
 *
 * @author Peter Zavadsky
 */
public class FormI18nSupport extends JavaI18nSupport {

    //see RADComponent and  RADVisualComponent
    private static final String RAD_PROPERTIES = "properties"; // NOI18N
    private static final String RAD_PROPERTIES2 = "properties2"; // NOI18N
    private static final String RAD_ACCESSIBILITY = "accessibility"; // NOI18N
    private static final String RAD_LAYOUT = "layout"; // NOI18N

// PENDING - Has not been used because of implementation of method
//           isGuardedPosition(int)  (now replaced with isInGuardedSection(...))
//           The replacement for the isGuardedPosition method was made
//           during bugfixing phase so the documentListener remained disabled
//           to keep the amount of changes small (and thus minimize the risk
//           of introducing new bugs.
    
//    /** Listener which listens on changes of form document. */
//    DocumentListener documentListener;
    
    /** Constructor. */
    private FormI18nSupport(DataObject sourceDataObject) {
        super(sourceDataObject);
    }

    
    /** Creates <code>I18nFinder</code>. */
    @Override
    protected I18nFinder createFinder() {
        return new FormI18nFinder(sourceDataObject, document);
    }

    /** Creates <code>I18nReplacer</code>. */
    @Override
    protected I18nReplacer createReplacer() {        
        return new FormI18nReplacer(/*sourceDataObject, document,*/ (FormI18nFinder)getFinder());
        
// PENDING - Has not been used because of implementation of method
//           isGuardedPosition(int)  (now replaced with isInGuardedSection(...))
//           See the first PENDING note.
        
//        documentListener = new DocumentListener() {
//            public void changedUpdate(DocumentEvent e) {
//            }

//            public void insertUpdate(DocumentEvent e) {
//                updateFormProperties();
//            }

//            public void removeUpdate(DocumentEvent e) {
//            }
//        };
        
//        document.addDocumentListener(documentListener);
    }
    
    /** Gets info panel about found hard string. */
    @Override
    public JPanel getInfo(HardCodedString hcString) {
        return new FormInfoPanel(hcString, document);
    }
    
    /** Helper method. */
    static String toAscii(String str) {
        if (str == null) {
            return null;
        }
        // Note: All this code is copied from org.netbeans.core.editors.StringEditor.
        StringBuffer buf = new StringBuffer(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
            case '\"': buf.append("\\\""); break; // NOI18N
                //        case '\'': buf.append("\\'"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && c <= 0x007f) {
                    buf.append(c);
                } else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(c);
                    for (int j = 0; j < 4 - hex.length(); j++) {
                        buf.append('0');
                    }
                    buf.append(hex);
                }
            }
        }
        return buf.toString();
    }

    /** Inner class for holding info about form proeprties which can include hardcoded string.
     * see formProperties variable in enclosing class. */
    private static class ValidFormProperty {
        /** Holds property of form. */
        private Node.Property property;
        
        /** Holds rad component name. */
        private String radCompName;
        
        /** How many occurences of found string should be skipped in this property.
         * 0 means find the first occurence.
         * All this just means that the property (mostly 'code generation-> pre-init, post-init etc.' properties)
         * could contain more than one occurence of found string
         * and in that case is very important to match and replace the same found in document. */
        private int skip;
   

        /** Constructor. */
        public ValidFormProperty(String radCompName, Node.Property property) {
            this.radCompName = radCompName;
            this.property    = property;
            this.skip        = 0;
        }

        /** Constructor. */
        public ValidFormProperty(ValidFormProperty validProperty) {
            radCompName = validProperty.getRADComponentName();
            property = validProperty.getProperty();
            skip = validProperty.getSkip();
        }
        
        
        /** Getter for <code>radCompName</code> property. */
        public String getRADComponentName() {
            return radCompName;
        }

        /** Getter for property.
         * @return property can contain hard-coded string */
        public Node.Property getProperty() {
            return property;
        }
        
        /** Getter for skip.
         * @return amount of occurences of hard-coded string to skip */
        public int getSkip() {
            return skip;
        }
        
        /** Increment the amount of occurences to skip. */
        public void incrementSkip() {
            skip++;
        }

        /** Decrement skip amount of occurences to skip. */
        public void decrementSkip() {
            if (skip > 0) {
                skip--;
            }
        }
        
    } // end of ValidFormProperty inner class

    /**
     * Iterator over form properties of a component.
     * Form properties are defined as properties from property sets named
     * &quot;properties&quot;, &quot;properties2&quot;,
     * &quot;accessibility&quot; and &quot;layout&quot;.
     * 
     * @author  Marian Petras
     */
    static final class FormPropertiesIterator implements Iterator<Node.Property> {

        private final RADComponent comp;

        private boolean ready = false;
        private Node.PropertySet[] propSets;
        private int propSetIndex = -1;
        private Node.Property[] properties;
        private int propIndex = -1;
        private boolean first = true;
        private boolean exhausted = false;

        FormPropertiesIterator(RADComponent comp) {
            this.comp = comp;
        }
        
        public boolean hasNext() {
            if (!ready) {
                if (first || (++propIndex == properties.length)) {
                    if (first) {
                        propSets = comp.getProperties();
                        first = false;
                    }
                    while (++propSetIndex != propSets.length) {
                        Node.PropertySet propSet;
                        if (isFormPropSet(propSet = propSets[propSetIndex])
                                && ((properties = propSet.getProperties()).length != 0)) {
                            propIndex = 0;
                            break;
                        }
                    }
                    if (propSetIndex == propSets.length) {
                        exhausted = true;
                    }
                }
                ready = true;
            }
            return !exhausted;
        }

        private static boolean isFormPropSet(PropertySet propertySet) {
            String propSetName = propertySet.getName();
            return RAD_PROPERTIES.equals(propSetName)
                   || RAD_PROPERTIES2.equals(propSetName)
                   || RAD_LAYOUT.equals(propSetName)
                   || RAD_ACCESSIBILITY.equals(propSetName);
        }

        public Node.Property next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            ready = false;
            return properties[propIndex];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
    
    /** Helper inner class for formProperties variable in enclosing class.
     * Provides sorting of ValidPropertyComparator classes with intenetion to get the order of
     * properties to match order like they are generated to initComponents form guarded block.
     * It has four stages of comparing two properies.
     * 1) the property which belongs to creation block (preCreationCode, customCreationCode, postCreationCode)
     *   is less (will be generated sooner) then property which is from init block(other names).
     * 2) than the property which component was added to form sooner is less then property which component was
     *   added later. (Top-level component is the least one.)
     * 3) than a) creation block: preCreationCode < (is less) customCreationCode < postCreationCode
     *         b) init block: preInitCode < set-method-properties < postInitCode
     * 4) than (for init block only) in case of set-method-properties. The property is less which has less index in
     *   array returned by method getAllProperties on component.
     * */
    private static class ValidFormPropertyComparator implements Comparator<ValidFormProperty> {
        
        private static final String CREATION_CODE_PRE    = "creationCodePre"; // NOI18N
        private static final String CREATION_CODE_CUSTOM = "creationCodeCustom"; // NOI18N
        private static final String CREATION_CODE_POST   = "creationCodePost"; // NOI18N
        
        private static final String INIT_CODE_PRE  = "initCodePre"; // NOI18N
        private static final String INIT_CODE_POST = "initCodePost"; // NOI18N

        /** <code>FormModel</code> on which <code>FormDataObject</code> the i18n session runs.*/
        private final FormModel formModel;
        
        
        /** Constructor. */
        public ValidFormPropertyComparator(FormDataObject formDataObject) {
            FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
            formModel = fes.getFormModel();
        }
        
        
        /** Compares two <code>ValidFormPropertiesObjects</code>. */
        public int compare(ValidFormProperty p1, ValidFormProperty p2) {
            Node.Property prop1 = p1.getProperty();
            Node.Property prop2 = p2.getProperty();

            // 1st stage
            String propName1 = prop1.getName();
            String propName2 = prop2.getName();
            
            boolean isInCreation1 = false;
            boolean isInCreation2 = false;
            
            if (propName1.equals(CREATION_CODE_PRE)
                    || propName1.equals(CREATION_CODE_CUSTOM)
                    || propName1.equals(CREATION_CODE_POST)) {
                isInCreation1 = true;
            }
            
            if (propName2.equals(CREATION_CODE_PRE)
                    || propName2.equals(CREATION_CODE_CUSTOM)
                    || propName2.equals(CREATION_CODE_POST)) {
                isInCreation2 = true;
            }
            
            if(isInCreation1 != isInCreation2) {
                return isInCreation1 ? -1 : 1; // end of 1st stage
            } // end of 1st stage
                
            // 2nd stage
            RADComponent comp1 = formModel.findRADComponent(p1.getRADComponentName());
            RADComponent comp2 = formModel.findRADComponent(p2.getRADComponentName());
            
            int index1 = -1;
            int index2 = -1;
            
            if (!comp1.equals(comp2)) {
                for (RADComponent comp : formModel.getOrderedComponentList()) {
                    if (comp == comp1) {
                        return -1;
                    }
                    if (comp == comp2) {
                        return 1;
                    }
                }
                assert false;
                return 0;
            } // end of 2nd stage
            
            // 3rd stage
            if (isInCreation1) {
                // 3a) stage
                index1 = -1;
                index2 = -1;
                
                if (propName1.equals(CREATION_CODE_PRE)) {
                    index1 = 0;
                } else if (propName1.equals(CREATION_CODE_CUSTOM)) {
                    index1 = 1;
                } else if (propName1.equals(CREATION_CODE_POST)) {
                    index1 = 2;
                }
                
                if (propName2.equals(CREATION_CODE_PRE)) {
                    index2 = 0;
                } else if (propName2.equals(CREATION_CODE_CUSTOM)) {
                    index2 = 1;
                } else if (propName2.equals(CREATION_CODE_POST)) {
                    index2 = 2;
                }
                
                return index1 - index2; // end of 3a) stage
            } else {
                // 3b) stage
                index1 = -1;
                index2 = -1;
                
                if (propName1.equals(INIT_CODE_PRE)) {
                    index1 = 0;
                } else if (propName1.equals(INIT_CODE_POST)) {
                    index1 = 2;
                } else {
                    index1 = 1; // is one of set-method property
                }
                
                if (propName2.equals(INIT_CODE_PRE)) {
                    index2 = 0;
                } else if (propName2.equals(INIT_CODE_POST)) {
                    index2 = 2;
                } else {
                    index2 = 1; // is one of set-method property
                }
                
                if ((index1 != 1) || (index2 != 1)) {
                    return index1 - index2; // end of 3b) stage
                }
            } // end of 3rd stage
            
            // 4th stage
            index1 = -1;
            index2 = -1;
            Iterator<Node.Property> it = new FormPropertiesIterator(comp1);
            for (int propIndex = 0; it.hasNext(); propIndex++) {
                Node.Property property = it.next();
                if (prop1.equals(property)) {
                    index1 = propIndex;
                }
                if (prop2.equals(property)) {
                    index2 = propIndex;
                }
                if ((index1 != -1) && (index2 != -1)) {
                    break;
                }
            }
            return index1 - index2; // end of 4th stage

        } // end of compare method
   
    } // End of ValidFormPropertyCompoarator inner class.

    
    /** Nested class. Finder in java source with form. */
    private static class FormI18nFinder extends JavaI18nFinder {
        /** */
        private DataObject sourceDataObject;
        
        /** Holds name of component which property has hardcoded string. */
        private String componentName = ""; // NOI18N

        /** Holds name of property with found hardcoded string */
        private String propertyName = ""; // NOI18N

        /** Collection for holding properties of form and their components, if search is in form performed. */
        private TreeSet<ValidFormProperty> formProperties;

        /** Found valid form property from last search. */
        private ValidFormProperty lastFoundProp;

        
        /** Constructor. */
        public FormI18nFinder(DataObject sourceDataObject, StyledDocument document) {
            super(document);
            this.sourceDataObject = sourceDataObject;
            
            init();
        }
        
        
        /** Initializes finder. */
        private void init() {
            clearFormInfoValues();
            
            lastFoundProp = null;
            
            createFormProperties();
        }

        /** Resets finder. */
        @Override
        protected void reset() {
            super.reset();
            
            init();
        }
        
        /** Decrements skip value of last found property. Called from replacer. */
        void decrementLastFoundSkip() {
            if (lastFoundProp != null) {
                lastFoundProp.decrementSkip();
            }
        }

        /** Cretaes collection of properties of form which are value type of String.class
         * and could have null value (it saves time to determine the cases the value was changed).
         * Collection is referenced to formProperties variable.
         * @return True if sorted collection was created. */
        private synchronized boolean createFormProperties() {
            // creates new collection
            formProperties = new TreeSet<ValidFormProperty>(
                    new ValidFormPropertyComparator((FormDataObject) sourceDataObject));
            updateFormProperties();

            return true;
        }

        /** Updates collection in formProperties variable. */
        private synchronized void updateFormProperties() {
            if (formProperties == null) {
                return;
            }

            // All components in current FormDataObject.
            FormDataObject formDataObject = (FormDataObject) sourceDataObject;
            FormEditorSupport fes = (FormEditorSupport)formDataObject.getFormEditorSupport();
            Collection<RADComponent> c = fes.getFormModel().getAllComponents();

            // search thru all RADComponents in the form
            for (RADComponent radComponent : c) {

                Iterator<Node.Property> it = new FormPropertiesIterator(radComponent);
                while (it.hasNext()) {
                    Node.Property property = it.next();

                    // skip hidden and unchanged properties
                    if (property.isHidden()
                            || !(property instanceof FormProperty)
                            || !((FormProperty)property).isChanged()) {
                        continue;
                    }

                    // get value
                    Object value;
                    try {
                        value = property.getValue();
                    } catch(IllegalAccessException iae) {
                        continue; // next property
                    } catch(InvocationTargetException ite) {
                        continue; // next property
                    }

                    // Property have to be a non-null value and have "value type" of String (don't confuse with the type of object referred by value variable!!)
                    // or value be RADconnectiondesignValue and be type of TYPE_VALUE or TYPE_CODE
                    if(value != null && (property.getValueType().equals(String.class)
                        || (value instanceof RADConnectionDesignValue
                            &&  ( ((RADConnectionDesignValue)value).getType() == RADConnectionDesignValue.TYPE_VALUE
                                || ((RADConnectionDesignValue)value).getType() == RADConnectionDesignValue.TYPE_CODE) 
                            ) )
                        ) {
                        // Actually add the property to the list.
                        // Note: add only ValidFormProperty instances
                        formProperties.add(new ValidFormProperty(radComponent.getName(), property));
                    }
                }
            }
        }
        
        @Override
        protected HardCodedString findNextString() {
            //boolean found;
            HardCodedString hcString;

            clearFormInfoValues();
            
            boolean guarded = false;

            do {
                hcString = super.findNextString();

                // If i18n search we are not interesting in form values.
                if (i18nSearch) {
                    return hcString;
                }
                
                if (hcString != null) {
                    guarded = isInGuardedSection(hcString.getStartPosition(),
                                                 hcString.getEndPosition());
                } else {
                    // No more hardcoded strings in source.
                    break;
                }

                if (guarded) {
                    hcString = findInForm(hcString);

                    // Skip found hardcoded string if it is in a guarded block and not found appropriate form component property.
                }

            // Skip found hardcoded string if it is in a guarded block and not found appropriate form component property.
            } while(guarded && (hcString == null));

            // PENDING
            // See first PENDING.
            //        if(!found)
            //            document.removeDocumentListener(documentListener);

            return hcString;
        }

        /** Helper method. */
        private void clearFormInfoValues() {
            componentName = ""; // NOI18N
            propertyName  = ""; // NOI18N
        }
        
        
        /** Analyzes the text in a guraded block, tries to find the name
         *  of the component and of the property which value matches
         *  with just found hardcoded string.
         */
        private synchronized HardCodedString findInForm(HardCodedString hcString) {
            boolean found = false;

            String hardString = hcString.getText();
            
            // Valid form property.
            ValidFormProperty validProp = null;

            // Node property.
            Node.Property nodeProperty = null;

            Iterator<ValidFormProperty> it;

            if (lastFoundProp != null) {
                validProp = lastFoundProp;
                it = formProperties.tailSet(lastFoundProp).iterator();
                /*
                 * In the first cycle of the following do-while loop, value
                 * of 'lastFoundProp' will be used, not the one from
                 * the iterator. So make sure the property following
                 * 'lastFoundProp' is used in the next cycle:
                 */
                it.next();
            } else {
                it = formProperties.iterator();
            }

            do {
                if (validProp == null && it.hasNext()) {
                    validProp = it.next();
                }
                if (validProp == null) {
                    break;
                }
                Node.Property property = validProp.getProperty();
                String radCompName = validProp.getRADComponentName();
                
                // get value
                Object value;
                try {
                    value = property.getValue();
                } catch(IllegalAccessException iae) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
                    validProp = null;
                    continue; // next property
                } catch(InvocationTargetException ite) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ite);
                    validProp = null;
                    continue; // next property
                }

                // Property have to be a non-null value and have "value type" of String (don't confuse with the type of object referred by value variable!!)
                // or value be RADconnectiondesignValue and be type of TYPE_VALUE or TYPE_CODE
                if(value != null && (property.getValueType().equals(String.class)
                    || (value instanceof RADConnectionDesignValue
                        &&  ( ((RADConnectionDesignValue)value).getType() == RADConnectionDesignValue.TYPE_VALUE
                            || ((RADConnectionDesignValue)value).getType() == RADConnectionDesignValue.TYPE_CODE) 
                        ) )
                    ) {
                        
                    String string;
                    
                    if(property instanceof FormProperty) {
                        // RADProperty, the value could be constructed from one of PropertyEditors
                        if(value instanceof FormI18nString) {
                            // resource bundle value, do not replace, is internationalized already !!
                            validProp = null;
                            continue; // next property
                        } else if (value instanceof RADConnectionDesignValue) {
                            // is Form connection value
                            string = ""; // NOI18N
                            RADConnectionDesignValue connectionValue = (RADConnectionDesignValue)value;
                            if(connectionValue.getType() == RADConnectionDesignValue.TYPE_VALUE) {
                                // is type of VALUE
                                string = connectionValue.getValue();

                                if (indexOfNonI18nString(string, hardString, validProp.getSkip()) != -1) {
                                    found = true;
                                }
                            } else if (connectionValue.getType() == RADConnectionDesignValue.TYPE_CODE) {
                                // is type of TYPE_CODE
                                string = connectionValue.getCode();

                                if (indexOfNonI18nString(string, hardString, validProp.getSkip()) != -1) {
                                    found = true;
                                }
                            }
                        } else if(value instanceof String) { // #179872
                            // Has to be plain String, there is other Property Editor for String RAD Property.
                            // It's converted via toAscii method cause for this value is used org.netbeans.core.editors.StringEditor 
                            // which does the same thing.
                            string = toAscii((String)value);
                            
                            if ((validProp.getSkip() == 0) && string.equals(toAscii(hardString))) {
                                found = true;
                            }
                        } else {
                            // TODO: now, do nothing, but see #179872 :
                            // org.netbeans.modules.swingapp.ResourceValueImpl
                        }
                    } else {
                        // Node.Property, the value should be plain String.
                        string = (String)value;
                        
                        if (indexOfNonI18nString(string, hardString, validProp.getSkip()) != -1) {
                            // non-internationalized hardString found.
                            found = true;
                        }
                    }
                }
                if (found) {
                    nodeProperty = property;
                    componentName = radCompName;
                    propertyName = property.getName();

                    break;
                } else {
                    validProp = null;
                }
            } while(it.hasNext());

            if(found) {
                lastFoundProp = new ValidFormProperty(validProp);
                lastFoundProp.incrementSkip();

                return new FormHardCodedString(
                    hcString.getText(),
                    hcString.getStartPosition(),
                    hcString.getEndPosition(),
                    validProp,
                    nodeProperty
                );
            } else {
                return null;
            }
        }
        
        
        /** Getter for <code>lastPosition</code> property. */
        public Position getLastPosition() {
            return lastPosition;
        }
        
        /** Setter for <code>lastPosition</code> property. */
        public void setLastPosition(Position lastPosition) {
            super.lastPosition = lastPosition;
        }
        
        /** Helper method. */
        public int indexOfNonI18nString(String source, String hardString, int skip) {
            // Index of found string in code described by source string.
            int index = 0;

            // Start index for each iteration of loop.
            int startIndex=0;

            while (true) {
                // Find out if there could be some string yet.
                int startString = source.indexOf('\"', startIndex);

                if (startString == -1) {
                    break;
                }

                // Get the string.
                int endString = source.indexOf('\"', startString+1);

                int endLineIndex = source.indexOf('\n', startString+1);

                // Check for validity of that string. (It has to be in one row).
                if(endLineIndex == -1 || endString < endLineIndex) {
                    // Get valid string.
                    String foundString = source.substring(startString, endString+1);

                    // Construct part line which will be compared in with regular expression.
                    String partLine;
                    
                    // Adjust start index so the part line starts at the same line like found string.
                    int startLine = source.indexOf('\n', startIndex + 1);
                    if (startLine != -1 && startLine < startString) {
                        startIndex = startLine + 1;
                    }

                    if (endLineIndex == -1) {
                        partLine = source.substring(startIndex);
                    } else {
                        int quote = source.indexOf('\"', endString+1);

                        // If there is another string on that line cut part line before that.
                        if (quote != -1 && quote < endLineIndex) {
                            // The second part is little trick to cheat out regular expression in minor case the next string is same and i18n-ized already.
                            partLine = source.substring(startIndex, quote) + source.substring(quote, endLineIndex).replace('\"', '_');
                        } else {
                            partLine = source.substring(startIndex, endLineIndex);
                        }
                    }

                    // Compare with regular expression.
                    if(isSearchedString(partLine, foundString)) {
                        // Is non-i18n-ized string and has the has the order number in source string code we search for.
                        if (index == skip) {
                            if (foundString.equals("\""+hardString+"\"")) {
                                // It is our hard string.
                                return startString;
                            } else {
                                // Is not our hardString.
                                return -1;
                            }
                        }

                        index++;
                    }

                    // Set start index for next iteration.
                    startIndex = endString + 1;
                } else {
                    startIndex = endLineIndex + 1;
                }
            } // End of infinite loop.

            return -1;
        }

        /**
         * Checks whether the given section of text overlaps with any of the
         * guarded sections in the editor.
         *
         * @param  startPos  beginning position if the section to check
         * @param  endPos    ending position of the section to check
         * @return  <code>true</code> if the section of text overlaps,
         *          <code>false</code> otherwise
         */
        private synchronized boolean isInGuardedSection(final Position startPos,
                                                        final Position endPos) {
            EditorCookie editor = sourceDataObject.getCookie(EditorCookie.class);
            StyledDocument doc = null;
            GuardedSectionManager guards = null;
            if (editor != null) {
                try {
                    doc = editor.openDocument();
                }
                catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    return false;
                }
            }
            
            if (doc != null) {
                guards = GuardedSectionManager.getInstance(doc);
            }
            
            if (guards != null) {
                for (Iterator it = guards.getGuardedSections().iterator(); it.hasNext();) {
                    GuardedSection gsection = (GuardedSection) it.next();
                    if (gsection.contains(startPos, true) ||
                            gsection.contains(endPos, true)) {
                        return true;
                    }
                }

            }
            return false;
        }

        DataObject getSourceDataObject() {
	  return sourceDataObject;
	}


    } // End of nested class I18nFormFinder.

    
    /** Replacer used by enclosing class. */
   private static class FormI18nReplacer extends JavaI18nReplacer {
       
       /** Reference to form finder. */
       private FormI18nFinder finder;
       
       private final ResourceBundle bundle;
       
       /** Constructor. */
       public FormI18nReplacer(FormI18nFinder finder) {
           this.finder = finder;
           bundle = NbBundle.getBundle(FormI18nSupport.class);
       }

       
       @Override
       public void replace(final HardCodedString hcString, final I18nString i18nString) {
           if(hcString instanceof FormHardCodedString) {
               replaceInGuarded((FormHardCodedString)hcString, (JavaI18nString)i18nString);
           } else {
               super.replace(hcString, i18nString);
           }
       }

       /** Replaces found hard coded string in guarded blocks. */
       private void replaceInGuarded(FormHardCodedString formHcString, JavaI18nString javaI18nString) {
           try {
               String replaceString = javaI18nString.getReplaceString();

               // Remember position offset before change of guarded block
               int lastPos = finder.getLastPosition().getOffset();

               int pos = formHcString.getEndPosition().getOffset();

               // new value to set
               Object newValue;

               Node.Property nodeProperty = formHcString.getNodeProperty();

               ValidFormProperty validProp = formHcString.getValidProperty();

               // old value
               Object oldValue = nodeProperty.getValue();

               // RAD property -> like text, title etc.
               if(nodeProperty instanceof FormProperty) {
                   if(oldValue instanceof RADConnectionDesignValue
                   && ((RADConnectionDesignValue)oldValue).getType() == RADConnectionDesignValue.TYPE_CODE) {
                       // The old value is set via RADConnectionPropertyEditor,
                       // (in our case if value was RADConnectionDesignValue of type TYPE_CODE (= user code))
                       String oldString = ((RADConnectionDesignValue)oldValue).getCode();

                       StringBuffer buff = new StringBuffer(oldString);

                       int index = indexOfHardString(oldString, formHcString.getText(), validProp.getSkip());

                       if (index == -1) {
                           NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                               bundle.getString("MSG_StringNotFoundInGuarded"), NotifyDescriptor.ERROR_MESSAGE);
                               DialogDisplayer.getDefault().notify(message);
                                
                           return;
                       }

                       int startOffset = index;

                       int endOffset = startOffset + formHcString.getText().length() + 2; // 2 for quotes.

                       buff.replace(startOffset, endOffset, replaceString);

                       RADConnectionDesignValue newConnectionValue = new RADConnectionDesignValue(buff.toString());
                       newValue = newConnectionValue;
                   } else {
                       // The old value is set via ResourceBundleStringFormEditor,
                       // (in our case if value was "plain string" or
                       // RADConnectionDesignValue of type TYPE_VALUE.
                       ((FormProperty)nodeProperty).setCurrentEditor(new FormI18nStringEditor());
                       newValue = new FormI18nString(javaI18nString);
                   }
               } else {
                   // Node.Property -> code generation properties.
                   // Replace the part of old value which matches "quoted" hardString only.
                   String oldString = (String)oldValue;
                   StringBuffer buff = new StringBuffer(oldString);

                   int index = indexOfHardString(oldString, formHcString.getText(), validProp.getSkip());

                   if(index == -1) {
                       NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                           bundle.getString("MSG_StringNotFoundInGuarded"), NotifyDescriptor.ERROR_MESSAGE);
                           DialogDisplayer.getDefault().notify(message);
                           
                       return;
                   }

                   int startOffset = index;
                   
                   int endOffset = startOffset + formHcString.getText().length() + 2; // 2 for quotes

                   buff.replace(startOffset, endOffset, replaceString);

                   newValue = buff.toString();
               }

               // Finally set the new value to property.
               nodeProperty.setValue(newValue);

               // Decrement last found skip.
               finder.decrementLastFoundSkip();
               
               final StyledDocument document = javaI18nString.getSupport().getDocument();

               // form editor may change the string, e.g. replace ResourceBundle.getBundle with bundle variable
               int replaceStringLength = newValue instanceof FormI18nString ? 0 : replaceString.length();
               final int lastP = lastPos + replaceStringLength - formHcString.getText().length() - 2; // 2 for quotes
               SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                       // Little trick to reset the last position in finder after guarded block was regenerated.
                       if (document instanceof AbstractDocument) {
                            ((AbstractDocument)document).readLock();
                       }
                       try {
                           finder.setLastPosition(document.createPosition(lastP));                   
                       } catch (BadLocationException ble) {
                           ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
                       } finally {
                           if (document instanceof AbstractDocument) {
                                ((AbstractDocument) document).readUnlock();
                           }
                       }
                    }
               });
            } catch (IllegalAccessException iae) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
            } catch (InvocationTargetException ite) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ite);
            }
        }   

        /** Helper method. */
        private int indexOfHardString(String source, String hardString, int skip) {
            while (skip >= 0) {
                int index = finder.indexOfNonI18nString(source, hardString, skip);
                if (index >= 0) {
                    return index;
                }
                skip--;
            }
            
            return -1;
        }
        
    } // End of nested class FormI18nReplacer
    
    
    /** HardCoded string found within form guarded block
     * and which contains values of form property to wich belongs. */
    private static class FormHardCodedString extends HardCodedString {
        
        /** Valid property with name etc. */
        private ValidFormProperty validProperty;
        
        /** Valid property. */
        private Node.Property nodeProperty;
        

        /** Constructor. */
        FormHardCodedString(String text, Position startPosition, Position endPosition, 
            ValidFormProperty validProperty, Node.Property nodeProperty) {
                
            super(text, startPosition, endPosition);
            this.validProperty = validProperty;
            this.nodeProperty = nodeProperty;
        }
        
        
        /** Getter for <code>validProperty</code>. */
        public ValidFormProperty getValidProperty() {
            return validProperty;
        }
        
        /** Getter for <code>nodeProperty</code>. */
        public Node.Property getNodeProperty() {
            return nodeProperty;
        }
        
    } // End of nested class FormHardCodedString.

    
    /** Panel for showing info about hard coded string. */
    private static class FormInfoPanel extends InfoPanel {
        
        /** Constructor. */
        public FormInfoPanel(HardCodedString hcString, StyledDocument document) {
            super(hcString, document);
        }
        
        
        /** Implements superclass abstract method. */
        protected void setHardCodedString(HardCodedString hcString, StyledDocument document) {

            getStringText().setText(hcString == null ? "" : hcString.getText()); // NOI18N
            
            int pos;

            String hardLine;
            
            if (hcString.getStartPosition() == null) {
                hardLine = ""; // NOI18N
            } else {
                pos = hcString.getStartPosition().getOffset();

                try {
                    Element paragraph = document.getParagraphElement(pos);
                    hardLine = document.getText(paragraph.getStartOffset(), paragraph.getEndOffset()-paragraph.getStartOffset()).trim();
                } catch (BadLocationException ble) {
                    hardLine = ""; // NOI18N
                }
            }

            getFoundInText().setText(hardLine);

            if(hcString instanceof FormHardCodedString) {
                getComponentText().setText( ((FormHardCodedString)hcString).getValidProperty().getRADComponentName());
                getPropertyText().setText( ((FormHardCodedString)hcString).getNodeProperty().getName());
            } else {            
                remove(getComponentLabel());
                remove(getComponentText());
                remove(getPropertyLabel());
                remove(getPropertyText());
            }
            
        }
    } // End of FormInfoPanel inner class.

    /** Factory for <code>FormI18nSupport</code>. */
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.i18n.I18nSupport.Factory.class)
    public static class Factory extends I18nSupport.Factory {

        /** Instantiated from META-INF/services */
        public Factory() {
        }

        /** Gets <code>I18nSupport</code> instance for specified data object and document.
         * @exception IOException when the document could not be loaded */
        @Override
        public I18nSupport create(DataObject dataObject) throws IOException {
            I18nSupport support = super.create(dataObject);
            
            FormDataObject formDataObject = (FormDataObject)dataObject;
            FormEditorSupport formSupport = (FormEditorSupport)formDataObject.getFormEditorSupport();
            if (formSupport.isOpened()) {
                return support;
            }
            if (formSupport.loadForm()) {
                return support;
            }
            throw new IOException("I18N: Loading form for " + dataObject.getName() + " was not succesful."); // NOI18N
        }
        
        /** Implements superclass abstract method. */
        public I18nSupport createI18nSupport(DataObject dataObject) {
            return new FormI18nSupport(dataObject);
        }
        
        /** Gets class of supported <code>DataObject</code>.
         * @return <code>FormDataObject</code> class or <code>null</code> */
        public Class getDataObjectClass() {
            return FormDataObject.class;
//            try {
//                // FIXME remove reflection with system classloader. what's wrong missing implementaion dependency?
//                return Class.forName("org.netbeans.modules.form..FormDataObject", false, (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class));  // NOI18N
//            } catch (ClassNotFoundException e) {
//                ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot enable I18N support: " + e.getMessage());  //NOI18N
//                return null;
//            }
        }

    } // End of class Factory.

}
