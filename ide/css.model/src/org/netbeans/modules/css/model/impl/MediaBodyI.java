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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.MediaBody;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.Rule;

/**
 *
 * @author marekfukala
 */
public class MediaBodyI extends ModelElement implements MediaBody {

    private List<MediaBodyItem> items = new ArrayList<>();
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(MediaBodyItem mediaBodyItem) {
            items.add(mediaBodyItem);
        }
        
    };

    public MediaBodyI(Model model) {
        super(model);
        
        addTextElement("\n");
        addEmptyElement(MediaBodyItem.class);
        addTextElement("\n");
    }

    public MediaBodyI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected Class getModelClass() {
        return MediaBody.class;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public List<Rule> getRules() {
        List<Rule> rules = new ArrayList<>();
        for(MediaBodyItem item : items) {
            Rule rule = item.getRule();
            if(rule != null) {
                rules.add(rule);
            }
        }
        return rules;
    }
    
    @Override
    public List<Page> getPages() {
        List<Page> pages = new ArrayList<>();
        for(MediaBodyItem item : items) {
            Page page = item.getPage();
            if(page != null) {
                pages.add(page);
            }
        }
        return pages;
    }

    @Override
    public void addRule(Rule rule) {
        MediaBodyItem mediaBodyItem = ((ElementFactoryImpl)model.getElementFactory()).createMediaBodyItem();
        mediaBodyItem.setRule(rule);

        int index;
        if(isArtificialElement()) {
            index = setElement(mediaBodyItem, true);
        } else {
            //insert before last element (should be PlainElement("})
            index = getElementsCount() - 1;
            insertElement(index, mediaBodyItem);
        }
        insertElement(index + 1, model.getElementFactory().createPlainElement("\n"));
    }

    @Override
    public void addPage(Page page) {
        MediaBodyItem mediaBodyItem = ((ElementFactoryImpl)model.getElementFactory()).createMediaBodyItem();
        mediaBodyItem.setPage(page);

        int index = setElement(mediaBodyItem, true);
        insertElement(index + 1, model.getElementFactory().createPlainElement("\n"));
    }

}
