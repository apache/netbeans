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

package org.netbeans.modules.websvc.design.view.widget;

/**
 * Implemented by those widgets that wish to expand and collapse under the
 * control of an <code>ExpanderWidget</code>. The implementation must make
 * the actual size change itself, when either of the collapse/expand
 * methods are invoked.
 *
 * @author Ajit Bhate
 * @author  Nathan Fiedler
 */
public interface ExpandableWidget {

    /**
     * Set the expanded state of the widget.
     *
     * @param  expanded  true to expand, false to collapse.
     */
    void setExpanded(boolean expanded);

    /**
     * Indicates if this widget is expanded or collapsed.
     *
     * @return  true if expanded, false if collapsed.
     */
    boolean isExpanded();

    /**
     * Returns the object that can be used as a hashtable key. This is
     * utilized in the ExpanderWidget for preserving the expanded state
     * of widgets in the event that they are recreated, as in the case
     * of an undo/redo operation.
     *
     * @return  hashtable key.
     */
    Object hashKey();
}
