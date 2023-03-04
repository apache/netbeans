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

package org.netbeans.modules.web.jsps.parserapi;

import java.util.EventListener;

/**
 * A {@link TldChangeEvent} event gets fired whenever any TLD or TAG file changes.
 * You can register a {@link TldChangeListener} with a source
 * bean so as to be notified of any of these changes.
 * @author Tomas Mysik
 * @since 3.1
 */
public interface TldChangeListener extends EventListener {

    /**
     * This method gets called when a TLD or TAG file changes.
     * @param evt a {@link TldChangeEvent} object describing the event source
     *            and {@link org.netbeans.modules.web.api.webmodule.WebModule} TLD or TAG file belongs to.
     */
    void tldChange(TldChangeEvent evt);
}
