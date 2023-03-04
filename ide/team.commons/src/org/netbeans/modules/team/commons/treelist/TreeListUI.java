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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Point;

/**
 * UI for tree-like list which forwards mouse events to renderer component under
 * mouse cursor.
 *
 * @author S. Aubrecht
 */
public class TreeListUI extends AbstractListUI {

    @Override
    boolean showPopupAt( int rowIndex, Point location ) {
        if (!(list instanceof TreeList)) {
            return false;
        }

        ((TreeList) list).showPopupMenuAt(rowIndex, location);
        return true;
    }
}
