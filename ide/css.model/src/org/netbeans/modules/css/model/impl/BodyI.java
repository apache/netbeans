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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.model.api.AtRule;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.BodyItem;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.FontFace;
import org.netbeans.modules.css.model.api.GenericAtRule;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.MozDocument;
import org.netbeans.modules.css.model.api.Page;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.VendorAtRule;
import org.netbeans.modules.css.model.api.WebkitKeyframes;

/**
 *
 * @author marekfukala
 */
public class BodyI extends ModelElement implements Body {

    private final List<BodyItem> bodyItems = new ArrayList<>();
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(BodyItem bodyItem) {
            bodyItems.add(bodyItem);
        }

        @Override
        public void elementRemoved(BodyItem bodyItem) {
            bodyItems.remove(bodyItem);
        }
        
    };

    public BodyI(Model model) {
        super(model);
    }

    public BodyI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public List<BodyItem> getBodyItems() {
        return Collections.unmodifiableList(bodyItems);
    }
    
    @Override
    public List<Rule> getRules() {
        List<Rule> rules = new ArrayList<>();
        for(BodyItem bi : getBodyItems()) {
            if(bi.getElement() instanceof Rule) {
                rules.add((Rule)bi.getElement());
            }
        }
        return Collections.unmodifiableList(rules);
    }
    
    @Override
    public void addRule(Rule rule) {
        BodyItem bi = model.getElementFactory().createBodyItem();
        bi.setElement(rule);
        addElement(bi);
    }
    
    @Override
    public boolean removeRule(Rule rule) {
        return removeBodyItemChild(rule);
    }
    
    private List<AtRule> getAtRules() {
        List<AtRule> atRules = new ArrayList<>();
        for(Element e : getBodyItems()) {
            if(e instanceof BodyItem) {
                Element biElement = ((BodyItem)e).getElement();
                if(biElement instanceof AtRule) {
                    AtRule atr = (AtRule)biElement;
                    atRules.add(atr);
                }
            }
        }
        return atRules;
    }
    
    @Override
    public List<Media> getMedias() {
        List<Media> rules = new ArrayList<>();
        for(AtRule bi : getAtRules()) {
            if(bi.getElement() instanceof Media) {
                rules.add((Media)bi.getElement());
            }
        }
        return Collections.unmodifiableList(rules);
    }
    
    @Override
    public void addMedia(Media media) {
        BodyItem bi = model.getElementFactory().createBodyItem();
        AtRule atr = model.getElementFactory().createAtRule();
        atr.setElement(media);
        bi.setElement(atr);
        addElement(bi);
    }
    
    @Override
    public boolean removeMedia(Media media) {
        return removeBodyItemChild(media);
    }
    
    @Override
    public List<Page> getPages() {
        List<Page> rules = new ArrayList<>();
        for(AtRule bi : getAtRules()) {
            if(bi.getElement() instanceof Page) {
                rules.add((Page)bi.getElement());
            }
        }
        return Collections.unmodifiableList(rules);
    }
    
    @Override
    public void addPage(Page page) {
        BodyItem bi = model.getElementFactory().createBodyItem();
        AtRule atr = model.getElementFactory().createAtRule();
        atr.setElement(page);
        bi.setElement(atr);
        addElement(bi);
    }

    @Override
    public boolean removePage(Page page) {
        return removeBodyItemChild(page);
    }
    
    @Override
    public List<FontFace> getFontFaces() {
        List<FontFace> rules = new ArrayList<>();
        for(AtRule bi : getAtRules()) {
            if(bi.getElement() instanceof FontFace) {
                rules.add((FontFace)bi.getElement());
            }
        }
        return Collections.unmodifiableList(rules);
    }
    
    @Override
    public void addFontFace(FontFace fontFace) {
        BodyItem bi = model.getElementFactory().createBodyItem();
        AtRule atr = model.getElementFactory().createAtRule();
        atr.setElement(fontFace);
        bi.setElement(atr);
        addElement(bi);
    }

    @Override
    public boolean removeFontFace(FontFace fontFace) {
        return removeBodyItemChild(fontFace);
    }
    
    private boolean removeBodyItemChild(Element element) {
        Element atRule = element.getParent();
        assert atRule != null;
        Element bodyItem = atRule.getParent();
        assert bodyItem != null;
        boolean removed = atRule.removeElement(element);
        assert removed;
        removed = bodyItem.removeElement(atRule);
        assert removed;
        return removeElement(bodyItem);
    }
    
    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return Body.class;
    }

    private List<VendorAtRule> getVendorAtRules() {
        List<VendorAtRule> rules = new ArrayList<>();
        for(AtRule bi : getAtRules()) {
            if(bi.getElement() instanceof VendorAtRule) {
                rules.add((VendorAtRule)bi.getElement());
            }
        }
        return Collections.unmodifiableList(rules);
    }
    
    private <T extends Element> List<T> getVendorAtRuleElements(Class<T> ofType) {
        List<T> rules = new ArrayList<>();
        for(VendorAtRule var : getVendorAtRules()) {
            Element element = var.getElement();
            if(ofType.isAssignableFrom(element.getClass())) {
                T t = ofType.cast(element);
                rules.add(t);
            }
        }
        return Collections.unmodifiableList(rules);
    }

    private void addVendorAtRuleMember(Element element) {
        VendorAtRule vendorAtRule = model.getElementFactory().createVendorAtRule();
        vendorAtRule.setElement(element);
        
        AtRule atr = model.getElementFactory().createAtRule();
        atr.setElement(vendorAtRule);
        
        BodyItem bi = model.getElementFactory().createBodyItem();
        bi.setElement(atr);
        
        addElement(bi);
    }
    
    @Override
    public List<GenericAtRule> getGenericAtRules() {
        return getVendorAtRuleElements(GenericAtRule.class);
    }

    @Override
    public void addGenericAtRule(GenericAtRule genericAtRule) {
        addVendorAtRuleMember(genericAtRule);
    }
    
    private boolean removeVendorAtRuleChild(Element element) {
        Element vendorAtRule = element.getParent();
        assert vendorAtRule != null;
        assert vendorAtRule instanceof VendorAtRule;
        
        return removeBodyItemChild(vendorAtRule);
    }

    @Override
    public boolean removeGenericAtRule(GenericAtRule genericAtRule) {
        return removeVendorAtRuleChild(genericAtRule);
    }

    @Override
    public List<MozDocument> getMozDocuments() {
        return getVendorAtRuleElements(MozDocument.class);
    }

    @Override
    public void addMozDocument(MozDocument mozDocument) {
        addVendorAtRuleMember(mozDocument);
    }

    @Override
    public boolean removeMozDocument(MozDocument mozDocument) {
        return removeVendorAtRuleChild(mozDocument);
    }

    @Override
    public List<WebkitKeyframes> getWebkitKeyFrames() {
        return getVendorAtRuleElements(WebkitKeyframes.class);
    }

    @Override
    public void addWebkitKeyFrames(WebkitKeyframes webkitKeyFrames) {
        addVendorAtRuleMember(webkitKeyFrames);
    }

    @Override
    public boolean removeWebkitKeyFrames(WebkitKeyframes webkitKeyFrames) {
        return removeVendorAtRuleChild(webkitKeyFrames);
    }
 

       
}
