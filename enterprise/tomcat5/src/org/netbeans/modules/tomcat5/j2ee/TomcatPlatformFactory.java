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

package org.netbeans.modules.tomcat5.j2ee;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;


/**
 * Tomcat's implementation of the J2eePlatformFactory.
 *
 * @author Stepan Herold
 */
public class TomcatPlatformFactory extends J2eePlatformFactory {

    /** Creates a new instance of TomcatPlatformFactory */
    public TomcatPlatformFactory() {
    }
    
    @Override
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        assert TomcatManager.class.isAssignableFrom(dm.getClass()) : this + " cannot create platform for unknown deployment manager:" + dm;
        return ((TomcatManager)dm).getTomcatPlatform();
    }
}
