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

package org.netbeans.modules.project.ant;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.ReferenceHelper;


/**
 * @author  mkleint
 */
public abstract class AntBuildExtenderAccessor {

    public static AntBuildExtenderAccessor DEFAULT = null;
    
    public static String AUX_NAMESPACE        = "http://www.netbeans.org/ns/ant-build-extender/1"; //NOI18N
    /**
     * 
     */
    public static String ELEMENT_ROOT         = "buildExtensions"; //NOI18N
    
    /**
     * 
     */
    public static String ELEMENT_EXTENSION    = "extension"; //NOI18N
    
    /**
     * 
     */
    public static String ELEMENT_DEPENDENCY   = "dependency"; //NOI18N
    
    /**
     * 
     */
    public static String ATTR_TARGET      = "target";
    /**
     * 
     */
    public static String ATTR_DEPENDSON   = "dependsOn";
    /**
     * 
     */
    public static String ATTR_ID          = "id";
    /**
     * 
     */
    public static String ATTR_FILE        = "file";
    
    
    

    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = AntBuildExtender.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public abstract AntBuildExtender createExtender(AntBuildExtenderImplementation impl);
    
    public abstract AntBuildExtender createExtender(AntBuildExtenderImplementation impl, ReferenceHelper refHelper);
    
    public abstract Set<AntBuildExtender.Extension> getExtensions(AntBuildExtender ext);
    
    public abstract String getPath(AntBuildExtender.Extension extension);

    public abstract Map<String, Collection<String>> getDependencies(AntBuildExtender.Extension extension);

    
}
