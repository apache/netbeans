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

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.openide.NotifyDescriptor;
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
        id = "org.netbeans.modules.cloud.oracle.actions.SetClusterNamespace"
)
@ActionRegistration(
        displayName = "#ClusterNamespace",
        asynchronous = true
)

@NbBundle.Messages({
    "ClusterNamespace=Set a cluster namespace",
    "Namespace=Namespace"
})
public class SetClusterNamespaceAction implements ActionListener {

    private final ClusterItem context;

    public SetClusterNamespaceAction(ClusterItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Steps.getDefault()
                .executeMultistep(new NamespaceStep(context), Lookup.EMPTY)
                .thenAccept(values -> context.setNamespace(values.getValueForStep(NamespaceStep.class)));
    }

    public static final class NamespaceStep extends AbstractStep<String> {

        private final ClusterItem cluster;
        private final List<String> namespaces = new ArrayList<>();
        private String selectedNamespace = null;

        public NamespaceStep(ClusterItem cluster) {
            this.cluster = cluster;
        }

        @Override
        public void prepare(ProgressHandle handle, Steps.Values values) {
            KubernetesUtils.runWithClient(cluster, client -> {
                NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaceOperation = client.namespaces();
                NamespaceList namespaceList = namespaceOperation.list();
                for (Namespace ns : namespaceList.getItems()) {
                    namespaces.add(ns.getMetadata().getName());
                }
            });
        }

        @Override
        public NotifyDescriptor createInput() {
            List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(namespaces.size());
            for (String namespace : namespaces) {
                items.add(new NotifyDescriptor.QuickPick.Item(namespace, ""));
            }
            return new NotifyDescriptor.QuickPick(Bundle.Namespace(), Bundle.Namespace(), items, false);
        }

        @Override
        public boolean onlyOneChoice() {
            return namespaces.size() == 1;
        }

        @Override
        public void setValue(String selected) {
            selectedNamespace = selected;
        }

        @Override
        public String getValue() {
            return selectedNamespace;
        }

    }

}
