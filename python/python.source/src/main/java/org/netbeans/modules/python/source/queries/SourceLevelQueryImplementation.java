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

package org.netbeans.modules.python.source.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 *
 * Permits providers to return specification source level of Java source file.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.python.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Python Project module and uses this style.
 * </p>
 * @see org.netbeans.modules.python.source.queries.SourceLevelQuery
 * @author Tomas Zezula
 * @author Ralph Benjamin Ruijs
 */
public interface SourceLevelQueryImplementation {

    /**
     * Returns source level of the given Python file.
     * @param pythonFile Python source file in question
     * @return source level of the Python file encapsulated as {@link Result}, or
     *    null if the file is not handled by this provider.
     */
    Result getSourceLevel(FileObject pythonFile);

    /**
     * Result of finding source level, encapsulating the answer as well as the
     * ability to listen to it.
     */
    interface Result {

        /**
         * Get the source level.
         * @return a source level of the Python file, e.g. "2.6", "3.0", "3.1"
         * or null if the source level is unknown. It is allowed to return source level synonyms
         *    e.g. "3" for "Python 3". These synonyms are always normalized by
         * {@link SourceLevelQuery#getSourceLevel}.
         */
        @CheckForNull String getSourceLevel();

        /**
         * Add a listener to changes of source level.
         * @param listener a listener to add
         */
        void addChangeListener(@NonNull ChangeListener listener);

        /**
         * Remove a listener to changes of source level.
         * @param listener a listener to add
         */
        void removeChangeListener(@NonNull ChangeListener listener);
    }
}
