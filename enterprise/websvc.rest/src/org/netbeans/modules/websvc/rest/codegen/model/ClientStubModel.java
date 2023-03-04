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
package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * ClientStubModel
 *
 * @author Ayub Khan
 * @author ads
 */
public class ClientStubModel {
    
    public static final int EXPAND_LEVEL_MAX = 2;

    public ClientStubModel() {        
    }
    
    public ResourceModel createModel(Project p) {
        return new SourceModeler(p);
    }
    
    public ResourceModel createModel(FileObject wadl) {
        return new WadlModeler(wadl);
    }

    public static String normalizeName(final String name) {
        return toValidJavaName(name);
    }

    private static String toValidJavaName(String name) {
        if ( name == null || name.length() ==0 ){
            return name;
        }
        StringBuilder sb = new StringBuilder(name.length());
        if (Character.isJavaIdentifierStart(name.charAt(0))) {
            sb.append(name.charAt(0));
        } else {
            sb.append("_");
        }
        for (int i=1; i<name.length(); i++) {
            if (Character.isJavaIdentifierPart(name.charAt(i))) {
                sb.append(name.charAt(i));
            } else {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
