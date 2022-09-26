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
package org.netbeans.modules.j2ee.ddloaders.ejb;

/**
 * Recognizes ejb-jar.xml for Jakarta EE 9/9.1/10, ejb-jar with version number 4.0.
 * Needed for providing a different set of actions than for older versions 
 * of ejb-jar.xml.
 * See #76967.
 * 
 * @author pepness
 */
public class EjbJar40DataLoader extends EjbJarDataLoader{
    
    private static final long serialVersionUID = 1L;
    private static final String REQUIRED_MIME_PREFIX_4_0 = "text/x-dd-ejbjar4.0"; // NOI18N

    public EjbJar40DataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject");  // NOI18N
    }

    protected String actionsContext() {
        return "Loaders/text/x-dd-ejbjar4.0/Actions/"; // NOI18N
    }
    
    protected String[] getSupportedMimeTypes(){
        return new String[]{REQUIRED_MIME_PREFIX_4_0};
    }

}
