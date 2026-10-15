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
package org.netbeans.modules.lsp.client.debugger;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration;
import org.openide.util.Exceptions;

public abstract class DAPConfigurationAccessor {
    private static DAPConfigurationAccessor instance;

    public static DAPConfigurationAccessor getInstance() {
        try {
            Class.forName(DAPConfiguration.class.getName(), true, DAPConfiguration.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return instance;
    }

    public static void setInstance(DAPConfigurationAccessor instance) {
        DAPConfigurationAccessor.instance = instance;
    }

    public abstract OutputStream getOut(DAPConfiguration config);
    public abstract InputStream getIn(DAPConfiguration config);
    public abstract boolean getDelayLaunch(DAPConfiguration config);
    public abstract Map<String, Object> getConfiguration(DAPConfiguration config);
    public abstract String getSessionName(DAPConfiguration config);

}
