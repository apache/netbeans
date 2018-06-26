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

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;

/**
 *
 * @author Petr Hejl
 */
public class TestDeploymentFactory implements DeploymentFactory {

    private Map managers = new HashMap();

    private final String prefix;

    /** Creates a new instance of DepFactory */
    protected TestDeploymentFactory(String prefix) {
        assert prefix != null;
        this.prefix = prefix;
    }

    public static TestDeploymentFactory create(Map map) {
        return new TestDeploymentFactory((String) map.get("prefix"));
    }

    public synchronized javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager(String str, String str1, String str2) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        TestDeploymentManager manager = (TestDeploymentManager) managers.get(str + str1 + str2);
        if (manager == null){
            manager = new TestDeploymentManager(str, str1, str2);
            managers.put(str, manager);
        }
        return manager;
    }
    
    public synchronized javax.enterprise.deploy.spi.DeploymentManager getDisconnectedDeploymentManager(String str) throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException {
        TestDeploymentManager manager = (TestDeploymentManager) managers.get(str);
        if (manager == null) {
            manager = new TestDeploymentManager(str,"","");
            managers.put(str, manager);
        }
        return manager;
    }
    
    public String getDisplayName() {
        return "Sample JSR88 plugin";// PENDING parameterize this.
    }
    
    public String getProductVersion() {
        return "0.9";// PENDING version this plugin somehow?
    }
    
    public boolean handlesURI(String str) {
        return (str != null && str.startsWith(prefix));
    }
    
}
