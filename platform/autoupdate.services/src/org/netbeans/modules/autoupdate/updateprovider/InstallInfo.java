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

package org.netbeans.modules.autoupdate.updateprovider;

import java.net.URL;
import org.netbeans.spi.autoupdate.CustomInstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public final class InstallInfo {
    
    private UpdateItemImpl item;
    
    /** Creates a new instance of InstallInfo */
    public InstallInfo (UpdateItemImpl item) {
        this.item = item;
    }
    
    public String getTargetCluster () {
        String res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl().getTargetCluster ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().getTargetCluster ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public Boolean needsRestart () {
        Boolean res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl ().needsRestart ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().needsRestart ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public Boolean isGlobal () {
        Boolean res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getUpdateItemDeploymentImpl ().isGlobal ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getUpdateItemDeploymentImpl ().isGlobal ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public URL getDistribution () {
        URL res = null;
        if (item instanceof ModuleItem) {
            res = ((ModuleItem) item).getDistribution ();
        } else if (item instanceof LocalizationItem) {
            res = ((LocalizationItem) item).getDistribution ();
        } else if (item instanceof FeatureItem) {
            assert false : "Feature not supported yet.";
        } else {
            assert false : "Unkown type of UpdateItem " + item;
        }
        return res;
    }
    
    public CustomInstaller getCustomInstaller () {
        CustomInstaller res = null;
        if (item instanceof NativeComponentItem) {
            res = ((NativeComponentItem) item).getUpdateItemDeploymentImpl ().getCustomInstaller ();
        }
        return res;
    }
    
    public UpdateItemImpl getUpdateItemImpl () {
        return item;
    }
}
