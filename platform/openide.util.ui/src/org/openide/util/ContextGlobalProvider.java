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
package org.openide.util;


/** An interface that can be registered in a lookup by subsystems
 * wish to provide a global context actions should react to. The global
 * context is accessible via {@link Utilities#actionsGlobalContext} method
 * and is expected to contain similar content as the context used when
 * context aware actions (see {@link ContextAwareAction}) are being
 * manipulated for example via method {@link Utilities#actionsToPopup}, so
 * in current state it is reasonable to put there all currently active
 * <a href="@org-openide-nodes@/org/openide/nodes/Node.html">Node</a>, their cookies and {@link javax.swing.ActionMap}.
 * By default this interface is implemented by window system to delegate
 * to currently activated <a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getLookup()">TopComponent's  lookup</a>.
 * <p>
 * There is an external FAQ entry describing how to
 * <a href="http://wiki.netbeans.org/DevFaqAddGlobalContext">add content to
 * the global context</a> by providing customized implementation of this
 * interface.
 * 
 * </p>
 *
 * @author Jaroslav Tulach
 * @since 4.10
*/
public interface ContextGlobalProvider {
    /** Creates the context in form of Lookup.
     * @return the context
     */
    public abstract Lookup createGlobalContext();
}
