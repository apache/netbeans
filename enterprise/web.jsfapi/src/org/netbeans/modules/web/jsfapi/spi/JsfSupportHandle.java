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

package org.netbeans.modules.web.jsfapi.spi;

import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.openide.filesystems.FileUtil;

/**
 * Supposed to be in web project's lookup.
 *
 * Hold a reference for an instance of JsfSupport for the project.
 *
 * @author marekfukala
 */
@Deprecated
public class JsfSupportHandle {

    private JsfSupport instance;
    private Throwable caller;

    /** can be called only once per session. */
    synchronized void install(JsfSupport instance) {
        if(this.instance != null) {
            throw new IllegalStateException(
                    String.format("An instance of JsfSupport has already been installed to this project %s!",
                    FileUtil.getFileDisplayName(instance.getProject().getProjectDirectory())), caller);
        }

        this.instance = instance;
        this.caller = new Throwable();
    }

    synchronized JsfSupport get() {
        return instance;
    }

    protected boolean isEnabled() {
        return true;
    }

}
