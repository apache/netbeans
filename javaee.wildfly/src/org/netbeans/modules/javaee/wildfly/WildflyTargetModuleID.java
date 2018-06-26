/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return (TargetModuleID[]) childs.toArray(new TargetModuleID[childs.size()]);
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
