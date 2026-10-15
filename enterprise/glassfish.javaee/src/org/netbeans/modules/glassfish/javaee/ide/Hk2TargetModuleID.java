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

package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2TargetModuleID implements TargetModuleID {

    private final Hk2Target target;
    private final String docBaseURI;
    private String contextPath;
    private final String location;
    private TargetModuleID parent;
    private final Vector<TargetModuleID> children;
    private static final Map<String,Hk2TargetModuleID> knownModules =
            new HashMap<String,Hk2TargetModuleID>();
    
    private Hk2TargetModuleID(Hk2Target target, String docBaseURI, String contextPath, String location) {
        this.target = target;
        this.docBaseURI = docBaseURI;
        this.contextPath = contextPath;
        this.location = location;
        this.parent = null;
        this.children = new Vector<TargetModuleID>();
    }

    public static Hk2TargetModuleID get(Hk2Target target, String docBaseURI, String contextPath, String location) {
        return get(target, docBaseURI, contextPath, location, false);
    }

    public static Hk2TargetModuleID get(Hk2Target target, String docBaseURI, String contextPath, String location, boolean clearChildren) {
        synchronized(knownModules) {
            // Normalize the location data
            if (!location.endsWith(File.separator)) {
                location += File.separator;
            }
            String key = target.getServerUri()+docBaseURI+location;
            Hk2TargetModuleID retVal = knownModules.get(key);
            if (null == retVal) {
                retVal = new Hk2TargetModuleID(target, docBaseURI, contextPath, location);
                knownModules.put(key,retVal);
            } else {
                if (null != contextPath)
                    retVal.setPath(contextPath);
            }
            if (clearChildren) {
                retVal.children.clear();
            }
            return retVal;

        }
    }

    // Retrieve the identifier of the parent object of this deployed module.
    public Target getTarget() {
        return target;
    }
    
    // Retrieve a list of identifiers of the children of this deployed module.
    public String getModuleID() {
        return docBaseURI;
    }

    public String getWebURL() {
        // !PW FIXME path ought to be URL encoded by the time we get here.
        if (null != contextPath) {
            if(!contextPath.startsWith("/")) {
                return target.getServerUri() + "/" + contextPath.replace(" ", "%20");
            } else {
                return target.getServerUri() + contextPath.replace(" ", "%20");
            }
        }
        return null;
    }
    
    public String getLocation() {
        return location;
    }
    
    // Retrieve the id assigned to represent the deployed module.
    public TargetModuleID getParentTargetModuleID() {
        return parent;
    }
    
    public TargetModuleID [] getChildTargetModuleID() {
        return children.toArray(TargetModuleID[]::new);
    }
    
    public void setParent(Hk2TargetModuleID parent) {
        this.parent = parent;
    }
    
    public void setPath(String p) {
        this.contextPath = p;
    }

    public void addChild(Hk2TargetModuleID child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public String toString() {
        return getModuleID();
    }
    
}
