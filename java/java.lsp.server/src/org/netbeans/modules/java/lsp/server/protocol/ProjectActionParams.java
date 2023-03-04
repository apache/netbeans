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
package org.netbeans.modules.java.lsp.server.protocol;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;

/**
 * Describes 1st command parameter for 'java.project.action' generic command. The 
 * {@link #getAction()} is the project action ID that should be executed, like {@link ActionProvider#COMMAND_BUILD}.
 * The client may specify a {@link ProjectConfiguration} to use for action's execution. By default, the active
 * (or default) configuration is used if {@link #getFallbackDefault()} is enabled.
 * <p>
 * Other parameters can be passed to {@code java.project.run.action} LSP commands: it tries to interpret
 * the 2nd and further arguments as URI strings, converts them into {@link FileObject}s and passes in context Lookup
 * to the action.
 * @author sdedic
 */
public final class ProjectActionParams {
    @NonNull
    private String action;
    private String configuration;
    private Boolean fallbackDefault;
    
    @Pure
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Pure
    @NonNull
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Pure
    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("action", action);
        b.add("configuration", configuration);
        b.add("fallbackDefault", fallbackDefault);
        return b.toString();
    }

    @Pure
    public Boolean getFallbackDefault() {
        return fallbackDefault;
    }

    public void setFallbackDefault(Boolean fallbackDefault) {
        this.fallbackDefault = fallbackDefault;
    }
}
