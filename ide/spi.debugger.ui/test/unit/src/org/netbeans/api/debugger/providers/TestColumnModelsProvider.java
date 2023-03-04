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

package org.netbeans.api.debugger.providers;

import org.netbeans.modules.debugger.ui.models.ColumnModels;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;

/**
 *
 * @author Martin Entlicher
 */
public class TestColumnModelsProvider {

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    @ColumnModelRegistration(path="unittest/annotated/LocalsView", position=10)
    public static ColumnModel createDefaultLocalsColumn() {
        return ColumnModels.createDefaultLocalsColumn();
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    @ColumnModelRegistration(path="unittest/annotated/LocalsView", position=40)
    public static ColumnModel createLocalsToStringColumn() {
        return ColumnModels.createLocalsToStringColumn();
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    @ColumnModelRegistration(path="unittest/annotated/LocalsView", position=20)
    public static ColumnModel createLocalsTypeColumn() {
        return ColumnModels.createLocalsTypeColumn();
    }
    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    @ColumnModelRegistration(path="unittest/annotated/LocalsView", position=30)
    public static ColumnModel createLocalsValueColumn() {
        return ColumnModels.createLocalsValueColumn();
    }


}
