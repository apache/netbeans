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
package org.netbeans.api.whitelist.index;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.modules.whitelist.index.WhiteListIndexAccessor;
import org.netbeans.modules.whitelist.index.WhiteListIndexerPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * A persistent index of white list violations.
 * @author Tomas Zezula
 * @since 1.2
 */
public final class WhiteListIndex {

    static {
        WhiteListIndexAccessor.setInstance(new WhiteListIndexAccessorImpl());
    }

    private static WhiteListIndex instance;
    private final List<WhiteListIndexListener> listeners = new CopyOnWriteArrayList<WhiteListIndexListener>();

    private WhiteListIndex() {}

    /**
     * Returns the white list violations for given root or file.
     * @param root the root to get white list violations for
     * @param file the file to restrict the white list violation search or null
     * @param whitelists if non empty the list is restricted only to violations of given white lists
     * @return a {@link Collection} of {@link Problem}s
     * @throws IllegalArgumentException in case when file is not under the root or it's not a valid file
     * @throws UnsupportedOperationException when the index is not supported by the IDE
     */
    @NonNull
    public Collection<? extends Problem> getWhiteListViolations(
            @NonNull final FileObject root,
            @NullAllowed final FileObject file,
            @NonNull final String... whitelists) throws IllegalArgumentException, UnsupportedOperationException {
        Parameters.notNull("scope", root); //NOI18N
        Parameters.notNull("whitelists", whitelists);   //NOI18N
        if (file != null && !(root.equals(file) || FileUtil.isParentOf(root, file))) {
            throw new IllegalArgumentException(
                    "The file: " + //NOI18N
                    FileUtil.getFileDisplayName(file) +
                    " has to be inside the root: " + //NOI18N
                    FileUtil.getFileDisplayName(root));
        }
        if (file != null && !file.isData()) {
            throw new IllegalArgumentException(
                    "The file: " + //NOI18N
                    FileUtil.getFileDisplayName(file) +
                    " has to be file.");    //NOI18N
        }
        return WhiteListIndexerPlugin.getWhiteListViolations(root,file);
    }

    /**
     * Adds {@link WhiteListIndexListener}.
     * The listener is notified when the white list index is changed.
     * @param listener the listener to be added
     */
    public void addWhiteListIndexListener(@NonNull final WhiteListIndexListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.add(listener);
    }

    /**
     * Removes {@link WhiteListIndexListener}.
     * @param listener the listener to be removed
     */
    public void removeWhiteListIndexListener(@NonNull final WhiteListIndexListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.remove(listener);
    }

    /**
     * Returns an instance of {@link WhiteListIndex}
     * @return the instance of {@link WhiteListIndex}
     */
    public static synchronized WhiteListIndex getDefault() {
        if (instance == null) {
            instance = new WhiteListIndex();
        }
        return instance;
    }


    /**
     * Represent a white list violation
     */
    public static final class Problem {

        private final WhiteListQuery.Result result;
        private final FileObject root;
        private final String relPath;
        private final int line;

        private Problem (
                @NonNull final WhiteListQuery.Result result,
                @NonNull final FileObject root,
                @NonNull final String relPath,
                final int line) {
            Parameters.notNull("result", result);   //NOI18N
            Parameters.notNull("root", root);   //NOI18N
            Parameters.notNull("relPath", relPath); //NOI18N
            this.result = result;
            this.root = root;
            this.relPath = relPath;
            this.line = line;
        }

        /**
         * Returns {@link WhiteListQuery.Result} describing the
         * white list violation.
         * @return the {@link WhiteListQuery.Result}
         */
        @NonNull
        public WhiteListQuery.Result getResult() {
            return result;
        }

        /**
         * Returns the file in which the white list violation occured.
         * @return the {@link FileObject}
         */
        @CheckForNull
        public FileObject getFile() {
            return root.getFileObject(relPath);
        }

        /**
         * Returns the line number of white list violation.
         * @return the line number
         */
        public int getLine() {
            return line;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Private implementation">
    private void fireIndexChange(final URL root) {
        final WhiteListIndexEvent event = new WhiteListIndexEvent(this, root);
        for (WhiteListIndexListener l : listeners) {
            l.indexChanged(event);
        }
    }


    private static final class WhiteListIndexAccessorImpl extends WhiteListIndexAccessor {
        @Override
        public void refresh(@NonNull URL root) {
            WhiteListIndex.getDefault().fireIndexChange(root);
        }

        @Override
        @NonNull
        public Problem createProblem(
                @NonNull final Result result,
                @NonNull final FileObject root,
                @NonNull final String key,
                int line) {
            return new Problem(result, root, key, line);
        }
    }
    //</editor-fold>
}
