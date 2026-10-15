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
package org.netbeans.modules.javaee.wildfly;

import java.util.Locale;
import java.util.Vector;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;

/**
 *
 * @author whd
 */
public class WildflyTargetModuleID implements TargetModuleID {

    private Target target;
    private String jar_name;
    private String context_url;
    private J2eeModule.Type type;
    private boolean exploded = false;

    private Vector childs = new Vector();
    private TargetModuleID parent = null;

    public WildflyTargetModuleID(Target target, String moduleName, J2eeModule.Type type, boolean exploded) {
        this.target = target;
        this.type = type;
        this.exploded = exploded;
        this.jar_name = computeArchiveName(moduleName);
        if (type == Type.WAR) {
            context_url = '/' + this.jar_name.substring(0, this.jar_name.length() - 4);
        }
    }

    public Type getType() {
        return type;
    }

    private final String computeArchiveName(String moduleName) {
        if (Type.WAR.equals(type)) {
            if (!checkExtension(moduleName, ".war")) {
                return moduleName + ".war";
            }
            return moduleName;
        }

        if (Type.EAR.equals(type)) {
            if (!checkExtension(moduleName, ".ear")) {
                return moduleName + ".ear";
            }
            return moduleName;
        }
        if (Type.EJB.equals(type)) {
            if (!checkExtension(moduleName, ".jar")) {
                return moduleName + ".jar";
            }
            return moduleName;
        }
        if (Type.RAR.equals(type)) {
            if (!checkExtension(moduleName, ".rar")) {
                return moduleName + ".rar";
            }
            return moduleName;
        }
        if (Type.CAR.equals(type)) {
            if (!checkExtension(moduleName, ".car")) {
                return moduleName + ".car";
            }
        }
        return moduleName;
    }

    private boolean checkExtension(String name, String extension) {
        return name.toLowerCase(Locale.getDefault()).endsWith(extension);
    }

    public void setContextURL(String context_url) {
        this.context_url = context_url;
    }

    public void setJARName(String jar_name) {
        this.jar_name = computeArchiveName(jar_name);
    }

    public void setParent(WildflyTargetModuleID parent) {
        this.parent = parent;

    }

    public void addChild(WildflyTargetModuleID child) {
        childs.add(child);
        child.setParent(this);
    }

    @Override
    public TargetModuleID[] getChildTargetModuleID() {
        return (TargetModuleID[]) childs.toArray(new TargetModuleID[0]);
    }

    //Retrieve a list of identifiers of the children of this deployed module.
    @Override
    public String getModuleID() {
        return jar_name;
    }

    //         Retrieve the id assigned to represent the deployed module.
    @Override
    public TargetModuleID getParentTargetModuleID() {

        return parent;
    }

    //Retrieve the identifier of the parent object of this deployed module.
    @Override
    public Target getTarget() {
        return target;
    }

    //Retrieve the name of the target server.
    @Override
    public String getWebURL() {
        return context_url;//"http://" + module_id; //NOI18N
    }

    //If this TargetModulID represents a web module retrieve the URL for it.
    @Override
    public String toString() {
        return getModuleID();
    }

    public boolean isExploded() {
        return exploded;
    }
}
