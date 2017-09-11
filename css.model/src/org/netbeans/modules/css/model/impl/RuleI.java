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
