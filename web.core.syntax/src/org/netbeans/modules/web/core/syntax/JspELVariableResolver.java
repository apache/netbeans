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
