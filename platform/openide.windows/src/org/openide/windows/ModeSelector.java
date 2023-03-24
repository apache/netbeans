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

package org.openide.windows;

/**
 * Selects mode which a TopComponent should initially dock into.
 * If a TopComponent being opened is not docked into any Mode, the system selects 
 * the last-used editor-kind Mode, or the default editor mode if no editor was used.
 * Plugin implementors can hint the Window System to use more appropriate
 * mode than the default to open the TopComppnent. 
 * <p>
 * If none of the registered {@code ModeSelector}s return a valid Mode, the TopComponent
 * will open in the mode selected by the default algorithm. Implementation of WindowManager 
 * may ignore the hint, for example if it conflicts with persisted settings or user choices.
 * <p>
 * Implementations of {@code ModeSelector} must be registered in the default Lookup.
 * @since 6.77
 */
public interface ModeSelector {
    /**
     * Choose a suitable Mode to open the TopComponent in. The implementation 
     * should return an existing Mode which the TopComponent will dock into. The
     * automatically selected Mode will be passed in {@code preselectedMode}.
     * The implementation can accept the default or ignore the request and return
     * {@code null}.
     * 
     * @param tc the {@link TopComponent} to be opened.
     * @param preselectedMode the default mode for opening
     * @return a more suitable Mode, or {@code null} to use the preselected one.
     */
    public Mode selectModeForOpen(TopComponent tc, Mode preselectedMode);
}
