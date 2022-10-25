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

package org.netbeans.modules.xsl.grammar;

import java.io.IOException;
import java.util.*;
import javax.swing.Icon;
//import org.apache.xpath.XPathAPI;

import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.modules.xsl.api.XSLCustomizer;
import org.openide.loaders.DataObject;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements code completion for XSL transformation files.
 * XSL elements in the completion are hardcoded from the XSLT spec, but the
 * result elements are gathered from the "doctype-public" and "doctype-system"
 * attributes of the xsl:output element.
 *
 * @author  asgeir@dimonsoftware.com
 */
public final class XSLGrammarQuery implements GrammarQuery{

    private DataObject dataObject;

    /** Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL children. Neither the element name keys nor the names in the
     * value set should contain the namespace prefix.
     */
    private static Map<String, Set<String>> elementDecls;

    /** Contains a mapping from XSL namespace element names to set of names of
     * allowed XSL attributes for that element.  The element name keys should
     * not contain the namespace prefix.
     */
    private static Map<String, Set<String>> attrDecls;

    /** A Set of XSL attributes which should be allowd for result elements*/
    private static Set<String> resultElementAttr;

    /** An object which indicates that result element should be allowed in a element Set */
    private static String resultElements = "RESULT_ELEMENTS_DUMMY_STRING"; // NOI18N

    /** A Set of elements which should be allowed at template level in XSL stylesheet */
    private static Set<String> template;

    /** Contains a mapping from XSL namespace element names to an attribute name which
     * should contain XPath expression.  The element name keys should
     * not contain the namespace prefix.
     */
    private static Map<String, String> exprAttributes;

    /** A set containing all functions allowed in XSLT */
    private static Set<String> xslFunctions;

    /** A set containing XPath axes */
    private static Set<String> xpathAxes;

    /** A list of prefixes using the "http://www.w3.org/1999/XSL/Transform" namespace
     * defined in the context XSL document.  The first prefix in the list is the actual XSL
     * transformation prefix, which is normally defined on the xsl:stylesheet element.
     */
    private List<String> prefixList = new LinkedList<>();

    /** A GrammarQuery for the result elements created for the doctype-public" and
     * "doctype-system" attributes of the xsl:output element.*/
    private GrammarQuery resultGrammarQuery;

    /** The value of the system identifier of the DTD which was used when
     * resultGrammarQuery was previously created */
    private String lastDoctypeSystem;

    /** The value of the public identifier of the DTD which was used when
     * resultGrammarQuery was previously created */
    private String lastDoctypePublic;

    // we cannot parse SGML DTD for HTML, let emulate it by XHTML DTD
    private static final String XHTML_PUBLIC_ID =
            System.getProperty("netbeans.xsl.html.public", "-//W3C//DTD XHTML 1.0 Transitional//EN");  // NOI18N

    // we cannot parse SGML DTD for HTML, let emulate it by XHTML DTD
    private static final String XHTML_SYSTEM_ID =
            System.getProperty("netbeans.xsl.html.system", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"); // NOI18N

    // namespace that this grammar supports
    public static final String XSLT_NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform"; // NOI18N

    /** Folder which stores instances of custom external XSL customizers */
    private static final String CUSTOMIZER_FOLDER = "Plugins/XML/XSLCustomizer"; // NOI18N

    private XSLCustomizer customizer = null;

    private ResourceBundle bundle = NbBundle.getBundle(XSLGrammarQuery.class);

    /** Creates a new instance of XSLGrammarQuery */
    public XSLGrammarQuery(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    //////////////////////////////////////////7
    // Getters for the static members

    private static Map getElementDecls() {
        if (elementDecls == null) {
            elementDecls = new HashMap<>();
            attrDecls = new HashMap<>();

            // Commonly used variables
            Set<String> emptySet = new TreeSet<>();
            Set<String> tmpSet;
            String spaceAtt = "xml:space";  // NOI18N

            ////////////////////////////////////////////////
            // Initialize common sets

            Set<String> charInstructions = new TreeSet<>(Arrays.asList(new String[]{"apply-templates", // NOI18N
            "call-template","apply-imports","for-each","value-of", // NOI18N
            "copy-of","number","choose","if","text","copy", // NOI18N
            "variable","message","fallback"})); // NOI18N

            Set<String> instructions = new TreeSet<>(charInstructions);
            instructions.addAll(Arrays.asList(new String[]{"processing-instruction", // NOI18N
            "comment","element","attribute"})); // NOI18N

            Set<String> charTemplate = charInstructions; // We don't care about PCDATA

            template = new TreeSet<>(instructions);
            template.add(resultElements);

            Set<String> topLevel = new TreeSet<>(Arrays.asList(new String[]{"import","include","strip-space", // NOI18N
            "preserve-space","output","key","decimal-format","attribute-set", // NOI18N
            "variable","param","template","namespace-alias"})); // NOI18N

            Set<String> topLevelAttr = new TreeSet<>(Arrays.asList(new String[]{"extension-element-prefixes", // NOI18N
            "exclude-result-prefixes","id","version",spaceAtt})); // NOI18N

            resultElementAttr = new TreeSet<>(Arrays.asList(new String[]{"extension-element-prefixes", // NOI18N
            "exclude-result-prefixes","use-attribute-sets","version"})); // NOI18N

            ////////////////////////////////////////////////
            // Add items to elementDecls and attrDecls maps

            // xsl:stylesheet
            elementDecls.put("stylesheet", topLevel); // NOI18N
            attrDecls.put("stylesheet", topLevelAttr); // NOI18N

            // xsl:transform
            elementDecls.put("transform", topLevel); // NOI18N
            attrDecls.put("transform", topLevelAttr); // NOI18N

            // xsl:import
            elementDecls.put("import", emptySet); // NOI18N
            attrDecls.put("import", new TreeSet<>(Arrays.asList(new String[]{"href"}))); // NOI18N

            // xxsl:include
            elementDecls.put("include", emptySet); // NOI18N
            attrDecls.put("include", new TreeSet<>(Arrays.asList(new String[]{"href"}))); // NOI18N

            // xsl:strip-space
            elementDecls.put("strip-space", emptySet); // NOI18N
            attrDecls.put("strip-space", new TreeSet<>(Arrays.asList(new String[]{"elements"}))); // NOI18N

            // xsl:preserve-space
            elementDecls.put("preserve-space", emptySet); // NOI18N
            attrDecls.put("preserve-space", new TreeSet<>(Arrays.asList(new String[]{"elements"}))); // NOI18N

            // xsl:output
            elementDecls.put("output", emptySet); // NOI18N
            attrDecls.put("output", new TreeSet<>(Arrays.asList(new String[]{"method", // NOI18N
            "version","encoding","omit-xml-declaration","standalone","doctype-public", // NOI18N
            "doctype-system","cdata-section-elements","indent","media-type"}))); // NOI18N

            // xsl:key
            elementDecls.put("key", emptySet); // NOI18N
            attrDecls.put("key", new TreeSet<>(Arrays.asList(new String[]{"name","match","use"}))); // NOI18N

            // xsl:decimal-format
            elementDecls.put("decimal-format", emptySet); // NOI18N
            attrDecls.put("decimal-format", new TreeSet<>(Arrays.asList(new String[]{"name", // NOI18N
            "decimal-separator","grouping-separator","infinity","minus-sign","NaN", // NOI18N
            "percent","per-mille","zero-digit","digit","pattern-separator"}))); // NOI18N

            // xsl:namespace-alias
            elementDecls.put("namespace-alias", emptySet); // NOI18N
            attrDecls.put("namespace-alias", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "stylesheet-prefix","result-prefix"}))); // NOI18N

            // xsl:template
            tmpSet = new TreeSet<>(instructions);
            tmpSet.add(resultElements);
            tmpSet.add("param"); // NOI18N
            elementDecls.put("template", tmpSet); // NOI18N
            attrDecls.put("template", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "match","name","priority","mode",spaceAtt}))); // NOI18N

            // xsl:value-of
            elementDecls.put("value-of", emptySet); // NOI18N
            attrDecls.put("value-of", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
            "select","disable-output-escaping"}))); // NOI18N

            // xsl:copy-of
            elementDecls.put("copy-of", emptySet); // NOI18N
            attrDecls.put("copy-of", new TreeSet<>(Arrays.asList(new String[]{"select"}))); // NOI18N

            // xsl:number
            elementDecls.put("number", emptySet); // NOI18N
            attrDecls.put("number", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "level","count","from","value","format","lang","letter-value", // NOI18N
                "grouping-separator","grouping-size"}))); // NOI18N

            // xsl:apply-templates
            elementDecls.put("apply-templates", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "sort","with-param"}))); // NOI18N
            attrDecls.put("apply-templates", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "select","mode"}))); // NOI18N

            // xsl:apply-imports
            elementDecls.put("apply-imports", emptySet); // NOI18N
            attrDecls.put("apply-imports", emptySet); // NOI18N

            // xsl:for-each
            tmpSet = new TreeSet<>(instructions);
            tmpSet.add(resultElements);
            tmpSet.add("sort"); // NOI18N
            elementDecls.put("for-each", tmpSet); // NOI18N
            attrDecls.put("for-each", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
            "select",spaceAtt}))); // NOI18N

            // xsl:sort
            elementDecls.put("sort", emptySet); // NOI18N
            attrDecls.put("sort", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "select","lang","data-type","order","case-order"}))); // NOI18N

            // xsl:if
            elementDecls.put("if", template); // NOI18N
            attrDecls.put("if", new TreeSet<>(Arrays.asList(new String[]{"test",spaceAtt}))); // NOI18N

            // xsl:choose
            elementDecls.put("choose", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "when","otherwise"}))); // NOI18N
            attrDecls.put("choose", new TreeSet<>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:when
            elementDecls.put("when", template); // NOI18N
            attrDecls.put("when", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "test",spaceAtt}))); // NOI18N

            // xsl:otherwise
            elementDecls.put("otherwise", template); // NOI18N
            attrDecls.put("otherwise", new TreeSet<>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:attribute-set
            elementDecls.put("sort", new TreeSet<>(Arrays.asList(new String[]{"attribute"}))); // NOI18N
            attrDecls.put("attribute-set", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","use-attribute-sets"}))); // NOI18N

            // xsl:call-template
            elementDecls.put("call-template", new TreeSet<>(Arrays.asList(new String[]{"with-param"}))); // NOI18N
            attrDecls.put("call-template", new TreeSet<>(Arrays.asList(new String[]{"name"}))); // NOI18N

            // xsl:with-param
            elementDecls.put("with-param", template); // NOI18N
            attrDecls.put("with-param", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:variable
            elementDecls.put("variable", template); // NOI18N
            attrDecls.put("variable", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:param
            elementDecls.put("param", template); // NOI18N
            attrDecls.put("param", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","select"}))); // NOI18N

            // xsl:text
            elementDecls.put("text", emptySet); // NOI18N
            attrDecls.put("text", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "disable-output-escaping"}))); // NOI18N

            // xsl:processing-instruction
            elementDecls.put("processing-instruction", charTemplate); // NOI18N
            attrDecls.put("processing-instruction", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name",spaceAtt}))); // NOI18N

            // xsl:element
            elementDecls.put("element", template); // NOI18N
            attrDecls.put("element", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","namespace","use-attribute-sets",spaceAtt}))); // NOI18N

            // xsl:attribute
            elementDecls.put("attribute", charTemplate); // NOI18N
            attrDecls.put("attribute", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                "name","namespace",spaceAtt}))); // NOI18N

            // xsl:comment
            elementDecls.put("comment", charTemplate); // NOI18N
            attrDecls.put("comment", new TreeSet<>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N

            // xsl:copy
            elementDecls.put("copy", template); // NOI18N
            attrDecls.put("copy", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                spaceAtt,"use-attribute-sets"}))); // NOI18N

            // xsl:message
            elementDecls.put("message", template); // NOI18N
            attrDecls.put("message", new TreeSet<>(Arrays.asList(new String[]{ // NOI18N
                spaceAtt,"terminate"}))); // NOI18N

            // xsl:fallback
            elementDecls.put("fallback", template); // NOI18N
            attrDecls.put("fallback", new TreeSet<>(Arrays.asList(new String[]{spaceAtt}))); // NOI18N
        }
        return elementDecls;
    }

    private static Map getAttrDecls() {
        if (attrDecls == null) {
            getElementDecls();
        }
        return attrDecls;
    }

    private static Set getResultElementAttr() {
        if (resultElementAttr == null) {
            getElementDecls();
        }
        return resultElementAttr;
    }

    private static Set getTemplate() {
        if (template == null) {
            getElementDecls();
        }
        return template;
    }

    private static Set getXslFunctions() {
        if (xslFunctions == null) {
            xslFunctions = new TreeSet<>(Arrays.asList(new String[]{
                "boolean(","ceiling(","concat(", "contains(","count(","current()","document(", // NOI18N
                "false()", "floor(","format-number(","generate-id(", // NOI18N
                "id(","local-name(","key(","lang(","last()","name(","namespace-uri(", "normalize-space(", // NOI18N
                "not(","number(","position()","round(","starts-with(","string(", // NOI18N
                "string-length(", "substring(","substring-after(","substring-before(", "sum(", // NOI18N
                "system-property(","translate(",   "true()","unparsed-entity-uri("})); // NOI18N
        }
        return xslFunctions;
    }

    private static Set getXPathAxes() {
        if (xpathAxes == null) {
            xpathAxes = new TreeSet<>(Arrays.asList(new String[]{"ancestor::", "ancestor-or-self::", // NOI18N
            "attribute::", "child::", "descendant::", "descendant-or-self::", "following::", // NOI18N
            "following-sibling::", "namespace::", "parent::", "preceding::", // NOI18N
            "preceding-sibling::", "self::"})); // NOI18N
        }
        return xpathAxes;
    }

    private static Map getExprAttributes() {
        if (exprAttributes == null) {
            exprAttributes = new HashMap<>();
            exprAttributes.put("key", "use"); // NOI18N
            exprAttributes.put("value-of", "select"); // NOI18N
            exprAttributes.put("copy-of", "select"); // NOI18N
            exprAttributes.put("number", "value"); // NOI18N
            //??? what about match one
            exprAttributes.put("apply-templates", "select"); // NOI18N
            exprAttributes.put("for-each", "select"); // NOI18N
            exprAttributes.put("sort", "select"); // NOI18N
            exprAttributes.put("if", "test"); // NOI18N
            exprAttributes.put("when", "test"); // NOI18N
            exprAttributes.put("with-param", "select"); // NOI18N
            exprAttributes.put("variable", "select"); // NOI18N
            exprAttributes.put("param", "select"); // NOI18N
        }
        return exprAttributes;
    }



    ////////////////////////////////////////////////////////////////////////////////
    // GrammarQuery interface fulfillment

    /**
     * Support completions of elements defined by XSLT spec and by the <output>
     * doctype attribute (in result space).
     */
    public Enumeration queryElements(HintContext ctx) {
        Node node = ((Node)ctx).getParentNode();

        String prefix = ctx.getCurrentPrefix();
        QueueEnumeration list = new QueueEnumeration();

        if (node instanceof Element) {
            Element el = (Element) node;
            updateProperties(el);
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();

            String firstXslPrefixWithColon = prefixList.get(0) + ":"; // NOI18N
            Set elements;
            if (el.getTagName().startsWith(firstXslPrefixWithColon)) {
                String parentNCName = el.getTagName().substring(firstXslPrefixWithColon.length());
                elements = (Set) getElementDecls().get(parentNCName);
            } else {
                // Children of result elements should always be the template set
                elements = getTemplate();
            }

            // First we add the Result elements
            if (elements != null  && resultGrammarQuery != null && elements.contains(resultElements)) {
                ResultHintContext resultHintContext = new ResultHintContext(ctx, firstXslPrefixWithColon, null);
                Enumeration resultEnum = resultGrammarQuery.queryElements(resultHintContext);
                while (resultEnum.hasMoreElements()) {
                    list.put(resultEnum.nextElement());
                }
            }

            // Then we add the XSLT elements of the first prefix (normally of the stylesheet node).
            addXslElementsToEnum(list, elements, prefixList.get(0) + ":", prefix); // NOI18N

            // Finally we add xsl namespace elements with other prefixes than the first one
            for (int prefixInd = 1; prefixInd < prefixList.size(); prefixInd++) {
                String curPrefix = (String)prefixList.get(prefixInd) + ":"; // NOI18N
                Node curNode = el;
                String curName = null;
                while(curNode != null && null != (curName = curNode.getNodeName()) && !curName.startsWith(curPrefix)) {
                    curNode = curNode.getParentNode();
                }

                if (curName == null) {
                    // This must be the document node
                    addXslElementsToEnum(list, getElementDecls().keySet(), curPrefix, prefix);
                } else {
                    String parentName = curName.substring(curPrefix.length());
                    elements = (Set) getElementDecls().get(parentName);
                    addXslElementsToEnum(list, elements, curPrefix, prefix);
                }
            }

        } else if (node instanceof Document) {
            //??? it should be probably only root element name
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();
            addXslElementsToEnum(list, getElementDecls().keySet(), prefixList.get(0) + ":", prefix); // NOI18N
        } else {
            return org.openide.util.Enumerations.empty();
        }

        return list;
    }

    public Enumeration queryAttributes(HintContext ctx) {
        Element el = null;
        // Support two versions of GrammarQuery contract
        if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            el = ((Attr)ctx).getOwnerElement();
        } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
            el = (Element) ctx;
        }
        if (el == null) return org.openide.util.Enumerations.empty();

        String elTagName = el.getTagName();
        NamedNodeMap existingAttributes = el.getAttributes();

        updateProperties(el);


        String curXslPrefix = null;
        for (int ind = 0; ind < prefixList.size(); ind++) {
            if (elTagName.startsWith((String)prefixList.get(ind) + ":")){ // NOI18N
                curXslPrefix = (String)prefixList.get(ind) + ":"; // NOI18N
                break;
            }
        }

        Set<String> possibleAttributes;
        if (curXslPrefix != null) {
            // Attributes of XSL element
            possibleAttributes = (Set) getAttrDecls().get(el.getTagName().substring(curXslPrefix.length()));
        } else {
            // XSL Attributes of Result element
            possibleAttributes = new TreeSet<>();
            if (prefixList.size() > 0) {
                Iterator<String> it = getResultElementAttr().iterator();
                while (it.hasNext()) {
                    possibleAttributes.add((String)prefixList.get(0) + ":" + (String) it.next()); // NOI18N
                }
            }
        }
        if (possibleAttributes == null) return org.openide.util.Enumerations.empty();

        String prefix = ctx.getCurrentPrefix();

        QueueEnumeration list = new QueueEnumeration();

        if (resultGrammarQuery != null) {
            Enumeration enum2 = resultGrammarQuery.queryAttributes(ctx);
            while(enum2.hasMoreElements()) {
                GrammarResult resNode = (GrammarResult)enum2.nextElement();
                if (!possibleAttributes.contains(resNode.getNodeName())) {
                    list.put(resNode);
                }
            }
        }

        Iterator<String> it = possibleAttributes.iterator();
        while (it.hasNext()) {
            String next = (String) it.next();
            if (next.startsWith(prefix)) {
                if (existingAttributes.getNamedItem(next) == null) {
                    list.put(new MyAttr(next));
                }
            }
        }

        return list;
    }

    public Enumeration queryValues(HintContext ctx) {
       if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
            updateProperties(((Attr)ctx).getOwnerElement());
            if (prefixList.size() == 0) return org.openide.util.Enumerations.empty();
            String xslNamespacePrefix = prefixList.get(0) + ":"; // NOI18N

            String prefix = ctx.getCurrentPrefix();

            Attr attr = (Attr)ctx;

            boolean isXPath = false;
            String elName = attr.getOwnerElement().getNodeName();
            if (elName.startsWith(xslNamespacePrefix)) {
                String key = elName.substring(xslNamespacePrefix.length());
                String xpathAttrName = (String)getExprAttributes().get(key);
                if (xpathAttrName != null && xpathAttrName.equals(attr.getNodeName())) {
                    // This is an XSLT element which should contain XPathExpression
                    isXPath = true;
                }

                // consult awailable public IDs with users catalog
                if ("output".equals(key)) {                             // NOI18N
                    if ("doctype-public".equals(attr.getName())) {      // NOI18N
                        UserCatalog catalog = UserCatalog.getDefault();
                        if (catalog == null) return org.openide.util.Enumerations.empty();
                        QueueEnumeration en = new QueueEnumeration();
                        Iterator<String> it = catalog.getPublicIDs();
                        while (it.hasNext()) {
                            String next = (String) it.next();
                            if (next != null && next.startsWith(prefix)) {
                                en.put(new  MyText(next));
                            }
                        }
                        return en;
                    }
                }
            }

            String preExpression = ""; // NOI18N

            if (!isXPath) {
                // Check if we are inside { } for attribute value
                String nodeValue = attr.getNodeValue();
                int exprStart = nodeValue.lastIndexOf('{', prefix.length() - 1); // NOI18N
                int exprEnd = nodeValue.indexOf('}', prefix.length()); // NOI18N
                //Util.THIS.debug("exprStart: " + exprStart); // NOI18N
                //Util.THIS.debug("exprEnd: " + exprEnd); // NOI18N
                if (exprStart != -1 && exprEnd != -1) {
                    isXPath = true;
                    preExpression = prefix.substring(0, exprStart + 1);
                    prefix = prefix.substring(exprStart + 1);
                }

            }

            if (isXPath) {
                // This is an XPath expression
                QueueEnumeration list = new QueueEnumeration();

                int curIndex = prefix.length();
                while (curIndex > 0) {
                    curIndex--;
                    char curChar = prefix.charAt(curIndex);
                    if (curChar == '(' || curChar == ',' || curChar == ' ') { // NOI18N
                        curIndex++;
                        break;
                    }
                }

                preExpression += prefix.substring(0, curIndex);
                String subExpression = prefix.substring(curIndex);

                int lastDiv = subExpression.lastIndexOf('/'); // NOI18N
                String subPre = ""; // NOI18N
                String subRest = ""; // NOI18N
                if (lastDiv != -1) {
                    subPre = subExpression.substring(0, lastDiv + 1);
                    subRest = subExpression.substring(lastDiv + 1);
                } else {
                    subRest = subExpression;
                }

                // At this point we need to consult transformed document or
                // its grammar.
// [93792] +
//              Object selScenarioObj = scenarioCookie.getModel().getSelectedItem();
// [93792] -
                /*
                if (selScenarioObj instanceof XSLScenario) {
                    XSLScenario scenario = (XSLScenario)selScenarioObj;
                    Document doc = null;
                    try {
                        doc = scenario.getSourceDocument(dataObject, false);
                    } catch(Exception e) {
                        // We don't care, ignore
                    }

                    if (doc != null) {
                        Element docElement = doc.getDocumentElement();

                        Set childNodeNames = new TreeSet();

                        String combinedXPath;
                        if (subPre.startsWith("/")) { // NOI18N
                            // This is an absolute XPath
                            combinedXPath = subPre;
                        } else {
                            // This is a relative XPath

                            // Traverse up the documents tree looking for xsl:for-each
                            String xslForEachName = xslNamespacePrefix + "for-each"; // NOI18N
                            List selectAttrs = new LinkedList();
                            Node curNode = attr.getOwnerElement();
                            if (curNode != null) {
                                // We don't want to add select of our selfs
                                curNode = curNode.getParentNode();
                            }

                            while (curNode != null && !(curNode instanceof Document)) {
                                if (curNode.getNodeName().equals(xslForEachName)) {
                                    selectAttrs.add(0, ((Element)curNode).getAttribute("select")); // NOI18N
                                }

                                curNode = curNode.getParentNode();
                            }

                            combinedXPath = ""; // NOI18N
                            for (int ind = 0; ind < selectAttrs.size(); ind++) {
                                combinedXPath += selectAttrs.get(ind) + "/"; // NOI18N
                            }
                            combinedXPath += subPre;
                        }

                        try {
                            NodeList nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "child::*"); // NOI18N
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add(curResNode.getNodeName());
                            }

                            nodeList = XPathAPI.selectNodeList(doc, combinedXPath + "@*"); // NOI18N
                            for (int ind = 0; ind < nodeList.getLength(); ind++) {
                                Node curResNode = nodeList.item(ind);
                                childNodeNames.add("@" + curResNode.getNodeName()); // NOI18N
                            }
                        } catch (Exception e) {
                            Util.THIS.debug("Ignored during XPathAPI operations", e); // NOI18N
                            // We don't care, ignore
                        }

                        addItemsToEnum(list, childNodeNames, subRest, preExpression + subPre);
                    }
                }*/

                addItemsToEnum(list, getXPathAxes(), subRest, preExpression + subPre);
                addItemsToEnum(list, getXslFunctions(), subExpression, preExpression);

                return list;
            }
        }

        return org.openide.util.Enumerations.empty();
    }

    public GrammarResult queryDefault(HintContext ctx) {
        //??? XSLT defaults are missing
        if (resultGrammarQuery == null) return null;
        return resultGrammarQuery.queryDefault(ctx);
    }

    public boolean isAllowed(Enumeration en) {
        return true; //!!! not implemented
    }

    public Enumeration queryEntities(String prefix) {
        QueueEnumeration list = new QueueEnumeration();

        // add well-know build-in entity names

        if ("lt".startsWith(prefix)) list.put(new MyEntityReference("lt"));     // NOI18N
        if ("gt".startsWith(prefix)) list.put(new MyEntityReference("gt"));     // NOI18N
        if ("apos".startsWith(prefix)) list.put(new MyEntityReference("apos")); // NOI18N
        if ("quot".startsWith(prefix)) list.put(new MyEntityReference("quot")); // NOI18N
        if ("amp".startsWith(prefix)) list.put(new MyEntityReference("amp"));   // NOI18N

        return list;
    }

    public Enumeration queryNotations(String prefix) {
        return org.openide.util.Enumerations.empty();
    }

    public java.awt.Component getCustomizer(HintContext ctx) {
        if (customizer == null) {
            customizer = lookupCustomizerInstance();
            if (customizer == null) {
                return null;
            }
        }

        return customizer.getCustomizer(ctx, dataObject);
    }

    public boolean hasCustomizer(HintContext ctx) {
        if (customizer == null) {
            customizer = lookupCustomizerInstance();
            if (customizer == null) {
                return false;
            }
        }

        return customizer.hasCustomizer(ctx);
    }

    public org.openide.nodes.Node.Property[] getProperties(final HintContext ctx) {

        if (ctx.getNodeType() != Node.ATTRIBUTE_NODE || ctx.getNodeValue() == null) {
            return null;
        }

        PropertySupport attrNameProp = new PropertySupport("Attribute name", String.class,  // NOI18N
        bundle.getString("BK0001"), bundle.getString("BK0002"), true, false) {
            public void setValue(Object value) {
                // Dummy
            }
            public Object getValue() {
                return ctx.getNodeName();
            }

        };

        PropertySupport attrValueProp = new PropertySupport("Attribute value", String.class, // NOI18N
        bundle.getString("BK0003"), bundle.getString("BK0004"), true, true) {
            public void setValue(Object value) {
                ctx.setNodeValue((String)value);
            }
            public Object getValue() {
                return ctx.getNodeValue();
            }

        };

        return new org.openide.nodes.Node.Property[]{attrNameProp, attrValueProp};
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Private helper methods

    /**
     * Looks up registered XSLCustomizer objects which will be used by this object
     */
    private static XSLCustomizer lookupCustomizerInstance() {
        Lookup.Template lookupTemplate =
            new Lookup.Template(XSLCustomizer.class);

        Lookup.Item lookupItem = Lookups.forPath(CUSTOMIZER_FOLDER).lookupItem(lookupTemplate);
        if (lookupItem == null) {
            return null;
        }

        return (XSLCustomizer)lookupItem.getInstance();
    }

    /**
     * @param enumX the Enumeration which the element should be added to
     * @param elements a set containing strings which should be added (with prefix) to the enum or <code>null</null>
     * @param namespacePrefix a prefix at the form "xsl:" which should be added in front
     *          of the names in the elements.
     * @param startWith Elements should only be added to enum if they start with this string
     */
    private static void addXslElementsToEnum(QueueEnumeration enumX, Set elements, String namespacePrefix, String startWith) {
        if (elements == null) return;
        if (startWith.startsWith(namespacePrefix) || namespacePrefix.startsWith(startWith)) {
            Iterator<String> it = elements.iterator();
            while ( it.hasNext()) {
                String next = it.next();
                if (next != resultElements) {
                    String nextText = namespacePrefix + (String)next;
                    if (nextText.startsWith(startWith)) {
                        // TODO pass true for empty elements
                        enumX.put(new MyElement(nextText, false));
                    }
                }
            }
        }
    }

    private static void addItemsToEnum(QueueEnumeration enumX, Set set, String startWith, String prefix) {
        Iterator<String> it = set.iterator();
        while ( it.hasNext()) {
            String nextText = (String)it.next();
            if (nextText.startsWith(startWith)) {
                enumX.put(new MyText(prefix + nextText));
            }
        }
    }

    /**
     * This method traverses up the document tree, investigates it and updates
     * prefixList, resultGrammarQuery, lastDoctypeSystem or lastDoctypePublic
     * members if necessery.
     * @param curNode the node which from wich the traversing should start.
     */
    private void updateProperties(Node curNode) {
        prefixList.clear();

        // Traverse up the documents tree
        Node rootNode = curNode;
        while (curNode != null && !(curNode instanceof Document)) {

            // Update the xsl namespace prefix list
            NamedNodeMap attributes = curNode.getAttributes();
            for (int ind = 0; ind < attributes.getLength(); ind++) {
                Attr attr = (Attr)attributes.item(ind);
                String attrName = attr.getName();
                if (attrName != null && attrName.startsWith("xmlns:")) {  // NOI18N
                    if (attr.getValue().equals(XSLT_NAMESPACE_URI)) {
                        prefixList.add(0, attrName.substring(6));
                    }
                }
            }


            rootNode = curNode;
            curNode = rootNode.getParentNode();
        }

        boolean outputFound = false;
        if (prefixList.size() > 0) {
            String outputElName = (String)prefixList.get(0) + ":output"; // NOI18N
            Node childOfRoot = rootNode.getFirstChild();
            while (childOfRoot != null) {
                String childNodeName = childOfRoot.getNodeName();
                if (childNodeName != null && childNodeName.equals(outputElName)) {
                    Element outputEl = (Element)childOfRoot;
                    String outputMethod = outputEl.getAttribute("method"); // NOI18N

                    String curDoctypePublic = outputEl.getAttribute("doctype-public"); // NOI18N
                    String curDoctypeSystem = outputEl.getAttribute("doctype-system"); // NOI18N

                    if ("html".equals(outputMethod)  // NOI18N
                        && (curDoctypePublic == null || curDoctypePublic.length() == 0)
                        && (curDoctypeSystem == null || curDoctypeSystem.length() == 0)) {                          // NOI18N
                        // html is special case that can be emulated using XHTML
                        curDoctypePublic = XHTML_PUBLIC_ID;
                        curDoctypeSystem = XHTML_SYSTEM_ID;
                    } else if ("text".equals(outputMethod)) {                   // NOI18N
                        // user error, ignore
                        break;
                    }

                    if (curDoctypePublic != null && !curDoctypePublic.equals(lastDoctypePublic) ||
                    curDoctypePublic == null && lastDoctypePublic != null ||
                    curDoctypeSystem != null && !curDoctypeSystem.equals(lastDoctypeSystem) ||
                    curDoctypeSystem == null && lastDoctypeSystem != null) {
                        setOutputDoctype(curDoctypePublic, curDoctypeSystem);
                    }

                    outputFound = true;
                    break;
                }
                childOfRoot = childOfRoot.getNextSibling();
            }
        }

        if (!outputFound) {
            setOutputDoctype(null, null);
        }
    }

    /**
     * Updates resultGrammarQuery by parsing the DTD specified by publicId and
     * systemId. lastDoctypeSystem and lastDoctypePublic are assigned to the new values.
     * @param publicId the public identifier of the DTD
     * @param publicId the system identifier of the DTD
     */
    private void setOutputDoctype(String publicId, String systemId) {
        lastDoctypePublic = publicId;
        lastDoctypeSystem = systemId;

        if (publicId == null && systemId == null) {
            resultGrammarQuery = null;
            return;
        }

        InputSource inputSource = null;
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            EntityResolver resolver = catalog.getEntityResolver();
            if (resolver != null) {
                try {
                    inputSource = resolver.resolveEntity(publicId, systemId);
                } catch(SAXException e) {
                } catch(IOException e) {
                } // Will be handled below
            }
        }

        if (inputSource == null) {
            try {
                java.net.URL url = new java.net.URL(systemId);
                inputSource = new InputSource(url.openStream());
                inputSource.setPublicId(publicId);
                inputSource.setSystemId(systemId);
            } catch(IOException e) {
                resultGrammarQuery = null;
                return;
            }
        }

        resultGrammarQuery = DTDUtil.parseDTD(true, inputSource);

    }

    ////////////////////////////////////////////////////////////////////////////////
    // Private helper classes

    private class ResultHintContext extends ResultNode implements HintContext {
        private String currentPrefix;

        public ResultHintContext(HintContext peer, String ignorePrefix, String onlyUsePrefix) {
            super(peer, ignorePrefix, onlyUsePrefix);
            currentPrefix = peer.getCurrentPrefix();
        }

        public String getCurrentPrefix() {
            return currentPrefix;
        }
    }

    // Result classes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


    private abstract static class AbstractResultNode extends AbstractNode implements GrammarResult {

        public Icon getIcon(int kind) {
            return null;
        }

        /**
         * @return provide additional information simplifiing decision
         */
        public String getDescription() {
            return NbBundle.getMessage(XSLGrammarQuery.class, "BK0005");
        }

        /**
         * @return text representing name of suitable entity
         * //??? is it really needed
         */
        public String getText() {
            return getNodeName();
        }

        /**
         * @return name that is presented to user
         */
        public String getDisplayName() {
            return null;
        }

        public boolean isEmptyElement() {
            return false;
        }

    }

    private static class MyEntityReference extends AbstractResultNode implements EntityReference {
        private String name;

        MyEntityReference(String name) {
            this.name = name;
        }

        public short getNodeType() {
            return Node.ENTITY_REFERENCE_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }
    }

    private static class MyElement extends AbstractResultNode implements Element {
        private String name;
        private boolean empty;

        MyElement(String name, boolean empty) {
            this.name = name;
            this.empty = empty;
        }

        public short getNodeType() {
            return Node.ELEMENT_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public String getTagName() {
            return name;
        }

        @Override
        public boolean isEmptyElement() {
            return empty;
        }
    }

    private static class MyAttr extends AbstractResultNode implements Attr {
        private String name;

        MyAttr(String name) {
            this.name = name;
        }

        public short getNodeType() {
            return Node.ATTRIBUTE_NODE;
        }

        @Override
        public String getNodeName() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return null;  //??? what spec says
        }
    }

    private static class MyText extends AbstractResultNode implements Text {
        private String data;

        MyText(String data) {
            this.data = data;
        }

        public short getNodeType() {
            return Node.TEXT_NODE;
        }

        @Override
        public String getNodeValue() {
            return getData();
        }

        @Override
        public String getData() throws DOMException {
            return data;
        }

        @Override
        public String getDisplayName() {
            return getData();
        }
        
        @Override
        public int getLength() {
            return data == null ? -1 : data.length();
        }
    }

    private static class QueueEnumeration implements Enumeration {
        private java.util.LinkedList list = new LinkedList ();
        
        public boolean hasMoreElements () {
            return !list.isEmpty ();
        }        
        
        public Object nextElement () {
            return list.removeFirst ();
        }        

        public void put (Object[] arr) {
            list.addAll (Arrays.asList (arr));
        }
        public void put (Object o) {
            list.add (o);
        }
        
    } // end of QueueEnumeration
}
