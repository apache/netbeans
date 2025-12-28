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

import io.fabric8.kubernetes.client.dsl.PodResource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.netbeans.api.io.IOProvider;
import org.netbeans.api.io.InputOutput;
import org.netbeans.api.io.OutputWriter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
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
    "PodLogs=Start port forwarding",
    "OutputName=Pod {0} Log"
})
public class ShowPodLogsAction implements ActionListener {

    private PodItem podItem;

    public ShowPodLogsAction(PodItem podItem) {
        this.podItem = podItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InputOutput io = IOProvider.getDefault().getIO(Bundle.OutputName(podItem.getName()), true);
        OutputWriter writer = io.getOut();

        KubernetesUtils.runWithClient(podItem.getCluster(), client -> {
            PodResource pod = client.pods().inNamespace(podItem.getNamespace()).withName(podItem.getName());
            InputStream is = pod.watchLog().getOutput();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.println(line);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

}
