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
package org.netbeans.modules.javaee.specs.support.api.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Provides the utilities to deal with JNDI namespace as defined in
 * the Java EE 6 spec.
 *
 * @author Petr Hejl
 * @since 1.23
 */
public final class JndiNamespacesDefinition {

    /**
     * The java:module namespace as defined by the Java EE spec.
     */
    public static final String MODULE_NAMESPACE = "java:module"; // NOI18N

    /**
     * The java:app namespace as defined by the Java EE spec.
     */
    public static final String APPLICATION_NAMESPACE = "java:app"; // NOI18N

    /**
     * The java:global namespace as defined by the Java EE spec.
     */
    public static final String GLOBAL_NAMESPACE = "java:global"; // NOI18N

    private static final Set<String> PREFIXES = new HashSet<String>();

    private static final String DEFAULT_PREFIX = "java:comp/env/"; // NOI18N

    static {
        Collections.addAll(PREFIXES, "java:comp/", MODULE_NAMESPACE + "/", APPLICATION_NAMESPACE + "/", GLOBAL_NAMESPACE + "/"); // NOI18N
    }

    private JndiNamespacesDefinition() {
        super();
    }

    /**
     * Normalizes the JNDI name. If the JNDI name starts with a spec defined
     * prefix it is returned. Otherwise if the defaultNamespace is defined
     * it is prefixed with it. As fallback it is prefixed with spec default
     * <code>java:comp/env/</code>
     *
     * @param jndi the JNDI name to normalize
     * @param defaultNamespace the default namespace; may be <code>null</code>
     * @return the normalized JNDI name
     */
    public static String normalize(String jndi, String defaultNamespace) {
        for (String p : PREFIXES) {
            if (jndi.startsWith(p)) {
                return jndi;
            }
        }
        // FIXME ugly hack for additional JBoss namespaces
        if (jndi.startsWith("java:/") || jndi.startsWith("java:jboss/")) { // NOI18N
            return jndi;
        }
        return defaultNamespace == null ? DEFAULT_PREFIX + jndi : defaultNamespace + "/" + jndi; // NOI18N
    }

    @CheckForNull
    public static String getNamespace(@NonNull String jndi) {
        for (String p : PREFIXES) {
            if (jndi.startsWith(p)) {
                return p.substring(0, p.length() - 1);
            }
        }
        // FIXME ugly hack for additional JBoss namespaces
        if (jndi.startsWith("java:/") || jndi.startsWith("java:jboss/")) { // NOI18N
            return jndi.substring(0, jndi.indexOf('/'));
        }
        return null;
    }
}
