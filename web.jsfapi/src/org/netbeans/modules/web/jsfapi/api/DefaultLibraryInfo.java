/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsfapi.api;

public enum DefaultLibraryInfo implements LibraryInfo {

    // JSF 2.0, JSF 2.1
    HTML("http://xmlns.jcp.org/jsf/html", "Html Basic", "h"), //NOI18N
    JSF_CORE("http://xmlns.jcp.org/jsf/core", "Jsf Core", "f"), //NOI18N
    JSTL_CORE("http://xmlns.jcp.org/jsp/jstl/core", "Jstl Core", "c"), //NOI18N
    JSTL_CORE_FUNCTIONS("http://xmlns.jcp.org/jsp/jstl/functions", "Jstl Core Functions", "fn"), //NOI18N
    FACELETS("http://xmlns.jcp.org/jsf/facelets", "Facelets", "ui"), //NOI18N
    COMPOSITE("http://xmlns.jcp.org/jsf/composite", "Composite Components", "cc"), //NOI18N

    // PrimeFaces
    PRIMEFACES("http://primefaces.org/ui", "PrimeFaces", "p"), //NOI18N
    PRIMEFACES_MOBILE("http://primefaces.org/mobile", "PrimeFaces Mobile", "pm"), //NOI18N

    // JSF 2.2+
    JSF("http://xmlns.jcp.org/jsf", "Jsf", "jsf"), //NOI18N
    PASSTHROUGH("http://xmlns.jcp.org/jsf/passthrough", "Passthrough", "p"); //NOI18N

    private static final DefaultLibraryInfo[] ALL_INFOS = values();

    private String namespace;
    private String displayName;
    private String defaultPrefix;


    private DefaultLibraryInfo(String namespace, String displayName, String defaultPrefix) {
        this.namespace = namespace;
        this.displayName = displayName;
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * Second supported namespace by the library.
     * @return legacy namespace if any or {@code null}
     */
    @Override
    public String getLegacyNamespace() {
        return NamespaceUtils.NS_MAPPING.get(namespace);
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public static LibraryInfo forNamespace(String namespace) {
        for (int i = 0; i < ALL_INFOS.length; i++) {
            LibraryInfo li = ALL_INFOS[i];
            if (li.getNamespace().equals(namespace)
                    || (li.getLegacyNamespace() != null && li.getLegacyNamespace().equals(namespace))) {
                return li;
            }
        }
        return null;
    }


}
