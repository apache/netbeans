/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.debugger.jpda;


/**
 * Represents some variable in the debuggee.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @see LocalVariable
 * @see Field
 * @see This
 * @see Super
 * @see JPDAThread#getContendedMonitor
 * @see JPDAThread#getOwnedMonitors
 *
 * @author   Jan Jancura
 */
public interface Variable {

    /**
     * Type of this instance.
     *
     * @return type of this instance
     */
    public abstract String getType ();

    /**
     * Text representation of current value of "this" variable.
     *
     * @return text representation of current value of "this" variable
     */
    public abstract String getValue ();

    /**
     * Create an object in this JVM, which mirrors the value of this variable in
     * the target JVM.
     *
     * @return The mirror object, or <code>null</code> when a mirror object can not be created.
     * @since 2.44
     */
    Object createMirrorObject();

    /**
     * Creates a <em>snapshot</em> of the variable value. It clones the variable
     * object to assure it <em>sticks to its current debugee value</em>
     * regardless if the variable state gets re-assigned in the future.
     *
     * This is an optional operation. Variables that have no notion of
     * <em>snapshotting</em> may return {@code this} as the default
     * implementation of this method does.
     *
     * @return a <em>snapshot</em> of the current variable value
     * @since 3.47
     */
    default Variable asValue() {
        return this;
    }
}
