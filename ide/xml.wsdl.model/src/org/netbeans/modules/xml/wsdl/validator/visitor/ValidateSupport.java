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
package org.netbeans.modules.xml.wsdl.validator.visitor;

import java.util.Collection;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;


/**
 * Supports validation of BPEL/WSDL documents.
 *
 * @author  edwong
 * @version 
 */
public class ValidateSupport  {

    
    /** Validate Configuration */
    private ValidateConfiguration mValConfig;
    
    /** Missing required attribute */
    public static final String VAL_MISSING_ATTRIB = "VAL_MISSING_ATTRIB";  // Not I18N
    
    /** Please define required attribute */
    public static final String FIX_MISSING_ATTRIB = "FIX_MISSING_ATTRIB";  // Not I18N
    
    /** Not NCName attribute */
    public static final String VAL_NOT_NCNAME_ATTRIB = "VAL_NOT_NCNAME_ATTRIB";  // Not I18N
    
    /** Please make attribute comply to NCName */
    public static final String FIX_NOT_NCNAME_ATTRIB = "FIX_NOT_NCNAME_ATTRIB";  // Not I18N
    
    /** Not QName attribute */
    public static final String VAL_NOT_QNAME_ATTRIB = "VAL_NOT_QNAME_ATTRIB";  // Not I18N
    
    /** Please make attribute comply to QName */
    public static final String FIX_NOT_QNAME_ATTRIB = "FIX_NOT_QNAME_ATTRIB";  // Not I18N
    
    /** Prefix in QName is null */
    public static final String VAL_PREFIX_NULL_QNAME_ATTRIB = 
        "VAL_PREFIX_NULL_QNAME_ATTRIB";  // Not I18N
    
    /** Please define a prefix in the QName */
    public static final String FIX_PREFIX_NULL_QNAME_ATTRIB = 
        "FIX_PREFIX_NULL_QNAME_ATTRIB";  // Not I18N

    /** Prefix not defined */
    public static final String VAL_PREFIX_NOT_DEFINED = "VAL_PREFIX_NOT_DEFINED";  // Not I18N
    
    /** Please associate prefix */
    public static final String FIX_PREFIX_NOT_DEFINED = "FIX_PREFIX_NOT_DEFINED";  // Not I18N
    
    /** Not boolean attribute */
    public static final String VAL_NOT_BOOLEAN_ATTRIB = "VAL_NOT_BOOLEAN_ATTRIB";  // Not I18N
    
    /** Please make attribute boolean */
    public static final String FIX_NOT_BOOLEAN_ATTRIB = "FIX_NOT_BOOLEAN_ATTRIB";  // Not I18N
    
    /** Not enumerated attribute */
    public static final String VAL_NOT_ENUM_ATTRIB = "VAL_NOT_ENUM_ATTRIB";  // Not I18N
    
    /** Please choose one of enumerated values */
    public static final String FIX_NOT_ENUM_ATTRIB = "FIX_NOT_ENUM_ATTRIB";  // Not I18N
    
    /** Minimum sub-elements not met */
    public static final String VAL_MIN_ELEM_NOT_MET = "VAL_MIN_ELEM_NOT_MET";  // Not I18N
    
    /** Please add more sub-elements */
    public static final String FIX_MIN_ELEM_NOT_MET = "FIX_MIN_ELEM_NOT_MET";  // Not I18N
    
    /** Missing required sub-element */
    public static final String VAL_MISSING_ELEM = "VAL_MISSING_ELEM";  // Not I18N
    
    /** Please define sub-element */
    public static final String FIX_MISSING_ELEM = "FIX_MISSING_ELEM";  // Not I18N
    
    /** Matching WSDL document not found */
    public static final String VAL_MATCHING_WSDL_NOT_FOUND = "VAL_MATCHING_WSDL_NOT_FOUND";  // Not I18N
    
    /** Please import matching WSDL document */
    public static final String FIX_MATCHING_WSDL_NOT_FOUND = "FIX_MATCHING_WSDL_NOT_FOUND";  // Not I18N

    /**Trying to compensate an invalid scope **/
    public static final String VAL_COMPENSATE_WITH_AN_INVALID_SCOPE =
        "VAL_COMPENSATE_WITH_AN_INVALID_SCOPE";  //Not I18N

    /** Fix compensate with an invalid scope **/
    public static final String FIX_COMPENSATE_WITH_AN_INVALID_SCOPE =
        "FIX_COMPENSATE_WITH_AN_INVALID_SCOPE";  //Not I18N

    /** message type in container key*/
    public static final String VAL_MESSAGETYPE_IN_CONTAINER = "VAL_MESSAGETYPE_IN_CONTAINER";  //Not I18N
    /**message type in container fix*/
    public static final String FIX_MESSAGETYPE_IN_CONTAINER = "FIX_MESSAGETYPE_IN_CONTAINER";  //Not I18N

    /**catch type in container key*/
    public static final String VAL_UNKNOWN_CONTAINER_IN_CATCH = "VAL_UNKNOWN_CONTAINER_IN_CATCH";  //Not I18N
    /**catch type in container fix*/    
    public static final String FIX_UNKNOWN_CONTAINER_IN_CATCH = "FIX_UNKNOWN_CONTAINER_IN_CATCH";  //Not I18N
    /**empty container key*/    
    public static final String VAL_EMPTY_CONTAINER_IN_CATCH = "VAL_EMPTY_CONTAINER_IN_CATCH";  //Not I18N
    /**sequence*/    
    public static final String SEQUENCE_TITLE_NAME = "SEQUENCE_TITLE_NAME";  //Not I18N
    /** missing connection */
    public static final String VAL_MISSING_CONNECTIONS = "VAL_MISSING_CONNECTIONS";  //Not I18N
    /**fix missing connection */
    public static final String FIX_MISSING_CONNECTIONS = "FIX_MISSING_CONNECTIONS";  //Not I18N

    /** correct correlation set */
    public static final String VAL_SEMANTICALLY_CORRECT_CORRELATION_SET =
        "VAL_SEMANTICALLY_CORRECT_CORRELATION_SET";  //Not I18N
    /** correct correlation set fix */
    public static final String FIX_SEMANTICALLY_CORRECT_CORRELATION_SET =
        "FIX_SEMANTICALLY_CORRECT_CORRELATION_SET";  //Not I18N
    /** empty fault name */
    public static final String VAL_FAULT_NAME_CANNOT_BE_EMPTY = "VAL_FAULT_NAME_CANNOT_BE_EMPTY";  //Not I18N
    /**empty fault name fix */
    public static final String FIX_FAULT_NAME_CANNOT_BE_EMPTY = "FIX_FAULT_NAME_CANNOT_BE_EMPTY";  //Not I18N
    /** unknown operation */
    public static final String VAL_UNKNOWN_OPERATION = "VAL_UNKNOWN_OPERATION";  //Not I18N
    /** unknown operation fix */
    public static final String FIX_UNKNOWN_OPERATION = "FIX_UNKNOWN_OPERATION";  //Not I18N
    /** unknown port type */
    public static final String VAL_UNKNOWN_PORT_TYPE = "VAL_UNKNOWN_PORT_TYPE";  //Not I18N
    /** unknown port type fix */
    public static final String FIX_UNKNOWN_PORT_TYPE = "FIX_UNKNOWN_PORT_TYPE";  //Not I18N
    /** minimym needed for assign */
    public static final String VAL_ASSIGN_MUST_HAVE_MINIMUM = "VAL_ASSIGN_MUST_HAVE_MINIMUM";  //Not I18N
    /** min fix for assing */
    public static final String FIX_ASSIGN_MUST_HAVE_MINIMUM = "FIX_ASSIGN_MUST_HAVE_MINIMUM";  //Not I18N
    /**
     * min foreach
     */
    public static final String VAL_FOR_EACH_MUST_HAVE_MIN_ONE_COPY =
        "VAL_FOR_EACH_MUST_HAVE_MIN_ONE_COPY";  //Not I18N
    /** for each min fix */
    public static final String FIX_FOR_EACH_MUST_HAVE_MIN_ONE_COPY =
        "FIX_FOR_EACH_MUST_HAVE_MIN_ONE_COPY";  //Not I18N

    /**
     * empty scope
     */
    public static final String VAL_EMPTY_SCOPE = "VAL_EMPTY_SCOPE";  //Not I18N
    
    /**
     * empty scope fix
     */
    public static final String FIX_EMPTY_SCOPE = "FIX_EMPTY_SCOPE";  //Not I18N
    
    /**
     * empty while
     */
    public static final String VAL_EMPTY_WHILE = "VAL_EMPTY_WHILE";  //Not I18N

    /**
     * empty while fix
     */
    public static final String FIX_EMPTY_WHILE = "FIX_EMPTY_WHILE";  //Not I18N

    /**
     * empty compensation handler
     */
    public static final String VAL_EMPTY_COMPENSATION_HANDLER = "VAL_EMPTY_COMPENSATION_HANDLER";  //Not I18N

    /**
     * fix for an empty compensdation handler
     */
    public static final String FIX_EMPTY_COMPENSATION_HANDLER = "FIX_EMPTY_COMPENSATION_HANDLER";  //Not I18N

     /**
      * empty catch
      */
    public static final String VAL_EMPTY_CATCH = "VAL_EMPTY_CATCH";  //Not I18N

    /**
     * fix for an empty catch
     */
     public static final String FIX_EMPTY_CATCH = "FIX_EMPTY_CATCH";  //Not I18N

     /**
      * empty catch
      */
    public static final String VAL_EMPTY_CATCHALL = "VAL_EMPTY_CATCHALL";  //Not I18N

    /**
     * fix for an empty catch
     */
     public static final String FIX_EMPTY_CATCHALL = "FIX_EMPTY_CATCHALL";  //Not I18N
     
     /** No eInsight license for eVison */
     public static final String VAL_NO_EINSIGHT_LICENSE_W_EVISION = "VAL_NO_EINSIGHT_LICENSE_W_EVISION";  // Not I18N
     
     /** Only create instance from Page Link for eVision */
     public static final String FIX_NO_EINSIGHT_LICENSE_W_EVISION = "FIX_NO_EINSIGHT_LICENSE_W_EVISION";  // Not I18N

        
    private Collection<ResultItem> mResultItems; 
    private Validator mValidator; 
    
    /** Creates a new instance of ValidateSupport.
     * @param   valConfig   Validate configuration.
     */
    public ValidateSupport(ValidateConfiguration valConfig) {
        super();
        mValConfig = valConfig;
    }
    
    /** Gets the validate configuration currently used.
     * @return  Validate configuration currently used.
     */
    public ValidateConfiguration getValidateConfiguration() {
        return mValConfig;
    }
    
    /** Sets the validate configuration currently used.
     * @param   valConfig   Validate configuration to use.
     */
    public void setValidateConfiguration(ValidateConfiguration valConfig) {
        mValConfig = valConfig;
    }
    
    public void setResultItems(Collection<ResultItem> resultItems) {
        mResultItems = resultItems;
    }
    public void setValidator(Validator validator) {
        mValidator = validator;
    }
    
    /** Tests if an attribute value is absent.
     * @param   value   Value of attribute.
     * @return  <code>true</code> if value is absent.
     */
    public static boolean isAttributeAbsent(String value) {
        return ((null == value) || (value.trim().length() == 0));
    }
    
    /** Asserts that a required attribute is present.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertRequiredAttrib(String value, String name, Component source) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_REQUIRED))) {
            return true;
        }
        
        if (isAttributeAbsent(value)) {
            return fireToDo(Validator.ResultType.ERROR, source, NbBundle.getMessage(ValidateSupport.class, VAL_MISSING_ATTRIB, name), 
                    NbBundle.getMessage(ValidateSupport.class, FIX_MISSING_ATTRIB, name));
        }
        
        return true;
    }

    /** Asserts that a required attribute is present.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertRequiredAttrib(QName value, String name, Component source, int category) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_REQUIRED))) {
            return true;
        }
        
        if (value == null || isAttributeAbsent(value.toString())) {
            return fireToDo(Validator.ResultType.ERROR, source, NbBundle.getMessage(ValidateSupport.class, VAL_MISSING_ATTRIB, name),
                NbBundle.getMessage(ValidateSupport.class, FIX_MISSING_ATTRIB, name));
        }
        
        return true;
    }

    /** Asserts that an attribute is a NCName.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertNCNameAttrib(String value, String name, Component source) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_NCNAME))) {
            return true;
        }
        
        if (!isAttributeAbsent(value) && !ValidationUtils.isNCName(value)) {
            return fireToDo(Validator.ResultType.ERROR, source,
                NbBundle.getMessage(ValidateSupport.class, VAL_NOT_NCNAME_ATTRIB, name),
                NbBundle.getMessage(ValidateSupport.class, FIX_NOT_NCNAME_ATTRIB, name)
            );
        }
        return true;
    }

    /** Asserts that an attribute is a QName list.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertQNameListAttrib(String value, String name, Component source, int category) {
        if ((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_QNAME)) {
            return true;
        }
        
        if (!isAttributeAbsent(value)) {
            StringTokenizer parser = new StringTokenizer(value, " ");
            while (parser.hasMoreTokens()) {
                if (!assertQNameAttrib(parser.nextToken(), name, source, category)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /** Asserts that a required attribute is a QName.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertQNameAttrib(QName value, String name, Component source, int category) {
        if(value != null) {
            return assertQNameAttrib(value.toString(), name, source, category);
        }
        return true;
    }
    
    /** Asserts that a required attribute is a QName.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertQNameAttrib(String value, String name, Component source, int category) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_QNAME))) {
            return true;
        }
        
        if (!isAttributeAbsent(value)) {
            // First, check if QName compliant
            if (!ValidationUtils.isQName(value)) {
                if (!fireToDo(Validator.ResultType.ERROR,
                            source,
                            
                            NbBundle.getMessage(ValidateSupport.class, VAL_NOT_QNAME_ATTRIB, name),
                            NbBundle.getMessage(ValidateSupport.class, FIX_NOT_QNAME_ATTRIB, name)
                        )) {
                    return false;
                }
            }
            String[] parts = value.split(":");//QName.getPrefix(value)
            if (parts != null && parts.length > 1) {
                
                String prefix = parts[0];
                if (prefix != null) {
                    // Second, check if prefix corresponds to a namespace
                    String ns = ((AbstractDocumentComponent) source).lookupNamespaceURI(prefix);
                     
                    if (null == ns) {
                        if (!fireToDo(Validator.ResultType.ERROR, 
                                source,
                                NbBundle.getMessage(ValidateSupport.class, VAL_PREFIX_NOT_DEFINED, prefix),
                                NbBundle.getMessage(ValidateSupport.class, FIX_PREFIX_NOT_DEFINED, prefix)
                        )) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** Asserts that a required attribute is a QName. Apart from the QName test, also
     * verify that the prefix is not null
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @param   verifyNullPrefix If set verify that the prefix in the QName is not null
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertQNameAttrib(String value, String name, Component source, 
                                     int category, boolean verifyNullPrefix) {
        boolean valid = assertQNameAttrib(value, name, source, category);
        if (valid && verifyNullPrefix) {
            /*String prefix = QName.getPrefix(value);*/
            String[] parts = value.split(":");//QName.getPrefix(value)
            if (parts != null && parts.length > 1) {
                
                String prefix = parts[0];
                // Second, check if prefix is not null
                if (prefix == null || "".equals(prefix.trim())) {
                    if (!fireToDo(Validator.ResultType.ERROR, 
                            source,
                                    NbBundle.getMessage(ValidateSupport.class, VAL_PREFIX_NULL_QNAME_ATTRIB, name),
                                    NbBundle.getMessage(ValidateSupport.class, FIX_PREFIX_NULL_QNAME_ATTRIB, name)
                            )) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    /** Asserts that an attribute has a boolean value.
     * @param   value       Value of attribute.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertBooleanAttrib(String value, String name, Component source, int category) {
        if ((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_BOOLEAN)) {
            return true;
        }
        
        if (!isAttributeAbsent(value) && !(value.equals("yes") || value.equals("no"))) {
            return fireToDo(Validator.ResultType.ERROR,
                source,
                NbBundle.getMessage(ValidateSupport.class, VAL_NOT_BOOLEAN_ATTRIB, name),
                NbBundle.getMessage(ValidateSupport.class, FIX_NOT_BOOLEAN_ATTRIB, name)
            );
        }
        return true;
    }
    
    /** Asserts that a minimum number of sub-elements is required.
     * @param   num         Number of existing sub-elements.
     * @param   min         Minimum number of sub-elements required.
     * @param   name        Name of sub-element.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertMinimumElem(int num, int min, String name, Component source, int category) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ELEM_MIN))) {
            return true;
        }
        
        if (num < min) {
            return fireToDo(Validator.ResultType.ERROR,
                source,
               /* category,
                Severity.ERROR,*/
                NbBundle.getMessage(ValidateSupport.class, VAL_MIN_ELEM_NOT_MET, Integer.valueOf(min), name, Integer.valueOf(num)),
                NbBundle.getMessage(ValidateSupport.class, FIX_MIN_ELEM_NOT_MET, Integer.valueOf(min - num), name)
            );
        }
        return true;
    }

    /** Asserts that an attribute has an enumerated value.
     * @param   value       Value of attribute.
     * @param   enums       Enumerated values as an array.
     * @param   name        Name of attribute.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertEnumeratedAttrib(String value, String[] enums, String name, Component source,
                                          int category) {
       if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ATTRIB_ENUMERATED))) {
           return true;
       }
       
       if (!isAttributeAbsent(value) && (enums != null) && (enums.length > 0)) {
           boolean found = false;
           for (int i = 0; i < enums.length; i++) {
               if (ValidationUtils.areEqualXMLValues(value, enums[i])) {
                   found = true;
                   break;
               }
           }
           if (!found) {
               StringBuffer enumsb = new StringBuffer();
               for (int i = 0; i < enums.length; i++) {
                   if (i > 0) {
                       enumsb.append(", ");
                   }
                   enumsb.append(enums[i]);
               }
               return fireToDo(Validator.ResultType.ERROR,
                    source, /*Category.BPEL_SYNTAX, Severity.ERROR,*/
                    NbBundle.getMessage(ValidateSupport.class, VAL_NOT_ENUM_ATTRIB, name, enumsb.toString()),
                    NbBundle.getMessage(ValidateSupport.class, FIX_NOT_ENUM_ATTRIB, enumsb.toString(), name)
               );
           }
       }
       return true;
    }
    
    /** Asserts that a required sub-element is present.
     * @param   subElem     Sub-element.
     * @param   name        Name of sub-element.
     * @param   source      Source element.
     * @param   category    Category of failure.
     * @return  <code>true</code> if more validations can be made; <code>false</code> otherwise.
     */
    public boolean assertRequiredElement(Component subElem, String name, Component source, int category) {
        if (((source instanceof WSDLComponent)
                    && !mValConfig.getBooleanProperty(ValidateConfiguration.WSDL_SYNTAX_ELEM_REQUIRED))) {
            return true;
        }
        
        if (null == subElem) {
            return fireToDo(Validator.ResultType.ERROR,
                source,
                NbBundle.getMessage(ValidateSupport.class, VAL_MISSING_ELEM, name),
                NbBundle.getMessage(ValidateSupport.class, FIX_MISSING_ELEM, name));
        }
        return true;
    }
    
    /** Fires to-do events to listeners.
     * @param   toDoEvent   To-do event to fire.
     * @return  <code>true</code> if more events can be accepted by the listener;
     *          <code>false</code> otherwise.
     */
    public boolean fireToDo(Validator.ResultType type, Component component,  String desc, String correction) {
        String message = desc;
        if (correction != null) {
            message = desc + " : " + correction;
        }
        ResultItem item = new Validator.ResultItem(mValidator, type, component, message);
        mResultItems.add(item);
        return true;
    }
}
