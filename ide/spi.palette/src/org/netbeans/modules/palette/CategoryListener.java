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

package org.netbeans.modules.palette;

import java.beans.PropertyChangeListener;

/**
 * Listens for changes in palette category.
 *
 * @author S. Aubrecht
 */
public interface CategoryListener {

    /**
     * A notification that palette category has been modified (items added/removed/reordered,
     * display name changes etc).
     *
     * @param src Category that has been modified.
     */
    void categoryModified( Category src );
}
