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
package org.netbeans.modules.javafx2.editor.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.sax.ContentLocator;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import static org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols.*;
import static org.netbeans.modules.javafx2.editor.JavaFXEditorUtils.FXML_FX_NAMESPACE;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.sax.SequenceContentHandler;
import org.netbeans.modules.javafx2.editor.completion.model.EventHandler;
import org.netbeans.modules.javafx2.editor.completion.model.FxInclude;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstanceCopy;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.openide.util.NbBundle;

import static org.netbeans.modules.javafx2.editor.parser.Bundle.*;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode.Kind;
import org.netbeans.modules.javafx2.editor.completion.model.FxObjectBase;
import org.netbeans.modules.javafx2.editor.completion.model.FxScriptFragment;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.modules.javafx2.editor.completion.model.ImportDecl;
import org.netbeans.modules.javafx2.editor.completion.model.LanguageDecl;
import org.netbeans.modules.javafx2.editor.completion.model.MapProperty;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.modules.javafx2.editor.completion.model.StaticProperty;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public class FxModelBuilder implements SequenceContentHandler, ContentLocator.Receiver {
    /**
     * URL of the current source.
     */
    private URL sourceURL;
    
    /**
     * Stack of Elements, as they are processed.
     */
    private Deque<FxNode> nodeStack = new LinkedList<FxNode>();
    
    /**
     * Supplemental interface, to get offsets & other info from the parser
     */
    private ContentLocator  contentLocator;
   
    /**
     * The created model
     */
    private FxModel     fxModel;
    
    /**
     * The current parsed bean instance.
     */
    private FxInstance  current;

    /**
     * Collected import declarations.
     */
    private List<ImportDecl>  imports = new ArrayList<ImportDecl>();
    
    /**
     * List of errors found during parsing
     */
    private List<ErrorMark> errors = new ArrayList<ErrorMark>();
    
    private ModelAccessor accessor = ModelAccessor.INSTANCE;
    
    private String controllerName;
    
    private FxInstance rootComponent;
    
    private LanguageDecl language;
    
    private String tagName;
    
    public void setBaseURL(URL sourceURL) {
        this.sourceURL = sourceURL;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // no op, we don't need SAX locator.
    }
    
    NodeInfo i(FxNode n) {
        return accessor.i(n);
    }
    
    private void initElement(FxNode node) {
        NodeInfo ni = i(node);
        ni.startAt(contentLocator.getElementOffset()).startContent(contentLocator.getEndOffset());
        ni.setTagName(tagName);
    }
    
    private void initAttribute(FxNode node, String atQName) {
        NodeInfo ni = i(node);
        ni.makeAttribute();
        int[] offsets =  contentLocator.getAttributeOffsets(atQName);
        ni.startAt(offsets[ContentLocator.OFFSET_START]).endsAt(offsets[ContentLocator.OFFSET_END]).
                startContent(offsets[ContentLocator.OFFSET_VALUE_START]).endContent(offsets[ContentLocator.OFFSET_VALUE_END]);
    }

    @Override
    public void startDocument() throws SAXException {
        fxModel = accessor.newModel(sourceURL, imports, instanceDefinitions);
        initElement(fxModel);
        
        nodeStack.push(fxModel);
    }
    
    @Override
    public void endDocument() throws SAXException {
        addElementErrors();
        accessor.initModel(fxModel, controllerName, rootComponent, language);
        accessor.addDefinitions(fxModel, instanceDefinitions);
        int end = contentLocator.getElementOffset();
        i(fxModel).endContent(end).endsAt(end, true);
        // attempt to fix up unclosed elements
        //fixNodes(i(fxModel), end);
    }
    
    private void fixNodes(NodeInfo ni, int pos) {
        for (Enumeration<FxNode> en = ni.getEnclosedNodes(); en.hasMoreElements(); ) {
            fixNode(i(en.nextElement()), pos);
        }
    }
    
    private void fixNode(NodeInfo ni, int pos) {
        if (ni.isDefined(TextPositions.Position.End)) {
            return;
        }
        if (ni.getEnd() == pos) {
            ni.markIncludeEnd();
            // recursively fix children
            fixNodes(ni, pos);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    
    private void addAttributeError(String qName, String code, String message, Object... params) {
        int[] offsets = contentLocator.getAttributeOffsets(qName);
        int s;
        
        if (offsets == null) {
            FxNode n = nodeStack.peek();
            if (n != null) {
                s = i(n).getStart();
            } else {
                s = -1;
            }
        } else {
            s = offsets[ContentLocator.OFFSET_START];
        }
        addError(new ErrorMark(
            s, qName.length(),
            code,
            message,
            params
        ));
    }
    
    private boolean isFxmlNamespaceUri(String uri) {
        if (uri == null || !uri.startsWith(FXML_FX_NAMESPACE)) {
            return false;
        }
        if (uri.length() == FXML_FX_NAMESPACE.length()) {
            return true;
        }
        if (uri.charAt(FXML_FX_NAMESPACE.length()) == '/') {
            try {
                Integer.parseInt(uri.substring(FXML_FX_NAMESPACE.length() + 1));
                return true;
            } catch (NumberFormatException ex) {
                // expected, invalid NS URI
            }
        }
        return false;
    }
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_tagNotJavaIdentifier=Invalid class name: {0}",
        "# {0} - tag name",
        "ERR_fxControllerPermittedOnRoot=fx:controller is not permitted on tag {0}. Can be only present on root element."
    })
    private FxNewInstance handleClassTag(String localName, Attributes atts) {
        String fxValueContent = null;
        String fxFactoryContent = null;
        String fxId = null;
        boolean constant = false;
        
        int off = contentLocator.getElementOffset() + 1; // the <
        
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (!isFxmlNamespaceUri(uri)) {
                // no special attribute
                continue;
            }
            String name = atts.getLocalName(i);
            if (FX_VALUE.equals(name)) {
                fxValueContent = atts.getValue(i);
            } else if (FX_ATTR_CONSTANT.equals(name)) {
                fxValueContent = atts.getValue(i);
                constant = true;
            } else if (FX_FACTORY.equals(name)) {
                fxFactoryContent = atts.getValue(i);
            } else if (FX_ID.equals(name)) {
                fxId = atts.getValue(i);
            } else if (FX_CONTROLLER.equals(name)) {
                if (nodeStack.peek().getKind() != Kind.Source) {
                    addAttributeError(atts.getQName(i),
                        "fx-controller-permitted-on-root",
                        ERR_fxControllerPermittedOnRoot(localName),
                        localName
                    );
                } else {
                    controllerName = atts.getValue(i);
                }
            } else {
                addAttributeError(
                    atts.getQName(i),
                    "invalid-property-reserved-name",
                    ERR_invalidReservedPropertyName(name),
                    name
                );
            }
        }
        
        // first we must check how this class tag is created. 
        FxNewInstance instance = accessor.createInstance(localName, fxValueContent, constant, fxFactoryContent, fxId);
        
        if (!FxXmlSymbols.isQualifiedIdentifier(localName)) {
            // not a java identifier, error
            addError(
                new ErrorMark(
                    off, localName.length(), 
                    "invalid-class-name", 
                    ERR_tagNotJavaIdentifier(localName),
                    localName
            ));
            accessor.makeBroken(instance);
            return instance;
        }

        return instance;
    }
    
    private static final String EVENT_HANDLER_PREFIX = "on"; // NOI18N
    private static final int EVENT_HANDLER_PREFIX_LEN = 2;
    private static final String EVENT_HANDLER_METHOD_PREFIX = "#";
    
    private FxNode processEventHandlerAttribute(String event, String content) {
        EventHandler eh;

        if (content != null && content.startsWith(EVENT_HANDLER_METHOD_PREFIX)) {
            eh = accessor.asMethodRef(accessor.createEventHandler(event));
            accessor.addContent(eh, content.substring(1));
            
        } else {
            eh = accessor.createEventHandler(event);
            if (content != null && content.length() > 0) {
                accessor.addContent(eh, content);
            }
        }
        return eh;
    }
    
    @NbBundle.Messages({
        "# {0} - attribute name",
        "ERR_lowercasePropertyName=Invalid property name: {0}. Property name, or the last component of a static property name must start with lowercase.",
        "# {0} - attribute name",
        "ERR_invalidReservedPropertyName=Unknown name in FXML reserved namespace: {0}",
        "# {0} - attribute qname",
        "# {1} - tag name",
        "ERR_unsupportedAttribute=Unsupported attribute {0} on {1}"
    })
    private void processInstanceAttributes(Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            String name = atts.getLocalName(i);
            String qname = atts.getQName(i);
            
            PropertySetter ps = null;
            
            FxNode node;
            
            if (qname.startsWith("xmlns")) { // NOI18N
                // FIXME - xmlns attributes will be represented as FxNodes :-/
                continue;
            }
            
            if (isFxmlNamespaceUri(uri)) {
                if (!FxXmlSymbols.isFxReservedAttribute(name)) {
                    addAttributeError(qname, "error-unsupported-attribute", 
                            ERR_unsupportedAttribute(qname, tagName), 
                            qname, tagName);
                }
                continue;
            }
            
            if (current instanceof FxInstanceCopy || current instanceof FxInclude) {
                if (FxXmlSymbols.FX_ATTR_REFERENCE_SOURCE.equals(name) && uri == null) {
                    // ignore source in fx:copy
                    continue;
                }
            } else if ((current instanceof FxNewInstance) && ((FxNewInstance)current).isCustomRoot()) {
                if (FxXmlSymbols.FX_ATTR_TYPE.equals(name) && uri == null) {
                    // ignore type in fx:root
                    continue;
                }
            }
            
            // if the name begins with "on", it's an event handler.
            if (name.startsWith(EVENT_HANDLER_PREFIX) && name.length() > EVENT_HANDLER_PREFIX_LEN) {
                String en = Character.toLowerCase(name.charAt(EVENT_HANDLER_PREFIX_LEN)) +
                        name.substring(EVENT_HANDLER_PREFIX_LEN + 1);
                node = processEventHandlerAttribute(en, atts.getValue(i));
                // special hack for fx:copy or fx:reference
            } else {
                // FIXME - error detection for static property
                int stProp = FxXmlSymbols.findStaticProperty(name);
                if (stProp == -2) {
                    // report error, not a well formed property name.
                    addAttributeError(
                            qname,
                            "invalid-property-name",
                            ERR_lowercasePropertyName(name),
                            name
                    );
                    node = accessor.makeBroken(accessor.createProperty(name, false));
                } else if (stProp == -1) {
                    // this is a normal property
                    node = ps = accessor.createProperty(name, false);
                } else {
                    // it is a static property
                    node = ps = accessor.createStaticProperty(
                            name.substring(stProp + 1),
                            name.substring(0, stProp)
                    );
                }
                if (ps != null) {
                    accessor.addContent(ps, atts.getValue(i));
                    node = ps;
                }
            }
            initAttribute(node, qname);
            attachProperty(ps);
            attachChildNode(node);
        }
    }
    
    private NodeInfo    definitionsNode;
    
    private List<FxNewInstance> instanceDefinitions = new ArrayList<FxNewInstance>();
    
    private int definitions;
    private boolean definitionsFound;
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_invalidFxElement=Unknown element in fx: namespace: {0}",
        "ERR_duplicateDefinitions=Duplicate 'definitions' element"
    })
    private FxNode handleFxmlElement(String localName, Attributes atts) {
        if (FX_DEFINITIONS.equals(localName)) {
            definitions++;
            
            if (definitionsFound) {
                // error, defs cannot be nested or used more than once. Ignore.
                addError("duplicate-definitions", ERR_duplicateDefinitions());
            }
            FxNode n = accessor.createElement(localName);
            definitionsNode = accessor.i(n);
            return n;
        } else if (FX_COPY.equals(localName)) {
            return handleFxReference(atts, true);
        } else if (FX_REFERENCE.equals(localName)) {
            return handleFxReference(atts, false);
        } else if (FX_INCLUDE.equals(localName)) {
            return handleFxInclude(atts, localName);
        } else if (FX_SCRIPT.equals(localName)) {
            return handleFxScript(atts);
        } else if (FX_ROOT.equals(localName)) {
            return handleFxRoot(atts);
        } else {
            // error, invalid fx: element
            FxNode n = accessor.createErrorElement(localName);
            initElement(n);
            addError("invalid-fx-element", ERR_invalidFxElement(localName), localName);
            return n;
        }
    }
    
    @NbBundle.Messages({
        "ERR_rootMissingType=fx:root is missing ''type'' attribute"
    })
    private FxNode handleFxRoot(Attributes atts) {
        String typeName = null;
        String fxId = null;
        
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
             if (FX_ATTR_TYPE.equals(name)) {
                 typeName = atts.getValue(i);
             } else if (FX_CONTROLLER.equals(name)) {
                if (nodeStack.peek().getKind() != Kind.Source) {
                    addAttributeError(atts.getQName(i),
                        "fx-controller-permitted-on-root",
                        ERR_fxControllerPermittedOnRoot(FX_ROOT),
                        FX_ROOT
                    );
                } else {
                    controllerName = atts.getValue(i);
                }
            } else if (FX_ID.equals(name)) {
                fxId = atts.getValue(i);
            }
        }
        int off = contentLocator.getElementOffset() + 1; // the <
        boolean broken = false;
        if (typeName == null) {
            addError(
                new ErrorMark(
                    off, contentLocator.getEndOffset() - off, 
                    "root-missing-type", 
                    ERR_rootMissingType(),
                    null
            ));
            typeName = "";
            broken = true;
        } else if (!FxXmlSymbols.isQualifiedIdentifier(typeName)) {
            // not a java identifier, error
            int[] offsets = contentLocator.getAttributeOffsets(FX_ATTR_TYPE);
            int start, len;

            if (offsets == null) {
                start = off;
                len = contentLocator.getEndOffset() - off;
            } else {
                start = offsets[0];
                len = offsets[1] - start;
            }
            addError(
                new ErrorMark(
                    start, len,
                    "invalid-class-name", 
                    ERR_tagNotJavaIdentifier(typeName),
                    typeName
            ));
            broken = true;
        }
        FxNewInstance instance = accessor.createCustomRoot(typeName, fxId);
        
        if (broken) {
            accessor.makeBroken(instance);
        }
        return instance;
    }
    
    @NbBundle.Messages({
        "#0 - attribute name",
        "ERR_unexpectedScriptAttribute=Unexpected attribute in fx:script: {0}"
    })
    private FxNode handleFxScript(Attributes atts) {
        String ref = null;
        
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getLocalName(i);
             if (!FX_ATTR_REFERENCE_SOURCE.equals(name)) {
                addAttributeError(atts.getQName(i),
                    "invalid-script-attribute",
                    ERR_unexpectedScriptAttribute(name),
                    name
                );
                continue;
            }
            ref = atts.getValue(i);
        }
        return accessor.createScript(ref);
    }
    
    @NbBundle.Messages({
        "# {0} - attribute local name",
        "ERR_unexpectedReferenceAttribute=Unexpected attribute in fx:reference or fx:copy: {0}",
        "ERR_missingReferenceSource=Missing 'source' attribute in fx:reference or fx:copy"
    })
    private FxNode handleFxReference(Attributes atts, boolean copy) {
        String refId = null;
        String id = null;
        
        for (int i = 0; i < atts.getLength(); i++) {
            String ns = atts.getURI(i);
            String name = atts.getLocalName(i);
            if (!isFxmlNamespaceUri(ns)) {
                if (FX_ATTR_REFERENCE_SOURCE.equals(name) && refId == null) {
                    refId = atts.getValue(i);
                } else if (!copy) {
                    // error, references do not support normal attributes
                    addAttributeError(atts.getQName(i),
                        "invalid-reference-attribute",
                        ERR_unexpectedReferenceAttribute(name),
                        name
                    );
                }
            } else {
                if (FX_ID.equals(name) && id == null) {
                    id = atts.getValue(i);
                } else {
                    // error, unexpected attribute
                    addAttributeError(atts.getQName(i),
                        "invalid-reference-attribute",
                        ERR_unexpectedReferenceAttribute(name),
                        name
                    );
                }
            }
        }
        
        FxObjectBase ref = accessor.createCopyReference(copy, refId);
        if (refId == null || "".equals(refId)) {
            // error, no source attribute found
            addError(
                    "missing-reference-source",
                    ERR_missingReferenceSource()
            );
            accessor.makeBroken(ref);
        }
        return ref;
    }
    
    private void pushInstance(FxNode instance) {
        nodeStack.push(instance);
        if (instance.getKind() == Kind.Instance || instance.getKind() == Kind.Include) {
            current = (FxInstance)instance;
        } else {
            current = null;
        }
    }
    
    /**
     * Checks that the instance is allowed in this context. May even create e.g.
     * default property setter etc. Will return true, if the instance can be attached to the parent.
     */
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_moreRootElements=Duplicate root element: {0}",
        "ERR_instanceInMapProperty=Cannot add instances directly to readonly Map",
        "# {0} - parent tag name",
        "ERR_parentNotSupportInstance=Instances cannot be added to the parent {0}"
    })
    private FxNode attachInstance(FxObjectBase instance) {
        String localName = instance.getSourceName();
        int off = contentLocator.getElementOffset() + 1;
        
        // check the parent, whether it is appropriate to host such a node:
        FxNode parent = nodeStack.peek();
        
        if (parent.getKind() == Kind.Instance) {
            // pretend we have a default property
            PropertySetter s = accessor.createProperty(null, true);
            i(s).startAt(contentLocator.getElementOffset());
            attachChildNode(s);
            parent = s;
        }
        
        if (parent.getKind() == Kind.Source) {
            FxObjectBase old = rootComponent;
            if (old != null) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "duplicate-root",
                    ERR_moreRootElements(localName),
                    localName
                ));
                accessor.makeBroken(instance);
            } else if (!(instance instanceof FxInstance)) {
                // FIXME - report error that fx:reference is not accepted on root element
                throw new UnsupportedOperationException();
            } else {
                rootComponent = (FxInstance)instance;
            }
        } else if (parent.getKind() == Kind.Property) {
            if (parent instanceof MapProperty) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "instance-in-map-property",
                    ERR_instanceInMapProperty(),
                    localName
                ));
                accessor.makeBroken(instance);
            }
        } else if (parent.getKind() == Kind.Element &&
                parent.getSourceName().equals(FxXmlSymbols.FX_DEFINITIONS) && (instance instanceof FxNewInstance)) {
            instanceDefinitions.add((FxNewInstance)instance);
        } else {
            if (parent.getKind() != Kind.Error) {
                addError(new ErrorMark(
                    off, contentLocator.getEndOffset() - off,
                    "parent-not-support-instance",
                    ERR_parentNotSupportInstance(parent.getSourceName()))
                );
                accessor.makeBroken(instance);
            }
        }
        return instance;
    }
    
    private int start;
    private int end;
    
    private FxNode handleEventHandlerTag(String eventName) {
        FxNode node = accessor.createEventHandler(eventName);
        FxNode parent = nodeStack.peek();
        if (!(parent instanceof FxInstance)) {
            accessor.makeBroken(node);
        }
        return node;
    }

    @NbBundle.Messages({
        "# {0} - NS prefix",
        "ERR_undeclaredElementPrefix=XML namespace prefix ''{0}'' is undeclared"
    })
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.tagName = localName;
        
        FxNode newElement;
        
        start = contentLocator.getElementOffset();
        end = contentLocator.getEndOffset();
        
        addElementErrors();
        
        
        if (uri == null && !qName.equals(localName)) {
            // undeclared prefix
            int prefColon = qName.indexOf(':');
            String prefix = qName.substring(0, prefColon);
            addError("undeclared-prefix", ERR_undeclaredElementPrefix(prefix));
            newElement = accessor.createErrorElement(localName);
        } else if ("".equals(localName)) {
            newElement = accessor.createErrorElement(localName);
        } else if (isFxmlNamespaceUri(uri)) {
            newElement = handleFxmlElement(localName, atts);
        } else {
            // non-fx namespace, should be either an instance, or a property or an event
            String eventName = FxXmlSymbols.getEventHandlerName(localName);
            if (rootComponent == null || FxXmlSymbols.isClassTagName(localName)) {
                newElement = handleClassTag(localName, atts);
            } else if (eventName != null) {
                newElement = handleEventHandlerTag(eventName);
            } else {
                newElement = handlePropertyTag(localName, atts);
            }
        }
        if (newElement == null) {
            throw new IllegalStateException();
        }
        initElement(newElement);
        
        FxNode newNode = newElement;
        
        // if not broken attempt to attach the Element to a parent
        if (!newElement.isBroken()) {
            if (newElement instanceof FxObjectBase) {
                newNode = attachInstance((FxObjectBase)newElement);
            } else if (newElement instanceof PropertyValue) {
                newNode = attachProperty((PropertyValue)newElement);
            }
        }
        attachChildNode(newNode);
        
        // process attributes, iff it is an instance. Attribute processing needs the node pushed
        // on the stack, so it is delayed after attachChildNode
        if (newNode.getKind() == Kind.Instance || newNode.getKind() == Kind.Include) {
            processInstanceAttributes(atts);
        }
    }
    
    private PropertyValue handleStaticProperty(String className,  String propName, Attributes atts) {
        // FIXME - check that attributes are empty
        StaticProperty s = accessor.createStaticProperty(propName, className);
        return s;
    }
    
    /**
     * Processes instance (non-static) property. As per examples in Guides, instance
     * property element must NOT have any attributes; otherwise it corresponds to
     * an readonly Map element, and the property must be of the Map type.
     * 
     * @param propName
     * @param atts As
     */
    @NbBundle.Messages({
        "# {0} - attribute name",
        "ERR_propertyElementNamespacedAttribute=Property elements may not contain attributes with namespace: {0}"
    })
    private PropertyValue handleSimpleProperty(String propName, Attributes atts) {
        PropertyValue p;
        
        // no relevant attributes to use, real simple property then
        p = accessor.createProperty(propName, false);
        
        return p;
    }
    
    @NbBundle.Messages({
        "# {0} - parent tag local name",
        "ERR_doesNotAcceptProperty=The parent element {0} does not accept properties"
    })
    private FxNode attachProperty(PropertyValue p) {
        // FIXME - if 'current' is null,
        if (current == null) {
            FxNode node = nodeStack.peek();
            addError(new ErrorMark(
                start, end - start, 
                "parent-not-accept-property",
                ERR_doesNotAcceptProperty(node.getSourceName()),
                node
            ));
            accessor.makeBroken(p);
        }
        return p;
    }
    
    private PropertyValue handleMapProperty(String propName, Attributes atts) {
        Map<String, CharSequence> contents = new HashMap<String, CharSequence>();
        
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (uri != null) {
                continue;
            }
            contents.put(atts.getLocalName(i), atts.getValue(i));
        }
        return accessor.createMapProperty(propName, contents);
    }
    
    @NbBundle.Messages({
        "# {0} - tag name",
        "ERR_invalidPropertyName=Invalid property name: {0}"
    })
    private FxNode handlePropertyTag(String propName, Attributes atts) {
        PropertyValue pv;
        
        int errorAttrs = 0;
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            if (uri != null) {
                String qn = atts.getQName(i);
                errorAttrs++;
                addAttributeError(qn, 
                    "property-namespaced-attribute",
                    ERR_propertyElementNamespacedAttribute(qn),
                    qn
                );
            }
        }
        
        int stProp = FxXmlSymbols.findStaticProperty(propName);
        switch (stProp) {
            case -1:
                // simple property
                if (!Utilities.isJavaIdentifier(propName)) {
                    addError(new ErrorMark(
                        start, end,
                        "invalid-property-name",
                        ERR_invalidPropertyName(propName),
                        propName
                    ));
                }
                if (errorAttrs == atts.getLength()) {
                    pv = handleSimpleProperty(propName, atts);
                } else {
                    pv = handleMapProperty(propName, atts);
                }
                break;
                
            case -2:
                // broken name, but must create a node
                pv = accessor.makeBroken(accessor.createProperty(propName, false));
                // do not add the property to the parent, it's broken beyond repair
                addError(new ErrorMark(
                    start, end,
                    "invalid-property-name",
                    ERR_invalidPropertyName(propName),
                    propName
                ));
                break;
                
            default:
                // static property, just ignore for now
                pv = handleStaticProperty(propName.substring(0, stProp), 
                        propName.substring(stProp + 1), atts);
                break;
        }
        
        return pv;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        addElementErrors();
        FxNode node = nodeStack.pop();
        i(node).endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getElementOffset());
        if (node instanceof PropertySetter) {
            PropertySetter s = (PropertySetter)node;
            if (s.isImplicit()) {
                // actually the outer element ends
                node = nodeStack.pop();
                // copy the offset information
                i(node).endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getElementOffset());
            }
        }
        String tn = node.getSourceName();
        if (!tn.equals(localName)) {
            throw new IllegalStateException();
        }
        // special hack for parent nodes, which are implicit property setters:
        FxNode parentNode = nodeStack.peek();
        if (parentNode instanceof PropertySetter) {
            PropertySetter ps = (PropertySetter)parentNode;
            if (ps.isImplicit() && ps.getContent() == null) {
                i(ps).endsAt(contentLocator.getEndOffset()).endContent(contentLocator.getEndOffset());
                // there should be a parent to the property setter, take the setter's parent as potential parent instance
                if (nodeStack.size() > 1) {
                    parentNode = ((LinkedList<? extends FxNode>)nodeStack).get(1);
                }
            }
        }
        if (parentNode != null && parentNode.getKind() == Kind.Instance) {
            current = (FxInstance)parentNode;
        } else {
            current = null;
        }
        
    }

    @Override
    public void characterSequence(CharSequence seq) {
        
        addElementErrors();
        
        int length = seq.length();
        FxNode node = nodeStack.peek();
        FxNode addedNode = null;
        
        switch (node.getKind()) {
            case Event:
                addedNode = handleEventContent(seq);
                break;
            case Instance:
                addedNode = handleInstanceContent(seq);
                break;
            case Property:
                addedNode = handlePropertyContent(seq);
                break;
            case Script:
                addedNode = handleScriptContent(seq);
                break;
            default:
                addError(new ErrorMark(
                    contentLocator.getElementOffset(),
                    length,
                    "unexpected-characters",
                    ERR_unexpectedCharacters()
                ));
        }
        if (addedNode != null) {
            i(addedNode).endsAt(contentLocator.getEndOffset());
        }

    }
    
    @NbBundle.Messages({
        "ERR_scriptHasContentAndSource=A script with external source cannot have inline content",
        "ERR_scriptWithoutLanguage=No script language is declared"
    })
    private FxNode handleScriptContent(CharSequence content) {
        FxScriptFragment script = (FxScriptFragment)nodeStack.peek();
        if (content.length() > 0) {
            if (language == null) {
                addError(
                    "script-np-language", 
                    ERR_scriptWithoutLanguage()
                );
                accessor.makeBroken(script);
            }
        }
        if (script.getSourcePath() != null) {
            // warn just for the 1st content chunk.
            if (!script.hasContent()) {
                addError(
                    "script-source-and-content", 
                    ERR_scriptHasContentAndSource()
                );
                accessor.makeBroken(script);
            }
        }
        accessor.addContent(script, content);
        return script;
    }
    
    private FxNode handleEventContent(CharSequence content) {
        EventHandler eh = (EventHandler)nodeStack.peek();
        if (eh.isScript() && !eh.hasContent()) {
            if (content.length() == 0) {
                throw new UnsupportedOperationException();
            } else {
                if (content.charAt(0) == '#') {
                    content = content.subSequence(1, content.length());
                    eh = accessor.asMethodRef(eh);
                }
            }
        }
        accessor.addContent(eh, content);
        return eh;
    }

    @NbBundle.Messages({
        "ERR_unexpectedCharacters=Unexpected character content"
    })
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        System.err.println("");
    }
    
    private FxNode handleInstanceContent(CharSequence seq) {
        // find among properties as setter, which is marked as implicit. If there's none, create one.
        PropertySetter defaultSetter = null;
        
        for (PropertyValue p : current.getProperties()) {
            if (p instanceof PropertySetter) {
                PropertySetter ps = (PropertySetter)p;
                if (ps.isImplicit()) {
                    defaultSetter = ps;
                }
            }
        }
        
        if (defaultSetter == null) {
            defaultSetter = accessor.createProperty(null, true);
            i(defaultSetter).startAt(contentLocator.getElementOffset());
            attachProperty(defaultSetter);
            attachChildNode(defaultSetter);
        }
        accessor.addContent(defaultSetter, seq);
        return defaultSetter;
    }
    
    private ErrorMark addError(String errCode, String message, Object... params) {
        int offs = contentLocator.getElementOffset();
        ErrorMark m = new ErrorMark(
            offs,
            contentLocator.getEndOffset() - offs,
            errCode,
            message, 
            params
        );
        addError(m);
        return m;
    }

    @NbBundle.Messages({
        "ERR_mixedContentNotAllowed=Mixed content is not allowed in property elements"
    })
    private FxNode handlePropertyContent(CharSequence seq) {
        FxNode node = nodeStack.peek();
        if (!(node instanceof PropertySetter)) {
            addError(
                "unexpected-characters", 
                ERR_unexpectedCharacters()
            );
            return null;
        }
        // if the property has already received some bean instances, report 
        // invalid content
        PropertySetter ps = (PropertySetter)node;
        if (!ps.getValues().isEmpty()) {
            addError(
                "mixed-content-not-allowed", 
                ERR_mixedContentNotAllowed()
            );
        }
        accessor.addContent((PropertySetter)node, seq);
        return node;
    }

    @Override
    public void ignorableWhitespaceSequence(CharSequence seq) {
        addElementErrors();
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // check whether the current node supports content
    }

    @NbBundle.Messages({
        "ERR_instructionBadPlacement=Bad placement for processing instruction. Must be before all elements."
    })
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        start = contentLocator.getElementOffset();
        end = contentLocator.getEndOffset();
        
        addElementErrors();
        
        FxNode node = null;
        boolean broken = false;
        if (!isTopLevel() || rootComponent != null) {
            addError("instruction-bad-placement", ERR_instructionBadPlacement());
            broken = true;
        }
        
        if (FX_IMPORT.equals(target)) {
            node = handleFxImport(data);
        } else if (FX_LANGUAGE.equals(target)) {
            node = handleFxLanguage(data);
        } else if (target != null && target.startsWith(FxXmlSymbols.SCENEBUILDER_PI_PREFIX)) {
            // valid, but uninterpreted
            return;
        } else if (!"xml".equals(target)) {
            handleErrorInstruction(target, data);
        }
        if (node == null) {
            return;
        }
        i(node).makePI().startAt(start).endsAt(end);
        if (broken) {
            accessor.makeBroken(node);
        }
        attachChildNode(node);
    }
    
    /**
     * Processes "import" PI. Checks syntax of the identifier
     * @param data 
     */
    @NbBundle.Messages({
        "ERR_importNotJavaIdentifier=Imported symbol must be a class or package name.",
        "ERR_importInsideElement=Imports must be at top level, not nested in elements",
        "ERR_importFollowsRoot=Import must not follow the root element",
        "ERR_missingImportIdentifier=Identifier missing in ?import instruction"
    })
    private FxNode handleFxImport(String data) {
        if (data.endsWith("?")) {
            // recovery from unterminated ?> -- the lexer will report ? as part of PI data.
            data = data.substring(0, data.length() -1);
        }
        if ("".equals(data)) {
            addError("missing-import-identifier", ERR_missingImportIdentifier());
            return null;
        }
        int lastDot = data.lastIndexOf('.');
        boolean starImport = false;
        
        if (lastDot != -1 && lastDot < data.length() - 1) {
            if (FX_IMPORT_STAR.equals(data.substring(lastDot + 1))) {
                starImport = true;
                data = data.substring(0, lastDot);
            }
        }
        ImportDecl decl = accessor.createImport(data, starImport);
        if (!FxXmlSymbols.isQualifiedIdentifier(data)) {
            addAttributeError(ContentLocator.ATTRIBUTE_DATA,
                "import-not-java-identifier",
                ERR_importNotJavaIdentifier(), data
            );
            accessor.makeBroken(decl);
        }
        
        imports.add(decl);
        
        return decl;
    }
    
    /**
     * Processes ?include directive
     * 
     * @param include 
     */
    @NbBundle.Messages({
        "ERR_missingIncludeName=Missing include name",
        "# {0} - attribute name",
        "ERR_unexpectedIncludeAttribute=Unexpected attribute in fx:include: {0}"
    })
    private FxNode handleFxInclude(Attributes atts, String localName) {
        String include = null;
        String id = null;
        
        for (int i = 0; i < atts.getLength(); i++) {
            String uri = atts.getURI(i);
            String attName = atts.getLocalName(i);
            if (FX_ATTR_REFERENCE_SOURCE.equals(attName)) {
                include = atts.getValue(i);
                continue;
            }
            if (isFxmlNamespaceUri(uri)) {
                if (FX_ID.equals(attName)) {
                    id = atts.getValue(i);
                } else {
                    String qName = atts.getQName(i);
                    addAttributeError(
                        qName,
                        "unexpected-include-attribute",
                        ERR_unexpectedIncludeAttribute(qName),
                        qName
                    );
                }
            }
        }
        if (include == null) {
            // must be some text, otherwise = error
            addAttributeError(
                ContentLocator.ATTRIBUTE_TARGET, 
                "missing-included-name",
                ERR_missingIncludeName()
            );
            
            FxNode n = accessor.createErrorElement(localName);
            initElement(n);
            addError("invalid-fx-element", ERR_invalidFxElement(localName), localName);
            return n;
        }
        // guide: fnames starting with slash are treated relative to the classpath
        FxInclude fxInclude = accessor.createInclude(include, id);
        return fxInclude;
    }
    
    @NbBundle.Messages({
        "ERR_missingLanguageName=Language name is missing",
        "ERR_duplicateLanguageDeclaration=Language is already declared"
    })
    private FxNode handleFxLanguage(String language) {
        LanguageDecl decl = accessor.createLanguage(language);
        if (language == null) {
            addAttributeError(
                ContentLocator.ATTRIBUTE_TARGET,
                "missing-language-name",
                ERR_missingLanguageName()
            );
            accessor.makeBroken(decl);
        } else {
            if (this.language != null) {
                // error, language can be specified only once:
                addError(new ErrorMark(
                    start, end - start,
                    "duplicate-language",
                    ERR_duplicateLanguageDeclaration(),
                    fxModel.getLanguage()
                ));
                accessor.makeBroken(decl);
            } else if (isTopLevel()) {
                this.language = decl;
            }
        }
        return decl;
    }
    
    private boolean isTopLevel() {
        return nodeStack.peek() == fxModel;
    }
    
    void addElementErrors() {
        this.errors.addAll(contentLocator.getErrors());
    }
    
    void addError(ErrorMark mark) {
        this.errors.add(mark);
    }
    
    private void attachChildNode(FxNode node) {
        FxNode top = nodeStack.peek();
        // special handling for opened implicit property setter, and a nested property tag:
        if (top instanceof PropertySetter) {
            PropertySetter topSetter = (PropertySetter)top;
            if (topSetter.isImplicit() && current != null) {
                if (!(node instanceof FxObjectBase)) {
                    top = current;
                }
            }
        }
        i(top).addChild(node);
//        if (!node.isBroken() && (node.getKind() != FxNode.Kind.Element)) {
        accessor.attach(node, fxModel);
        if (!node.isBroken() &&
            // special case, see #220424
            i(node) != definitionsNode) {
            accessor.addChild(top, node);
        }
        if (i(node).isElement()) {
            pushInstance(node);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - PI target",
        "ERR_invalidProcessingInstruction=Invalid processing instruction: {0}. Expected 'import', 'include' or 'language'",
        "ERR_missingProcessingInstruction=Missing processing intruction."
    })
    private void handleErrorInstruction(String target, String data) {
        int start = contentLocator.getElementOffset();
        int offset = -1;
        int piOffset = -1;
        
        TokenSequence<XMLTokenId> seq = contentLocator.getTokenSequence();
        
        // lex up to the invalid target:
        seq.move(start);
        boolean found = false;
        while (!found && seq.moveNext()) {
            Token<XMLTokenId> t = seq.token();
            switch (t.id()) {
                case PI_START:
                    piOffset = offset;
                    if (target == null) {
                        found = true;
                    }
                case WS:
                    break;
                    
                default:
                case PI_TARGET:
                    offset = seq.offset();
                    found = true;
                    break;
            }
        }
        ErrorMark mark;
        
        if (target != null) {
            mark = new ErrorMark(offset, seq.token().length(), 
                    "invalid-processing-instruction", 
                    ERR_invalidProcessingInstruction(target),
                    target
            );
        } else {
            mark = new ErrorMark(piOffset, seq.token().length(), 
                    "missing-processing-instruction",
                    ERR_missingProcessingInstruction()
            );
        }
        addError(mark);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void setContentLocator(ContentLocator l) {
        this.contentLocator = l;
    }
    
    FxModel getModel() {
        return fxModel;
    }
    
    List<ErrorMark> getErrors() {
        return errors;
    }
}
