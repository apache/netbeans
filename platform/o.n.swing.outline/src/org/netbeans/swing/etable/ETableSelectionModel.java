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

package org.netbeans.swing.etable;

import javax.swing.DefaultListSelectionModel;

/**
 * This class prevents from automatic selection of inserted lines.
 * 
 * @author Martin Entlicher
 */
class ETableSelectionModel extends DefaultListSelectionModel {

    private ThreadLocal<Boolean> insertingLines = new ThreadLocal<Boolean>();

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        insertingLines.set(Boolean.TRUE);
        super.insertIndexInterval(index, length, before);
    }

    @Override
    public int getSelectionMode() {
        if (insertingLines.get() == Boolean.TRUE) {
            insertingLines.remove();
            // When we're inserting lines, they are not automatically selected
            // if the selection mode is single selection.
            return SINGLE_SELECTION;
        }
        return super.getSelectionMode();
    }

}
