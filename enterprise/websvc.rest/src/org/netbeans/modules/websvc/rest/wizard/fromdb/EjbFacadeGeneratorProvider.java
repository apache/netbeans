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

package org.netbeans.modules.websvc.rest.wizard.fromdb;

import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGeneratorProvider;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;

/**
 *
 * @author Andrei Badea
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.persistence.wizard.fromdb.FacadeGeneratorProvider.class)
public class EjbFacadeGeneratorProvider implements FacadeGeneratorProvider {

    public String getGeneratorType() {
        return "ejb_rest_facade"; // NOI18N
    }

    public FacadeGenerator createGenerator() {
        return new EjbFacadeGenerator();
    }
    
    public FacadeGenerator createGenerator(EntityResourceBeanModel model) {
        return new EjbFacadeGenerator(model);
    }
}
