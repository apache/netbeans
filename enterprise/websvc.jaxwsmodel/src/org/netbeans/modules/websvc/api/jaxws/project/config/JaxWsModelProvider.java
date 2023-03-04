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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import org.netbeans.modules.websvc.jaxwsmodel.project.JaxWsModelImpl;
import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileObject;

/** Accessor for JaxWsModel
 *
 * @author mkuchtiak
 */
public class JaxWsModelProvider {
    
    private static JaxWsModelProvider provider;
    
    /** Creates a new instance of ModelProvider */
    private JaxWsModelProvider() {
    }
    
    public static synchronized JaxWsModelProvider getDefault() {
        if (provider==null) {
            provider = new JaxWsModelProvider();
        }
        return provider;
    }
    
    public JaxWsModel getJaxWsModel(InputStream is) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs impl =
                org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(is);
        return (impl==null?null:new JaxWsModelImpl(impl));
    }

    public JaxWsModel getJaxWsModel(FileObject fo) throws IOException {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs impl =
                org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(fo.getInputStream());
        return (impl==null?null:new JaxWsModelImpl(impl,fo));
    }

    public Service createService(Object serviceImpl) {
        return new Service((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service)serviceImpl);
    }

    public Client createClient(Object clientImpl) {
        return new Client((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client)clientImpl);
    }
}
