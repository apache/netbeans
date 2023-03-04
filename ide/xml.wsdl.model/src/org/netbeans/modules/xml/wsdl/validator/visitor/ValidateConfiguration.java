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

import java.util.Properties;

/**
 * Provides configuration for the validate visitor of BPEL and WSDL documents.
 *
 * @author  ed.wong
 * @version 
 */
public class ValidateConfiguration extends Properties {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5171902287808106628L;

    /** BPEL document type */
    public static final String BPEL = "bpel";
    
    /** WSDL document type */
    public static final String WSDL = "wsdl";
    
    // =============================================================
    
    /** Syntax check */
    public static final String SYNTAX = ".syntax";
    
    /** Semantics check */
    public static final String SEMANTICS = ".semantics";
    
    /** Consistency check */
    public static final String CONSISTENCY = ".consistency";
    
    // =============================================================
    
    /** Attribute type */
    public static final String ATTRIB = ".attrib";
    
    /** Element type */
    public static final String ELEM = ".elem";
    
    // =============================================================
    
    /** Required check */
    public static final String REQUIRED = ".required";
    
    /** QName check */
    public static final String QNAME = ".qname";
    
    /** NCName check */
    public static final String NCNAME = ".ncname";
    
    /** Boolean check */
    public static final String BOOLEAN = ".boolean";
    
    /** Enumerated check */
    public static final String ENUMERATED = ".enumerated";
    
    /** CreateInstance check */
    public static final String CREATE_INSTANCE = ".createInstance";
    
    /** Match catch to fault check */
    public static final String MATCH_CATCH = ".matchCatch";
    
    /** Minimum check */
    public static final String MIN = ".min";
    
    // =============================================================
    
    /** BPEL SYNTAX ATTRIB REQUIRED check */
    public static final String BPEL_SYNTAX_ATTRIB_REQUIRED = BPEL + SYNTAX + ATTRIB + REQUIRED;
    
    /** BPEL SYNTAX ATTRIB QNAME check */
    public static final String BPEL_SYNTAX_ATTRIB_QNAME = BPEL + SYNTAX + ATTRIB + QNAME;
    
    /** BPEL SYNTAX ATTRIB NCNAME check */
    public static final String BPEL_SYNTAX_ATTRIB_NCNAME = BPEL + SYNTAX + ATTRIB + NCNAME;
    
    /** BPEL SYNTAX ATTRIB BOOLEAN check */
    public static final String BPEL_SYNTAX_ATTRIB_BOOLEAN = BPEL + SYNTAX + ATTRIB + BOOLEAN;
    
    /** BPEL SYNTAX ATTRIB ENUMERATED check */
    public static final String BPEL_SYNTAX_ATTRIB_ENUMERATED = BPEL + SYNTAX + ATTRIB + ENUMERATED;
    
    /** BPEL SYNTAX ELEM MIN check */
    public static final String BPEL_SYNTAX_ELEM_MIN = BPEL + SYNTAX + ELEM + MIN;
    
    /** BPEL SYNTAX ELEM REQUIRED check */
    public static final String BPEL_SYNTAX_ELEM_REQUIRED = BPEL + SYNTAX + ELEM + REQUIRED;
    
    /** BPEL SEMANTICS CREATE_INSTANCE check */
    public static final String BPEL_SEMANTICS_CREATE_INSTANCE = BPEL + SEMANTICS + CREATE_INSTANCE;
    
    /** BPEL CONSISTENCY MATCH_CATCH check */
    public static final String BPEL_CONSISTENCY_MATCH_CATCH = BPEL + CONSISTENCY + MATCH_CATCH;
    
    // =============================================================
    
    /** WSDL SYNTAX ATTRIB REQUIRED check */
    public static final String WSDL_SYNTAX_ATTRIB_REQUIRED = WSDL + SYNTAX + ATTRIB + REQUIRED;
    
    /** WSDL SYNTAX ATTRIB QNAME check */
    public static final String WSDL_SYNTAX_ATTRIB_QNAME = WSDL + SYNTAX + ATTRIB + QNAME;
    
    /** WSDL SYNTAX ATTRIB NCNAME check */
    public static final String WSDL_SYNTAX_ATTRIB_NCNAME = WSDL + SYNTAX + ATTRIB + NCNAME;
    
    /** WSDL SYNTAX ATTRIB BOOLEAN check */
    public static final String WSDL_SYNTAX_ATTRIB_BOOLEAN = WSDL + SYNTAX + ATTRIB + BOOLEAN;
    
    /** WSDL SYNTAX ATTRIB ENUMERATED check */
    public static final String WSDL_SYNTAX_ATTRIB_ENUMERATED = WSDL + SYNTAX + ATTRIB + ENUMERATED;
    
    /** WSDL SYNTAX ELEM MIN check */
    public static final String WSDL_SYNTAX_ELEM_MIN = WSDL + SYNTAX + ELEM + MIN;
    
    /** WSDL SYNTAX ELEM REQUIRED check */
    public static final String WSDL_SYNTAX_ELEM_REQUIRED = WSDL + SYNTAX + ELEM + REQUIRED;
    
    /** Creates a new instance of ValidateConfiguration */
    public ValidateConfiguration() {
        super();
    }
    
    /** Creates a new instance of ValidateConfiguration
     * @param   defaults    Defaults to use.
     */
    public ValidateConfiguration(Properties defaults) {
        super(defaults);
    }
    
    /** Gets the boolean property.  If the key doesn't exist, it's assumed <code>true</code>
     * since this yields a stricter validation.
     *
     * @param   key     Key for the property.
     * @return  <code>boolean</code> value for the property.
     */
    public boolean getBooleanProperty(String key) {
        String val = getProperty(key);
        return (null == val ? true : Boolean.valueOf(val).booleanValue());
    }
    
    /** Gets the integer property.  If the key doesn't exist, it's assumed <code>1</code>
     * since this yields a stricter validation.
     *
     * @param   key     Key for the property.
     * @return  <code>int</code> value for the property.
     */
    public int getIntegerProperty(String key) {
        String val = getProperty(key);
        return (null == val ? 1 : Integer.parseInt(val));
    }
}
