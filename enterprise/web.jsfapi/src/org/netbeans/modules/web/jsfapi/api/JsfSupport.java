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

package org.netbeans.modules.web.jsfapi.api;

import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public interface JsfSupport {

    public Project getProject();

    public ClassPath getClassPath();

    public WebModule getWebModule();

    public Library getLibrary(String namespace);

    /**
     * @return Library namespace to Library instance map
     */
    public Map<String, ? extends Library> getLibraries();

    public Lookup getLookup();

    public boolean isJsf22Plus();

    public boolean isJsf30Plus();
}
