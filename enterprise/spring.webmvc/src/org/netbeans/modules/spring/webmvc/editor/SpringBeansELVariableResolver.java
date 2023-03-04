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
package org.netbeans.modules.spring.webmvc.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.api.beans.model.SpringModel;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class SpringBeansELVariableResolver implements ELVariableResolver {

    private static final Logger LOG = Logger.getLogger(SpringBeansELVariableResolver.class.getName());

    private static final String CONTENT_NAME = "SpringBeans"; //NOI18N
    
    @Override
    public FieldInfo getInjectableField(String beanName, FileObject target, ResolverContext context) {
        for (SpringBean bean : getSpringBeans(target, context)) {
            if (beanName.equals(getBeanName(bean))) {
                return new FieldInfo(bean.getClassName());
            }
        }
        return null;
    }

    @Override
    public String getBeanName(String clazz, FileObject target, ResolverContext context) {
        for (SpringBean bean : getSpringBeans(target, context)) {
            if (clazz.equals(bean.getClassName())) {
                return getBeanName(bean);
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
        List<SpringBean> beans = getSpringBeans(target, context);
        List<VariableInfo> result = new ArrayList<VariableInfo>(beans.size());
        for (SpringBean bean : beans) {
            String beanName = getBeanName(bean);
            if(beanName == null) {
                continue; //an invalid bean?
            }
            result.add(VariableInfo.createResolvedVariable(getBeanName(bean), bean.getClassName()));
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

    private List<SpringBean> getSpringBeans(FileObject target, ResolverContext context) {
        SpringScope scope = SpringScope.getSpringScope(target);
        if (scope == null) {
            return Collections.<SpringBean>emptyList();
        } else {
            if (context.getContent(CONTENT_NAME) == null) {
                context.setContent(CONTENT_NAME, getBeansFromScope(target, scope));
            }
            return (List<SpringBean>) context.getContent(CONTENT_NAME);
        }
    }

    private List<SpringBean> getBeansFromScope(FileObject target, SpringScope scope) {
        final List<SpringBean> springBeans = new ArrayList<SpringBean>();

        // gets all Spring beans from config files
        for (SpringConfigModel model : scope.getAllConfigModels()) {
            try {
                model.runReadAction(new Action<SpringBeans>() {

                    @Override
                    public void run(SpringBeans beans) {
                        springBeans.addAll(beans.getBeans());
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // gets all Spring beans from annotation support
        springBeans.addAll(getAnnotatedBeans(scope.getSpringAnnotationModel(target)));

        return springBeans;
    }

    private static String getBeanName(SpringBean bean) {
        String beanName = null;
        for (String name : bean.getNames()) {
            beanName = name;
            break;
        }
        if (beanName == null) {
            beanName = bean.getId();
        }

        return beanName;
    }
    
    private static List<SpringBean> getAnnotatedBeans(MetadataModel<SpringModel> model) {
        try {
            return model.runReadAction(new MetadataModelAction<SpringModel, List<SpringBean>>() {

                @Override
                public List<SpringBean> run(SpringModel model) throws Exception {
                    List<SpringBean> beans = new LinkedList<SpringBean>();
                    beans.addAll(model.getBeans());
                    return beans;
                }
            });
        } catch (MetadataModelException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        } catch (IOException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return Collections.<SpringBean>emptyList();
    }
    
}
