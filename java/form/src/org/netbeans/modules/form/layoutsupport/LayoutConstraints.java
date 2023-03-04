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

package org.netbeans.modules.form.layoutsupport;

import org.openide.nodes.Node;

/**
 * This interface represents one layout constraints object describing position
 * of a component in visual container layout. This interface is the second part
 * of the layout support extensions - alongside LayoutSupportDelegate, which
 * takes care about container layout as a whole.
 *
 * @see LayoutSupportDelegate
 *
 * @author Tomas Pavek
 */

public interface LayoutConstraints {

    /** Gets the properties of these component layout constraints to be
     * presented in Component Inspector for the component.
     * @return properties of these constraints
     */
    Node.Property[] getProperties();

    /** Gets the real (reference) constraints object behind this metaobject.
     * This object is used as the constraints parameter when adding a component
     * to container.
     * @return the real constraints object
     */
    Object getConstraintsObject();

    /** Cloning method - creates a copy of the constraints. It should clone
     * the reference object inside.
     * @return cloned LayoutConstraints
     */
    LayoutConstraints cloneConstraints();
}
