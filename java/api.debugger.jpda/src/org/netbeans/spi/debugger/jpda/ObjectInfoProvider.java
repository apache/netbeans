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
package org.netbeans.spi.debugger.jpda;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ObjectVariable;

/** Provider of additional information about objects in the heap.
 * Register your implementation as
 * <pre>
 * @DebuggerServiceRegistration(path="netbeans-JPDASession", types=ObjectInfoProvider.class)
 * public final class YourProvider implements ObjectInfoProvider {
 * ...
 * }
 * </pre>
 * @since 3.45
 */
public interface ObjectInfoProvider {
    /** Handles showing the info about provided object.
     *
     * @param session the debugging session sending the request
     * @param obj the object to display additional information about
     * @return {@code true} if the object information can be shown,
     *    {@code false} otherwise
     */
    boolean display(Session session, ObjectVariable obj);


    /** Requests additional information about an object to be shown.
     * Iterates all providers registered in the
     * {@code session.lookup(null, ObjectInfoProvider.class)}.
     *
     * @param session the debugging session that's requesting
     * @param obj reference to an object in the heap
     * @return {@code true} if the object information can be shown,
     *    {@code false} otherwise
     */
    static boolean showObjectInfo(Session session, ObjectVariable obj) {
        for (var output : session.lookup(null, ObjectInfoProvider.class)) {
            if (output != null && output.display(session, obj)) {
                return true;
            }
        }
        return false;
    }

}
