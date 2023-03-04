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
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.javafx2.editor.JavaFXEditorUtils;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.beans.FxProperty;
import org.netbeans.modules.javafx2.editor.completion.model.CompoundCharSequence;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.javafx2.editor.parser.processors.ImportProcessor;
import org.netbeans.modules.javafx2.editor.completion.model.PropertySetter;
import org.netbeans.modules.javafx2.editor.completion.model.PropertyValue;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 * Provides information about the soruce, caret position etc to the individual
 * Completers. It is created for each invocation of the CC and shared between
 * Completers. The infrastructure can alter state between Completer invocations,
 * e.g. to provide custom state flags.
 *
 * @author sdedic
 */
public final class CompletionContext {
    private boolean attribute;
    
    /**
     * Caret offset where the completion was invoked
     */
    private int caretOffset;
    
    /**
     * The completion prefix, typed in the current token
     */
    private String prefix;
    
    /**
     * Offset at which the replacement is supposed to happen
     */
    private int startOffset = -1;
    
    /**
     * Length of the tail of the token after the caret, possibly replaced
     */
    private int tokenTail;
    
    /**
     * For processing instructions, this is the target of the instruction
     */
    private String piTarget;
    
    /**
     * The data / content for processing instructions
     */
    private String piData;
    
    /**
     * Tag name of the tag with the caret. Null if caret is outside of all tags or
     * in the tag content.
     */
    private String tagName;
    
    /**
     * Start of the tag or PI where the position is located. 
     */
    private int tagStartOffset = -1;
    
    /**
     * Start of the root tag, or -1 if does not exist yet
     */
    private int rootTagStartOffset = -1;
    
    private Type type;
    
    private ClasspathInfo   cpInfo;
    
    private CompilationInfo compilationInfo;

    private Document doc;
    
    private WhiteListQuery.WhiteList    typeWhiteList;
    
    /**
     * Insertion offset for additional root element attributes
     */
    private int rootAttrInsertOffset;
    
    /**
     * True, if the current tag is the root one
     */
    private boolean currentIsRoot;
    
    public enum Type {
        /**
         * The context is not known, probably empty completion
         */
        UNKNOWN,
        /**
         * The user is trying to type in a root element, processing instruction
         * or a bean
         */
        ROOT,
        /**
         * The user is trying to type a processing instruction target
         */
        INSTRUCTION_TARGET,
        /**
         * Data for processing instruction, the behavour depends on the
         * instruction target
         */
        INSTRUCTION_DATA,
        
        /**
         * PI end marker
         */
        INSTRUCTION_END,
        
        /**
         * Unspecified child element: can be either a Bean or a property element,
         * depending on further analysis
         */
        CHILD_ELEMENT,
         
        /**
         * Bean element
         */
        BEAN,
        
        /**
         * Property element
         */
        PROPERTY_ELEMENT,
        
        /**
         * Property attribute
         */
        PROPERTY,
        
        /**
         * Property value contents, either in attribute, or in element content
         */
        PROPERTY_VALUE,
        
        /**
         * Property value content; without ""
         */
        PROPERTY_VALUE_CONTENT,
        
        /**
         * Variable completion inside property elements or attributes
         */
        VARIABLE,
        
        /**
         * Variable binding inside property values
         */
        BINDING,
        
        /**
         * Reference to bundles inside property values
         */
        BUNDLE_REF,
        
        /**
         * Reference to resources inside property values
         */
        RESOURCE_REF,
        
        /**
         * Event handler name
         */
        HANDLER;
    }
    
    /**
     * Completion query type
     */
    private int queryType;
    
    /**
     * Result of FXML parsing
     */
    private FxmlParserResult    fxmlParserResult;
    
    private TokenHierarchy<?> hierarchy;
    
    /**
     * Parent FxNodes of the current position.
     */
    private List<? extends FxNode>  parents;
    
    /**
     * Parent for newly inserted elements. Not necessarily immediate position's parent: if the
     * caret is positioned within an attribute, the element parent is the FxNode corresponding
     * to the attribute's element
     */
    private FxNode  elementParent;
    
    public CompletionContext(Document doc, int offset, int completionType) {
        this.doc = doc;
        this.caretOffset = offset;
        this.queryType = completionType;
    }

    @SuppressWarnings("unchecked")
    /* For testing only */ CompletionContext(FxmlParserResult result, int offset, int completionType) {
        this.fxmlParserResult = result;
        this.hierarchy = result.getTokenHierarchy();
        this.caretOffset = offset;
        this.queryType = completionType;
        processTokens(hierarchy);
    }

    public int getCompletionType() {
        return queryType;
    }
    
    public void init(TokenHierarchy h, CompilationInfo info, FxmlParserResult fxmlResult) {
        this.hierarchy = h;
        this.compilationInfo = info;
        this.cpInfo = info.getClasspathInfo();
        this.fxmlParserResult = fxmlResult;
        processTokens(h);
    }
    
    public Source getSource() {
        return fxmlParserResult.getSnapshot().getSource();
    }
    
    public FxModel    getModel() {
        return fxmlParserResult.getSourceModel();
    }
    
    public FxBean   getBeanInfo(String className) {
        if (className == null) {
            return null;
        }
        return FxBean.getBeanProvider(compilationInfo).getBeanInfo(className);
    }
    
    public FxBean   getBeanInfo(FxInstance inst) {
        return getBeanInfo(inst.getResolvedName());
    }
    
    public String getSimpleClassName(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        String sn = fqn.substring(lastDot + 1);
        if (fqn.equals(resolveClassName(sn))) {
            return sn;
        } else {
            return null;
        }
    }
    
    /**
     * Resolves name using import rules
     * 
     * @param name
     * @return resolved name or null if not known or ambiguous.
     */
    public String resolveClassName(String name) {
        Collection<String> names = fxmlParserResult.resolveClassName(compilationInfo, name);
        if (names == null || names.size() > 1) {
            return null;
        }
        return names.iterator().next();
    }

    /**
     * Main method for initializing the context.
     * @param h 
     */
    @SuppressWarnings("unchecked")
    private void processTokens(TokenHierarchy h) {
        TokenSequence<XMLTokenId> ts = (TokenSequence<XMLTokenId>)h.tokenSequence();
        
        readRootElement(ts);

        // no completion after root end:
        FxModel m = fxmlParserResult.getSourceModel();
        FxNode n = m.getRootComponent();
        if (n != null) {
            TextPositions pos = fxmlParserResult.getTreeUtilities().positions(n);
            int end = pos.getEnd();
            // if root is not defined, offer.
            if (caretOffset >= end && pos.isDefined(TextPositions.Position.End)) {
                type = Type.UNKNOWN;
            }
        }

        processType(ts);
        processValueType();
        
        
        // do not allow real content before root tag
        if (rootTagStartOffset != -1 && caretOffset < rootTagStartOffset) {
            switch (type) {
                case BEAN:
                case PROPERTY_VALUE:
                case PROPERTY_ELEMENT:
                case CHILD_ELEMENT:
                case ROOT:
                    type = Type.INSTRUCTION_TARGET;
                    break;
            }
        }
        
        readCurrentContent(ts);

        processNamespaces();
        
        processPath();
        
        // no parents above
        if (parents.size() == 1) {
            switch (type) {
                case PROPERTY_VALUE:
                    type = Type.UNKNOWN;
                    break;
                    
                case PROPERTY_ELEMENT:
                case CHILD_ELEMENT:
                    type = Type.BEAN;
            }
        }
        
        // try to narrow the CHILD_ELEMENT if possible:
        if (getType() == Type.CHILD_ELEMENT && !getParents().isEmpty()) {
            List<? extends FxNode> parents = getParents();
            n = parents.get(0);
            if (n.getKind() == FxNode.Kind.Property) {
                type = Type.BEAN;
            } else if (n.getKind() == FxNode.Kind.Instance) {
                FxInstance inst = (FxInstance)n;
                FxBean bi = getBeanInfo(inst);
                if (bi != null) {
                    if (bi.getDefaultProperty() == null) {
                        type = Type.PROPERTY_ELEMENT;
                    } else {
                        for (PropertyValue pv : inst.getProperties()) {
                            if ((pv instanceof PropertySetter) &&
                                ((PropertySetter)pv).isImplicit()) {
                                type = Type.PROPERTY_ELEMENT;
                                break;
                            }
                        }
                    }
                }
            } else if (n.getKind() == FxNode.Kind.Property) {
                type = Type.PROPERTY_VALUE_CONTENT;
            } else if (n.getKind() == FxNode.Kind.Event) {
                type = Type.HANDLER;
            }
        }
        findNextCaretPos(ts);
    }
    
    private boolean replaceExisting;

    public boolean isReplaceExisting() {
        return replaceExisting;
    }
    
    /**
     * Given the caret pos, the method finds a suitable next position for the caret,
     * after the text at caret pos is completed or replaced.
     * <p/>
     * Also initializes the 'replaceExisting' property - if the caret is positioned
     * within a token, and the completion should overwrite it. This property controls
     * whether whitespace separators should be added after inserted text. If replacement
     * is made, no whitespace is inserted - it's already present.
     * 
     * @param ts 
     */
    private void findNextCaretPos(TokenSequence ts) {
        int off = ts.move(caretOffset);
        Token<XMLTokenId>  t;
        if (off == 0 && caretOffset == startOffset) {
            return;
        }
        
        switch (type) {
            case PROPERTY: {
                // the next position is within the value, if it is present
                boolean wsFound = false;
                
                while (ts.moveNext()) {
                    t = ts.token();
                    switch (t.id()) {
                        case WS:
                            wsFound = true;
                            break;
                            
                        case ARGUMENT:
                            if (wsFound) {
                                // ws before next attribute name, bail out
                                return;
                            }
                            
                        case OPERATOR: 
                        case VALUE:
                            replaceExisting = true;
                            nextCaretPos = ts.offset() + 1;
                            return;

                        default:
                            return;
                    }
                }
                break;
            }
            case ROOT:
            case BEAN:
            case CHILD_ELEMENT:
            case PROPERTY_ELEMENT:
                // do not search, if the prefix is just "<" and nothing important follows
                if (prefix.length() == 1 && this.tokenTail == 0) {
                    return;
                }
                // after the > sign, or the 1st attribute start
                while (ts.moveNext()) {
                    t = ts.token();
                    switch (t.id()) {
                        case WS:
                            nextCaretPos = ts.offset() + 1;
                            break;

                        case TAG:
                            if (ts.offset() == startOffset) {
                                // completion at the <, it's insertion not a replacement.
                                break;
                            }
                            replaceExisting = true;
                            if (t.text().charAt(0) != '>' || nextCaretPos > -1) {
                                // do not set caret pos, caret will be right after insertion
                                return;
                            }
                            if (type == Type.PROPERTY_ELEMENT) {
                                // properties do not have attributes, position after
                                // the closing >
                                nextCaretPos = ts.offset() + 1;
                                return;
                            } else {
                                nextCaretPos = ts.offset();
                            }
                            return;

                        case ARGUMENT:
                            replaceExisting = true;
                            return;

                        default:
                            return;
                    }
                }
        }
    }

    public int getNextCaretPos() {
        return nextCaretPos;
    }
    
    /**
     * Caret offset after completion into the original text. -1, 
     * if the caret should be positioned elsewhere than right after the inserted
     * text.
     */
    private int nextCaretPos = -1;

    public List<? extends FxNode> getParents() {
        return Collections.unmodifiableList(parents);
    }
    
    /**
     * Node, which will host inserted elements. Because of 'default properties', 
     * content inserted into a PropertySetter may become actually a peer of the setter.
     * 
     * @return FxNode which will act as a parent for inserted elements.
     */
    public FxNode getElementParent() {
        return elementParent;
    }
    
    /**
     * Provides enclosing property. If the caret is not positioned within
     * @return 
     */
    public FxProperty getEnclosingProperty() {
        if (parents.isEmpty()) {
            return null;
        }
        FxNode parent = parents.get(0);
        if (parent instanceof PropertySetter) {
            return ((PropertySetter)parent).getPropertyInfo();
        } else if (parent.getKind() == FxNode.Kind.Instance) {
            FxInstance inst = (FxInstance)parent;
            FxBean bean = inst.getDefinition();
            // instance with a default property, caret is positioned WITHIN the content = in the default property.
            // This specifically activates in empty content, which is reported as ignorable whitespace, but can hold property value.
            if (bean != null && bean.getDefaultProperty() != null) {
                String pn = bean.getDefaultProperty().getName();
                if (inst.getProperty(pn) != null) {
                    // there's already defined a default property in the tree, should have been in the parent list, or we are outside of it.
                    return null;
                }
                if (fxmlParserResult.getTreeUtilities().positions(inst).contentContains(caretOffset, true)) {
                    return bean.getDefaultProperty();
                }
            }
        }
        return null;
    }
    
    public FxInstance getInstanceElement() {
        return instanceElement;
    }
    
    /**
     * Initialized by processPath
     */
    private FxInstance instanceElement;
    
    /**
     * Processes path obtained from the parser, attempts to find context and initializes
     * parents, elementParent and instanceElement properties. Must be called after processType.
     */
    private void processPath() {
        // in tag completion (resolved already by processType), ignores the currently opened tag as a parent,
        // as it is THAT tag, which is going to be replaced or changed.
        parents = fxmlParserResult.getTreeUtilities().findEnclosingElements(
                getCaretOffset(), isTag(), true);
        if (parents.isEmpty()) {
            return;
        }
        int index = 1;
        
        FxNode parent = parents.get(0);
        // go up from attribute, the parent must be an element; the attribute cannot accept elements, but its parent can
        if (fxmlParserResult.getTreeUtilities().isAttribute(parent)) {
            if (parents.size() > index) {
                parent = parents.get(index++);
            }
        }
        
        if (parent instanceof PropertySetter) {
            PropertySetter ps = (PropertySetter)parent;
            // if default property = content of the instance element, another
            // properties may be defined in the same content.
            if (ps.isImplicit()) {
                // the caret is inside some char content. It's legal to suggest 
                // a property element here
                if (parents.size() > index) {
                    FxNode superParent = parents.get(index++);
                    if (superParent.getKind() != FxNode.Kind.Instance) {
                        throw new IllegalStateException();
                    }
                    parent = (FxInstance)superParent;
                }
            }
        } 
        this.elementParent = parent;
        if (parent != null && parent.getKind() == FxNode.Kind.Instance) {
            this.instanceElement = (FxInstance)parent;
        }
    }
 
    /**
     * Returns the text/x-java compilation info.
     * The CompilationInfo can be used to execute java queries.
     * 
     * @return CompilationInfo instance
     */
    @NonNull
    public CompilationInfo getCompilationInfo() {
        return compilationInfo;
    }
    
    /**
     * Returns the classpath info for the project.
     * 
     * @return classpath info
     */
    @NonNull
    public ClasspathInfo getClasspathInfo() {
        return cpInfo;
    }

    void setClasspathInfo(ClasspathInfo cpInfo) {
        this.cpInfo = cpInfo;
    }
    
    /**
     * Provides completion type, see {@link Type}.
     * @return completion type
     */
    public Type getType() {
        return type;
    }

    /**
     * Offset of the caret in the editor
     * @return caret offset
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    /**
     * The prefix the user has typed. The prefix can be used to narrow searches
     * for classes and/or fields. The prefix contains the text from the start of the
     * token till the caret offset. Note: the prefix starts at the TOKEN start, so for
     * e.g. BEAN type, the prefix ALSO contains "<".
     * @return 
     */
    public String getPrefix() {
        return prefix;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getTokenTail() {
        return tokenTail;
    }

    public String getPiTarget() {
        return piTarget;
    }

    public String getPiData() {
        return piData;
    }

    public String getTagName() {
        return tagName;
    }

    public int getTagStartOffset() {
        return tagStartOffset;
    }
    
    public int getEndOffset() {
        return getCaretOffset() + getTokenTail();
    }
    
    public int getReplaceLength() {
        return getEndOffset() - getStartOffset();
    }
    
    /** 
     * End offset of the tag with caret
     */
    private int tagEndOffset = -1;
    
    /**
     * True, if the tag is finished (note: this does not mean the corresponding close
     * tag is present ! just that the tag property finishes with >)
     */
    private boolean finished;
    
    /**
     * True, if the tag is self-closed
     */
    private boolean selfClosed;

    public Document getDoc() {
        return doc;
    }

    public int getTagEndOffset() {
        return tagEndOffset;
    }

    public boolean isTagFinished() {
        return finished;
    }

    public boolean isSelfClosed() {
        return selfClosed;
    }
    
    private void markUnclosed(int offendingContent) {
        this.finished = false;
        this.selfClosed = false;
        this.tagEndOffset = offendingContent;
    }
    
    public boolean isProcessingInstruction() {
        switch (type) {
            case INSTRUCTION_DATA:
            case INSTRUCTION_TARGET:
//            case ROOT:
                return true;
        }
        return false;
    }
    
    public boolean isAttribute() {
        return attribute;
    }
    
    /**
     * Determines if the thing being completed can be a tag name
     * @return true, if tag name is being completed
     */
    public boolean isTag() {
        switch (type) {
            case BEAN:
            case CHILD_ELEMENT:
            case PROPERTY_ELEMENT:
            case ROOT:
                return true;
        }
        return false;
    }

    /**
     * Scans the processing instruction, reads target and data if it present
     * 
     * @param seq 
     */
    private void readPIContent(TokenSequence<XMLTokenId> seq) {
        boolean cont = true;
        Token<XMLTokenId> t;
        
        while (cont && seq.moveNext()) {
            t = seq.token();
            XMLTokenId id = t.id();
            switch (id) {
                case TAG:
                    markUnclosed(seq.offset());
                    return;

                // OK for tag content, not OK for PI
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                case CHARACTER:
                case BLOCK_COMMENT:
                case CDATA_SECTION:
                case DECLARATION:
                case TEXT:
                case PI_START:
                case ERROR:
                    markUnclosed(seq.offset());
                    return;

                // not OK for tag
                case PI_TARGET:
                    this.piTarget = t.text().toString();
                    break;
                    
                case PI_CONTENT:
                    this.piData = t.text().toString();
                    break;

                case PI_END:
                    selfClosed = true;
                    finished = true;
                    tagEndOffset = seq.offset() + t.length();
                    return;
                    
                // this is OK for all
                case WS:
                    break;
                default:
                    markUnclosed(seq.offset());
                    return;
            }
        }
    }
    
    public boolean isRootElement() {
        return currentIsRoot;
    }

    public int getRootAttrInsertOffset() {
        return rootAttrInsertOffset;
    }
    
    /**
     * Finds a prefix for the nsURi
     * @return prefix for the URI or {@code null} if URI was not declared
     */
    public String findNsPrefix(String nsUri) {
        return rootNamespacePrefixes.get(nsUri);
    }
    
    /**
     * Finds namespace prefix, that corresponds to the FX namespace (the namespace where
     * fx: special attributes, like fx:id or fx:controller reside). It is assumed that the
     * text does not contain more than one such prefix. If it contains more of them, the one
     * with the highest number after / is returned. 
     * 
     * @return namespace prefix or {@code null} if no such prefix is found.1
     */
    public String findFxmlNsPrefix() {
        final String fxPrefix = JavaFXEditorUtils.FXML_FX_NAMESPACE;
        String candidate = null;
        int version = -1;
        for (Map.Entry<String, String> nsE : rootNamespacePrefixes.entrySet()) {
            String p = nsE.getKey();
            if (p.startsWith(fxPrefix)) {
                final int thisVersion;
                if (p.length() > fxPrefix.length()) {
                    if (p.charAt(fxPrefix.length()) != '/') {
                        continue;
                    }
                    try {
                        thisVersion = Integer.parseInt(p.substring(fxPrefix.length() + 1));
                    } catch (NumberFormatException ex) {
                        // expected, error in the ns prefix, ignore
                        continue;
                    }
                } else {
                    thisVersion = 0;
                }
                
                if (version < thisVersion) {
                    candidate = nsE.getValue();
                }
            }
        }
        return version == -1 ? null : candidate;
    }
    
    /**
     * Finds a suitable non conflicting prefix string for the namespace URI.
     * If the URI is already declared, returns the existing prefix. It will return
     * either the value of 'suggested' parameter, or 'suggested' with some unique
     * suffix or an existing prefix for the URI
     * 
     * @param nsURI namespace URI
     * @param suggested suggested prefix
     * @return suitable prefix
     */
    public String findPrefixString(@NonNull String nsURI, @NonNull String suggested) {
        String existing = findNsPrefix(nsURI);
        if (existing != null) {
            return existing;
        }
        boolean repeat = false;
        int counter = 1;
        String pref = suggested;
        
        while (repeat) {
            repeat = false;
            for (String s : rootNamespacePrefixes.values()) {
                if (s.equals(pref)) {
                    pref = suggested + counter;
                    counter++;
                    repeat = true;
                    break;
                }
            }
        }
        
        return pref;
    }
    
    private Map<String, String> rootNamespacePrefixes = new HashMap<String, String>();
    
    private static final String NAMESPACE_PREFIX = "xmlns:"; // NOI18N
    
    private void processNamespaces() {
        for (String s : rootAttributes.keySet()) {
            if (s.startsWith(NAMESPACE_PREFIX)) {
                String nsPrefix = s.substring(NAMESPACE_PREFIX.length());
                String uri = rootAttributes.get(s).valueContent;
                
                rootNamespacePrefixes.put(uri, nsPrefix);
            }
        }
    }
    
    private void copyToRoot() {
        rootTagStartOffset = tagStartOffset;
        rootAttributes = attributes;
        
        tagStartOffset = -1;
        attributes = Collections.emptyMap();
        finished = false;
        selfClosed = false;
    }
    
    /**
     * Skips to and reads the root Element. Reads root element's attributes,
     * so we can detect whether required namespace(s) are present.
     * 
     * This MUST be called prior to readCurrentContent().
     * @param s 
     */
    private void readRootElement(TokenSequence<XMLTokenId> seq) {
        seq.move(0);
        while (seq.moveNext()) {
            Token<XMLTokenId> t = seq.token();
            XMLTokenId id = t.id();
            if (id == XMLTokenId.TAG && t.length() > 1) {
                int startOffset = seq.offset();
                readTagContent(seq);
                // reassign stuff:
                copyToRoot();
                rootTagStartOffset = startOffset;
                rootAttrInsertOffset = startOffset + t.length();
                if (t.text().charAt(t.length() - 1) == '>') {
                    rootAttrInsertOffset--;
                    if (t.length() > 2 && t.text().charAt(t.length() - 2) == '/') {
                        rootAttrInsertOffset--;
                    }
                }
                findRootInsertionPoint();
                return;
            }
        }
    }
    
    private void findRootInsertionPoint() {
        if (!rootAttributes.isEmpty()) {
            int min = Integer.MAX_VALUE;

            for (ArgumentInfo ai : rootAttributes.values()) {
                min = Math.min(min, ai.argStart);
            }
            rootAttrInsertOffset = min;
        }
    }
    
    static class ArgumentInfo {
        private int argStart;
        private int valueStart;
        private int valueEnd;
        private String valueContent;

        public ArgumentInfo(int argStart, int valueStart, int valueEnd, String valContent) {
            this.argStart = argStart;
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.valueContent = valContent;
        }
    }
    
    private static final ArgumentInfo NO_ARGUMENT = new ArgumentInfo(-1, -1, -1, null);
    
    private Map<String, ArgumentInfo> attributes = Collections.emptyMap();
    private Map<String, ArgumentInfo> rootAttributes = Collections.emptyMap();
    
    ArgumentInfo attr(String aName) {
        ArgumentInfo ai = attributes.get(aName);
        return ai != null ? ai : NO_ARGUMENT;
    }
    
    public Enumeration<String> attributeNames() {
        return Collections.enumeration(attributes.keySet());
    }
    
    public String value(String arg) {
        return attr(arg).valueContent;
    }
    
    public String createNSName(String prefix, String name) {
        if (prefix == null) {
            return name;
        } else {
            return prefix + ":" + name; // NOI18N
        }
    }
    
    public String fxAttributeName(String name) {
        String prefix = findFxmlNsPrefix();
        if (prefix == null) {
            return null;
        }
        return prefix + ":" + name; // NOI18N
    }
    
    public String attributeNSName(String prefix, String name) {
        if (prefix == null) {
            return name;
        }
        if (!rootNamespacePrefixes.containsValue(prefix)) {
            return null;
        }
        return prefix + ":" + name; // NOI18N
    }
    
    public String attributeName(String nsURI, String name) {
        if (nsURI == null) {
            return name;
        }
        String prefix = findNsPrefix(nsURI);
        if (prefix == null) {
            return null;
        }
        return prefix + ":" + name; // NOI18N
    }
    
    public boolean contains(String a) {
        return attr(a).argStart != -1;
    }
    
    public int attrStart(String a) {
        return attr(a).argStart;
    }
    
    public int valueStart(String a) {
        return attr(a).valueStart;
    }
    
    public int valueEnd(String a) {
        return attr(a).valueEnd;
    }
    
    private void readCurrentContent(TokenSequence<XMLTokenId> seq) {
        if (tagStartOffset == -1) {
            return;
        }
        int diff = seq.move(tagStartOffset);
        if (diff > 0) {
            throw new IllegalStateException();
        }
        
        if (!seq.moveNext()) {
            return;
        }
        Token<XMLTokenId> t = seq.token();
                
        if (t.id() == XMLTokenId.TAG) {
            // the tag can be self-closed, without any arguments:
            if (t.text().toString().endsWith("/>")) {
                finished = true;
                tagEndOffset = seq.offset() + t.length();
                selfClosed = true;
                return;
            }
            if (rootTagStartOffset == seq.offset()) {
                currentIsRoot = true;
            }
            readTagContent(seq);
        } else if (t.id() == XMLTokenId.PI_START) {
            readPIContent(seq);
        }
        
    }
    
    private CharSequence stripQuotes(CharSequence s) {
        char q = 0;
        char c;
        
        if (s == null || s.length() == 0) {
            return s;
        }
        c = s.charAt(0);
        int start = 0;
        int end = s.length();
        if (c == '\'' || c == '\"') {
            start++;
            q = c;
        }
        if (end == start - 1) {
            return s.subSequence(start, end);
        }
        c = s.charAt(end - 1);
        if (c == q) {
            return s.subSequence(start, end - 1);
        }
        while (end > start && Character.isWhitespace(c)) {
            end--;
            c = s.charAt(end);
        }
        return s.subSequence(start, end + 1);
    }
    
    /**
     * Reads the TokenSequence forward, and reach the end of the tag, or 
     * the processing instruction.
     * If the tag is not terminated, ie some TEXT will appear, or another tag token,
     * the search terminates, and the tag is recorded as unterminated
     */
    @SuppressWarnings("fallthrough")
    private void readTagContent(TokenSequence<XMLTokenId> seq) {
        attributes = new HashMap<String, ArgumentInfo>();
        String argName = null;
        int argStart = -1;
        Token<XMLTokenId> t;
        while (seq.moveNext()) {
            t = seq.token();
            XMLTokenId id = t.id();
            switch (id) {
                case TAG:
                    CharSequence s = t.text();
                    if (s.charAt(0) == '<') {
                        // unterminated tag
                        markUnclosed(seq.offset());
                        return;
                    } else if (s.charAt(s.length() - 1) == '>') {
                        // end tag marker
                        finished = true;
                        tagEndOffset = seq.offset() + s.length();
                        selfClosed = s.length() >= 2 && s.charAt(s.length() - 2) == '/';
                        return;
                    }

                // OK for tag content, not OK for PI
                case ARGUMENT:
                    argName = t.text().toString();
                    argStart = seq.offset();
                    break;
                case VALUE:
                    if (argName != null) {
                        int len = t.length();
                        CharSequence val = t.text();
                        StringBuilder compound = null;
                        while (seq.moveNext()) {
                            Token<XMLTokenId> nt = seq.token();
                            if (nt.id() != XMLTokenId.CHARACTER &&
                                nt.id() != XMLTokenId.VALUE) {
                                seq.movePrevious();
                                break;
                            }
                            if (compound == null) {
                                compound = new StringBuilder();
                                compound.append(val);
                            }
                            compound.append(nt.text());
                            len += nt.length();
                        }
                        attributes.put(argName, new ArgumentInfo(argStart, 
                                seq.offset(), seq.offset() + len, 
                                stripQuotes(
                                    compound == null ?
                                        t.text() :
                                        compound
                                ).toString()));
                    }
                    break;
                case OPERATOR:
                    break;

                // these are neither OK for tag or PI
                case CHARACTER:
                case BLOCK_COMMENT:
                case CDATA_SECTION:
                case DECLARATION:
                case TEXT:
                case ERROR:
                    markUnclosed(seq.offset());
                    return;
 
                // not OK for tag
                case PI_TARGET:
                case PI_CONTENT:
                case PI_END:
                    markUnclosed(seq.offset());
                    return;
                    
                // this is OK for all
                case WS:
                    break;
                    
                case PI_START:
            }
        }
    }

    private void processValueType() {
        switch (type) {
            case PROPERTY:
            case PROPERTY_VALUE:
                attribute = true;
        }
        if (type != Type.PROPERTY_VALUE && type != Type.PROPERTY_VALUE_CONTENT) {
            return;
        }
        
        if (!prefix.isEmpty()) {
            char c = prefix.charAt(0);
            if (c == '\'' || c == '"') {
                prefix = prefix.substring(1);
            }
        }
        
        if (prefix.startsWith("@")) {
            type = Type.RESOURCE_REF;
        } else if (prefix.startsWith("%")) {
            type = Type.BUNDLE_REF;
        } else if (prefix.startsWith("${")) {
            type = Type.BINDING;
        } else if (prefix.startsWith("$")) {
            type = Type.VARIABLE;
        } else if (prefix.startsWith("#")) {
            type = Type.HANDLER;
        }
    }
    
    private boolean isTextContent(XMLTokenId id) {
        return id == XMLTokenId.TEXT || id == XMLTokenId.CDATA_SECTION || id == XMLTokenId.CHARACTER;
    }
    
    private void setTextContentBoundaries(TokenSequence<XMLTokenId> ts) {
        while (ts.movePrevious()) {
            Token<XMLTokenId> t = ts.token();
            
            XMLTokenId id = t.id();
            if (isTextContent(id)) {
                break;
            }
        }
        int start = ts.offset() + ts.token().length();
        
        while (ts.moveNext()) {
            Token<XMLTokenId> t = ts.token();
            
            XMLTokenId id = t.id();
            if (isTextContent(id)) {
                break;
            }
        }
        int end = ts.offset();
        
        this.startOffset = start;
        this.tokenTail = end - caretOffset;
    }
    
    /**
     * Name of the property, if property name or value is being completed. Does
     * NOT work for Type.CHILD_ELEMENT.
     */
    private String propertyName;
    
    public String getPropertyName() {
        if (type == Type.PROPERTY_VALUE) {
            return propertyName;
        } else if (type == Type.PROPERTY) {
            return tagName;
        } else if (type == Type.PROPERTY_ELEMENT) {
            return tagName;
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("fallthrough")
    private void processType(TokenSequence<XMLTokenId> ts) {
        int diff = ts.move(caretOffset);
        boolean hasToken;
        boolean middle = diff > 0;

        if (middle) {
            // we are in the middle of some token, scan that token first
            hasToken = ts.moveNext();
        } else {
            hasToken = ts.movePrevious();
        }

        boolean wsFound = false;
        Token<XMLTokenId> t = null;
        boolean dontAdvance = false;

        while (type == null && hasToken) {
            t = ts.token();
            if (middle) {
                tokenTail = t.length() - diff;
            }
            XMLTokenId id = t.id();

            switch (id) {
                case PI_END:
                    // was after PI, PI or Bean is allowed; no completion 
                    type = Type.INSTRUCTION_END;
                    break;

                case PI_CONTENT: {
                    // do not count whitespace after caret into the replacement length
                    String tail = t.text().subSequence(diff, diff + tokenTail).toString();
                    String trimmed = tail.trim();
                    if (trimmed.isEmpty()) {
                        tokenTail = 0;
                    } else {
                        tokenTail = tail.indexOf(trimmed) + trimmed.length();
                    }
                    type = Type.INSTRUCTION_DATA;
                    break;
                }

                case PI_TARGET:
                    type = Type.INSTRUCTION_TARGET;
                    piTarget = t.text().toString();
                    tagStartOffset = ts.offset();
                    dontAdvance = caretOffset == ts.offset() + t.length();
                    break;

                case PI_START:
                    type = Type.INSTRUCTION_TARGET;
                    dontAdvance = true;
                    break;
                    
                case VALUE:
                    type = Type.PROPERTY_VALUE;
                    break;

                case OPERATOR:
                    type = Type.PROPERTY_VALUE;
                    if (startOffset == -1) {
                        startOffset = caretOffset;
                    }
                    prefix = ""; // NOI18N
                    dontAdvance = true;
                    break;

                case CHARACTER:
                case CDATA_SECTION:
                case TEXT:
                    // text in between tags; end tag should be suggested by XML completion itself, 
                    // check if all the text is whitespace and if not, do not suggest anything,
                    // as the content is likely a value
                    String nonWh = t.text().toString().trim();
                    if (!nonWh.isEmpty()) {
                        if (nonWh.startsWith("<")) {
                            // correct start & end offset:
                            int nonWhPos = t.text().toString().indexOf(nonWh);
                            if (startOffset == -1) {
                                startOffset = ts.offset() + nonWhPos;
                            }
                            if (caretOffset > startOffset + nonWh.length()) {
                                type = Type.UNKNOWN;
                            } else {
                                tokenTail = nonWh.length() - (caretOffset - startOffset);
                                type = Type.CHILD_ELEMENT;
                            }
                            tagName = t.text().subSequence(nonWhPos + 1, nonWh.length()).toString();
                            tagStartOffset = ts.offset();
                            break;
                        }

                        if (rootTagStartOffset == -1 || rootTagStartOffset <= startOffset) {
                            type = Type.ROOT;
                        } else {
                            // some content; assume it is a property value
                            type = Type.PROPERTY_VALUE;
                        }
                        
                        // traverse back to the 1st nonWhite character, record start position and length of the token
                        setTextContentBoundaries(ts);
                        break;
                    }
                    // fall through to whitespace
                case ERROR:
                case WS:
                    wsFound = true;
                    middle = false;
                    tokenTail = 0;
                    // replacement will start at the caret offset, whitespaces preserved
                    startOffset = caretOffset;
                    prefix = ""; // NOI18N
                    break;
                    
                case TAG:
                    CharSequence s = t.text();
                    if (s.length() == 1 && s.charAt(0) == '>') {
                        // after the ending > of a tag
                        type = Type.CHILD_ELEMENT;
                        if (startOffset == -1) {
                            startOffset = ts.offset() + 1;
                            prefix = "";
                        }
                        break;
                    }
                    if (s.length() > 1 && s.charAt(1) == '/') {
                        // end tag, no completion:
                        type = Type.UNKNOWN;
                        break;
                    }
                    s = s.subSequence(1, s.length());
                    CharSequence s2 = getUnprefixed(s);
                    if (s.length() != s2.length()) {
                        // all prefixed stuff are beans, most probably
                        type = Type.CHILD_ELEMENT;
                    } else {
                        // check if there's a prefix; if so, return the unprefixed tagname.
                        if (wsFound) {
                            type = Type.PROPERTY;
                        } else if (s2.length() == 0) {
                            type = Type.CHILD_ELEMENT;
                        } else if (isClassTagName(s2)) {
                            type = Type.BEAN;
                        } else {
                            type = Type.PROPERTY_ELEMENT;
                        }
                    }
                    
                    int l = s2.length();
                    if (s2.length() > 1 && s2.charAt(l - 2) == '/') {
                        l--;
                    }
                    tagName = s2.subSequence(0, l).toString();
                    tagStartOffset = ts.offset();
                    dontAdvance = caretOffset == tagStartOffset + t.length();
                    break;

                case ARGUMENT:
                    type = Type.PROPERTY;
                    dontAdvance = caretOffset == ts.offset() + t.length();
            }
            if (type == null) {
                hasToken = ts.movePrevious();
                middle = false;
            }
        }

        // compute prefix and replacement offset, if was not set from the iteration
        if (!wsFound && prefix == null) {
            if (t == null) {
                prefix = "";
            } else if (diff > 0) {
                prefix = t.text().subSequence(0, diff).toString();
            } else if (ts.offset() < caretOffset) {
                // assume preceding token
                
                prefix = t.text().toString();
            }
        }
        if (startOffset == -1) {
            if (hasToken) {
                startOffset = ts.offset();
            } else {
                startOffset = caretOffset;
            }
        }
        
        // advance to the next state, as caret is positioned AFTER the token
        if (!dontAdvance && (wsFound || !middle) && type != null) {
            // in between tokens, so shift the type
            Type oldType = this.type;
            switch (oldType) {
                case INSTRUCTION_TARGET: type = Type.INSTRUCTION_DATA; break;
                    
                case PROPERTY_VALUE: type = Type.PROPERTY; break;

                case BEAN:
                case PROPERTY_ELEMENT: type = Type.PROPERTY; break;
                        
                case INSTRUCTION_END: type = Type.ROOT; break;
                    
            }
            if (oldType != type) {
                prefix = "";
                tokenTail = 0;
                startOffset = caretOffset;
            }
        }
        
        // traverse back to reach the opening tag or processing instruction:
        boolean cont = tagStartOffset == -1;
        while (ts.movePrevious() && cont) {
            t = ts.token();
            XMLTokenId id = t.id();
            switch (id) {
                case TAG: {
                    CharSequence s = t.text();
                    if (s.length() == 1 && s.charAt(0) == '>') {
                        // closing >, go on
                        break;
                    }
                    int start = 0;
                    int end = s.length();
                    if (s.charAt(0) == '<') {
                        start++;
                        if (s.length() > 1 && s.charAt(1) == '/') {
                            start++;
                        }
                    }
                    if (s.charAt(s.length() - 1) == '>') {
                        end--;
                    }
                    tagName = s.subSequence(start, end).toString();
                    tagStartOffset = ts.offset();
                    cont = false;
                    break;
                }
                
                case PI_END:
                    cont = false;
                    break;
                    
                case PI_START:
                    tagStartOffset = ts.offset();
                    cont = false;
                    break;
                            
                case TEXT:
                case BLOCK_COMMENT:
                    break;

                case ARGUMENT:
                    if (type == Type.PROPERTY_VALUE && propertyName == null) {
                        this.propertyName = t.text().toString();
                    }
                    break;
                case PI_TARGET:
                    piTarget = t.text().toString();
                    // fall through
                case PI_CONTENT:
                case OPERATOR:
                case VALUE:
                case WS:
                    break;
            }
        }
        
        if (cont && type == null) {
            type = Type.ROOT;
        }
        
        // root tag cannot be placed between processing instructions, iterate forward
        if (type == Type.ROOT) {
            ts.move(caretOffset);
            
            cont = true;
            while (cont && ts.moveNext()) {
                t = ts.token();
                XMLTokenId id = t.id();
                switch (id) {
                    case BLOCK_COMMENT:
                        // this is ok
                        break;
                        
                    case TEXT:
                    case CDATA_SECTION:
                        // this is maybe also OK
                        break;
                        
                    case PI_START:
                        // must not present tag for completion
                        type = Type.INSTRUCTION_TARGET;
                        cont = false;
                        break;
                    case TAG:
                        cont = false;
                        break;
                }
            }
        }
    }
    
    private static boolean isClassTagName(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '.') {
                if (i < s.length() - 1) {
                    char c = s.charAt(i + 1);
                    return Character.isUpperCase(c);
                }
            }
        }
        return Character.isUpperCase(s.charAt(0));
    }
    
    public boolean isBlackListed(Element elem) {
        if (getCompilationInfo().getElements().isDeprecated(elem)) {
            return true;
        }
        if (typeWhiteList == null) {
            return false;
        }
        Result r = typeWhiteList.check(ElementHandle.create(elem), WhiteListQuery.Operation.USAGE);
        return r != null && !r.isAllowed();
    }
    
    private static CharSequence getUnprefixed(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ':') {
                return s.subSequence(i + 1, s.length());
            }
        }
        return s;
    }
}
