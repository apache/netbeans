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
package org.netbeans.modules.xml.schema.completion.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.Element;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext.CompletionType;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRoot;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRootAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helps in populating the completion list.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionContextImpl extends CompletionContext {
    public static final String PREFIX                   = "ns"; //NOI18N
    public static final String XSI_SCHEMALOCATION       = "schemaLocation"; //NOI18N
    public static final String XSI_NONS_SCHEMALOCATION  = "noNamespaceSchemaLocation"; //NOI18N
    private static final String XSD_TARGET_NAMESPACE = "targetNamespace"; // NOI18N

    private static final Logger _logger = Logger.getLogger(CompletionContextImpl.class.getName());

    private XMLSyntaxSupport support;
    private int completionAtOffset = -1;
    private FileObject primaryFile;
    private String typedChars;
    private Token<XMLTokenId> token;
    private int tokenOffset;
    private SyntaxElement element;
    private String attribute;
    private DocRoot docRoot;
    private char lastTypedChar;
    private CompletionType completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
    private List<QName> pathFromRoot;
    /**
     * Tags on the path from root to the context element (the one the CC tries to fill)
     */
    private List<SyntaxElement> elementsFromRoot;
    private Map<String, String>  schemaLocationMap = new HashMap<>();
    private String schemaLocation;
    private String noNamespaceSchemaLocation;
    private String defaultNamespace;
    private Document document;
    private HashMap<String, CompletionModel> nsModelMap = new HashMap<>();
    private List<CompletionModel> noNSModels = new ArrayList<>();
    private HashMap<String, String> declaredNamespaces = new HashMap<>();
    private HashMap<String, String> suggestedNamespaces = new HashMap<>();
    private HashMap<String, String> specialNamespaceMap = new HashMap<>();
    private CompletionModel noNamespaceModel;
    private transient List<String> existingAttributes;
    private boolean specialCompletion;
    /**
     * HACK: target namespace for XML schemas. See defect #212972
     */
    private String targetNamespace;

    /**
     * Creates a new instance of CompletionQueryHelper
     */
    public CompletionContextImpl(FileObject primaryFile, XMLSyntaxSupport support,
        int offset) {
        try {
            this.support = support;
            this.completionAtOffset = offset;
            this.primaryFile = primaryFile;
            this.document = support.getDocument();
            this.element = support.getElementChain(offset);
            int[] off = new int[2];
            this.token = support.getPreviousToken(offset, off);
            this.tokenOffset = off[0];
            this.docRoot = CompletionUtil.getDocRoot(document);
            this.lastTypedChar = support.lastTypedChar();
            populateNamespaces();
        } catch(BadLocationException | RuntimeException ex) {
            //in the worst case, there won't be any code completion help
            _logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    ////////////////START CompletionContext Implementations////////////////
    @Override
    public CompletionType getCompletionType() {
        return completionType;
    }

    @Override
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    @Override
    public List<QName> getPathFromRoot() {
        return pathFromRoot;
    }

    @Override
    public FileObject getPrimaryFile() {
        return primaryFile;
    }

    @Override
    public BaseDocument getBaseDocument() {
        return (BaseDocument)document;
    }

    @Override
    public HashMap<String, String> getDeclaredNamespaces() {
        return declaredNamespaces;
    }

    @Override
    public String getTypedChars() {
        return typedChars;
    }
    ////////////////END CompletionContext Implementations////////////////

    public boolean isSchemaAwareCompletion() {
        return (schemaLocation != null) || (noNamespaceSchemaLocation != null);
    }

    /**
     * Namespaces suggested by the context, but NOT DECLARED yet in the document
     *
     * @return suggested namespaces
     */
    public Map<String, String> getSuggestedNamespace() {
        return Collections.unmodifiableMap(suggestedNamespaces);
    }

    public List<URI> getSchemas() {
        List<URI> uris = new ArrayList<>();

        if(schemaLocation != null) {
            CompletionUtil.loadSchemaURIs(schemaLocation, uris, schemaLocationMap);
        }

        if(noNamespaceSchemaLocation != null) {
            CompletionUtil.loadSchemaURIs(noNamespaceSchemaLocation, uris, null);
        }

        // If there are schemas, that have no mapping, try to lookup the
        // systemId using the namespace as systemId.
        for(String ns: getDeclaredNamespaces().values()) {
            if(! schemaLocationMap.containsKey(ns)) {
                try {
                    InputSource is = UserCatalog.getDefault().getEntityResolver().resolveEntity(ns, null);
                    if(is != null && is.getSystemId() != null) {
                        schemaLocationMap.put(ns, is.getSystemId());
                        uris.add(URI.create(is.getSystemId()));
                    }
                } catch (SAXException | IOException | IllegalArgumentException ex) {
                    _logger.log(Level.FINE, "Failed to resolve namespace: " + ns, ex);
                }
            }
        }
        return uris;
    }

    /**
     * Extracts prefix from the tagname, returns "" or {@code null} if there's no
     * prefix
     *
     * @param tagName tag name string, incl. possible prefix (DOM Level 1)
     * @param empty if true, "" is returned for no prefix.
     * @return namespace prefix, "" or {@code null) for no prefix
     */
    private String getPrefix(String tagName, boolean empty) {
        int index = tagName.indexOf(':');
        if (index == -1) {
            return empty ? "" : null;
        }
        return tagName.substring(index + 1);
    }

    /**
     * Adds namespaces from the tag to this context, possibly overriding namespaces
     * from previously added tags. Tags should be added starting from the root down
     * to the context position.
     */
    private void addNamespacesFrom(SyntaxElement s) {
        Node e = s.getNode();
        NamedNodeMap attrs = e.getAttributes();
        String nodePrefix = getPrefix(e.getNodeName(), false);
        String version = null;
        String xsltAttrName = null;

        for (int i = attrs.getLength() - 1; i >= 0; i--) {
            Node n = attrs.item(i);
            if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                Attr a = (Attr)n;
                String attrName = a.getName();
                String value = a.getValue();
                addNamespace(attrName, value, nodePrefix);


                if(value.trim().equals("http://www.w3.org/1999/XSL/Transform")) { //NOI18N
                    xsltAttrName = attrName;
                }
                if(CompletionUtil.getLocalNameFromTag(attrName).
                        equals("version")) { //NOI18N
                    version = value.trim();
                }
            }
        }

        if (xsltAttrName != null && "2.0".equals(version)) {
            String prefix = getPrefix(xsltAttrName, false);
            if (prefix == null) {
                // override nonNS location because nonNS schema is XSLT 2.0
                noNamespaceSchemaLocation = "http://www.w3.org/2007/schema-for-xslt20.xsd";
            } else {
                addSchemaLocation(prefix + " http://www.w3.org/2007/schema-for-xslt20.xsd"); //NOI18N
            }
        }
    }

    /**
     * Initializes the declared namespaces from the context path.
     * Adds all declared namespaces from context element and its parents - these are
     * in scope and will form {@link #declaredNamespaces} map.
     *
     * @param stack path from the context element (index 0) to the root (index N-1).
     */
    private void addContextNamespaces(List<SyntaxElement> stack) {
        // must iterate from root down to the context element, to properly override
        // namespaces and replace default/noNamespace information.
        for (int i = stack.size() - 1; i >= 0; i--) {
            SyntaxElement t = stack.get(i);
            addNamespacesFrom(t);
        }
    }

    /**
     * Processes an attribute for namespace-related stuff. Detects and sets
     * {@link #schemaLocation}, {@link #noNamespaceSchemaLocation}, {@link #defaultNamespace} URIs.
     * Should be called for elements starting from root down to the context element for
     * proper overriding
     */
    private void addNamespace(String attrName, String value, String nodePrefix) {
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;

        if(CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSI_SCHEMALOCATION)) {
            schemaLocation = value.trim();
            return;
        }
        if(CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSI_NONS_SCHEMALOCATION)) {
            noNamespaceSchemaLocation = value.trim();
            return;
        }
        if (CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSD_TARGET_NAMESPACE)) {
            targetNamespace = value.trim();
            return;
        }

        if(! attrName.startsWith(XMLConstants.XMLNS_ATTRIBUTE))
            return;

        if(attrName.equals(defNS)) {
            this.defaultNamespace = value;
        }
        declaredNamespaces.put(attrName, value);
    }

    private void addSchemaLocation(String s) {
        if (schemaLocation == null) {
            schemaLocation = s;
        } else {
            schemaLocation = schemaLocation + " " + s; // NO18N
        }
    }

    /**
     * Keeps all namespaces along with their prefixes in a HashMap.
     * This is obtained from the root element's attributes, with
     * the attribute value(namespace) as the key and name with prefix
     * as the value.
     * For example the hashmap may look like this
     *  KEY                                    VALUE
     *  http://www.camera.com                  xmlns:c
     *  http://www.nikon.com                   xmlns:n
     */
    private void populateNamespaces() {
        if(docRoot == null)
            return;
        //Check if the tag has any prefix. If yes, the defaultNamespace
        //is the one with this prefix.
        String tagName = docRoot.getName();
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;
        String temp = CompletionUtil.getPrefixFromTag(tagName);
        if(temp != null) defNS = defNS+":"+temp; //NOI18N
        List<DocRootAttribute> attributes = docRoot.getAttributes();

        String version = null;
        boolean xsltDeclared = false;

        for(int index=0; index<attributes.size(); index++) {
            DocRootAttribute attr = attributes.get(index);
            String attrName = attr.getName();
            String value = attr.getValue();
            addNamespace(attrName, value, temp);

            //resolve xsl stylesheets w/o the schema location specification.
            //In such case the root element only contains the xmlns:xsl=""http://www.w3.org/1999/XSL/Transform"
            //along with the version attribute.
            //If such ns is declared and the version is 2.0 then use the
            //http://www.w3.org/2007/schema-for-xslt20.xsd schema for the completion model

            if(attr.getValue().trim().equals("http://www.w3.org/1999/XSL/Transform")) { //NOI18N
                xsltDeclared = true;
            }
            if(CompletionUtil.getLocalNameFromTag(attrName).
                    equals("version")) { //NOI18N
                version = attr.getValue().trim();
            }
        }

        if(schemaLocation == null && xsltDeclared && "2.0".equals(version)) {
            //only the second "token" from the schemaLocation is considered as the schema
            schemaLocation = "schema http://www.w3.org/2007/schema-for-xslt20.xsd"; //NOI18N
        }

    }

    private TokenSequence getTokenSequence() {
        TokenSequence tokenSequence = null;
        try {
            ((AbstractDocument) document).readLock();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
            tokenSequence = tokenHierarchy.tokenSequence();
        } catch(Exception e) {
            _logger.log(Level.WARNING,
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
        return tokenSequence;
    }

    private boolean isTagAttributeRequired(TokenSequence tokenSequence) {
        int caretPos = completionAtOffset;

        int diff = tokenSequence.move(caretPos);
        tokenSequence.moveNext();

        Token tok = tokenSequence.token();
        if (tok == null) return false;

        TokenId tokID = tok.id();
        if (tokID.equals(XMLTokenId.TAG) && CompletionUtil.isEndTagSuffix(tok) &&
           (tokenSequence.offset() + 1 == caretPos)) { // <... /|>, | - a caret position
            return false;
        }
        boolean
            isAttributeOrSpace = tokID.equals(XMLTokenId.ARGUMENT) ||
                                 tokID.equals(XMLTokenId.WS),
            isTagLastCharFound = tokID.equals(XMLTokenId.TAG) &&
                                 (CompletionUtil.isTagLastChar(tok) ||
                                  CompletionUtil.isEndTagSuffix(tok)),
            //this may happen when there's a lexical error inside the tag itself,
            //for example in this valid case: <tag att|> or in something errorneous
            //like: <tag att|#$#$#>
            isJustBeforeTagErrorToken = false;
        if (tokID.equals(XMLTokenId.ERROR)) {
            String tokenText = tok.text().toString();
            isJustBeforeTagErrorToken = tokenText.subSequence(0, diff).toString().trim().isEmpty() ||
                    diff == 0;
        }
        while (true) {
            if (tokID.equals(XMLTokenId.TAG) || tokID.equals(XMLTokenId.TEXT)) {
                if (CompletionUtil.isEndTagPrefix(tok)) break;
                else if (tokID.equals(XMLTokenId.TAG)) {
                    String tagName = CompletionUtil.getTokenTagName(tok);
                    if (tagName != null) {
                        int tokOffset = tokenSequence.offset(),
                            tagNameEndPos = tokOffset + CompletionUtil.TAG_FIRST_CHAR.length() +
                                            tagName.length();
                        if ((tagNameEndPos < caretPos) &&
                            (isAttributeOrSpace || isTagLastCharFound || isJustBeforeTagErrorToken)) {
                            return true;
                        }
                    }
                }
            }
            if (! tokenSequence.movePrevious()) break;

            tok = tokenSequence.token();
            tokID = tok.id();
            if (CompletionUtil.isEndTagSuffix(tok) || CompletionUtil.isTagLastChar(tok)) break;
        }
        return false;
    }

    /**
     * At a given context, that is, at the current cursor location
     * in the document, finds the type of query that needs to be
     * carried out and finds the path from root.
     */
    public boolean initContext() {
        boolean res = doInitContext();
        if (pathFromRoot != null) {
            addContextNamespaces(elementsFromRoot);
        } else {
            populateNamespaces();
        }
        return res;
    }

    private boolean doInitContext() {
        TokenSequence tokenSequence = getTokenSequence();
        try {
            if (isTagAttributeRequired(tokenSequence)) {
                completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                if (token.id() == XMLTokenId.WS) {
                    typedChars = null;
                } else {
                    String str = token.text().toString();
                    int e = str.length();
                    int l = Math.min(completionAtOffset - tokenOffset /* initial quote */, e);
                    typedChars = str.substring(0, l).trim();
                    if (typedChars.isEmpty()) {
                        typedChars = null;
                    }
                }
                createPathFromRoot(element);
                return true;
            }

            XMLTokenId id = token.id();
            switch (id) {
                //user enters < character
                case TEXT:
                    String chars = token.text().toString().trim();
                    Token previous = support.getPreviousToken(tokenOffset);
                    String previousTokenText = previous == null ?
                            "" : previous.text().toString().trim();
                    if(chars != null && chars.startsWith("&")) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }
                    if (chars != null && chars.equals("") && //previousTokenText.equals("/>")) {
                        previousTokenText.endsWith(">")) {
                        //completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        createPathFromRoot(element);
                        break;
                    }
                    if(chars != null && chars.startsWith("<")) {
                        typedChars = chars.substring(1);
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        createPathFromRoot(element);
                        break;
                    }
                    if (chars != null && previousTokenText.equals(">")) {
                        if(!chars.equals("") && !chars.equals(">"))
                            typedChars = chars;
                        createPathFromRoot(element);
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT_VALUE;
                        break;
                    }
                    if (chars != null && !chars.equals("<") && previousTokenText.equals(">")) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }
                    break;

                case BLOCK_COMMENT:
                    completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                    createPathFromRoot(element);
                    break;

                //start tag of an element
                case TAG:
                    if(support.isEndTag(element)) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }
                    if (support.isEmptyTag(element)) {
                        /*
                        if (token != null &&
                            token.getImage().trim().equals("/>")) {
                            completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                            break;
                        }
                        */
                        String tagName = element.getNode().getNodeName();
                        if ((element.getElementOffset() + 1 == completionAtOffset) ||
                            (tokenOffset + token.length()== completionAtOffset)) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            createPathFromRoot(element.getPrevious());
                            break;
                        }
                        if (completionAtOffset > element.getElementOffset() + 1 &&
                            completionAtOffset <= (element.getElementOffset() + 1 +
                                                  tagName.length())) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            int index = completionAtOffset - element.getElementOffset() - 1;
                            typedChars = index < 0 ? tagName :
                                tagName.substring(0, index);
                            createPathFromRoot(element.getPrevious());
                            break;
                        }
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???pathFromRoot = getPathFromRoot(element);
                        break;
                    }
                    if (element.getType() == Node.ELEMENT_NODE &&
                        support.isStartTag(element)) {
                        if(token != null &&
                           token.text().toString().trim().equals(">")) {
                            createPathFromRoot(element);
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT_VALUE;
                            break;
                        }
                        if(element.getElementOffset() + 1 == this.completionAtOffset) {
                            typedChars = null;
                        } else {
                            String tagName = element.getNode().getNodeName();
                            int index = completionAtOffset-element.getElementOffset()-1;
                            typedChars = index < 0 ? tagName :
                                tagName.substring(0, index);
                        }
                    }
                    completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                    createPathFromRoot(element.getPrevious());
                    break;

                //user enters an attribute name
                case ARGUMENT:
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???typedChars = token.getImage();
//***???pathFromRoot = getPathFromRoot(element);
                    break;

                //some random character
                case CHARACTER:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;

                //user enters = character, we should ignore all other operators
                case OPERATOR:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;

                //user enters either ' or "
                case VALUE: {
                    //user enters start quote and no end quote exists
                    Token<XMLTokenId> next = support.getNextToken(tokenOffset + token.length());
                    if(next == null) {
                        if(lastTypedChar == '\'' || lastTypedChar == '\"') {
                            typedChars = null;
                        }   else  {
                            String tt = token.text().toString();
                            typedChars = tt.substring(1, tt.indexOf(">"));
                        }
                    }

                    //user is inside start/end quotes
                    if(lastTypedChar != '\'' && lastTypedChar != '\"') {
                        String str = token.text().toString();
                        if( str != null && !str.equals("\"\"") && !str.equals("\'\'") &&
                            (str.startsWith("\"") || str.startsWith("\'")) &&
                            (str.endsWith("\"") || str.endsWith("\'")) ) {
                            int e = str.length() - 1;
                            int l = Math.min(completionAtOffset - tokenOffset /* initial quote */, e);
                            typedChars = str.substring(1, l);
                            if(completionAtOffset == tokenOffset + 1)
                                typedChars = "";
                        }
                    }
                    attribute = findAttributeName();
                    completionType = attribute == null ?
                            CompletionType.COMPLETION_TYPE_UNKNOWN :
                            CompletionType.COMPLETION_TYPE_ATTRIBUTE_VALUE;
                    createPathFromRoot(element);
                    break;
                }

                //user enters white-space character
                case WS: {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        boolean cont = support.runWithSequence(tokenOffset, (TokenSequence ts) -> {
                            if (!ts.movePrevious()) {
                                return false;
                            }
                            Token<XMLTokenId> prev = ts.token();
                            while(ts.movePrevious()) {
                                prev = ts.token();
                                if (prev.id() != XMLTokenId.WS) {
                                    break;
                                }
                            }
                            if( (prev.id() == XMLTokenId.VALUE) ||
                                    (prev.id() == XMLTokenId.TAG) ) {
                                //no attr completion for end tags
                                if (prev.text().toString().startsWith("</")) {
                                    return true;
                                } else {
                                    completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                                    createPathFromRoot(element);
                                }

                                //***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                                //***???pathFromRoot = getPathFromRoot(element);
                            }
                            return false;
                        });
                    break;
                }

                default:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
            }
        } catch (BadLocationException | RuntimeException e) {
            _logger.log(Level.INFO,
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
            return false;
        }
        // TODO - complete namespaces based on 'pathToRoot'

        return true;
    }

    /**
     * Assumes that the current token is at the attribute name, operator or
     * somewhere in the attribute value. In all cases, attempt to return the
     * context attribute name.
     *
     * @return attribute name, or <code>null</code> in unexpected situations
     */
    private String findAttributeName() {
        try {
            return support.runWithSequence(tokenOffset, (TokenSequence ts) -> {
                Token<XMLTokenId> item = ts.token();
                while (item != null) {
                    switch (item.id()) {
                        case VALUE:
                        case OPERATOR:
                        case WS:
                        case TEXT:
                            if (!ts.movePrevious()) {
                                return null;
                            }
                            item = ts.token();
                            break;
                        case ARGUMENT:
                            return item.text().toString();
                        default:
                            return null;
                    }
                }
                return null;
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public List<DocRootAttribute> getDocRootAttributes() {
        return docRoot.getAttributes();
    }

    public DocRoot getDocRoot() {
        return docRoot;
    }

    public String getAttribute() {
        return attribute;
    }

    /**
     * This is a 2-pass algorithm. In the 1st pass, it pushes all the relevant
     * start tags all the way to the root into the stack, root being at the top.
     * In the 2nd pass, it gets items from the stack in reverse order and checks
     * if any intermediate item can be treated as root. This is possible when dealing
     * with items from multiple namespaces.
     */
    private void createPathFromRoot(SyntaxElement se) {
        //1st pass
        if(se == null)
            return;
        Stack<SyntaxElement> stack = new Stack<>();
        if(support.isEmptyTag(se)) {
            stack.push(se);
        }

        while( se != null) {
            if (
                (stack.isEmpty() && support.isStartTag(se)) ||
                    support.isEndTag(se)) {
                stack.push(se);
                se = se.getPrevious();
                continue;
            }
            if (support.isStartTag(se)) {
                SyntaxElement end = stack.isEmpty() ? null : stack.peek();
                if (support.isEndTag(end)) {
                    if(end.getNode().getNodeName().equals(se.getNode().getNodeName())) {
                        stack.pop();
                    }
                } else {
                    stack.push(se);
                }
            }
            se = se.getPrevious();
        }

        this.elementsFromRoot = stack;
        //2nd pass
        this.pathFromRoot = createPath(stack);
    }

    /**
     * Peeks items from the stack in reverse order and checks if that tag is a
     * root. If yes, it returns the path. If no, continues to the next tag.
     * While creating the path it always adds items to the start of the list so
     * that the returned path starts from root, all the way to the current tag.
     */
    private ArrayList<QName> createPath(Stack<SyntaxElement> stack) {
        ArrayList<QName> path = new ArrayList<>();
        ListIterator<SyntaxElement> tags = stack.listIterator();
        while(tags.hasNext()) {
            SyntaxElement tag = tags.next();
            //add to the start of the list
            path.add(0, createQName(tag));
            if(isRoot(tag, tags.hasNext() ? tags.next() : null)) {
                return path;
            }
            tags.previous();//since we moved twice.
        }
        //CompletionUtil.printPath(path);
        return path;
    }

    /**
     * If namespace differs from previous, then this one is considered a root.
     * However, there are exceptions to this and may not work well for cases when
     * you combine itmes from schemas with/without namespace.
     */
    private boolean isRoot(SyntaxElement elem, SyntaxElement previousTag) {
        //no previous => this has to be the root
        Node thisTag = elem.getNode();
        if(previousTag == null)
            return true;

        //if the tag declares a namespace and is diff from default, then it is root
        String prefix = CompletionUtil.getPrefixFromTag(thisTag.getNodeName());
        Attr namespaceAttr;
        NamedNodeMap attrs = thisTag.getAttributes();
        if(prefix==null) {
            namespaceAttr = (Attr)attrs.getNamedItem(XMLConstants.XMLNS_ATTRIBUTE);
        } else {
            namespaceAttr = (Attr)attrs.getNamedItem(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
        }
        if(namespaceAttr != null) {
            String namespace = namespaceAttr.getValue();
            if(!namespace.equals(defaultNamespace)) {
                //see if it declares a schemaLocation or noNamespaceSchemaLocation
                String sl = getAttributeValue(thisTag, XSI_SCHEMALOCATION);
                if(sl != null)
                    this.schemaLocation = sl;
                String nnsl = getAttributeValue(thisTag, XSI_NONS_SCHEMALOCATION);
                if(nnsl != null)
                    this.noNamespaceSchemaLocation = nnsl;
                if(prefix != null)
                    declaredNamespaces.put(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix, namespace);
                return true;
            }
        }

        return !fromSameNamespace(thisTag, previousTag.getNode());
    }

    private String getAttributeValue(Node tag, String attrName) {
        NamedNodeMap attrs = tag.getAttributes();
        for(int i=0; i<attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if(name!= null && name.contains(attrName)) {
                return attr.getNodeValue();
            }
        }
        return null;
    }

    /**
     * This is onlyused from createPath() for the path from the current element to the root.
     * It's possible to use elementsFromRoot so that content need not to be lexed (so much)
     * as with navigation using tag.getParent().
     * @param tag
     * @return
     */
    private QName createQName(SyntaxElement tag) {
        String tagName = tag.getNode().getNodeName();
        String prefix = CompletionUtil.getPrefixFromTag(tagName);
        String lName = CompletionUtil.getLocalNameFromTag(tagName);

        int index = elementsFromRoot.indexOf(tag);
        if (index == -1) {
            throw new IllegalStateException();
        }
        for (int i = index; i < elementsFromRoot.size(); i++) {
            SyntaxElement s = elementsFromRoot.get(i);
            NamedNodeMap atts = s.getNode().getAttributes();
            if (prefix == null) {
                Attr attrNode = (Attr)atts.getNamedItem(XMLConstants.XMLNS_ATTRIBUTE);
                if (attrNode != null) {
                    return new QName(attrNode.getValue(), lName);
                }
            } else {
                Attr attrNode = (Attr)atts.getNamedItem(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
                if(attrNode != null) {
                    return new QName(attrNode.getValue(), lName, prefix); //NOI18N
                }
            }
        }
        if (prefix == null) {
            return new QName(defaultNamespace, lName);
        } else {
            return new QName(null, lName, prefix);
        }
    }

    /**
     * Determines if this and previous tags are from same namespaces.
     */
    private boolean fromSameNamespace(Node current, Node previous) {
        String prevPrefix = CompletionUtil.getPrefixFromTag(previous.getNodeName());
        String thisPrefix = CompletionUtil.getPrefixFromTag(current.getNodeName());
        String thisNS = (thisPrefix == null) ? declaredNamespaces.get(
            XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+thisPrefix);
        String prevNS = (prevPrefix == null) ? declaredNamespaces.get(
            XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prevPrefix);

        return (thisNS == null && prevNS == null) ||
               (thisNS != null && thisNS.equals(prevNS)) ||
               (prevNS != null && prevNS.equals(thisNS));
    }

    /**
     * Returns the active no namespace model.
     */
    public CompletionModel getActiveNoNSModel() {
        return noNamespaceModel;
    }

    /**
     * Returns the CompletionModel map.
     * Maps target namespaces to the CompletionModels
     */
    public HashMap<String, CompletionModel> getCompletionModelMap() {
        return nsModelMap;
    }

    /**
     * Returns the list of no namespace CompletionModels.
     */
    public List<CompletionModel> getNoNamespaceModels() {
        return noNSModels;
    }

    /**
     * Returns the combined list of CompletionModels.
     */
    public List<CompletionModel> getCompletionModels() {
        List<CompletionModel> models = new ArrayList<>();
        models.addAll(nsModelMap.values());
        models.addAll(noNSModels);
        return models;
    }

    public void addCompletionModel(CompletionModel cm) {
        String tns = cm.getTargetNamespace();
        if(tns == null && !noNSModels.contains(cm)) {
            noNSModels.add(cm);
            return;
        }
        if(nsModelMap.get(tns) == null)
            nsModelMap.put(tns, cm);
    }

    /**
     * Finds all CompletionModelProviders and builds a model map for schemas having TNS
     * and builds a list for all no namespace models.
     */
    public boolean initModels() {
        Lookup.Template templ = new Lookup.Template(CompletionModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        if(impls == null || impls.isEmpty())
            return false;
        //first try all providers
        for(Object obj: impls) {
            CompletionModelProvider modelProvider = (CompletionModelProvider)obj;
            List<CompletionModel> models = modelProvider.getModels(this);
            if(models == null || models.isEmpty())
                continue;
            for(CompletionModel m: models) {
                populateModelMap(m);
            }
        }

        if(noNamespaceSchemaLocation != null && noNSModels.size() == 1) {
            noNamespaceModel = noNSModels.get(0);
        }

        //last resort: try special completion
        specialCompletion();

        return !(nsModelMap.isEmpty() && noNSModels.isEmpty());
    }

    private void populateModelMap(CompletionModel m) {
        if (m != null) {
            SchemaModel sm = m.getSchemaModel();
            if (sm != null) {
                Schema schema = sm.getSchema();
                if (schema != null) {
                    String tns = schema.getTargetNamespace();
                    if (tns != null) {
                        //models with namespaces
                        nsModelMap.put(tns, m);
                        return;
                    }
                }
            }
        }
        //
        noNSModels.add(m); //no namespace models
    }

    /**
     *  Special completion comes into play when all register providers fail.
     *  And we try to provide completion for docs like project.xml.
     */
    private void specialCompletion() {
        //instance documents with neither schemaLocation nor
        //noNamespaceSchemaLocation attribute, e.g. project.xml
        if(primaryFile == null)
            return;

//        specialCompletion = true;
        for(String temp : declaredNamespaces.values()) {
            try {
                if (nsModelMap.containsKey(temp)) {
                    // ignore, was added from specific location
                    continue;
                }
                if (schemaLocationMap.get(temp) != null) {
                    // ignore; already processed by default provider from schemaLocation attribute
                    continue;
                }
                if (temp.equals(targetNamespace)) {
                    // ignore: the NS is a targetNamespace defined by this schema
                    continue;
                }
                CompletionModel cm = DefaultModelProvider.getCompletionModel(new java.net.URI(temp), true, this);
                if (cm != null) {
                    populateModelMap(cm);
                    continue;
                }
            } catch (URISyntaxException | RuntimeException ex) {
                _logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Lets first try with "xmlns:ns1". If not used, use it. If used, we
     * keep trying with ns2, ns3 etc.
     */
    String suggestPrefix(String tns) {
        if(tns == null)
            return null;

        if (specialNamespaceMap.containsKey(tns)) {
            return specialNamespaceMap.get(tns);
        }

        //if the tns is already present in declared namespaces,
        //return the prefix
        for(Map.Entry<String, String> entry : getDeclaredNamespaces().entrySet()) {
            String key = entry.getKey();
            String ns = entry.getValue();
            if(ns.equals(tns))
                return key;
        }

        //then try to look that up in the suggested namespace
        for(Map.Entry<String, String> entry : suggestedNamespaces.entrySet()) {
            String key = entry.getKey();
            String ns = entry.getValue();
            if(ns.equals(tns))
                return key;
        }

        int index = suggestedNamespaces.size() + 1;
        String prefix = PREFIX + index;
        String nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+prefix;
        while(getDeclaredNamespaces().get(nsDecl) != null) {
            prefix = PREFIX + index++;
            nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":" + prefix;
        }
        suggestedNamespaces.put(prefix, tns);
        return prefix;
    }

    public boolean isPrefixBeingUsed(String prefix) {
        return getDeclaredNamespaces().
                get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix) != null;
    }

    public Boolean prefixConflicts(String prefix, String namespaceURI) {
        String n = getDeclaredNamespaces().get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
        if (n == null) {
            return null;
        } else {
            return n.equals(namespaceURI);
        }
    }

    public boolean isSpecialCompletion() {
        return specialCompletion;
    }

    public boolean canReplace(String text) {
        if(completionType == CompletionType.COMPLETION_TYPE_ELEMENT &&
                element.getType() == Node.ELEMENT_NODE) {
            String name = element.getNode().getNodeName();
            if(name != null && name.equals(typedChars) && text.equals(name))
                return false;
        }
        if(completionType == CompletionType.COMPLETION_TYPE_ATTRIBUTE) {
            Element e = CompletionUtil.findAXIElementAtContext(this);
            for(AbstractAttribute a : e.getAttributes()) {
                if(a.getName().equals(typedChars))
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns target namespace for a given prefix.
     */
    public String getTargetNamespaceByPrefix(String prefix) {
        for(CompletionModel cm : getCompletionModelMap().values()) {
            if(prefix.equals(cm.getSuggestedPrefix()))
                return cm.getTargetNamespace();
        }

        return null;
    }

    /**
     * Issue 108636 : Eliminate existing attibutes.
     */
    List<String> getExistingAttributes() {
        if(existingAttributes != null)
            return existingAttributes;
        existingAttributes = new ArrayList<>();
        try {
            support.runWithSequence(tokenOffset, (TokenSequence ts) -> {
                if (ts.movePrevious()) {
                    return null;
                }
                Token<XMLTokenId> item;

                while(ts.movePrevious()) {
                    item = ts.token();
                    if(item.id() == XMLTokenId.TAG)
                        break;
                    if(item.id() == XMLTokenId.ARGUMENT) {
                        existingAttributes.add(item.text().toString());
                    }
                }
                return null;
            });
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return existingAttributes;
    }
}