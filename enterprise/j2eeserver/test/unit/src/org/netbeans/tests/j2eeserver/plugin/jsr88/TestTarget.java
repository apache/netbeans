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


package org.netbeans.tests.j2eeserver.plugin.jsr88;


import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 *
 * @author  gfink
 */
public class TestTarget implements Target {

    private final String name;

    private final Map modules = new HashMap();

    public TestTarget(String name) {
        this.name = name;
    }

    public String getDescription() {
        return "Description for " + name;
    }

    public String getName() {
        return name;
    }

    public void add(TargetModuleID tmid) {
        modules.put(tmid.toString(), tmid);
    }

    public TargetModuleID getTargetModuleID(String id) {
        return (TargetModuleID) modules.get(id);
    }

    public TargetModuleID[] getTargetModuleIDs() {
        return (TargetModuleID[]) modules.values().toArray(new TargetModuleID[0]);
    }
}
