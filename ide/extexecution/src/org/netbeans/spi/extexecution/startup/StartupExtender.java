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
package org.netbeans.spi.extexecution.startup;

import java.util.Map;
import org.netbeans.modules.extexecution.startup.ProxyStartupExtender;

/**
 *
 * @author Petr Hejl
 */
final class StartupExtender {

    /**
     * Legacy layer factory that is still used by binaries compiled against &lt 1.62
     * @param map
     * @return 
     */
    static StartupExtenderImplementation createProxy(Map<String,?> map) {
        return new ProxyStartupExtender(map);
    }

    /**
     * New layer factory, allows to declare quoting/escaping policy
     */
    static StartupExtenderImplementation createProxy2(Map<String,?> map) {
        return new ProxyStartupExtender.V2(map);
    }

    private StartupExtender() {}

}
