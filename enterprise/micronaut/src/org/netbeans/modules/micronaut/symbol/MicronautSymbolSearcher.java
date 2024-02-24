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
package org.netbeans.modules.micronaut.symbol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dusan Balek
 */
@ServiceProvider(service=IndexSearcher.class)
public class MicronautSymbolSearcher implements IndexSearcher {

    @Override
    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind searchType, Helper helper) {
        return Collections.emptySet();
    }

    @Override
    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind searchType, Helper helper) {
        if (project == null || !textForQuery.startsWith("@") || IndexingManager.getDefault().isIndexing()) {
            return Collections.emptySet();
        }
        if (textForQuery.equals("@/")) {
            RequestProcessor.getDefault().post(() -> {
                try {
                    Set<FileObject> duplicates = getSymbolsWithPathDuplicates(project, null).stream().map(descriptor -> descriptor.getFileObject()).collect(Collectors.toSet());
                    if (!duplicates.isEmpty()) {
                        Diagnostic.ReporterControl control = Diagnostic.findReporterControl(Lookup.getDefault(), project.getProjectDirectory());
                        control.diagnosticChanged(duplicates, "text/x-java");
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }
        return getSymbols(project, textForQuery);
    }

    static Set<SymbolDescriptor> getSymbolsWithPathDuplicates(Project project, FileObject fo) throws IOException {
        EditorCookie ec = fo != null ? fo.getLookup().lookup(EditorCookie.class) : null;
        StyledDocument doc = ec != null ? ec.openDocument() : null;
        Set<SymbolDescriptor> duplicates = new HashSet<>();
        Map<String, SymbolDescriptor> map = new HashMap<>();
        for (SymbolDescriptor symbol : getSymbols(project, "@/")) {
            if (doc == null || symbol.getFileObject() != fo) {
                SymbolDescriptor previous = map.put(symbol.getSimpleName().replaceAll("\\{.*}", "{}"), symbol);
                if (previous != null) {
                    duplicates.add(symbol);
                    duplicates.add(previous);
                }
            }
        }
        JavaSource js = doc != null ? JavaSource.forDocument(doc) : null;
        if (js != null) {
            js.runUserActionTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (MicronautSymbolFinder.SymbolLocation sl : MicronautSymbolFinder.scan(cc, true)) {
                    SymbolDescriptor symbol = new SymbolDescriptor(sl.getName(), fo, sl.getSelectionStart(), sl.getSelectionEnd());
                    SymbolDescriptor previous = map.put(symbol.getSimpleName().replaceAll("\\{.*}", "{}"), symbol);
                    if (previous != null) {
                        duplicates.add(symbol);
                        duplicates.add(previous);
                    }
                }
            }, true);
        }
        return duplicates;
    }

    private static Set<SymbolDescriptor> getSymbols(Project project, String textForQuery) {
        Set<SymbolDescriptor> symbols = new HashSet<>();
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            try {
                FileObject cacheRoot = getCacheRoot(sg.getRootFolder().toURL());
                if (cacheRoot != null) {
                    cacheRoot.refresh();
                    Enumeration<? extends FileObject> children = cacheRoot.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        if (child.hasExt("mn")) { //NOI18N
                            loadSymbols(child, textForQuery, symbols);
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return symbols;
    }

    private static void loadSymbols(FileObject input, String textForQuery, Set<SymbolDescriptor> symbols) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
            FileObject fo = null;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("url: ")) { //NOI18N
                    String url = line.substring(5);
                    fo = URLMapper.findFileObject(URI.create(url).toURL());
                    if (fo == null) {
                        return;
                    }
                } else if (line.startsWith("symbol: ")) { //NOI18N
                    String info = line.substring(8);
                    int idx = info.lastIndexOf(':');
                    if (idx < 0) {
                        return;
                    }
                    String name = info.substring(0, idx).trim();
                    if (name.startsWith(textForQuery)) {
                        String[] range = info.substring(idx + 1).split("-");
                        int start = range.length > 0 ? Integer.parseInt(range[0]) : 0;
                        int end = range.length > 1 ? Integer.parseInt(range[1]) : start;
                        symbols.add(new SymbolDescriptor(name, fo, start, end));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static FileObject getCacheRoot(URL root) throws IOException {
        final FileObject dataFolder = CacheFolder.getDataFolder(root, true);
        return dataFolder != null ? FileUtil.createFolder(dataFolder, MicronautSymbolFinder.NAME + "/" + MicronautSymbolFinder.VERSION) : null; //NOI18N
    }

    static class SymbolDescriptor extends IndexSearcher.Descriptor implements org.netbeans.modules.csl.api.ElementHandle {

        private final String name;
        private final FileObject fo;
        private final Project project;
        private final OffsetRange range;

        private SymbolDescriptor(String name, FileObject fo, int start, int end) {
            this.name = name;
            this.fo = fo;
            this.project = FileOwnerQuery.getOwner(fo);
            this.range = new OffsetRange(start, end);
        }

        @Override
        public org.netbeans.modules.csl.api.ElementHandle getElement() {
            return this;
        }

        @Override
        public String getSimpleName() {
            return name;
        }

        @Override
        public String getOuterName() {
            return null;
        }

        @Override
        public String getTypeName() {
            return null;
        }

        @Override
        public String getContextName() {
            return null;
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getProjectName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        @Override
        public Icon getProjectIcon() {
            return ProjectUtils.getInformation(project).getIcon();
        }

        @Override
        public FileObject getFileObject() {
            return fo;
        }

        @Override
        public int getOffset() {
            return range.getStart();
        }

        @Override
        public void open() {
            GsfUtilities.open(getFileObject(), getOffset(), null);
        }


        @Override
        public String getMimeType() {
            return getFileObject().getMIMEType();
        }

        @Override
        public String getName() {
            return getSimpleName();
        }

        @Override
        public String getIn() {
            return getOuterName();
        }

        @Override
        public org.netbeans.modules.csl.api.ElementKind getKind() {
            return org.netbeans.modules.csl.api.ElementKind.INTERFACE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.singleton(Modifier.PUBLIC);
        }

        @Override
        public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle handle) {
            if (handle instanceof SymbolDescriptor) {
                return this.getName().equals(handle.getName());
            } else {
                return false;
            }
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return range;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + Objects.hashCode(this.fo);
            hash = 67 * hash + Objects.hashCode(this.range);
            hash = 67 * hash + Objects.hashCode(this.name);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SymbolDescriptor other = (SymbolDescriptor) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.fo, other.fo)) {
                return false;
            }
            return Objects.equals(this.range, other.range);
        }
    }
}
