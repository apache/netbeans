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
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Expression;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.api.PropertyDeclaration;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.openide.util.CharSequences;

/**
 *
 * @author marekfukala
 */
public class DeclarationsI extends ModelElement implements Declarations {

    private List<Declaration> declarations = new ArrayList<>();
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {
        @Override
        public void elementAdded(Declaration declaration) {
            declarations.add(declaration);
        }

        @Override
        public void elementRemoved(Declaration declaration) {
            declarations.remove(declaration);
        }
    };

    public DeclarationsI(Model model) {
        super(model);
    }

    public DeclarationsI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public List<Declaration> getDeclarations() {
        return declarations;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public void addDeclaration(Declaration declaration) {
        if (!getDeclarations().isEmpty()) {
            //there's already a declaration...
//            PropertyDeclaration last = getDeclarations().get(getDeclarations().size() - 1);
            Declaration last = getDeclarations().get(getDeclarations().size() - 1);
            int lastIndex = getElementIndex(last);
            //check if there's a semicolon after the declaration
            PlainElement pe = getElementAt(lastIndex + 1, PlainElement.class);
            if (pe == null || CharSequences.indexOf(pe.getContent(), ";") == -1) {
                //find out if there's a semicolon in the element's source
                PropertyDeclaration propertyDeclaration = last.getPropertyDeclaration();
                PropertyValue propertyValue = propertyDeclaration.getPropertyValue();
                if (propertyValue != null) {
                    Expression expression = propertyValue.getExpression();
                    CharSequence content = expression.getContent();

                    //find last non-white char
                    for (int i = content.length() - 1; i >= 0; i--) {
                        char c = content.charAt(i);
                        if (!Character.isWhitespace(c)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(content.subSequence(0, i + 1));
                            sb.append(';');
                            sb.append(content.subSequence(i + 1, content.length()));

                            expression.setContent(sb);
                            break;
                        }
                    }

                }

            }
        }

        addElement(declaration);

        addTextElement(
                ";\n");
    }

    @Override
    public boolean removeDeclaration(Declaration declaration) {
        int index = getElementIndex(declaration);
        if (index == -1) {
            return false;
        }

        removeElement(index); //remove the declaration

        //update the whitespaces in the preceding plain element
        PlainElement before = getElementAt(index - 1, PlainElement.class);
        if (before
                != null) {
            //remove all whitespace after last endline
            wipeWhitespaces(before, false);
        }
        //update the whitespaces after the declaration element
        PlainElement after = getElementAt(index, PlainElement.class);//the indexes shifted by the removal!
        if (after
                != null) {
            String afterTrimmed = after.getContent().toString().trim();
            if (";".equals(afterTrimmed)) {
                //semicolon - remove
                removeElement(index);
                PlainElement afterafter = getElementAt(index, PlainElement.class);//the indexes shifted by the removal!
                if (afterafter != null) {
                    //whitespace
                    //remove all whitespace after last endline
                    wipeWhitespaces(afterafter, true);
                }
            } else {
                //remove all whitespace after last endline
                wipeWhitespaces(after, true);
            }
        }

        return true;

    }

    @Override
    protected Class getModelClass() {
        return Declarations.class;
    }
}
