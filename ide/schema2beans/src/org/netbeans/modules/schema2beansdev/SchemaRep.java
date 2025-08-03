/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.schema2beansdev;

import org.netbeans.modules.schema2beansdev.gen.JavaUtil;
import org.netbeans.modules.schema2beansdev.gen.XMLWriter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.netbeans.modules.schema2beans.*;

/**
 * This class will represent a schema for XML as a Java object.
 * (This implementation isn't complete, but does enough for my purposes
 * for now.)
 *
 * @author  cliffwd
 */
public class SchemaRep implements PrefixGuesser {
    public static boolean debug = false;

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    /*
     * The Schema Representation is internally made up of a tree of
     * Element Expressions (ElementExpr).
     */
    public abstract class ElementExpr {
        public abstract String getName();
        public abstract void writeDTD(StringBuffer out);

        // writeDTDName returns true if there were any named elements
        public abstract boolean writeDTDName(StringBuffer out);
        public abstract void writeXMLSchema(XMLWriter out) throws IOException;

        // validate checks to make sure everything is okay
        public abstract void validate();

        // optimize returns a replacement node.  null means delete.
        public abstract ElementExpr optimize();
        public abstract void readSchema(org.w3c.dom.Element node);

        // Return null for getContentName if this expr doesn't have one.
        public abstract String getContentName();
        protected ElementExpr parentExpr;
        protected void setParentExpr(ElementExpr ee) {
            parentExpr = ee;
            fullContentName = null;
        }
        public ElementExpr getParentExpr() {
            return parentExpr;
        }
        private String fullContentName = null;
        public String getFullContentName() {
            if (fullContentName == null) {
                String contentName = getContentName();
                if (parentExpr == null) {
                    if (contentName == null)
                        fullContentName = "/";
                    else
                        fullContentName = ("/" + contentName).intern();
                } else {
                    String parentFullContentName = parentExpr.getFullContentName();
                    if (contentName == null)
                        fullContentName = parentFullContentName;
                    else if (parentFullContentName == "/")
                        fullContentName = (parentFullContentName + contentName).intern();
                    else
                        fullContentName = (parentFullContentName + "/" + contentName).intern();
                }
            }
            return fullContentName;
        }
        protected String uniquifyFullContentName() {
            fullContentName = (getFullContentName()+"/#").intern();
            return fullContentName;
        }
    }

    // This class represents all elements (or nodes in the metadata)
    // that can have subelements.
    public abstract class ContainsSubElements extends ElementExpr {
        protected List subElements;  // List<ElementExpr>

        public ContainsSubElements() {
            subElements = new LinkedList();
        }

        public void addSubElement(ElementExpr subElement) {
            //System.out.println("Adding:"+subElement);
            subElement.setParentExpr(this);
            if (subElement instanceof Element) {
                String subElementFullContentName;
                boolean checkAgain;
                do {
                    checkAgain = false;
                    subElementFullContentName = subElement.getFullContentName();
                    Iterator it = subElements.iterator();
                    while (it.hasNext()) {
                        ElementExpr otherElement = (ElementExpr) it.next();
                        String otherElementFullContentName = otherElement.getFullContentName();
                        if (subElementFullContentName == otherElementFullContentName) {
                            if (debug)
                                System.out.println("Found duplicate fullContentName for "+otherElement.getName()+" : "+subElementFullContentName);
                            subElement.uniquifyFullContentName();
                            checkAgain = true;
                        }
                    }
                } while (checkAgain);
            }
            subElements.add(subElement);
        }

        public void addSubElement(List se) {
            //System.out.println("Adding: "+se+" to:"+toString());
            Iterator it = se.iterator();
            while (it.hasNext()) {
                addSubElement((ElementExpr) it.next());
            }
        }

        public Iterator subElementsIterator() {
            return subElements.iterator();
        }

        public ElementExpr findSubElement(String nodeName) {
            //System.out.println("Looking for subelement "+nodeName);
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                if (el.getName().equals(nodeName))
                    return el;
            }
            //System.out.println("Did not find it");
            return null;
        }

        public ElementExpr findSubElement(Class type) {
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                if (type.isAssignableFrom(el.getClass()))
                    return el;
            }
            return null;
        }

        /**
         * This will do an xpath like search.
         */
        public ElementExpr findSubElement(String[] nodeNames) {
            ContainsSubElements current = this;
            for (int i = 0; i < nodeNames.length; ++i) {
                ElementExpr ee = current.findSubElement(nodeNames[i]);
                if (ee == null)
                    return null;
                if (ee instanceof ContainsSubElements)
                    current = (ContainsSubElements) ee;
                else if (i+1 != nodeNames.length)
                    return null;
            }
            return current;
        }

        /**
         * Find all matching sub elements and put them into lst.
         */
        public void findAllSubElements(String name, List lst) {
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                if (el.getName().equals(name))
                    lst.add(el);
                if (el instanceof ContainsSubElements)
                    ((ContainsSubElements)el).findAllSubElements(name, lst);
            }
        }

        /**
         * Find all matching sub elements and put them into lst.
         */
        public void findAllSubElements(Class type, List lst) {
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                if (type.isAssignableFrom(el.getClass()))
                    lst.add(el);
                if (el instanceof ContainsSubElements)
                    ((ContainsSubElements)el).findAllSubElements(type, lst);
            }
        }

        public void validate() {
            //System.out.println("** validate: "+this);
            Map possibleSubElements = validSubElementTypeMap();
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr ee = (ElementExpr) it.next();
                if (!possibleSubElements.containsKey(ee.getClass())) {
                    throw new IllegalStateException(Common.getMessage("MSG_InvalidContents", toString(), ee.getName(), getFullContentName()));
                }
                ee.validate();
            }
        }

        public abstract Map validSubElementTypeMap();

        public void writeDTD(StringBuffer out) {
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                el.writeDTD(out);
            }
        }

        /**
         * Return the attributes as a string for the XML Schema.
         * null means that we have no attributes to add.
         */
        public String getAttributeString() {
            return null;
        }

        /**
         * If whitespace is used on the outside of this element.
         */
        public boolean compressWhiteSpaceOuter() {
            return false;
        }

        /**
         * If whitespace is used on the inside of this element.
         */
        public boolean compressWhiteSpaceInner() {
            return false;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            writeXMLSchema(out, true);
        }

        /**
         * If @printMyElement is false, then we only print subelements.
         */
        public void writeXMLSchema(XMLWriter out, boolean printMyElement) throws IOException {
            if (printMyElement) {
                //if (!compressWhiteSpaceOuter())
                //    out.indentRight();
                out.startTag(getXSDNamespace(), getName(), false);
                String attrs = getAttributeString();
                if (attrs != null)
                    out.write(attrs);
                out.finishStartTag(subElements.size() > 0, !compressWhiteSpaceInner());
                if (subElements.size() == 0)
                    return;
            }
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                el.writeXMLSchema(out);
            }
            if (printMyElement) {
                //if (!compressWhiteSpaceInner())
                //    out.indentLeft();
                out.endTag(!compressWhiteSpaceOuter());
            }
        }

        protected boolean writeDTDSubElementNames(StringBuffer out) {
            return writeDTDSubElementNames(out, true);
        }
        protected boolean writeDTDSubElementNames(StringBuffer out, boolean writeParens) {
            if (subElements.size() == 0)
                return false;
            boolean first = true;
            Iterator it = subElements.iterator();
            boolean hasNamedSubElements = false;
            // Put all of the DTD names of our subelements into subOut
            StringBuffer subOut = new StringBuffer();
            // Let each individual subelement do it's work in freshOut
            // (it's always empty at the top of the loop).
            StringBuffer freshOut = new StringBuffer();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                boolean anyNamed = el.writeDTDName(freshOut);
                if (anyNamed) {
                    hasNamedSubElements = true;
                    if (first)
                        first = false;
                    else
                        subOut.append(", ");	// NOI18N
                    subOut.append(freshOut.toString());
                    freshOut = new StringBuffer();
                }
            }
            if (!hasNamedSubElements)
                return false;
            if (writeParens && subElements.size() >= 2)
                out.append("(");	// NOI18N
            out.append(subOut);
            if (writeParens && subElements.size() >= 2)
                out.append(")");	// NOI18N
            return hasNamedSubElements;
        }

        // Inserts String's into @param list.
        public void findSubElementNames(List list) {
            Iterator it = subElements.iterator();
            StringBuffer freshOut = new StringBuffer();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                boolean anyNamed = el.writeDTDName(freshOut);
            }
        }

        public String toString() {
            String attributeString = getAttributeString();
            if (attributeString == null)
                return getName();
            else
                return getName()+attributeString;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ContainsSubElements))
                return false;
            ContainsSubElements e = (ContainsSubElements) o;
            //System.out.println("Checking for equals of ContainsSubElements");
            if (subElements.size() != e.subElements.size())
                return false;
            Iterator it = subElements.iterator();
            Iterator ite = e.subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                ElementExpr ele = (ElementExpr) ite.next();
                //System.out.println("el="+el);
                //System.out.println("ele="+ele);
                if (!el.equals(ele))
                    return false;
            }
            return true;
        }

        public int hashCode() {
            int result = 17;
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                result = 37*result + el.hashCode();
            }
            return result;
        }

        public ElementExpr optimize() {
            ListIterator it = subElements.listIterator();
            while (it.hasNext()) {
                ElementExpr el = (ElementExpr) it.next();
                ElementExpr result = el.optimize();
                //System.out.println("optimize: result="+result);
                if (result == null) {
                    el.setParentExpr(null);
                    it.remove();
                } else if (el != result)
                    it.set(result);
            }
            return this;
        }

        public String getJavaTypeName() {
            Iterator it = subElements.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof HasJavaTypeName) {
                    //System.out.println("Container: getJavaTypeName: o="+o);
                    return ((HasJavaTypeName)o).getJavaTypeName();
                }
            }
            return null;
        }
    }

    public interface HasJavaTypeName {
        public String getJavaTypeName();
    }

    public interface MinMaxOccurs {
        public String getMinOccurs();
        public String getMaxOccurs();
    }

    public interface CanRef {
        public boolean hasRef();
        /**
         * @return the name of the ref or null if !hasRef()
         */
        public String getRef();
        public ElementExpr getRefElementExpr();
    }


    public abstract class HasTypeName extends ContainsSubElements {
        private String typeNameLocalPart = null;
        private String typeNameNamespace = null;
        
        protected String setTypeName(String typeName) {
            if (typeName == null) {
                typeNameLocalPart = null;
                typeNameNamespace = null;
                return null;
            }
            typeName = normalizeTargetNamespace(typeName).intern();
            typeNameLocalPart = removePrefix(typeName).intern();
            String prefix = prefixOf(typeName);
            typeNameNamespace = getNamespaceURI(prefix);
            if (typeNameNamespace != null)
                typeNameNamespace = typeNameNamespace.intern();
            return typeName;
        }

        public String getTypeNameLocalPart() {
            return typeNameLocalPart;
        }
        
        public String getTypeNameNamespace() {
            return typeNameNamespace;
        }

        /**
         * Can return null.
         */
        public String getTypeName() {
            if (typeNameLocalPart == null)
                return null;
            String result = normalizeNamespace(typeNameNamespace, typeNameLocalPart);
            return result;
        }

    }
    
    // Top of an XML Schema
    public class SchemaNode extends ContainsSubElements {
        protected String targetNamespace;
        private Boolean elementFormQualifiedDefault = null;
        private Boolean attributeFormQualifiedDefault = null;

        public SchemaNode() {
        }

        public String getName() {
            return "schema";
        }

        public String getContentName() {
            return null;
        }

        public void setTargetNamespace(String tn) {
            targetNamespace = tn;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public boolean isElementFormQualified() {
            if (elementFormQualifiedDefault == null)
                return false;
            return elementFormQualifiedDefault.booleanValue();
        }

        public boolean isAttributeFormQualified() {
            if (attributeFormQualifiedDefault == null)
                return false;
            return attributeFormQualifiedDefault.booleanValue();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String tns = node.getAttribute("targetNamespace");	// NOI18N
            String efd = node.getAttribute("elementFormDefault");	// NOI18N
            String afd = node.getAttribute("attributeFormDefault");	// NOI18N
            if (tns != null && !"".equals(tns))
                targetNamespace = tns;
            if (efd != null)
                elementFormQualifiedDefault = Boolean.valueOf("qualified".equals(efd));
            SchemaRep.this.elementFormQualifiedDefault = isElementFormQualified();
            if (afd != null)
                attributeFormQualifiedDefault = Boolean.valueOf("qualified".equals(afd));
            SchemaRep.this.attributeFormQualifiedDefault = isAttributeFormQualified();
        }

        void merge(org.w3c.dom.Element node) {
            //System.out.println("merge: "+node);
            String tns = node.getAttribute("targetNamespace");	// NOI18N
            String efd = node.getAttribute("elementFormDefault");	// NOI18N
            String afd = node.getAttribute("attributeFormDefault");	// NOI18N
            if (targetNamespace == null)
                targetNamespace = tns;
            
            boolean value = "qualified".equals(efd);
            if (efd != null) {
                if (elementFormQualifiedDefault == null || elementFormQualifiedDefault.booleanValue())
                    elementFormQualifiedDefault = Boolean.valueOf(value);
            }
            SchemaRep.this.elementFormQualifiedDefault = value;
            
            value = "qualified".equals(afd);
            if (afd != null) {
                if (attributeFormQualifiedDefault == null || attributeFormQualifiedDefault.booleanValue())
                    attributeFormQualifiedDefault = Boolean.valueOf(value);
            }
            SchemaRep.this.attributeFormQualifiedDefault = value;
        }

        public void addSubElement(ElementExpr subElement) {
            if (subElement instanceof Element) {
                Element el = (Element) subElement;
                //System.out.println("SchemaNode.addSubElement: el="+el);
                if (el.isDefiningNewType()) {
                    if (debug)
                        System.out.println("SchemaNode new element type: name="+el.getElementName());
                    definedTypes.put(el.getElementName(), el);
                    definedTypesFull.put(canonicalQName(el.getElementNamespace(), el.getElementName()), el);
                }
            }
            if (subElement instanceof Attribute) {
                Attribute attr = (Attribute) subElement;
                //System.out.println("SchemaNode.addSubElement: attr="+attr);
                if (attr.isDefiningNewType()) {
                    if (debug)
                        System.out.println("SchemaNode new element type: attr="+attr);
                    definedAttributes.put(attr.getAttributeName(), attr);
                }
            }
            super.addSubElement(subElement);
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer(" xmlns:"+getXSDNamespace()+"='"+getNamespaceURI(getXSDNamespace())+"'");	// NOI18N
            if (targetNamespace != null)
                sb.append(" targetNamespace='"+getTargetNamespace()+"'");	// NOI18N
            if (documentNamespace != null) {
                sb.append(" xmlns='"+documentNamespace+"'");	// NOI18N
            }
            if (isElementFormQualified())
                sb.append(" elementFormDefault='qualified'");
            if (isAttributeFormQualified())
                sb.append(" attributeFormDefault='qualified'");
            return sb.toString();
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            out.startTag(getXSDNamespace(), getName(), getAttributeString());	// NOI18N
            out.cr();
            Iterator it = requiredPredefinedTypes.keySet().iterator();
            while (it.hasNext()) {
                String type = (String) it.next();
                //System.out.println("Required predefined type "+type);
                ElementExpr el = (ElementExpr) optionallyDefinedTypes.get(type);
                el.writeXMLSchema(out);
            }

            super.writeXMLSchema(out, false);
            out.endTag();	// NOI18N
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public Map validSubElementTypeMap() {
            return schemaValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SchemaNode))
                return false;
            SchemaNode el = (SchemaNode) o;
            if (targetNamespace == null) {
                if (el.targetNamespace != null)
                    return false;
            } else {
                if (!targetNamespace.equals(el.targetNamespace))
                    return false;
            }
            if (elementFormQualifiedDefault != el.elementFormQualifiedDefault)
                return false;
            if (attributeFormQualifiedDefault != el.attributeFormQualifiedDefault)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((targetNamespace == null) ? 0 : targetNamespace.hashCode());
            result = 37*result + (elementFormQualifiedDefault.booleanValue() ? 1 : 0);
            result = 37*result + (attributeFormQualifiedDefault.booleanValue() ? 1 : 0);
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map schemaValidSubElementTypeMap = null;
    static {
        schemaValidSubElementTypeMap = new HashMap();
        schemaValidSubElementTypeMap.put(Annotation.class, null);
        schemaValidSubElementTypeMap.put(SimpleType.class, null);
        schemaValidSubElementTypeMap.put(ComplexType.class, null);
        schemaValidSubElementTypeMap.put(Element.class, null);
        schemaValidSubElementTypeMap.put(Attribute.class, null);
        schemaValidSubElementTypeMap.put(AttributeGroup.class, null);
        schemaValidSubElementTypeMap.put(Include.class, null);
        schemaValidSubElementTypeMap.put(Import.class, null);
        schemaValidSubElementTypeMap.put(Group.class, null);
    }

    // See XML Schema complexType
    public class ComplexType extends HasTypeName implements HasJavaTypeName {
        //protected String typeName;
        private boolean mixed = false;
        private boolean abstractType = false;

        public ComplexType() {
        }

        public ComplexType(String typeName) {
            if (typeName != null && !typeName.equals("")) {
                typeName = setTypeName(typeName);
                putSchemaTypeDef(typeName, this);
            }
        }

        public String getName() {
            return "complexType";
        }

        public String getContentName() {
            return getTypeName();
        }

        public boolean isMixed() {
            return mixed;
        }

        public boolean isAbstract() {
            return abstractType;
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public String getAttributeString() {
            String result = "";
            if (getTypeName() != null)
                result += " name='"+getTypeName()+"'";
            if (mixed)
                result += " mixed='true'";
            if (abstractType)
                result += " abstract='true'";
            return result;
        }

        public Map validSubElementTypeMap() {
            return complexTypeValidSubElementTypeMap;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String elementType = node.getAttribute("name");	// NOI18N
            String myMixed = node.getAttribute("mixed");	// NOI18N
            String myAbstract = node.getAttribute("abstract");	// NOI18N
            ComplexType el = new ComplexType(elementType);
            if (myMixed != null && (myMixed.equals("true") || myMixed.equals("yes") || myMixed.equals("on")))
                el.mixed = true;
            if (myAbstract != null && (myAbstract.equals("true") || myAbstract.equals("yes") || myAbstract.equals("on")))
                el.abstractType = true;
            if (debug)
                System.out.println("Created complexType "+elementType);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ComplexType))
                return false;
            ComplexType el = (ComplexType) o;
            if ((getTypeName() == null) ? (el.getTypeName() == null) : getTypeName().equals(el.getTypeName()))
                return false;
            if (mixed != el.mixed)
                return false;
            if (abstractType != el.abstractType)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((getTypeName() == null) ? 0 : getTypeName().hashCode());
            result = 37*result + (mixed ? 1 : 0);
            result = 37*result + (abstractType ? 1 : 0);
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map complexTypeValidSubElementTypeMap = null;
    static {
        complexTypeValidSubElementTypeMap = new HashMap();
        complexTypeValidSubElementTypeMap.put(Annotation.class, null);
        complexTypeValidSubElementTypeMap.put(Choice.class, null);
        complexTypeValidSubElementTypeMap.put(Group.class, null);
        complexTypeValidSubElementTypeMap.put(Sequence.class, null);
        complexTypeValidSubElementTypeMap.put(All.class, null);
        complexTypeValidSubElementTypeMap.put(Attribute.class, null);
        complexTypeValidSubElementTypeMap.put(AttributeGroup.class, null);
        complexTypeValidSubElementTypeMap.put(AnyAttribute.class, null);
        complexTypeValidSubElementTypeMap.put(SimpleContent.class, null);
        complexTypeValidSubElementTypeMap.put(ComplexContent.class, null);
    }

    public class SimpleContent extends ContainsSubElements implements HasJavaTypeName {
        public SimpleContent() {
        }

        public String getName() {
            return "simpleContent";
        }

        public String getContentName() {
            return null;
        }

        public void validate() {
        }

        public ElementExpr optimize() {
            return this;
        }

        public void readSchema(org.w3c.dom.Element node) {
            SimpleContent el = new SimpleContent();
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public Map validSubElementTypeMap() {
            return simpleContentValidSubElementTypeMap;
        }
    }
    private static Map simpleContentValidSubElementTypeMap = null;
    static {
        simpleContentValidSubElementTypeMap = new HashMap();
        simpleContentValidSubElementTypeMap.put(Annotation.class, null);
        simpleContentValidSubElementTypeMap.put(Restriction.class, null);
        simpleContentValidSubElementTypeMap.put(Extension.class, null);
    }


    public class ComplexContent extends ContainsSubElements implements HasJavaTypeName {
        private boolean mixed;

        public ComplexContent() {
        }

        public String getName() {
            return "complexContent";
        }

        public String getContentName() {
            return null;
        }

        public void validate() {
        }

        public ElementExpr optimize() {
            return this;
        }

        public boolean isMixed() {
            return mixed;
        }

        public void readSchema(org.w3c.dom.Element node) {
            ComplexContent el = new ComplexContent();
            String myMixed = node.getAttribute("mixed");	// NOI18N
            if (myMixed != null && (myMixed.equals("true") || myMixed.equals("yes") || myMixed.equals("on")))
                el.mixed = true;
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public Map validSubElementTypeMap() {
            return complexContentValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ComplexContent))
                return false;
            ComplexContent el = (ComplexContent) o;
            if (mixed != el.mixed)
                return false;
            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + (mixed ? 0 : 1);
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map complexContentValidSubElementTypeMap = null;
    static {
        complexContentValidSubElementTypeMap = new HashMap();
        complexContentValidSubElementTypeMap.put(Annotation.class, null);
        complexContentValidSubElementTypeMap.put(Restriction.class, null);
        complexContentValidSubElementTypeMap.put(Extension.class, null);
    }

    // See XML Schema simpleType
    public class SimpleType extends HasTypeName implements HasJavaTypeName {
        //private String typeName = null;
        private String javaTypeName = null;

        public SimpleType(String tn) {
            //System.out.println("SimpleType1: tn="+tn);
            if (tn != null && !tn.equals("")) {
                String typeName = normalizeTargetNamespace(tn).intern();
                ElementExpr previousDef = getSchemaTypeDef(typeName);
                if (previousDef instanceof SimpleType && (((SimpleType)previousDef).javaTypeName != null || ((SimpleType)previousDef).subElements.size() > 0)) {
                    // The previous definition has more information.  Keep it.
                    System.out.println("Keeping schemaTypeDefs1 for "+previousDef);
                } else {
                    if (previousDef != null)
                        System.out.println("!!! Overwriting schemaTypeDefs1 ("+previousDef+") for "+this);
                    typeName = setTypeName(typeName);
                    putSchemaTypeDef(typeName, this);
                }
            }
        }
        
        /**
         * @param tn is the type
         * @param javaTypeName is a java type to associate with @param tn
         *    and should be linearly dependent.
         */
        public SimpleType(String tn, String javaTypeName) {
            super();
            //System.out.println("SimpleType2: tn="+tn+" javaTypeName="+javaTypeName);
            String typeName = setTypeName(tn);
            this.javaTypeName = (javaTypeName == null) ? null : javaTypeName.intern();
            /*
            if (schemaTypeDefs.get(typeName) != null)
                System.out.println("!!! Overwriting schemaTypeDefs2 for "+this);
            */
            putSchemaTypeDef(typeName, this);
        }

        public SimpleType(String tn, Restriction restrict) {
            super();
            //System.out.println("SimpleType3: tn="+tn+" restrict="+restrict);
            String typeName = setTypeName(tn);
            this.addSubElement(restrict);
            /*
            if (schemaTypeDefs.get(typeName) != null)
                System.out.println("!!! Overwriting schemaTypeDefs3 for "+this);
            */
            putSchemaTypeDef(typeName, this);
        }

        public String getName() {
            return "simpleType";
        }

        public String getContentName() {
            return getTypeName();
        }

        // This may return null
        public String getJavaTypeName() {
            if (javaTypeName != null)
                return javaTypeName;
            return super.getJavaTypeName();
        }

        public String getAttributeString() {
            if (getTypeName() == null)
                return null;
            return (" name='"+getTypeName()+"'");
        }

        public boolean compressWhiteSpaceInner() {
            if (subElements.size() == 0)
                return true;
            ElementExpr subElement = (ElementExpr) subElementsIterator().next();
            if (subElement instanceof ContainsSubElements)
                return ((ContainsSubElements)subElement).compressWhiteSpaceOuter();
            return true;
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public void readSchema(org.w3c.dom.Element node) {
            String elementType = node.getAttribute("name");	// NOI18N
            SimpleType el = new SimpleType(elementType);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return simpleTypeValidSubElementTypeMap;
        }

        public String toString() {
            if (javaTypeName == null) {
                if (getTypeName() == null)
                    return "simpleType (not named)";	// NOI18N
                else
                    return "simpleType "+getTypeName();	// NOI18N
            } else
                return "simpleType "+getTypeName()+" "+javaTypeName;	// NOI18N
        }

        public boolean equals(Object o) {
            if (!(o instanceof SimpleType))
                return false;
            SimpleType el = (SimpleType) o;
            if ((getTypeName() == null) ? (el.getTypeName() == null) : getTypeName().equals(el.getTypeName()))
                return false;
            // javaTypeName ought to be redundant to typeName
            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            if (getTypeName() != null)
                result = 37*result + getTypeName().hashCode();
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map simpleTypeValidSubElementTypeMap = null;
    static {
        simpleTypeValidSubElementTypeMap = new HashMap();
        simpleTypeValidSubElementTypeMap.put(Annotation.class, null);
        simpleTypeValidSubElementTypeMap.put(Restriction.class, null);
        simpleTypeValidSubElementTypeMap.put(ListElement.class, null);
        simpleTypeValidSubElementTypeMap.put(UnionType.class, null);
    }

    // See XML Schema simpleType
    public class UnionType extends ContainsSubElements {
        private String typeName = null;
        private String memberTypes = null;

        public UnionType(String tn, String memberTypes) {
            //System.out.println("UnionType1: tn="+tn);
            if (tn != null && !tn.equals("")) {
                typeName = normalizeTargetNamespace(tn).intern();
            }
			this.memberTypes = memberTypes;
        }

		public String getMemberTypes() {
			return memberTypes;
		}

        public ElementExpr[] getMemberTypeElements() {
            if (memberTypes == null || memberTypes.trim().length() == 0)
                return null;
            ArrayList mlist = new ArrayList();
            String[] members = memberTypes.trim().split(" ");
            for (int i=0; i < members.length; i++) {
                if (members[i].length() == 0)
                    continue;
                SchemaRep.ElementExpr ee = getSchemaTypeDef(members[i]);
                if (ee != null)
                    mlist.add(ee);
            }

            ElementExpr[] memberList = new ElementExpr[mlist.size()];
            memberList = (ElementExpr[]) mlist.toArray(memberList);
            return memberList;
        }

        public String getName() {
            return "union";
        }

        public String getContentName() {
            return typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getAttributeString() {
            if (memberTypes == null)
                return null;
            return (" memberTypes='"+memberTypes+"'");
        }

        public boolean compressWhiteSpaceInner() {
            if (subElements.size() == 0)
                return true;
            ElementExpr subElement = (ElementExpr) subElementsIterator().next();
            if (subElement instanceof ContainsSubElements)
                return ((ContainsSubElements)subElement).compressWhiteSpaceOuter();
            return true;
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public void readSchema(org.w3c.dom.Element node) {
            String memberTypes = node.getAttribute("memberTypes");	// NOI18N
            String typeName = "_union";
            org.w3c.dom.Node parent = node.getParentNode();
            if (parent instanceof org.w3c.dom.Element) {
                do {
                    org.w3c.dom.Element parentEl = (org.w3c.dom.Element) parent;
                    if (parent != null) {
                        String name = parentEl.getAttribute("name");
                        if (name != null) {
                            typeName = name + typeName;
                            break;
                        } else {
                            // Only simpletypes have unions..So
                            typeName = "_simpleType" + typeName;
                        }
                        parent = parent.getParentNode();
                    } else
                        break;
                } while (parent != null);
            }
            UnionType el = new UnionType(typeName, memberTypes);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return unionTypeValidSubElementTypeMap;
        }

        public String toString() {
			if (typeName == null)
				return "unionType (not named)";	// NOI18N
			else
				return "unionType "+typeName;	// NOI18N
        }

        public boolean equals(Object o) {
            if (!(o instanceof UnionType))
                return false;
            UnionType el = (UnionType) o;
            if (typeName != el.typeName)
                return false;
            if (memberTypes != el.memberTypes)
                return false;
            // javaTypeName ought to be redundant to typeName
            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + memberTypes.hashCode();
            result = 37*result + typeName.hashCode();
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map unionTypeValidSubElementTypeMap = null;
    static {
        unionTypeValidSubElementTypeMap = new HashMap();
        unionTypeValidSubElementTypeMap.put(Annotation.class, null);
        unionTypeValidSubElementTypeMap.put(SimpleType.class, null);
    }

    public interface EncodingStyle {
    }

    public class HexBinary extends SimpleType implements EncodingStyle {
        public HexBinary() {
            super(getXSDNamespace()+":hexBinary", "byte[]");
        }

        public String toString() {
            return "hexBinary";
        }
    }

    public class Base64Binary extends SimpleType implements EncodingStyle {
        public Base64Binary() {
            super(getXSDNamespace()+":base64Binary", "byte[]");
        }

        public String toString() {
            return "base64Binary";
        }
    }

    // See XML Schema restriction (usually underneath simpleType)
    public class Restriction extends ContainsSubElements implements HasJavaTypeName {
        protected String base;
        public Restriction() {
        }

        public Restriction(String base) {
            setBase(base);
        }

        public String getName() {
            return "restriction";
        }

        public String getContentName() {
            return null;
        }

        public void setBase(String b) {
            if (b == null) {
                base = null;
                return;
            }
            base = normalizeDocumentNamespace(b).intern();
        }

        public String getBase() {
            return base;
        }

        public String getJavaTypeName() {
            return schemaTypeToJavaType(base);
        }

        public boolean compressWhiteSpaceOuter() {
            return subElements.size() == 0;
        }

        public boolean compressWhiteSpaceInner() {
            return subElements.size() == 0;
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public String getAttributeString() {
            if (base == null)
                return null;
            return (" base='"+getBase()+"'");
        }

        public void readSchema(org.w3c.dom.Element node) {
            String base = node.getAttribute("base");	// NOI18N
            Restriction el = new Restriction(base);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return restrictionValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Restriction))
                return false;
            Restriction el = (Restriction) o;
            if (base == null) {
                if (el.base != null)
                    return false;
            } else {
                if (!base.equals(el.base))
                    return false;
            }

            return super.equals(el);
        }

        public String toString() {
            Iterator subElements = subElementsIterator();
            StringBuffer sb = null;
            while (subElements.hasNext()) {
                ElementExpr ee = (ElementExpr) subElements.next();
                if (ee instanceof RestrictionType) {
                    if (sb == null)
                        sb = new StringBuffer();
                    else
                        sb.append(", ");
                    RestrictionType restrictionType = (RestrictionType) ee;
                    sb.append(restrictionType.toString());
                }
            }
/*
            if (sb == null)
                return super.toString();
            else
*/
            if (sb != null)
                return sb.toString();
            else
                return "";
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((base == null) ? 0: base.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map restrictionValidSubElementTypeMap = null;
    static {
        restrictionValidSubElementTypeMap = new HashMap();
        restrictionValidSubElementTypeMap.put(Annotation.class, null);
        restrictionValidSubElementTypeMap.put(SimpleType.class, null);
        restrictionValidSubElementTypeMap.put(All.class, null);
        restrictionValidSubElementTypeMap.put(Choice.class, null);
        restrictionValidSubElementTypeMap.put(Sequence.class, null);
        restrictionValidSubElementTypeMap.put(Group.class, null);
        restrictionValidSubElementTypeMap.put(Attribute.class, null);
        restrictionValidSubElementTypeMap.put(AttributeGroup.class, null);
        restrictionValidSubElementTypeMap.put(AnyAttribute.class, null);
        restrictionValidSubElementTypeMap.put(MinExclusive.class, null);
        restrictionValidSubElementTypeMap.put(MaxExclusive.class, null);
        restrictionValidSubElementTypeMap.put(Enumeration.class, null);
        restrictionValidSubElementTypeMap.put(Pattern.class, null);
        restrictionValidSubElementTypeMap.put(MinLength.class, null);
        restrictionValidSubElementTypeMap.put(MaxLength.class, null);
        restrictionValidSubElementTypeMap.put(TotalDigits.class, null);
        restrictionValidSubElementTypeMap.put(MinInclusive.class, null);
        restrictionValidSubElementTypeMap.put(MaxInclusive.class, null);
        restrictionValidSubElementTypeMap.put(FractionDigits.class, null);
        restrictionValidSubElementTypeMap.put(Length.class, null);
        restrictionValidSubElementTypeMap.put(WhiteSpace.class, null);
    }

    public class Extension extends ContainsSubElements implements HasJavaTypeName {
        protected String base;
        public Extension() {
        }

        public Extension(String base) {
            setBase(base);
        }

        public String getContentName() {
            return null;
        }

        public String getName() {
            return "extension";
        }

        public void setBase(String b) {
            base = normalizeDocumentNamespace(b);
        }

        public String getBase() {
            return base;
        }

        public String getJavaTypeName() {
            return schemaTypeToJavaType(base);
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public String getAttributeString() {
            if (base == null)
                return null;
            return (" base='"+getBase()+"'");
        }

        public void readSchema(org.w3c.dom.Element node) {
            String base = node.getAttribute("base");	// NOI18N
            Extension el = new Extension(base);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return extensionValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Extension))
                return false;
            Extension el = (Extension) o;
            if (base == null) {
                if (el.base != null)
                    return false;
            } else {
                if (!base.equals(el.base))
                    return false;
            }

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((base == null) ? 0: base.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map extensionValidSubElementTypeMap = null;
    static {
        extensionValidSubElementTypeMap = new HashMap();
        extensionValidSubElementTypeMap.put(Annotation.class, null);
        extensionValidSubElementTypeMap.put(Attribute.class, null);
        extensionValidSubElementTypeMap.put(AttributeGroup.class, null);
        extensionValidSubElementTypeMap.put(AnyAttribute.class, null);
        extensionValidSubElementTypeMap.put(Choice.class, null);
        extensionValidSubElementTypeMap.put(Group.class, null);
        extensionValidSubElementTypeMap.put(Sequence.class, null);
        extensionValidSubElementTypeMap.put(All.class, null);
    }

    public class ListElement extends ContainsSubElements {
        protected String itemType;
        public ListElement() {
        }

        public ListElement(String itemType) {
            this.itemType = itemType;
        }

        public String getContentName() {
            return null;
        }

        public String getName() {
            return "list";
        }

        public void setItemType(String b) {
            itemType = b;
        }

        public String getItemType() {
            return itemType;
        }

        public String getJavaTypeName() {
            return schemaTypeToJavaType(itemType);
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public String getAttributeString() {
            if (itemType == null)
                return null;
            return (" itemType='"+getItemType()+"'");
        }

        public void readSchema(org.w3c.dom.Element node) {
            String itemType = node.getAttribute("itemType");	// NOI18N
            ListElement el = new ListElement(itemType);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return listValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ListElement))
                return false;
            ListElement el = (ListElement) o;
            if (itemType == null) {
                if (el.itemType != null)
                    return false;
            } else {
                if (!itemType.equals(el.itemType))
                    return false;
            }

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((itemType == null) ? 0: itemType.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map listValidSubElementTypeMap = null;
    static {
        listValidSubElementTypeMap = new HashMap();
        listValidSubElementTypeMap.put(Annotation.class, null);
    }

    public abstract class RestrictionType extends ContainsSubElements {
        protected String value;

        public RestrictionType(String value) {
            super();
            this.value = (value == null) ? null : value.intern();
        }

        public String getContentName() {
            return null;
        }

        public String getValue() {
            return value;
        }

        public void writeDTD(StringBuffer out) {
            // Has no bearing on a DTD
        }
        public boolean writeDTDName(StringBuffer out) {
            // Has no bearing on a DTD
            return false;
        }

        public void validate() {
        }

        public ElementExpr optimize() {
            return this;
        }

        public Map validSubElementTypeMap() {
            return restrictionTypeValidSubElementTypeMap;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            out.startTag(getXSDNamespace(), getName(), false);
            out.write(" value='");
            XMLUtil.printXML(out, value);
            out.write("'");
            out.finishStartTag(false, true);
        }

        public void readSchema(org.w3c.dom.Element node) {
            String myValue = node.getAttribute("value");	// NOI18N
            RestrictionType el = newInstance(myValue);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        protected abstract RestrictionType newInstance(String value);

        /*
        public void genRestriction(Writer out, String var, String type, String failVar) throws IOException {
            out.write("// FIXME "+getClass()+"\n");
        }
        */

        public String toString() {
            return getName()+" ("+value+")";
        }
    }
    private static Map restrictionTypeValidSubElementTypeMap = null;
    static {
        restrictionTypeValidSubElementTypeMap = new HashMap();
        restrictionTypeValidSubElementTypeMap.put(Annotation.class, null);
    }

    public class MaxExclusive extends RestrictionType implements DataTypeRestriction {
        public MaxExclusive(String value) {
            super(value);
        }

        public String getName() {
            return "maxExclusive";
        }

        protected RestrictionType newInstance(String value) {
            return new MaxExclusive(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" >= 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" < 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class MinExclusive extends RestrictionType implements DataTypeRestriction {
        public MinExclusive(String value) {
            super(value);
        }

        public String getName() {
            return "minExclusive";
        }

        protected RestrictionType newInstance(String value) {
            return new MinExclusive(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" <= 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" > 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class Enumeration extends RestrictionType implements DataEnumRestriction {
        public Enumeration(String value) {
            super(value);
        }

        public String getName() {
            return "enumeration";
        }

        protected RestrictionType newInstance(String value) {
            return new Enumeration(value);
        }

        public void genRestriction(Writer out, String type) throws IOException {
            out.write(JavaUtil.instanceFrom(type, value));
        }
    }

    public class Pattern extends RestrictionType implements DataTypeRestriction {
        public Pattern(String value) {
            super(value);
        }

        public String getName() {
            return "pattern";
        }

        protected RestrictionType newInstance(String value) {
            return new Pattern(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if (!("+JavaUtil.typeToString(type, var)+").matches("+xsdRegExpToJava(JavaUtil.instanceFrom("java.lang.String", value))+")) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if (("+JavaUtil.typeToString(type, var)+").matches("+xsdRegExpToJava(JavaUtil.instanceFrom("java.lang.String", value))+")) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }

        // this is imperfect xsd regexp to java regexp translation for common cases
        private String xsdRegExpToJava(String xsd) {
            String fixed = xsd;
            fixed =fixed.replaceAll(java.util.regex.Pattern.quote("\\\\i-[:]"), // NOI18N
                    Matcher.quoteReplacement("[_A-Za-z]")); // NOI18N
            fixed =fixed.replaceAll(java.util.regex.Pattern.quote("\\\\i"), // NOI18N
                    Matcher.quoteReplacement("[_:A-Za-z]")); // NOI18N
            fixed = fixed.replaceAll(java.util.regex.Pattern.quote("\\\\c-[:]"), // NOI18N
                    Matcher.quoteReplacement("[-._A-Za-z0-9]")); // NOI18N
            fixed = fixed.replaceAll(java.util.regex.Pattern.quote("\\\\c"), // NOI18N
                    Matcher.quoteReplacement("[-._:A-Za-z0-9]")); // NOI18N
            fixed =fixed.replaceAll(java.util.regex.Pattern.quote("\\\\I"), // NOI18N
                    Matcher.quoteReplacement("[^_:A-Za-z]")); // NOI18N
            fixed = fixed.replaceAll(java.util.regex.Pattern.quote("\\\\C"), // NOI18N
                    Matcher.quoteReplacement("[^-._:A-Za-z0-9]")); // NOI18N
            return fixed;
        }
    }

    public class MinLength extends RestrictionType implements DataTypeRestriction {
        public MinLength(String value) {
            super(value);
        }

        public String getName() {
            return "minLength";
        }

        protected RestrictionType newInstance(String value) {
            return new MinLength(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if (("+JavaUtil.typeToString(type, var)+").length() < "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if (("+JavaUtil.typeToString(type, var)+").length() >= "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class MaxLength extends RestrictionType implements DataTypeRestriction {
        public MaxLength(String value) {
            super(value);
        }

        public String getName() {
            return "maxLength";
        }

        protected RestrictionType newInstance(String value) {
            return new MaxLength(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if (("+JavaUtil.typeToString(type, var)+").length() > "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if (("+JavaUtil.typeToString(type, var)+").length() <= "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class TotalDigits extends RestrictionType implements DataTypeRestriction {
        public TotalDigits(String value) {
            super(value);
        }

        public String getName() {
            return "totalDigits";
        }

        protected RestrictionType newInstance(String value) {
            return new TotalDigits(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("{\n");
                out.write("String _tmp = "+JavaUtil.typeToString(type, var)+";\n");
                out.write("int digitCount = 0;\n");
                out.write("for (int _index1 = 0; _index1 < _tmp.length(); ++_index1) {\n");
                out.write("if (Character.isDigit(_tmp.charAt(_index1))) {\n");
                out.write("++digitCount;\n");
                out.write("if (digitCount > "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("break;\n");
                out.write("}\n");
                out.write("}\n");
                out.write("}\n");
                out.write("}\n");
            } else {
                out.write("{\n");
                out.write("String _tmp = "+JavaUtil.typeToString(type, var)+";\n");
                out.write("int digitCount = 0;\n");
                out.write("for (int _index1 = 0; _index1 < _tmp.length(); ++_index1) {\n");
                out.write("if (Character.isDigit(_tmp.charAt(_index1))) {\n");
                out.write("++digitCount;\n");
                out.write("}\n");
                out.write("}\n");
                out.write("if (digitCount <= "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
                out.write("}\n");
            }
        }
    }

    public class MinInclusive extends RestrictionType implements DataTypeRestriction {
        public MinInclusive(String value) {
            super(value);
        }

        public String getName() {
            return "minInclusive";
        }

        protected RestrictionType newInstance(String value) {
            return new MinInclusive(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" < 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" >= 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class MaxInclusive extends RestrictionType implements DataTypeRestriction {
        public MaxInclusive(String value) {
            super(value);
        }

        public String getName() {
            return "maxInclusive";
        }

        protected RestrictionType newInstance(String value) {
            return new MaxInclusive(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" > 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+JavaUtil.compareToText(var, type, value)+" <= 0) {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class FractionDigits extends RestrictionType implements DataTypeRestriction {
        public FractionDigits(String value) {
            super(value);
        }

        public String getName() {
            return "fractionDigits";
        }

        protected RestrictionType newInstance(String value) {
            return new FractionDigits(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("{\n");
                out.write("String _tmp = "+JavaUtil.typeToString(type, var)+";\n");
                out.write("int dotPos = _tmp.indexOf('.');\n");
                out.write("if (dotPos >= 0) {\n");
                out.write("_tmp = _tmp.substring(dotPos+1, _tmp.length());\n");
                out.write("int digitCount = 0;\n");
                out.write("for (int _index1 = 0; _index1 < _tmp.length(); ++_index1) {\n");
                out.write("if (Character.isDigit(_tmp.charAt(_index1))) {\n");
                out.write("++digitCount;\n");
                out.write("if (digitCount > "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("break;\n");
                out.write("}\n");
                out.write("}\n");
                out.write("}\n");
                out.write("}\n");
                out.write("}\n");
            } else {
                out.write("{\n");
                out.write("String _tmp = "+JavaUtil.typeToString(type, var)+";\n");
                out.write("int dotPos = _tmp.indexOf('.');\n");
                out.write("if (dotPos >= 0) {\n");
                out.write("_tmp = _tmp.substring(dotPos+1, _tmp.length());\n");
                out.write("int digitCount = 0;\n");
                out.write("for (int _index1 = 0; _index1 < _tmp.length(); ++_index1) {\n");
                out.write("if (Character.isDigit(_tmp.charAt(_index1))) {\n");
                out.write("++digitCount;\n");
                out.write("}\n");
                out.write("}\n");
                out.write("if (digitCount <= "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
                out.write("}\n");
                out.write("else\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class Length extends RestrictionType implements DataTypeRestriction {
        public Length(String value) {
            super(value);
        }

        public String getName() {
            return "length";
        }

        protected RestrictionType newInstance(String value) {
            return new Length(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            if (!passCheck) {
                out.write("if ("+JavaUtil.typeToString(type, var)+".length() != "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+JavaUtil.typeToString(type, var)+".length() == "+value+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
    }

    public class WhiteSpace extends RestrictionType implements DataTypeRestriction {
        public WhiteSpace(String value) {
            super(value);
        }

        public String getName() {
            return "whiteSpace";
        }

        public boolean isPreserve() {
            return "preserve" == value;
        }

        public boolean isReplace() {
            return "replace" == value;
        }

        public boolean isCollapse() {
            return "collapse" == value;
        }

        public void validate() {
            super.validate();
            if (value == null || value.equals(""))
                throw new IllegalStateException(Common.getMessage("MSG_InvalidWhiteSpaceValue", value));
            if (!(value == "preserve" || value == "replace" || value == "collapse"))
                throw new IllegalStateException(Common.getMessage("MSG_InvalidWhiteSpaceValue", value));
        }

        protected RestrictionType newInstance(String value) {
            return new WhiteSpace(value);
        }

        public void genRestriction(Writer out, String var, String type, String failVar, boolean passCheck) throws IOException {
            // Does not cause verify changes
            out.write("// has whitespace restriction\n");
        }
    }

    // the parent class for some others
    public abstract class ElementInformationItem extends ContainsSubElements {
        private String id;
        private String name;

        public ElementInformationItem() {
        }

        public ElementInformationItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getContentName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getElementName() {
            return name;
        }

        public void setElementName(String name) {
            this.name = name;
        }

        public boolean writeDTDName(StringBuffer out) {
            boolean hasNamedSubElements = writeDTDSubElementNames(out, true);
            return hasNamedSubElements;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (name != null)
                sb.append(" name='"+name+"'");
            if (id != null)
                sb.append(" id='"+id+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String id = node.getAttribute("id");	// NOI18N
            String name = node.getAttribute("name");	// NOI18N
            ElementInformationItem el = newInstance();
            pushCurrent(el);
            if (id != null && !id.equals(""))
                el.setId(id);
            if (name != null && !name.equals(""))
                el.setElementName(name);
            read(node);
            popCurrent();
        }

        protected abstract ElementInformationItem newInstance();

        public boolean equals(Object o) {
            if (!(o instanceof ElementInformationItem))
                return false;
            ElementInformationItem el = (ElementInformationItem) o;
            if (id != el.id)
                return false;
            if (name != el.name)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((id == null) ? 0 : id.hashCode());
            result = 37*result + ((name == null) ? 0 : name.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }

    public class Key extends ElementInformationItem {
        public Key() {
        }

        public String getName() {
            return "key";
        }

        public Map validSubElementTypeMap() {
            return keyValidSubElementTypeMap;
        }

        protected ElementInformationItem newInstance() {
            return new Key();
        }
    }
    private static Map keyValidSubElementTypeMap = null;
    static {
        keyValidSubElementTypeMap = new HashMap();
        keyValidSubElementTypeMap.put(Annotation.class, null);
        keyValidSubElementTypeMap.put(Selector.class, null);
        keyValidSubElementTypeMap.put(Field.class, null);
    }

    public class Unique extends ElementInformationItem {
        public Unique() {
        }

        public String getName() {
            return "unique";
        }

        public Map validSubElementTypeMap() {
            return uniqueValidSubElementTypeMap;
        }

        protected ElementInformationItem newInstance() {
            return new Unique();
        }
    }
    private static Map uniqueValidSubElementTypeMap = null;
    static {
        uniqueValidSubElementTypeMap = new HashMap();
        uniqueValidSubElementTypeMap.put(Annotation.class, null);
        uniqueValidSubElementTypeMap.put(Selector.class, null);
        uniqueValidSubElementTypeMap.put(Field.class, null);
    }

    public class KeyRef extends ElementInformationItem {
        private String refer;

        public KeyRef() {
        }

        public String getName() {
            return "keyref";
        }

        public void setRefer(String refer) {
            this.refer = refer;
        }

        public String getRefer() {
            return refer;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer(super.getAttributeString());
            if (refer != null)
                sb.append(" refer='"+refer+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String id = node.getAttribute("id");	// NOI18N
            String name = node.getAttribute("name");	// NOI18N
            String refer = node.getAttribute("refer");	// NOI18N
            KeyRef el = new KeyRef();
            pushCurrent(el);
            if (id != null && !id.equals(""))
                el.setId(id);
            if (name != null && !name.equals(""))
                el.setElementName(name);
            if (refer != null && !refer.equals(""))
                el.setRefer(refer);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return keyrefValidSubElementTypeMap;
        }

        protected ElementInformationItem newInstance() {
            return new KeyRef();
        }
    }
    private static Map keyrefValidSubElementTypeMap = null;
    static {
        keyrefValidSubElementTypeMap = new HashMap();
        keyrefValidSubElementTypeMap.put(Annotation.class, null);
        keyrefValidSubElementTypeMap.put(Selector.class, null);
        keyrefValidSubElementTypeMap.put(Field.class, null);
    }

    public abstract class SelectorOrField extends ContainsSubElements {
        private String id;
        private String xpath;

        public SelectorOrField() {
        }

        public String getContentName() {
            return null;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getXPath() {
            return xpath;
        }

        public void setXPath(String xpath) {
            this.xpath = xpath;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (xpath != null)
                sb.append(" xpath='"+xpath+"'");
            if (id != null)
                sb.append(" id='"+id+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String id = node.getAttribute("id");	// NOI18N
            String xpath = node.getAttribute("xpath");	// NOI18N
            SelectorOrField el = newInstance();
            pushCurrent(el);
            if (id != null && !id.equals(""))
                el.setId(id);
            if (xpath != null && !xpath.equals(""))
                el.setXPath(xpath);
            read(node);
            popCurrent();
        }

        protected abstract SelectorOrField newInstance();

        public void writeDTD(StringBuffer out) {
            // ?
        }

        public boolean writeDTDName(StringBuffer out) {
            // ?
            return false;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SelectorOrField))
                return false;
            SelectorOrField el = (SelectorOrField) o;
            if (id == null) {
                if (el.id != null)
                    return false;
            } else if (id != el.id)
                return false;
            if (xpath == null) {
                if (el.xpath != null)
                    return false;
            } else if (xpath != el.xpath)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((id == null) ? 0 : id.hashCode());
            result = 37*result + ((xpath == null) ? 0 : xpath.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }

    public class Selector extends SelectorOrField {
        public Selector() {
        }

        public String getName() {
            return "selector";
        }

        public Map validSubElementTypeMap() {
            return selectorValidSubElementTypeMap;
        }

        protected SelectorOrField newInstance() {
            return new Selector();
        }
    }
    private static Map selectorValidSubElementTypeMap = null;
    static {
        selectorValidSubElementTypeMap = new HashMap();
        selectorValidSubElementTypeMap.put(Annotation.class, null);
    }

    public class Field extends SelectorOrField {
        public Field() {
        }

        public String getName() {
            return "field";
        }

        public Map validSubElementTypeMap() {
            return fieldValidSubElementTypeMap;
        }

        protected SelectorOrField newInstance() {
            return new Field();
        }
    }
    private static Map fieldValidSubElementTypeMap = null;
    static {
        fieldValidSubElementTypeMap = new HashMap();
        fieldValidSubElementTypeMap.put(Annotation.class, null);
    }

    public class Include extends ElementExpr {
        private String schemaLocation;

        public Include(String schemaLocation) {
            this.schemaLocation = schemaLocation;
        }

        public String getContentName() {
            return null;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String schemaLocation = node.getAttribute("schemaLocation");	// NOI18N
            if (includedAlready.containsKey(schemaLocation))
                return;
            includedAlready.put(schemaLocation, null);

            //System.out.println("Attempting to include "+schemaLocation);
            ParserSchemaState oldState = new ParserSchemaState();
            try {
                readSchemaFromLocation(schemaLocation, true);
            } catch (org.xml.sax.SAXException e) {
                throw new Schema2BeansRuntimeException(Common.getMessage("MSG_FailedToParse", schemaLocation), e);
            } catch (java.io.IOException e) {
                throw new Schema2BeansRuntimeException(Common.getMessage("MSG_FailedToParse", schemaLocation), e);
            } finally {
                oldState.reload();
            }
            //System.out.println("Finished reading include\n");
            /*
              if (oldNamespaceTable != null)
              namespaceTable = oldNamespaceTable;
            */
        }

        public ElementExpr optimize() {
            return this;
        }

        public void validate() {
        }

        public String getName() {
            return "include";
        }

        public void writeDTD(StringBuffer out) {
            // ?
        }

        public boolean writeDTDName(StringBuffer out) {
            // ?
            return false;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            out.startTag(getXSDNamespace(), getName(), false);
            if (schemaLocation != null) {
                out.write(" schemaLocation='");
                out.write(schemaLocation);
                out.write("'");
            }
            out.finishStartTag(false, true);
        }

        public String toString() {
            if (schemaLocation == null)
                return getName();
            else
                return getName()+" schemaLocation="+schemaLocation;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Include))
                return false;
            Include el = (Include) o;
            if (schemaLocation == null) {
                if (el.schemaLocation != null)
                    return false;
            } else if (!schemaLocation.equals(el.schemaLocation))
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((schemaLocation == null) ? 0 : schemaLocation.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }

    public class Import extends ElementExpr {
        private String theNamespace;
        private String schemaLocation;

        public Import() {
        }

        public String getContentName() {
            return null;
        }

        public String getNamespace() {
            return theNamespace;
        }

        public void setNamespace(String namespace) {
            theNamespace = namespace;
        }

        public String getSchemaLocation() {
            return schemaLocation;
        }

        public void setSchemaLocation(String schemaLocation) {
            this.schemaLocation = schemaLocation;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String namespace = node.getAttribute("namespace");	// NOI18N
            String schemaLocation = node.getAttribute("schemaLocation");	// NOI18N
            //System.out.println("Attempting to import "+schemaLocation);
            ParserSchemaState oldState = new ParserSchemaState();
            if (namespace != null && !namespace.equals(""))
                targetNamespace = namespace;
            try {
                readSchemaFromLocation(schemaLocation, false);
            } catch (org.xml.sax.SAXException e) {
                throw new Schema2BeansRuntimeException(Common.getMessage("MSG_FailedToParse", schemaLocation), e);
            } catch (java.io.IOException e) {
                throw new Schema2BeansRuntimeException(Common.getMessage("MSG_FailedToParse", schemaLocation), e);
            } finally {
                oldState.reload();
            }
            //System.out.println("Finished reading import\n");
        }

        public ElementExpr optimize() {
            return this;
        }

        public void validate() {
        }

        public String getName() {
            return "import";
        }

        public void writeDTD(StringBuffer out) {
            // ?
        }

        public boolean writeDTDName(StringBuffer out) {
            // ?
            return false;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            out.startTag(getXSDNamespace(), getName(), false);
            if (theNamespace != null) {
                out.write(" theNamespace='");
                out.write(theNamespace);
                out.write("'");
            }
            if (schemaLocation != null) {
                out.write(" schemaLocation='");
                out.write(schemaLocation);
                out.write("'");
            }
            out.finishStartTag(false, true);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Import))
                return false;
            Import el = (Import) o;
            if (theNamespace == null) {
                if (el.theNamespace != null)
                    return false;
            } else if (!theNamespace.equals(el.theNamespace))
                return false;
            if (schemaLocation == null) {
                if (el.schemaLocation != null)
                    return false;
            } else if (!schemaLocation.equals(el.schemaLocation))
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((theNamespace == null) ? 0 : theNamespace.hashCode());
            result = 37*result + ((schemaLocation == null) ? 0 : schemaLocation.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }

    // See XML Schema sequence.
    public abstract class ModelGroup extends ContainsSubElements implements MinMaxOccurs {
        private String minOccurs;
        private String maxOccurs;

        public ModelGroup() {
            super();
            minOccurs = "1";
            maxOccurs = "1";
        }

        public String getContentName() {
            return null;
        }

        public void setMinOccurs(String mino) {
            if (mino == null)
                mino = "1";
            minOccurs = mino.intern();
        }

        public void setMaxOccurs(String maxo) {
            if (maxo == null)
                maxo = "1";
            maxOccurs = maxo.intern();
        }

        public String getMinOccurs() {
            return minOccurs;
        }

        public String getMaxOccurs() {
            return maxOccurs;
        }

        /**
         * If we have no subelements, we ask to be deleted.
         * If we have only 1 element and attributes haven't been set, then
         * we replace ourselves with that element.
         */
        public ElementExpr optimize() {
            super.optimize();
            if (subElements.size() == 0)
                return null;
            /*
            if (subElements.size() == 1 && minOccurs == "1" && maxOccurs == "1")
                return (ElementExpr) subElements.iterator().next();
            */
            return this;
        }

        public boolean writeDTDName(StringBuffer out) {
            boolean many = ("unbounded" == maxOccurs);
            boolean hasNamedSubElements = writeDTDSubElementNames(out, true);
            if (many)
                out.append("*");
            return hasNamedSubElements;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (minOccurs != "1")
                sb.append(" minOccurs='"+minOccurs+"'");
            if (maxOccurs != "1")
                sb.append(" maxOccurs='"+maxOccurs+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String minOccurs = node.getAttribute("minOccurs");	// NOI18N
            String maxOccurs = node.getAttribute("maxOccurs");	// NOI18N
            ModelGroup el = newInstance();
            pushCurrent(el);
            if (minOccurs != null && !minOccurs.equals(""))
                el.setMinOccurs(minOccurs);
            if (maxOccurs != null && !maxOccurs.equals(""))
                el.setMaxOccurs(maxOccurs);
            read(node);
            popCurrent();
        }

        protected abstract ModelGroup newInstance();

        public boolean equals(Object o) {
            if (!(o instanceof ModelGroup))
                return false;
            ModelGroup el = (ModelGroup) o;
            if (minOccurs != el.minOccurs)
                return false;
            if (maxOccurs != el.maxOccurs)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((minOccurs == null) ? 0 : minOccurs.hashCode());
            result = 37*result + ((maxOccurs == null) ? 0 : maxOccurs.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }

    // See XML Schema sequence.
    public class Sequence extends ModelGroup {
        public Sequence() {
        }

        public String getName() {
            return "sequence";
        }

        public Map validSubElementTypeMap() {
            return sequenceValidSubElementTypeMap;
        }

        protected ModelGroup newInstance() {
            return new Sequence();
        }
    }
    private static Map sequenceValidSubElementTypeMap = null;
    static {
        sequenceValidSubElementTypeMap = new HashMap();
        sequenceValidSubElementTypeMap.put(Annotation.class, null);
        sequenceValidSubElementTypeMap.put(Element.class, null);
        sequenceValidSubElementTypeMap.put(Any.class, null);
        sequenceValidSubElementTypeMap.put(Choice.class, null);
        sequenceValidSubElementTypeMap.put(Sequence.class, null);
        sequenceValidSubElementTypeMap.put(Group.class, null);
    }

    public class Choice extends ModelGroup {
        public Choice() {
        }

        public String getName() {
            return "choice";
        }

        public Map validSubElementTypeMap() {
            return choiceValidSubElementTypeMap;
        }

        protected ModelGroup newInstance() {
            return new Choice();
        }
    }
    private static Map choiceValidSubElementTypeMap = null;
    static {
        choiceValidSubElementTypeMap = new HashMap();
        choiceValidSubElementTypeMap.put(Annotation.class, null);
        choiceValidSubElementTypeMap.put(Element.class, null);
        choiceValidSubElementTypeMap.put(Any.class, null);
        choiceValidSubElementTypeMap.put(Choice.class, null);
        choiceValidSubElementTypeMap.put(Sequence.class, null);
        choiceValidSubElementTypeMap.put(Group.class, null);
    }

    public class All extends ModelGroup {
        public All() {
        }

        public String getName() {
            return "all";
        }

        public Map validSubElementTypeMap() {
            return allValidSubElementTypeMap;
        }

        protected ModelGroup newInstance() {
            return new All();
        }
    }
    private static Map allValidSubElementTypeMap = null;
    static {
        allValidSubElementTypeMap = new HashMap();
        allValidSubElementTypeMap.put(Annotation.class, null);
        allValidSubElementTypeMap.put(Element.class, null);
        allValidSubElementTypeMap.put(Any.class, null);
    }

    public class Group extends ModelGroup implements CanRef {
        private String name;
        private String ref;

        public Group() {
        }

        public Group(String n) {
            if (n != null && !n.equals("")) {
                name = normalizeTargetNamespace(n).intern();
                putSchemaTypeDef(name, this);
            }
        }

        public ElementExpr optimize() {
            if (ref != null)
                return this;
            return super.optimize();
        }

        public String getName() {
            return "group";
        }

        public String getGroupName() {
            return name;
        }

        public boolean hasRef() {
            return getRef() != null;
        }

        /**
         * May return null.
         */
        public String getRef() {
            return ref;
        }

        public Group getRefGroup() {
            if (ref == null)
                return null;
            Object o = getSchemaTypeDef(ref);
            if (o instanceof Group) {
                Group referredGroup = (Group) o;
                return referredGroup;
            } else {
                throw new IllegalStateException(Common.getMessage("MSG_FailedToFindRef",
                                                                  ref,
                                                                  this.toString()));
            }
        }

        public ElementExpr getRefElementExpr() {
            return getRefGroup();
        }

        public Map validSubElementTypeMap() {
            return groupValidSubElementTypeMap;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (name != null)
                sb.append(" name='"+name+"'");
            if (ref != null)
                sb.append(" ref='"+ref+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String myName = node.getAttribute("name");	// NOI18N
            String myRef = node.getAttribute("ref");	// NOI18N
            Group g = new Group(myName);
            if (myRef != null && !myRef.equals(""))
                g.ref = myRef.intern();
            pushCurrent(g);
            read(node);
            popCurrent();
        }

        protected ModelGroup newInstance() {
            return new Group();
        }

        public boolean equals(Object o) {
            if (!(o instanceof AttributeGroup))
                return false;
            AttributeGroup el = (AttributeGroup) o;
            if (name != el.name)
                return false;
            if (ref != el.ref)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((name == null) ? 0 : name.hashCode());
            result = 37*result + ((ref == null) ? 0 : ref.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map groupValidSubElementTypeMap = null;
    static {
        groupValidSubElementTypeMap = new HashMap();
        groupValidSubElementTypeMap.put(All.class, null);
        groupValidSubElementTypeMap.put(Choice.class, null);
        groupValidSubElementTypeMap.put(Sequence.class, null);
        groupValidSubElementTypeMap.put(Annotation.class, null);
    }

    // See XML Schema annotation
    // (an XML Schema comment)
    public class Annotation extends ContainsSubElements {
        public Annotation() {
        }

        public String getName() {
            return "annotation";
        }

        public String getContentName() {
            return null;
        }

        public ElementExpr optimize() {
            super.optimize();
            if (subElements.size() == 0)
                return null;
            return this;
        }

        public boolean compressWhiteSpaceInner() {
            return true;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void readSchema(org.w3c.dom.Element node) {
            Annotation el = new Annotation();
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return annotationValidSubElementTypeMap;
        }
    }
    private static Map annotationValidSubElementTypeMap = null;
    static {
        annotationValidSubElementTypeMap = new HashMap();
        annotationValidSubElementTypeMap.put(AppInfo.class, null);
        annotationValidSubElementTypeMap.put(Documentation.class, null);
    }


    // See XML Schema appInfo
    public class AppInfo extends ContainsSubElements {
        public AppInfo() {
        }

        public String getContentName() {
            return null;
        }

        public boolean compressWhiteSpaceOuter() {
            return true;
        }

        public boolean compressWhiteSpaceInner() {
            return true;
        }

        public String getName() {
            return "appinfo";
        }

        public ElementExpr optimize() {
            super.optimize();
            if (subElements.size() == 0)
                return null;
            return this;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void readSchema(org.w3c.dom.Element node) {
            AppInfo el = new AppInfo();
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return appInfoValidSubElementTypeMap;
        }
    }
    private static Map appInfoValidSubElementTypeMap = null;
    static {
        appInfoValidSubElementTypeMap = new HashMap();
        appInfoValidSubElementTypeMap.put(AnyNode.class, null);
        appInfoValidSubElementTypeMap.put(TextNode.class, null);
    }


    // See XML Schema documentation
    public class Documentation extends ContainsSubElements {
        public Documentation() {
        }

        public String getContentName() {
            return null;
        }

        public boolean compressWhiteSpaceOuter() {
            return true;
        }

        public boolean compressWhiteSpaceInner() {
            return true;
        }

        public String getName() {
            return "documentation";
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void readSchema(org.w3c.dom.Element node) {
            Documentation el = new Documentation();
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public Map validSubElementTypeMap() {
            return documentationValidSubElementTypeMap;
        }
    }
    private static Map documentationValidSubElementTypeMap = null;
    static {
        documentationValidSubElementTypeMap = new HashMap();
        documentationValidSubElementTypeMap.put(AnyNode.class, null);
        documentationValidSubElementTypeMap.put(TextNode.class, null);
    }


    // This represents any undefined node
    public class AnyNode extends ContainsSubElements {
        protected String name;
        protected String value;

        public AnyNode(String n, String v) {
            name = n;
            value = v;
        }

        public String getContentName() {
            return name;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean compressWhiteSpaceInner() {
            return findSubElement("TextNode") != null;
        }

        public ElementExpr optimize() {
            super.optimize();
            if (name == null && value == null && subElements.size() == 0)
                return null;
            return this;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            if (value == null)
                this.writeXMLSchema(out, true);
            else {
                out.startTag(getName());
                XMLUtil.printXML(out, value, false);
                this.writeXMLSchema(out, false);
                out.endTag();
            }
        }

        public void readSchema(org.w3c.dom.Element node) {
            String myName = node.getLocalName();
            String myValue = node.getNodeValue();
            AnyNode el = new AnyNode(myName, myValue);

            // Should read in attributes too.
            //System.out.println("Just read AnyNode: myName="+myName+" myValue="+myValue);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public String toString() {
            return "AnyNode("+name+")";
        }

        public Map validSubElementTypeMap() {
            return anyNodeValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof AnyNode))
                return false;
            AnyNode el = (AnyNode) o;
            //System.out.println("value="+value+" el.value="+el.value);
            if (value == null) {
                if (el.value != null)
                    return false;
            } else if (!value.equals(el.value))
                return false;
            if (name == null) {
                if (el.name != null)
                    return false;
            } else if (!name.equals(el.name))
                return false;
            return true;
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((value == null) ? 0 : value.hashCode());
            result = 37*result + ((name == null) ? 0 : name.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map anyNodeValidSubElementTypeMap = null;
    static {
        anyNodeValidSubElementTypeMap = new HashMap();
        anyNodeValidSubElementTypeMap.put(AnyNode.class, null);
        anyNodeValidSubElementTypeMap.put(TextNode.class, null);
    }

    // This represents a text element and allows for subelements.
    public class TextNode extends ContainsSubElements {
        protected String text;

        public TextNode(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public String getName() {
            return "TextNode";
        }

        public String getContentName() {
            return null;
        }

        public boolean compressWhiteSpaceInner() {
            return true;
        }

        public ElementExpr optimize() {
            super.optimize();
            if (text == null || text.equals(""))
                return null;
            return this;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void writeXMLSchema(XMLWriter out) throws IOException {
            XMLUtil.printXML(out, text, false);
            this.writeXMLSchema(out, false);
        }

        public void readSchema(org.w3c.dom.Element node) {
            readSchema((Text) node);
        }

        public void readSchema(Text node) {
            TextNode el = new TextNode(node.getData());
            //System.out.println("Just read TextNode: myName="+myName+" myValue="+myValue);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public String toString() {
            return "TextNode("+text+")";
        }

        public Map validSubElementTypeMap() {
            return textNodeValidSubElementTypeMap;
        }

        public boolean equals(Object o) {
            if (!(o instanceof TextNode))
                return false;
            TextNode el = (TextNode) o;
            if (text == null) {
                if (el.text != null)
                    return false;
            } else if (!text.equals(el.text))
                return false;
            return true;
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((text == null) ? 0 : text.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map textNodeValidSubElementTypeMap = null;
    static {
        textNodeValidSubElementTypeMap = new HashMap();
        textNodeValidSubElementTypeMap.put(AnyNode.class, null);
        textNodeValidSubElementTypeMap.put(TextNode.class, null);
    }

    // See XML Schema element
    // This implementation is missing attributes right now.
    public class Element extends ContainsSubElements implements MinMaxOccurs, CanRef {
        private String elementName;
        private String elementNamespace;   // a URI
        private String defaultTargetNamespace;
        private boolean defaultFormQualified;
        private Boolean formQualified;
        // If type is set and there are subelements, then the type
        // is a "comment" about which java type should be used.
        private String type;
        private String xmlSchemaType;
        private String ref;
        private String refWithNamespace;
        private String minOccurs = "1";
        private String maxOccurs = "1";
        private boolean nillable = false;
        private String defaultValue;
        private ElementExpr parentElementExpr;

        protected Element(String n, String t) {
            this(n);
            setType(t);
            putSchemaTypeDef(type, this);
        }

        protected Element(String n) {
            setElementName(n);
            type = null;
        }
        
        private Element() {
        }
        
        public String getName() {
            return "element";
        }

        public String getContentName() {
            return elementName;
        }

        /**
         * @param n The qualified element name;
         *          for instance, "address" or "tns:address"
         */
        private void setElementName(String n) {
            if (n != null && !n.equals("")) {
                String prefix = prefixOf(n);
                if (prefix == null) {
                    //
                    // See XML Schema spec Part 1, section 3.3.2 and search
                    // for "namespace" to figure out where the namespace
                    // comes from.
                    //
                    if (parentElementExpr instanceof SchemaNode) {
                        elementNamespace = defaultTargetNamespace;
                    } else {
                        if (isFormQualified())
                            elementNamespace = defaultTargetNamespace;
                        else {
                            //elementNamespace = "";
                            elementNamespace = null;
                        }
                    }
                    //System.out.println("n="+n+" elementNamespace="+elementNamespace);
                    /*
                    if (!"dummy".equals(n) &&
                            ((elementNamespace == null && defaultTargetNamespace != null)
                            || (elementNamespace != null && !elementNamespace.equals(targetNamespace)))) {
                        System.out.println("Different namespace on "+n+" elementNamespace="+elementNamespace+" targetNamespace="+targetNamespace+" defaultTargetNamespace="+defaultTargetNamespace);
                    }
                     */
                    elementName = n;
                } else {
                    elementNamespace = getNamespaceURI(prefix);
                    elementName = removePrefix(n);
                }
            } else {
                elementName = null;
                elementNamespace = null;
            }
        }

        public String getElementName() {
            return elementName;
        }

        /**
         * Returns the URI namespace
         */
        public String getElementNamespace() {
            return elementNamespace;
        }
        
        public String getPrefix() {
            return getNamespace(elementNamespace);
        }
        
        public boolean isFormQualified() {
            if (formQualified != null)
                return formQualified.booleanValue();
            return defaultFormQualified;
        }

        public String getType() {
            return type;
        }

        public boolean hasRef() {
            return getRef() != null;
        }

        /**
         * May return null.
         */
        public String getRef() {
            return ref;
        }

        public Element getRefElement() {
            if (ref == null)
                return null;
            //Element referredElement = getDefinedElement(ref);
            Element referredElement = getDefinedElementResolvedNamespace(refWithNamespace);
            return referredElement;
        }

        public ElementExpr getRefElementExpr() {
            return getRefElement();
        }

        /**
         * Set the type of this element.  The String passed in, @t, should
         * be a java type like 'java.lang.Integer' or
         * 'com.sun.forte4j.webdesigner.SOAPTest.Foo' or 'float'.
         * This type will later get converted into the XML Schema type
         * ('int' -> 'xsd:int', 'java.lang.String' -> 'xsd:string').
         */
        public void setType(String t) {
            if (t != null)
                t = t.intern();
            if ("void" == t)
                t = null;
            String oldType = type;
            type = t;
            schemaTypeDefs.remove(oldType); // FIXME
            if (t == null)
                return;
            putSchemaTypeDef(type, this);
            ref = null;
            if (debug)
                System.out.println("setType("+t+")");
            if (optionallyDefinedTypes.containsKey(t)) {
                //System.out.println("Found it!");
                requiredPredefinedTypes.put(t, "keep");  // Keep this one.  NOI18N
            }
        }

        /**
         * Bypass setType's converstion from java type to XML Schema type.
         * @t should be the XML Schema type.  This should only be used
         * for types that are defined previously in this schema
         * or have special meaning to the eventual reader of this schema
         * (like, 'SOAP-INC:Array').
         */
        public void setXMLSchemaType(String t) {
            if (t == null) {
                xmlSchemaType = null;
            } else {
                xmlSchemaType = normalizeDocumentNamespace(t).intern();
                ref = null;
            }
        }

        public String getJavaType() {
            //System.out.println("Element.getJavaType: type="+type+" ref="+ref+" xmlSchemaType="+xmlSchemaType);
            if (type != null)
                return type;
            if (ref != null) {
                Element referredElement = getRefElement();
                //System.out.println("Found "+referredElement);
                return referredElement.getJavaType();
            }
            if (xmlSchemaType == null)
                return null;
            String javaType = schemaTypeToJavaType(xmlSchemaType);
            if (nillable) {
                javaType = JavaUtil.toObjectType(javaType);
            }
            return javaType;
        }

        public String getXMLSchemaType() {
            return xmlSchemaType;
        }

        public void setMinOccurs(String mino) {
            if (mino == null)
                mino = "1";
            minOccurs = mino.intern();
        }

        public void setMaxOccurs(String maxo) {
            if (maxo == null)
                maxo = "1";
            maxOccurs = maxo.intern();
        }

        public String getMinOccurs() {
            return minOccurs;
        }

        public String getMaxOccurs() {
            return maxOccurs;
        }

        public boolean isNillable() {
            return nillable;
        }

        public String getDefault() {
            return defaultValue;
        }

        public void setDefault(String d) {
            defaultValue = d;
        }

        public void writeDTD(StringBuffer out) {
            Element firstElement = (Element) elementTable.get(getElementName());
            if (firstElement == null)
                elementTable.put(getElementName(), this);
            else {
                // Gotta compare
                if (debug)
                    System.out.println("Found another element named "+getElementName());
                if (!equals(firstElement)) {
                    throw new RuntimeException(Common.getMessage("MSG_SameNameDifferentContents", getElementName()));
                }
                return;
            }

            out.append("<!ELEMENT "+getElementName()+" ");
            // "EMPTY" correlates with hasNamedSubElements
            if (subElements.size() == 0) {
                if (type == null || type == "void")
                    out.append("()");
                else
                    out.append("#PCDATA");
            } else {
                if (!writeDTDSubElementNames(out))
                    out.append("()");
            }
            out.append(">\n");

            // Now tell the subelements to print themselves out too
            super.writeDTD(out);
        }

        public boolean hasNamedSubElements() {
            if (subElements.size() == 0) {
                return false;
            } else {
                if (!writeDTDSubElementNames(new StringBuffer()))
                    return false;
                else
                    return true;
            }
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (elementName != null) {
                sb.append(" name='");
                if (elementNamespace != null && !elementNamespace.equals(targetNamespace)) {
                    sb.append(getNamespace(elementNamespace));
                    sb.append(':');
                }
                sb.append(elementName);
                sb.append("'");
            }
            if (ref != null) {
                sb.append(" ref='");
                sb.append(ref);
                sb.append("'");
            }
            if (xmlSchemaType != null) {
                sb.append(" type='");
                sb.append(xmlSchemaType);
                sb.append("'");
            } else if (type != null) {
                String theXmlSchemaType = javaType2XMLSchemaTypeComplex(getType());
                if (theXmlSchemaType != null) {
                    sb.append(" type='");
                    sb.append(theXmlSchemaType);
                    sb.append("'");
                } else {
                    //throw new IllegalStateException(Common.getMessage("MSG_FailedToFindXMLSchemaType", type));
                }
            }
            if (minOccurs != "1")
                sb.append(" minOccurs='"+minOccurs+"'");
            if (maxOccurs != "1")
                sb.append(" maxOccurs='"+maxOccurs+"'");
            if (nillable)
                sb.append(" nillable='true'");
            if (defaultValue != null)
                sb.append(" default='"+defaultValue+"'");
            if (formQualified != null)
                sb.append(" form='"+(formQualified.booleanValue() ? "qualified" : "unqualified")+"'");
            return sb.toString();
        }

        public boolean isDefiningNewType() {
            if (ref == null)
                return true;
            return (subElements.size() >= 1 && type != null);
        }

        public boolean writeDTDName(StringBuffer out) {
            out.append(getElementName());
            if ("unbounded" == maxOccurs)
                out.append("*");
            return true;
        }

        public Map validSubElementTypeMap() {
            return elementValidSubElementTypeMap;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String elementName = node.getAttribute("name");	// NOI18N
            String elementType = node.getAttribute("type");	// NOI18N
            String elementRef = node.getAttribute("ref");	// NOI18N
            String minOccurs = node.getAttribute("minOccurs");	// NOI18N
            String maxOccurs = node.getAttribute("maxOccurs");	// NOI18N
            String myNillable = node.getAttribute("nillable");	// NOI18N
            String myDefault = node.getAttribute("default");	// NOI18N
            String myForm = node.getAttribute("form");	// NOI18N
            String defaultTargetNamespace;
            boolean defaultFormQualified;
            ElementExpr parent = peekCurrent();
            //System.out.println("elementName="+elementName+" parent="+parent);
            SchemaNode parentSchema;
            if (parent instanceof SchemaNode) {
                parentSchema = (SchemaNode) parent;
            } else {
                parentSchema = (SchemaNode) findAncestor(SchemaNode.class);
            }
            if (parentSchema != null) {
                //
                // Get the targetNamespace from the instance variable instead of
                // querying the schema node, since if multiple schema nodes are
                // merged, only the first keeps it's targetNamespace.
                //
                //defaultTargetNamespace = parentSchema.getTargetNamespace();
                defaultTargetNamespace = targetNamespace;
                //defaultFormQualified = parentSchema.isElementFormQualified();
                defaultFormQualified = elementFormQualifiedDefault;
            } else {
                defaultTargetNamespace = targetNamespace;
                defaultFormQualified = false;
            }
            Element el = new Element();
            el.parentElementExpr = parent;
            el.defaultTargetNamespace = defaultTargetNamespace;
            el.defaultFormQualified = defaultFormQualified;
            el.setElementName(elementName);
            if (myForm != null && !"".equals(myForm))
                el.formQualified = Boolean.valueOf("qualified".equals(myForm));
            if (elementRef != null && !elementRef.equals("")) {
                el.ref = elementRef.intern();
                el.refWithNamespace = resolveNamespace(el.ref);
            }
            if (elementType != null && !elementType.equals(""))
                el.setXMLSchemaType(elementType);
            if (minOccurs != null && !minOccurs.equals(""))
                el.setMinOccurs(minOccurs);
            if (maxOccurs != null && !maxOccurs.equals(""))
                el.setMaxOccurs(maxOccurs);
            if (myNillable != null && (myNillable.equals("true") || myNillable.equals("yes") || myNillable.equals("on")))
                el.nillable = true;
            if (myDefault != null && !"".equals(myDefault))
                el.setDefault(myDefault);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Element))
                return false;
            Element el = (Element) o;
            //System.out.println("type="+type);
            if (type != el.type)
                return false;
            if (ref != el.ref)
                return false;
            if (xmlSchemaType != el.xmlSchemaType)
                return false;
            if (minOccurs != el.minOccurs)
                return false;
            if (maxOccurs != el.maxOccurs)
                return false;
            if (nillable != el.nillable)
                return false;
            if (formQualified == null) {
                if (el.formQualified != null)
                    return false;
            } else {
                if (el.formQualified == null
                        || formQualified.booleanValue() != el.formQualified.booleanValue())
                    return false;
            }

            if (!elementName.equals(el.elementName))
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((type == null) ? 0 : type.hashCode());
            result = 37*result + ((ref == null) ? 0 : ref.hashCode());
            result = 37*result + ((xmlSchemaType == null) ? 0 : xmlSchemaType.hashCode());
            result = 37*result + ((minOccurs == null) ? 0 : minOccurs.hashCode());
            result = 37*result + ((maxOccurs == null) ? 0 : maxOccurs.hashCode());
            result = 37*result + (nillable ? 1 : 0);
            result = 37*result + ((elementName == null) ? 0 : elementName.hashCode());
            result = 37*result + super.hashCode();
            result = 37*result + ((formQualified == null) ? 0 : formQualified.hashCode());
            return result;
        }
    }
    private static Map elementValidSubElementTypeMap = null;
    static {
        elementValidSubElementTypeMap = new HashMap();
        elementValidSubElementTypeMap.put(Annotation.class, null);
        elementValidSubElementTypeMap.put(SimpleType.class, null);
        elementValidSubElementTypeMap.put(ComplexType.class, null);
        elementValidSubElementTypeMap.put(Unique.class, null);
        elementValidSubElementTypeMap.put(Key.class, null);
        elementValidSubElementTypeMap.put(KeyRef.class, null);
    }

    public class Any extends ContainsSubElements implements MinMaxOccurs {
        private String minOccurs;
        private String maxOccurs;
        private String namespace;
        private String processContents;

        public Any() {
            init();
        }

        private void init() {
            minOccurs = "1";
            maxOccurs = "1";
        }

        public String getName() {
            return "any";
        }

        public String getContentName() {
            return null;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String n) {
            namespace = (n == null) ? null : n.intern();
        }

        public String getProcessContents() {
            return processContents;
        }

        public void setProcessContents(String pc) {
            processContents = (pc == null) ? null : pc.intern();
        }

        public void setMinOccurs(String mino) {
            if (mino == null)
                mino = "1";
            minOccurs = mino.intern();
        }

        public void setMaxOccurs(String maxo) {
            if (maxo == null)
                maxo = "1";
            maxOccurs = maxo.intern();
        }

        public String getMinOccurs() {
            return minOccurs;
        }

        public String getMaxOccurs() {
            return maxOccurs;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (minOccurs != "1")
                sb.append(" minOccurs='"+minOccurs+"'");
            if (maxOccurs != "1")
                sb.append(" maxOccurs='"+maxOccurs+"'");
            if (namespace != null)
                sb.append(" namespace='"+namespace+"'");
            if (processContents != null)
                sb.append(" processContents='"+processContents+"'");
             return sb.toString();
       }

        public Map validSubElementTypeMap() {
            return anyValidSubElementTypeMap;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String namespace = node.getAttribute("namespace");	// NOI18N
            String processContents = node.getAttribute("processContents");	// NOI18N
            String minOccurs = node.getAttribute("minOccurs");	// NOI18N
            String maxOccurs = node.getAttribute("maxOccurs");	// NOI18N
            Any el = new Any();
            if (namespace != null && !namespace.equals(""))
                el.setNamespace(namespace);
            if (processContents != null && !processContents.equals(""))
                el.setProcessContents(processContents);
            if (minOccurs != null && !minOccurs.equals(""))
                el.setMinOccurs(minOccurs);
            if (maxOccurs != null && !maxOccurs.equals(""))
                el.setMaxOccurs(maxOccurs);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Any))
                return false;
            Any el = (Any) o;
            if (minOccurs != el.minOccurs)
                return false;
            if (maxOccurs != el.maxOccurs)
                return false;
            if (namespace != el.namespace)
                return false;
            if (processContents != el.processContents)
                return false;
            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((namespace == null) ? 0 : namespace.hashCode());
            result = 37*result + ((processContents == null) ? 0 : processContents.hashCode());
            result = 37*result + ((minOccurs == null) ? 0 : minOccurs.hashCode());
            result = 37*result + ((maxOccurs == null) ? 0 : maxOccurs.hashCode());
            return result;
        }
    }
    private static Map anyValidSubElementTypeMap = null;
    static {
        anyValidSubElementTypeMap = new HashMap();
        anyValidSubElementTypeMap.put(Annotation.class, null);
    }

    public class AnyAttribute extends ContainsSubElements {
        private String namespace;
        private String processContents;

        public AnyAttribute() {
        }

        public String getName() {
            return "anyAttribute";
        }

        public String getContentName() {
            return null;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String n) {
            namespace = (n == null) ? null : n.intern();
        }

        public String getProcessContents() {
            return processContents;
        }

        public void setProcessContents(String pc) {
            processContents = (pc == null) ? null : pc.intern();
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (namespace != null)
                sb.append(" namespace='"+namespace+"'");
            if (processContents != null)
                sb.append(" processContents='"+processContents+"'");
             return sb.toString();
       }

        public Map validSubElementTypeMap() {
            return anyAttributeValidSubElementTypeMap;
        }

        public boolean writeDTDName(StringBuffer out) {
            return false;
        }

        public void readSchema(org.w3c.dom.Element node) {
            String namespace = node.getAttribute("namespace");	// NOI18N
            String processContents = node.getAttribute("processContents");	// NOI18N
            AnyAttribute el = new AnyAttribute();
            if (namespace != null && !namespace.equals(""))
                el.setNamespace(namespace);
            if (processContents != null && !processContents.equals(""))
                el.setProcessContents(processContents);
            pushCurrent(el);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof AnyAttribute))
                return false;
            AnyAttribute el = (AnyAttribute) o;
            if (namespace != el.namespace)
                return false;
            if (processContents != el.processContents)
                return false;
            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((namespace == null) ? 0 : namespace.hashCode());
            result = 37*result + ((processContents == null) ? 0 : processContents.hashCode());
            return result;
        }
    }
    private static Map anyAttributeValidSubElementTypeMap = null;
    static {
        anyAttributeValidSubElementTypeMap = new HashMap();
        anyAttributeValidSubElementTypeMap.put(Annotation.class, null);
    }

    // An Schema Attribute
    // An XML Schema attribute may contain simpleType as a subelement
    // and/or annotation.
    public class Attribute extends ContainsSubElements implements CanRef {
        private String namespace;
        private String name;
        private String type;
        private String defaultValue;
        private String id;
        private String ref;
        private String fixed;
        private String use;
        private String arrayType;	// for rpc/encoded WSDL

        public Attribute(String n) {
            this(n, targetNamespace);
        }

        public Attribute(String n, String ns) {
            this(n, ns, null);
        }

        public Attribute(String n, String ns, String type) {
            if (n == null || n.equals(""))
                name = null;
            else
                name = n.intern();
            namespace = ns;
            setType(type);
        }

        public String getName() {
            return "attribute";
        }

        public String getContentName() {
            return "@"+name;
        }

        public String getAttributeName() {
            if (namespace == null || namespace.equals(targetNamespace)) {
                return name;
            }
            return getNamespace(namespace)+":"+name;
        }

        public String getAttributeNamespace() {
            return namespace;
        }

        public String getAttributeNameNoNS() {
            return name;
        }

        private void setType(String ty) {
            if (ty == null) {
                type = null;
                return;
            }
            type = normalizeDocumentNamespace(ty).intern();
        }

        public String getType() {
            return type;
        }

        public String getJavaType() {
            if (type == null)
                return null;
            String javaType = schemaTypeToJavaType(type);
            if (false) {
                javaType = JavaUtil.toObjectType(javaType);
            }
            return javaType;
        }

        public String getFixed() {
            return fixed;
        }

        public boolean isRequired() {
            return use == "required";
        }

        public boolean isOptional() {
            return use == "optional";
        }

        public boolean isProhibited() {
            return use == "prohibited";
        }

        public String getUse() {
            return use;
        }

        public boolean isAttributeNamed(String n) {
            n = resolveNamespace(n);
            String fullName = resolveNamespace(namespace, name);
            return n.equals(fullName);
        }

        public boolean hasRef() {
            return getRef() != null;
        }

        /**
         * May return null.
         */
        public String getRef() {
            return ref;
        }

        public Attribute getRefAttribute() {
            if (ref == null)
                return null;
            Object o = definedAttributes.get(ref);
            if (o instanceof Attribute) {
                Attribute referredAttribute = (Attribute) o;
                return referredAttribute;
            } else {
                throw new IllegalStateException(Common.getMessage("MSG_FailedToFindRef",
                                                                  ref,
                                                                  this.toString()));
            }
        }

        public ElementExpr getRefElementExpr() {
            return getRefAttribute();
        }

        public boolean isDefiningNewType() {
            if (ref == null)
                return true;
            return (subElements.size() >= 1 && type != null);
        }

        public String getArrayType() {
            return arrayType;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void writeDTD(StringBuffer out) {
            writeDTD(out, "UNKNOWN");
        }

        public void writeDTD(StringBuffer out, String elementName) {
            out.append("<!ATTLIST "+elementName+" "+name+" ");
            out.append(">\n");
        }

        public boolean writeDTDName(StringBuffer out) {
            out.append(name);
            return false;
        }

        public void validate() {

            super.validate();
        }

        public Map validSubElementTypeMap() {
            return attributeValidSubElementTypeMap;
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (name != null)
                sb.append(" name='"+getAttributeName()+"'");
            if (type != null)
                sb.append(" type='"+type+"'");
            if (fixed != null) {
                sb.append(" fixed='");
                XMLUtil.printXML(sb, fixed, true);
                sb.append("'");
            }
            if (ref != null)
                sb.append(" ref='"+ref+"'");
            if (id != null)
                sb.append(" id='"+id+"'");
            if (defaultValue != null) {
                sb.append(" default='");
                XMLUtil.printXML(sb, defaultValue, true);
                sb.append("'");
            }
            if (use != null)
                sb.append(" use='"+use+"'");
            if (arrayType != null)
                sb.append(" wsdl:arrayType='"+arrayType+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String myName = node.getAttribute("name");	// NOI18N
            String myType = node.getAttribute("type");	// NOI18N
            String myFixed = node.getAttribute("fixed");	// NOI18N
            String myRef = node.getAttribute("ref");	// NOI18N
            String myId = node.getAttribute("id");	// NOI18N
            String myDefault = node.getAttribute("default");	// NOI18N
            String myUse = node.getAttribute("use");	// NOI18N
            String myArrayType = node.getAttributeNS("http://schemas.xmlsoap.org/wsdl/", "arrayType");

            Attribute attr = new Attribute(myName);
            if (myType != null && !myType.equals(""))
                attr.setType(myType.intern());
            if (myFixed != null && !myFixed.equals(""))
                attr.fixed = myFixed.intern();
            if (myRef != null && !myRef.equals(""))
                attr.ref = myRef.intern();
            if (myId != null && !myId.equals(""))
                attr.id = myId.intern();
            if (myDefault != null && !myDefault.equals(""))
                attr.defaultValue = myDefault.intern();
            if (myUse != null && !myUse.equals(""))
                attr.use = myUse.intern();
            if (myArrayType != null && !myArrayType.equals(""))
                attr.arrayType = myArrayType.intern();
            pushCurrent(attr);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof Attribute))
                return false;
            Attribute el = (Attribute) o;
            if (name != el.name)
                return false;
            if (type != el.type)
                return false;
            if (fixed != el.fixed)
                return false;
            if (ref != el.ref)
                return false;
            if (id != el.id)
                return false;
            if (use != el.use)
                return false;
            if (arrayType != el.arrayType)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((name == null) ? 0 : name.hashCode());
            result = 37*result + ((type == null) ? 0 : type.hashCode());
            result = 37*result + ((fixed == null) ? 0 : fixed.hashCode());
            result = 37*result + ((ref == null) ? 0 : ref.hashCode());
            result = 37*result + ((id == null) ? 0 : id.hashCode());
            result = 37*result + ((use == null) ? 0 : use.hashCode());
            result = 37*result + ((arrayType == null) ? 0 : arrayType.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map attributeValidSubElementTypeMap = null;
    static {
        attributeValidSubElementTypeMap = new HashMap();
        attributeValidSubElementTypeMap.put(Annotation.class, null);
        attributeValidSubElementTypeMap.put(SimpleType.class, null);
    }


    // An Schema AttributeGroup
    public class AttributeGroup extends ContainsSubElements implements CanRef {
        private String name;
        private String ref;

        public AttributeGroup() {
        }

        /**
         * Create it by name.
         */
        public AttributeGroup(String n) {
            if (n != null && !n.equals("")) {
                name = normalizeTargetNamespace(n).intern();
                putSchemaTypeDef(name, this);
            }
        }

        public Map validSubElementTypeMap() {
            return attributeGroupValidSubElementTypeMap;
        }

        public String getName() {
            return "attributeGroup";
        }

        public String getContentName() {
            return name;
        }

        public String getGroupName() {
            return name;
        }

        public boolean hasRef() {
            return getRef() != null;
        }

        public String getRef() {
            return ref;
        }

        public AttributeGroup getRefAttributeGroup() {
            if (ref == null)
                return null;
            Object o = getSchemaTypeDef(ref);
            if (o instanceof AttributeGroup) {
                AttributeGroup referredGroup = (AttributeGroup) o;
                return referredGroup;
            } else {
                throw new IllegalStateException(Common.getMessage("MSG_FailedToFindRef",
                                                                  ref,
                                                                  this.toString()));
            }
        }

        public ElementExpr getRefElementExpr() {
            return getRefAttributeGroup();
        }

        public boolean writeDTDName(StringBuffer out) {
            return writeDTDSubElementNames(out, false);
        }

        public String getAttributeString() {
            StringBuffer sb = new StringBuffer();
            if (name != null)
                sb.append(" name='"+name+"'");
            if (ref != null)
                sb.append(" ref='"+ref+"'");
            return sb.toString();
        }

        public void readSchema(org.w3c.dom.Element node) {
            String myName = node.getAttribute("name");	// NOI18N
            String myRef = node.getAttribute("ref");	// NOI18N
            AttributeGroup ag = new AttributeGroup(myName);
            if (myRef != null && !myRef.equals(""))
                ag.ref = myRef.intern();
            pushCurrent(ag);
            read(node);
            popCurrent();
        }

        public boolean equals(Object o) {
            if (!(o instanceof AttributeGroup))
                return false;
            AttributeGroup el = (AttributeGroup) o;
            if (name != el.name)
                return false;
            if (ref != el.ref)
                return false;

            return super.equals(el);
        }

        public int hashCode() {
            int result = 17;
            result = 37*result + ((name == null) ? 0 : name.hashCode());
            result = 37*result + ((ref == null) ? 0 : ref.hashCode());
            result = 37*result + super.hashCode();
            return result;
        }
    }
    private static Map attributeGroupValidSubElementTypeMap = null;
    static {
        attributeGroupValidSubElementTypeMap = new HashMap();
        attributeGroupValidSubElementTypeMap.put(Annotation.class, null);
        attributeGroupValidSubElementTypeMap.put(Attribute.class, null);
        attributeGroupValidSubElementTypeMap.put(AttributeGroup.class, null);
        attributeGroupValidSubElementTypeMap.put(AnyAttribute.class, null);
    }

    private class ParserSchemaState {
        private String targetNamespace;
        private String documentNamespace;
        private boolean elementFormQualifiedDefault;
        private boolean attributeFormQualifiedDefault;
        
        public ParserSchemaState() {
            this.targetNamespace = SchemaRep.this.targetNamespace;
            this.documentNamespace = SchemaRep.this.documentNamespace;
            this.elementFormQualifiedDefault = SchemaRep.this.elementFormQualifiedDefault;
            this.attributeFormQualifiedDefault = SchemaRep.this.attributeFormQualifiedDefault;
        }
        
        public void reload() {
            SchemaRep.this.targetNamespace = this.targetNamespace;
            SchemaRep.this.documentNamespace = this.documentNamespace;
            SchemaRep.this.elementFormQualifiedDefault = this.elementFormQualifiedDefault;
            SchemaRep.this.attributeFormQualifiedDefault = this.attributeFormQualifiedDefault;
        }
    }
    
    //protected String docType;  // Not implemented
    protected Map namespaceTable;	// Map<String, String>  example: <xsd, http://www.w3.org/2001/XMLSchema>
    protected String targetNamespace;
    protected String documentNamespace;
    private boolean elementFormQualifiedDefault;
    private boolean attributeFormQualifiedDefault;
    protected ContainsSubElements rootElement;

    // elementTable is used when creating the DTD to make sure that only
    // 1 element of a particular name is ever created.
    protected Map elementTable;   // Map<String, Element>

    // Several simple types are hard coded into this class and put into
    // optionallyDefinedTypes.  They define how to map these simple types into
    // XML Schema types ('java.lang.Integer' -> 'xsd:int').
    protected Map optionallyDefinedTypes = null;   // Map<String, ElementExpr>

    // Top types defined in this schema.  Mapping of java type name into
    // element expressions (just like optionallyDefinedTypes)
    protected Map definedTypes;   // Map<String, ElementExpr>
    protected Map definedTypesFull;   // Map<String, ElementExpr>

    protected Map definedAttributes;	// Map<String, Attribute>

    // Standard types
    protected Map predefinedSchemaTypes;	// Map<String, ElementExpr>

    // requiredPredefinedTypes gets an entry whenever something in
    // optionallyDefinedTypes is used.
    protected Map requiredPredefinedTypes;   // Map<String, null>

    // The client adds elements to the current thing on the stack
    protected Stack currentElementStack;  // Stack<ElementExpr>

    // It's possible to have an Annotation added to the top level
    // and it's checked in writeXMLSchemaStandalone.
    protected Annotation topAnnotation;

    // It's useful for some clients to know what the last popped was.
    protected ElementExpr lastPopped;

    // A sample instance of every node that can be read in.
    protected Map sampleNodes;

    // map from type name to ElementExpr
    private Map schemaTypeDefs;	// Map<String, ElementExpr>

    // Whether or not a URL has been included already or not.
    protected Map includedAlready = new HashMap();	// <String, null>

    private String currentParsedURI;

    private boolean useBigDataTypes = true;

    public SchemaRep() {
        init();
    }

    public SchemaRep(Document schemaDoc, String uri) {
        init();
        currentParsedURI = uri;
        readDocument(schemaDoc);
    }

    public SchemaRep(Document schemaDoc, String uri, boolean useBigDataTypes) {
        this.useBigDataTypes = useBigDataTypes;
        init();
        currentParsedURI = uri;
        readDocument(schemaDoc);
    }

    private void init() {
        targetNamespace = null;
        documentNamespace = null;
        namespaceTable = new HashMap();
        addToNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        addToNamespace("xsd", XSD_NS);

        schemaTypeDefs = new HashMap();
        predefinedSchemaTypes = new HashMap();
        insertPredefinedSchemaTypes(predefinedSchemaTypes);

        currentElementStack = new Stack();
        topAnnotation = null;
        definedTypes = new HashMap();
        definedTypesFull = new HashMap();
        definedAttributes = new HashMap();
        //mapSimpleJavaTypesPredefined(definedTypes);
        mapSimpleAttributes(definedAttributes);
        requiredPredefinedTypes = new HashMap();
        if (optionallyDefinedTypes == null) {
            optionallyDefinedTypes = new HashMap();
            mapSimpleJavaTypesOptional(optionallyDefinedTypes);
        }

        // sampleNodes are used while reading an XML Schema in
        sampleNodes = new HashMap();
        putSampleNode(new Element("dummy"));	// NOI18N
        putSampleNode(new ComplexType());
        putSampleNode(new SimpleType(null));	// NOI18N
        putSampleNode(new UnionType(null, null));	// NOI18N
        putSampleNode(new Restriction());
        putSampleNode(new Sequence());
        putSampleNode(new Choice());
        putSampleNode(new All());
        putSampleNode(new Group());
        putSampleNode(new Annotation());
        putSampleNode(new AppInfo());
        putSampleNode(new Documentation());
        putSampleNode(new Attribute("dummy"));	// NOI18N
        putSampleNode(new AttributeGroup());
        putSampleNode(new MaxExclusive("dummy"));	// NOI18N
        putSampleNode(new MinExclusive("dummy"));	// NOI18N
        putSampleNode(new Enumeration("dummy"));	// NOI18N
        putSampleNode(new Pattern("dummy"));	// NOI18N
        putSampleNode(new MinLength("0"));	// NOI18N
        putSampleNode(new MaxLength("0"));	// NOI18N
        putSampleNode(new TotalDigits("dummy"));	// NOI18N
        putSampleNode(new MinInclusive("dummy"));	// NOI18N
        putSampleNode(new MaxInclusive("dummy"));	// NOI18N
        putSampleNode(new FractionDigits("dummy"));	// NOI18N
        putSampleNode(new Length("dummy"));	// NOI18N
        putSampleNode(new WhiteSpace("dummy"));	// NOI18N
        putSampleNode(new Key());
        putSampleNode(new Unique());
        putSampleNode(new KeyRef());
        putSampleNode(new Selector());
        putSampleNode(new Field());
        putSampleNode(new Include("dummy"));	// NOI18N
        putSampleNode(new Import());
        putSampleNode(new SimpleContent());
        putSampleNode(new ComplexContent());
        putSampleNode(new Extension());
        putSampleNode(new ListElement());
        putSampleNode(new Any());
        putSampleNode(new AnyAttribute());
    }


    /**
     * Example: ns=xsd, URI=http://www.w3.org/2001/XMLSchema
     */
    public void addToNamespace(String ns, String URI) {
        //System.out.println("Adding namespace "+ns+" as "+URI);
        namespaceTable.put(ns, URI);
    }

    /**
     * Return the namespace URI for a given namespace prefix.
     * Will return null if the namespace URI is unheard of.
     * Example: ns="xsd", returns "http://www.w3.org/2001/XMLSchema"
     */
    public String getNamespaceURI(String ns) {
        return (String) namespaceTable.get(ns);
    }

    /**
     * Return a namespace name for a given namespace URI.  One will
     * be made up, if it doesn't already exist.
     * Example: URI="http://www.w3.org/2001/XMLSchema", returns "xsd"
     */
    public String getNamespace(String URI) {
        //assert !URI.equals("xml");
        String ns;
        Iterator it = namespaceTable.keySet().iterator();
        while (it.hasNext()) {
            ns = (String) it.next();
            if (URI.equals(namespaceTable.get(ns)))
                return ns;
        }
        ns = guessPrefix(URI);
        //System.out.println("guessing ns: ns="+ns+" gets="+namespaceTable.get(ns)+" URI="+URI);
        String baseNs = ns;
        for (int count = 2; namespaceTable.containsKey(ns); ++count)
            ns = baseNs+count;
        namespaceTable.put(ns, URI);
        return ns;
    }

    /**
     * @return the Set of all namespace prefixes in use.
     */
    public Set getAllNamespaces() {
        return namespaceTable.keySet();
    }

    public String getXSDNamespace() {
        String ns = getNamespace(XSD_NS);
        if (ns == null)
            return "xsd";
        return ns;
    }

    public String getXMLNamespace() {
        String ns = getNamespace("http://www.w3.org/XML/1998/namespace");
        if (ns == null)
            return "xml";
        return ns;
    }

    public Attribute getAttribute(String name) {
        if (name == null)
            return null;
        String ns = prefixOf(name);
        if (ns == null)
            name = resolveNamespace(documentNamespace, removePrefix(name));
        else
            name = resolveNamespace(ns, removePrefix(name));
        //System.out.println("getAttribute: looking up "+name);
        Attribute result = (Attribute) definedAttributes.get(name);
        if (result == null && ns == null) {
            // try the other namespace
            name = resolveNamespace(targetNamespace, name);
            //System.out.println("getAttribute2: looking up "+name);
            result = (Attribute) definedAttributes.get(name);
        }
        return result;
    }

    protected void putSampleNode(ElementExpr ee) {
        sampleNodes.put(ee.getName(), ee);
    }

    protected ElementExpr getSampleNode(String name) {
        return (ElementExpr) sampleNodes.get(name);
    }

    public void setTargetNamespace(String ns) {
        targetNamespace = ns;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setCurrentParsedURI(String uri) {
        currentParsedURI = uri;
    }

    protected String getCurrentParsedURI() {
        return currentParsedURI;
    }

    public void setRootElement(ContainsSubElements el) {
        if (debug)
            System.out.println("Changing rootElement of "+this+" to "+el);
        rootElement = el;
    }

    public ContainsSubElements getRootElement() {
        return rootElement;
    }

    public void addToTopAnnotation(ElementExpr subElement) {
        if (topAnnotation == null)
            topAnnotation = new Annotation();
        topAnnotation.addSubElement(subElement);
    }

    public void addAppInfoToTopAnnotation(String name, String value) {
        AppInfo ai = new AppInfo();
        AnyNode ue = new AnyNode(name, value);
        ai.addSubElement(ue);
        addToTopAnnotation(ai);
    }

    public void pushCurrent(ElementExpr el) {
        if (currentElementStack.empty()) {
            //System.out.println("Pushing '"+el+"'");
            setRootElement((ContainsSubElements)el);
        } else {
            //System.out.println("Pushing '"+el+"' into '"+peekCurrent()+"'");
            peekCurrentNeedSub().addSubElement(el);
        }
        currentElementStack.push(el);
    }

    /**
     * Create the element, then push it onto the stack, making it the
     * current one.
     */
    public void pushElement(String elementName, String elementType) {
        pushCurrent(createElement(elementName, elementType));
    }

    public void pushSchemaNode() {
        pushCurrent(new SchemaNode());
    }

    public void pushComplexType() {
        pushCurrent(new ComplexType());
    }

    public void pushSequence() {
        pushCurrent(new Sequence());
    }

    public ElementExpr popCurrent() {
        lastPopped = (ElementExpr) currentElementStack.pop();
        return lastPopped;
    }

    public ElementExpr peekCurrent() {
        return (ElementExpr) currentElementStack.peek();
    }
    
    /**
     * Find an ancestor in the current element stack of a certain type.
     * Will return null if not found.
     */
    private ElementExpr findAncestor(Class type) {
        for (int i = currentElementStack.size() - 1; i >= 0; --i) {
            ElementExpr ee = (ElementExpr) currentElementStack.get(i);
            if (type.isAssignableFrom(ee.getClass()))
                return ee;
        }
        return null;
    }

    /**
     * Same thing as peekCurrent, but the caller needs it to be a
     * ContainsSubElements.  If it isn't an exception is thrown.
     */
    public ContainsSubElements peekCurrentNeedSub() {
        if (!(currentElementStack.peek() instanceof ContainsSubElements))
            throw new ClassCastException("Expected ContainsSubElements, but got "+currentElementStack.peek().getClass()+" instead on object "+currentElementStack.peek());
        return (ContainsSubElements) currentElementStack.peek();
    }

    public ElementExpr getLastPopped() {
        return lastPopped;
    }

    /**
     * Create an Element an add it to the current one in the stack.
     */
    public void addElement(String name, String type) {
        Element el = createElement(name);
        el.setType(type);
        if (currentElementStack.empty())
            setRootElement(el);
        else
            peekCurrentNeedSub().addSubElement(el);
    }

    /**
     * Create AppInfo and add it to the current thing in the stack.
     */
    public void addAppInfo(String name, String value) {
        ElementExpr e = peekCurrent();
        Annotation ann;
        if (e instanceof Annotation)
            ann = (Annotation) e;
        else
            ann = new Annotation();
        AppInfo ai = new AppInfo();
        AnyNode ue = new AnyNode(name, value);
        ai.addSubElement(ue);
        ann.addSubElement(ai);
        peekCurrentNeedSub().addSubElement(ann);
    }

    /**
     * Only works if the current thing in the stack is an Element.
     * @t should be a java type.
     */
   public void setType(String t) {
        ElementExpr e = peekCurrent();
        if (e instanceof Element)
            ((Element)e).setType(t);
        else
            throw new IllegalStateException(Common.getMessage("MSG_TryingToCallOnWrongClass", "setType", e.getClass()));
    }

    public List findAllSubElements(String name) {
        List lst = new LinkedList();
        rootElement.findAllSubElements(name, lst);
        return lst;
    }

    /**
     * Only works if the current thing in the stack is an Element.
     * @t should be a java type.
     */
    /*
   public void setDefiningType(String t) {
        ElementExpr e = peekCurrent();
        if (e instanceof Element) {
            Element el = (Element) e;
            el.setDefiningType(t);
        } else
            throw new IllegalStateException(Common.getMessage("MSG_TryingToCallOnWrongClass", "setDefiningType", e.getClass()));
    }
    */

    /**
     * Only works if the current thing in the stack is an Element.
     * @t should be a XML Schema type.
     */
   public void setXMLSchemaType(String t) {
        ElementExpr e = peekCurrent();
        if (e instanceof Element)
            ((Element)e).setXMLSchemaType(t);
        else
            throw new IllegalStateException(Common.getMessage("MSG_TryingToCallOnWrongClass", "setXMLSchemaType", e.getClass()));
    }

    /**
     * Only works if the current thing in the stack is an Element or ModelGroup.
     */
    public void setMinOccurs(String t) {
        ElementExpr e = peekCurrent();
        if (e instanceof Element)
            ((Element)e).setMinOccurs(t);
        else if (e instanceof ModelGroup)
            ((ModelGroup)e).setMinOccurs(t);
        else
            throw new IllegalStateException(Common.getMessage("MSG_TryingToCallOnWrongClass", "setMinOccurs", e.getClass()));
    }

    /**
     * Only works if the current thing in the stack is an Element or ModelGroup.
     */
    public void setMaxOccurs(String t) {
        ElementExpr e = peekCurrent();
        if (e instanceof Element)
            ((Element)e).setMaxOccurs(t);
        else if (e instanceof ModelGroup)
            ((ModelGroup)e).setMaxOccurs(t);
        else
            throw new IllegalStateException(Common.getMessage("MSG_TryingToCallOnWrongClass", "setMaxOccurs", e.getClass()));
    }

    /**
     * Return an Element that represents an ELEMENT.
     */
    public Element createElement(String name) {
        Element el = new Element(name);
        return el;
    }

    /**
     * Return an Element that represents an ELEMENT.
     */
    public Element createElement(String name, String type) {
        Element el = new Element(name);
        el.setType(type);
        return el;
    }

    /*
    public Attribute createAttirbute(String name) {
        Attribute attr = (Attribute) attributeTable.get(name);
        if (attr == null) {
            attr = new Attribute(name);
            attributeTable.put(name, attr);
        }
        return attr;
    }
    */

    public void addSubElement(String elementName, ElementExpr subElement) {
        Element el = createElement(elementName);
        el.addSubElement(subElement);
    }

    public void addSubElement(String elementName, List subElements) {
        Element el = createElement(elementName);
        el.addSubElement(subElements);
    }

    public void addSubElement(String elementName, String subElementName) {
        Element subElement = createElement(subElementName);
        addSubElement(elementName, subElement);
    }

    /**
     * This addSubElement creates an Element and then adds it underneath
     * the current one.
     */
    public void addSubElementCurrent(String subElementName) {
        Element subElement = createElement(subElementName);
        peekCurrentNeedSub().addSubElement(subElement);
    }

    /**
     * This is called before writing out a schema.
     */
    public ElementExpr optimize() {
        if (rootElement == null)
            return null;
        return rootElement.optimize();
    }

    /**
     * Returns false if the schema is too simpile to need binding classes.
     */
    /*
    public boolean needsBindingClasses() {
        if (rootElement == null)
            return false;
        System.out.println("rootElement="+rootElement+" .class="+rootElement.getClass());
        if (rootElement instanceof Element) {
            Element root = (Element) rootElement;
            return root.hasNamedSubElements();
        }
        return true;
    }
    */

    /**
     * If you want a DTD written to a Writer, this is the method
     * to call.
     */
    public void writeDTD(Writer out) throws java.io.IOException {
        if (rootElement == null)
            return;
        elementTable = new HashMap();
        optimize();
        rootElement.validate();
        StringBuffer outBuffer = new StringBuffer();
        rootElement.writeDTD(outBuffer);
        out.write(outBuffer.toString());
        elementTable = null;
    }

    /**
     * If you want an XML Schema written to a Writer, this is the method
     * to call.
     */
    public void writeXMLSchemaStandalone(Writer out) throws java.io.IOException {
        if (rootElement == null)
            return;
        XMLWriter xw = new XMLWriter();
        ContainsSubElements realRootElement = rootElement;
        if (!(realRootElement instanceof SchemaNode)) {
            SchemaNode sn = new SchemaNode();
            sn.addSubElement(realRootElement);
            realRootElement = sn;
            if (topAnnotation != null) {
                sn.addSubElement(topAnnotation);
            }
        }

        if (true || optimize() != null) {
            realRootElement.validate();
            realRootElement.writeXMLSchema(xw);
        }
        xw.writeTo(out);
    }

    public void writeXMLSchema(XMLWriter out) throws IOException {
        if (rootElement == null)
            return;
        rootElement.writeXMLSchema(out);
    }

    public void readSchemaFromLocation(String schemaLocation, boolean include) throws IOException, SAXException {
        if (debug)
            System.out.println("Reading schema from "+schemaLocation);
        if (schemaLocation == null || "".equals(schemaLocation))
            return;

        String oldParsedURI = currentParsedURI;
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = null;
            IOException ioException = null;
            SAXException saxException = null;
            try {
                doc = db.parse(schemaLocation);
                currentParsedURI = schemaLocation;
            } catch (java.io.FileNotFoundException e) {
                ioException = e;
            } catch (org.xml.sax.SAXParseException e) {
                // Apache will throw this one for a file not found error,
                // it really should be java.io.FileNotFoundException
                saxException = e;
            }
            if (ioException != null || saxException != null) {
                //System.out.println("currentParsedURI="+currentParsedURI);
                if (currentParsedURI != null) {
                    // Try making a relative URI out of this.
                    java.net.URI uri;
                    try {
                        uri = new java.net.URI(currentParsedURI);
                    } catch (java.net.URISyntaxException e) {
                        uri = new File(currentParsedURI).toURI();
                    }
                    if (debug)
                        System.out.println("uri="+uri);
                    java.net.URI schemaLocationURI = uri.resolve(schemaLocation);
                    currentParsedURI = schemaLocationURI.toString();
                    if (debug)
                        System.out.println("Since the first try failed, now trying to read currentParsedURI:"+ currentParsedURI);
                    doc = db.parse(currentParsedURI);
                    ioException = null;
                    saxException = null;
                }
            }
            if (ioException != null)
                throw ioException;
            if (saxException != null)
                throw saxException;
            org.w3c.dom.Element childNode = doc.getDocumentElement();
            if (childNode != null) {
                if (!childNode.getLocalName().equals("schema"))	// NOI18N
                    throw new IllegalStateException(Common.getMessage("MSG_ExpectedNode", "schema", childNode.getNodeName()));
                // Make sure to preserve the old stuff.
                readSchemaElement(childNode, include);
            }
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new Schema2BeansRuntimeException(Common.getMessage("MSG_FailedToParse", schemaLocation), e);
        } finally {
            currentParsedURI = oldParsedURI;
        }
    }

    public void readDocument(Document doc) {
        includedAlready.clear();
        org.w3c.dom.Element childNode = doc.getDocumentElement();
        if (childNode != null) {
            if (!childNode.getLocalName().equals("schema"))	// NOI18N
                throw new IllegalStateException(Common.getMessage("MSG_ExpectedNode", "schema", childNode.getNodeName()));
            pushSchemaNode();
            peekCurrentNeedSub().readSchema(childNode);
            readSchemaElement(childNode, false);
            popCurrent();
        }
    }

    public void readSchemaElement(org.w3c.dom.Element el, boolean include) {
        NamedNodeMap attrs = el.getAttributes();
        String theTargetNamespace = null;
        String theDocumentNamespaceURI = null;
        for (int i = 0, size = attrs.getLength(); i < size; ++i) {
            Node attr = attrs.item(i);
            //System.out.println("readSchemaElement: attr.prefix="+attr.getPrefix()+" localname="+attr.getLocalName()+" nodename="+attr.getNodeName()+" nodevalue="+attr.getNodeValue());
            if ("xmlns".equals(attr.getPrefix())) {
                addToNamespace(attr.getLocalName(), attr.getNodeValue());
            } else if ("targetNamespace".equals(attr.getNodeName())) {
                theTargetNamespace = attr.getNodeValue();
            } else if ("xmlns".equals(attr.getNodeName())) {
                theDocumentNamespaceURI = attr.getNodeValue();
            }
        }
        if (theDocumentNamespaceURI != null) {
            documentNamespace = theDocumentNamespaceURI;
            //System.out.println("readSchemaElement: just set documentNamespace to "+documentNamespace);
        }
        if (theTargetNamespace != null) {
            // This needs to be done after all the namespaces have been read
            targetNamespace = theTargetNamespace;
            //System.out.println("readSchemaElement: just set targetNamespace to "+targetNamespace);
        }
        // included document as chameleon with no target and default NS
        if (include && theDocumentNamespaceURI == null && theTargetNamespace == null
                && targetNamespace != null) {
            // spec is bit unclear on this chameleon inclusion without
            // default and target namespace
            // see http://lists.w3.org/Archives/Public/xmlschema-dev/2001Oct/0122.html
            documentNamespace = targetNamespace;
        }
        ContainsSubElements re = getRootElement();
        if (re instanceof SchemaNode) {
            SchemaNode schemaNode = (SchemaNode) re;
            schemaNode.merge(el);
        }
        read(el);
    }

    protected void read(Node node) {
        String nodeName;
        boolean ignoreUnknown = false;
        boolean keepText = false;
        if (peekCurrent() instanceof AnyNode || peekCurrent() instanceof Documentation || peekCurrent() instanceof AppInfo || peekCurrent() instanceof TextNode) {
            keepText = true;
            ignoreUnknown = true;
        }
        AnyNode anyNode = new AnyNode("dummy1", "dummy2");	// NOI18N
        TextNode textNode = new TextNode("dummy"); 	// NOI18N
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node childNode = children.item(i);
            if (childNode instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element childElement = (org.w3c.dom.Element) childNode;
                //nodeName = childElement.getNodeName().intern();
                nodeName = childElement.getLocalName().intern();
                //System.out.println("Found a "+nodeName);
                ElementExpr ee = getSampleNode(nodeName);
                if (ee == null) {
                    if (!ignoreUnknown)
                        System.out.println("SchemaRep Warning: unknown node at "+getXPathExpr(childElement));
                    anyNode.readSchema(childElement);
                } else {
                    ee.readSchema(childElement);
                }
            } else if (keepText && childNode instanceof Text) {
                textNode.readSchema((Text)childNode);
            }
        }
    }

    public static String getXPathExpr(Node node) {
        return getXPathExpr(node, true);
    }

    public static String getXPathExpr(Node node, boolean addDescription) {
        if (node instanceof Document)
            return "/";
        Node parentNode = node.getParentNode();
        if (parentNode instanceof Document)
            return "/" + node.getNodeName();
        String curNodeName = node.getNodeName();
        if (addDescription && node instanceof org.w3c.dom.Element) {
            String nameAttr = ((org.w3c.dom.Element)node).getAttribute("name");
            if (nameAttr != null && !"".equals(nameAttr))
                curNodeName += " name='"+nameAttr+"'";
        }
        return getXPathExpr(node.getParentNode(), addDescription) + "/" + curNodeName;
    }

    public String schemaTypeToJavaType(String xmlSchemaType) {
        ElementExpr schemaTypeDef = getSchemaTypeDef(xmlSchemaType);
        if (schemaTypeDef == null) {
            System.out.println(Common.getMessage("MSG_UnableToFindTypeDef", xmlSchemaType));
            return null;
        }

        String javaType = null;
        //System.out.println("schemaTypeToJavaType: "+xmlSchemaType+" schemaTypeDef="+schemaTypeDef);
        if (schemaTypeDef instanceof HasJavaTypeName) {
            javaType = ((SchemaRep.HasJavaTypeName)schemaTypeDef).getJavaTypeName();
        } else {
            System.out.println("!!! What's the java type of "+schemaTypeDef+" for "+xmlSchemaType);
        }
        //System.out.println("schemaTypeToJavaType: "+xmlSchemaType+" -> "+javaType);
        return javaType;
    }

    /**
     * Get an ElementExpr node for a named type.
     * null is an acceptable return value if not found.
     * If @param typeName is null, null is returned.
     * If @param typeName's namespace is not set, then we first
     * check the documentNamespace, then the targetNamespace.
     */
    public ElementExpr getSchemaTypeDef(String origTypeName) {
        String typeName = origTypeName;
        //System.out.println("getSchemaTypeDef: looking up "+typeName);
        if (typeName == null)
            return null;
        typeName = resolveNamespace(typeName);
        ElementExpr result = (ElementExpr) schemaTypeDefs.get(typeName);
        if (result != null)
            return result;
        String ns = prefixOf(origTypeName);
        if (ns == null)
            typeName = canonicalQName(documentNamespace,
                                      removePrefix(origTypeName));
        result = (ElementExpr) schemaTypeDefs.get(typeName);
        if (result != null)
            return result;
        /*
        if (result == null && ns == null) {
            // try the other namespace
            typeName = resolveNamespace(targetNamespace, typeName);
            System.out.println("getSchemaTypeDef2: looking up "+typeName);
            result = (ElementExpr) schemaTypeDefs.get(typeName);
        }
        */
        return (ElementExpr) definedTypes.get(origTypeName);
    }

    /**
     * Same thing as getSchemaTypeDef, but for already resolved type names.
     */
    public ElementExpr getSchemaTypeDefResolvedNamespace(String typeName) {
        ElementExpr result = (ElementExpr) schemaTypeDefs.get(typeName);
        if (result == null) {
            result = (ElementExpr) definedTypes.get(typeName);
        }
        return result;
    }

    /**
     * @return the Set of all defined element names in a format that
     *         can be passed to getSchemaTypeDefResolvedNamespace.
     */
    public Set/*<String>*/ getSchemaTypeNames() {
        return schemaTypeDefs.keySet();
    }


    public Element getDefinedElement(String typeName) {
        String t = resolveNamespace(typeName);
        return getDefinedElementResolvedNamespace(t);
    }

    /**
     * @param typeName is the resolved type name "{http://foo}login"
     * @return the Element which defines it, or null if not found.
     */
    public Element getDefinedElementResolvedNamespace(String typeName) {
        return (Element) definedTypesFull.get(typeName);
    }

    /**
     * @return the Set of all defined element names in a format that
     *         can be passed to getDefinedElementResolvedNamespace.
     */
    public Set/*<String>*/ getDefinedElementNames() {
        return definedTypesFull.keySet();
    }

    public void putSchemaTypeDef(String typeName, ElementExpr ee) {
        if (typeName == null)
            return;
        String ns = prefixOf(typeName);
        if (ns == null)
            typeName = resolveNamespace(targetNamespace, removePrefix(typeName));
        else
            typeName = resolveNamespace(ns, removePrefix(typeName));
        //System.out.println("putSchemaTypeDef: putting in "+typeName);
        schemaTypeDefs.put(typeName, ee);
    }

    /**
     * Will convert a typeName into {namespace}typeName format.
     * eg: 'xsd:string' -> '{http://www.w3.org/2001/XMLSchema}string'
     * eg: 'item' -> 'item'
     */
    public String resolveNamespace(String typeName) {
        return resolveNamespaceDefault(typeName, targetNamespace);
    }

    protected String resolveNamespaceDefault(String typeName, String defaultNS) {
        if (typeName == null)
            return null;
        String prefix = prefixOf(typeName);
        //System.out.println("resolveNamespace1: ns="+ns+" typeName="+typeName);
        typeName = resolveNamespaceDefault(prefix, defaultNS, removePrefix(typeName));
        //System.out.println("resolveNamespace2: typeName="+typeName);
        return typeName;
    }

    public String resolveNamespace(String ns, String type) {
        return resolveNamespaceDefault(ns, targetNamespace, type);
    }

    protected String resolveNamespaceDefault(String prefix, String defaultNS, String type) {
        String uri;
        if (prefix == null) {
            if (defaultNS == null)
                return type;
            uri = defaultNS;
        } else {
            uri = getNamespaceURI(prefix);
            if (uri == null) {
                System.out.println("resolveNamespace: Namespace prefix '"+prefix+"' was not found (type="+type+").");
                new Exception().printStackTrace();
            }
        }
        return canonicalQName(uri, type);
    }

    public static String canonicalQName(String namespaceURI, String localPart) {
        if (namespaceURI == null || namespaceURI.equals(""))
            return localPart;
        return "{" + namespaceURI + "}" + localPart;
    }

    /**
     * Will add a namespace prefix to a type if the targetNamespace != null.
     * eg: targetNamespace='j2ee' type='ejbType' -> 'j2ee:ejbType'
     * eg: targetNamespace='j2ee' type='xsd:string' -> 'xsd:string'
     * eg: targetNamespace=null type='item' -> 'item'
     */
    protected String normalizeTargetNamespace(String type) {
        if (type == null)
            return null;
        if (targetNamespace == null)
            return type;
        if (type.indexOf(':') >= 0)
            return type;
        return normalizeNamespace(targetNamespace, type);
    }
    
    /**
     * return prefix ':' localPart
     * eg: namespaceURI="http://www.w3.org/XML/1998/namespace" localPart="id" -> "xml:id"
     * eg: namespaceURI="" localPart="foo" -> "foo"
     * eg: namespaceURI=null localPart="bar" -> "bar"
     */
    protected String normalizeNamespace(String namespaceURI, String localPart) {
        if (localPart == null)
            return null;
        if (namespaceURI == null || "".equals(namespaceURI))
            return localPart;
        String prefix = getNamespace(namespaceURI);
        return prefix + ":" + localPart;
    }

    protected String normalizeDocumentNamespace(String type) {
        if (type == null)
            return null;
        if (documentNamespace == null)
            return type;
        if (type.indexOf(':') >= 0)
            return type;
        return normalizeNamespace(documentNamespace, type);
    }

    /**
     * @param schemaTypeName The name of a leaf schema element, like 'integer'.
     * @param javaType  The java class schemaTypeName gets mapped to, like 'int'.
     */
    public void setSchemaTypeMapping(String schemaTypeNamespace,
                                     String schemaTypeName, String javaType) {
        //System.out.println("setSchemaTypeMapping: schemaTypeNamespace="+schemaTypeNamespace+" schemaTypeName="+schemaTypeName+" javaType="+javaType);
        String ns;
        SimpleType st;
        if (schemaTypeNamespace == null) {
            st = new SimpleType(schemaTypeName, javaType);
        } else {
            ns = getNamespace(schemaTypeNamespace);
            st = new SimpleType(ns+":"+schemaTypeName, javaType);
        }
    }

    /**
     * Setup the schema to java type mapping to make more sense for J2ME.
     */
    public void setSchemaTypesForME(boolean isFloatingPoint) {
        setSchemaTypeMapping(XSD_NS, "anyURI", "java.lang.String");
        setSchemaTypeMapping(XSD_NS, "decimal", isFloatingPoint ? "double" : "java.lang.String");
        setSchemaTypeMapping(XSD_NS, "integer", "long");
        if (!isFloatingPoint) {
            setSchemaTypeMapping(XSD_NS, "double", "java.lang.String");
            setSchemaTypeMapping(XSD_NS, "float", "java.lang.String");
        }
    }

    private void insertPredefinedSchemaTypes(Map st) {
        //System.out.println("Hit insertPredefinedSchemaTypes");
        String namespace = getXSDNamespace()+":";
        String[] types = new String[] {"java.lang.String", "java.util.Calendar", "java.util.Calendar",
             "long", "int", "char", "short", "double", "float", "byte", "boolean",
             "java.lang.String", "long", "long", "long", "long", "long", "int", "short",
             "byte", "java.net.URI", "javax.xml.namespace.QName", "java.lang.String", "java.lang.String",
             "java.lang.String", "java.lang.String", "java.lang.String",
             "java.lang.String", "java.lang.String", "int", "java.lang.String", "java.lang.String",
             "java.lang.String", "java.lang.String", "java.lang.String"};
        String[] schemaType = new String[] {"string", "dateTime", "date",
             "long", "int", "char", "short", "double", "float", "byte", "boolean",
             "NMTOKEN", "positiveInteger", "nonNegativeInteger", "nonPositiveInteger", "negativeInteger", "unsignedLong", "unsignedInt", "unsignedShort",
             "unsignedByte", "anyURI", "QName", "NCName", "Name",
             "duration", "time", "ID",
             "token", "normalizedString", "gYear", "gYearMonth", "gMonthDay",
             "gDay", "gMonth", "language"};
        for (int i = 0; i < schemaType.length; ++i)
            st.put(schemaType[i], new SimpleType(namespace+schemaType[i], types[i]));
        if (useBigDataTypes) {
            st.put("decimal", new SimpleType(namespace+"decimal", "java.math.BigDecimal"));
            st.put("integer", new SimpleType(namespace+"integer", "java.math.BigInteger"));
        } else {
            st.put("decimal", new SimpleType(namespace+"decimal", "double"));
            st.put("integer", new SimpleType(namespace+"integer", "long"));
        }
        String[] whiteSpaceReplace =
            new String[] {"normalizedString"};
        for (int i = 0; i < whiteSpaceReplace.length; ++i) {
            SimpleType t = (SimpleType) st.get(whiteSpaceReplace[i]);
            Restriction restrict = new Restriction(namespace+"string");
            t.addSubElement(restrict);
            WhiteSpace ws = new WhiteSpace("replace");
            restrict.addSubElement(ws);
        }
        String[] whiteSpaceCollapse =
            new String[] {"token", "NMTOKEN", "Name", "NCName", "ID"};
        for (int i = 0; i < whiteSpaceCollapse.length; ++i) {
            SimpleType t = (SimpleType) st.get(whiteSpaceCollapse[i]);
            Restriction restrict = new Restriction(namespace+"normalizedString");
            t.addSubElement(restrict);
            WhiteSpace ws = new WhiteSpace("collapse");
            restrict.addSubElement(ws);
        }
        addRestriction((SimpleType) st.get("language"), namespace+"token",
                    new WhiteSpace("collapse"), new Pattern("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*"));
        addRestriction((SimpleType) st.get("nonPositiveInteger"), namespace+"integer", new MaxInclusive("0"));
        addRestriction((SimpleType) st.get("negativeInteger"), namespace+"nonPositiveInteger", new MaxExclusive("0"));
        addRestriction((SimpleType) st.get("nonNegativeInteger"), namespace+"integer", new MinInclusive("0"));
        addRestriction((SimpleType) st.get("unsignedLong"), namespace+"nonNegativeInteger", new MinInclusive("0"));
        addRestriction((SimpleType) st.get("unsignedInt"), namespace+"unsignedLong", new MinInclusive("0"));
        addRestriction((SimpleType) st.get("positiveInteger"), namespace+"nonNegativeInteger", new MinExclusive("0"));

        st.put("hexBinary", new HexBinary());
        st.put("base64Binary", new Base64Binary());
    }

    protected void addRestriction(ContainsSubElements cse,
                                  String restrictionBase, RestrictionType... rt) {
        Restriction restrict = new Restriction(restrictionBase);
        cse.addSubElement(restrict);
        for (RestrictionType r : rt) {
            restrict.addSubElement(r);
        }
    }

    /**
     * Map simple types to our schema.
     */
    public void mapSimpleJavaTypesOptional(Map optionalElementMap) {
        //System.out.println("Hit mapSimpleJavaTypesOptional");
        String namespace = getXSDNamespace()+":";
        // These guys are ones that we are willing to add to the XML Schema,
        // but are optional (not everyone uses everyone of them).
        String[] types = new String[] {"java.lang.Integer", "java.lang.Short", "java.lang.Long", "java.lang.Double", "java.lang.Float", "java.lang.Boolean", "java.lang.Character", "java.lang.StringBuffer", "java.lang.Byte", "java.math.BigInteger", "char[]", "char"};
        String[] restrictions = new String[] {"int", "short", "long", "double", "float", "boolean", "string", "string", "byte", "integer", "string", "string"};
        for (int i = 0; i < types.length; ++i)
            optionalElementMap.put(types[i], new SimpleType(javaType2XMLSchemaType(types[i]), new Restriction(namespace+restrictions[i])));
    }

    private void mapSimpleAttributes(Map a) {
        a.put("xml:lang", new Attribute("lang", getNamespaceURI("xml"),
                                        "xsd:string"));
    }

    /**
     * Map simple types to our schema.
     */
    public void mapSimpleJavaTypesPredefined(Map definedElementMap) {
        //System.out.println("Hit mapSimpleJavaTypesPredefined");
        String namespace = getXSDNamespace()+":";
        // These guys are already defined in XML Schema.  The java
        // types have 1-to-1 mappings.
        String[] types = new String[] {"java.lang.String", "String", "java.math.BigDecimal", "java.util.Calendar", "long", "int", "char", "short", "double", "float", "byte", "boolean"};
        String[] schemaType = new String[] {"string", "string", "decimal", "dateTime", "long", "int", "char", "short", "double", "float", "byte", "boolean"};
        for (int i = 0; i < types.length; ++i) {
            ElementExpr def = getSchemaTypeDef(schemaType[i]);
            if (def == null)
                def = new SimpleType(namespace+schemaType[i], types[i]);
            definedElementMap.put(types[i], def);
        }
    }

    /**
     * Define some helpful java to XML Schema data types that allow us
     * to go back and forth between the two (allows me to setup a 1-to-1
     * mapping with the client proxy generator).
     */
    public static void printExtraJavaTypes(Writer out, int level) throws java.io.IOException {
        // Create one of ourselves to get around the problem of calling
        // a nonstatic method (that does not need any context)
        // from a static method.
        SchemaRep schema = new SchemaRep();
        Map elementMap = new HashMap();
        schema.mapSimpleJavaTypesOptional(elementMap);
        Iterator it = elementMap.keySet().iterator();
        XMLWriter xw = new XMLWriter(false);
        while (it.hasNext()) {
            Object key = it.next();
            ElementExpr el = (ElementExpr) elementMap.get(key);
            el.writeXMLSchema(xw);
        }
        xw.writeTo(out);
        out.write("\n");	// NOI18N
    }

    private static String javaType2XMLSchemaType(String typeName) {
        return javaType2XMLSchemaType(typeName, false);
    }
    /**
     * Convert a java type into an XML Schema type.
     * eg, java.lang.String -> xsd:string
     */
    private static String javaType2XMLSchemaType(String typeName,
                                                boolean unknownOkay) {
        // BEGIN_NOI18N
        if ("java.lang.String".equals(typeName) || "String".equals(typeName))
            return "xsd:string";
        if ("java.lang.Integer".equals(typeName) ||
            "java.lang.Short".equals(typeName) ||
            "java.lang.Long".equals(typeName) ||
            "java.lang.Double".equals(typeName) ||
            "java.lang.Boolean".equals(typeName) ||
            "java.lang.Character".equals(typeName) ||
            "java.lang.Float".equals(typeName) ||
            "java.lang.StringBuffer".equals(typeName) ||
            "java.lang.Byte".equals(typeName) ||
            "java.math.BigInteger".equals(typeName) ||
            "char".equals(typeName))
            return typeName;
        if ("char[]".equals(typeName))
            return "char_lb_rb";
        if (JavaUtil.isPrimitiveType(typeName))
            return "xsd:"+typeName;
        if ("java.math.BigDecimal".equals(typeName))
            return "xsd:decimal";
        if ("java.util.Calendar".equals(typeName))
            return "xsd:dateTime";
        if ("java.net.URI".equals(typeName))
            return "xsd:anyURI";
        if ("javax.xml.namespace.QName".equals(typeName))
            return "xsd:QName";
        if (!unknownOkay) {
            String err = "javaType2XMLSchemaType: No known XML Schema type for '"+typeName+"'.";
            System.out.println(err);
            return typeName;
        }
        return null;
        // END_NOI18N
    }

    /**
     * Takes a Java type (like 'java.lang.String' or 'double') and
     * says whether or not javaType2XMLSchemaType will return a good,
     * predefined type for it.
     */
    private static boolean isXMLSchemaDefinedType(String typeName) {
        if (javaType2XMLSchemaType(typeName, true) == null)
            return false;
        return true;
    }

    public String javaType2XMLSchemaTypeComplex(String type) {
        //System.out.println("javaType2XMLSchemaTypeComplex type="+type);
        Object el;
        el = definedTypes.get(type);
        if (el == null)
            el = optionallyDefinedTypes.get(type);
        if (el == null)
            el = predefinedSchemaTypes.get(type);
        if (el == null) {
            if (debug)
                System.out.println("No type found for "+type);
            return javaType2XMLSchemaType(type);
        }
        if (el instanceof SimpleType)
            return ((SimpleType)el).getTypeName();
        if (el instanceof String)
            return (String) el;
        if (el instanceof Element)
            return ((Element)el).getElementName();
        return el.getClass().toString();
    }

    public boolean isDefinedType(String type) {
        if (optionallyDefinedTypes.containsKey(type))
            return true;
        if (definedTypes.containsKey(type))
            return true;
        return false;
    }

    public boolean isPredefinedType(String type) {
        boolean rv = false;
        if (predefinedSchemaTypes.get(type) != null)
            rv = true;
        return rv;
    }

    /*
     * Removes any namespace prefix.
     *    eg: 'xsd:element' -> 'element'
     */
    public static String removePrefix(String typeName) {
        int pos = typeName.indexOf(':');
        if (pos < 0)
            return typeName;
        return typeName.substring(pos+1);
    }

    /**
     * eg: 'xsd:element' -> 'xsd'
     */
    public static String prefixOf(String typeName) {
        int pos = typeName.indexOf(':');
        if (pos < 0)
            return null;
        return typeName.substring(0, pos);
    }

    /**
     * Guess what a prefix of this namespaceURI might be.
     */
    public static String guessPrefix(String namespaceURI) {
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI))
            return "xml";
        String prefix;
        int pos = namespaceURI.lastIndexOf('/');
        if (pos >= 0)
            prefix = namespaceURI.substring(pos+1);
        else
            prefix = namespaceURI;
        prefix = prefix.replace('#', '_').replace('\'', '_').replace('"', '_').
            replace(':', '_');
        // Not supposed to have a prefix that starts with xml
        if (prefix.startsWith("xml"))
            prefix = "x"+prefix;
        return prefix;
    }

    /**
     * Try to figure out what prefix to use for the namespaceURI
     * using the namespace context mapping, and if that fails then it
     * calls guessPrefix.
     */
    public String guessPrefixFromURI(String uri) {
        String prefix = getNamespace(uri);
        if (prefix != null)
            return prefix;
        return guessPrefix(uri);
    }
}
