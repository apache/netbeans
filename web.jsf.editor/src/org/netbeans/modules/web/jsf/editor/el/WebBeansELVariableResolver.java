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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            return webBeansModel.runReadAction(new MetadataModelAction<WebBeansModel, List<WebBean>>() {

                @Override
                public List<WebBean> run(WebBeansModel metadata) throws Exception {
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
                }
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
