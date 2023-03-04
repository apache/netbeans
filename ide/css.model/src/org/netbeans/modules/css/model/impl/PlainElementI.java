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
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.PlainElement;

/**
 *
 * @author marekfukala
 */
public class PlainElementI extends ModelElement implements PlainElement {

    private CharSequence content;
    private final ModelElementListener emptyElementListener = new ModelElementListener.Adapter();

    public PlainElementI(Model model) {
        super(model);
    }

    public PlainElementI(Model model, CharSequence text) {
        super(model);
        this.content = text;
    }

    public PlainElementI(Model model, Node node) {
        super(model, node);
        this.content = node.image();
    }

    @Override
    public CharSequence getContent() {
        return content;
    }

    @Override
    public void setContent(CharSequence content) {
        this.content = content;
        fireElementChanged();
    }

    @Override
    public String toString() {
        return super.toString() + ":'" + getContent().toString() + "'";
    }

    @Override
    protected ModelElementListener getElementListener() {
        return emptyElementListener;
    }

    @Override
    protected Class getModelClass() {
        return PlainElement.class;
    }
}
