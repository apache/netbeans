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
