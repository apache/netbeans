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

import java.util.Collection;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Various utilities for css source modifications.
 *
 * The utility methods needs to be called from within either 
 * {@link Model#runReadTask(org.netbeans.modules.css.model.api.Model.ModelTask) }
 * or null {@link Model#runWriteTask(org.netbeans.modules.css.model.api.Model.ModelTask) !!!
 *
 * TODO: add model tasks checking
 * TODO: generify the findMatchingXXX code so it works for more elements w/o extra coding
 *
 * @author marekfukala
 */
public class ModelUtils {

    private Model model;
    private StyleSheet styleSheet;
    private ElementFactory factory;

    public ModelUtils(Model model) {
        this.model = model;
        this.factory = model.getElementFactory();
        this.styleSheet = model.getStyleSheet();
    }

    /**
     * Creates a new {@link Rule}
     *
     * @param selectors list of selectors from the selector group
     * @param declarations list of declarations in the "property:value" form
     */
    public Rule createRule(Collection<String> selectors, Collection<String> declarations) {
        SelectorsGroup selectorsGroup = factory.createSelectorsGroup();
        for (String selectorName : selectors) {
            selectorsGroup.addSelector(factory.createSelector(selectorName));
        }
        Declarations decls = factory.createDeclarations();
        for (String declarationCode : declarations) {
            PropertyDeclaration propertyDeclaration = createPropertyDeclaration(declarationCode);
            Declaration declaration = factory.createDeclaration();
            declaration.setPropertyDeclaration(propertyDeclaration);
            decls.addDeclaration(declaration);
        }
        return factory.createRule(selectorsGroup, decls);
    }

    /**
     * Returns an instance of {@link Body}.
     *
     * If the body doesn't exist in the stylesheet it is created
     *
     * @return non-null instance of {@link Body}
     */
    public Body getBody() {
        Body body = styleSheet.getBody();
        if (body == null) {
            //create body if empty file
            body = factory.createBody();
            styleSheet.setBody(body);
        }
        return body;
    }

    public PropertyDeclaration createPropertyDeclaration(String code) {
        int separatorIndex = code.indexOf(':');
        if (separatorIndex == -1) {
            throw new IllegalArgumentException(String.format("Bad declaration value (forgotten colon): %s", code));
        }
        String propertyImg = code.substring(0, separatorIndex);
        String valueImg = code.substring(separatorIndex + 1);

        Property property = factory.createProperty(propertyImg);
        PropertyValue propertyValue = factory.createPropertyValue(factory.createExpression(valueImg));

        return factory.createPropertyDeclaration(property, propertyValue, false);
    }
    
    /**
     * Finds corresponding {@link Rule} in another instance of {@link Model}.
     * 
     * Should not be called under Model's read lock! It does it own locking
     * 
     * @since 1.5
     * @param model
     * @param rule
     * @return An instance of {@link Rule} belonging to current model and corresponding to 
     * the given rule from the other model.
     */
    public Rule findMatchingRule(Model model, Rule rule) {
       assert rule.getModel() == model;
        
       if(rule.getParent() == null) {
           //detached or not attached yet rule
           return null;
       }
       
       //find id of the given rule in the given model 
       final RuleRefModelVisitor ruleRef = new RuleRefModelVisitor(model, rule);
       model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.accept(ruleRef);
            }
       });
       
       int ruleIndex = ruleRef.getRuleIndex();
       assert ruleIndex != -1; //the rule must be found
       
       CharSequence ruleId = LexerUtils.trim(ruleRef.getRuleId());
       
       //now resolve the rule ref to the current model
       final ResolveRuleRefModelVisitor resolveRuleRef = new ResolveRuleRefModelVisitor(this.model, ruleId, ruleIndex);
       this.styleSheet.accept(resolveRuleRef); //we are under lock already, at least should be
       
       return resolveRuleRef.getResolvedRule();
        
    }
    
    /**
     * Finds corresponding {@link Media} in another instance of {@link Model}.
     * 
     * Should not be called under Model's read lock! It does it own locking
     * 
     * @since 1.13
     * @param model
     * @param media
     * @return An instance of {@link Media} belonging to current model and corresponding to 
     * the given media from the other model.
     */
    public Media findMatchingMedia(Model model, Media media) {
       assert media.getModel() == model;
        
       if(media.getParent() == null) {
           //detached or not attached yet rule
           return null;
       }
       
       //find id of the given rule in the given model 
       final MediaRefModelVisitor ruleRef = new MediaRefModelVisitor(model, media);
       model.runReadTask(new Model.ModelTask() {
            @Override
            public void run(StyleSheet styleSheet) {
                styleSheet.accept(ruleRef);
            }
       });
       
       int mediaIndex = ruleRef.getMediaIndex();
       assert mediaIndex != -1; //the rule must be found
       
       CharSequence ruleId = LexerUtils.trim(ruleRef.getMediaId());
       
       //now resolve the rule ref to the current model
       final ResolveMediaRefModelVisitor resolveRuleRef = new ResolveMediaRefModelVisitor(this.model, ruleId, mediaIndex);
       this.styleSheet.accept(resolveRuleRef); //we are under lock already, at least should be
       
       return resolveRuleRef.getResolvedMedia();
        
    }
    
    /**
     * Finds an index of the given rule in rules with same ID (selectors group).
     * 
     * If there's just one rule of the name index will be 0, if there are 
     * for example two div rules and the second is passed then the index will be 1.
     */
    private static class RuleRefModelVisitor extends ModelVisitor.Adapter {
        
        private final Model model;
        private final Rule rule;
        private CharSequence ruleId;
        
        private int ruleIndex = -1; //if there're more Rules with same ID
        
        private boolean cancelled;

        public RuleRefModelVisitor(Model model, Rule rule) {
            this.model = model;
            this.rule = rule;
        }
        
        @Override
        public void visitRule(Rule rule) {
            if(cancelled) {
                return ;
            }
            SelectorsGroup selectorsGroup = rule.getSelectorsGroup();
            if(selectorsGroup != null) {
                CharSequence foundRuleId = LexerUtils.trim(model.getElementSource(selectorsGroup));
                if (LexerUtils.equals(getRuleId(), foundRuleId, false, false)) {
                    ruleIndex++;
                }

                if(this.rule == rule) {
                    cancelled = true;
                }
            }
            
        }
        
        public int getRuleIndex() {
            return ruleIndex;
        }
        
        public synchronized CharSequence getRuleId() {
            if(ruleId == null) {
                SelectorsGroup selectorsGroup = rule.getSelectorsGroup();
                ruleId = selectorsGroup == null ? null : LexerUtils.trim(model.getElementSource(selectorsGroup));
            }
            return ruleId;
        }
        
    }
    
    /**
     * Finds an instance of {@link Rule} corresponding to the given ruleid and the rule index.
     */
    private static class ResolveRuleRefModelVisitor extends ModelVisitor.Adapter {
        
        private final Model model;
        private final CharSequence ruleId;
        private final int ruleIndex;
        
        private int index;
        private Rule rule;
        private boolean cancelled;

        public ResolveRuleRefModelVisitor(Model model, CharSequence ruleId, int ruleIndex) {
            this.model = model;
            this.ruleId = ruleId;
            this.ruleIndex = ruleIndex;
        }
        
        @Override
        public void visitRule(Rule rule) {
            if(cancelled) {
                return ;
            }
            SelectorsGroup selectorsGroup = rule.getSelectorsGroup();
            if(selectorsGroup != null) {
                CharSequence foundRuleId = LexerUtils.trim(model.getElementSource(selectorsGroup));
                if (LexerUtils.equals(ruleId, foundRuleId, false, false)) {
                    if(index == ruleIndex) {
                        this.rule = rule;
                        cancelled = true;
                    } else {
                        index++;
                    }
                }
            }
        }
        
        public Rule getResolvedRule() {
            return rule;
        }
        
    }
    
    /**
     * Finds an index of the given rule in rules with same ID (selectors group).
     * 
     * If there's just one rule of the name index will be 0, if there are 
     * for example two div rules and the second is passed then the index will be 1.
     */
    private static class MediaRefModelVisitor extends ModelVisitor.Adapter {
        
        private final Model model;
        private final Media media;
        private CharSequence ruleId;
        
        private int ruleIndex = -1; //if there're more Rules with same ID
        
        private boolean cancelled;

        public MediaRefModelVisitor(Model model, Media rule) {
            this.model = model;
            this.media = rule;
        }
        
        @Override
        public void visitMedia(Media rule) {
            if(cancelled) {
                return ;
            }
            MediaQueryList mediaQueryList = rule.getMediaQueryList();
            if(mediaQueryList != null) {
                CharSequence foundRuleId = LexerUtils.trim(model.getElementSource(mediaQueryList));
                if (LexerUtils.equals(getMediaId(), foundRuleId, false, false)) {
                    ruleIndex++;
                }

                if(this.media == rule) {
                    cancelled = true;
                }
            }
        }
        
        public int getMediaIndex() {
            return ruleIndex;
        }
        
        public synchronized CharSequence getMediaId() {
            if(ruleId == null) {
                MediaQueryList mediaQueryList = media.getMediaQueryList();
                ruleId = mediaQueryList == null ? null : LexerUtils.trim(model.getElementSource(mediaQueryList));
            }
            return ruleId;
        }
        
    }
    
    /**
     * Finds an instance of {@link Rule} corresponding to the given ruleid and the rule index.
     */
    private static class ResolveMediaRefModelVisitor extends ModelVisitor.Adapter {
        
        private final Model model;
        private final CharSequence mediaId;
        private final int ruleIndex;
        
        private int index;
        private Media rule;
        private boolean cancelled;

        public ResolveMediaRefModelVisitor(Model model, CharSequence ruleId, int ruleIndex) {
            this.model = model;
            this.mediaId = ruleId;
            this.ruleIndex = ruleIndex;
        }
        
        @Override
        public void visitMedia(Media rule) {
            if(cancelled) {
                return ;
            }
            
            CharSequence foundRuleId = LexerUtils.trim(model.getElementSource(rule.getMediaQueryList()));
            if (LexerUtils.equals(mediaId, foundRuleId, false, false)) {
                if(index == ruleIndex) {
                    this.rule = rule;
                    cancelled = true;
                } else {
                    index++;
                }
            }
        }
        
        public Media getResolvedMedia() {
            return rule;
        }
        
    }
    
   
}
