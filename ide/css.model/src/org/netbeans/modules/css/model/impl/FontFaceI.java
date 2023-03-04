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
import org.netbeans.modules.css.model.api.FontFace;
import org.netbeans.modules.css.model.api.Model;

/**
 *
 * @author marekfukala
 */
public class FontFaceI extends ModelElement implements FontFace {

    private Declarations declarations;
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(Declarations value) {
            declarations = value;
        }
    };

    public FontFaceI(Model model) {
        super(model);
        
        //default elements
        addTextElement("\n"); //not acc. to the grammar!
        
        addTextElement("@font-face");
        addTextElement(" ");
        addTextElement("{\n");
        addEmptyElement(Declarations.class);
        addTextElement("}\n");
    }

    public FontFaceI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Declarations getDeclarations() {
        return declarations;
    }

    @Override
    public void setDeclarations(Declarations declarations) {
        setElement(declarations);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return FontFace.class;
    }
}
