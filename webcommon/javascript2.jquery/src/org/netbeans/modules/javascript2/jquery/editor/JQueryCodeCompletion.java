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
package org.netbeans.modules.javascript2.jquery.editor;

import org.netbeans.modules.javascript2.jquery.PropertyNameDataItem;
import org.netbeans.modules.javascript2.jquery.SelectorItem;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.*;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.spi.CompletionProvider;
import org.netbeans.modules.javascript2.jquery.PropertyNameDataLoader;
import org.netbeans.modules.javascript2.jquery.model.JQueryUtils;
import org.netbeans.modules.javascript2.jquery.SelectorsLoader;
import org.netbeans.modules.javascript2.jquery.editor.JQueryCompletionItem.DocSimpleElement;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
@CompletionProvider.Registration(priority=10)
public class JQueryCodeCompletion implements CompletionProvider {

    private static final Logger LOGGER = Logger.getLogger(JQueryCodeCompletion.class.getName());

    public static final String HELP_LOCATION = "docs/jquery-api.xml"; //NOI18N
    private static File jQueryApiFile;
    
    private static final String PROPERTY_NAME_FILE_LOCATION = "docs/jquery-propertyNames.xml"; //NOI18N
    private static File propertyNameFile;

    private static Collection<HtmlTagAttribute> allAttributes;

    private int lastTsOffset = 0;

    @SuppressWarnings("fallthrough")
    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContext jsCompletionContext, String prefix) {
        long start = System.currentTimeMillis();
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();
        ParserResult parserResult = ccContext.getParserResult();
        int offset = ccContext.getCaretOffset();
        lastTsOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(offset);
        switch (jsCompletionContext) {
            case IN_STRING:
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
                if (ts != null) {
                    ts.move(lastTsOffset);
                    if (ts.moveNext()) {
                        if (ts.token().id() == JsTokenId.STRING_END) {
                            ts.movePrevious();
                        }
                        if (ts.token().id() == JsTokenId.STRING) {
                            prefix = ts.token().text().toString().substring(0, lastTsOffset - ts.offset());
                        }
                    }
                }
            case GLOBAL:
            case EXPRESSION:
            case OBJECT_PROPERTY:
                if (JQueryUtils.isInJQuerySelector(parserResult, lastTsOffset)) {
                    addSelectors(result, parserResult, prefix, lastTsOffset, ccContext);
                }
                break;
            case OBJECT_PROPERTY_NAME:
                completeObjectPropertyName(ccContext, result, prefix);
                break;
            default:
                break;
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Counting jQuery CC took {0}ms ", (end - start));
        return result;
    }

    private int findParamIndex(ParserResult parserResult, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!(ts.moveNext() && ts.movePrevious())) {
            return -1;
        }
        Token<? extends JsTokenId> token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
        // count index of parameters
        int paramIndex = 0;
        while(token.id() != JsTokenId.EOL && token.id() != JsTokenId.BRACKET_LEFT_PAREN) {
            if (token.id() == JsTokenId.OPERATOR_COMMA) {
                paramIndex ++;
            } else if (token.id() == JsTokenId.OPERATOR_DOT) {
                // we are not inside ()
                return -1;
            }
            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
        }
        if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
            return paramIndex;
        }
        return -1;
    }
    
    private String findFunctionName(ParserResult parserResult, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return null;
        }
        ts.move(offset);
        if (!(ts.moveNext() && ts.movePrevious())) {
            return null;
        }
        Token<? extends JsTokenId> token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
        while(token.id() != JsTokenId.EOL && token.id() != JsTokenId.BRACKET_LEFT_PAREN) {
            if (token.id() == JsTokenId.OPERATOR_DOT) {
                // we are not inside ()
                return null;
            }
            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
        }
        if (token.id() == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
            if (token.id() == JsTokenId.IDENTIFIER){
                return token.text().toString();
            }
        }
        return null;
    }
    
    

    @Override
    public String getHelpDocumentation(ParserResult info, ElementHandle element) {
        if (element instanceof DocSimpleElement) {
            return ((DocSimpleElement)element).getDocumentation();
        }
        if (element != null && element.getKind() == ElementKind.CALL) {
            String name = element.getName();
            name = name.substring(1); // remove :
            int index = name.indexOf('(');
            if (index > -1) {
                name = name.substring(0, index);
            }
            return SelectorsLoader.getDocumentation(getJQueryAPIFile(), name);
        } else if (element != null &&  element.getKind() == ElementKind.METHOD) {
            if (JQueryUtils.isJQuery(info, lastTsOffset)) {
                return SelectorsLoader.getMethodDocumentation(getJQueryAPIFile(), element.getName());
            }
        }
        return null;
    }

    private void completeObjectPropertyName(CodeCompletionContext ccContext, List<CompletionProposal> result, String prefix) {
        
        // find the object that can be configured
        TokenHierarchy<?> th = ccContext.getParserResult().getSnapshot().getTokenHierarchy();
        if (th == null) {
            return;
        }
        int carretOffset  = ccContext.getCaretOffset();
        int eOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(carretOffset);
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, eOffset);
        if (ts == null) {
            return;
        }
        
        ts.move(eOffset);
        
        if (!ts.moveNext() && !ts.movePrevious()){
            return;
        }
        
        Token<? extends JsTokenId> token = null;
        JsTokenId tokenId;
        //find the begining of the object literal
        int balance = 1;
        while (ts.movePrevious() && balance > 0) {
            token = ts.token();
            tokenId = token.id();
            if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                balance++;
            } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                balance--;
            }
        }
        if (token == null || balance != 0) {
            return;
        }
        
        // now we should be at the beginning of the object literal. 
        token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
        tokenId = token.id();
        StringBuilder sb = new StringBuilder(token.text());
        while ((tokenId == JsTokenId.IDENTIFIER || tokenId == JsTokenId.OPERATOR_DOT) && ts.movePrevious()) {
            token = ts.token(); tokenId = token.id();
            if (tokenId == JsTokenId.OPERATOR_DOT) {
                sb.insert(0, '.'); // NOI18N
            } else if (tokenId == JsTokenId.IDENTIFIER) {
                sb.insert(0, token.text());
            }
        }
        
        String fqn = sb.toString();
        Map<String, Collection<PropertyNameDataItem>> data = getPropertyNameData();
        if (fqn.startsWith(JQueryUtils.JQUERY$)) { // NOI18N
            fqn = fqn.replace(JQueryUtils.JQUERY$, JQueryUtils.JQUERY); //NOI18N
        }
        Collection<PropertyNameDataItem> items = data.get(fqn);
        int anchorOffset = ccContext.getParserResult().getSnapshot().getOriginalOffset(eOffset) - ccContext.getPrefix().length();
        if (items != null) {
            boolean addComma = addComma(ts, eOffset);
            for (PropertyNameDataItem item : items) {
                if (item.getName().startsWith(prefix)) {
                    result.add(JQueryCompletionItem.createPropertyNameItem(item, anchorOffset, addComma));
                }
            }
        }
    }

    private static synchronized Map<String, Collection<PropertyNameDataItem>> getPropertyNameData() {
        return PropertyNameDataLoader.getData(getPropertyNameDataFile());
    }
    
    private static synchronized File getPropertyNameDataFile() {
        if (propertyNameFile == null) {
            propertyNameFile = InstalledFileLocator.getDefault().locate(PROPERTY_NAME_FILE_LOCATION, "org.netbeans.modules.javascript2.jquery", false); //NOI18N
        }
        return propertyNameFile;
    }

    private boolean addComma(TokenSequence<? extends JsTokenId> ts, int eOffset) {
        // we know that we are at the position, where the name of property is entered
        ts.move(eOffset);
        if (ts.moveNext()) {
            // we are looking for ',' -> don't add comma, is already there
            // ':' or an identifier -> need to add comma, next expression is new property definition
            // '}' -> end of object literal object definition -> no need comma there
            Token<? extends JsTokenId>token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
            if (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.STRING_BEGIN) {
                return true;
            } 
        }
        return false;
    }
    
    private enum SelectorKind {
        TAG, TAG_ATTRIBUTE, CLASS, ID, TAG_ATTRIBUTE_COMPARATION, AFTER_COLON
    }
    
    private static class SelectorContext {
        String prefix;
        Collection<SelectorKind> kinds;
        int prefixIndex;

        public SelectorContext(String prefix, int prefixIndex, Collection<SelectorKind> kinds) {
            this.prefix = prefix;
            this.kinds = kinds;
            this.prefixIndex = prefixIndex;
        }
        
    }
    
    private static HashMap<String, List<SelectorKind>> contextMap = new HashMap<String, List<SelectorKind>>();    
    private static Collection< SelectorItem> afterColonList = Collections.emptyList();
    
    private void fillContextMap() {
        contextMap.put(" (", Arrays.asList(SelectorKind.TAG, SelectorKind.ID, SelectorKind.CLASS, SelectorKind.AFTER_COLON));
        contextMap.put("#", Arrays.asList(SelectorKind.ID));
        contextMap.put(".", Arrays.asList(SelectorKind.CLASS));
        contextMap.put("[", Arrays.asList(SelectorKind.TAG_ATTRIBUTE));
        contextMap.put(":", Arrays.asList(SelectorKind.AFTER_COLON));
    }
    
    private void fillAfterColonList() {
        if(getJQueryAPIFile() != null) {
            afterColonList = SelectorsLoader.getSelectors(getJQueryAPIFile());
        }
    }

    private synchronized File getJQueryAPIFile() {
        if (jQueryApiFile == null) {
            jQueryApiFile = InstalledFileLocator.getDefault().locate(HELP_LOCATION, "org.netbeans.modules.javascript2.jquery", false); //NOI18N
        }
        return jQueryApiFile;
    }
  
    private SelectorContext findSelectorContext(String text) {
        int index = text.length() - 1;
        StringBuilder prefix = new StringBuilder();
        while (index > -1) {
            char c = text.charAt(index);
            switch (c) {
                case ' ':
                case '(':
                case ',':
                    return new SelectorContext(prefix.toString(), index, Arrays.asList(SelectorKind.TAG, SelectorKind.ID, SelectorKind.CLASS));
                case '#':
                    return new SelectorContext(prefix.toString(), index, Arrays.asList(SelectorKind.ID));
                case '.':
                    return new SelectorContext(prefix.toString(), index, Arrays.asList(SelectorKind.CLASS));
                case '[':
                    return new SelectorContext(prefix.toString(), index, Arrays.asList(SelectorKind.TAG_ATTRIBUTE));
                case ':':
                    return new SelectorContext(prefix.toString(), index, Arrays.asList(SelectorKind.AFTER_COLON));
            }
            prefix.insert(0, c);
            index--;
        }
        if (index < 0) {
            return new SelectorContext(prefix.toString(), 0, Arrays.asList(SelectorKind.TAG, SelectorKind.ID, SelectorKind.CLASS, SelectorKind.AFTER_COLON));
        }
        return null;
    }
    
    private void addSelectors(final List<CompletionProposal> result, final ParserResult parserResult, final String prefix, final int offset, CodeCompletionContext ccContex) {
        /*
         * basic selectors: 
         * $(document); // Activate jQuery for object
         * $('#mydiv') // Element with ID "mydiv" 
         * $('p.first') // P tags with class first. 
         * $('p[title="Hello"]') // P tags with title "Hello"
         * $('p[title^="H"]') // P tags title starting with H
         */
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return;
        }
        ts.move(offset);
        if (!(ts.moveNext() && ts.movePrevious())) {
            return;
        }
        String wrapup = ""; //NOI18N
        String prefixText = prefix;
        int anchorOffsetDelta = prefix.length() - ccContex.getPrefix().length();
        if (!prefixText.isEmpty() && anchorOffsetDelta > 0) {
            char ch = prefixText.charAt(anchorOffsetDelta - 1);
            if (ch == '#' || ch == '.' || ch == ':') {
                anchorOffsetDelta--;
            }
        }
//        if (!(ts.token().id() == JsTokenId.STRING || ts.token().id() == JsTokenId.STRING_END || ts.token().id() == JsTokenId.STRING_BEGIN)) {
//            wrapup = "'"; //NOI18N
//            if (ts.token().id() == JsTokenId.IDENTIFIER) {
//                ts.movePrevious();
//            }
//            if(ts.token().id() == JsTokenId.OPERATOR_COLON) {
//                prefixText = ":" + prefixText; //NOI18N
//                anchorOffsetDelta = prefix.isEmpty() ? 0 : -1;
//            } else if (ts.token().id() == JsTokenId.OPERATOR_DOT) {
//                prefixText = "." + prefixText; //NOI18N
//                anchorOffsetDelta = prefix.isEmpty() ? 0 : -1;
//            } else {
//                anchorOffsetDelta = 0;
//            }
////            if (prefix.isEmpty()) {
////                anchorOffsetDelta = 1;
////            }
//
//
//        }
        
        
        if(contextMap.isEmpty()) {
            fillContextMap();
        }
        
        SelectorContext context = findSelectorContext(prefixText);
        
        if (context != null) {
            int docOffset = parserResult.getSnapshot().getOriginalOffset(offset) - prefixText.length();
            int anchorOffset = docOffset + anchorOffsetDelta;
            for (SelectorKind selectorKind : context.kinds) {
                switch (selectorKind) {
                    case TAG:
                        Collection<HtmlTag> tags = getHtmlTags(context.prefix);
                        for (HtmlTag htmlTag : tags) {
                            result.add(JQueryCompletionItem.create(htmlTag, anchorOffset, wrapup));
                        }
                        break;
                    case TAG_ATTRIBUTE:
                        // provide attributes
                        int index = prefix.lastIndexOf('[');
                        String tagName = "";
                        if (index > 0) {
                            tagName = prefix.substring(0, index);
                        }
                        
                        if (!tagName.isEmpty()) {
                            index = tagName.lastIndexOf(' ');
                            if (index > -1) {
                                tagName = tagName.substring(index + 1);
                            }
                            index = tagName.indexOf('.');
                            if (index > -1) {
                                tagName = tagName.substring(0, index);
                            }
                            index = tagName.indexOf('#');
                            if (index > -1) {
                                tagName = tagName.substring(0, index);
                            }
                            index = tagName.lastIndexOf('(');
                            if (index > -1) {
                                tagName = tagName.substring(index + 1);
                            }
                        }
                        
                        
                        
                        if (!tagName.isEmpty() && (tagName.charAt(0) == '.' || tagName.charAt(0) == '#' || tagName.charAt(0) == '(')) {
                            if (ts.token().id() == JsTokenId.STRING_BEGIN) {
                                ts.moveNext();
                            }
                            if (ts.token().id() == JsTokenId.STRING) {
                                String value = ts.token().text().toString();
                                index = value.indexOf(prefix);
                                if (index > -1) {
                                    tagName = value.substring(0, index);
                                    index--;
                                    while (index > -1) {
                                        char ch = tagName.charAt(index);
                                        if (ch == '.' || ch == '#' || ch == ',' || ch == '=' 
                                                || ch == '"' || ch == '\'' || ch == '[' 
                                                || ch == ']' || ch == '(' || ch == ')' 
                                                || ch == ':') {
                                            break;
                                        }
                                        index --;
                                    }
                                    if (index > -1) {
                                        tagName = tagName.substring(index + 1);
                                    }
                                } else {
                                    tagName = "";
                                }
                            } else {
                                tagName = "";
                            }
                        }
                        String attributePrefix = prefix.substring(context.prefixIndex + 1);
                        anchorOffset = docOffset + prefix.length() - context.prefix.length();
                        Collection<HtmlTagAttribute> attributes = getHtmlAttributes(tagName, attributePrefix);
                        for (HtmlTagAttribute htmlTagAttribute : attributes) {
                            result.add(JQueryCompletionItem.create(htmlTagAttribute, anchorOffset, ""));
                        }
                        break;
                    case ID:
                        Collection<String> tagIds = getTagIds(context.prefix, parserResult);
                        for (String tagId : tagIds) {
                            result.add(JQueryCompletionItem.createCSSItem("#" + tagId, anchorOffset, wrapup));
                        }
                        break;
                    case CLASS:
                        Collection<String> classes = getCSSClasses(context.prefix, parserResult);
                        for (String cl : classes) {
                            result.add(JQueryCompletionItem.createCSSItem("." + cl, anchorOffset, wrapup));
                        }
                        break;
                    case AFTER_COLON:
                        if(afterColonList.isEmpty()) {
                            fillAfterColonList();
                        }
                        for (SelectorItem selector : afterColonList) {
                            if (selector.getDisplayText().startsWith(context.prefix)) {
                                result.add(JQueryCompletionItem.createJQueryItem(":" + selector.getDisplayText(), anchorOffset, wrapup, selector.getInsertTemplate()));
                            }
                        }
                        break;
                }
            }
        }
    }

    private Collection<String> getTagIds(String tagIdPrefix, ParserResult parserResult) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unique = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findIdsByPrefix(tagIdPrefix);

            for (Collection<String> ids : findIdsByPrefix.values()) {
                for (String id : ids) {
                    unique.add(id);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return unique;
    }

    private Collection<String> getCSSClasses(String classPrefix, ParserResult parserResult) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        if(fo == null) {
            return Collections.emptyList();
        }
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unique = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findClassesByPrefix(classPrefix);

            for (Collection<String> ids : findIdsByPrefix.values()) {
                for (String id : ids) {
                    unique.add(id);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return unique;

    }

    private Collection<HtmlTagAttribute> getHtmlAttributes(final String tagName, final String prefix) {
        Collection<HtmlTagAttribute> result = Collections.<HtmlTagAttribute>emptyList();
        HtmlModel htmlModel = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        HtmlTag htmlTag = htmlModel.getTag(tagName);
        if (htmlTag != null) {
            if (prefix.isEmpty()) {
                if (tagName.isEmpty()) {
                    result = getAllAttributes(htmlModel);
                } else {
                    result = htmlTag.getAttributes();
                }
            } else {
                Collection<HtmlTagAttribute> attributes = htmlTag.getAttributes();
                if (tagName.isEmpty() || htmlTag.getTagClass() == HtmlTagType.UNKNOWN) {
                    attributes = getAllAttributes(htmlModel);
                }
                result = new ArrayList<HtmlTagAttribute>();
                for (HtmlTagAttribute htmlTagAttribute : attributes) {
                    if(htmlTagAttribute.getName().startsWith(prefix)) {
                        result.add(htmlTagAttribute);
                    }
                }
            }
        }
        return result;
    }

    private Collection<HtmlTag> getHtmlTags(String prefix) {
        Collection<HtmlTag> result;
        HtmlModel htmlModel = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        Collection<HtmlTag> allTags = htmlModel.getAllTags();
        if (prefix.isEmpty()) {
            result = allTags;
        } else {
            result = new ArrayList<HtmlTag>();
            for (HtmlTag htmlTag : allTags) {
                if (htmlTag.getName().startsWith(prefix)) {
                    result.add(htmlTag);
                }
            }
        }
        return result;
    }

    private synchronized Collection<HtmlTagAttribute> getAllAttributes(HtmlModel htmlModel) {
        if (allAttributes == null) {
            initAllAttributes(htmlModel);
        }
        return allAttributes;
    }

    private synchronized void initAllAttributes(HtmlModel htmlModel) {
        assert allAttributes == null;
        Map<String, HtmlTagAttribute> result = new HashMap<String, HtmlTagAttribute>();
        for (HtmlTag htmlTag : htmlModel.getAllTags()) {
            for (HtmlTagAttribute htmlTagAttribute : htmlTag.getAttributes()) {
                // attributes can probably differ per tag so we can just offer some of them,
                // at least for the CC purposes it should be complete list of attributes for unknown tag
                if (!result.containsKey(htmlTagAttribute.getName())) {
                    result.put(htmlTagAttribute.getName(), htmlTagAttribute);
                }
            }
        }
        allAttributes = result.values();
    }
}
