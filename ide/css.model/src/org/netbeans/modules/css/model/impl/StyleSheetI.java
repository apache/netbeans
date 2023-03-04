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
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.CharSet;
import org.netbeans.modules.css.model.api.Imports;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Namespaces;
import org.netbeans.modules.css.model.api.StyleSheet;

/**
 *
 * @author marekfukala
 */
public class StyleSheetI extends ModelElement implements StyleSheet {

    private CharSet charSet;
    private Imports imports;
    private Namespaces namespaces;
    private Body body;
    
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(CharSet value) {
            charSet = value;
        }

        @Override
        public void elementAdded(Imports value) {
            imports = value;
        }

        @Override
        public void elementAdded(Namespaces value) {
            namespaces = value;
        }

        @Override
        public void elementAdded(Body value) {
            body = value;
        }
    };

    public StyleSheetI(Model model) {
        super(model);
    }

    public StyleSheetI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setBody(Body body) {
        setElement(body);
    }

    @Override
    public CharSet getCharSet() {
        return charSet;
    }

    @Override
    public void setCharSet(CharSet charSet) {
        setElement(charSet);
    }

    @Override
    public Imports getImports() {
        return imports;
    }

    @Override
    public void setImports(Imports imports) {
        setElement(imports);
    }

    @Override
    public Namespaces getNamespaces() {
        return namespaces;
    }

    @Override
    public void setNamespaces(Namespaces namespaces) {
        setElement(namespaces);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return StyleSheet.class;
    }
    
}
