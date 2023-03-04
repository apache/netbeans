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
package org.netbeans.modules.javaee.wildfly.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author <a href="mailto:ehugonne@redhat.com">Emmanuel Hugonnet</a> (c) 2015 Red Hat, inc.
 */
public class WildflyJaxrsResource {

    private final String serverUrl;
    private final String className;
    private final String path;
    private final Set<String> httpMethods = new HashSet<>();

    public WildflyJaxrsResource(String className, String path, String serverUrl, List<String> methods) {
        this.className = className;
        this.path = path;
        this.httpMethods.addAll(methods);
        this.serverUrl = serverUrl;
    }

    public void addMethods(List<String> methods) {
        this.httpMethods.addAll(methods);
    }

    public Set<String> getMethods() {
        return Collections.unmodifiableSet(httpMethods);
    }

    public String getClassName() {
        return className;
    }

    public String getPath() {
        return path;
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
