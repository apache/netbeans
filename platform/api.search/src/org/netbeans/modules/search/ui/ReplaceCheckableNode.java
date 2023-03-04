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
package org.netbeans.modules.search.ui;

import org.netbeans.modules.search.Selectable;
import org.openide.explorer.view.CheckableNode;

/**
 * Checkable node for replacable matches.
 *
 * @author jhavlin
 */
public class ReplaceCheckableNode implements CheckableNode {

    private final boolean replacing;
    private Selectable model;

    public ReplaceCheckableNode(Selectable model, boolean replacing) {
        this.replacing = replacing;
        this.model = model;
    }

    @Override
    public boolean isCheckable() {
        return replacing;
    }

    @Override
    public boolean isCheckEnabled() {
        return true;
    }

    @Override
    public Boolean isSelected() {
        return model.isSelected();
    }

    @Override
    public void setSelected(Boolean selected) {
        model.setSelectedRecursively(selected);
    }
}
