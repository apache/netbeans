/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.requirejs.editor.index;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsIndexer extends EmbeddingIndexer {

    private static final Logger LOG = Logger.getLogger(RequireJsIndexer.class.getName());

    public static final String FIELD_EXPOSED_TYPES = "et"; //NOI18N
    public static final String FIELD_MODULE_NAME = "mn"; //NOI18N
    public static final String FIELD_PATH_MAP = "mp";   //NOI18N
    public static final String FIELD_BASE_PATH = "bp";  //NOI18N
    public static final String FIELD_USED_PLUGINS = "up"; //NOI18N
    public static final String FIELD_PACKAGES = "pk"; //NOI18N
    public static final String FIELD_SOURCE_ROOT = "sr"; //NOI18N

    private static final ThreadLocal<Map<URI, Collection<? extends TypeUsage>>> exposedTypes = new ThreadLocal();
    private static final ThreadLocal<Map<URI, Map<String, String>>> pathsMapping = new ThreadLocal();
    private static final ThreadLocal<Map<URI, String>> basePath = new ThreadLocal();
    private static final ThreadLocal<Map<URI, Collection<String>>> usedPlugins = new ThreadLocal();
    private static final ThreadLocal<Map<URI, Map<String, String>>> packageLocations = new ThreadLocal();
    private static final ThreadLocal<Map<URI, String>> sourceRoot = new ThreadLocal();

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        if (fo == null) {
            return;
        }
        
        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }
        IndexDocument elementDocument = support.createDocument(fo);
        boolean storeDocument = false;
        Map<URI, Collection<? extends TypeUsage>> types = exposedTypes.get();
        if (types != null && !types.isEmpty()) {
            Collection<? extends TypeUsage> resolvedTypes = types.remove(fo.toURI());
            if (resolvedTypes != null && !resolvedTypes.isEmpty()) {
                for (TypeUsage type : resolvedTypes) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(type.getType()).append(":").append(type.getOffset()).append(":").append(type.isResolved() ? "1" : "0"); //NOI18N
                    elementDocument.addPair(FIELD_EXPOSED_TYPES, sb.toString(), false, true);
                }
                storeDocument = true;
                elementDocument.addPair(FIELD_MODULE_NAME, fo.getName(), true, true);
            }
        }

        Map<URI, Map<String, String>> mappings = pathsMapping.get();
        if (mappings != null && !mappings.isEmpty()) {
            Map<String, String> pathMappings = mappings.remove(fo.toURI());
            if (pathMappings != null && !pathMappings.isEmpty()) {
                for (Map.Entry<String, String> entry : pathMappings.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                        sb.append(entry.getKey()).append(";").append(entry.getValue()); //NOI18N
                        elementDocument.addPair(FIELD_PATH_MAP, sb.toString(), true, true);
                }
                storeDocument = true;
            }
        }
        
        Map<URI, String>baseUrls = basePath.get();
        if (baseUrls != null && !baseUrls.isEmpty()) {
            String baseUrl = baseUrls.remove(fo.toURI());
            if (baseUrl != null && !baseUrl.isEmpty()) {
                elementDocument.addPair(FIELD_BASE_PATH, baseUrl, true, true);
                storeDocument = true;
            }
        }
        
        Map<URI, Collection<String>>uPlugins = usedPlugins.get();
        if (uPlugins != null && !uPlugins.isEmpty()) {
            Collection<String> plugins = uPlugins.remove(fo.toURI());
            if (plugins != null && !plugins.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String plugin : plugins) {
                    sb.append(plugin);
                    sb.append(';');
                }
                String pluginNames = sb.toString();
                pluginNames = pluginNames.substring(0, pluginNames.length() - 1);
                elementDocument.addPair(FIELD_USED_PLUGINS, pluginNames, true, true);
                storeDocument = true;
            }
        }

        Map<URI, Map<String, String>> pkgLocations = packageLocations.get();
        if (pkgLocations != null && !pkgLocations.isEmpty()) {
            Map<String, String> pkgLocation = pkgLocations.remove(fo.toURI());
            if (pkgLocation != null && !pkgLocation.isEmpty()) {
                for (Map.Entry<String, String> entry : pkgLocation.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getKey()).append(";").append(entry.getValue()); //NOI18N
                    elementDocument.addPair(FIELD_PACKAGES, sb.toString(), true, true);
                }
                storeDocument = true;
            }
        }

        Map<URI, String> sourceRoots = sourceRoot.get();
        if (sourceRoots != null && !sourceRoots.isEmpty()) {
            String rootName = sourceRoots.remove(fo.toURI());
            if (rootName != null && !rootName.isEmpty()) {
                elementDocument.addPair(FIELD_SOURCE_ROOT, rootName, true, true);
                storeDocument = true;
            }
        }

        if (storeDocument) {
            support.addDocument(elementDocument);
        }
    }

    public static void addTypes(final URI uri, final Collection<? extends TypeUsage> exported) {
        final Map<URI, Collection<? extends TypeUsage>> map = exposedTypes.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addTypes can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, exported);
    }

    public static void addPathMapping(final URI uri, final Map<String, String> mappings) {
        final Map<URI, Map<String, String>> map = pathsMapping.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addPathMapping can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, mappings);
    }
    
    public static void addBasePath(final URI uri, String path) {
        final Map<URI, String> map = basePath.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addBasePath can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, path);
    }
    
    public static void addUsedPlugings(final URI uri, Collection<String> plugins) {
        final Map<URI, Collection<String>> map = usedPlugins.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addUsedPlugins can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, plugins);
    }

    public static void addPackages(final URI uri, final Map<String, String> packages) {
        final Map<URI, Map<String, String>> map = packageLocations.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addPackages can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, packages);
    }

    public static void addSourceRoot(final URI uri, String rootName) {
        final Map<URI, String> map = sourceRoot.get();

        if (map == null) {
            throw new IllegalStateException("RequireJsIndexer.addSourceRoot can be called only from scanner thread.");  //NOI18N
        }
        map.put(uri, rootName);
    }

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "requirejs"; // NOI18N
        public static final int VERSION = 2;
        private static final int PRIORITY = 210;

        private static final ThreadLocal<Collection<Runnable>> postScanTasks = new ThreadLocal<Collection<Runnable>>();

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new RequireJsIndexer();
            } else {
                return null;
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {

        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            return JsTokenId.JAVASCRIPT_MIME_TYPE.equals(snapshot.getMimeType());
        }

        @Override
        public boolean scanStarted(Context context) {
            postScanTasks.set(new LinkedList<Runnable>());
            exposedTypes.set(new HashMap<URI, Collection<? extends TypeUsage>>());
            pathsMapping.set(new HashMap<URI, Map<String, String>>());
            basePath.set(new HashMap<URI, String>(1));
            usedPlugins.set(new HashMap<URI, Collection<String>>());
            packageLocations.set(new HashMap<URI, Map<String, String>>());
            sourceRoot.set(new HashMap<URI, String>(1));
            return super.scanStarted(context);
        }

        @Override
        public void scanFinished(Context context) {
            try {
                for (Runnable task : postScanTasks.get()) {
                    task.run();
                }
            } finally {
                postScanTasks.remove();
                super.scanFinished(context);
            }
        }

        public static boolean isScannerThread() {
            return postScanTasks.get() != null;
        }

        public static void addPostScanTask(final Runnable task) {
            Parameters.notNull("task", task);   //NOI18N
            final Collection<Runnable> tasks = postScanTasks.get();
            if (tasks == null) {
                throw new IllegalStateException("JsIndexer.postScanTask can be called only from scanner thread.");  //NOI18N
            }
            tasks.add(task);
        }

        @Override
        public int getPriority() {
            return PRIORITY;
        }

    }
}
