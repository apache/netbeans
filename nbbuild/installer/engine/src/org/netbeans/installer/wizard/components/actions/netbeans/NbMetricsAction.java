/**
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
package org.netbeans.installer.wizard.components.actions.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 */
public class NbMetricsAction extends WizardAction {

    public NbMetricsAction() {
    }

    @Override
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> products = new LinkedList<Product>();
        products.addAll(registry.getProducts(DetailedStatus.INSTALLED_SUCCESSFULLY));
        products.addAll(registry.getProducts(DetailedStatus.INSTALLED_WITH_WARNINGS));
        for (Product product : products) {
            final String uid = product.getUid();
            if (uid.equals("nb-base") || uid.equals("nb-all")) {
                File location = product.getInstallationLocation();
                try {
                    boolean metricsEnabled = Boolean.getBoolean(ENABLE_NETBEANS_METRICS_PROPERTY);
                    LogManager.log("Set usage statistics enabled of NetBeans at " + location + " to " + metricsEnabled);
                    NetBeansUtils.setUsageStatistics(location, metricsEnabled);
                } catch (IOException e) {
                    LogManager.log(e);
                }
                break;
            }
        }
    }

    @Override
    public boolean canExecuteForward() {
        return UiMode.getCurrentUiMode() != UiMode.SILENT;
    }

    @Override
    public WizardActionUi getWizardUi() {
        return null;
    }
    public static final String ENABLE_NETBEANS_METRICS_PROPERTY =
            "enable.netbeans.metrics";
}
