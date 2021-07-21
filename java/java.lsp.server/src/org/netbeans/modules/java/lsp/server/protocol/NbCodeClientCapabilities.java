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
package org.netbeans.modules.java.lsp.server.protocol;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;

/**
 * Encapsulates all nbcode-specific client capabilities. Need to be passed in
 * an object:
 * <code><pre>
 * "nbcodeCapabilities" : {
 *      "statusBarMessageSupport"? : boolean
 *      ...
 * }
 * </pre></code>
 * @author sdedic
 */
public final class NbCodeClientCapabilities {
    /**
     * The LSP client official capabilities.
     */
    private ClientCapabilities clientCaps;
    
    /**
     * Supports status bar messages:
     * <ul>
     * <li>window/showStatusBarMessage
     * </ul>
     */
    private Boolean statusBarMessageSupport;

    /**
     * Supports test results display:
     * <ul>
     * <li>window/notifyTestProgress
     * </ul>
     */
    private Boolean testResultsSupport;
    
    /**
     * Asks for groovy support. Temporary option, will be removed.
     */
    private Boolean wantsGroovySupport;

    public ClientCapabilities getClientCapabilities() {
        return clientCaps;
    }

    public Boolean getStatusBarMessageSupport() {
        return statusBarMessageSupport;
    }

    public boolean hasStatusBarMessageSupport() {
        return statusBarMessageSupport != null && statusBarMessageSupport.booleanValue();
    }

    public void setStatusBarMessageSupport(Boolean statusBarMessageSupport) {
        this.statusBarMessageSupport = statusBarMessageSupport;
    }

    public Boolean getTestResultsSupport() {
        return testResultsSupport;
    }

    public boolean hasTestResultsSupport() {
        return testResultsSupport != null && testResultsSupport.booleanValue();
    }

    public void setTestResultsSupport(Boolean testResultsSupport) {
        this.testResultsSupport = testResultsSupport;
    }

    public Boolean getWantsGroovySupport() {
        return wantsGroovySupport;
    }

    public void setWantGroovySupport(Boolean enableGroovy) {
        this.wantsGroovySupport = enableGroovy;
    }

    public boolean wantsGroovySupport() {
        return wantsGroovySupport != null && wantsGroovySupport.booleanValue();
    }

    private NbCodeClientCapabilities withCapabilities(ClientCapabilities caps) {
        if (caps == null) {
            caps = new ClientCapabilities();
        }
        this.clientCaps = caps;
        return this;
    }
    
    public static NbCodeClientCapabilities get(InitializeParams initParams) {
        if (initParams == null) {
            return null;
        }
        Object ext = initParams.getInitializationOptions();
        if (!(ext instanceof JsonElement)) {
            return null;
        }
        InitializationExtendedCapabilities root = new GsonBuilder().
                /*
                    hypothetically needed for formatting options with Either type
                registerTypeAdapterFactory(new EitherTypeAdapter.Factory()).
                */
                create().
                fromJson((JsonElement)ext, InitializationExtendedCapabilities.class);
        return root == null ? null : root.getNbcodeCapabilities().withCapabilities(initParams.getCapabilities());
                
    }
    
    static final class InitializationExtendedCapabilities {
        private NbCodeClientCapabilities nbcodeCapabilities;

        public NbCodeClientCapabilities getNbcodeCapabilities() {
            return nbcodeCapabilities;
        }

        public void setNbcodeCapabilities(NbCodeClientCapabilities nbcodeCapabilities) {
            this.nbcodeCapabilities = nbcodeCapabilities;
        }
    }
}
