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
package org.netbeans.modules.j2ee.jboss4;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/**
 *
 * @author Petr Hejl
 */
public class WrappedTargetModuleID implements TargetModuleID {

    private final TargetModuleID original;

    private final String webUrl;

    private final String moduleId;

    private final TargetModuleID parent;

    public WrappedTargetModuleID(TargetModuleID original, String webUrl,
            String moduleId, TargetModuleID parent) {
        this.original = original;
        this.webUrl = webUrl;
        this.moduleId = moduleId;
        this.parent = parent;
    }

    @Override
    public Target getTarget() {
        return original.getTarget();
    }

    @Override
    public String getModuleID() {
        if (moduleId != null) {
            return moduleId;
        }
        return original.getModuleID();
    }

    @Override
    public String getWebURL() {
        if (webUrl != null) {
            return webUrl;
        }
        return original.getWebURL();
    }

    @Override
    public String toString() {
        return original.toString();
    }

    @Override
    public TargetModuleID getParentTargetModuleID() {
        if (parent != null) {
            return parent;
        }
        return original.getParentTargetModuleID();
    }

    @Override
    public TargetModuleID[] getChildTargetModuleID() {
        TargetModuleID[] ids = original.getChildTargetModuleID();
        if (ids == null) {
            return null;
        }
        TargetModuleID[] ret = new TargetModuleID[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = new WrappedTargetModuleID(ids[i], webUrl, null, this);
        }
        return ret;
    }

    public TargetModuleID getOriginal() {
        return original;
    }
}
