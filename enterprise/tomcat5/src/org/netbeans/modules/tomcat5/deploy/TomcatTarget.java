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

package org.netbeans.modules.tomcat5.deploy;

import javax.enterprise.deploy.spi.Target;

/** Dummy implementation of target for Tomcat 5 server
 *
 * @author  Radim Kubacki
 */
public class TomcatTarget implements Target {

    private String name;

    private String desc;

    private String uri;

    public TomcatTarget (String name, String desc, String uri) {
        this.name = name;
        this.desc = desc;
        this.uri = uri;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public String getDescription () {
        return desc;
    }
    
    public String getServerUri () {
        return uri;
    }
    
    @Override
    public String toString () {
        return name;
    }
}
