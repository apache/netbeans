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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.beans.BeansElements;
import org.netbeans.modules.spring.beans.completion.CompletionContext;
import org.netbeans.modules.spring.beans.completion.Completor;
import org.netbeans.modules.spring.beans.completion.CompletorUtils;
import org.netbeans.modules.spring.beans.completion.SpringXMLConfigCompletionItem;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefCompletor extends Completor {

    private final boolean includeGlobal;

    public BeansRefCompletor(boolean includeGlobal, int invocationOffset) {
        super(invocationOffset);
        this.includeGlobal = includeGlobal;
    }

    @Override
    protected int initAnchorOffset(CompletionContext context) {
        return context.getCurrentTokenOffset() + 1;
    }
    
    @Override
    protected void compute(final CompletionContext context) throws IOException {
        final FileObject fo = context.getFileObject();
        SpringConfigModel model = SpringConfigModel.forFileObject(fo);
        if (model == null) {
            return;
        }
        
        final String contextPrefix = getContextPrefix(context);
        final Set<String> forbiddenNames = getForbiddenNames(context);

        final List<String> cNames = new ArrayList<String>();
        String tagName = context.getTag().getNodeName();
        // get current bean parameters
        if (tagName.equals(BeansElements.BEAN) && SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), BeansAttributes.ID)) {
            String cId = SpringXMLConfigEditorUtils.getAttribute(context.getTag(), BeansAttributes.ID); 
            cNames.add(cId);
        }
        if (tagName.equals(BeansElements.BEAN) && SpringXMLConfigEditorUtils.hasAttribute(context.getTag(), BeansAttributes.NAME)) { 
            List<String> names = StringUtils.tokenize(
                    SpringXMLConfigEditorUtils.getAttribute(context.getTag(), BeansAttributes.NAME),
                    SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS);
            cNames.addAll(names);
        }

        if(isCancelled()) {
            return;
        }
        
        model.runReadAction(new Action<SpringBeans>() {

            public void run(SpringBeans sb) {
                Map<String, SpringBean> name2Bean = getName2Beans(sb); // if local beans, then add only bean ids;

                for (Map.Entry<String, SpringBean> it : name2Bean.entrySet()) {
                    String beanName = it.getKey();
                    if (!beanName.startsWith(contextPrefix) || cNames.contains(beanName) || forbiddenNames.contains(beanName)) {
                        continue;
                    }
                    SpringBean bean = it.getValue();
                    
                    SpringXMLConfigCompletionItem item =
                            SpringXMLConfigCompletionItem.createBeanRefItem(getAnchorOffset(), beanName, bean, fo);
                    addCacheItem(item);
                }
            }

            private Map<String, SpringBean> getName2Beans(SpringBeans sb) {
                List<SpringBean> beans = includeGlobal ? sb.getBeans() : sb.getFileBeans(fo).getBeans();
                Map<String, SpringBean> name2Bean = new HashMap<String, SpringBean>();
                for (SpringBean bean : beans) {
                    String beanId = bean.getId();
                    if (beanId != null) {
                        name2Bean.put(beanId, bean);
                    }
                    if (includeGlobal) {
                        List<String> beanNames = bean.getNames();
                        for (String beanName : beanNames) {
                            name2Bean.put(beanName, bean);
                        }
                    }
                }
                
                // handle aliases also
                if(includeGlobal) {
                    Set<String> aliases = sb.getAliases();
                    for (String alias : aliases) {
                        SpringBean bean = sb.findBean(alias);
                        if (bean != null) {
                            name2Bean.put(alias, bean);
                        }
                    }
                }

                return name2Bean;
            }
        });
    }

    @Override
    public boolean canFilter(CompletionContext context) {
        return CompletorUtils.canFilter(context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset(), CompletorUtils.BEAN_NAME_ACCEPTOR);
    }

    @Override
    protected List<SpringXMLConfigCompletionItem> doFilter(CompletionContext context) {
        return CompletorUtils.filter(getCacheItems(), context.getDocument(), getInvocationOffset(), context.getCaretOffset(), getAnchorOffset());
    }
    
    protected String getContextPrefix(CompletionContext context) {
        return context.getTypedPrefix();
    }
    
    protected Set<String> getForbiddenNames(CompletionContext context) {
        return Collections.emptySet();
    }
}
