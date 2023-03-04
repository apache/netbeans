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
