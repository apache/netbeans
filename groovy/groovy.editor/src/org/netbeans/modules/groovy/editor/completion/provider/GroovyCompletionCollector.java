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
package org.netbeans.modules.groovy.editor.completion.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lsp.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementKind;
import static org.netbeans.modules.csl.api.ElementKind.METHOD;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.ConstructorItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.MetaMethodItem;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.NamedParameter;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem.TypeItem;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.api.parser.GroovyLanguage;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.editor.completion.provider.GroovyCompletionImpl.CompletionImplResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 *
 * @author sdedic
 */
@MimeRegistration(mimeType = GroovyLanguage.GROOVY_MIME_TYPE, service = CompletionCollector.class)
public class GroovyCompletionCollector implements CompletionCollector {
    private final Lookup lkp;

    private GroovyCompletionImpl cService;
    
    public GroovyCompletionCollector() {
        this(Lookup.getDefault());
    }

    public GroovyCompletionCollector(Lookup lkp) {
        this.lkp = lkp;
    }
    
    private GroovyCompletionImpl impl() {
        if (cService == null) {
            cService = lkp.lookup(GroovyCompletionImpl.class);
        }
        return cService;
    }
    
    @Override
    public boolean collectCompletions(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) {
        
        try {
            CompletionTask task = collectCompletions2(doc, offset, context, consumer);
            if (task == null) {
                return true;
            }
            return task.complete;
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return true;
        }
    }
    
    public CompletionTask collectCompletions2(Document doc, int offset, Completion.Context context, Consumer<Completion> consumer) throws ParseException {
        Source src = Source.create(doc);
        if (src == null) {
            return null;
        }
        CompletionTask task = new CompletionTask(offset, QueryType.COMPLETION, context, consumer);

        ParserManager.parse(Collections.singleton(src), task);
        return task;
    }
    
    // the ORDER_* constnats values follow those of JavaComplectionCollector for non-smart
    // items.
    private static final int ORDER_CONSTRUCTOR = 1550;
    private static final int ORDER_METHOD = 1500;
    private static final int ORDER_META_METHOD = 1650;

    private static int ORDER_KEYWORD = 1670;
    private static int ORDER_TYPE = 1800;
    private static int ORDER_NAMED_PARAMETER = 1300;
    private static int ORDER_FIELD = 1200;
    private static int ORDER_FIELD_DYNAMIC = 1250;
    private static int ORDER_LOCAL_VAR = 1100;
    
    private static int ORDER_PACKAGE = 1900;
    
    
    // fpr testing only
    public class CompletionTask extends UserTask {
        final int offset;
        final Consumer<Completion> consumer;
        final Completion.Context lspContext;
        final QueryType queryType;
        CompletionRequestContext cslContext;
        GroovyParserResult groovyResult;
        boolean complete = true;
        CompletionImplResult groovyCompletion;
        final List<CompletionProposal> proposals = new ArrayList<>();

        public CompletionTask(int offset, QueryType queryType, Completion.Context context, Consumer<Completion> consumer) {
            this.offset = offset;
            this.lspContext = context;
            this.consumer = consumer;
            this.queryType = queryType;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            if (GroovyLanguage.GROOVY_MIME_TYPE.equals(resultIterator.getSnapshot().getMimeType())) {
                process(resultIterator.getParserResult());
            }
            for (Embedding e : resultIterator.getEmbeddings()) {
                if (e.containsOriginalOffset(offset)) {
                    run(resultIterator.getResultIterator(e));
                }
            }
        }
        
        public List<CompletionProposal> getOriginalProposals() {
            return proposals;
        }
        
        void process(Parser.Result r) {
            if (!(r instanceof GroovyParserResult)) {
                return;
            }
            groovyResult = (GroovyParserResult)r;
            // CSL context for completion
            cslContext = new CompletionRequestContext(offset,  queryType, groovyResult, true);
            
            proposals.addAll(impl().makeProposals(cslContext).getProposals());
            for (CompletionProposal cp : proposals) {
                Completion.Kind k = lspCompletionKind(cp.getKind());
                if (k == null) {
                    // unknown / unrepresentable completion kind, ignore.
                    continue;
                }
                builder = CompletionCollector.newBuilder(cp.getName()).kind(k);
                
                Builder b = buildCompletion(cp);
                if (b != null) {
                    b.documentation(() -> getDocumentation(cp));
                    consumer.accept(b.build());
                }
            }
        }
        
        String getDocumentation(CompletionProposal cp) {
            AtomicReference<String> doc = new AtomicReference<>();
            Source s = groovyResult.getSnapshot().getSource();
            if (s == null) {
                return null;
            }
            GroovyCompletionImpl impl = Lookup.getDefault().lookup(GroovyCompletionImpl.class);
            if (impl == null) {
                return null;
            }
            try {
                ParserManager.parse(Collections.singleton(s), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Parser.Result r = resultIterator.getParserResult();
                        if (r instanceof GroovyParserResult) {
                            GroovyParserResult gpr = (GroovyParserResult)r;
                            Documentation d = impl.documentElement(gpr, cp.getElement(), () -> false);
                            if (d != null) {
                                doc.set(d.getContent());
                                return;
                            }
                        }
                        for (Embedding e : resultIterator.getEmbeddings()) {
                            if (e.containsOriginalOffset(cp.getAnchorOffset())) {
                                run(resultIterator.getResultIterator(e));
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            return doc.get();
        }
        
        /**
         * Temporary value, recreated for each processed item.
         */
        CompletionCollector.Builder builder;
        
        /*
        Handled:
            JavaMethodItem
            ConstructorItem
            JavaFieldItem
            DynamicFieldItem    
            DynamicMethodItem  
            NamedParameter      -> property

            MetaMethodItem
        Defaults / literal:
            KeywordItem
            PackageItem         -> folder
            TypeItem
            LocalVarItem
            NewVarItem
            NewFieldItem
         */
        
        Builder  buildCompletion(CompletionProposal cp) {
            if (!(cp instanceof CompletionItem)) {
                return null;
            }
            CompletionItem ci = (CompletionItem)cp;
            StringBuilder sb = new StringBuilder(cp.getLhsHtml(nullFormatter()));
            String rhs = cp.getRhsHtml(nullFormatter());
            if (rhs != null && !rhs.isEmpty()) {
                sb.append(" : "); // NOI18N
                sb.append(rhs);
            }
            builder.label(sb.toString());
            builder.insertText(cp.getInsertPrefix());
            
            // override the default CSL > LSP type mapping, for specific cases
            if (cp instanceof ConstructorItem) {
                return nameAndParameters(ci);
            } else if (cp instanceof NamedParameter) {
                builder.kind(Completion.Kind.Property).
                        insertText(cp.getCustomInsertTemplate()).
                        sortText(String.format("%04d%s", ORDER_NAMED_PARAMETER, cp.getName()));
            } else if (cp instanceof CompletionItem.DynamicFieldItem) {
                builder.kind(Completion.Kind.Field);
            } else if (cp instanceof CompletionItem.TypeItem) {
                return buildType(ci);
            } else if (cp instanceof CompletionItem.KeywordItem) {
                return buildKeyword(ci);
            } else if (cp instanceof CompletionItem.PackageItem) {
                return buildPackage(ci);
            } else if (cp.getKind() == ElementKind.METHOD) {
                return nameAndParameters((CompletionItem)cp);
            } else if (cp.getKind() == ElementKind.FIELD) {
                return buildField(ci);
            } else if (cp.getKind() == ElementKind.VARIABLE) {
                return buildVariable(ci);
            }
            return builder;
        }
        
        Builder buildField(CompletionItem item) {
            int priority = ORDER_FIELD;
            if (item instanceof CompletionItem.DynamicFieldItem) {
                priority = ORDER_FIELD_DYNAMIC;
            }
            return builder.sortText(String.format("%04d%s", priority,  item.getName()));
        }
        
        Builder buildVariable(CompletionItem item) {
            return builder.sortText(String.format("%04d%s", ORDER_LOCAL_VAR,  item.getName()));
        }
        
        Builder buildType(CompletionItem item) {
            TypeItem ti = (TypeItem)item;
            return builder.sortText(String.format("%04d%s#%s", ORDER_TYPE, 
                    ti.getName(), ti.getFqn()));
        }
        
        Builder buildKeyword(CompletionItem item) {
            return builder.sortText(String.format("%04d%s", ORDER_KEYWORD, item.getName()));
        }
        
        Builder buildPackage(CompletionItem item) {
            return builder.sortText(String.format("%04d%s", ORDER_PACKAGE, item.getName()));
        }
        
        Builder buildExecutable(CompletionItem item, String simpleName, String sortParams, int cnt) {
            int priority = item.getKind() == METHOD ? ORDER_METHOD : ORDER_CONSTRUCTOR;
            if (item instanceof MetaMethodItem) {
                builder.kind(Completion.Kind.Function);
                priority = ORDER_META_METHOD;
            }
            return builder.sortText(String.format("%04d%s#%02d%s", priority, 
                    simpleName, cnt, sortParams.toString()));
        }

        Builder nameAndParameters(CompletionItem item) {
            Pair<String, List<MethodParameter>> paramsAndType = CompletionAccessor.instance().getParametersAndType(item);
            // FIXME: report return type.
            List<MethodParameter> params = paramsAndType.second();
            String n = item.getName();
            StringBuilder sortParams = new StringBuilder();
            sortParams.append("(");
            int paren = n.indexOf('(');
            if (paren > -1) {
                // constructor's name does not contain (), method's does.
                n = n.substring(0, paren);
            }
            if (params.isEmpty()) {
                return buildExecutable(item, n, sortParams.toString(), 0).
                        insertText(n + "()");
            }
            StringBuilder sb = new StringBuilder(n);
            sb.append("(");
            int cnt = 0;
            for (MethodParameter p : params) {
                if (cnt > 0) {
                    sb.append(", ");
                    sortParams.append("#");
                }
                cnt++;
                if (p.getName() == null) {
                    sb.append("${").append(cnt);
                } else {
                    sb.append("${").append(cnt).append(':').append(p.getName());
                }
                sb.append("}");
                sortParams.append(p.getType());
            }
            sb.append(")$0");
            return buildExecutable(item, n, sortParams.toString(), params.size()).
                    insertText(sb.toString()).insertTextFormat(Completion.TextFormat.Snippet);
        }
    }
    
    private static HtmlFormatter nullFormatter() {
        return new NullHtmlFormatter();
    }
    
    static class NullHtmlFormatter extends HtmlFormatter {
        StringBuilder sb = new StringBuilder();
        
        @Override
        public void reset() {
            sb = new StringBuilder();
        }

        static String stripHtml( String htmlText ) {
            if( null == htmlText )
                return null;
            return htmlText.replaceAll( "<[^>]*>", "" ) // NOI18N
                           .replace( "&nbsp;", " " ) // NOI18N
                           .trim();
        }
        
        @Override
        public void appendHtml(String html) {
            sb.append(stripHtml(html));
        }

        @Override
        public void appendText(String text, int fromInclusive, int toExclusive) {
            int l = toExclusive - fromInclusive;
            if (sb.length() + l < maxLength) {
                sb.append(text.subSequence(fromInclusive, toExclusive));
            } else {
                sb.append(text.subSequence(fromInclusive, toExclusive - (l - maxLength)));
                sb.append("...");
            }
        }

        @Override
        public void emphasis(boolean start) {
        }

        @Override
        public void name(ElementKind kind, boolean start) {
        }

        @Override
        public void parameters(boolean start) {
        }

        @Override
        public void active(boolean start) {
        }

        @Override
        public void type(boolean start) {
        }

        @Override
        public void deprecated(boolean start) {
        }

        @Override
        public String getText() {
            return sb.toString();
        }
    }
    
    static Completion.Kind lspCompletionKind(ElementKind cslKind) {
        switch (cslKind) {
            case CONSTRUCTOR:   return Completion.Kind.Constructor;
            case MODULE:        return Completion.Kind.Module;
            case PACKAGE:       return Completion.Kind.Folder;
            case CLASS:         return Completion.Kind.Class;
            case METHOD:        return Completion.Kind.Method;
            case FIELD:         return Completion.Kind.Field;
            case VARIABLE:      return Completion.Kind.Variable;
            case ATTRIBUTE:     return Completion.Kind.Variable;
            case CONSTANT:      return Completion.Kind.Constant;
            case KEYWORD:       return Completion.Kind.Keyword;
            case OTHER:         return Completion.Kind.Snippet;
            case PARAMETER:     return Completion.Kind.Variable;
            case GLOBAL:        return Completion.Kind.Variable;
            case PROPERTY:      return Completion.Kind.Property;

            case FILE:          return Completion.Kind.File;
            case INTERFACE:     return Completion.Kind.Interface;

            case TEST:
            case DB:
            case CALL:
            case TAG:
            case RULE:
            case ERROR:         
            default:
                return null;
            
        }
    }
    
    private static class CompletionRequestContext extends CodeCompletionContext {
        final ParserResult parserResult;
        final boolean prefixMatch;
        final QueryType queryType;
        
        int offset;
        String prefix;

        public CompletionRequestContext(int offset, QueryType queryType, ParserResult parserResult, boolean prefixMatch) {
            this.offset = offset;
            this.parserResult = parserResult;
            this.prefixMatch = prefixMatch;
            this.queryType = queryType;
            init();
        }
        
        void init() {
            final Source s = parserResult.getSnapshot().getSource();
            Document doc = s.getDocument (false);
            int length = doc != null ? doc.getLength() : (int)s.getFileObject().getSize();
            if (offset > length) {
                offset = length;
            }
            
            try {
                if (doc != null) {
                    int[] blk =
                        org.netbeans.editor.Utilities.getIdentifierBlock((BaseDocument)doc,
                            offset);

                    if (blk != null) {
                        int start = blk[0];

                        if (start < offset ) {
                            if (prefixMatch) {
                                prefix = doc.getText(start, offset - start);
                            } else {
                                prefix = doc.getText(start, blk[1]-start);
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        @Override
        public int getCaretOffset() {
            return offset;
        }

        @Override
        public ParserResult getParserResult() {
            return parserResult;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public CodeCompletionHandler.QueryType getQueryType() {
            return queryType;
        }

        @Override
        public boolean isPrefixMatch() {
            return prefixMatch;
        }

        @Override
        public boolean isCaseSensitive() {
            return GroovyCompletionCollector.isCaseSensitive();
        }
    }

    private static boolean caseSensitive = true;
    private static boolean autoPopup = true;
    private static boolean inited;

    private static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    private static class SettingsListener implements PreferenceChangeListener {

//        public void settingsChange(SettingsChangeEvent evt) {
//            setCaseSensitive(SettingsUtil.getBoolean(GsfEditorKitFactory.GsfEditorKit.class,
//                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
//                    ExtSettingsDefaults.defaultCompletionCaseSensitive));
//        }

        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey() == null || SimpleValueNames.COMPLETION_CASE_SENSITIVE.equals(evt.getKey())) {
                setCaseSensitive(Boolean.valueOf(evt.getNewValue()));
            } else if (SimpleValueNames.COMPLETION_AUTO_POPUP.equals(evt.getKey())) {
                setAutoPopup(Boolean.valueOf(evt.getNewValue()));
            }
        }
    }

    private static PreferenceChangeListener settingsListener = new SettingsListener();

    private static void setCaseSensitive(boolean b) {
        lazyInit();
        caseSensitive = b;
    }

    private static void setAutoPopup(boolean b) {
        lazyInit();
        autoPopup = b;
    }

    private static void lazyInit() {
        if (!inited) {
            inited = true;
            
            // correctly we should use a proper mime type for the document where the completion runs,
            // but at the moment this is enough, because completion settings are mainted globaly for all mime types
            // (ie. their the same for all mime types). Also, if using a specific mime type
            // this code should hold the prefs instance somewhere, but not in a static field!
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, settingsListener, prefs));
            
            setCaseSensitive(prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false));
            setAutoPopup(prefs.getBoolean(SimpleValueNames.COMPLETION_AUTO_POPUP, false));
        }
    }
}
