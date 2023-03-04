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
