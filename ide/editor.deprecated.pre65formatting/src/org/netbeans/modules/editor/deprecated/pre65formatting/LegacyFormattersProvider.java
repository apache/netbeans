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

package org.netbeans.modules.editor.deprecated.pre65formatting;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author vita
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.MimeDataProvider.class)
public final class LegacyFormattersProvider implements MimeDataProvider {

    public LegacyFormattersProvider() {
        // no-op
    }

    // ------------------------------------------------------------------------
    // MimeDataProvider implementation
    // ------------------------------------------------------------------------

    @Override
    public Lookup getLookup(final MimePath mimePath) {
        if (mimePath.size() == 1) {
            return new ProxyLookup() {
                private final AtomicBoolean initialized = new AtomicBoolean();
                @Override
                protected void beforeLookup(Template<?> template) {
                    super.beforeLookup(template);
                    final Class<?> clz = template.getType();
                    if (IndentTask.Factory.class.isAssignableFrom(clz) ||
                        ReformatTask.Factory.class.isAssignableFrom(clz) ||
                        TypedTextInterceptor.Factory.class.isAssignableFrom(clz)) {
                        if (!initialized.getAndSet(true)) {
                            final IndentReformatTaskFactoriesProvider provider = IndentReformatTaskFactoriesProvider.get(mimePath);
                            if (provider != null) {
                                final IndentTask.Factory legacyIndenter = provider.getIndentTaskFactory();
                                final ReformatTask.Factory legacyFormatter = provider.getReformatTaskFactory();
                                final TypedTextInterceptor.Factory legacyAutoIndenter = provider.getTypedTextInterceptorFactory();
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.log(
                                        Level.FINE,
                                        "''{0}'' uses legacyIndenter={1}, legacyFormatter={2}, legacyAutoIndenter={3}",   //NOI18N
                                        new Object[]{
                                            mimePath.getPath(),
                                            legacyIndenter,
                                            legacyFormatter,
                                            legacyAutoIndenter});
                                }
                                setLookups(Lookups.fixed(legacyIndenter, legacyFormatter, legacyAutoIndenter));
                            }
                        }
                    }
                }
            };
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // Formatting context manipulation methods
    // ------------------------------------------------------------------------

    public static Document getFormattingContextDocument() {
        Stack<Reference<Document>> stack = FORMATTING_CONTEXT_DOCUMENT.get();
        return stack.isEmpty() ? null : stack.peek().get();
    }

    public static void pushFormattingContextDocument(Document doc) {
        FORMATTING_CONTEXT_DOCUMENT.get().push(new WeakReference<Document>(doc));
    }

    public static void popFormattingContextDocument(Document doc) {
        Stack<Reference<Document>> stack = FORMATTING_CONTEXT_DOCUMENT.get();
        assert !stack.empty() : "Calling popFormattingContextDocument without pushFormattingContextDocument"; //NOI18N

        Reference<Document> ref = stack.pop();
        Document docFromStack = ref.get();
        assert docFromStack == doc : "Popping " + doc + ", but the stack contains " + docFromStack;

        ref.clear();
    }

    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(LegacyFormattersProvider.class.getName());
    
    private static ThreadLocal<Stack<Reference<Document>>> FORMATTING_CONTEXT_DOCUMENT = new ThreadLocal<Stack<Reference<Document>>>() {
        @Override
        protected Stack<Reference<Document>> initialValue() {
            return new Stack<Reference<Document>>();
        }
    };

    private static final class IndentReformatTaskFactoriesProvider {

        public static IndentReformatTaskFactoriesProvider get(MimePath mimePath) {
            Reference<IndentReformatTaskFactoriesProvider> ref = cache.get(mimePath);
            IndentReformatTaskFactoriesProvider provider = ref == null ? null : ref.get();
            if (provider == null) {
                provider = new IndentReformatTaskFactoriesProvider(mimePath);
                cache.put(mimePath, new WeakReference<IndentReformatTaskFactoriesProvider>(provider));
            }
            return provider;
        }

        public IndentTask.Factory getIndentTaskFactory() {
            if (indentTaskFactory == null) {
                indentTaskFactory = new IndentTask.Factory() {
                    public IndentTask createTask(Context context) {
                        Formatter formatter = getFormatter();
                        if (formatter != null && context.document() instanceof BaseDocument) {
                            return new Indenter(context, formatter);
                        } else {
                            return null;
                        }
                    }
                };
            }
            return indentTaskFactory;
        }

        public ReformatTask.Factory getReformatTaskFactory() {
            if (reformatTaskFactory == null) {
                reformatTaskFactory = new ReformatTask.Factory() {
                    public ReformatTask createTask(Context context) {
                        Formatter formatter = getFormatter();
                        if (formatter != null && context.document() instanceof BaseDocument) {
                            return new Reformatter(context, formatter);
                        } else {
                            return null;
                        }
                    }
                };
            }
            return reformatTaskFactory;
        }

        public TypedTextInterceptor.Factory getTypedTextInterceptorFactory() {
            if (typedTextInterceptorFactory == null) {
                typedTextInterceptorFactory = new TypedTextInterceptor.Factory() {
                    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
                        Formatter formatter = getFormatter();
                        if (formatter instanceof ExtFormatter) {
                            return new AutoIndenter((ExtFormatter)formatter);
                        } else {
                            return null;
                        }
                    }
                };
            }
            return typedTextInterceptorFactory;
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private static final Map<MimePath, Reference<IndentReformatTaskFactoriesProvider>> cache = new WeakHashMap<MimePath, Reference<IndentReformatTaskFactoriesProvider>>();
        private static final String NO_FORMATTER = "NO_FORMATTER"; //NOI18N

        private final MimePath mimePath;

        private IndentTask.Factory indentTaskFactory;
        private ReformatTask.Factory reformatTaskFactory;
        private TypedTextInterceptor.Factory typedTextInterceptorFactory;
        private Object legacyFormatter;

        private IndentReformatTaskFactoriesProvider(MimePath mimePath) {
            this.mimePath = mimePath;
        }

        private Formatter getFormatter() {
            if (legacyFormatter == null) {
                EditorKit kit = MimeLookup.getLookup(mimePath).lookup(EditorKit.class);
                if (kit != null) {
                    try {
                        Method createFormatterMethod = kit.getClass().getDeclaredMethod("createFormatter"); //NOI18N
                        legacyFormatter = createFormatterMethod.invoke(kit);
                    } catch (Exception e) {
                        legacyFormatter = e;
                    }
                } else {
                    legacyFormatter = NO_FORMATTER;
                }
            }
            return legacyFormatter instanceof Formatter ? (Formatter) legacyFormatter : null;
        }

    } // End of IndentReformatTaskFactoriesProvider class

    private static final class Indenter implements IndentTask {

        private final Context context;
        private final Formatter formatter;

        public Indenter(Context context, Formatter formatter) {
            this.context = context;
            this.formatter = formatter;
        }

        public void reindent() throws BadLocationException {
            Document doc = context.document();
            int startOffset = context.startOffset();
            int endOffset = context.endOffset();
            
            pushFormattingContextDocument(doc);
            try {
                // Original formatter does not have reindentation of multiple lines
                // so reformat start line and continue for each line.
                Element lineRootElem = lineRootElement(doc);
                Position endPos = doc.createPosition(endOffset);
                do {
                    startOffset = formatter.indentLine(doc, startOffset);
                    int startLineIndex = lineRootElem.getElementIndex(startOffset) + 1;
                    if (startLineIndex >= lineRootElem.getElementCount())
                        break;
                    Element lineElem = lineRootElem.getElement(startLineIndex);
                    startOffset = lineElem.getStartOffset(); // Move to next line
                } while (startOffset < endPos.getOffset());
            } finally {
                popFormattingContextDocument(doc);
            }
        }

        public ExtraLock indentLock() {
            return new ExtraLock() {
                public void lock() {
                    formatter.indentLock();
                }

                public void unlock() {
                    formatter.indentUnlock();
                }
            };
        }

        private static Element lineRootElement(Document doc) {
            return (doc instanceof StyledDocument)
                ? ((StyledDocument)doc).getParagraphElement(0).getParentElement()
                : doc.getDefaultRootElement();
        }
    } // End of Indenter class

    private static final class Reformatter implements ReformatTask {

        private final Context context;
        private final Formatter formatter;

        public Reformatter(Context context, Formatter formatter) {
            this.context = context;
            this.formatter = formatter;
        }

        public void reformat() throws BadLocationException {
            pushFormattingContextDocument(context.document());
            try {
                formatter.reformat((BaseDocument) context.document(), context.startOffset(), context.endOffset());
            } finally {
                popFormattingContextDocument(context.document());
            }
        }

        public ExtraLock reformatLock() {
            return new ExtraLock() {
                public void lock() {
                    formatter.reformatLock();
                }

                public void unlock() {
                    formatter.reformatUnlock();
                }
            };
        }
    } // End of Reformatter class

    private static final class AutoIndenter implements TypedTextInterceptor {

        private final ExtFormatter formatter;

        public AutoIndenter(ExtFormatter formatter) {
            this.formatter = formatter;
        }

        public boolean beforeInsert(Context context) throws BadLocationException {
            // no-op
            return false;
        }

        public void insert(MutableContext context) throws BadLocationException {
            // no-op
        }

        public void afterInsert(Context context) throws BadLocationException {
            if (context.getDocument() instanceof BaseDocument) {
                BaseDocument doc = (BaseDocument) context.getDocument();
                int [] fmtBlk = formatter.getReformatBlock(context.getComponent(), context.getText());
                if (fmtBlk != null) {
                    try {

                        fmtBlk[0] = LineDocumentUtils.getLineStartOffset(doc, fmtBlk[0]);
                        fmtBlk[1] = LineDocumentUtils.getLineEndOffset(doc, fmtBlk[1]);

                        //this was the of #18922, that causes the bug #20198
                        //ef.reformat(doc, fmtBlk[0], fmtBlk[1]);

                        //bugfix of the bug #20198. Bug #18922 is fixed too as well as #6968
                        formatter.reformat(doc, fmtBlk[0], fmtBlk[1], true);

                    } catch (BadLocationException e) {
                    } catch (IOException e) {
                    }
                }
            }
        }

        public void cancelled(Context context) {
            // no-op
        }

    } // End of AutoIndenter class
}
