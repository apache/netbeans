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
package org.netbeans.modules.javaee.resources.impl.model;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.javaee.resources.api.model.JndiResourcesAbstractModel;
import org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel;
import org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelUnit;

/**
 * Implementation of the MetaModel.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JndiResourcesModelImpl extends JndiResourcesAbstractModel implements MetadataModelImplementation<JndiResourcesModel> {

    private JndiResourcesModelImpl(JndiResourcesModelUnit modelUnit) {
        super(modelUnit);
    }

    @Override
    public <R> R runReadAction(final MetadataModelAction<JndiResourcesModel, R> action) throws MetadataModelException, IOException {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    @Override
    public boolean isReady() {
        return !getHelper().isJavaScanInProgress();
    }

    @Override
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<JndiResourcesModel, R> action) throws MetadataModelException, IOException {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    public static MetadataModelImplementation<JndiResourcesModel> createMetaModel(JndiResourcesModelUnit modelUnit) {
        return new JndiResourcesModelImpl(modelUnit);
    }

}
