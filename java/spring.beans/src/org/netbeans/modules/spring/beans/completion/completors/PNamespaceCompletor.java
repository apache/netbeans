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
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;

/**
 *
 * @author Rohan Ranade
 */
public class PNamespaceCompletor extends Completor {

    public PNamespaceCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        String typedPrefix = context.getTypedPrefix();
        return context.getCaretOffset() - typedPrefix.length();
    }

    @Override
    protected void compute(final CompletionContext context) throws IOException {
        final JavaSource js = JavaUtils.getJavaSource(context.getFileObject());
        if (js == null) {
            return;
        }

        final String typedPrefix = context.getTypedPrefix();
        final String pNamespacePrefix = context.getDocumentContext().getNamespacePrefix(ContextUtilities.P_NAMESPACE);
        final int substitutionOffset = context.getCaretOffset() - typedPrefix.length();
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                String className = new BeanClassFinder(SpringXMLConfigEditorUtils.getTagAttributes(context.getTag()),
                        context.getFileObject()).findImplementationClass(true);
                if (className == null) {
                    return;
                }
                TypeElement te = JavaUtils.findClassElementByBinaryName(className, cc);
                if (te == null) {
                    return;
                }
                ElementUtilities eu = cc.getElementUtilities();
                Property[] props = new PropertyFinder(te.asType(), "", eu, MatchType.PREFIX).findProperties(); // NOI18N

                for (Property prop : props) {
                    if (prop.getSetter() == null) {
                        continue;
                    }
                    String attribName = pNamespacePrefix + ":" + prop.getName(); // NOI18N

                    if (!context.getExistingAttributes().contains(attribName) && attribName.startsWith(typedPrefix)) {
                        SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createPropertyAttribItem(substitutionOffset,
                                attribName, prop);
                        addCacheItem(item);
                    }
                    attribName += "-ref"; // NOI18N
                    if (!context.getExistingAttributes().contains(attribName) && attribName.startsWith(typedPrefix)) {
                        SpringXMLConfigCompletionItem refItem = SpringXMLConfigCompletionItem.createPropertyAttribItem(substitutionOffset,
                                attribName, prop); // NOI18N
                        addCacheItem(refItem);
                    }
                }
            }
        }, true);
    }

    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.P_ATTRIBUTE_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
    }
}
