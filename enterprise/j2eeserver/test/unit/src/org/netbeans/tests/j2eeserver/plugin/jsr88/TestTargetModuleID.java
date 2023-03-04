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

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 *
 * @author  nn136682
 */
public class TestTargetModuleID implements TargetModuleID {

    private final Target target;

    private final String moduleID;

    private final  ModuleType type;

    private TestTargetModuleID parent;

    private TestTargetModuleID[] children;

    public TestTargetModuleID(Target target, String module, ModuleType type) {
        this.target = target;
        this.moduleID = module.replace('.', '_');
        this.type = type;
    }
    public TargetModuleID[] getChildTargetModuleID() {
        return children;
    }

    public ModuleType getModuleType() {
        return type;
    }

    public String getModuleID() {
        return moduleID;
    }

    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }

    public Target getTarget() {
        return target;
    }

    public String getWebURL() {
        return null;
    }

    @Override
    public String toString() {
        return "TestPlugin:" + target.getName() + ":" + moduleID;
    }

    public TestTargetModuleID getParent() {
        return parent;
    }

    public String getModuleUrl() {
        return moduleID;
    }
}
