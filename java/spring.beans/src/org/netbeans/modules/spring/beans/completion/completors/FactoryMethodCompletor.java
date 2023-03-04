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
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class FactoryMethodCompletor extends JavaMethodCompletor {

    private Static staticFlag = Static.YES;

    public FactoryMethodCompletor(int invocationOffset) {
        super(invocationOffset);
    }
    
    @Override
    protected Public getPublicFlag(CompletionContext context) {
        return Public.DONT_CARE;
    }

    @Override
    protected Static getStaticFlag(CompletionContext context) {
        return staticFlag;
    }

    @Override
    protected int getArgCount(CompletionContext context) {
        return -1;
    }

    @Override
    protected String getTypeName(CompletionContext context) {
        Node tag = context.getTag();
        SpringBean mergedBean = SpringXMLConfigEditorUtils.getMergedBean(SpringXMLConfigEditorUtils.getTagAttributes(tag),
                context.getFileObject());
        if (mergedBean == null) {
            return null;
        }

        final String[] className = {mergedBean.getClassName()};

        // if factory-bean has been defined, resolve it and get it's class name
        if (mergedBean.getFactoryBean() != null) {
            final String factoryBeanName = mergedBean.getFactoryBean();
            FileObject fo = context.getFileObject();
            SpringConfigModel model = SpringConfigModel.forFileObject(fo);
            if (model == null) {
                return null;
            }
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    public void run(SpringBeans beans) {
                        SpringBean bean = beans.findBean(factoryBeanName);
                        if (bean == null) {
                            className[0] = null;
                            return;
                        }
                        className[0] = bean.getClassName();
                    }
                });
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                className[0] = null;
            }

            staticFlag = Static.NO;
        }

        return className[0];
    }

    @Override
    protected Iterable<? extends Element> filter(Iterable<? extends Element> methods) {
        List<ExecutableElement> ret = new ArrayList<ExecutableElement>();
        for (Element e : methods) {
            ExecutableElement method = (ExecutableElement) e;
            if (method.getReturnType().getKind() != TypeKind.VOID) {
                ret.add(method);
            }
        }

        return ret;
    }
}
