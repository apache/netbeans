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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Element;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.Any.ProcessContents;
import org.netbeans.modules.xml.schema.model.Derivation;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * An enumeration representing the schema attribute names.
 * @author Samaresh Panda (Samaresh.Panda@Sun.Com)
 * @author Chris Webster
 */
public enum SchemaAttributes implements Attribute {
    ABSTRACT("abstract", Boolean.class),
    ATTR_FORM_DEFAULT("attributeFormDefault", Form.class),
    BASE("base", String.class),
    BLOCK("block", Set.class, Derivation.Type.class),
    BLOCK_DEFAULT("blockDefault", Set.class, Schema.Block.class),
    DEFAULT("default", String.class),
    ELEM_FORM_DEFAULT("elementFormDefault", Form.class),
    ID("id", String.class),
    ITEM_TYPE("itemType", String.class), 
    FINAL("final", Set.class, Derivation.Type.class),
    FINAL_DEFAULT("finalDefault", Set.class, Schema.Final.class),
    FIXED("fixed", Boolean.class),
    FORM("form", Form.class),
    LANGUAGE("xml:lang", String.class),
    MAX_OCCURS("maxOccurs", String.class),
    MEMBER_TYPES("memberTypes", String.class),
    MIN_OCCURS("minOccurs", Integer.class),
    MIXED("mixed", Boolean.class),
    NAME("name", String.class),
    NAMESPACE("namespace", String.class),
    NILLABLE("nillable", Boolean.class),
    PROCESS_CONTENTS("processContents", ProcessContents.class),
    PUBLIC("public", String.class),
    REF("ref", String.class),
    REFER("refer", String.class), 
    SCHEMA_LOCATION("schemaLocation", String.class),
    SOURCE("source", String.class),
    SUBSTITUTION_GROUP("substitutionGroup", String.class),
    SYSTEM("system", String.class),
    TARGET_NS("targetNamespace", String.class),
    TYPE("type", String.class),
    USE("use", LocalAttribute.Use.class),
    VALUE("value", String.class),
    VERSION("version", String.class),
    XPATH("xpath", String.class);

    SchemaAttributes(String docName, Class type, Class memberType) {
        this.docName = docName;
        this.type = type;
        this.memberType = memberType;
    }
    
    SchemaAttributes(String docName, Class type) {
        this(docName, type, null);
    }
    
    public String getName() {
        return docName;
    }
    
    public Class getType() {
        return type;
    }
    
    public Class getMemberType() {
        return memberType;
    }
    
    public static Map<QName,List<QName>> getQNameValuedAttributes() {
        return qnameValuedAttributes;
    }
    
    private QName qname() {
        return new QName(docName);
    }
    
    private final String docName;
    private final Class type;
    private final Class memberType;

    
    private static Map<QName,List<QName>> qnameValuedAttributes = new HashMap<QName,List<QName>>();
    static {
        qnameValuedAttributes.put(
                SchemaElements.UNION.getQName(), Arrays.asList(new QName[] { MEMBER_TYPES.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.RESTRICTION.getQName(), Arrays.asList(new QName[] { BASE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.EXTENSION.getQName(), Arrays.asList(new QName[] { BASE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.LIST.getQName(), Arrays.asList(new QName[] { ITEM_TYPE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ATTRIBUTE.getQName(), Arrays.asList(new QName[] { REF.qname(), TYPE.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ELEMENT.getQName(), Arrays.asList(new QName[] { REF.qname(), SUBSTITUTION_GROUP.qname(), TYPE.qname() }));
        qnameValuedAttributes.put(
                SchemaElements.GROUP.getQName(), Arrays.asList(new QName[] { REF.qname()}));
        qnameValuedAttributes.put(
                SchemaElements.ATTRIBUTE_GROUP.getQName(), Arrays.asList(new QName[] { REF.qname()}));
    }

} 
