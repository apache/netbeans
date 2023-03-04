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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import org.netbeans.modules.debugger.jpda.models.JPDAThreadGroupImpl;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThreadGroup;

/**
 *
 * @author Martin Entlicher
 */
public class JPDADVThreadGroup implements DVThreadGroup, WeakCacheMap.KeyedValue<JPDAThreadGroupImpl> {
    
    private final DebuggingViewSupportImpl dvSupport;
    private final JPDAThreadGroupImpl tg;

    JPDADVThreadGroup(DebuggingViewSupportImpl dvSupport, JPDAThreadGroupImpl tg) {
        this.dvSupport = dvSupport;
        this.tg = tg;
    }

    @Override
    public String getName() {
        return tg.getName();
    }

    @Override
    public DVThreadGroup getParentThreadGroup() {
        return dvSupport.get(tg.getParentThreadGroup());
    }

    @Override
    public DVThread[] getThreads() {
        return dvSupport.get(tg.getThreads());
    }

    @Override
    public DVThreadGroup[] getThreadGroups() {
        return dvSupport.get(tg.getThreadGroups());
    }

    @Override
    public JPDAThreadGroupImpl getKey() {
        return tg;
    }
    
}
