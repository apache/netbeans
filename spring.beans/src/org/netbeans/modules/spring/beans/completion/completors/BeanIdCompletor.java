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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.FileSpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.FieldNamesCalculator;
import org.netbeans.modules.spring.java.JavaUtils;
import org.openide.filesystems.FileObject;

/**
 * Completion for bean id values. Looks at the bean's implementation class
 * 
 * TODO: look at bean implementation class' parentage 
 * (implemented interfaces/superclass(es)) for additional names 
 * (perhaps as 2 step completion)
 * 
 * TODO: short forms : BadLocationException => "ble"
 * 
 * @author Rohan Ranade
 */
public final class BeanIdCompletor extends Completor {

    public BeanIdCompletor(int invocationOffset) {
        super(invocationOffset);
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        return context.getCurrentTokenOffset() + 1;
    }

    @Override
    protected void compute(final CompletionContext context) throws IOException {
        final FileObject fileObject = context.getFileObject();
        JavaSource javaSource = JavaUtils.getJavaSource(fileObject);
        if (javaSource == null) {
            return;
        }

        final String typedPrefix = context.getTypedPrefix();

        javaSource.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) throws Exception {
                Map<String, String> tagAttributes = SpringXMLConfigEditorUtils.getTagAttributes(context.getTag());
                String beanClassName = new BeanClassFinder(tagAttributes, fileObject).findImplementationClass(true);
                if (beanClassName == null) {
                    return;
                }
                TypeElement beanType = JavaUtils.findClassElementByBinaryName(beanClassName, cc);
                if (beanType == null) {
                    return;
                }

                FieldNamesCalculator calculator = new FieldNamesCalculator(beanType.getSimpleName().toString(), getForbiddenNames(fileObject));
                List<String> names = calculator.calculate();

                int i = 10;
                for (String name : names) {
                    if (name.startsWith(typedPrefix)) {
                        SpringXMLConfigCompletionItem item = SpringXMLConfigCompletionItem.createBeanNameItem(getAnchorOffset(), name, i);
                        addCacheItem(item);
                    i += 10;
                }
            }
            }
        }, true);
    }

    private Set<String> getForbiddenNames(final FileObject fileObject) throws IOException {
        SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
        if (model == null) {
            return (Collections.emptySet());
        }
        final Set<String> names = new HashSet<String>();
        model.runReadAction(new Action<SpringBeans>() {

            public void run(SpringBeans sb) {
                FileSpringBeans fileBeans = sb.getFileBeans(fileObject);
                if (fileBeans == null) {
                    return;
                }

                for (SpringBean bean : fileBeans.getBeans()) {
                    names.add(bean.getId());
                    names.addAll(bean.getNames());
                }
            }
        });

        return Collections.unmodifiableSet(names);
    }
}
