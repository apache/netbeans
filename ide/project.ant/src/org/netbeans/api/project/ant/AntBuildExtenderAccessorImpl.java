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
package org.netbeans.api.project.ant;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ant.AntBuildExtender.Extension;
import org.netbeans.modules.project.ant.AntBuildExtenderAccessor;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author mkleint
 */
class AntBuildExtenderAccessorImpl extends  AntBuildExtenderAccessor {

    static void createAccesor() {
        if (DEFAULT == null) {
            DEFAULT= new AntBuildExtenderAccessorImpl();
        }
    }
    
    private AntBuildExtenderAccessorImpl() {
    }
    

    public AntBuildExtender createExtender(AntBuildExtenderImplementation impl) {
        return new AntBuildExtender(impl);
    }
    
    public AntBuildExtender createExtender(AntBuildExtenderImplementation impl, ReferenceHelper refHelper) {
        return new AntBuildExtender(impl, refHelper);
    }
    
    public Set<Extension> getExtensions(AntBuildExtender ext) {
        return ext.getExtensions();
    }

    public String getPath(Extension extension) {
        return extension.getPath();
    }

    public Map<String, Collection<String>> getDependencies(Extension extension) {
        return extension.getDependencies();
    }

}
