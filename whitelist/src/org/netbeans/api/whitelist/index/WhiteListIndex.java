/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
