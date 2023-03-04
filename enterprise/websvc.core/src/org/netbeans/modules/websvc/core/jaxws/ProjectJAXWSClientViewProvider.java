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

package org.netbeans.modules.websvc.core.jaxws;

import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider.class)
public class ProjectJAXWSClientViewProvider implements JAXWSClientViewProvider {
    
    private ProjectJAXWSClientView clientView = new ProjectJAXWSClientView();
    /** Creates a new instance of ProjectJAXWSClientViewProvider */
    public ProjectJAXWSClientViewProvider() {
    }

    public JAXWSClientView findJAXWSClientView() {
        return JAXWSClientViewFactory.createJAXWSClientView(clientView);
    }
    
}
