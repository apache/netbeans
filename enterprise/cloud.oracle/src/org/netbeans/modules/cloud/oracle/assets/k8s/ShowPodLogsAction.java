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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.cloud.oracle.compute.PodItem;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.ShowPodLogsAction"
)
@ActionRegistration(
        displayName = "#PodLogs",
        asynchronous = true
)

@NbBundle.Messages({
    "PodLogs=Start port forwarding"
})
public class ShowPodLogsAction implements ActionListener {
    
    private PodItem pod;

    public ShowPodLogsAction(PodItem pod) {
        this.pod = pod;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
//        KubernetesLoaders.startPortForward(pod);
        
        InputOutputProvider<?, ?, ?, ?> newSpiDef
                    = Lookup.getDefault().lookup(InputOutputProvider.class);
        Object io = newSpiDef.getIO("test io", true, Lookup.EMPTY);
//        newSpiDef.getOut(io);
    }
    
}
