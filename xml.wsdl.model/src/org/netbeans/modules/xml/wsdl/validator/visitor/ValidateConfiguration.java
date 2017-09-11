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
