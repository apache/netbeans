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

import java.util.EventObject;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.util.Parameters;

/**
 * A {@link TldChangeEvent} event gets fired whenever any TLD or TAG file changes.
 * @author Tomas Mysik
 * @since 3.1
 */
public final class TldChangeEvent extends EventObject {
    private static final long serialVersionUID = 33496907234978697L;

    private final WebModule webModule;

    /**
     * Constructs a new <code>TldChangeEvent</code>.
     * @param source the bean that fired the event.
     * @param webModule {@link org.netbeans.modules.web.api.webmodule.WebModule} changed TLD or TAG file belongs to.
     */
    public TldChangeEvent(Object source, WebModule webModule) {
        super(source);
        Parameters.notNull("webModule", webModule);

        this.webModule = webModule;
    }

    /**
     * Gets the {@link org.netbeans.modules.web.api.webmodule.WebModule} changed TLD or TAG file belongs to.
     * @return {@link org.netbeans.modules.web.api.webmodule.WebModule} changed TLD or TAG file belongs to.
     *          Never <code>null</code>.
     */
    public WebModule getWebModule() {
        return webModule;
    }
}
