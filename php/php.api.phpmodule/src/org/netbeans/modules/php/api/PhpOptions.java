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

package org.netbeans.modules.php.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Instance of this class can be found in global lookup.
 * @since 2.28
 */
public interface PhpOptions {

    /**
     * Get the PHP interpreter file path.
     * <p>
     * <b>Clients probably want to use {@code PhpInterpreter#getDefault()} from PHP Executable API module.</b>
     * @return the PHP interpreter file path or {@code null} if none is found
     */
    @CheckForNull
    String getPhpInterpreter();

    /**
     * Get debugger session ID.
     * @return debugger session ID
     */
    @NonNull
    String getDebuggerSessionId();

}
