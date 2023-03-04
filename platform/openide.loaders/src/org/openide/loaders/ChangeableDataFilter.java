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

package org.openide.loaders;

import javax.swing.event.ChangeListener;

/** Allows certain data objects to be excluded from being displayed.
* @see RepositoryNodeFactory
* @author Jaroslav Tulach
*/
public interface ChangeableDataFilter extends DataFilter {

    /** Adds a ChangeListener to the filter. The ChangeListeners must be notified
     * when the filtering strategy changes.
     * @param listener The ChangeListener to add
     */
    public void addChangeListener( ChangeListener listener );

    /** Removes a ChangeListener from the filter. 
     * @param listener The ChangeListener to remove.
     */
    public void removeChangeListener( ChangeListener listener );
    
}
