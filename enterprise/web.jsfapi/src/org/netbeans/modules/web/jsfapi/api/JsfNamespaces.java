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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Benjamin Asbach
 */
public final class JsfNamespaces {

    private JsfNamespaces() {
    }

    static final Map<String, String> JAVA_SUN_COM;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("c", "http://java.sun.com/jsp/jstl/core");
        map.put("cc", "http://java.sun.com/jsf/composite");
        map.put("f", "http://java.sun.com/jsf/core");
        map.put("h", "http://java.sun.com/jsf/html");
        map.put("ui", "http://java.sun.com/jsf/facelets");
        map.put("mdjnm", "http://mojarra.dev.java.net/mojarra_ext");
        JAVA_SUN_COM = Collections.unmodifiableMap(map);
    }

    static final Map<String, String> XMLNS_JCP_ORG;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("c", "http://xmlns.jcp.org/jsp/jstl/core");
        map.put("cc", "http://xmlns.jcp.org/jsf/composite");
        map.put("f", "http://xmlns.jcp.org/jsf/core");
        map.put("h", "http://xmlns.jcp.org/jsf/html");
        map.put("ui", "http://xmlns.jcp.org/jsf/facelets");
        map.put("mdjnm", "http://mojarra.dev.java.net/mojarra_ext");
        XMLNS_JCP_ORG = Collections.unmodifiableMap(map);
    }

    static final Map<String, String> JAKARTA;
    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("c", "jakarta.tags.core");
        map.put("cc", "jakarta.faces.composite");
        map.put("f", "jakarta.faces.core");
        map.put("h", "jakarta.faces.html");
        map.put("ui", "jakarta.faces.facelets");
        map.put("mdjnm", "http://mojarra.dev.java.net/mojarra_ext");
        JAKARTA = Collections.unmodifiableMap(map);
    }
}
