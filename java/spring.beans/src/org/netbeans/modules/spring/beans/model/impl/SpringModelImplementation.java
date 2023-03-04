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
package org.netbeans.modules.spring.beans.model.impl;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.spring.api.beans.model.AbstractModelImplementation;
import org.netbeans.modules.spring.api.beans.model.ModelUnit;
import org.netbeans.modules.spring.api.beans.model.SpringModel;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SpringModelImplementation extends AbstractModelImplementation implements MetadataModelImplementation<SpringModel> {

    private SpringModelImplementation(ModelUnit modelUnit) {
        super(modelUnit);
    }

    @Override
    public <R> R runReadAction(final MetadataModelAction<SpringModel, R> action) throws MetadataModelException, IOException {
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
    public <R> Future<R> runReadActionWhenReady(final MetadataModelAction<SpringModel, R> action) throws MetadataModelException, IOException {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {

            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }
    
    public static MetadataModelImplementation<SpringModel> createMetaModel(ModelUnit unit) {
        return new SpringModelImplementation(unit);
    }
}
