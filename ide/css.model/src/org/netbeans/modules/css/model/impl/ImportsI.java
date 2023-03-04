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
import org.netbeans.modules.css.model.api.ImportItem;
import org.netbeans.modules.css.model.api.Imports;
import org.netbeans.modules.css.model.api.Model;

/**
 *
 * @author marekfukala
 */
public class ImportsI extends ModelElement implements Imports {

    private List<ImportItem> imports = new ArrayList<>();
    private final ModelElementListener elementListener = new ModelElementListener.Adapter() {

        @Override
        public void elementAdded(ImportItem importItem) {
            imports.add(importItem);
        }
    };

    public ImportsI(Model model) {
        super(model);
    }

    public ImportsI(Model model, Node node) {
        super(model, node);
        initChildrenElements();
    }

    @Override
    public List<ImportItem> getImportItems() {
        return imports;
    }

    @Override
    protected ModelElementListener getElementListener() {
        return elementListener;
    }

    @Override
    public void addImportItem(ImportItem item) {
        addElement(item);
    }

    @Override
    protected Class getModelClass() {
        return Imports.class;
    }
}
