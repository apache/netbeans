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

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.parsing.impl.indexing.errors.TaskCache;
import org.openide.filesystems.FileObject;

/**Cache of errors in a file. The errors are shown in the projects tab and tasklist, as appropriate.
 *
 * @author lahvac
 * @since 1.30
 */
public class ErrorsCache {

    /**Sets errors for a file/indexable. All previously set errors are forgotten.
     *
     * @param root inside which the given file resides
     * @param i indexable for which the errors are being set
     * @param errors errors to set
     * @param convertor getter for properties of {@code T}. The methods on the
     *                  convertor are not invoked after this method finishes.
     */
    public static <T> void setErrors(URL root, Indexable i, Iterable<? extends T> errors, Convertor<T> convertor) {
        TaskCache.getDefault().dumpErrors(root, i, errors, convertor);
    }

    /**Whether or not the given file has an error badge.
     *
     * @param file file to test
     * @param recursive true if and only if folders should be tested recursively
     * @return true if the given file or folder has an error badge
     */
    public static boolean isInError(FileObject file, boolean recursive) {
        return TaskCache.getDefault().isInError(file, recursive);
    }

    /**Return all files with error badge under the given source root
     *
     * @param root source root to test
     * @return all files with error badge under the given root
     */
    public static Collection<? extends URL> getAllFilesInError(URL root) throws IOException {
        return Collections.unmodifiableCollection(TaskCache.getDefault().getAllFilesInError(root));
    }

    /**Return all files with error or warning badge under the given source root
     *
     * @param root source root to test
     * @return all files with error or warning badge under the given root
     * @since 9.36
     */
    public static Collection<? extends URL> getAllFilesWithRecord(URL root) throws IOException {
        return Collections.unmodifiableCollection(TaskCache.getDefault().getAllFilesWithRecord(root));
    }

    /**Getter for properties of the given error description.
     */
    public static interface Convertor<T> {
        public ErrorKind getKind(T t);
        public int       getLineNumber(T t);
        public String    getMessage(T t);
    }

    public static enum ErrorKind {
        /**Error, that should be used to show error badge in the projects tab.
         */
        ERROR,
        /**Error, that should be not used to show error badge in the projects tab.
         */
        ERROR_NO_BADGE,
        /**Warning.
         */
        WARNING;
    }

    private ErrorsCache() {}
}
