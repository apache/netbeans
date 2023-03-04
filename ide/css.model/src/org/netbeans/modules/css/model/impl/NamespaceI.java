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
import org.netbeans.modules.css.model.api.Namespace;
import org.netbeans.modules.css.model.api.NamespacePrefixName;
import org.netbeans.modules.css.model.api.ResourceIdentifier;

/**
 *
 * @author marekfukala
 */
public class NamespaceI extends ModelElement implements Namespace {

    private NamespacePrefixName namespacePrefixName;
    private ResourceIdentifier resourceIdentifier;
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(NamespacePrefixName value) {
            namespacePrefixName = value;
        }

        @Override
        public void elementAdded(ResourceIdentifier value) {
            resourceIdentifier = value;
        }
    };

    public NamespaceI(Model model) {
        super(model);
    }

    public NamespaceI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public NamespacePrefixName getNamespacePrefixName() {
        return namespacePrefixName;
    }

    @Override
    public void setNamespacesPrefixName(NamespacePrefixName namespacePrefixName) {
        setElement(namespacePrefixName);
    }

    @Override
    public ResourceIdentifier getResourceIdentifier() {
        return resourceIdentifier;
    }

    @Override
    public void setResourceIdentifier(ResourceIdentifier resourceIdentifier) {
        setElement(resourceIdentifier);
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    protected Class getModelClass() {
        return Namespace.class;
    }
}
