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

package org.netbeans.modules.xml.xam;

/**
 * Updater for children list of a component.
 *
 * @author nn136682
 */
public interface ComponentUpdater<C extends Component> {
    public enum Operation { ADD, REMOVE };
    
    /**
     * Updates children list.
     *
     * @param target component to be updated
     * @param child component to be added or removed.
     * @param operation add or remove; if null, no update should happen, only 
     * query for possibility the update.
     */
    void update(C target, C child, Operation operation);
    
    /**
     * Updates children list.
     *
     * @param target component to be updated
     * @param child component to be added or removed.
     * @param index of child component to be added or removed.
     * @param operation add or remove; if null, no update should happen, only 
     * query for possibility the update.
     */
    void update(C target, C child, int index, Operation operation);
    
    /**
     *  Provide capability to query for updatability.
     */
    interface Query<C extends Component> {
        /**
         * Check if a component can be added to target component.
         *
         * @param target component to be updated
         * @param child component to be added.
         */
        boolean canAdd(C target, Component child);
    }
}
