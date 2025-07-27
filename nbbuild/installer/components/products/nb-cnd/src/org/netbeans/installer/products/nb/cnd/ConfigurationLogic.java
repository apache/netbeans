/**
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

package org.netbeans.installer.products.nb.cnd;

import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.utils.exceptions.InitializationException;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    // Constants
    private static final String CND_CLUSTER = 
            "{cnd-cluster}"; // NOI18N
    private static final String CNDEXT_CLUSTER = 
            "{cndext-cluster}"; // NOI18N
    private static final String DLIGHT_CLUSTER = 
            "{dlight-cluster}"; // NOI18N
    private static final String ID = 
            "CND"; // NOI18N

    
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            CND_CLUSTER, CNDEXT_CLUSTER, DLIGHT_CLUSTER}, ID);
    }
}
