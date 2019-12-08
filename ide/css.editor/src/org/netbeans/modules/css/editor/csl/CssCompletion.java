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
package org.netbeans.modules.css.editor.csl;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import javax.swing.ImageIcon;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementHandle.UrlHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.CssProjectSupport;
import org.netbeans.modules.css.editor.HtmlTags;
import org.netbeans.modules.css.editor.URLRetriever;
import org.netbeans.modules.css.editor.module.CssModuleSupport;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssCompletionItem;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.CssTokenIdCategory;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.netbeans.modules.css.lib.api.properties.ResolvedProperty;
import org.netbeans.modules.css.lib.api.properties.UnitGrammarElement;
import org.netbeans.modules.css.lib.api.properties.ValueGrammarElement;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.ui.api.FileReferenceCompletion;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssCompletion implements CodeCompletionHandler {

    private static final Collection<String> AT_RULES = Arrays.asList(new String[]{"@media", "@page", "@import", "@charset", "@font-face", "@supports"}); //NOI18N
    private static char firstPrefixChar; //read getPrefix() comment!
    private static final String EMPTY_STRING = ""; //NOI18N
    private static final String UNIVERSAL_SELECTOR = "*"; //NOI18N

    /**
     * Units which shouldn't appear in the code completion.
     */
    private static final Collection<String> HIDDEN_UNITS = new HashSet<>(Arrays.asList(new String[]{"!hash_color_code"}));

    //unit testing support
    static String[] TEST_USED_COLORS;
    static String[] TEST_CLASSES;
    static String[] TEST_IDS;
    public static String testFileObjectMimetype;

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {

        final List<CompletionProposal> completionProposals = new ArrayList<>();

        CssParserResult info = (CssParserResult) context.getParserResult();
        Snapshot snapshot = info.getSnapshot();
        FileObject file = snapshot.getSource().getFileObject();

        int caretOffset = context.getCaretOffset();
        String prefix = context.getPrefix() != null ? context.getPrefix() : "";

        //read getPrefix() comment!
        if (firstPrefixChar != 0) {
            prefix = firstPrefixChar + prefix;
        }

        TokenHierarchy<?> th = snapshot.getTokenHierarchy();
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());

        assert ts != null;

        //handle lexical completion only
        CodeCompletionResult lexicalCompletionResult = handleLexicalBasedCompletion(file, ts, snapshot, caretOffset);
        if (lexicalCompletionResult != null) {
            return lexicalCompletionResult;
        }

        //continue with AST completion
        int offset = caretOffset - prefix.length();
        int astOffset = snapshot.getEmbeddedOffset(offset);
        int astCaretOffset = snapshot.getEmbeddedOffset(caretOffset);

        boolean unmappableClassOrId = false;
        if (astOffset == -1) {
            if ((prefix.length() == 1 && prefix.charAt(0) == '.') || (prefix.length() > 0 && prefix.charAt(0) == '#')) {
                //this happens if completion is invoked in empty css embedding,
                //for example in <div class="|"/>. The virtual source contains doesn't
                //map the position do the document, se we need to hack it
                unmappableClassOrId = true;
            } else {
                //cannot map the offset
                return null;
            }
        }

        int diff = ts.move(astCaretOffset);
        boolean tokenFound;
        if (diff == 0) {
            if (ts.movePrevious()) {
                tokenFound = true;
            } else {
                //no token, try next
                tokenFound = ts.moveNext();
            }
        } else {
            if (ts.moveNext()) {
                tokenFound = true;
            } else {
                //no token, try next
                tokenFound = ts.movePrevious();
            }
        }

        Node root = info.getParseTree();
        if (root == null) {
            //broken source
            return CodeCompletionResult.NONE;
        }

        char charAfterCaret = snapshot.getText().length() > (astCaretOffset + 1)
                ? snapshot.getText().subSequence(astCaretOffset, astCaretOffset + 1).charAt(0)
                : ' '; //NOI18N

        //if the caret points to a token node then determine its type
        Node tokenNode = NodeUtil.findNodeAtOffset(root, astCaretOffset);
        CssTokenId tokenNodeTokenId = null;
        if (tokenNode != null && tokenNode.type() == NodeType.token) {
            tokenNodeTokenId = NodeUtil.getTokenNodeTokenId(tokenNode);
        }
        
        Node node = NodeUtil.findNonTokenNodeAtOffset(root, astCaretOffset);
        if(node == null) {
            return CodeCompletionResult.NONE; //can happen if the parsed source is too big to parse -> see the CssParser parsing limit
        }
        
        if (node.type() == NodeType.ws) {
            node = node.parent();
        }
//        if (node.type() == NodeType.error) {
//            node = node.parent();
//            if (node == null) {
//                return CodeCompletionResult.NONE;
//            }
//        }

        //xxx: handleLexicalBasedCompletion breaks the contract - in the case it is used the css modules are
        //     not asked for the completion results. The main reason is that the CompletionProposal doesn't 
        //     allow to move the caret somewhere when the completion item is completed. This functionality
        //     is achievable onto the CompletionResult. However this means it is not completion item specific
        //     and as such cannot vary from different items from various css modules.
        //css modules
        CompletionContext completionContext
                = new CompletionContext(node,
                tokenNode,
                info,
                ts,
                ts.index(),
                diff,
                context.getQueryType(),
                caretOffset,
                offset,
                astCaretOffset,
                astOffset,
                prefix,
                file != null ? file.getMIMEType() : testFileObjectMimetype);

        List<CompletionProposal> cssModulesCompletionProposals = CssModuleSupport.getCompletionProposals(completionContext);
        completionProposals.addAll(cssModulesCompletionProposals);

        //non-modules based (legacy) completion - to be refactored to modules later
        completeClassSelectors(completionContext, completionProposals, unmappableClassOrId);
        completeIdSelectors(completionContext, completionProposals, unmappableClassOrId);
        completeAtRulesAndHtmlSelectors(completionContext, completionProposals);
        completeHtmlSelectors(completionContext, completionProposals, tokenNodeTokenId);
        completeKeywords(completionContext, completionProposals, tokenFound);
        completePropertyName(completionContext, completionProposals);
        completePropertyValue(completionContext, completionProposals, charAfterCaret);

        return new DefaultCompletionResult(completionProposals, false);
    }

    private List<? extends CompletionProposal> completeImport(FileObject base, int offset, String prefix, boolean addQuotes, boolean addSemicolon) {
        FileReferenceCompletion<CssCompletionItem> fileCompletion = new CssLinkCompletion(base, addQuotes, addSemicolon);
        return fileCompletion.getItems(base, offset - prefix.length(), prefix);
    }

    private List<CompletionProposal> completeHtmlSelectors(CompletionContext context, String prefix, int offset) {
        List<CompletionProposal> proposals = new ArrayList<>(20);
        Collection<String> items = new ArrayList<>(Arrays.asList(HtmlTags.getTags()));
        items.add(UNIVERSAL_SELECTOR);
        for (String tagName : items) {
            if (tagName.startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
                proposals.add(CssCompletionItem.createSelectorCompletionItem(new CssElement(context.getSource().getFileObject(), tagName),
                        tagName,
                        offset,
                        true));
            }
        }
        return proposals;
    }

    private Collection<CompletionProposal> wrapPropertyValues(CompletionContext context,
            String prefix,
            PropertyDefinition propertyDescriptor,
            Collection<ValueGrammarElement> props,
            int anchor,
            boolean addSemicolon,
            boolean addSpaceBeforeItem,
            boolean extendedItemsOnly) {

        //there might be more grammar elements from multiple branches with the same name
        Map<String, Collection<ValueGrammarElement>> value2GrammarElement
                = new HashMap<>();
        for (ValueGrammarElement element : props) {
            String elementValue = element.getValue().toString();
            Collection<ValueGrammarElement> col = value2GrammarElement.get(elementValue);
            if (col == null) {
                col = new LinkedList<>();
                value2GrammarElement.put(elementValue, col);
            }
            col.add(element);
        }

        List<CompletionProposal> proposals = new ArrayList<>(props.size());
        boolean colorChooserAdded = false;

        for (Map.Entry<String, Collection<ValueGrammarElement>> entry : value2GrammarElement.entrySet()) {

            String elementValue = entry.getKey();
            Collection<ValueGrammarElement> elements = entry.getValue();
            ValueGrammarElement element = elements.iterator().next();
            CssValueElement handle = new CssValueElement(propertyDescriptor, element);
            String origin = element.origin();
            String visibleOrigin = element.getVisibleOrigin();

            if (element instanceof UnitGrammarElement) {
                UnitGrammarElement unit = (UnitGrammarElement) element;
                String unitName = unit.getValue();
                if (!HIDDEN_UNITS.contains(unitName)) {
                    if (unit.getFixedValues() != null) {
                        for (String fixedValue : unit.getFixedValues()) {
                            proposals.add(
                                    CssCompletionItem.createValueCompletionItem(
                                    handle,
                                    fixedValue,
                                    visibleOrigin,
                                    anchor,
                                    addSemicolon,
                                    addSpaceBeforeItem));
                        }

                    } else {
                        proposals.add(CssCompletionItem.createUnitCompletionItem((UnitGrammarElement) element));
                    }
                }

                continue;
            }

            //some hardcoded items filtering
            //1. do not show NAMED operators in the code completion
            if (origin.endsWith("_operator")) { //NOI18N
                continue;
            }
            if ("@colors-list".equals(origin)) { //NOI18N
                if (!colorChooserAdded) {
                    //add color chooser item
                    proposals.add(CssCompletionItem.createColorChooserCompletionItem(anchor, visibleOrigin, addSemicolon));
                    //add used colors items
                    proposals.addAll(getUsedColorsItems(context, prefix, handle, visibleOrigin, anchor, addSemicolon, addSpaceBeforeItem));
                    colorChooserAdded = true;
                }
                if (!extendedItemsOnly) {
                    proposals.add(CssCompletionItem.createColorValueCompletionItem(handle, element, anchor, addSemicolon, addSpaceBeforeItem));
                }
            } else {
                if (!extendedItemsOnly) {
                    //for elements which are alternatives from multiple grammar braches do not
                    //show the origin

                    //check if the visible origin off all the elements is the same, 
                    //if so use it if not, do not show any origin
                    String vo = null;
                    boolean same = true;
                    for (ValueGrammarElement e : elements) {
                        if (vo == null) {
                            vo = e.getVisibleOrigin();
                        } else {
                            if (!vo.equals(e.getVisibleOrigin())) {
                                same = false;
                                break;
                            }
                        }
                    }

                    proposals.add(
                            CssCompletionItem.createValueCompletionItem(
                            handle,
                            element,
                            same ? visibleOrigin : "...",
                            anchor,
                            addSemicolon,
                            addSpaceBeforeItem));
                }
            }
        }
        return proposals;
    }

    private Collection<CompletionProposal> getUsedColorsItems(CompletionContext context, String prefix,
            CssElement element, String origin, int anchor, boolean addSemicolon,
            boolean addSpaceBeforeItem) {
        Collection<CompletionProposal> proposals = new HashSet<>();
        //unit testing - inject some testing used colors to the completion
        if (TEST_USED_COLORS != null) {
            for (String color : TEST_USED_COLORS) {
                proposals.add(CssCompletionItem.createHashColorCompletionItem(element, color, origin,
                        anchor, addSemicolon, addSpaceBeforeItem, true));
            }
        }
        //

        FileObject current = context.getParserResult().getSnapshot().getSource().getFileObject();
        if (current == null) {
            return proposals;
        }
        CssProjectSupport support = CssProjectSupport.findFor(current);
        if (support == null) {
            //we are outside of a project
            return proposals;
        }
        CssIndex index = support.getIndex();
        Map<FileObject, Collection<String>> result = index.findAll(RefactoringElementType.COLOR);

        //resort the files collection so the current file it first
        //we need that to ensure the color from current file has precedence
        //over the others
        List<FileObject> resortedKeys = new ArrayList<>(result.keySet());
        if (resortedKeys.remove(current)) {
            resortedKeys.add(0, current);
        }
        for (FileObject file : resortedKeys) {
            Collection<String> colors = result.get(file);
            boolean usedInCurrentFile = file.equals(current);
            for (String color : colors) {
                if (color.startsWith(prefix)) {
                    proposals.add(CssCompletionItem.createHashColorCompletionItem(element, color, origin,
                            anchor, addSemicolon, addSpaceBeforeItem, usedInCurrentFile));
                }
            }
        }

        return proposals;
    }

    private Collection<String> filterStrings(Collection<String> values, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(value);
            }
        }
        return filtered;
    }

    private Collection<ValueGrammarElement> filterElements(Collection<ValueGrammarElement> values, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<ValueGrammarElement> filtered = new ArrayList<>();
        for (ValueGrammarElement value : values) {
            if (value.toString().toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(value);
            }
        }
        return filtered;
    }

    private Collection<PropertyDefinition> filterProperties(Collection<PropertyDefinition> props, String propertyNamePrefix) {
        propertyNamePrefix = propertyNamePrefix.toLowerCase();
        List<PropertyDefinition> filtered = new ArrayList<>();
        for (PropertyDefinition p : props) {
            if (p.getName().toLowerCase().startsWith(propertyNamePrefix)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        FileObject fileObject = info.getSnapshot().getSource().getFileObject();
        HelpResolver resolver = CssModuleSupport.getHelpResolver();
        if (resolver != null) {
            if (element instanceof CssPropertyElement) {
                CssPropertyElement e = (CssPropertyElement) element;
                PropertyDefinition property = e.getPropertyDescriptor();
                return resolver.getHelp(fileObject, property);
            } else if (element instanceof ElementHandle.UrlHandle) {
                try {
                    return URLRetriever.getURLContentAndCache(new URL(element.getName()));
                } catch (MalformedURLException e) {
                    assert false;
                }
            }
        }
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle elementHandle) {
        if (elementHandle instanceof CssPropertyElement) {
            CssPropertyElement e = (CssPropertyElement) elementHandle;
            PropertyDefinition property = e.getPropertyDescriptor();
            HelpResolver helpResolver = CssModuleSupport.getHelpResolver();
            if (helpResolver != null) {
                URL url = helpResolver.resolveLink(elementHandle.getFileObject(), property, link);
                if (url != null) {
                    return new UrlHandle(url.toExternalForm());
                }
            }
        }
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, final int caretOffset, boolean upToOffset) {
        CssParserResult result = (CssParserResult) info;
        Snapshot snapshot = info.getSnapshot();
        int embeddedCaretOffset = snapshot.getEmbeddedOffset(caretOffset);

        TokenHierarchy hi = snapshot.getTokenHierarchy();
        String prefix = getPrefix(hi.tokenSequence(CssTokenId.language()), embeddedCaretOffset);
        if (prefix == null) {
            return null;
        }
        Node leaf = NodeUtil.findNonTokenNodeAtOffset(result.getParseTree(), embeddedCaretOffset);
        if(leaf == null) {
            return null;
        }
        boolean inPropertyDeclaration = NodeUtil.getAncestorByType(leaf, NodeType.propertyDeclaration) != null;

        //really ugly handling of class or id selector prefix:
        //Since the getPrefix() method is parser result based it is supposed
        //to work on top of the snapshot, while GsfCompletionProvider$Task.canFilter()
        //should be fast and hence operates on document, there arises a big contradiction -
        //For the virtually generated class and id selectors, the dot or hash chars
        //are part of the virtual source and hence becomes a part of the prefix in
        //this method call, while in the real html document they are invisible and an
        //attribute quote resides on their place.
        //So if a GsfCompletionProvider$CompletionEnvironment is created, an anchor
        //is computed from the caret offset and prefix lenght (prefix returned from
        //this method). After subsequent user's keystrokes the canFilter() method
        //gets text from this anchor to the caret from the edited document! So the
        //prefix contains the attribute quotation and any css items cannot be filtered.
        //this is a poor and hacky solution to this issue, some bug may appear for
        //non class or id elements starting with dot or hash?!?!?
        //do not apply the hack in property declarations as it breaks the hash colors completion items filtering
        if (!inPropertyDeclaration && (prefix.length() > 0 && (prefix.charAt(0) == '.' || prefix.charAt(0) == '#'))) {
            firstPrefixChar = prefix.charAt(0);
            return prefix.substring(1);
        } else {
            firstPrefixChar = 0;
            return prefix;
        }

    }

//    private String normalizeLink(ElementHandle handle , String link){
//        if ( link.startsWith("." )|| link.startsWith("/" )){ // NOI18N
//            return normalizeLink(handle, link.substring( 1 ));
//        }
//        int index = link.lastIndexOf('#');
//        if ( index !=-1 ){
//            if ( index ==0 || link.charAt(index-1) =='/'){
//                String helpZipUrl = CssHelpResolver.getHelpZIPURL().getPath();
//                if ( handle instanceof CssPropertyElement ){
//                    String name = ((CssPropertyElement)handle).getPropertyDescriptor().getName();
//                    URL propertyHelpURL = CssHelpResolver.instance().
//                        getPropertyHelpURL(name);
//                    String path = propertyHelpURL.getPath();
//                    if ( path.startsWith( helpZipUrl )){
//                        path = path.substring(helpZipUrl.length());
//                    }
//                    return path+link.substring( index );
//                }
//                else if (handle instanceof UrlHandle){
//                    String url = handle.getName();
//                    int anchorIndex = url.lastIndexOf('#');
//                    if ( anchorIndex!= -1 ){
//                        url = url.substring( 0, anchorIndex);
//                    }
//                    //"normalize" the URL - use just the "path" part
//                    try {
//                        URL _url = new URL(url);
//                        url = _url.getPath();
//                    } catch(MalformedURLException mue) {
//                        Logger.getLogger("global").log(Level.INFO, null, mue);
//                    }
//
//                    if ( url.startsWith( helpZipUrl)){
//                        url = url.substring(helpZipUrl.length());
//                    }
//                    return url+link.substring( index );
//                }
//            }
//        }
//        return link;
//    }
    private String getPrefix(TokenSequence<CssTokenId> ts, int caretOffset) {
        //we are out of any css
        if (ts == null || caretOffset < 0) {
            return null;
        }

        int diff = ts.move(caretOffset);
        if (diff <= 0) {
            if (!ts.movePrevious()) {
                //beginning of the token sequence, cannot get any prefix
                return ""; //NOI18N
            }
        } else {
            if (!ts.moveNext()) {
                return null;
            }
        }
        Token<CssTokenId> t = ts.token();

        switch (t.id().getTokenCategory()) {
            case KEYWORDS:
            case OPERATORS:
            case BRACES:
                return EMPTY_STRING;
        }

        int skipPrefixChars = 0;
        switch (t.id()) {
            case COLON:
            case DCOLON:
            case COMMA:
            case LBRACKET:
                return EMPTY_STRING;

            case STRING:
                skipPrefixChars = 1; //skip the leading quotation char
                break;
            case URI:
                if (diff > 0) {
                    //inside the URI value
                    String text = ts.token().text().toString();
                    Matcher m = Css3Utils.URI_PATTERN.matcher(text);
                    if (m.matches()) {
                        int groupIndex = 1;
                        //content of the url(...) function w/o ws prefix/postfix if there's any
                        String value = m.group(groupIndex);
                        int valueStart = m.start(groupIndex);
                    
                        if(diff >= valueStart) {
                            //cut off everyhing after caret: fold|er/file.css
                            int cutIndex = diff - valueStart;
                            value = value.substring(0, cutIndex); 

                            int lastSeparatorIndex = value.lastIndexOf(Css3Utils.FILE_SEPARATOR); 
                            if(lastSeparatorIndex != -1) {
                                //url(folder/xxx|)
                                skipPrefixChars = valueStart + lastSeparatorIndex + 1;
                            } else {
                                //url(xx|)
                                skipPrefixChars = valueStart;
                                 //is the value quoted?
                                if(!value.isEmpty() && (value.charAt(0) == '"' || value.charAt(0) == '\'')) {
                                    skipPrefixChars++;
                                }
                            }
                        }
                    }
            }
            break;
        }

        return t.text().subSequence(skipPrefixChars, diff == 0 ? t.text().length() : diff).toString().trim();

    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        int offset = component.getCaretPosition();
        if (typedText == null || typedText.length() == 0) {
            return QueryType.NONE;
        }
        char c = typedText.charAt(typedText.length() - 1);

        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(component.getDocument(), offset, CssTokenId.language());
        if (ts != null) {
            int diff = ts.move(offset);
            TokenId currentTokenId = null;
            if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                currentTokenId = ts.token().id();
            }

            if (currentTokenId == CssTokenId.IDENT) {
                return QueryType.COMPLETION;
            }

            //#177306  Eager CSS CC
            //
            //open completion when a space is pressed, but only
            //if typed by user by pressing the spacebar.
            //
            //1) filters out tabs which are converted to spaces
            //   before being put into the document
            //2) filters out newline keystrokes which causes the indentation
            //   to put some spaces on the newline
            //3) filters out typing spaces in comments
            //
            if (typedText.length() == 1 && c == ' ' && currentTokenId != CssTokenId.COMMENT) {
                return QueryType.COMPLETION;
            }
        }

        switch (c) {
            case '\n':
            case '}':
            case ';': {
                return QueryType.STOP;
            }
            case '.':
            case '#':
            case ':':
            case ',': {
                return QueryType.COMPLETION;
            }
        }
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private CodeCompletionResult handleLexicalBasedCompletion(FileObject file, TokenSequence<CssTokenId> ts, Snapshot snapshot, int caretOffset) {
        //position the token sequence on the caret position, not the recomputed offset with substracted prefix length
        int tokenDiff = ts.move(snapshot.getEmbeddedOffset(caretOffset));
        if (ts.moveNext() || ts.movePrevious()) {
            boolean addSemicolon = true;
            switch (ts.token().id()) {
                case SEMI: //@import |;
                    addSemicolon = false;
                case WS: //@import |
                case NL:
                    if (addSemicolon) {
                    Token semicolon = LexerUtils.followsToken(ts, CssTokenId.SEMI, false, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT);
                    addSemicolon = (semicolon == null);
                }
                if (null != LexerUtils.followsToken(ts, CssTokenId.IMPORT_SYM, true, false, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT)) {
                        List<CompletionProposal> imports = (List<CompletionProposal>) completeImport(file, caretOffset, "", true, addSemicolon);
                        int moveBack = (addSemicolon ? 1 : 0) + 1; //+1 means the added quotation mark length
                        return new CssFileCompletionResult(imports, moveBack);
                    }
                    break;
                case STRING: //@import "|"; or @import "fil|";
                    Token<CssTokenId> originalToken = ts.token();
                    addSemicolon = false;
                    if (null != LexerUtils.followsToken(ts, CssTokenId.IMPORT_SYM, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT)) {
                        //strip off the leading quote and the rest of token after caret
                        if (tokenDiff < 1) { // @import |"";
                            return null;
                        }
                        String valuePrefix = originalToken.text().toString().substring(1, tokenDiff);
                        List<CompletionProposal> imports = (List<CompletionProposal>) completeImport(file,
                                caretOffset, valuePrefix, false, addSemicolon);
                        int moveBack = addSemicolon ? 1 : 0;
                        return new CssFileCompletionResult(imports, moveBack);

                    }
                    break;
                    
                case URI:
                    //url(...)
                    String text = ts.token().text().toString();
                    Matcher m = Css3Utils.URI_PATTERN.matcher(text);
                    if (m.matches()) {
                        int groupIndex = 1;
                        //content of the url(...) function w/o ws prefix/postfix if there's any
                        String value = m.group(groupIndex);
                        int valueStart = m.start(groupIndex);
                    
                        if(tokenDiff >= valueStart) {
                            int cutIndex = tokenDiff - valueStart;
                            value = value.substring(0, cutIndex); //cut off everyhing after caret: fold|er/file.css
                        }
                        
                        //is the value quoted?
                        if(!value.isEmpty() && (value.charAt(0) == '"' || value.charAt(0) == '\'')) {
                            value = value.substring(1); //cut of the quote
                        }
                        
                        //use prefix from last separator in the URL
                        int lastSeparatorIndex = value.lastIndexOf(Css3Utils.FILE_SEPARATOR); 
                        if(lastSeparatorIndex != -1) {
                            //some folders already in the path, we also need to adjust the base file
                            String valuePrefix = value.substring(0, lastSeparatorIndex);
                            FileObject base = WebUtils.resolve(file, valuePrefix);
                            if(base != null) {
                                String prefix = value.substring(lastSeparatorIndex + 1);
                                List<CompletionProposal> imports = (List<CompletionProposal>) completeImport(base,
                                caretOffset, prefix, false, false);
                            return new CssFileCompletionResult(imports, 0);
                            }
                        } else {
                            //no separator in the URL, prefix from the beginning
                            List<CompletionProposal> imports = (List<CompletionProposal>) completeImport(file,
                                    caretOffset, value, false, false);
                            return new CssFileCompletionResult(imports, 0);
                        }
                        
                       
                    }
                    break;

            }
        }

        return null;
    }

    private void completeClassSelectors(CompletionContext context, List<CompletionProposal> completionProposals, boolean unmappableClassOrId) {
        Node node = context.getActiveNode();
        FileObject file = context.getSnapshot().getSource().getFileObject();
        String prefix = context.getPrefix();
        int offset = context.getAnchorOffset();
        NodeType nodeType = node.type();

        switch (nodeType) {
            case cssClass:
                break;
            case error:
            case recovery:
                //check if the error is in a rule
                if (NodeUtil.getAncestorByType(node, NodeType.rule) != null) {
                //check the prefix
                try {
                    TokenSequence<CssTokenId> tokenSequence = context.getTokenSequence();
                    switch (tokenSequence.token().id()) {
                        case DOT:
                            //.| case
                            break;
                        case IDENT:
                            if (tokenSequence.movePrevious()) {
                            if (tokenSequence.token().id() == CssTokenId.DOT) {
                                //.sg| case
                                break;
                            }
                        }
                        default:
                            return;
                    }
                } finally {
                    context.restoreTokenSequence();
                }
            }
                break;

            default:
                return; //exit
        }

        //complete class selectors
        Collection<String> allclasses = new HashSet<>();
        Collection<String> refclasses = new HashSet<>();

        //adjust prefix - if there's just . before the caret, it is returned
        //as a prefix. If there are another characters, the dot is ommited
        if (prefix.length() == 1 && prefix.charAt(0) == '.') {
            prefix = "";
            offset++; //offset point to the dot position, we need to skip it
        }

        if (file != null) {
            CssProjectSupport sup = CssProjectSupport.findFor(file);
            if (sup != null) {
                CssIndex index = sup.getIndex();
                DependenciesGraph deps = index.getDependencies(file);
                Collection<FileObject> refered = deps.getAllReferedFiles();

                //get map of all fileobject declaring classes with the prefix
                Map<FileObject, Collection<String>> search = index.findClassesByPrefix(prefix);
                for (FileObject fo : search.keySet()) {
                    allclasses.addAll(search.get(fo));
                    //is the file refered by the current file?
                    if (refered.contains(fo)) {
                        //yes - add its classes
                        refclasses.addAll(search.get(fo));
                    }
                }

            }
        }

        //unit test support
        if (TEST_CLASSES != null) {
            allclasses.addAll(Arrays.asList(TEST_CLASSES));
        }

        //lets create the completion items
        List<CompletionProposal> proposals = new ArrayList<>(refclasses.size());
        for (String clazz : allclasses) {
            proposals.add(CssCompletionItem.createSelectorCompletionItem(new CssElement(context.getFileObject(), clazz),
                    clazz,
                    offset,
                    refclasses.contains(clazz)));
        }
        completionProposals.addAll(proposals);
    }

    private void completeIdSelectors(CompletionContext context, List<CompletionProposal> completionProposals, boolean unmappableClassOrId) {
        Node node = context.getActiveNode();
//        Node node = context.getNodeForNonWhiteTokenBackward();
        FileObject file = context.getSnapshot().getSource().getFileObject();
        String prefix = context.getPrefix();
        int offset = context.getAnchorOffset();
        NodeType nodeType = node.type();

        if (prefix.length() > 0 && (node.type() == NodeType.cssId
                || (unmappableClassOrId || nodeType == NodeType.error /*
                 * || nodeType == NodeType.JJTERROR_SKIPBLOCK
                 */) && prefix.charAt(0) == '#')) {
            //complete class selectors
            Collection<String> allids = new HashSet<>();
            Collection<String> refids = new HashSet<>();
            Collection<String> fileids = new HashSet<>();

            //adjust prefix - if there's just # before the caret, it is returned as a prefix
            //if there is some text behind the prefix the hash is part of the prefix
            if (prefix.length() == 1 && prefix.charAt(0) == '#') {
                prefix = "";
            } else {
                prefix = prefix.substring(1); //cut off the #
            }
            offset++; //offset point to the hash position, we need to skip it

            if (file != null) {
                CssProjectSupport sup = CssProjectSupport.findFor(file);
                if (sup != null) {
                    CssIndex index = sup.getIndex();
                    DependenciesGraph deps = index.getDependencies(file);
                    Collection<FileObject> refered = deps.getAllReferedFiles();
                    //get map of all fileobject declaring classes with the prefix
                    Map<FileObject, Collection<String>> search = index.findIdsByPrefix(prefix); //cut off the dot (.)
                    for (FileObject fo : search.keySet()) {
                        allids.addAll(search.get(fo));
                        //is the file refered by the current file?
                        if (refered.contains(fo)) {
                            //yes - add its classes
                            refids.addAll(search.get(fo));
                        }
                    }
                    fileids = search.get(file);
                }
            }

            //unit test support
            if (TEST_IDS != null) {
                allids.addAll(Arrays.asList(TEST_IDS));
            }

            //lets create the completion items
            List<CompletionProposal> proposals = new ArrayList<>(allids.size());
            for (String id : allids) {
                proposals.add(CssCompletionItem.createSelectorCompletionItem(new CssElement(context.getFileObject(), id),
                        id,
                        offset,
                        fileids == null || !fileids.contains(id))); 
            }
            completionProposals.addAll(proposals);

        }
    }

    /**
     * Complete at-rules and html selectors if *there's no prefix*.
     *
     * @param context
     * @param completionProposals
     */
    private void completeAtRulesAndHtmlSelectors(CompletionContext context, List<CompletionProposal> completionProposals) {
        if (!context.getPrefix().trim().isEmpty()) {
            return;
        }

        Node node = context.getActiveNode();
        //switch to first non error node
        loop:
        for (;;) {
            switch (node.type()) {
                case error:
                case recovery:
                    node = node.parent();
                    break;
                default:
                    break loop;
            }
        }
        switch (node.type()) {
            case root:
            case styleSheet:
            case body:
            case moz_document:
            case imports:
            case namespaces:
                TokenSequence<CssTokenId> ts = context.getTokenSequence();
                if (ts.movePrevious()) {
                    if (ts.token().id().getTokenCategory() == CssTokenIdCategory.AT_RULE_SYMBOL) {
                        //filter out situation(s) like: @import | a { ... } where we fall into 
                        //imports node w/ empty prefix, but don't want to complete any at-rules or html selectors
                        return;
                    }
                }
                /*
                 * somewhere between rules, in an empty or very broken file, between
                 * rules
                 */
                List<CompletionProposal> all = new ArrayList<>();
                //complete at keywords without prefix
                all.addAll(Utilities.createRAWCompletionProposals(AT_RULES, ElementKind.FIELD, context.getAnchorOffset()));
                //complete html selector names
                all.addAll(completeHtmlSelectors(context, context.getPrefix(), context.getAnchorOffset()));
                completionProposals.addAll(all);
        }
    }

    private void completeHtmlSelectors(CompletionContext completionContext, List<CompletionProposal> completionProposals, TokenId tokenNodeTokenId) {
        String prefix = completionContext.getPrefix();
        int caretOffset = completionContext.getCaretOffset();

        Node node = completionContext.getActiveNode();
        switch (node.type()) {
            case media:
                //check if we are in the mediaQuery section and not in the media body
                if (null == LexerUtils.followsToken(completionContext.getTokenSequence(), CssTokenId.LBRACE, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT)) {
                //@media | { div {} }
                break;
            }
            //@media xxx { | }
            //=>fallback to the mediaQuery 
            case mediaBody:
                //work only in WS after a semicolon or left or right curly brace
                //1. @media screen { | }, @media screen { div {} | }
                //2. @media screen { @include x; | }
                if (null != LexerUtils.followsToken(completionContext.getTokenSequence(),
                        EnumSet.of(CssTokenId.SEMI, CssTokenId.LBRACE, CssTokenId.RBRACE),
                        true, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT)) {
                completionProposals.addAll(completeHtmlSelectors(completionContext, completionContext.getPrefix(), completionContext.getCaretOffset()));
            }
                break;
            case elementName:
                //complete selector's element name - with a prefix
                completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, completionContext.getSnapshot().getOriginalOffset(completionContext.getActiveNode().from())));
                break;

            case elementSubsequent:
            case typeSelector:
                //complete element name - without a or with a prefix
                completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                break;

            case selectorsGroup:
            case simpleSelectorSequence:
            case combinator:
            case selector:
            case rule:
                CssTokenId activeTokenId = completionContext.getActiveTokenId();
                if (activeTokenId == CssTokenId.WS || activeTokenId == CssTokenId.NL || activeTokenId.getTokenCategory() == CssTokenIdCategory.OPERATORS) {
                    //complete selector list without prefix in selector list e.g. BODY, | { ... }

                    //filter out situation when the completion is invoked just after the left curly bracket
                    // div { | color: red;} or div { | }
                    //in this case the caret position falls to the rule node as the declarations node
                    //doesn't contain the whitespace before first declaration
                    //
                    //note: in css preprocessor source we want the selectors to be offered even in this filtered out situation
                    //
                    TokenSequence<CssTokenId> tokenSequence = completionContext.getTokenSequence();
                    if (completionContext.isCssPreprocessorSource() || null == LexerUtils.followsToken(tokenSequence, CssTokenId.LBRACE, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT)) {
                        completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                    }
                }
                break;
            case declarations:
                if(completionContext.isCssPreprocessorSource()) {
                    completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                    break;
                }
                //@mixin mymixin() { div {} | }
                if (NodeUtil.getAncestorByType(node, NodeType.cp_mixin_block) == null) {
                break; //do not complete
            }
            //fallback to cp_mixin_block

            case cp_mixin_block:
                completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                break;

            case error:
                Node parentNode = completionContext.getActiveNode().parent();
                if (parentNode == null) {
                    break;
                }
                switch (completionContext.getActiveTokenId()) {
                    //completion of selectors after universal selector * | { ... }
                    case WS:
                        switch (parentNode.type()) {
                        case bodyItem:
                        case rule:
                        case typeSelector:
                        case selector:
                        case selectorsGroup:
                        case simpleSelectorSequence:
                            //complete selector list in selector list with an error
                            completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                            break;
                    }
                        break;

                    case IDENT:
                        switch (parentNode.type()) {
                        case declaration:
                            //@mixin mymixin() { tabl| } 
                            if (NodeUtil.getAncestorByType(parentNode, NodeType.cp_mixin_block) != null) {
                            //declaration in mixin block
                            //the prefix may represent either the property name of selector
                            completionProposals.addAll(completeHtmlSelectors(completionContext, prefix, caretOffset));
                        }
                            break;
                    }
                        break;

                }
                break;
        }

    }

    /**
     * Complete at-rules if there's a completion prefix.
     *
     * @param completionContext
     * @param completionProposals
     * @param tokenFound
     */
    private void completeKeywords(CompletionContext completionContext, List<CompletionProposal> completionProposals, boolean tokenFound) {
        if (!tokenFound) {
            return;
        }
        Node node = completionContext.getActiveNode();
        //switch to first non error node
        loop:
        for (;;) {
            switch (node.type()) {
                case error:
                case recovery:
                    node = node.parent();
                    break;
                default:
                    break loop;
            }
        }
        switch (node.type()) {
            case imports:
            case media:
            case page:
            case charSet:
            case generic_at_rule:
            case bodyItem:
            case fontFace:
                switch (completionContext.getTokenSequence().token().id()) {
                case IMPORTANT_SYM:
                case MEDIA_SYM:
                case PAGE_SYM:
                case CHARSET_SYM:
                case AT_IDENT:
                case FONT_FACE_SYM:
                case ERROR:
                    Collection<String> possibleValues = filterStrings(AT_RULES, completionContext.getPrefix());
                    completionProposals.addAll(Utilities.createRAWCompletionProposals(possibleValues, ElementKind.FIELD, completionContext.getSnapshot().getOriginalOffset(completionContext.getActiveNode().from())));
            }
                break;
            case simpleSelectorSequence:
                //@| -- parsed as simpleSelectorSequence due to the possible less_selector_interpolation -- @{...} in selectorsGroup
                switch (completionContext.getTokenSequence().token().id()) {
                case AT_SIGN:
                    Collection<String> possibleValues = filterStrings(AT_RULES, completionContext.getPrefix());
                    completionProposals.addAll(Utilities.createRAWCompletionProposals(possibleValues, ElementKind.FIELD, completionContext.getSnapshot().getOriginalOffset(completionContext.getActiveNode().from())));
                    break;
            }
            case styleSheet:
                //@| in empty file
                switch (completionContext.getTokenSequence().token().id()) {
                case ERROR:
                    Collection<String> possibleValues = filterStrings(AT_RULES, completionContext.getPrefix());
                    completionProposals.addAll(Utilities.createRAWCompletionProposals(possibleValues, ElementKind.FIELD, completionContext.getSnapshot().getOriginalOffset(completionContext.getActiveNode().from())));
                    break;
            }
        }
    }

    private void completePropertyName(CompletionContext cc, List<CompletionProposal> completionProposals) {
//        Node activeNode = cc.getActiveNode();

//        System.out.println("caret =" + cc.getCaretOffset());
//        Node nonwsnode = cc.getNodeForNonWhiteTokenBackward();
//        System.out.println("non ws node back= " + nonwsnode);
        Node node = cc.getActiveNode();
//        System.out.println("active node= " + node);
//        CssTokenId tid = cc.getActiveTokenId();
//        System.out.println("token id= " + tid);
        NodeType nodeType = node.type();

        String prefix = cc.getPrefix();
        Collection<PropertyDefinition> defs = Properties.getPropertyDefinitions(cc.getFileObject());

        //1. css property name completion with prefix
        if (nodeType == NodeType.property && (prefix.length() > 0 || cc.getEmbeddedCaretOffset() == cc.getActiveNode().from())) {
            Collection<PropertyDefinition> possibleProps = filterProperties(defs, prefix);
            completionProposals.addAll(Utilities.wrapProperties(possibleProps, cc.getSnapshot().getOriginalOffset(cc.getActiveNode().from())));
        } else if (nodeType == NodeType.elementName) {
            //@mixin mymixin() { co| div { } } 
            if (NodeUtil.getAncestorByType(node, NodeType.cp_mixin_block) != null || NodeUtil.getAncestorByType(node, NodeType.declarations) != null ) {
                //in mixin block
                Collection<PropertyDefinition> possibleProps = filterProperties(defs, prefix);
                completionProposals.addAll(Utilities.wrapProperties(possibleProps, cc.getSnapshot().getOriginalOffset(cc.getActiveNode().from())));
            }
        }

        //2. in a garbage (may be for example a dash prefix in a ruleset
        if (nodeType == NodeType.recovery || nodeType == NodeType.error) {
            Node parent = cc.getActiveNode().parent();

            //recovery can have error as parent
            if (parent != null && parent.type() == NodeType.error) {
                parent = parent.parent();
            }

            if (parent != null && (parent.type() == NodeType.property
                    || parent.type() == NodeType.declarations
                    || parent.type() == NodeType.declaration //related to the declarations rule error recovery issue
                    || parent.type() == NodeType.propertyDeclaration //related to the declarations rule error recovery issue
                    || parent.type() == NodeType.cp_mixin_block
                    || parent.type() == NodeType.moz_document)) {
                //Bug 233584 - Sass: completion for mixin after @include 
                //do not show properties after @include in sass
                CssTokenId nonWhiteTokenIdBackward = cc.getNonWhiteTokenIdBackward();
                if (nonWhiteTokenIdBackward != null) {
                    switch (nonWhiteTokenIdBackward) {
                        case SASS_INCLUDE:
                        case COLON: //div { &:| } -- sass parent selector reference + pseudo class
                        case DCOLON: //div { &::| } -- sass parent selector reference + pseudo element
                            return;

                        case IDENT:
                            //div { &:hov| } -- sass parent selector reference + pseudo element
                            //div { &::firs| } -- sass parent selector reference + pseudo element
                            TokenSequence<CssTokenId> ts = cc.getTokenSequence();
                            int index = ts.index();
                            try {
                                if (ts.movePrevious()) {
                                    switch (ts.token().id()) {
                                        case COLON:
                                        case DCOLON:
                                            if (ts.movePrevious()) {
                                            switch (ts.token().id()) {
                                                case LESS_AND:
                                                    //do not complete properties
                                                    return;
                                                default:
                                                    break;
                                            }
                                        }
                                    }
                                }
                            } finally {
                                ts.moveIndex(index);
                            }

                    }
                }

                //>>> Bug 204821 - Incorrect completion for vendor specific properties
                boolean bug204821 = false;
                if (prefix.length() == 0) {
                    //
                    //If the vendor specific property name is completed with the - (MINUS)
                    //prefix the cc.getPrefix() is empty since minus is operator
                    //But particulary in this case the prefix must count
                    if (cc.getActiveTokenId() == CssTokenId.MINUS) {
                        bug204821 = true;
                    }
                }
                if (bug204821) {
                    //get all "-" prefixed props
                    Collection<PropertyDefinition> possibleProps
                            = filterProperties(Properties.getPropertyDefinitions(cc.getFileObject()), "-");
                    //and add them to the result with the "-" prefix stripped
                    completionProposals.addAll(Utilities.wrapProperties(possibleProps, cc.getCaretOffset(), 1));
                } else {
                    Collection<PropertyDefinition> possibleProps
                            = filterProperties(defs, prefix);
                    completionProposals.addAll(Utilities.wrapProperties(possibleProps, cc.getAnchorOffset()));
                }
            }
        }

        //3. in empty rule (NodeType.ruleSet)
        //h1 { | }
        //
        //4. between declaration-s (NodeType.declarations)
        //h1 { color:red; | font: bold }
        //
        //should be no prefix 
        switch (nodeType) {
            case declarations:
                //div { font: bold | } case -- we need to continue offering property values
                //until the property declaration is not closed by semicolon
                Node nodeBw = cc.getNodeForNonWhiteTokenBackward();
                if (NodeUtil.getAncestorByType(nodeBw, NodeType.propertyDeclaration) != null) {
                    //the previous non-ws token belongs to a property declaration so 
                    //this means the property declaration is not closed by semicolon as the
                    //semicolon belongs directly to declarations node => do not offer properties here
                    break;
                }
            //fall through
            case rule:
            case moz_document:
            case cp_mixin_block: //XXX should be defined in css.prep module
                completionProposals.addAll(Utilities.wrapProperties(defs, cc.getCaretOffset()));
                break;

        }

    }

    private void completePropertyValue(
            CompletionContext context,
            List<CompletionProposal> completionProposals,
            char charAfterCaret) {

        Node node = context.getActiveNode();
//        Node node = context.getNodeForNonWhiteTokenBackward();
        String prefix = context.getPrefix();
        NodeType nodeType = node.type();

        switch (nodeType) {

            case declarations:
                //In following case the caret offset falls into the declarations node
                //which contains the whitespace after the propertyDescription.
                //However as the propertyDeclaration is not closed by semicolon 
                //we should go on and offer property values for "color" property
                //div { color: red | }
                if (context.getActiveTokenId() == CssTokenId.SEMI
                        || LexerUtils.followsToken(context.getTokenSequence(), CssTokenId.SEMI, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT) != null) {
                //semicolon found when searching backward - we are not going to
                //complete property values
                break;
            }
                //find the latest declaration backward
                Node[] declarations = NodeUtil.getChildrenByType(node, NodeType.declaration);
                if (declarations.length > 0) {
                    Node declarationNode = declarations[declarations.length - 1];
                    //check for propertyDeclaration subnode
                    Node propertyDeclaration = NodeUtil.getChildByType(declarationNode, NodeType.propertyDeclaration);
                    if (propertyDeclaration == null) {
                        break; //do not complete property value
                    }
                    //fall through to the next section
                } else {
                    break; //do not complete property value
                }

            case declaration:
            case propertyDeclaration: {
                //value cc without prefix
                //find property node

                final Node[] result = new Node[3];
                NodeVisitor propertySearch = new NodeVisitor() {

                    @Override
                    public boolean visit(Node node) {
                        switch (node.type()) {
                            case property:
                                result[0] = node;
                                break;
                            case propertyValue:
                            case cp_propertyValue:
                                result[1] = node;
                                break;
                            case error:
                                result[2] = node;
                                break;
                        }
                        return false;
                    }
                };
                propertySearch.visitChildren(node);

                Node property = result[0];
                if (property == null) {
                    return;
                }

                String expressionText = ""; //NOI18N

                if (result[1] != null) {
                    //issue "231081 - Completion requires semicolon if it's not the last one in rule" workaround
                    //div{
                    //    color: |
                    //    font-size: 12px;
                    //}
                    //
                    //the "font-size" becomes a propertyValue node and the following COLON causes error outside of the propertyValue node (correctly)
                    if (LexerUtils.followsToken(context.getTokenSequence(), EnumSet.of(CssTokenId.COLON), true, true, true, CssTokenId.WS, CssTokenId.NL, CssTokenId.COMMENT) != null) {
                        //we are just after the colon
                        expressionText = "";
                    } else {
                        //take the expression text from the existing property value
                        expressionText = result[1].image().toString();
                    }
                }

                if (result[2] != null) {
                    //error in the property value
                    //we need to extract the value from the property node image

                    String propertyImage = node.image().toString().trim();
                    //if the property is the last one in the rule then the error
                    //contains the closing rule bracket
                    if (propertyImage.endsWith("}")) { //NOI18N
                        propertyImage = propertyImage.substring(0, propertyImage.length() - 1);
                    }

                    int colonIndex = propertyImage.indexOf(':'); //NOI18N
                    if (colonIndex >= 0) {
                        expressionText = propertyImage.substring(colonIndex + 1);
                    }

                    //remove semicolon if it happens to appear in the image
                    //completion in place like: background: | ;
                    //or in html code <div style="backgroung:|"/> (virtual source generator adds the semi)
                    int semiIndex = expressionText.lastIndexOf(';');
                    if (semiIndex > 0) {
                        expressionText = expressionText.substring(0, semiIndex);
                    }

                    //use just the current line, if the expression spans to multiple
                    //lines it is likely because of parsing error
                    int eolIndex = expressionText.indexOf('\n');
                    if (eolIndex > 0) {
                        expressionText = expressionText.substring(0, eolIndex);
                    }

                }

                PropertyDefinition prop = Properties.getPropertyDefinition(property.image().toString().trim());
                if (prop != null) {

                    ResolvedProperty propVal = new ResolvedProperty(context.getFileObject(), prop, expressionText);

                    Collection<ValueGrammarElement> alts = propVal.getAlternatives();

                    Collection<ValueGrammarElement> filteredByPrefix = filterElements(alts, prefix);

                    int completionItemInsertPosition = prefix.trim().length() == 0
                            ? context.getCaretOffset()
                            : context.getSnapshot().getOriginalOffset(node.from());

                    //test the situation when completion is invoked just after a valid token
                    //like color: rgb|
                    //in such case the parser offers ( alternative which is valid
                    //so we must not use the prefix for filtering the results out.
                    //do that only if the completion is not called in the middle of a text,
                    //there must be a whitespace after the caret
                    boolean addSpaceBeforeItem = false;
                    if (alts.size() > 0 && filteredByPrefix.isEmpty() && Character.isWhitespace(charAfterCaret)) {
                        completionItemInsertPosition = context.getCaretOffset(); //complete on the position of caret
                        filteredByPrefix = alts; //prefix is empty, do not filter at all
                        addSpaceBeforeItem = true;
                    }

                    completionProposals.addAll(wrapPropertyValues(context,
                            prefix,
                            prop,
                            filteredByPrefix,
                            completionItemInsertPosition,
                            false,
                            addSpaceBeforeItem,
                            false));

                }
            }
            break;

            case recovery:
                Node parent = node.parent();
                if (parent.type() != NodeType.error) {
                    break;
                }
                node = parent;
            //fall through

            case error:
                Node parentNode = node.parent();
                NodeType parentType = parentNode.type();

                if (!(parentType == NodeType.propertyValue
                        || parentType == NodeType.term
                        || parentType == NodeType.expression
                        || parentType == NodeType.operator
                        || parentType == NodeType.propertyDeclaration
                        || parentType == NodeType.declaration)) {
                    break;
                }
            //fall through

            case hexColor:
            case propertyValue:
            case function:
            case functionName:
            case term:
            case expression:
            case operator: {
                parentNode = node.parent();
                parentType = parentNode.type();
                Node declarationNode = null;

                if (parentType != null && parentType == NodeType.declaration) {
                    //fallen through from the error case
                    //this means there's an error in a property value and 
                    //due to some semantic predicates we are not in propertyValue
                    //node but in some of its predecessors.
                    //Lets ry to find the preceeding propertyDeclaration node
                    Node siblingBefore = NodeUtil.getSibling(parentNode, true);
                    if (siblingBefore != null && siblingBefore.type() == NodeType.declaration) {
                        declarationNode = NodeUtil.getChildByType(siblingBefore, NodeType.propertyDeclaration);
                    }
                }

                //value cc with prefix
                //a. for term nodes
                //b. for error skip declaration nodes with declaration parent,
                //for example if user types color: # and invokes the completion
                //find property node
                //1.find declaration node first
                final Node[] result = new Node[1];
                if (declarationNode == null) {
                    NodeVisitor declarationSearch = new NodeVisitor() {

                        @Override
                        public boolean visit(Node node) {
                            if (node.type() == NodeType.propertyDeclaration) {
                                result[0] = node;
                            }
                            return false;
                        }

                    };
                    declarationSearch.visitAncestors(node);
                    declarationNode = result[0];
                }

                if (declarationNode == null) {
                    //not in property declaration, give up
                    break;
                }

                //2.find the property node
                result[0] = null;
                NodeVisitor propertySearch = new NodeVisitor() {

                    @Override
                    public boolean visit(Node node) {
                        if (node.type() == NodeType.property) {
                            result[0] = node;
                        }
                        return false;
                    }
                };
                propertySearch.visitChildren(declarationNode);

                Node property = result[0];
                if (property == null) {
                    //the property part may be replaced by the scss interpolation expression
                    return;
                }

                String propertyName = property.image().toString();
                PropertyDefinition propertyDefinition = Properties.getPropertyDefinition(propertyName);
                if (propertyDefinition == null) {
                    return;
                }

                //text from the node start to the embedded anchor offset (=embedded caret offset - prefix length)
                Node value = NodeUtil.query(declarationNode, NodeType.propertyValue.name()); //NOI18N
                if (value == null) {
                    //no propertyValue node, may be CP expression
                    value = NodeUtil.query(declarationNode, NodeType.cp_propertyValue.name()); //NOI18N
                    if (value == null) {
                        return;
                    }
                }
                String expressionText = context.getSnapshot().getText().subSequence(
                        value.from(),
                        context.getEmbeddedAnchorOffset()).toString();

                //use just the current line, if the expression spans to multiple
                //lines it is likely because of parsing error
                int eolIndex = expressionText.indexOf('\n');
                if (eolIndex > 0) {
                    expressionText = expressionText.substring(0, eolIndex);
                }

                ResolvedProperty propVal = new ResolvedProperty(context.getFileObject(), propertyDefinition, expressionText);

                Collection<ValueGrammarElement> alts = propVal.getAlternatives();
                Collection<ValueGrammarElement> filteredByPrefix = filterElements(alts, prefix);

                int completionItemInsertPosition = context.getAnchorOffset();
                boolean addSpaceBeforeItem = false;

                boolean includePrefixInTheExpression = false;
                boolean extendedItemsOnly = false;

                block:
                {
                    //case #1
                    //========
                    //color: #| completion
                    if (prefix.startsWith("#")) {
                        completionItemInsertPosition = context.getCaretOffset() - prefix.length();
                        filteredByPrefix = alts; //prefix is empty, do not filter at all
                        extendedItemsOnly = true; //do not add any default alternatives items
                        break block;
                    }

                    if (!Character.isWhitespace(charAfterCaret)) {
                        //do the following heuristics only if the completion is invoked after, not inside a token
                        break block;
                    }

                    //case #2
                    //========
                    //in the situation that there's a prefix, but no alternatives matches
                    //we may also try alternatives after the prefix (at the end of the expression)
                    //
                    //animation: cubic-bezier(20|  => evaluated expression is "cubic-bezier("
                    //                                so !number is the alternative, but filtered out since
                    //                                the completion prefix is "20"
                    //
                    //so lets use "cubic-bezier(20" as the expression and empty prefix
                    //this usually happens for unit acceptors (they typically do not put all possible
                    //values to the completion list so the filtering fails).
                    if (!prefix.isEmpty() && filteredByPrefix.isEmpty()) {
                        includePrefixInTheExpression = true;
                        break block;
                    }

                    //case #3
                    //test the situation when completion is invoked just after a valid token
                    //like color: rgb| or font-family: cursive|
                    for (ValueGrammarElement vge : filteredByPrefix) {
                        if (vge.getValue().toString().equals(prefix)) {
                            includePrefixInTheExpression = true;
                            break;
                        }
                    }
                }

                if (includePrefixInTheExpression) {
                    //re-run the property value evaluation with expression including the prefix token
                    expressionText = context.getSnapshot().getText().subSequence(
                            value.from(),
                            context.getEmbeddedCaretOffset()).toString();

                    //use just the current line, if the expression spans to multiple
                    //lines it is likely because of parsing error
                    eolIndex = expressionText.indexOf('\n');
                    if (eolIndex > 0) {
                        expressionText = expressionText.substring(0, eolIndex);
                    }

                    propVal = new ResolvedProperty(context.getFileObject(), propertyDefinition, expressionText);
                    alts = propVal.getAlternatives();
                    filteredByPrefix = alts; //no prefix
                    completionItemInsertPosition = context.getCaretOffset(); //no prefix
                    addSpaceBeforeItem = true;
                }

                completionProposals.addAll(wrapPropertyValues(context,
                        prefix,
                        propertyDefinition,
                        filteredByPrefix,
                        completionItemInsertPosition,
                        false,
                        addSpaceBeforeItem,
                        extendedItemsOnly));

            } //case
        } //switch
    }

    private static class CssFileCompletionResult extends DefaultCompletionResult {

        private int moveCaretBack;

        public CssFileCompletionResult(List<CompletionProposal> list, int moveCaretBack) {
            super(list, false);
            this.moveCaretBack = moveCaretBack;
        }

        @Override
        public void afterInsert(CompletionProposal item) {
            Caret c = EditorRegistry.lastFocusedComponent().getCaret();
            if(moveCaretBack > 0) {
                c.setDot(c.getDot() - moveCaretBack);
            }
        }
    }

    private static class CssLinkCompletion extends FileReferenceCompletion<CssCompletionItem> {

        private static final String GO_UP_TEXT = "../"; //NOI18N
        private final boolean addQuotes;
        private final boolean addSemicolon;
        private final FileObject file;

        public CssLinkCompletion(FileObject file, boolean addQuotes, boolean addSemicolon) {
            this.file = file;
            this.addQuotes = addQuotes;
            this.addSemicolon = addSemicolon;
        }

        @Override
        public CssCompletionItem createFileItem(FileObject file, int anchor) {
            String name = file.getNameExt();
            Color color = file.isFolder() ? Color.BLUE : null;
            ImageIcon icon = FileReferenceCompletion.getIcon(file);

            return CssCompletionItem.createFileCompletionItem(new CssElement(file, name), name, anchor, color, icon, addQuotes, addSemicolon);
        }

        @Override
        public CssCompletionItem createGoUpItem(int anchor, Color color, ImageIcon icon) {
            return CssCompletionItem.createFileCompletionItem(new CssElement(file, GO_UP_TEXT), GO_UP_TEXT, anchor, color, icon, addQuotes, addSemicolon);
        }
    }
}
