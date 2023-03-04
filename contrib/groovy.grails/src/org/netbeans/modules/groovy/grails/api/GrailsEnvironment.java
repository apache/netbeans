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
