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
package org.netbeans.modules.php.editor.completion;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@ServiceProvider(service = CompletionProvider.class, path = "Neon/completion/type") //NOI18N
public final class PhpTypeCompletionProviderWrapper implements CompletionProvider {

    @Override
    public Set<String> getItems(FileObject sourceFile, String prefix) {
        return PhpTypeCompletionProvider.getInstance().getItems(sourceFile, prefix);
    }

    public static final class PhpTypeCompletionProvider implements CompletionProvider {
        private static final PhpTypeCompletionProvider INSTANCE = new PhpTypeCompletionProvider();
        //@GuardedBy("this")
        private Set<TypeElement> cachedElements;

        private PhpTypeCompletionProvider() {
        }

        public static PhpTypeCompletionProvider getInstance() {
            return INSTANCE;
        }

        @Override
        public Set<String> getItems(FileObject sourceFile, String prefix) {
            Set<String> result = new HashSet<>();
            for (TypeElement typeElement : ElementFilter.forName(NameKind.prefix(prefix)).filter(getElements(sourceFile))) {
                if (!CodeUtils.isSyntheticTypeName(typeElement.getName())) {
                    result.add(typeElement.getFullyQualifiedName().toString());
                }
            }
            return result;
        }

        private synchronized Set<TypeElement> getElements(FileObject fileObject) {
            if (cachedElements == null) {
                ElementQuery.Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(fileObject));
                cachedElements = indexQuery.getTypes(NameKind.empty());
            }
            return cachedElements;
        }

        public synchronized void clearCache() {
            cachedElements = null;
        }

    }

}
