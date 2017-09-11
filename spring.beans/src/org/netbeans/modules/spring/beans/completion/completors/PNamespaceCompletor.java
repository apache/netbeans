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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
