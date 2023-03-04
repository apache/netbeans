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
package org.netbeans.api.io;

/**
 * Operations that should take part when showing a component. The actual
 * behavior depends on implementation (some features may be unsupported).
 *
 * @author jhavlin
 */
public enum ShowOperation {
    /**
     * Open the GUI component (Output Window) if it is closed.
     */
    OPEN,
    /**
     * Make the GUI component (Output Window) visible. E.g. show it if it is
     * minimized.
     */
    MAKE_VISIBLE,
    /**
     * Activate the GUI component (Output Window). E.g. highlight and possibly
     * focus it.
     */
    ACTIVATE
}
