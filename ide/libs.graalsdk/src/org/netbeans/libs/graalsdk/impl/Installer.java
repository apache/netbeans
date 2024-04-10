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

package org.netbeans.libs.graalsdk.impl;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void validate() {
        super.validate();
        // If truffle runtimes are executed directly they try to load another
        // copy of the native integration library. This fails. The fallback
        // runtime can run independently of this.
        System.setProperty( "polyglot.engine.WarnInterpreterOnly", "false" ); //NOI18N
        System.setProperty( "truffle.UseFallbackRuntime", "true" ); //NOI18N
    }
}