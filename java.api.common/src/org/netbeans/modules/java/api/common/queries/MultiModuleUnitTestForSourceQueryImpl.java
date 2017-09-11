/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.queries;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;


/**
 * An implementation of {@link MultipleRootsUnitTestForSourceQueryImplementation} for a multi-module project.
 * @author Tomas Zezula
 */
final class MultiModuleUnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private final MultiModule sourceModules;
    private final MultiModule testModules;

    MultiModuleUnitTestForSourceQueryImpl(
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("sourceModules", sourceModules);     //NOI18N
        Parameters.notNull("testModules", testModules);         //NOI18N
        this.sourceModules = sourceModules;
        this.testModules = testModules;
    }

    @Override
    public URL[] findUnitTests(FileObject source) {
        return map(source, sourceModules, testModules);
    }

    @Override
    public URL[] findSources(FileObject unitTest) {
        return map(unitTest, testModules, sourceModules);
    }

    @CheckForNull
    private static URL[] map(
            @NonNull final FileObject artefact,
            @NonNull final MultiModule from,
            @NonNull final MultiModule to) {
        final String moduleName = from.getModuleName(artefact);
        if (moduleName == null) {
            return null;
        }
        final ClassPath srcPath = to.getModuleSources(moduleName);
        if (srcPath == null) {
            return null;
        }
        return srcPath.entries().stream()
                .map((e) -> e.getURL())
                .toArray((len) -> new URL[len]);
    }
}
