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
import org.netbeans.modules.languages.neon.spi.completion.MethodCompletionProvider;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class PhpMethodCompletionProvider implements MethodCompletionProvider {
    private static final PhpMethodCompletionProvider INSTANCE = new PhpMethodCompletionProvider();

    @MethodCompletionProvider.Registration(position = 100)
    public static PhpMethodCompletionProvider getInstance() {
        return INSTANCE;
    }

    private PhpMethodCompletionProvider() {
    }

    @Override
    public Set<String> complete(String prefix, String typeName, FileObject fileObject) {
        Set<String> result = new HashSet<>();
        if (typeName != null && !typeName.isEmpty()) {
            ElementQuery.Index indexQuery = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(fileObject));
            Set<TypeElement> types = indexQuery.getTypes(NameKind.prefix(typeName));
            for (TypeElement typeElement : types) {
                Set<MethodElement> accessibleMethods = indexQuery.getAccessibleMethods(typeElement, typeElement);
                Set<MethodElement> filteredMethods = ElementFilter.forName(NameKind.prefix(prefix)).filter(accessibleMethods);
                for (MethodElement methodElement : filteredMethods) {
                    result.add(methodElement.asString(BaseFunctionElement.PrintAs.NameAndParamsInvocation).trim());
                }
            }
        }
        return result;
    }

}
