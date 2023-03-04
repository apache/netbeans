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

package org.netbeans.modules.viewmodel;

import java.awt.datatransfer.Transferable;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModelFilter;

/**
 *
 * @author Martin Entlicher
 */
public class HyperCompoundModel implements Model {

    private Models.CompoundModel main;
    private Models.CompoundModel[] models;
    private TreeModelFilter treeFilter;

    public HyperCompoundModel(Models.CompoundModel main,
                              Models.CompoundModel[] models,
                              TreeModelFilter treeFilter) {
        this.main = main;
        this.models = models;
        this.treeFilter = treeFilter;
    }

    ColumnModel[] getColumns() {
        return main.getColumns();
    }

    Models.CompoundModel getMain() {
        return main;
    }

    Models.CompoundModel[] getModels() {
        return models;
    }

    TreeModelFilter getTreeFilter() {
        return treeFilter;
    }

    int getAllowedDragActions() {
        int actions = 0;
        for (Models.CompoundModel m : models) {
            actions |= m.getAllowedDragActions();
        }
        return actions;
    }

    int getAllowedDropActions(Transferable t) {
        int actions = 0;
        for (Models.CompoundModel m : models) {
            actions |= m.getAllowedDropActions(t);
        }
        return actions;
    }
}
