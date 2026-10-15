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
package org.netbeans.modules.html.editor.gsf;

import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.EntryHandle;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.HtmlExtensions;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.modules.web.common.api.WebPageMetadata;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * csl.api to HtmlExtension bridge
 *
 * @author marekfukala
 */
public class HtmlDeclarationFinder implements DeclarationFinder {

    private static final Logger LOG = Logger.getLogger(HtmlDeclarationFinder.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(HtmlDeclarationFinder.class);

    /**
     * Cache of the {@link Document} to mimeType obtained from {@link WebPageMetadata#getMetadata(org.openide.util.Lookup)
     * }. The cache is updated by tasks triggered from {@link #getReferenceSpan(javax.swing.text.Document, int)
     * }.
     */
    private static final Map<Document, String> DOC_TO_WEB_MIMETYPE_CACHE
            = new WeakHashMap<>();
    
    /**
     * {@link Document} to {@link RequestProcessor.Task} map.
     */
    private static final Map<Document, Reference<Task>> DOC_TO_UPDATE_TASK_MAP
            = new WeakHashMap<>();

    /**
     * Task which updates the {@link #DOC_TO_WEB_MIMETYPE_CACHE} cache.
     */
    private static final class DocumentMimeTypeCacheUpdateTask implements Runnable {

        private final Document document;

        public DocumentMimeTypeCacheUpdateTask(Document document) {
            this.document = document;
        }

        @Override
        public void run() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(document)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        ResultIterator htmlRi = WebUtils.getResultIterator(resultIterator, "text/html");
                        if (htmlRi != null) {
                            HtmlParserResult result = (HtmlParserResult) htmlRi.getParserResult();
                            if(result != null) {
                                String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
                                DOC_TO_WEB_MIMETYPE_CACHE.put(document, sourceMimetype);
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    };

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        HtmlParserResult result = (HtmlParserResult) info;
        DeclarationLocation loc = findCoreHtmlDeclaration(info, caretOffset);
        if (loc != null) {
            return loc;
        }
        String sourceMimetype = WebPageMetadata.getContentMimeType(result, true);
        for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(sourceMimetype)) {
            loc = ext.findDeclaration(info, caretOffset);
            if (loc != null) {
                return loc;
            }
        }
        return DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final AtomicReference<OffsetRange> result_ref = new AtomicReference<>(OffsetRange.NONE);
        doc.render(new Runnable() {
            @Override
            public void run() {
                OffsetRange range = getCoreHtmlReferenceSpan(doc, caretOffset);
                if (range != null) {
                    result_ref.set(range);
                    return;
                }
                //Issue 233671>>>
                //Wrong HtmlExtension triggered for facelets files
                //
                //We need to obtain the document's mimetype from WebPageMetadata, 
                //but we need it quickly as we are in EDT. No parsing task running
                //is allowed either.
                Reference<Task> taskReference = DOC_TO_UPDATE_TASK_MAP.get(doc);
                Task task = taskReference == null ? null : taskReference.get();
                if(task == null) {
                    task = RP.create(new DocumentMimeTypeCacheUpdateTask(doc));
                    DOC_TO_UPDATE_TASK_MAP.put(doc, new WeakReference(task));
                }
                
                String mimeType = DOC_TO_WEB_MIMETYPE_CACHE.get(doc);
                if (mimeType == null) {
                    //no cached result -- we need to update the cache,
                    //but lazily.
                    task.schedule(0);

                    //well for a while, until the task finishes and updates the cache,
                    //we will be malfunctioning, but noone except Vlada will notice :-)
                    mimeType = NbEditorUtilities.getMimeType(doc);
                } else {
                    //ok, we obtained the result from the cache, but what if the 
                    //mimetype from the WebPageMetadata has changed and the cache entry
                    //won't be GCed? 
                    //=>post the update task with some longer delay
                    task.schedule(5000); //5 seconds -- will reschedule if called more times in row
                }
                //<<<Issue 233671

                //html extensions
                for (HtmlExtension ext : HtmlExtensions.getRegisteredExtensions(mimeType)) {
                    range = ext.getReferenceSpan(doc, caretOffset);
                    if (range != null && range != OffsetRange.NONE) {
                        result_ref.set(range);
                        return;
                    }
                }
            }
        });
        return result_ref.get();
    }

    private OffsetRange getCoreHtmlReferenceSpan(Document doc, int caretOffset) {
        TokenHierarchy hi = TokenHierarchy.get(doc);
        final TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(hi, caretOffset);
        if (ts == null) {
            return null;
        }

        //tag attribute value hyperlinking
        if (ts.token().id() == HTMLTokenId.VALUE) {
            return new AttributeValueAction<OffsetRange>(hi, ts) {

                @Override
                public OffsetRange resolve() {
                    if (tagName != null && attrName != null) {
                        ValueCompletion<HtmlCompletionItem> support = AttrValuesCompletion.getSupport(tagName, attrName);
                        if (AttrValuesCompletion.FILE_NAME_SUPPORT == support) {
                            //some file to hyperlink to
                            return valueRange;
                        }
                    }
                    return null;
                }
            }.run();

        } else if (ts.token().id() == HTMLTokenId.VALUE_CSS) {
            //css class or id hyperlinking
            Object cssTokenType = ts.token().getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
            if(cssTokenType != null) {

                OffsetRange offsetRange = getPointedRange(ts, hi, caretOffset);
                if(offsetRange != null) {
                    return offsetRange;
                }
            }
        }

        return null;
    }

    private OffsetRange getPointedRange(TokenSequence<HTMLTokenId> ts, TokenHierarchy hi, int caretOffset) {
        OffsetRange offsetRange = null;
        List<? extends Token<HTMLTokenId>> parts = ts.token().joinedParts();
        if(parts == null) {
            parts = Collections.singletonList(ts.token());
        }
        for(Token<HTMLTokenId> partToken: parts) {
            int tokenOffset = partToken.offset(hi);
            int tokenLength = partToken.length();
            int offsetIntoToken = caretOffset - tokenOffset;
            if(offsetIntoToken > 0 && offsetIntoToken < tokenLength) {
                CharSequence tokenText = partToken.text();
                int startToken = offsetIntoToken;
                int endToken = offsetIntoToken;
                char currentChar = tokenText.charAt(offsetIntoToken);
                if(currentChar != '"' && currentChar != '\'' && !Character.isWhitespace(currentChar)) {
                    do {
                        currentChar = tokenText.charAt(startToken - 1);
                        if(currentChar == '"' || currentChar == '\'' || Character.isWhitespace(currentChar)) {
                            break;
                        }
                        startToken--;
                    } while(startToken > 0);
                    do {
                        currentChar = tokenText.charAt(endToken + 1);
                        if(currentChar == '"' || currentChar == '\'' || Character.isWhitespace(currentChar)) {
                            break;
                        }
                        endToken++;
                    } while(endToken < tokenLength);
                    offsetRange = new OffsetRange(tokenOffset + startToken, tokenOffset + endToken + 1);
                }
            }
        }
        return offsetRange;
    }

    private DeclarationLocation findCoreHtmlDeclaration(final ParserResult info, final int caretOffset) {
        final FileObject file = info.getSnapshot().getSource().getFileObject();
        TokenHierarchy hi = info.getSnapshot().getTokenHierarchy();
        final TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return null;
        }
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if (astCaretOffset == -1) {
            return null;
        }

        ts.move(astCaretOffset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        //tag attribute value hyperlinking
        if (ts.token().id() == HTMLTokenId.VALUE) {
            return new AttributeValueAction<DeclarationLocation>(hi, ts) {

                @Override
                public DeclarationLocation resolve() {
                    if (tagName != null && attrName != null) {
                        ValueCompletion<HtmlCompletionItem> support = AttrValuesCompletion.getSupport(tagName, attrName);
                        if (AttrValuesCompletion.FILE_NAME_SUPPORT == support) {
                            //some file to hyperlink to
                            FileObject resolved = WebUtils.resolve(info.getSnapshot().getSource().getFileObject(), unquotedValue);
                            if (resolved != null) {
                                return new DeclarationLocation(resolved, 0);
                            }
                        }
                    }
                    return null;
                }
            }.run();

        } else if (ts.token().id() == HTMLTokenId.VALUE_CSS) {
            //css class or id hyperlinking

            //I need to somehow determine the type of the selector - whether it's
            //a class or an id. There are two (bad) ways to do this:
            //1. either get the original html token containing the meta info
            //2. or parse the file and get css parser result for given offset
            //
            //both may cause some offset inconsistencies because of the lack of locking
            //
            //#1 seems to be at least faster
            final Document doc = info.getSnapshot().getSource().getDocument(true);
            final AtomicReference<RefactoringElementType> type = new AtomicReference<>();
            final AtomicReference<String> unquotedValue = new AtomicReference<>();
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenSequence ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
                    if (ts != null && ts.token() != null) {
                        //seems to be valid and properly positioned
                        Token<HTMLTokenId> valueToken = ts.token();
                        if (valueToken.id() == HTMLTokenId.VALUE_CSS) {
                            try {
                                //the value_css token contains a metainfo about the type of its css embedding
                                String cssTokenType = (String) valueToken.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                                if (cssTokenType == null) {
                                    return;
                                }
                                OffsetRange offsetRange = getPointedRange(ts, hi, caretOffset);
                                unquotedValue.set(doc.getText(offsetRange.getStart(), offsetRange.getLength()));
                                switch (cssTokenType) {
                                    case HTMLTokenId.VALUE_CSS_TOKEN_TYPE_CLASS:
                                        //class selector
                                        type.set(RefactoringElementType.CLASS);
                                        break;
                                    case HTMLTokenId.VALUE_CSS_TOKEN_TYPE_ID:
                                        // instances comparison is ok here!
                                        //id selector
                                        type.set(RefactoringElementType.ID);
                                        break;
                                    default:
                                        assert false;
                                        break;
                                }
                            } catch (BadLocationException ex) {
                                LOG.log(Level.WARNING, "Failed to get text for declaration", ex);
                            }
                        }
                    }
                }
            });

            if (unquotedValue.get() == null || type.get() == null) {
                return null;
            }

            Map<FileObject, Collection<EntryHandle>> occurances = CssRefactoring.findAllOccurances(unquotedValue.get(), type.get(), file, true); //non virtual element only - this means only css declarations, not usages in html code
            if (occurances == null) {
                return null;
            }

            DeclarationLocation dl = null;
            for (Map.Entry<FileObject, Collection<EntryHandle>> entry : occurances.entrySet()) {
                FileObject f = entry.getKey();
                Collection<EntryHandle> entries = entry.getValue();
                for (EntryHandle entryHandle : entries) {
                    //grrr, the main declarationlocation must be also added to the alternatives
                    //if there are more than one
                    DeclarationLocation dloc = new DeclarationLocation(f, entryHandle.entry().getDocumentRange().getStart());
                    if (dl == null) {
                        //ugly DeclarationLocation alternatives handling workaround - one of the
                        //locations simply must be "main"!!!
                        dl = dloc;
                    }
                    HtmlDeclarationFinder.AlternativeLocation aloc = new HtmlDeclarationFinder.AlternativeLocationImpl(dloc, entryHandle, type.get());
                    dl.addAlternative(aloc);
                }
            }

            //and finally if there was just one entry, remove the "alternative"
            if (dl != null && dl.getAlternativeLocations().size() == 1) {
                dl.getAlternativeLocations().clear();
            }

            return dl;

        }

        return null;
    }

    private abstract class AttributeValueAction<T> {

        private final TokenHierarchy hi;
        private final TokenSequence<HTMLTokenId> ts;
        protected String tagName, attrName, unquotedValue;
        protected OffsetRange valueRange;

        public AttributeValueAction(TokenHierarchy hi, TokenSequence<HTMLTokenId> ts) {
            this.hi = hi;
            this.ts = ts;
        }

        public abstract T resolve();

        public T run() {
            parseSquence();
            return resolve();
        }

        private void parseSquence() {
            //find attribute name
            int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
            unquotedValue = WebUtils.unquotedValue(ts.token().text().toString());

            Token<HTMLTokenId> token = ts.token();
            List<? extends Token<HTMLTokenId>> tokenParts = token.joinedParts();
            if (tokenParts == null) {
                //continuos token
                valueRange = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
            } else {
                //joined token
                //the range is first token part start to last token part end
                Token<HTMLTokenId> first = tokenParts.get(0);
                Token<HTMLTokenId> last = tokenParts.get(tokenParts.size() - 1);

                valueRange = new OffsetRange(first.offset(hi), last.offset(hi) + last.length());
            }

            while (ts.movePrevious()) {
                HTMLTokenId id = ts.token().id();
                if (id == HTMLTokenId.ARGUMENT && attrName == null) {
                    attrName = ts.token().text().toString();
                } else if (id == HTMLTokenId.TAG_OPEN) {
                    tagName = ts.token().text().toString();
                    break;
                } else if (id == HTMLTokenId.TAG_OPEN_SYMBOL || id == HTMLTokenId.TAG_CLOSE_SYMBOL || id == HTMLTokenId.TEXT) {
                    break;
                }
            }
        }
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

        private final DeclarationLocation location;
        private final EntryHandle entryHandle;
        private final RefactoringElementType type;
        
        private static final int SELECTOR_TEXT_MAX_LENGTH = 50;

        private static final Color SELECTOR_COLOR = new Color(0x00, 0x7c, 0x00);

        public AlternativeLocationImpl(DeclarationLocation location, EntryHandle entry, RefactoringElementType type) {
            this.location = location;
            this.entryHandle = entry;
            this.type = type;
        }

        @Override
        public ElementHandle getElement() {
            return CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON;
        }

        private static String hexColorCode(Color c) {
            Color tweakedToLookAndFeel = LFCustoms.shiftColor(c);
            return Integer.toHexString(tweakedToLookAndFeel.getRGB()).substring(2);
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder b = new StringBuilder();
            //colorize the 'current line text' a bit
            //find out if there's the opening curly bracket
            String lineText = entryHandle.entry().getLineText().toString();
            assert lineText != null;

            //split the text to three parts: the element text itself, its prefix and postfix
            //then render the element test in bold
            String elementTextPrefix;
            switch (type) {
                case CLASS:
                    elementTextPrefix = "."; //NOI18N
                    break;
                case ID:
                    elementTextPrefix = "#"; //NOI18N
                    break;
                default:
                    elementTextPrefix = "";
            }
            String elementText = elementTextPrefix + entryHandle.entry().getName();

            String prefix = "";
            String postfix = "";
            //strip the line to the body start
            int elementIndex = lineText.indexOf(elementText);
            if (elementIndex >= 0) {
                //find the closest opening curly bracket or NL forward
                int to;
                for (to = elementIndex; to < lineText.length(); to++) {
                    char c = lineText.charAt(to);
                    if (c == '{' || c == '\n') {
                        break;
                    }
                }
                //now find nearest closing curly bracket or newline backward
                int from;
                for (from = elementIndex; from >= 0; from--) {
                    char ch = lineText.charAt(from);
                    if (ch == '}' || ch == '\n') {
                        break;
                    }
                }

                prefix = lineText.substring(from + 1, elementIndex).trim();
                postfix = lineText.substring(elementIndex + elementText.length(), to).trim();

                //now strip the prefix and postfix so the whole text is not longer than SELECTOR_TEXT_MAX_LENGTH
                int overlap = (prefix.length() + elementText.length() + postfix.length()) - SELECTOR_TEXT_MAX_LENGTH;
                if (overlap > 0) {
                    //strip
                    int stripFromPrefix = Math.min(overlap / 2, prefix.length());
                    prefix = ".." + prefix.substring(stripFromPrefix);
                    int stripFromPostfix = Math.min(overlap - stripFromPrefix, postfix.length());
                    postfix = postfix.substring(0, postfix.length() - stripFromPostfix) + "..";
                }
            }

            b.append("<font color=");//NOI18N
            b.append(hexColorCode(SELECTOR_COLOR));
            b.append(">");
            b.append(prefix);
            b.append(' '); //NOI18N
            b.append("<b>"); //NOI18N
            b.append(elementText);
            b.append("</b>"); //NOI18N
            b.append(' '); //NOI18N
            b.append(postfix);
            b.append("</font> in "); //NOI18N

            //add a link to the file relative to the web root
            FileObject file = location.getFileObject();
            FileObject pathRoot = ProjectWebRootQuery.getWebRoot(file);

            String path = null;
            String resolveTo = null;
            if (file != null) {
                if (pathRoot != null) {
                    path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                }
                if (path == null) {
                    //the file cannot be resolved relatively to the webroot or no webroot found
                    //try to resolve relative path to the project's root folder
                    Project project = FileOwnerQuery.getOwner(file);
                    if (project != null) {
                        pathRoot = project.getProjectDirectory();
                        path = FileUtil.getRelativePath(pathRoot, file); //this may also return null
                        if (path != null) {
                            resolveTo = "${project.home}/"; //NOI18N
                        }
                    }
                }

                if (path == null) {
                    //if everything fails, just use the absolute path
                    path = file.getPath();
                }
            }

            if (resolveTo != null) {
                b.append("<i>"); //NOI18N
                b.append(resolveTo);
                b.append("</i>"); //NOI18N
            }
            b.append(path == null ? "???" : path); //NOI18N
            int lineOffset = entryHandle.entry().getLineOffset();
            if (lineOffset != -1) {
                b.append(":"); //NOI18N
                b.append(lineOffset + 1); //line offsets are counted from zero, but in editor lines starts with one.
            }
            if (!entryHandle.isRelatedEntry()) {
                b.append(" <font color=ff0000>(");
                b.append(NbBundle.getMessage(HtmlDeclarationFinder.class, "MSG_Unrelated"));
                b.append(")</font>");
            }
            return b.toString();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            FileObject file = loc.getLocation().getFileObject();
            return new StringBuilder().append(loc.getLocation().getOffset()) //offset
                    .append(file == null ? "???" : file.getPath()).toString(); //filename //NOI18N
        }
    }
    //useless class just because we need to put something into the AlternativeLocation to be
    //able to get some icon from it
    private static final CssSelectorElementHandle CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON = new CssSelectorElementHandle();

    private static class CssSelectorElementHandle implements ElementHandle {

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }
    }
}
