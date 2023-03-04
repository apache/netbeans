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
package org.netbeans.modules.css.model.api;

import java.util.List;

/**
 *
 * @author marekfukala
 */
public interface Body extends Element {
    
    /**
     * List of body items as declared in the source. 
     */
    public List<BodyItem> getBodyItems();
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the rule type
     */
    public List<Rule> getRules();
    
    /**
     * Adds a new Rule at the end of the Body
     */
    public void addRule(Rule rule);
    
    /**
     * Remove the Rule element
     */
    public boolean removeRule(Rule rule);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the media type
     */
    public List<Media> getMedias();
    
    /**
     * Adds a new Media at the end of the Body
     */
    public void addMedia(Media media);
    
    /**
     * Remove the Media element
     */
    public boolean removeMedia(Media media);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the page type
     */
    public List<Page> getPages();
    
    /**
     * Adds a new Page at the end of the Body
     */
    public void addPage(Page media);
    
    /**
     * Remove the Page element
     */
    public boolean removePage(Page page);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the page type
     */
    public List<FontFace> getFontFaces();
    
    /**
     * Adds a new FontFace at the end of the Body
     */
    public void addFontFace(FontFace fontFace);
    
    /**
     * Remove the FontFace element
     */
    public boolean removeFontFace(FontFace fontFace);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the GenericAtRule type
     */
    public List<GenericAtRule> getGenericAtRules();
    
    /**
     * Adds a new GenericAtRule at the end of the Body
     */
    public void addGenericAtRule(GenericAtRule genericAtRule);
    
    /**
     * Remove the GenericAtRule element
     */
    public boolean removeGenericAtRule(GenericAtRule genericAtRule);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the MozDocument type
     */
    public List<MozDocument> getMozDocuments();
    
    /**
     * Adds a new MozDocument at the end of the Body
     */
    public void addMozDocument(MozDocument mozDocument);
    
    /**
     * Remove the MozDocument element
     */
    public boolean removeMozDocument(MozDocument mozDocument);
    
    /**
     * Unmodifiable subset of the body items - contains just body items of the WebkitKeyFrames type
     */
    public List<WebkitKeyframes> getWebkitKeyFrames();
    
    /**
     * Adds a new WebkitKeyFrames at the end of the Body
     */
    public void addWebkitKeyFrames(WebkitKeyframes webkitKeyFrames);
    
    /**
     * Remove the GenericAtRule element
     */
    public boolean removeWebkitKeyFrames(WebkitKeyframes webkitKeyFrames);
    
    
}
