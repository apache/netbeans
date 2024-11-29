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
package org.netbeans.modules.micronaut.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.modules.micronaut.symbol.MicronautSymbolFinder;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class MicronautConfigErrorProvider extends CustomIndexer implements ErrorProvider {

    private static final MicronautConfigErrorProvider INSTANCE = new MicronautConfigErrorProvider();
    private static final ErrorsCache.Convertor<Diagnostic> ERROR_CONVERTOR = new ErrorConvertorImpl();
    private static final String ERR_CODE_PREFIX = "WARN_PropertyWithoutValue:";

    @MimeRegistrations({
        @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = ErrorProvider.class),
        @MimeRegistration(mimeType = MicronautConfigUtilities.PROPERTIES_MIME, service = ErrorProvider.class)
    })
    public static MicronautConfigErrorProvider createProvider() {
        return INSTANCE;
    }

    @NbBundle.Messages({
        "ERR_PropertyWithoutValue=Property with unset value"
    })
    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        if (context.errorKind() == Kind.ERRORS) {
            FileObject fo = context.file();
            if (fo != null && MicronautConfigUtilities.isMicronautConfigFile(fo)) {
                return computeErrors(fo);
            }
        }
        return Collections.emptyList();
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, org.netbeans.modules.parsing.spi.indexing.Context context) {
        FileObject root = context.getRoot();
        for (Indexable file : files) {
            FileObject fo = root.getFileObject(file.getRelativePath());
            if (fo != null && MicronautConfigUtilities.isMicronautConfigFile(fo)) {
                ErrorsCache.setErrors(context.getRootURI(), file, computeErrors(fo), ERROR_CONVERTOR);
            }
        }
    }

    private List<? extends Diagnostic> computeErrors(FileObject fo) {
        List<Diagnostic> diags = new ArrayList<>();
        if (MicronautConfigUtilities.YAML_MIME.equals(fo.getMIMEType())) {
            try {
                ParserManager.parse(Collections.singletonList(Source.create(fo)), new UserTask() {
                    @Override
                    public void run(ResultIterator it) throws Exception {
                        Parser.Result result = it.getParserResult();
                        Snapshot snapshot = result != null ? result.getSnapshot() : null;
                        if (snapshot != null) {
                            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(snapshot.getMimeType());
                            if (language != null) {
                                StructureScanner scanner = language.getStructure();
                                if (scanner != null && result instanceof ParserResult) {
                                    String text = snapshot.getText().toString();
                                    scan(text, scanner.scan((ParserResult) result), structure -> {
                                        int start = (int) structure.getPosition();
                                        int end = (int) structure.getEndPosition();
                                        diags.add(Diagnostic.Builder.create(() -> start, () -> end, Bundle.ERR_PropertyWithoutValue())
                                                .setSeverity(Diagnostic.Severity.Warning)
                                                .setCode(ERR_CODE_PREFIX + text.substring(0, start).split("\n").length)
                                                .build());
                                    });
                                }
                            }
                        }
                    }
                });
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            int offset = 0;
            String[] lines = Source.create(fo).createSnapshot().getText().toString().split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.length() > 0 && !line.startsWith("#") && !line.startsWith("!")) {
                    int eqIdx = line.indexOf('=');
                    if (eqIdx > 0) {
                        if (line.substring(eqIdx + 1).trim().length() == 0) {
                            int start = offset;
                            int end = offset + line.length();
                            diags.add(Diagnostic.Builder.create(() -> start, () -> end, Bundle.ERR_PropertyWithoutValue())
                                    .setSeverity(Diagnostic.Severity.Warning)
                                    .setCode(ERR_CODE_PREFIX + (i + 1))
                                    .build());
                        }
                    }
                }
                offset += line.length() + 1;
            }
        }
        return diags;
    }

    private static void scan(String sourceText, List<? extends StructureItem> structures, Consumer<StructureItem> callback) {
        if (structures != null) {
            for (StructureItem structure : structures) {
                if (structure.isLeaf()) {
                    int start = (int) structure.getPosition();
                    int end = (int) structure.getEndPosition();
                    String text = sourceText.substring(start, end);
                    int idx = text.indexOf(':');
                    if (idx >= 0) {
                        if (text.substring(idx + 1).trim().length() == 0) {
                            callback.accept(structure);
                        }
                    }
                } else {
                    scan(sourceText, structure.getNestedItems(), callback);
                }
            }
        }
    }

    @MimeRegistration(mimeType = "", service = CustomIndexerFactory.class)
    public static final class CIFImpl extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return INSTANCE;
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, org.netbeans.modules.parsing.spi.indexing.Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, org.netbeans.modules.parsing.spi.indexing.Context context) {
        }

        @Override
        public String getIndexerName() {
            return MicronautSymbolFinder.NAME;
        }

        @Override
        public int getIndexVersion() {
            return MicronautSymbolFinder.VERSION;
        }
    }

    private static final class ErrorConvertorImpl implements ErrorsCache.Convertor<Diagnostic> {
        @Override
        public ErrorsCache.ErrorKind getKind(Diagnostic t) {
            return t.getSeverity() == Diagnostic.Severity.Error ? ErrorsCache.ErrorKind.ERROR : ErrorsCache.ErrorKind.WARNING;
        }
        @Override
        public int getLineNumber(Diagnostic t) {
            return Integer.parseInt(t.getCode().substring(ERR_CODE_PREFIX.length()));
        }
        @Override
        public String getMessage(Diagnostic t) {
            return t.getDescription();
        }
    }
}
