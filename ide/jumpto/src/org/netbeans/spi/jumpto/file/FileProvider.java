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

package org.netbeans.spi.jumpto.file;

import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.jumpto.file.FileDescription;
import org.netbeans.modules.jumpto.file.FileProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * A FileProvider participates in the Goto File dialog by providing matched files
 * for given {@link SourceGroup}.
 *
 * @since 1.15
 * @author Tomas Zezula
 */
public interface FileProvider {

    /**
     * Compute a list of files that match the given search text for the given
     * search type. This might be a slow operation, and the infrastructure may end
     * up calling {@link #cancel} on the file provider during the operation, in which
     * case the method can return incomplete results.
     * <p>
     * Note that a useful performance optimization is for the FileProvider to cache
     * a few of its most recent search results, and if the next search (e.g. more user
     * keystrokes) is a simple narrowing of the search, just filter the previous search
     * result. The same {@link FileProvider} instance is used during the GoTo File session and
     * it's freed when the GoTo File dialog is closed.
     *
     * @param context search context containg search text, type, project and {@link SourceGroup} root
     * @param result  filled with files and optional message
     * @return returns true if the root was handled by this FileProvider. When false
     * next provider is used.
     */
    boolean computeFiles(Context context, Result result);

    /**
     * Cancel the current operation, if possible. This might be called if the user
     * has typed something (including the backspace key) which makes the current
     * search obsolete and a new one should be initiated.
     */
    void cancel();

    /**
     * Represents search context.
     * Contains search type (such as prefix, regexp), search text,
     * {@link SourceGroup} root and project where to search.
     *
     */
    public static final class Context {

        //<editor-fold defaultstate="collapsed" desc="Private data">
        private final String text;
        private final SearchType type;
        private final int lineNr;
        private final Project currentProject;
        private FileObject sourceGroupRoot;
        private Project project;
        //</editor-fold>

        /**
         * Returns project owning the {@link SourceGroup} root
         * @return project to search in.
         */
        public Project getProject() {
            if (project == null) {
                project = ProjectConvertors.getNonConvertorOwner(this.sourceGroupRoot);
            }
            return project;
        }

        /**
         * Returns the {@link SourceGroup} root to search files in.
         * @return root to search in.
         */
        public FileObject getRoot() { return sourceGroupRoot;}

        /**
          * Return the text used for search.
          *
          * @return The text used for the search; e.g. when getSearchType() == SearchType.PREFIX,
          *   text is the prefix that all returned types should start with.
          */
        public String getText() { return text; }

        /**
         * Return the type of search.
         *
         * @return Type of search performed, such as prefix, regexp or camel case.
         */
        public SearchType getSearchType() { return type; }

        /**
         * Returns a line number on which the file should be opened.
         * @return the preferred line number or -1 if no preferred line number is given.
         * @since 1.30
         */
        public int getLineNumber() { return lineNr; }

        //<editor-fold defaultstate="collapsed" desc="Private methods">
        private Context(
                @NonNull final String text,
                @NonNull final SearchType type,
                final int lineNr,
                @NullAllowed final Project currentProject) {
            Parameters.notNull("text", text);   //NOI18N
            Parameters.notNull("type", type);   //NOI18N
            this.text = text;
            this.type = type;
            this.lineNr = lineNr;
            this.currentProject = currentProject;
        }

        Project getCurrentProject() {
            return currentProject;
        }

        static {
            FileProviderAccessor.setInstance(new FileProviderAccessor() {
                @Override
                public Context createContext(
                        @NonNull final String text,
                        @NonNull final SearchType searchType,
                        final int lineNr,
                        @NullAllowed final Project currentProject) {
                    return new Context(text, searchType, lineNr, currentProject);
                }
                @Override
                public Result createResult(List<? super FileDescriptor> result, String[] message, Context ctx) {
                    return new Result(result, message, ctx);
                }
                @Override
                public int getRetry(Result result) {
                    return result.retry;
                }
                @Override
                public void setRoot(Context ctx, FileObject root) {
                    ctx.sourceGroupRoot = root;
                    ctx.project = null;
                }
                @Override
                public void setFromCurrentProject(FileDescriptor desc, boolean value) {
                    desc.setFromCurrentProject(value);
                }
                @Override
                public boolean isFromCurrentProject(FileDescriptor desc) {
                    return desc.isFromCurrentProject();
                }
                @Override
                public void setLineNumber(@NonNull final FileDescriptor desc, final int lineNo) {
                    desc.setLineNumber(lineNo);
                }
            });
        }
        //</editor-fold>
    }

    /**
     * Represents a collection of files that match
     * the given search criteria. Moreover, it can contain message
     * for the user, such as an incomplete search result.
     *
     */
    public static final class Result {

        //<editor-fold defaultstate="collapsed" desc="Private data">
        private final List<? super FileDescriptor> result;
        private final String[] message;
        private final Context ctx;
        private int retry;
        //</editor-fold>

        /**
         * Optional message. It can inform the user about result, e.g.
         * that result can be incomplete etc.
         *
         * @param  msg  message
         */
        public void setMessage(String msg) {
            message[0] = msg;
        }

        /**
         * Adds a file into the result
         * When the file is under processed {@link  SourceGroup} relative path
         * is displayed in the dialog. If the file is not under
         * {@link SourceGroup} absolute path is used. Never add a file owned by
         * other {@link SourceGroup}.
         * @param file The file to be added into result
         */
        public void addFile (final FileObject file) {
            Parameters.notNull("file", file);   //NOI18N
            String path = FileUtil.getRelativePath(ctx.getRoot(), file);
            if (path == null) {
                path = FileUtil.getFileDisplayName(file);
            }
            final Project prj = ctx.getProject();
            addFileDescriptor(new FileDescription(file, path, prj));
        }

        /**
         * Adds a {@link FileDescriptor} into the result
         * @param desc The {@link FileDescriptor} to be added into result
         */
        public void addFileDescriptor(final FileDescriptor desc) {
            Parameters.notNull("desc", desc);
            result.add(setLineNumber(setFromCurrentProject(desc)));
        }

        /**
         * Notify caller that a provider should be called again because
         * of incomplete or inaccurate results.
         *
         * Method can be used when long running task blocks the provider
         * to complete the data.
         *
         */
        public void pendingResult() {
            retry = 2000;
        }

        //<editor-fold defaultstate="collapsed" desc="Private methods">
        private Result(
                @NonNull final List<? super FileDescriptor> result,
                @NonNull final String[] message,
                @NonNull final Context ctx) {
            Parameters.notNull("result", result);   //NOI18N
            Parameters.notNull("message", message); //NOI18N
            Parameters.notNull("ctx", ctx);
            if (message.length != 1) {
                throw new IllegalArgumentException("message.length != 1");  //NOI18N
            }
            this.result = result;
            this.message = message;
            this.ctx = ctx;
        }

        private FileDescriptor setFromCurrentProject(final FileDescriptor desc) {
            final Project prj = ctx.getProject();
            final Project curPrj = ctx.getCurrentProject();
            desc.setFromCurrentProject(curPrj != null && prj != null && curPrj.getProjectDirectory() == prj.getProjectDirectory());
            return desc;
        }

        private FileDescriptor setLineNumber(@NonNull final FileDescriptor desc) {
            desc.setLineNumber(ctx.getLineNumber());
            return desc;
        }
        //</editor-fold>
    }

}
