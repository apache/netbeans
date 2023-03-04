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

package org.netbeans.modules.notifications.checklist;

/**
 * Default model for a CheckList
 */
public class DefaultCheckListModel extends AbstractCheckListModel {

    private static final long serialVersionUID = 1;

    private final boolean state[];
    private final Object[] values;
    private final String[] descriptions;

    /**
     * Creates a new model with the given state of checkboxes and the given
     * values
     *
     * @param state state of the checkboxes. A copy of this array will NOT be
     * created.
     * @param values values. A copy of this array will NOT be
     * created.
     */
    public DefaultCheckListModel(boolean[] state, Object[] values, String[] descriptions) {
        if (state.length != values.length)
            throw new IllegalArgumentException("state.length != values.length"); //NOI18N
        if (state.length != descriptions.length) {
            throw new IllegalArgumentException();
        }
        this.state = state;
        this.values = values;
        this.descriptions = descriptions;
    }
    
    @Override
    public boolean isChecked(int index) {
        return state[index];
    }
    
    @Override
    public void setChecked(int index, boolean c) {
        state[index] = c;
        fireContentsChanged(this, index, index);
    }

    @Override
    public int getSize() {
        return values.length;
    }

    @Override
    public Object getElementAt(int index) {
        return values[index];
    }

    @Override public String getDescription(int index) {
        return descriptions[index];
    }

}
