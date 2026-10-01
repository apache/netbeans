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

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.schema2beansdev.gen.XMLWriter;
import org.netbeans.modules.schema2beansdev.metadd.*;

public class XMLSchemaParser extends GeneralParser implements SchemaParser {
    public static final String JAVA_TYPE_NS = "http://schema2beans.netbeans.org/javaTypes";

    //	Handler to callback with the tokens found in the Schema.
    private DocDefHandler	handler;

    private boolean debug;
    private GenBeans.Config config = null;
    private Stack parentTypes = new Stack();
    private Stack parentUniqueNames = new Stack();
    private String lastDefinedType = null;
    private boolean lastDefinedExternalType = true;
    private List<SwitchData> perAttributeExtraData = new LinkedList<>();
    private String targetNamespace;
    private Map elementsAlreadyDefined = new IdentityHashMap();

    SchemaRep schema;

    public XMLSchemaParser(GenBeans.Config config, DocDefHandler handler) {
        this.config = config;
        this.filename = config.getFilename();
        this.schemaIn = config.getFileIn();
        this.handler = handler;
        this.debug = config.isTraceParse();

        schema = new SchemaRep();
        schema.debug = debug;
        handler.setPrefixGuesser(schema);
    }

    public void process() throws java.io.IOException, Schema2BeansException {
        startupReader();
        try {
            MetaDD mdd = config.getMetaDD();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document xmlSchema = db.parse(new InputSource(reader));
            schema.setCurrentParsedURI(getReaderURI());
            if (config.isForME())
                schema.setSchemaTypesForME(true);
            overrideSchemaTypes();
            schema.readDocument(xmlSchema);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new Schema2BeansNestedException(Common.getMessage("MSG_FailedToParse", filename), e);
        } catch (org.xml.sax.SAXException e) {
            throw new Schema2BeansNestedException(Common.getMessage("MSG_FailedToParse", filename), e);
        } finally {
            shutdownReader();
        }
        if (debug) {
            PrintWriter pw = new PrintWriter(config.messageOut);
            schema.writeXMLSchemaStandalone(pw);
            pw.flush();
        }
        schema.optimize();
        handler.startDocument(config.getDocRoot());
        process(schema.getRootElement());
        handler.endDocument();
    }

    /**
     * Search thru the input beangraphs for anything that might affect
     * the definition of types from xsd: or xml:
     */
    protected void overrideSchemaTypes() {
        String xsdNS = schema.getNamespaceURI("xsd");
        String xmlNS = schema.getNamespaceURI("xml");
        for (Iterator it = config.readBeanGraphs(); it.hasNext(); ) {
            org.netbeans.modules.schema2beansdev.beangraph.BeanGraph bg = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph) it.next();
            for (int i = 0; i < bg.sizeSchemaTypeMapping(); ++i) {
                org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType stm = bg.getSchemaTypeMapping(i);
                if (xsdNS.equals(stm.getSchemaTypeNamespace()) ||
                    xmlNS.equals(stm.getSchemaTypeNamespace())) {
                    setSchemaType(stm);
                }
            }
        }
    }

    public void setSchemaType(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph bg) {
        for (int i = 0; i < bg.sizeSchemaTypeMapping(); ++i) {
            setSchemaType(bg.getSchemaTypeMapping(i));
        }
    }

    public void setSchemaType(org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType stm) {
        schema.setSchemaTypeMapping(stm.getSchemaTypeNamespace(),
                                    stm.getSchemaTypeName(),
                                    stm.getJavaType());
    }

    protected void process(SchemaRep.ElementExpr ee) throws Schema2BeansException {
        if (ee instanceof SchemaRep.Element) {
            processElement((SchemaRep.Element) ee);
        } else if (ee instanceof SchemaRep.ComplexType) {
            // named ComplexType
            processComplexType((SchemaRep.ComplexType) ee);
        } else if (ee instanceof SchemaRep.UnionType) {
            // named SimpleType
            processUnionType((SchemaRep.UnionType) ee);
        } else if (ee instanceof SchemaRep.SimpleType) {
            // named SimpleType
            processSimpleType((SchemaRep.SimpleType) ee);
        } else if (ee instanceof SchemaRep.Restriction) {
            processRestriction((SchemaRep.Restriction) ee);
        } else if (ee instanceof SchemaRep.SchemaNode) {
            processSchemaNode((SchemaRep.SchemaNode) ee);
        } else if (ee instanceof SchemaRep.ElementInformationItem) {
            if (parentTypes.empty()) {
                //config.messageOut.println("parentTypes1 is empty! ee="+ee);
            } else {
                handler.addExtraDataNode((String) parentUniqueNames.peek(),
                        (String) parentTypes.peek(), ee);
            }
        } else if (ee instanceof SchemaRep.RestrictionType) {
            // Do nothing here as RestrictionType's are handled elsewhere.
        } else if (ee instanceof SchemaRep.ModelGroup) {
            processModelGroup((SchemaRep.ModelGroup) ee);
        } else if (ee instanceof SchemaRep.Annotation) {
            processAnnotation((SchemaRep.Annotation) ee);
        } else if (ee instanceof SchemaRep.Extension) {
            processExtension((SchemaRep.Extension) ee);
        } else if (ee instanceof SchemaRep.ContainsSubElements) {
            processContainsSubElements((SchemaRep.ContainsSubElements) ee);
        } else {
            config.messageOut.println("XMLSchemaPraser.process: Hit unknown ElementExpr: "+ee);
        }

    }

    protected void processContainsSubElements(SchemaRep.ContainsSubElements cse) throws Schema2BeansException {
        Iterator it = cse.subElementsIterator();
        while (it.hasNext()) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            process(childee);
        }
    }

    protected void processContainsSubElementsAndAttributes(SchemaRep.ContainsSubElements cse, String elementName) throws Schema2BeansException {
        Iterator it = cse.subElementsIterator();
        while (it.hasNext()) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Attribute) {
                processAttribute(elementName, (SchemaRep.Attribute)childee);
            } else {
                process(childee);
            }
        }
    }

    protected void processElement(SchemaRep.Element el) throws Schema2BeansException {
        boolean alreadyDefined = false;
        if (elementsAlreadyDefined.containsKey(el))
            alreadyDefined = true;
        else
            elementsAlreadyDefined.put(el, el);
        setLastDefined(config.getDefaultElementType(), true);
        perAttributeExtraData.clear();

        String name = el.getElementName();
        SchemaRep.Restriction[] restrict = null;
        boolean externalType = false;
        String schemaType;
        String namespace = targetNamespace;
        if (el.getRef() == null) {
            schemaType = el.getXMLSchemaType();
            namespace = el.getElementNamespace();
        } else {
            SchemaRep.Element referredElement = el.getRefElement();
            if (referredElement == null) {
                config.messageOut.println("referredElement is null for "+el);
                throw new IllegalStateException("referredElement is null for "+el);
            }
            if (elementsAlreadyDefined.containsKey(referredElement))
                alreadyDefined = true;
            else
                elementsAlreadyDefined.put(referredElement, el);
            name = referredElement.getElementName();
            schemaType = referredElement.getXMLSchemaType();
            if (schemaType == null) {
                // The referred to element either does not have a type
                // associated with it, or it's type is defined in it's
                // subelements.
                schemaType = name;
            }
            String ns = schema.prefixOf(name);
            if (ns != null) {
                name = schema.removePrefix(name);
                //namespace = schema.getNamespaceURI(ns);
            }
            namespace = referredElement.getElementNamespace();
        }
        if (debug)
            config.messageOut.println("processElement (start: elementName="+name+" namespace="+namespace);
        if (name == null) {
            config.messageOut.println("WARNING: elementName is null.");
        }
        boolean definedInSubElements;
        if (schemaType == null) {
            definedInSubElements = true;
            schemaType = name;
        } else {
            definedInSubElements = false;
        }
        String fullSchemaType = schema.resolveNamespaceDefault(schemaType, namespace);
        /*
        if (schemaType.equals(fullSchemaType) && namespace != null) {
            // No namespace prefix, so add the one we're defined with.
            fullSchemaType = schema.canonicalQName(namespace, schemaType);
            System.out.println("HAD to add namespace to fullSchemaType: "+fullSchemaType);
            }*/
        String defaultValue = el.getDefault();
        if (debug)
            config.messageOut.println("processElement: name="+name+" schemaType="+schemaType+" fullSchemaType="+fullSchemaType+" definedInSubElements="+definedInSubElements);
        if (!definedInSubElements) {
            SchemaRep.ElementExpr schemaTypeDef = schema.getSchemaTypeDef(schemaType);
            if (schemaTypeDef instanceof SchemaRep.ContainsSubElements)
                if (hasUnionType((SchemaRep.ContainsSubElements)schemaTypeDef))
                    handler.setUnion(el.getFullContentName(), fullSchemaType, true);
            //config.messageOut.println("schemaType="+schemaType+" schemaTypeDef="+schemaTypeDef);
            // Look for restriction
            if (schemaTypeDef instanceof SchemaRep.ContainsSubElements) {
                SchemaRep.ContainsSubElements cse = (SchemaRep.ContainsSubElements) schemaTypeDef;
                restrict = lookForRestriction(cse);
                String foundDefault = lookForDefault(restrict);
                if (foundDefault != null)
                    defaultValue = foundDefault;
            }
            String javaType = null;
            if (schemaTypeDef instanceof SchemaRep.HasJavaTypeName) {
                javaType = ((SchemaRep.HasJavaTypeName)schemaTypeDef).getJavaTypeName();
            } else {
                String ns = schema.prefixOf(schemaType);
                String nsURI = schema.getNamespaceURI(ns);
                if (JAVA_TYPE_NS.equals(nsURI)) {
                    javaType = schema.removePrefix(schemaType);
                }
            }
            handler.element(el.getFullContentName(), fullSchemaType,
                            name, namespace,
                            getInstanceValue(el.getMinOccurs(),
                                             el.getMaxOccurs()),
                            externalType, defaultValue);
            addExtraDataForType(el.getFullContentName(), fullSchemaType, schemaTypeDef);
            if (javaType != null) {
                //config.messageOut.println("It has a java type: "+javaType);
                handler.javaType(el.getFullContentName(), fullSchemaType, javaType);
                if (parentTypes.isEmpty()) {
                    // Top level element def
                    String mySchemaType = schema.resolveNamespaceDefault(name, namespace);
                    //config.messageOut.println("mySchemaType="+mySchemaType);
                    handler.javaType(el.getFullContentName(), mySchemaType, javaType);
                }
            }
            handler.nillable(el.isNillable());
        } else {
            restrict = lookForRestriction(el);
            boolean existsAlready;
            while (!alreadyDefined) {
                existsAlready = handler.doesElementExist(fullSchemaType);
                if (!existsAlready)
                    break;
                if (debug)
                    config.messageOut.println("existsAlready: "+existsAlready+",  "+el);
                String contextName = null;
                if (!parentTypes.isEmpty()) {
                    contextName = (String) parentTypes.peek();
                    int curlyBracePos = contextName.lastIndexOf('}');
                    if (curlyBracePos >= 0)
                        contextName = contextName.substring(curlyBracePos+1);
                }
                if (contextName == null)
                    contextName = "other";
                fullSchemaType += '-' + contextName;
                if (debug)
                    config.messageOut.println("New name: "+fullSchemaType);
            }
        }
        parentTypes.push(fullSchemaType);
        parentUniqueNames.push(el.getFullContentName());
        processContainsSubElements(el);

        parentUniqueNames.pop();
        parentTypes.pop();

        if (definedInSubElements) {
            //config.messageOut.println("lastDefinedType="+lastDefinedType+" name="+name);
            // If we're a top level element and they hadn't defined the type,
            // then the sub elements defined it for us.  If they hadn't
            // defined a type, and we're not a top level element, then we now
            // know what type we are (in lastDefinedType), and we should
            // add ourselves to our enclosing element.
            if (!parentTypes.isEmpty()) {
                if (restrict != null) {
                    String foundDefault = lookForDefault(restrict);
                    if (foundDefault != null)
                        defaultValue = foundDefault;
                }
                handler.element(el.getFullContentName(), lastDefinedType,
                                name, namespace,
                                getInstanceValue(el.getMinOccurs(),
                                                 el.getMaxOccurs()),
                                lastDefinedExternalType, defaultValue);
                handler.nillable(el.isNillable());
            }
            SchemaRep.ElementExpr schemaTypeDef = schema.getSchemaTypeDefResolvedNamespace(lastDefinedType);
            if (schemaTypeDef instanceof SchemaRep.HasJavaTypeName) {
                String javaType = ((SchemaRep.HasJavaTypeName)schemaTypeDef).getJavaTypeName();
                //config.messageOut.println("javaType="+javaType);
                if (javaType != null) {
                    handler.javaType(el.getFullContentName(),
                                     lastDefinedType, javaType);
                }
            }
        }

        // Load in all of the restrictions into this element
        if (restrict != null) {
            addExtraDataCurLink(restrict);
        }
        if (!("1".equals(el.getMaxOccurs()) || "unbounded".equals(el.getMaxOccurs()))) {
            // Unusual maxOccurs amount, add a restriction
            handler.addExtraDataCurLink(new MaxOccursRestriction(el.getMaxOccurs()));
        }
        if (!("1".equals(el.getMinOccurs()) || "0".equals(el.getMinOccurs()))) {
            // Unusual minOccurs amount, add a restriction
            handler.addExtraDataCurLink(new MinOccursRestriction(el.getMinOccurs()));
        }
        if (perAttributeExtraData.size() > 0) {
            for (Iterator<SwitchData> it = perAttributeExtraData.iterator(); it.hasNext(); ) {
                handler.addExtraDataCurLink(it.next());
            }
        }

        if (debug)
            config.messageOut.println("processElement finish): elementName="+name);
    }

    protected boolean hasUnionType(SchemaRep.ContainsSubElements schemaTypeDef) {
        if (schemaTypeDef instanceof SchemaRep.UnionType)
            return true;
        Iterator itr = schemaTypeDef.subElementsIterator();
        while (itr.hasNext()) {
            SchemaRep.ElementExpr ee = (SchemaRep.ElementExpr) itr.next();
            if (ee instanceof SchemaRep.ContainsSubElements) {
                if (hasUnionType((SchemaRep.ContainsSubElements)ee))
                    return true;
            }
        }
        return false;
    }

    protected SchemaRep.Restriction[] lookForRestriction(SchemaRep.ContainsSubElements schemaTypeDef) {
        if (schemaTypeDef instanceof SchemaRep.UnionType) {
            ArrayList restrictions = new ArrayList();
            SchemaRep.Restriction restricts[] = null;
/*
            String memberTypes = ((SchemaRep.UnionType)schemaTypeDef).getMemberTypes();
            if (memberTypes != null && memberTypes.trim().length() > 0) {
                String[] members = memberTypes.trim().split(" ");
                for (int i=0; i < members.length; i++) {
                    restricts = null;
                    if (members[i].length() == 0)
                        continue;
                    SchemaRep.ElementExpr ee = schema.getSchemaTypeDef(members[i]);
                    if (ee instanceof SchemaRep.ContainsSubElements)
                        restricts = lookForRestriction((SchemaRep.ContainsSubElements)ee);
                    if (restricts != null) {
                        for (int j=0; j < restricts.length; j++)
                            restrictions.add(restricts[j]);
                    }
                }
            }
*/
            SchemaRep.ElementExpr[] eeList =
                    ((SchemaRep.UnionType)schemaTypeDef).getMemberTypeElements();
            if (eeList != null) {
                for (int i=0; i < eeList.length; i++) {
                    restricts = null;
                    if (eeList[i] instanceof SchemaRep.ContainsSubElements)
                        restricts = lookForRestriction((SchemaRep.ContainsSubElements)eeList[i]);
                    if (restricts != null) {
                        for (int j=0; j < restricts.length; j++)
                            restrictions.add(restricts[j]);
                    }
                }
            }
            Iterator itr = schemaTypeDef.subElementsIterator();
            while (itr.hasNext()) {
                restricts = null;
                SchemaRep.ElementExpr ee = (SchemaRep.ElementExpr) itr.next();
                if (ee instanceof SchemaRep.ContainsSubElements)
                    restricts = lookForRestriction((SchemaRep.ContainsSubElements)ee);
                else {
                    continue;
                }
                if (restricts != null) {
                    for (int i=0; i < restricts.length; i++)
                        restrictions.add(restricts[i]);
                }
            }
            if (restrictions.size() == 0)
                return null;
            restricts = new SchemaRep.Restriction[restrictions.size()];
            return (SchemaRep.Restriction[])restrictions.toArray(restricts);
        } else if (schemaTypeDef instanceof SchemaRep.SimpleType) {
            SchemaRep.ContainsSubElements sube =
                (SchemaRep.ContainsSubElements)
                        schemaTypeDef.findSubElement(SchemaRep.UnionType.class);
            if (sube != null)
                return lookForRestriction(sube);
            else {
                SchemaRep.Restriction restrict = 
                                (SchemaRep.Restriction)
                                    schemaTypeDef.findSubElement(
                                        SchemaRep.Restriction.class);
                if (restrict == null)
                    return null;
                ArrayList restrictions = new ArrayList();
                // FIXME:
                // If restrict.subElementsIterator().hasNext() is false, should it be
                // added to the list?
                restrictions.add(restrict);
                if (restrict.getBase() != null) {
                    String unprefixedTypeName = schema.removePrefix(restrict.getBase());
                    if (!schema.isPredefinedType(unprefixedTypeName)) {
                    	SchemaRep.ElementExpr baseee = schema.getSchemaTypeDef(restrict.getBase());
                        SchemaRep.Restriction[] baseRestrictions =
                            lookForRestriction((SchemaRep.ContainsSubElements) baseee);
                        if (baseRestrictions != null) {
                            List rList = Arrays.asList(baseRestrictions);
                            restrictions.addAll(rList);
                        }
                    }
                }
                if (restrictions.size() == 0)
                    return null;
                SchemaRep.Restriction[] restrictArray = new SchemaRep.Restriction[restrictions.size()];
                return (SchemaRep.Restriction[]) restrictions.toArray(restrictArray);
            }
        } else if (schemaTypeDef instanceof SchemaRep.SimpleContent) {
            SchemaRep.Restriction restrict = 
                            (SchemaRep.Restriction)
                                schemaTypeDef.findSubElement(
                                    SchemaRep.Restriction.class);
            if (restrict == null)
                return null;
            return (new SchemaRep.Restriction[] { restrict });
        } else if (schemaTypeDef instanceof SchemaRep.Element) {
            return lookForRestriction((SchemaRep.ContainsSubElements)schemaTypeDef.findSubElement(SchemaRep.SimpleType.class));
        } else if (schemaTypeDef instanceof SchemaRep.ComplexType) {
            return lookForRestriction((SchemaRep.ContainsSubElements)schemaTypeDef.findSubElement(SchemaRep.SimpleContent.class));
        }
        return null;
    }

    protected void processComplexType(SchemaRep.ComplexType el) throws Schema2BeansException {
        setLastDefined(null, true);
        String name = el.getTypeName();
        if (debug)
            config.messageOut.println("processComplexType: el="+el);
        if (name == null) {
            if (debug)
                config.messageOut.println("Found unnamed complexType.");
            if (!parentTypes.isEmpty())
                name = (String) parentTypes.peek();
            if (name == null)
                name = el.getFullContentName();
        } else {
            name = schema.resolveNamespace(name);
        }
        parentTypes.push(name);
        parentUniqueNames.push(el.getFullContentName());
        handler.startElement(el.getFullContentName(), name, Common.ELEMENT);
        handler.setAbstract(el.getFullContentName(), name, el.isAbstract());
        for (Iterator it = el.subElementsIterator(); it.hasNext(); ) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.ModelGroup) {
                processModelGroup((SchemaRep.ModelGroup) childee);
            } else if (childee instanceof SchemaRep.Attribute) {
                processAttribute(name, (SchemaRep.Attribute)childee);
            } else if (childee instanceof SchemaRep.AttributeGroup) {
                processAttributeGroup(name, (SchemaRep.AttributeGroup)childee);
            } else if (childee instanceof SchemaRep.SimpleContent) {
                processSimpleContent((SchemaRep.SimpleContent) childee);
            } else if (childee instanceof SchemaRep.Annotation) {
                processAnnotation((SchemaRep.Annotation) childee);
            } else if (childee instanceof SchemaRep.ComplexContent) {
                processComplexContent((SchemaRep.ComplexContent) childee);
            } else {
                config.messageOut.println("processComplexType: Unfamiliar subelement: "+childee);
            }
        }
        handler.endElement();
        parentUniqueNames.pop();
        parentTypes.pop();
        setLastDefined(name, false);
    }

    protected void processComplexContent(SchemaRep.ComplexContent el) throws Schema2BeansException {
        //config.messageOut.println("el="+el);
        for (Iterator it = el.subElementsIterator(); it.hasNext(); ) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Extension) {
                processExtension((SchemaRep.Extension) childee);
            } else if (childee instanceof SchemaRep.Restriction) {
                processRestriction((SchemaRep.Restriction) childee);
            } else if (childee instanceof SchemaRep.Annotation) {
                processAnnotation((SchemaRep.Annotation) childee);
            } else {
                config.messageOut.println("processComplexContent: Unfamiliar subelement: "+childee);
            }
        }
    }

    protected void processSimpleContent(SchemaRep.SimpleContent el) throws Schema2BeansException {
        processContainsSubElements(el);
        //System.out.println("lastDefinedType="+lastDefinedType);
        if (lastDefinedType == null)
            return;
        SchemaRep.ElementExpr schemaTypeDef = schema.getSchemaTypeDefResolvedNamespace(lastDefinedType);
        if (schemaTypeDef == null)
            return;
        //System.out.println("processSimpleContent: schemaTypeDef="+schemaTypeDef);
        String javaType = null;
        if (schemaTypeDef instanceof SchemaRep.HasJavaTypeName) {
            javaType = ((SchemaRep.HasJavaTypeName)schemaTypeDef).getJavaTypeName();
        }
        addExtraDataForType((String) parentUniqueNames.peek(), 
                (String) parentTypes.peek(), schemaTypeDef);
        if (javaType != null) {
            handler.javaType((String) parentUniqueNames.peek(), 
                    (String) parentTypes.peek(), javaType);
        }
    }

    protected void processExtension(SchemaRep.Extension el) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("extension el="+el);
        String uniqueName = (String) parentUniqueNames.peek();
        String name = (String) parentTypes.peek();
        String base = el.getBase();
        SchemaRep.ElementExpr baseDef = schema.getSchemaTypeDef(base);
        //config.messageOut.println("baseDef="+baseDef);
        SchemaRep.Restriction[] restrict = null;
        if (baseDef instanceof SchemaRep.ContainsSubElements) {
            restrict = lookForRestriction((SchemaRep.ContainsSubElements)baseDef);
            // We're extending something defined internally.
            if (!config.isRespectExtension())
                processContainsSubElementsAndAttributes((SchemaRep.ContainsSubElements)baseDef, name);
        }
        addExtraDataForType(uniqueName, name, baseDef);
        if (baseDef instanceof SchemaRep.ComplexType) {
            SchemaRep.ComplexType complexType = (SchemaRep.ComplexType) baseDef;
            String resolvedExtendsName = schema.resolveNamespace(complexType.getTypeName());
            //config.messageOut.println("resolvedExtendsName="+resolvedExtendsName);
            handler.setExtension(uniqueName, name, resolvedExtendsName);
        }
        String javaType = el.getJavaTypeName();
        if (javaType != null) {
            if (debug)
                config.messageOut.println("Setting javatype of "+name+" to "+javaType);
            handler.javaType(uniqueName, name, javaType);
            if (restrict != null) {
                addExtraDataNode(uniqueName, name, restrict);
            }
        }
        processContainsSubElementsAndAttributes(el, name);
    }

    protected void addExtraDataForType(String uniqueName, String name,
                                       SchemaRep.ElementExpr schemaTypeDef) throws Schema2BeansException {
        if (schemaTypeDef instanceof SchemaRep.Base64Binary ||
            schemaTypeDef instanceof SchemaRep.HexBinary) {
            handler.addExtraDataNode(uniqueName, name, schemaTypeDef);
            //System.out.println("Adding extradata to "+name+" of "+schemaTypeDef);
        } else if (schemaTypeDef instanceof SchemaRep.ContainsSubElements) {
            SchemaRep.Restriction[] restrict =
                lookForRestriction((SchemaRep.ContainsSubElements) schemaTypeDef);
            //System.out.println("restrict="+restrict);
            if (restrict != null)
                for (int i=0; i < restrict.length; i++)
                    addExtraDataForType(uniqueName, name,
                                    schema.getSchemaTypeDef(restrict[i].getBase()));
        } else {
            //System.out.println("name="+name+" schemaTypeDef="+schemaTypeDef);
        }
    }

    protected void processModelGroup(SchemaRep.ModelGroup group) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("processModelGroup: group="+group);
        if (group instanceof SchemaRep.Group) {
            SchemaRep.Group grp = (SchemaRep.Group) group;
            if (grp.getRef() == null) {
                // This is a group definition which only used thru
                // a reference.
                return;
            } else {
                SchemaRep.Group referredGroup = grp.getRefGroup();
                if (referredGroup == null) {
                    config.messageOut.println(Common.getMessage("MSG_UnableToFind", "group", grp.getRef()));
                } else {
                    processContainsSubElements(referredGroup);
                }
                return;
            }
        }
        char separator = ' ';
        if (group instanceof SchemaRep.Sequence)
            separator = ',';
        else if (group instanceof SchemaRep.Choice)
            separator = '|';
        int groupInstance = getInstanceValue(group.getMinOccurs(),
                                             group.getMaxOccurs());
        handler.startGroupElements();

        boolean first = true;
        Iterator it = group.subElementsIterator();
        while (it.hasNext()) {
            if (first)
                first = false;
            else
                handler.character(separator);

            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Element) {
                processElement((SchemaRep.Element) childee);
            } else if (childee instanceof SchemaRep.ModelGroup) {
                processModelGroup((SchemaRep.ModelGroup) childee);
            } else if (childee instanceof SchemaRep.Annotation) {
            } else if (childee instanceof SchemaRep.Any) {
                processAny((SchemaRep.Any) childee);
            } else {
                config.messageOut.println("processModelGroup: Unfamiliar subelement: "+childee);
            }
        }

        handler.endGroupElements(groupInstance);
    }

    protected void processSimpleType(SchemaRep.SimpleType el) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("processSimpleType: el="+el);
        /*
        String name = el.getTypeName();
        if (name != null) {
            parentTypes.push(name);
            handler.startElement(el.getFullContentName(), name, Common.ELEMENT);
        }
        */
        processContainsSubElements(el);
        //addExtraDataForType(schema.resolveNamespace(el.getTypeName()), el);
        /*
        if (name != null) {
            handler.endElement();
            parentTypes.pop();
            setLastDefined(name, true);
        }
        */
    }

    protected void processUnionType(SchemaRep.UnionType el) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("processUnionType: el="+el);
        /*
        String name = el.getTypeName();
        if (name != null) {
            parentTypes.push(name);
            handler.startElement(el.getFullContentName(), name, Common.ELEMENT);
        }
        */
/*
        String memberTypes = el.getMemberTypes();
        if (memberTypes != null && memberTypes.trim().length() > 0) {
			String[] members = memberTypes.trim().split(" ");
			for (int i=0; i < members.length; i++) {
				if (members[i].length() == 0)
					continue;
				SchemaRep.ElementExpr schemaTypeDef = schema.getSchemaTypeDef(members[i]);
				process(schemaTypeDef);
			}
		}
*/
        processContainsSubElements(el);
        //addExtraDataForType(schema.resolveNamespace(el.getTypeName()), el);
        /*
        if (name != null) {
            handler.endElement();
            parentTypes.pop();
            setLastDefined(name, true);
        }
        */
    }

    protected void processRestriction(SchemaRep.Restriction el) throws Schema2BeansException {
        /*
        boolean externalType = true;
        String typeName = el.getJavaTypeName();
        if (typeName == null) {
            externalType = false;
            typeName = el.getBase();
        } else {
            setLastDefined(typeName, externalType);
        }
        */
        //System.out.println("processRestriction: el.base="+el.getBase()+" el.javaType="+el.getJavaTypeName());
        setLastDefined(schema.resolveNamespace(el.getBase()), false);
        //setLastDefined(el.getBase(), false);
        //handler.element(el.getFullContentName(), typeName, typeName, Common.TYPE_1, externalType);
        processContainsSubElements(el);
    }

    protected void processAny(SchemaRep.Any el) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("Found "+el);
        String namespace = el.getNamespace();
        if (namespace != null && namespace.startsWith("##"))
            namespace = null;
        handler.element(el.getFullContentName(), "any",
                        "any", namespace,
                        getInstanceValue(el.getMinOccurs(),
                                         el.getMaxOccurs()),
                        true, null);
        handler.javaType("any", "any", "org.w3c.dom.Element");
        handler.addExtraDataCurLink(el);
    }

    protected void processSchemaNode(SchemaRep.SchemaNode sn) throws Schema2BeansException {
        targetNamespace = sn.getTargetNamespace();
        if (targetNamespace != null && !"".equals(targetNamespace))
            handler.setDefaultNamespace(targetNamespace);
        processContainsSubElements(sn);
    }

    protected void setLastDefined(String typeName) {
        //config.messageOut.println("setLastDefined: typeName="+typeName);
        this.lastDefinedType = typeName;
        this.lastDefinedExternalType = false;
    }

    protected void setLastDefined(String typeName, boolean externalType) {
        //config.messageOut.println("setLastDefined: typeName="+typeName);
        this.lastDefinedType = typeName;
        this.lastDefinedExternalType = externalType;
    }

    protected void addTopAttributes(SchemaRep.Element parentElement,
                                    SchemaRep.Element el) {
        //config.messageOut.println("fullContentName="+el.getFullContentName());
        Iterator it = el.subElementsIterator();
        while (it.hasNext()) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Attribute) {
                //addTopAttributes(parentElement, (SchemaRep.Attribute) childee);
            } else if (childee instanceof SchemaRep.AttributeGroup) {
                //addTopAttributes(parentElement, (SchemaRep.AttributeGroup) childee);
            } else if (childee instanceof SchemaRep.ComplexType) {
                addTopAttributes(parentElement, (SchemaRep.ComplexType) childee);
            }
        }
    }

    protected void addTopAttributes(SchemaRep.Element parentElement,
                                    SchemaRep.ComplexType el) {
        Iterator it = el.subElementsIterator();
        while (it.hasNext()) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Attribute) {
                //addTopAttributes(parentElement, (SchemaRep.Attribute) childee);
            } else if (childee instanceof SchemaRep.AttributeGroup) {
                //addTopAttributes(parentElement, (SchemaRep.AttributeGroup) childee);
            }
        }
    }

    protected void processAttribute(String parentElement,
                                    SchemaRep.Attribute attr) throws Schema2BeansException {
        if (debug)
            config.messageOut.println("processAttribute to "+parentElement+" attr="+attr);
        if (attr.getRef() != null) {
            SchemaRep.Attribute referredAttr = attr.getRefAttribute();
            if (referredAttr == null) {
                config.messageOut.println(Common.getMessage("MSG_UnableToFind", "attribute", attr.getRef()));
            } else {
                processAttribute(parentElement, referredAttr);
            }
            return;
        }

        //config.messageOut.println("fullContentName="+attr.getFullContentName());
        String attributeName = attr.getAttributeName();
        boolean externalType = true;
        String attrType = attr.getJavaType();
        String schemaType = attr.getType();
        String defaultValue = attr.getDefaultValue();
        SchemaRep.Restriction[] restrict = null;
        SchemaRep.ElementExpr ee = null;
        if (schemaType != null && defaultValue == null) {
            ee = schema.getSchemaTypeDef(schemaType);
            if (ee instanceof SchemaRep.SimpleType) {
                SchemaRep.SimpleType st = (SchemaRep.SimpleType) ee;
                SchemaRep.Restriction r =
                        (SchemaRep.Restriction) st.findSubElement("restriction");
                if (r != null)
                    restrict = new SchemaRep.Restriction[] { r };
                //config.messageOut.println("restrict="+restrict);
                String foundDefault = lookForDefault(restrict);
                if (foundDefault != null)
                    defaultValue = foundDefault;
            } else {
                config.messageOut.println("Type for attribute "+attributeName+" is not simple enough: "+ee);
            }
            //config.messageOut.println("defaultValue="+defaultValue);
        }
        int instance;
        if (defaultValue != null || attr.getFixed() != null || attr.isRequired())
            instance = Common.TYPE_1;
        else {
            instance = Common.TYPE_0_1;
        }
        handler.startElement(attr.getFullContentName(),
                             parentElement,
                             Common.ATTLIST);
        handler.element(attr.getFullContentName(), attrType,
                        attributeName, attr.getAttributeNamespace(),
                        instance, externalType, defaultValue);
        handler.element("CDATA", "CDATA", instance);
        if (attr.getFixed() != null) {
            handler.element("#FIXED", "#FIXED", instance);	// NOI18N
            handler.element(attr.getFullContentName(), attrType,
                            attr.getFixed(), null, instance, externalType,
                            defaultValue);
        } else if (attr.isRequired()) {
            handler.element("#REQUIRED", "#REQUIRED", instance);	// NOI18N
        } else {
            handler.element("#IMPLIED", "#IMPLIED", instance);	// NOI18N
        }
        handler.javaType(attr.getFullContentName(), attr.getAttributeName(),
                         attrType);
        if (ee != null) {
            addExtraDataForType(attr.getFullContentName(), attr.getAttributeName(), ee);
        }
        if (restrict != null) {
            addExtraDataCurLink(restrict);
        }
        handler.endElement();
    }

    protected void addExtraDataCurLink(SchemaRep.Restriction[] restrict) {
        //config.messageOut.println("restrict="+restrict);
        for (int i=0; i < restrict.length; i++)
            if (restrict[i].subElementsIterator().hasNext())
                handler.addExtraDataCurLink(restrict[i]);
/*
            for (Iterator it = restrict[i].subElementsIterator(); it.hasNext(); ) {
                Object o = it.next();
                if (o instanceof SchemaRep.RestrictionType) {
                    //config.messageOut.println("Adding RestrictionType: "+o);
                    handler.addExtraDataCurLink(o);
                }
            }
*/
    }

    protected void addExtraDataNode(String uniqueName, String name,
                                    SchemaRep.Restriction[] restrict) throws org.netbeans.modules.schema2beans.Schema2BeansException {
        for (int i=0; i < restrict.length; i++)
            if (restrict[i].subElementsIterator().hasNext())
                handler.addExtraDataNode(uniqueName, name, restrict[i]);
/*
            for (Iterator it = restrict[i].subElementsIterator(); it.hasNext(); ) {
                Object o = it.next();
                if (o instanceof SchemaRep.RestrictionType) {
                    handler.addExtraDataNode(name, o);
                }
            }
*/
    }

    /**
     * Given the restrictions, can we pick a decent default.
     */
    protected String lookForDefault(SchemaRep.Restriction[] restrict) {
        if (config.isMakeDefaults() && restrict != null) {
            for (int i=0; i < restrict.length; i++)
                for (Iterator subelements = restrict[i].subElementsIterator();
                     subelements.hasNext(); ) {
                    Object rt = subelements.next();
                    //config.messageOut.println("rt="+rt);
                    // If we find an Enumeration, then let's pick the
                    // first value as the default, since it's gotta be
                    // one of the values.
                    if (rt instanceof SchemaRep.Enumeration) {
                        String defaultValue = ((SchemaRep.Enumeration)rt).getValue();
                        //config.messageOut.println("Found defaultValue="+defaultValue);
                        return defaultValue;
                    }
                }
        }
        return null;
    }

    protected void processAttributeGroup(String parentElement,
                                         SchemaRep.AttributeGroup attrGroup) throws Schema2BeansException {
        SchemaRep.AttributeGroup schemaTypeDef = (SchemaRep.AttributeGroup) schema.getSchemaTypeDef(attrGroup.getRef());
        if (debug)
            config.messageOut.println("processAttributeGroup schemaTypeDef="+schemaTypeDef);
        if (schemaTypeDef == null)
            throw new IllegalStateException("attributeGroup ref has reference to unknown name: "+attrGroup.getRef());

        Iterator it = schemaTypeDef.subElementsIterator();
        while (it.hasNext()) {
            SchemaRep.ElementExpr childee = (SchemaRep.ElementExpr) it.next();
            if (childee instanceof SchemaRep.Attribute) {
                processAttribute(parentElement, (SchemaRep.Attribute) childee);
            } else if (childee instanceof SchemaRep.AttributeGroup) {
                processAttributeGroup(parentElement, (SchemaRep.AttributeGroup) childee);
            }
        }
    }

    protected void processAnnotation(SchemaRep.Annotation ann) throws Schema2BeansException {
        SchemaRep.Documentation doc = (SchemaRep.Documentation) ann.findSubElement(SchemaRep.Documentation.class);
        String name = null;
        if (!parentTypes.isEmpty())
            name = (String) parentTypes.peek();
        if (name == null)
            return;
        String uniqueName = (String) parentUniqueNames.peek();
        if (doc != null) {
            StringBuffer comment = new StringBuffer();
            for (Iterator subelements = doc.subElementsIterator();
                 subelements.hasNext(); ) {
                SchemaRep.ElementExpr el = (SchemaRep.ElementExpr) subelements.next();
                if (el instanceof SchemaRep.TextNode) {
                    comment.append(((SchemaRep.TextNode)el).getText());
                } else if (el instanceof SchemaRep.AnyNode) {
                    try {
                        XMLWriter xw = new XMLWriter(false);
                        ((SchemaRep.AnyNode)el).writeXMLSchema(xw);
                        xw.writeTo(comment);
                    } catch (IOException e) {
                        // Should not occur
                        throw new RuntimeException(e);
                    }
                }
            }
            handler.setExtendedProperty(uniqueName, name, "comment", comment.toString());
        }
        SchemaRep.AppInfo appInfo = (SchemaRep.AppInfo) ann.findSubElement(SchemaRep.AppInfo.class);
        if (appInfo != null) {
            String switchName = null;
            String switchHelp = null;
            boolean switchMandatory = false;
            for (Iterator subelements = appInfo.subElementsIterator();
                 subelements.hasNext(); ) {
                SchemaRep.ElementExpr el = (SchemaRep.ElementExpr) subelements.next();
                if (el instanceof SchemaRep.AnyNode) {
                    SchemaRep.AnyNode anyNode = (SchemaRep.AnyNode) el;
                    String anyNodeName = anyNode.getContentName();
                    if (anyNodeName == null)
                        continue;
                    anyNodeName = anyNodeName.intern();
                    if ("extends" == anyNodeName) {
                        SchemaRep.TextNode value = (SchemaRep.TextNode) anyNode.findSubElement(SchemaRep.TextNode.class);
                        if (value != null) {
                            handler.setExtendedProperty(uniqueName, name, "extends", value.getText());
                        }
                    } else if ("implements" == anyNodeName) {
                        SchemaRep.TextNode value = (SchemaRep.TextNode) anyNode.findSubElement(SchemaRep.TextNode.class);
                        if (value != null) {
                            handler.setExtendedProperty(uniqueName, name, "implements", value.getText());
                        }
                    } else if ("switch" == anyNodeName) {
                        SchemaRep.TextNode value = (SchemaRep.TextNode) anyNode.findSubElement(SchemaRep.TextNode.class);
                        if (value != null)
                            switchName = value.getText();
                    } else if ("switchHelp" == anyNodeName) {
                        SchemaRep.TextNode value = (SchemaRep.TextNode) anyNode.findSubElement(SchemaRep.TextNode.class);
                        if (value != null)
                            switchHelp = value.getText();
                    } else if ("switchMandatory" == anyNodeName) {
                        SchemaRep.TextNode value = (SchemaRep.TextNode) anyNode.findSubElement(SchemaRep.TextNode.class);
                        if (value != null) {
                            switchMandatory = "true".equalsIgnoreCase(value.getText());
                        }
                    }
                }
            }
            if (switchName != null) {
                perAttributeExtraData.add(new SwitchData(switchName,
                                                         switchHelp,
                                                         switchMandatory));
            }
        }
    }

    protected static int getInstanceValue(String minOccurs, String maxOccurs) {
        if (minOccurs == null)
            minOccurs = "1";
        if (maxOccurs == null)
            maxOccurs = "1";

        if (minOccurs.equals("0")) {
            if (maxOccurs.equals("1"))
                return Common.TYPE_0_1;
            //if (maxOccurs.equals("unbounded"))
            return Common.TYPE_0_N;
        }
        if (maxOccurs.equals("1"))
            return Common.TYPE_1;
        return Common.TYPE_1_N;
    }

    public static class MaxOccursRestriction implements DataListRestriction/*, HasAnnotation*/ {
        private String maxOccurs;

        public MaxOccursRestriction(String maxOccurs) {
            //assert "unbounded".equalsIgnoreCase(maxOccurs) || Integer.parseInt(maxOccurs) >= 0;
            this.maxOccurs = maxOccurs;
        }

        public void genRestriction(Writer out, String sizeExpr,
                                   String readMethod, String type,
                                   String failVar, boolean passCheck)
                throws IOException {
            if (!passCheck) {
                out.write("if ("+sizeExpr+" > "+maxOccurs+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+sizeExpr+" <= "+maxOccurs+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
        
        public int getMaxOccurs() {
            if ("unbounded".equalsIgnoreCase(maxOccurs))
                return Integer.MAX_VALUE;
            return Integer.parseInt(maxOccurs);
        }

        public String toString() {
            return "maxOccurs ("+maxOccurs+")";
        }

        public String genAnnotation() {
            return "MaxOccurs("+maxOccurs+")";
        }

    }

    public static class MinOccursRestriction implements DataListRestriction/*, HasAnnotation*/ {
        private String minOccurs;

        public MinOccursRestriction(String minOccurs) {
            //assert Integer.parseInt(minOccurs) >= 0;
            this.minOccurs = minOccurs;
        }

        public void genRestriction(Writer out, String sizeExpr,
                                   String readMethod, String type,
                                   String failVar, boolean passCheck)
                throws IOException {
            if (!passCheck) {
                out.write("if ("+sizeExpr+" < "+minOccurs+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            } else {
                out.write("if ("+sizeExpr+" >= "+minOccurs+") {\n");
                out.write(failVar+" = true;\n");
                out.write("}\n");
            }
        }
        
        public int getMinOccurs() {
            return Integer.parseInt(minOccurs);
        }

        public String toString() {
            return "minOccurs ("+minOccurs+")";
        }
        
        public String genAnnotation() {
            return "MinOccurs("+minOccurs+")";
        }

    }
    
    public static class GeneralAnnotation implements HasAnnotation {
        private String annotation;
        
        public GeneralAnnotation(String annotation) {
            this.annotation = annotation;
        }
        
        public String genAnnotation() {
            return annotation;
        }
    }

    public static class SwitchData {
        private String switchName;
        private String switchHelp;
        private boolean mandatory;

        public SwitchData(String switchName) {
            this.switchName = switchName;
        }

        public SwitchData(String switchName, String switchHelp) {
            this.switchName = switchName;
            this.switchHelp = switchHelp;
        }

        public SwitchData(String switchName, String switchHelp, boolean mandatory) {
            this.switchName = switchName;
            this.switchHelp = switchHelp;
            this.mandatory = mandatory;
        }

        public String getName() {
            return switchName;
        }

        public String getHelp() {
            return switchHelp;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public String toString() {
            return "Switch";
        }
    }
}
