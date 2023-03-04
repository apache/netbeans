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
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

/**
 * This interface provides an ability for switching focus.
 *
 * @author David Kaspar
 */
public interface CycleFocusProvider {

    /**
     * Switches a focus to the previous widget/object on a scene.
     * @param widget the widget where the action was invoked
     * @return true, if switching was successful
     */
    boolean switchPreviousFocus (Widget widget);

    /**
     * Switches a focus to the next widget/object on a scene.
     * @param widget the widget where the action was invoked
     * @return true, if switching was successful
     */
    boolean switchNextFocus (Widget widget);

}
