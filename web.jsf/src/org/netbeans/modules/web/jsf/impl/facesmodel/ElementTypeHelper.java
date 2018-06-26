/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.impl.facesmodel;

/**
 * This is helper class for getting correct, validated element types.
 * Element types corresponds with javaee_x.xsd and web-facesconfig_x_x.xsd element types.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class ElementTypeHelper {

    private ElementTypeHelper() {
    }

    /**
     * Picks valid string value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to string element type
     */
    public static String pickString(String string) {
        return (string != null) ? string.trim() : null;
    }

    /**
     * Picks valid fully-qualified-classType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to fully-qualified-classType element type
     */
    public static String pickFullyQualifiedClassType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid java-identifierType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to java-identifierType element type
     */
    public static String pickJavaIdentifierType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid java-typeType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to java-typeType element type
     */
    public static String pickJavaTypeType(String string) {
        return pickString(string);
    }

    /**
     * Picks valid faces-config-value-classType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to faces-config-value-classType element type
     */
    public static String pickFacesConfigValueClassType(String string) {
        return pickFullyQualifiedClassType(string);
    }

    /**
     * Picks valid faces-config-from-view-idType value from the given string.
     * @param string string
     * @return trimmed {@code String} which correspond to faces-config-from-view-idType element type
     */
    public static String pickFacesConfigFromViewIdType(String string) {
        return pickString(string);
    }

}
