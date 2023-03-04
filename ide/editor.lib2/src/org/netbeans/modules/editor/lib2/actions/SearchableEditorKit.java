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

package org.netbeans.modules.editor.lib2.actions;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

/**
 * This interface should be implemented by editor kits that hold their actions
 * in a map. They may also notify
 *
 * @since 1.13
 */
public interface SearchableEditorKit {

    /**
     * Find action with the given name.
     *
     * @param actionName non-null action's name.
     * @return action's instance or null if an action with the given name does not exist.
     */
    Action getAction(String actionName);

    /**
     * Add listener for notifications about any change in a set of actions
     * maintained by this editor kit.
     *
     * @param listener non-null listener to be added.
     */
    void addActionsChangeListener(ChangeListener listener);

    void removeActionsChangeListener(ChangeListener listener);

}
