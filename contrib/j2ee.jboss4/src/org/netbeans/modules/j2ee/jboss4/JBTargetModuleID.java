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
package org.netbeans.modules.j2ee.jboss4;

import java.util.Vector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
/**
 *
 * @author whd
 */
class JBTargetModuleID implements TargetModuleID {

    private Target target;
    private String jar_name;
    private String context_url;

    private Vector childs = new Vector();
    private TargetModuleID  parent = null;

    JBTargetModuleID(Target target) {
        this(target, "");
    }

    JBTargetModuleID(Target target, String jar_name) {
        this.target = target;
        this.jar_name = jar_name;

    }
    public void setContextURL(String context_url) {
        this.context_url = context_url;
    }
    public void setJARName(String jar_name) {
        this.jar_name = jar_name;
    }

    public void setParent(JBTargetModuleID parent) {
        this.parent = parent;

    }

    public void addChild(JBTargetModuleID child) {
        childs.add(child);
        child.setParent(this);
    }

    public TargetModuleID[] getChildTargetModuleID() {
        return (TargetModuleID[]) childs.toArray(new TargetModuleID[0]);
    }
    //Retrieve a list of identifiers of the children of this deployed module.
    public String getModuleID() {
        return jar_name ;
    }
    //         Retrieve the id assigned to represent the deployed module.
    public TargetModuleID getParentTargetModuleID() {

        return parent;
    }
    //Retrieve the identifier of the parent object of this deployed module.
    public Target getTarget() {
        return target;
    }
    //Retrieve the name of the target server.
    public String getWebURL() {
        return context_url;//"http://" + module_id; //NOI18N
    }
    //If this TargetModulID represents a web module retrieve the URL for it.
    public String toString() {
        return getModuleID() +  hashCode();
    }
}
