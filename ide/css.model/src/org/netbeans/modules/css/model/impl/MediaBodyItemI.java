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
package org.netbeans.modules.css.model.impl;

import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.Rule;

/**
 * @author marekfukala
 */
public class MediaBodyItemI extends ModelElement implements MediaBodyItem {

    private Page page;
    private Rule rule;

    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(Rule element) {
            rule = element;
        }

        @Override
        public void elementAdded(Page element) {
            page = element;
        }
        
    };

    public MediaBodyItemI(Model model) {
        super(model);
        
        addEmptyElement(Rule.class);
        addEmptyElement(Page.class);
    }

    public MediaBodyItemI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public Page getPage() {
        return page;
    }
    
    @Override
    public void setRule(Rule rule) {
        setElement(rule);
        this.rule = rule;
    }
    
    @Override
    public void setPage(Page page) {
        setElement(page);
        this.page = page;
    }
    
    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }
    
    @Override
    protected Class getModelClass() {
        return MediaBodyItem.class;
    }
    
}
