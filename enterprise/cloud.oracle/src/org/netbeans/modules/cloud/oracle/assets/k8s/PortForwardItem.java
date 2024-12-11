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
package org.netbeans.modules.cloud.oracle.assets.k8s;

import io.fabric8.kubernetes.client.PortForward;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.cloud.oracle.items.ContextValuesProvider;

/**
 *
 * @author Jan Horvath
 */
public class PortForwardItem implements ContextValuesProvider {
    final PodItem pod;
    final int portLocal;
    final int portRemote;
    final PortForward forward;

    public PortForwardItem(PodItem pod, int portLocal, int portRemote, PortForward forward) {
        this.pod = pod;
        this.portLocal = portLocal;
        this.portRemote = portRemote;
        this.forward = forward;
    }

    public PodItem getPod() {
        return pod;
    }

    public int getPortLocal() {
        return portLocal;
    }

    public int getPortRemote() {
        return portRemote;
    }
    
    public PortForward getForward() {
        return forward;
    }

    @Override
    public Map<String, String> getContextValues() {
        return Collections.singletonMap("portForward", "http://localhost:" + portLocal); //NOI18N
    }

    
}
