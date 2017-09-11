/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
