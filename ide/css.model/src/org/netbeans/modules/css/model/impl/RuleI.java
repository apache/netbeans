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
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public class RuleI extends ModelElement implements Rule {

    private SelectorsGroup selectorsGroup;
    private Declarations declarations;
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(SelectorsGroup value) {
            selectorsGroup = value;
        }

        @Override
        public void elementAdded(Declarations value) {
            declarations = value;
        }
    };

    public RuleI(Model model) {
        super(model);
        
        //default elements
        addTextElement("\n"); //not acc. to the grammar!

        addEmptyElement(SelectorsGroup.class);
        addTextElement(" {\n");
        addEmptyElement(Declarations.class);
        addTextElement("\n}\n");
    }

    public RuleI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    protected String getCustomElementID() {
        SelectorsGroup sGroup = getSelectorsGroup();
        if(sGroup != null) {
            return new StringBuilder()
                    .append(getModelClass().getSimpleName())
                    .append('-')
                    .append(model.getElementSource(sGroup).toString().hashCode())
                    .toString();
        }
        return null;
    }
    
    @Override
    public boolean isValid() {
        if(!super.isValid()) {
            return false; //parsing error
        } else {
            //no parsing error in the node's scope, but some sub-nodes are missing
            return getSelectorsGroup() != null && getDeclarations() != null;
        }
    }
    
    @Override
    public SelectorsGroup getSelectorsGroup() {
        return selectorsGroup;
    }

    @Override
    public void setSelectorsGroup(SelectorsGroup selectorsGroup) {
        setElement(selectorsGroup);
    }

    @Override
    public Declarations getDeclarations() {
        return declarations;
    }

    @Override
    public void setDeclarations(Declarations declarations) {
        if(!isArtificialElement()) {
            if(getDeclarations() == null) {
                //XXX this code is really not nice and should be possibly be
                //generified somehow
                
                int rightCBIndex = -1;
                int leftCBIndex = -1;
                //the element has been created from a source code, 
                //but there was no content between the { } curly braces
                //so there's no Declarations node inside
                for(int i = 0; i < getElementsCount(); i++) {
                    Element e = getElementAt(i);
                    if(e instanceof PlainElement) {
                        PlainElement pe = (PlainElement)e;
                        if(LexerUtils.equals("{", pe.getContent(), true, true)) {
                            rightCBIndex = i;
                        }
                        if(LexerUtils.equals("}", pe.getContent(), true, true)) {
                            leftCBIndex = i;
                        }
                    }
                }
                
                if(rightCBIndex == (leftCBIndex - 1)) {
                    //nothing between the curly braces
                } else {
                    //remove the plain elements from the { } content
                    for(int i = rightCBIndex + 1; i < leftCBIndex - 1; i++) {
                        removeElement(i);
                    }
                }
                //insert the declarations between the left curly brace
                insertElement(leftCBIndex, declarations);
                
                return ;
            }
        }
        setElement(declarations);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return Rule.class;
    }
}
