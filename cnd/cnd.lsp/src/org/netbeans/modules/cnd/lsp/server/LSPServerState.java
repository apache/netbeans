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
package org.netbeans.modules.cnd.lsp.server;

import java.awt.Image;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Identifies the state of a LSP server.
 * @author antonio
 */
public enum LSPServerState {

    /** clangd is configured and ready to be started. */
    STOPPED,
    /** clangd is not configured yet */
    MISCONFIGURED,
    /** clangd is running */
    RUNNING,
    /** clang generated an error */
    ERROR
    ;

    public String getDisplayName() {
        return NbBundle.getMessage(LSPServerState.class, "LSPServerState." + name()); // NOI18N
    }

    public Image getBadge() {
        switch(this) {
            case STOPPED:
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/server/resources/black.png");
            case MISCONFIGURED:
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/server/resources/orange.png");
            case RUNNING:
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/server/resources/green.png");
            case ERROR:
            default:
                return ImageUtilities.loadImage("org/netbeans/modules/cnd/lsp/server/resources/red.png");
        }
    }
    
}
