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
package org.netbeans.modules.form.binding;

import org.netbeans.modules.form.BindingDesignSupport;
import org.netbeans.modules.form.BindingDesignSupportProvider;
import org.netbeans.modules.form.FormModel;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider of binding support.
 *
 * @author Jan Stola
 */
@ServiceProvider(service=BindingDesignSupportProvider.class)
public class BindingDesignSupportProviderImpl implements BindingDesignSupportProvider {

    @Override
    public BindingDesignSupport create(FormModel formModel) {
        return new BindingDesignSupportImpl(formModel);
    }
    
}
