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
package org.netbeans.modules.lsp.client.bindings;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class BaseSymbolProvider {

    private static final Pattern WORD_START = Pattern.compile("(^|[^\\p{L}])(\\p{L})");
    private final AtomicBoolean cancel = new AtomicBoolean();
    private CompletableFuture<List<? extends SymbolInformation>> currentQuery;

    public String name() {
        return "lsp-client";
    }

    public void computeSymbolNames(SearchType searchType, String searchText, BiConsumer<SymbolInformation, String> found) {
        cancel.set(false);

        List<CompletableFuture<List<? extends SymbolInformation>>> queries = new ArrayList<>();

        try {
            for (LSPBindings b : LSPBindings.getAllBindings()) {
                if (cancel.get()) {
                    return ;
                }
                queries.add(b.getWorkspaceService().symbol(new WorkspaceSymbolParams(searchText)));
            }

            NameMatcher matcher = NameMatcherFactory.createNameMatcher(searchText, searchType);

            while (!queries.isEmpty()) {
                if (cancel.get()) {
                    return ;
                }


                try {
                    currentQuery = queries.remove(queries.size() - 1);

                    List<? extends SymbolInformation> infos = currentQuery.get();

                    currentQuery = null;

                    if (infos != null) {
                        for (SymbolInformation info : infos) {
                            if (cancel.get()) {
                                return ;
                            }
                            Matcher wordStartMatcher = WORD_START.matcher(info.getName());
                            while (wordStartMatcher.find()) {
                                int nameStart = wordStartMatcher.start(2);
                                String namePart = info.getName().substring(nameStart);
                                if (matcher.accept(namePart)) {
                                    found.accept(info, namePart);
                                }
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    //ignore?
                } catch (CancellationException ex) {
                    return ;
                } catch (ExecutionException ex) {
                    LOG.log(Level.FINE, null, ex);
                }
            }
        } finally {
            if (cancel.get()) {
                if (currentQuery != null) {
                    currentQuery.cancel(true);
                }
                queries.forEach(cf -> cf.cancel(true));
            }
            currentQuery = null;
        }
    }

    private static final Logger LOG = Logger.getLogger(BaseSymbolProvider.class.getName());

    public void cancel() {
        cancel.set(true);
        if (currentQuery != null) {
            currentQuery.cancel(true);
        }
    }

    public void cleanup() {
    }

    public static interface BaseSymbolDescriptor {

        public SymbolInformation getInfo();

        public default Icon getIcon() {
            return Icons.getSymbolIcon(getInfo().getKind());
        }

        public default String getSymbolName() {
            return getInfo().getName();
        }

        public default String getOwnerName() {
            String container = getInfo().getContainerName();

            if (container == null || "".equals(container)) {
                String uri = getInfo().getLocation().getUri();

                container = uri.substring(uri.lastIndexOf('/') + 1);
            }

            return container;
        }

        public default String getProjectName() {
            return getProjectInformation().map(pi -> pi.getDisplayName()).orElse(null);
        }

        public default Icon getProjectIcon() {
            return getProjectInformation().map(pi -> pi.getIcon()).orElse(null);
        }

        //XXX: should be private:
        public default Optional<ProjectInformation> getProjectInformation() {
            FileObject file = getFileObject();

            if (file != null) {
                Project owningProject = FileOwnerQuery.getOwner(file);

                if (owningProject != null) {
                    return Optional.of(ProjectUtils.getInformation(owningProject));
                }
            }

            return Optional.empty();
        }

        public default FileObject getFileObject() {
            try {
                URI target = URI.create(getInfo().getLocation().getUri());

                return URLMapper.findFileObject(target.toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        public default int getOffset() {
            return -1; //XXX
        }

        public default void open() {
            Utils.open(getInfo().getLocation().getUri(), getInfo().getLocation().getRange());
        }

    }
}
