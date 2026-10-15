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
        // Truffle runtime is very vocal if it is run in interpreter only mode.
        // That mode is the case in "normal" JDKs, which are the NetBeans
        // baseline - the warnings should be silenced accordingly
        if (!System.getProperties().contains("polyglot.engine.WarnInterpreterOnly")) { //NOI18N
            System.setProperty("polyglot.engine.WarnInterpreterOnly", "false"); //NOI18N
        }
        // The default Truffle runtime uses a native library. That library fails
        // hard when it shall be loaded by multiple classloaders. So for now
        // use the fallback runtime
        if (!System.getProperties().contains("truffle.UseFallbackRuntime")) { //NOI18N
            System.setProperty( "truffle.UseFallbackRuntime", "true" ); //NOI18N
        }
    }
}