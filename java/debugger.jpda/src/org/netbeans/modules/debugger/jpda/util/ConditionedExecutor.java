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

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.event.Event;

/**
 * Conditioned executor of an event.
 *
 * @author Martin Entlicher
 */
public interface ConditionedExecutor extends Executor {

    /**
     * Process the condition associated with this event.
     * The method is called before {@link Executor#exec(com.sun.jdi.event.Event)}.
     * Depending on the returned value, the {@link Executor#exec(com.sun.jdi.event.Event)}
     * method will be called as normally, or this event will be ignored and suspended threads resumed.
     *
     * @param event The event to process.
     * @return <code>true</code> if the condition was evaluated successfully and
     * the event will be furter processed, <code>false</code> if the event should
     * not be considered for furter processing.
     */
    boolean processCondition(Event event);

}
