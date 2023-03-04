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

package org.netbeans.modules.j2ee.sun.validation.constraints;

import java.util.Collection;

/**
 * Constraint is an <code>Interface</code> needed to perform a Validation.
 * User can  define a new type of Validation by implementing this
 * <code>Interface</code>.
 * Method <code>match</code> defines the Validation logic. This method
 * returns an empty collection if the value being validated is valid;
 * else it returns a <code>Collection</code> with a {@link ConstraintFailure}
 * object in it.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public interface Constraint {
    /**
     * Validates the given value against this <code>Constraint</code>.
     * 
     * @param value the value to be validated
     * @param name the element name, value of which is being validated.
     *
     * @return <code>Collection</code> the Collection of failure Objects.
     * Collection is empty if there are no failures.
     */
    public Collection match(String value, String name);
}
