/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
