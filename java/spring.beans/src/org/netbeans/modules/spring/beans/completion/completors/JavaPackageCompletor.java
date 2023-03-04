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
package org.netbeans.modules.spring.beans.completion.completors;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JavaPackageCompletor extends Completor {

    private static final Set<SearchScope> ALL = EnumSet.allOf(SearchScope.class);
    private static final Set<SearchScope> LOCAL = EnumSet.of(SearchScope.SOURCE);

    public JavaPackageCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        int idx = context.getCurrentTokenOffset() + 1;
        String typedChars = context.getTypedPrefix();
        if (typedChars.contains(".") || typedChars.equals("")) {
            int dotIndex = typedChars.lastIndexOf(".");
            idx += dotIndex + 1;
        }

        return idx;
    }

    @Override
    protected void compute(CompletionContext context) throws IOException {
        final String typedChars = context.getTypedPrefix();
        JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
        if (js == null) {
            return;
        }

        doPackageCompletion(js, typedChars, context.getCurrentTokenOffset() + 1);
    }

    private void doPackageCompletion(JavaSource js, final String typedPrefix, final int substitutionOffset) throws IOException {
        js.runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController cc) throws Exception {
                if (isCancelled()) {
                    return;
                }

                cc.toPhase(Phase.ELEMENTS_RESOLVED);
                ClassIndex ci = cc.getClasspathInfo().getClassIndex();
                int index = substitutionOffset;
                int dotIndex = typedPrefix.lastIndexOf('.'); // NOI18N
                if (dotIndex != -1) {
                    index += (dotIndex + 1);  // NOI18N
                }

                addPackages(ci,  typedPrefix, index, CompletionProvider.COMPLETION_ALL_QUERY_TYPE);
            }
        }, true);
    }

    private void addPackages(ClassIndex ci, String typedPrefix, int substitutionOffset, int queryType) {
        Set<SearchScope> scope = (queryType == CompletionProvider.COMPLETION_ALL_QUERY_TYPE) ? ALL : LOCAL;
        Set<String> packages = ci.getPackageNames(typedPrefix, true, scope);
        for (String pkg : packages) {
            if (pkg.length() > 0) {
                SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createPackageItem(substitutionOffset, pkg, false);
                addCacheItem(item);
            }
        }
    }
}
