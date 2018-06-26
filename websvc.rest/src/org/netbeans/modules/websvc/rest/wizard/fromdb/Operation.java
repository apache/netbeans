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
package org.netbeans.modules.websvc.rest.wizard.fromdb;

import org.netbeans.modules.websvc.rest.codegen.RestMethod;

public enum Operation implements RestMethod {

    CREATE("javax.ws.rs.POST", "create", true ),
    EDIT("javax.ws.rs.PUT", "edit", "{id}" ),
    REMOVE("javax.ws.rs.DELETE", "remove", "{id}"),
    FIND("javax.ws.rs.GET", "find", "{id}"),
    FIND_ALL("javax.ws.rs.GET", "findAll", true),
    FIND_RANGE("javax.ws.rs.GET", "findRange", "{from}/{to}"),
    COUNT("javax.ws.rs.GET", "countREST", "count");

    private String method, methodName, uriPath;
    private boolean override;
    
    private Operation(String method, String methodName, boolean override) {
        this.method = method;
        this.methodName = methodName;
        this.override = override;
    }

    private Operation(String method, String methodName) {
        this.method = method;
        this.methodName = methodName;
    }

    private Operation(String method, String methodName, String uriPath) {
        this.method = method;
        this.methodName = methodName;
        this.uriPath = uriPath;
    }
    
    public String getMethod() {
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getUriPath() {
        return uriPath;
    }
    
    public boolean overrides(){
        return override;
    }
}