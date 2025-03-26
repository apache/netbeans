/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.editor.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

// TODO web.beans would be a better module for this, but
// that would cause cyclic dependencies due to JsfSupport - perhaps
// there is another way to get named beans?
@ServiceProvider(service=org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class WebBeansELVariableResolver implements ELVariableResolver {

    private static final String CONTENT_NAME = "NamedBeans"; //NOI18N

    @Override
    public FieldInfo getInjectableField(String beanName, FileObject target, ResolverContext context) {
        for (WebBean bean : getWebBeans(target, context)) {
            if (beanName.equals(bean.getName())) {
                return new FieldInfo(bean.getEnclodingClass(), bean.getBeanClassName());
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject target, ResolverContext context) {
        for (WebBean bean : getWebBeans(target, context)) {
            if (clazz.equals(bean.getBeanClassName())) {
                return bean.getName();
            }
        }
        return null;
    }

//    @Override
//    public String getReferredExpression(Snapshot snapshot, int offset) {
//        return null;
//    }

    @Override
    public List<VariableInfo> getManagedBeans(FileObject target, ResolverContext context) {
        List<WebBean> beans = getWebBeans(target, context);
        List<VariableInfo> result = new ArrayList<>(beans.size());
        for (WebBean bean : beans) {
            result.add(VariableInfo.createResolvedVariable(bean.getName(), bean.getBeanClassName()));
        }
        return result;
    }

    @Override
    public List<VariableInfo> getVariables(Snapshot snapshot, int offset, ResolverContext context) {
        return Collections.emptyList();
    }

    @Override
    public List<VariableInfo> getBeansInScope(String scope, Snapshot snapshot, ResolverContext context) {
        return Collections.emptyList();
    }

    @Override
    public List<VariableInfo> getRawObjectProperties(String name, Snapshot snapshot, ResolverContext context) {
        return Collections.emptyList();
    }

    private List<WebBean> getWebBeans(FileObject target, ResolverContext context) {
        JsfSupportImpl jsfSupport = JsfSupportImpl.findFor(target);
        if (jsfSupport == null) {
            return Collections.<WebBean>emptyList();
        } else {
            if (context.getContent(CONTENT_NAME) == null) {
                context.setContent(CONTENT_NAME, getNamedBeans(jsfSupport.getWebBeansModel()));
            }
            return (List<WebBean>) context.getContent(CONTENT_NAME);
        }
    }

    private static List<WebBean> getNamedBeans(MetadataModel<WebBeansModel> webBeansModel) {
        try {
            return webBeansModel.runReadAction((WebBeansModel metadata) -> {
                List<Element> namedElements = metadata.getNamedElements();
                List<WebBean> webBeans = new LinkedList<>();
                for (Element e : namedElements) {
                    //filter out null elements - probably a WebBeansModel bug,
                    //happens under some circumstances when renaming/deleting beans
                    if (e != null) {
                        webBeans.add(new WebBean(e, metadata.getName(e)));
                    }
                }
                return webBeans;
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return Collections.emptyList();
    }

     private static final class WebBean {

        private Element element;
        private String name;

        private WebBean(Element element, String name) {
            this.element = element;
            this.name = name;
        }

        private Element getElement() {
            return element;
        }

        public String getBeanClassName() {
            if (getElement() instanceof ExecutableElement) {
                ExecutableElement methodElement = (ExecutableElement) getElement();
                String returnType = methodElement.getReturnType().toString();
                int genericOffset = returnType.indexOf('<');
                return genericOffset == -1 ? returnType : returnType.substring(0, genericOffset);
            } else {
                return getElement().asType().toString();
            }
        }

        public String getName() {
            return name;
        }

        private String getEnclodingClass() {
            if (getElement() instanceof ExecutableElement) {
                ExecutableElement methodElement = (ExecutableElement) getElement();
                return methodElement.getEnclosingElement().asType().toString();
            } else {
                return getElement().asType().toString();
            }
        }
    }

}
