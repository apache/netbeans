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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.api;

import org.openide.util.Parameters;

/**
 * Represents the environment of the Grails.
 * <p>
 * This class is <i>Immutable</i>.
 *
 * @author Petr Hejl
 */
public final class GrailsEnvironment {

    /** Development environment. */
    public static final GrailsEnvironment DEV = new GrailsEnvironment("dev"); // NOI18N

    /** Production environment. */
    public static final GrailsEnvironment PROD = new GrailsEnvironment("prod"); // NOI18N

    /** Test environment. */
    public static final GrailsEnvironment TEST = new GrailsEnvironment("test"); // NOI18N

    private final String value;

    private GrailsEnvironment(String value) {
        this.value = value;
    }

    /**
     * Parses the string value and returns the proper environment object.
     * If the value is well known environment (dev, prod or test) returns
     * the corresponding constant (DEV, PROD or TEST).
     *
     * @param value string representation to parse
     * @return environment object
     */
    public static GrailsEnvironment valueOf(String value) {
        Parameters.notNull("value", value);

        if (DEV.toString().equals(value)) {
            return DEV;
        } else if (PROD.toString().equals(value)) {
            return PROD;
        } else if (TEST.toString().equals(value)) {
            return TEST;
        } else {
            return new GrailsEnvironment(value);
        }
    }

    /**
     * Returns the well known environment values.
     *
     * @return the well known environment values
     */
    public static GrailsEnvironment[] standardValues() {
        return new GrailsEnvironment[] {DEV, PROD, TEST};
    }

    /**
     * Returns <code>true</code> if this environment is well known (DEV, PROD or TEST).
     *
     * @return <code>true</code> if this environment is well known (DEV, PROD or TEST)
     */
    public boolean isCustom() {
        return this != DEV && this != PROD && this != TEST;
    }

    /**
     * Returns the string representation usable in grails.
     *
     * @return the string representation usable in grails
     */
    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GrailsEnvironment other = (GrailsEnvironment) obj;
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

}
