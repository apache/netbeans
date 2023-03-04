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

package org.netbeans.modules.uihandler.api;

import java.util.logging.Logger;

/** Shall be registered in {@link org.openide.util.Lookup#getDefault} and
 * is then called when the UI logger module is activated. Can generate
 * various log events, for example info about set of opened projects, etc.
 *
 * @author Jaroslav Tulach
 */
public interface Activated {
    
    /**
     * Called when the UI logger is activated.
     * @param uiLogger the activated UI logger
     */
    public void activated(Logger uiLogger);
}
