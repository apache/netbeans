/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.api.queries;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

/**
 * Factory for all queries.
 * @since 2.24
 */
public final class Queries {

    private static final PhpVisibilityQuery DEFAULT_PHP_VISIBILITY_QUERY = new DefaultPhpVisibilityQuery();


    private Queries() {
    }

    /**
     * Get PHP visibility query for the given PHP module. If the PHP module is {@code null},
     * {@link VisibilityQuery#getDefault() default} visibility query is returned.
     * @param phpModule PHP module, can be {@code null}
     * @return PHP visibility query
     */
    public static PhpVisibilityQuery getVisibilityQuery(@NullAllowed PhpModule phpModule) {
        if (phpModule == null) {
            return DEFAULT_PHP_VISIBILITY_QUERY;
        }
        PhpVisibilityQuery visibilityQuery = phpModule.getLookup().lookup(PhpVisibilityQuery.class);
        assert visibilityQuery != null : "No php visibility query for php module " + phpModule.getClass().getName();
        return visibilityQuery;
    }

    //~ Inner classes

    private static final class DefaultPhpVisibilityQuery implements PhpVisibilityQuery {

        @Override
        public boolean isVisible(File file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }

        @Override
        public boolean isVisible(FileObject file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }

        @Override
        public Collection<FileObject> getIgnoredFiles() {
            return Collections.emptyList();
        }

        @Override
        public Collection<FileObject> getCodeAnalysisExcludeFiles() {
            return Collections.emptyList();
        }

    }

}
