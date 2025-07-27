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
package org.netbeans.modules.whitelist.index;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.api.whitelist.index.WhiteListIndex;
import org.netbeans.api.whitelist.support.WhiteListSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListIndexerPlugin implements JavaIndexerPlugin {

    private static final String WHITE_LIST_INDEX = "whitelist"; //NOI18N
    private static final String RULE_MSG = "msg";    //NOI18N
    private static final String RULE_NAME = "name";  //NOI18N
    private static final String WHITE_LIST_ID = "whitelist";   //NOI18N
    private static final String LINE = "line";  //NOI18N
    private static Map<URL,File> roots2whiteListDirs = new ConcurrentHashMap<URL, File>();

    private final URL root;
    private final File whiteListDir;
    private final WhiteListQuery.WhiteList whiteList;
    private final DocumentIndex index;

    private WhiteListIndexerPlugin(
            @NonNull final URL root,
            @NonNull final WhiteListQuery.WhiteList whiteList,
            @NonNull final File whiteListDir) throws IOException {
        assert root != null;
        assert whiteList != null;
        assert whiteListDir != null;
        this.root = root;
        this.whiteList = whiteList;
        this.whiteListDir = whiteListDir;
        this.index = IndexManager.createDocumentIndex(whiteListDir);
    }

    @Override
    public void process(
            @NonNull final CompilationUnitTree toProcess,
            @NonNull final Indexable indexable,
            @NonNull final Lookup services) {
        final Trees trees = services.lookup(Trees.class);
        assert trees != null;
        final Map<? extends Tree, ? extends WhiteListQuery.Result> problems = WhiteListSupport.getWhiteListViolations(toProcess, whiteList, trees, null);
        assert problems != null;
        final LineMap lm = toProcess.getLineMap();
        final SourcePositions sp = trees.getSourcePositions();
        for (Map.Entry<? extends Tree, ? extends WhiteListQuery.Result> p : problems.entrySet()) {
            assert !p.getValue().isAllowed() : "only violations should be stored"; // NOI18N
            final int start = (int) sp.getStartPosition(toProcess, p.getKey());
            int ln;
            if (start>=0 && (ln=(int)lm.getLineNumber(start))>=0) {
                for (WhiteListQuery.RuleDescription rule : p.getValue().getViolatedRules()) {
                    //Lucene API does not promise that the field are returned in the order they were
                    //added (currently it behavis in this way but may be changed in the future).
                    //see http://www.gossamer-threads.com/lists/lucene/java-dev/64013
                    final IndexDocument doc = IndexManager.createDocument(indexable.getRelativePath());
                    final String wlID = rule.getWhiteListID();
                    if (wlID != null) {
                        doc.addPair(WHITE_LIST_ID, wlID, true, true);
                    }
                    doc.addPair(RULE_NAME, rule.getRuleName(),true,true);
                    doc.addPair(RULE_MSG, rule.getRuleDescription(), false, true);
                    doc.addPair(LINE, Integer.toString(ln), false, true);
                    index.addDocument(doc);
                }
            }
        }
    }

    @Override
    public void delete(@NonNull final Indexable indexable) {
        index.removeDocument(indexable.getRelativePath());
    }

    @Override
    public void finish() {
        try {
            index.store(true);
            roots2whiteListDirs.put(root, whiteListDir);
            WhiteListIndexAccessor.getInstance().refresh(root);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                index.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @CheckForNull
    private static DocumentIndex getIndex(@NonNull final FileObject root) {
        try {
            final File whiteListFolder = roots2whiteListDirs.get(root.toURL());
            if (whiteListFolder != null) {
                final DocumentIndex index = IndexManager.createDocumentIndex(whiteListFolder);
                return index.getStatus() == Index.Status.VALID ? index : null;
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }

    @NonNull
    public static Collection<? extends WhiteListIndex.Problem> getWhiteListViolations(
            @NonNull final FileObject root,
            @NullAllowed final FileObject resource) {
        final List<WhiteListIndex.Problem> result = new ArrayList<WhiteListIndex.Problem>();
        try {
            IndexManager.priorityAccess(new IndexManager.Action<Void>() {
                @Override
                public Void run() throws IOException, InterruptedException {
                    final DocumentIndex index = getIndex(root);
                    if (index != null) {
                        try {
                            for (IndexDocument doc : index.findByPrimaryKey(
                                    resource == null ? "" : FileUtil.getRelativePath(root,resource),    //NOI18N
                                    QueryKind.PREFIX)) {
                                try {
                                    final String key = doc.getPrimaryKey();
                                    final String wlName = doc.getValue(WHITE_LIST_ID);
                                    final String ruleName = doc.getValue(RULE_NAME);
                                    assert ruleName != null;
                                    final String ruleDesc = doc.getValue(RULE_MSG);
                                    assert ruleDesc != null;
                                    final int line = Integer.parseInt(doc.getValue(LINE));
                                    final WhiteListQuery.Result wr = new WhiteListQuery.Result(
                                            Collections.singletonList(
                                                new WhiteListQuery.RuleDescription(ruleName, ruleDesc, wlName)));
                                    result.add(WhiteListIndexAccessor.getInstance().createProblem(wr, root, key, line));
                                } catch (ArithmeticException ae) {
                                    Exceptions.printStackTrace(ae);
                                }
                            }
                        } finally {
                            index.close();
                        }
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        }
        return result;
    }

    @MimeRegistration(mimeType="text/x-java",service=JavaIndexerPlugin.Factory.class)
    public static class Factory implements JavaIndexerPlugin.Factory {
        @Override
        public JavaIndexerPlugin create(final URL root, final FileObject cacheFolder) {
            try {
                File whiteListDir = roots2whiteListDirs.get(root);
                if (whiteListDir == null) {
                    //First time
                    final FileObject whiteListFolder = FileUtil.createFolder(cacheFolder, WHITE_LIST_INDEX);
                    whiteListDir = FileUtil.toFile(whiteListFolder);
                    if (whiteListDir == null) {
                        return null;
                    }
                }
                final FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo == null) {
                    delete(whiteListDir);
                    return null;
                } else {
                    final WhiteListQuery.WhiteList wl = WhiteListQuery.getWhiteList(rootFo);
                    if (wl == null) {
                        return null;
                    }
                    return new WhiteListIndexerPlugin(
                        root,
                        wl,
                        whiteListDir);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }

        private static void delete(@NonNull final File folder) throws IOException {
            try {
                IndexManager.writeAccess(new IndexManager.Action<Void>(){
                    @Override
                    public Void run() throws IOException, InterruptedException {
                        deleteImpl(folder);
                        return null;
                    }
                });
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        private static void deleteImpl (final File folder) {
            final File[] children = folder.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (child.isDirectory()) {
                        deleteImpl(child);
                    }
                    child.delete();
                }
            }
        }
    }

}
