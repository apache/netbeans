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

package org.netbeans.modules.cnd.debugger.dbx.capture;

import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStart;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartProvider;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 *≥
 */
@ServiceProvider(service=ExternalStartProvider.class, position=1000)
public class ExternalStartProviderImpl implements ExternalStartProvider {

    public ExternalStart createExternalStart(Host host) {
	return new ExternalStartImpl(host);
    }

    public boolean isSupported() {
	return true;
    }
}
