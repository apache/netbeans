/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
