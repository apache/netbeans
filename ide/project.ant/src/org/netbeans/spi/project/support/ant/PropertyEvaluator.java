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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeListener;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * A way of mapping property names to values.
 * <p>
 * This interface defines no independent thread safety, but in typical usage
 * it will be used with the project manager mutex. Changes should be fired
 * synchronously.
 * @author Jesse Glick
 * @see PropertyUtils#sequentialPropertyEvaluator
 * @see AntProjectHelper#getStandardPropertyEvaluator
 */
public interface PropertyEvaluator {

    /**
     * Evaluate a single property.
     * @param prop the name of a property
     * @return its value, or null if it is not defined or its value could not be
     *         retrieved for some reason (e.g. a circular definition)
     */
    @CheckForNull String getProperty(@NonNull String prop);
    
    /**
     * Evaluate a block of text possibly containing property references.
     * The syntax is the same as for Ant: <em>${foo}</em> means the value
     * of the property <em>foo</em>; <em>$$</em> is an escape for
     * <em>$</em>; references to undefined properties are left unsubstituted.
     * @param text some text possibly containing one or more property references
     * @return its value, or null if some problem (such a circular definition) made
     *         it impossible to retrieve the values of some properties
     */
    @CheckForNull String evaluate(@NonNull String text);
    
    /**
     * Get a set of all current property definitions at once.
     * This may be more efficient than evaluating individual properties,
     * depending on the implementation.
     * @return an immutable map from property names to values, or null if the
     *         mapping could not be computed (e.g. due to a circular definition)
     */
    @CheckForNull Map<String,String> getProperties();
    
    /**
     * Add a listener to changes in particular property values.
     * As generally true with property change listeners, the old and new
     * values may both be null in case the true values are not known or not
     * easily computed; and the property name might be null to signal that any
     * property might have changed.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a listener to changes in particular property values.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
