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

package org.netbeans.modules.javaee.beanvalidation.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.beanvalidation.api.BeanValidationConfig;
import org.netbeans.modules.javaee.beanvalidation.spi.BeanValidationConfigProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 *
 * @author alexeybutenko
 */
public class BeanValidationConfigProviderImpl extends BeanValidationConfigProvider{

    private static final String DEFAULT_NAME = "validation.xml";    //NOI18N
    private static final BeanValidationConfigProviderImpl INSTANCE = new BeanValidationConfigProviderImpl();

    private  BeanValidationConfigProviderImpl() {}

    public static BeanValidationConfigProviderImpl getInstance() {
        return INSTANCE;
    }

    public List<BeanValidationConfig> getConfigs(Project project) {
        List<BeanValidationConfig> list = new ArrayList<BeanValidationConfig>();
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject webInf = wm.getWebInf();
            if (webInf != null) {
                FileObject defaultValidationConfig = webInf.getFileObject(DEFAULT_NAME);
                if (defaultValidationConfig != null) {
                    list.add(new BeanValidationConfigImpl(defaultValidationConfig));
                }
            }
        }
        return list;
    }

}
