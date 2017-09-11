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


package org.netbeans.modules.xml.schema.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * An enumeration representing the valid schema element and attribute
 * names.
 * @author Samaresh Panda (Samaresh.Panda@Sun.Com)
 * @author Chris Webster
 */
public enum SchemaElements {
    ALL("all"),
    ANNOTATION("annotation"),
    ANY("any"),
    ANYTYPE("anyType"),
    ANY_ATTRIBUTE("anyAttribute"),
    APPINFO("appinfo"),
    ATTRIBUTE("attribute"),
    ATTRIBUTE_GROUP("attributeGroup"),
    CHOICE("choice"),
    COMPLEX_CONTENT("complexContent"),
    COMPLEX_TYPE("complexType"),
    DOCUMENTATION("documentation"),
    ELEMENT("element"),
    ENUMERATION("enumeration"),
    EXTENSION("extension"),
    FIELD("field"),
    FRACTION_DIGITS("fractionDigits"),
    GROUP("group"),
    INCLUDE("include"),
    IMPORT("import"),
    KEY("key"),
    KEYREF("keyref"),
    LENGTH("length"),
    LIST("list"),
    MAX_EXCLUSIVE("maxExclusive"),
    MAX_INCLUSIVE("maxInclusive"),
    MIN_EXCLUSIVE("minExclusive"),
    MIN_INCLUSIVE("minInclusive"),
    MAX_LENGTH("maxLength"),
    MIN_LENGTH("minLength"),
    NOTATION("notation"),
    PATTERN("pattern"),
    REDEFINE("redefine"),
    RESTRICTION("restriction"),
    SCHEMA("schema"),
    SELECTOR("selector"),
    SEQUENCE("sequence"),
    SIMPLE_CONTENT("simpleContent"),
    SIMPLE_TYPE("simpleType"),
    TOTAL_DIGITS("totalDigits"),
    UNION("union"),
    UNIQUE("unique"),
    WHITESPACE("whiteSpace");

    SchemaElements(String docName) {
        this.docName = docName;
        //this.possibleAttrs = attrs;
    }
    
    public String getName() {
        return docName;
    }
    
    public QName getQName() {
        return new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, docName);
    }
    
    public static Set<QName> allQNames() {
        if (allQNames == null) {
            allQNames = new HashSet<QName>();
            for (SchemaElements v : values()) {
                allQNames.add(v.getQName());
            }
        }
        return allQNames;
    }
    
    private final String docName;
    private static Set<QName> allQNames = null;
} 
