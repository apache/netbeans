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

package org.netbeans.modules.hibernateweb.framework;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 * This class provides utility methods such as getting the project from a 
 * WebModule to be used in Hibernate Framework Provider classes.
 * 
 * @author gowri
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class Util {

    /**
     * Returns the enclosing project that this web module is in.
     * @param webModule the web module for which the project needs to be determined.
     * @return the enclosing project or null of there is no project found.
     */
    public static Project getEnclosingProjectFromWebModule(WebModule webModule) {
        FileObject documentBase = webModule.getDocumentBase();
        if(documentBase == null) {
            documentBase = webModule.getDeploymentDescriptor();
            if (documentBase == null) {
                documentBase = webModule.getWebInf();
                if (documentBase == null) {
                    return null;
                }
            }
        }
        return FileOwnerQuery.getOwner(documentBase);
    }
}
