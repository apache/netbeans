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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.spi.viewmodel.ColumnModel;

/**
 * Represents a binding of specific column model of inner view
 * to the main colum model of the enclosing view.
 *
 * @author Martin Entlicher
 */
final class HyperColumnModel extends ColumnModel {

    private ColumnModel main;
    private ColumnModel specific;
    private final Set<String> ids;

    public HyperColumnModel(ColumnModel main, ColumnModel specific) {
        this.main = main;
        this.specific = specific;
        ids = createAllIDs(main, specific);
    }
    
    private static Set<String> createAllIDs(ColumnModel... cms) {
        Set<String> allIds = new HashSet<String>();
        for (ColumnModel cm : cms) {
            if (cm instanceof HyperColumnModel) {
                allIds.addAll(((HyperColumnModel) cm).getAllIDs());
            } else {
                allIds.add(cm.getID());
            }
        }
        return allIds;
    }

    public ColumnModel getMain() {
        return main;
    }

    public ColumnModel getSpecific() {
        return specific;
    }

    @Override
    public String getID() {
        return main.getID();
    }

    @Override
    public String getDisplayName() {
        return main.getDisplayName();
    }

    @Override
    public String getShortDescription() {
        return main.getShortDescription();
    }

    @Override
    public Class getType() {
        return specific.getType();
    }
    
    Set<String> getAllIDs() {
        return ids;
    }

}
