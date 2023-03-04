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
package org.netbeans.modules.nativeimage.api.debug;

/**
 * Representation of a native variable.
 * @since 0.1
 */
public interface NIVariable {

    /**
     * Name of the variable.
     *
     * @since 0.1
     */
    String getName();

    /**
     * Type of the variable value.
     *
     * @since 0.1
     */
    String getType();

    /**
     * String representation of the variable value.
     *
     * @since 0.1
     */
    String getValue();

    /**
     * The parent variable, if any. Every child variable has a corresponding parent
     * variable.
     *
     * @return the parent variable, or <code>null</code>.
     * @since 0.1
     */
    NIVariable getParent();

    /**
     * Number of child variables (properties, or array elements) of this variable.
     *
     * @since 0.1
     */
    int getNumChildren();

    /**
     * Get the child variables in the specified index range. Children starting at
     * <code>from</code> index and up to and excluding <code>to</code> index will
     * be returned. If <code>from</code> is less than zero, all children are returned.
     *
     * @since 0.1
     */
    NIVariable[] getChildren(int from, int to);

    /**
     * Get all variable's children.
     *
     * @since 0.1
     */
    default NIVariable[] getChildren() {
        return getChildren(0, Integer.MAX_VALUE);
    }

    /**
     * Get the full expression that this variable object represents.
     *
     * @since 0.1
     */
    String getExpressionPath();

    /**
     * Get the frame this variable is associated with.
     *
     * @return the frame, or <code>null</code>.
     * @since 0.1
     */
    NIFrame getFrame();
}
