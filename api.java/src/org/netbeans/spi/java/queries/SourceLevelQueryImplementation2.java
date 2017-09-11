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

package org.netbeans.spi.java.queries;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;

/**
 *
 * Permits providers to return specification source level of Java source file.
 * <p>
 * A default implementation is registered by the
 * <code>org.netbeans.modules.java.project</code> module which looks up the
 * project corresponding to the file (if any) and checks whether that
 * project has an implementation of this interface in its lookup. If so, it
 * delegates to that implementation. Therefore it is not generally necessary
 * for a project type provider to register its own global implementation of
 * this query, if it depends on the Java Project module and uses this style.
 * </p>
 * @see org.netbeans.api.java.queries.SourceLevelQuery
 * @see org.netbeans.api.queries.FileOwnerQuery
 * @see org.netbeans.api.project.Project#getLookup
 * @see org.netbeans.api.java.classpath.ClassPath#BOOT
 * @author Tomas Zezula
 * @since 1.30
 */
public interface SourceLevelQueryImplementation2 {

    /**
     * Returns source level of the given Java file. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of
     * <code>javac</code> compiler .
     * @param javaFile Java source file in question
     * @return source level of the Java file encapsulated as {@link Result}, or
     *    null if the file is not handled by this provider.
     */
    Result getSourceLevel(FileObject javaFile);

    /**
     * Result of finding source level, encapsulating the answer as well as the
     * ability to listen to it.
     * @since 1.30
     */
    interface Result {

        /**
         * Get the source level.
         * @return a source level of the Java file, e.g. "1.3", "1.4", "1.5"
         * or null if the source level is unknown. It is allowed to return source level synonyms
         *    e.g. "5" for "1.5". These synonyms are always normalized by
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

    /**
     * Result of finding a source level and profile as well as the ability
     * to listen on changes.
     * @since 1.47
     */
    interface Result2 extends Result {

        /**
         * Returns the required profile.
         * @return the required profile. If the profile is either unknown
         * or unsupported by actual source level it returns the {@link SourceLevelQuery.Profile#DEFAULT}.
         * <div class="nonnormative">
         * The JDK 8 provides three limited profiles (compact1, compact2, compact3) in addition
         * to the full JDK. Each profile specifies a specific set of Java API packages and
         * contains all of the APIs of the smaller profile, @see http://openjdk.java.net/jeps/161
         * </div>
         */
        @NonNull
        SourceLevelQuery.Profile getProfile();
    }
}
