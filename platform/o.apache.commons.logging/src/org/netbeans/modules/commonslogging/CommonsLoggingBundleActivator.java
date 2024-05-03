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
package org.netbeans.modules.commonslogging;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class CommonsLoggingBundleActivator implements BundleActivator {

    @Override
    public void start(BundleContext bc) throws Exception {
        // Configure commons-logging to log using java.util.logging unless
        // overridden by the user
        if((! System.getProperties().containsKey("org.apache.commons.logging.Log"))
                && (! System.getProperties().containsKey("org.apache.commons.logging.LogFactory"))) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
            System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.LogFactoryImpl");
        }
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
    }

}
