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

package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 * Provider interface for web modules.
 *
 * <p>The <code>org.netbeans.api.web.webmodule</code> module registers an
 * implementation of this interface to global lookup which looks for the
 * project which owns a file (if any) and checks its lookup for this interface,
 * and if it finds an instance, delegates to it. Therefore it is not normally
 * necessary for a project type provider to register its own instance just to
 * define the web module for files it owns, assuming it uses projects for
 * implementation of the web module.</p>
 *
 * <p>If needed a new implementation of this interface can be registered in
 * global lookup.</p>
 *
 * @see WebModule#getWebModule
 *
 * @author Pavel Buzek
 */
public interface WebModuleProvider {

    /**
     * Find the web module containing a given file.
     *
     * @param  file a file; never null.
     * @return a web module, or null if this provider could not find a web module
     *         containing this file.
     *
     * @see WebModuleFactory
     */
    WebModule findWebModule(FileObject file);
}
