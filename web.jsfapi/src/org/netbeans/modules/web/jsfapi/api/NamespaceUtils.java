/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsfapi.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Contains constants and helper methods for work with new ang legacy namespaces.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class NamespaceUtils {

    /** Location of namespaces since JSF 2.2. */
    public static final String JCP_ORG_LOCATION = "http://xmlns.jcp.org"; //NOI18N
    
    /** Location of namespaces up to JSF 2.1. */
    public static final String SUN_COM_LOCATION = "http://java.sun.com";  //NOI18N

    /** Mapping of the new namespace to the legacy one. */
    public static final Map<String, String> NS_MAPPING = new HashMap<String, String>(8);

    static {
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/html", "http://java.sun.com/jsf/html");                     //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/core", "http://java.sun.com/jsf/core");                     //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/core", "http://java.sun.com/jsp/jstl/core");           //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsp/jstl/functions", "http://java.sun.com/jsp/jstl/functions"); //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/facelets", "http://java.sun.com/jsf/facelets");             //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/composite", "http://java.sun.com/jsf/composite");           //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf", "http://java.sun.com/jsf");                               //NOI18N
        NS_MAPPING.put("http://xmlns.jcp.org/jsf/passthrough", "http://java.sun.com/jsf/passthrough");       //NOI18N
    }

    /**
     * Takes map of libraries and namespace and return library for the namespace or its legacy version.
     * @param map map of libraries
     * @param ns namespace to examine
     * @return library for the given or its legacy namespace, {@code null} if no such library was found
     */
    @CheckForNull
    public static <T> T getForNs(Map<String, T> map, String ns) {
        T result = checkMapForNs(map, ns);

        // try out shortened URL without ending slash if available - issue #226002
        if (result == null) {
            if (ns.endsWith("/")) { //NOI18N
                ns = ns.substring(0, ns.length() - 1);
                return checkMapForNs(map, ns);
            }
        }

        return result;
    }

    private static <T> T checkMapForNs(Map<String, T> map, String ns) {
        T result = map.get(ns);
        if (result == null) {
            if (NS_MAPPING.containsKey(ns)) {
                result = map.get(NS_MAPPING.get(ns));
            } else if (ns.startsWith(DefaultLibraryInfo.COMPOSITE.getLegacyNamespace())) {
                result = map.get(ns.replace(DefaultLibraryInfo.COMPOSITE.getLegacyNamespace(), DefaultLibraryInfo.COMPOSITE.getNamespace()));
            }
        }
        return result;
    }

    /**
     * Says whether given namespaces collection contains namespace of the library.
     * @param collection collection of namespaces
     * @param library library to check
     * @return {@code true} if the collection contains new or legacy library namespace, {@code false} otherwise
     */
    public static boolean containsNsOf(Collection<String> collection, DefaultLibraryInfo library) {
        if (collection.contains(library.getNamespace())) {
            return true;
        }
        if (library.getLegacyNamespace() != null) {
            return collection.contains(library.getLegacyNamespace());
        }
        return false;
    }

    public static Set<String> getAvailableNss(Map<String, ? extends Library> libraries, boolean jsf22plus) {
        Set<String> nss = new HashSet<String>();
        for (Map.Entry<String, ? extends Library> entry : libraries.entrySet()) {
            // library well known namespace
            nss.add(entry.getKey());

            // in case of JSF 2.2 add also its legacy namespaces
            if (jsf22plus) {
                Library library = entry.getValue();
                nss.add(library.getNamespace());
                if (NS_MAPPING.containsKey(library.getNamespace())) {
                    nss.add(NS_MAPPING.get(library.getNamespace()));
                }
            }
        }
        return nss;
    }

}
