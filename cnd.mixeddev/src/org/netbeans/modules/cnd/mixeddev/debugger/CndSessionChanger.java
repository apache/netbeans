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

package org.netbeans.modules.cnd.mixeddev.debugger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.SessionBridge;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.utils.PsProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 */

@DebuggerServiceRegistration(types = SessionBridge.SessionChanger.class)
public class CndSessionChanger implements SessionBridge.SessionChanger{

    @Override
    public Set<String> getActions() {
        return Collections.singleton((String) ActionsManager.ACTION_STEP_INTO);
    }

    @Override
    public Session changeSuggested(Session origin, String action, Map<Object, Object> properties) {
        return MixedDevUtils.attachToJavaProcess(".*java.+:" + properties.get("conn_port") + ".*", //NOI18N
                "" + properties.get("javaClass"), "" + properties.get("javaMethod"), false);//NOI18N
    }
}
