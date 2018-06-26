/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    final static private Map<String,Hk2TargetModuleID> knownModules =
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
                return target.getServerUri() + "/" + contextPath.replaceAll(" ", "%20");
            } else {
                return target.getServerUri() + contextPath.replaceAll(" ", "%20");
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
        return children.toArray(new TargetModuleID[children.size()]);
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
