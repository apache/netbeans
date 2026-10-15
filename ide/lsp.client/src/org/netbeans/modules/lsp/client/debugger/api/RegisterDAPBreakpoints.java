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
package org.netbeans.modules.lsp.client.debugger.api;

/**If registered in the MIME Lookup for a given MIME-type, this module will
 * provide basic support for breakpoints for this language.
 *
 * Please use {@link RegisterDAPDebugger} to get full UI integration.
 *
 * @since 1.29
 */
public final class RegisterDAPBreakpoints {
    private RegisterDAPBreakpoints() {}
    /**
     * Create a new instance of {@link RegisterDAPBreakpoints}, to be registred
     * in the MIME Lookup.
     * @return a new instance of RegisterDAPBreakpoints.
     */
    public static RegisterDAPBreakpoints newInstance() {
        return new RegisterDAPBreakpoints();
    }
}
