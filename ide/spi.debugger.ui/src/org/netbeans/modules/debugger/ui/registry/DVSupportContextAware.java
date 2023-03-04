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

package org.netbeans.modules.debugger.ui.registry;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView.DVFilter;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.Deadlock;

/**
 *
 * @author Martin Entlicher
 */
public class DVSupportContextAware extends DVSupport implements ContextAwareService<DVSupport>  {

    private final String serviceName;
    private ContextProvider context;
    private DVSupport delegate;
    
    private DVSupportContextAware(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public DVSupport forContext(ContextProvider context) {
        if (context == this.context) {
            return this;
        } else {
            return (DVSupport) ContextAwareSupport.createInstance(serviceName, context);
        }
    }
    
    // A dummy impl. follows:
    
    @Override
    public STATE getState() {
        return STATE.DISCONNECTED;
    }

    @Override
    public List<DVThread> getAllThreads() {
        return Collections.emptyList();
    }

    @Override
    public DVThread getCurrentThread() {
        return null;
    }

    @Override
    public String getDisplayName(DVThread thread) {
        return "";
    }

    @Override
    public Image getIcon(DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return null;
    }

    @Override
    public void resume() {
    }

    @Override
    public Set<Deadlock> getDeadlocks() {
        return Collections.emptySet();
    }

    @Override
    protected List<DVFilter> getFilters() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return super.toString() + " with service = "+serviceName+" and delegate = "+delegate;
    }

    /**
     * Creates instance of <code>ContextAwareService</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>ContextAwareService</code> instance
     */
    static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
        String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
        return new DVSupportContextAware(serviceName);
    }

}
