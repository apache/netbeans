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

package org.netbeans.modules.payara.jakartaee.ide;

import java.io.File;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.netbeans.modules.payara.jakartaee.RunTimeDDCatalog;
import org.netbeans.modules.payara.spi.RegisteredDDCatalog;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author raccah
 */
@ServiceProvider(service=RegisteredDDCatalog.class,path="Servers/Payara")
public class RegisteredDDCatalogImpl implements RegisteredDDCatalog {
    private void registerRunTimeDDCatalog(RunTimeDDCatalog catalog, ServerInstanceProvider pip) {
        if (catalog != null) {
            catalog.setInstanceProvider(pip);
        }
    }

    @Override
    public void registerRunTimeDDCatalog(ServerInstanceProvider pip) {
        registerRunTimeDDCatalog(RunTimeDDCatalog.getDefaultRunTimeDDCatalog(), pip);
    }

    @Override
    public void refreshRunTimeDDCatalog(ServerInstanceProvider pip, String installRoot) {
        RunTimeDDCatalog catalog = RunTimeDDCatalog.getRunTimeDDCatalog(pip);
        if (catalog != null) {
            catalog.refresh((installRoot != null) ? new File(installRoot) : null);
        }
    }
}
