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

package org.netbeans.modules.viewmodel;

import java.util.concurrent.Executor;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter.CALL;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Not in public API, AsynchronousModelFilter is sufficient, since we have a default.
 *
 * @author Martin Entlicher
 */
public interface AsynchronousModel {

    /**
     * Provide the threading information for view models method calls.
     * The returned Executor is used to call methods identified by
     * {@link CALL} enum.
     *
     * @param asynchCall Identification of the method call
     * @param node Object node
     * @return an instance of Executor
     */
    Executor asynchronous(CALL asynchCall, Object node) throws UnknownTypeException;
    
}
