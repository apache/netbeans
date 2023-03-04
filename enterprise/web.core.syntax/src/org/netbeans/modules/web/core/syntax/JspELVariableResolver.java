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
package org.netbeans.modules.web.core.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.ResolverContext;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=org.netbeans.modules.web.el.spi.ELVariableResolver.class)
public final class JspELVariableResolver implements ELVariableResolver {

    private static final String CONTENT_NAME = "JspBeans"; //NOI18N

    @Override
    public FieldInfo getInjectableField(String beanName, FileObject target, ResolverContext context) {
        for (VariableInfo bean : getManagedBeans(target, context)) {
            if (beanName.equals(bean.name)) {
                return new FieldInfo(bean.clazz);
            }
        }
        return null;
    }
    
    @Override
    public String getBeanName(String clazz, FileObject target, ResolverContext context) {
        for (VariableInfo bean : getManagedBeans(target, context)) {
            if (clazz.equals(bean.clazz)) {
                return bean.name;
            }
        }
        return null;
    }

    @Override
    public List<VariableInfo> getManagedBeans(FileObject target, ResolverContext context) {
        JspParserAPI.ParseResult result = JspUtils.getCachedParseResult(target, true, false);
        if (result == null) {
            //workaround for ??? copied from JspSyntaxSupport.getParseResult()
            result = JspUtils.getCachedParseResult(target, false, false);
        }

        if( result == null) {
            return Collections.emptyList();
        } else {
            if (context.getContent(CONTENT_NAME) == null) {
                context.setContent(CONTENT_NAME, getBeans(result));
            }
            return (List<VariableInfo>) context.getContent(CONTENT_NAME);
        }
    }

    public List<VariableInfo> getBeans(JspParserAPI.ParseResult result) {
        PageInfo pageInfo = result.getPageInfo();
        if(pageInfo == null) {
            return Collections.emptyList();
        }

        BeanData[] beans = pageInfo.getBeans();

        List<VariableInfo> beansVars = new ArrayList<VariableInfo>(beans.length);
        for(BeanData bd : beans) {
            beansVars.add(VariableInfo.createResolvedVariable(bd.getId(), bd.getClassName()));
        }
        return beansVars;
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

    
}
