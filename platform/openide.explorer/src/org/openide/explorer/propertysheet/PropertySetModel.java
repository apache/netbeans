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
/*
 * PropertySetModel.java
 *
 * Created on January 5, 2003, 5:22 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.*;

import java.beans.*;

import java.util.Comparator;


/** Interface for the property set model for property sheets.
 *  PropertySetModel is a model-within-a-model that manages
 *  the available properties and property sets and
 *  the available property sets' expanded state.  Since
 *  both Node.Property and Node.PropertySet extend FeatureDescriptor,
 *  the getters for indexed elements return FeatureDescriptor
 *  objects - clients must test and cast to determine which
 *  they are dealing with.
 *  @author  Tim Boudreau
 */
interface PropertySetModel {
    /** Determines if a given property set is expanded */
    boolean isExpanded(FeatureDescriptor fd);

    /** Set the expanded state for a feature descriptor of the
     *  given index.  */
    void toggleExpanded(int index);

    /** Returns either a Node.Property or a Node.PropertySet
     *  instance for a given index.  */
    FeatureDescriptor getFeatureDescriptor(int index);

    /** Registers a PropertySetModelListener to receive events.
     * @param listener The listener to register. */
    void addPropertySetModelListener(PropertySetModelListener listener);

    /** Removes a PropertySetModelListener from the list of listeners.
     * @param listener The listener to remove. */
    void removePropertySetModelListener(PropertySetModelListener listener);

    /** Assign the property sets this model will manage */
    void setPropertySets(PropertySet[] sets);

    /** Utility method to determine if a given index holds a property
     *  or property set.*/
    boolean isProperty(int index);

    /** Get the number of feature descriptors (properties and
     *  property sets) currently represented by the model, not
     *  including properties belonging to unexpanded property
     *  sets - in other words, the current number of objects
     *  a component rendering this model is being asked to display. */
    int getCount();

    /** Get the index, in the model, of a given feature descriptor.
     *  If it is not currently available (either not part of the model
     *  at all, or part of an unexpanded property set), returns -1. */
    int indexOf(FeatureDescriptor fd);

    /** Set the comparator the model will use for sorting properties */
    void setComparator(Comparator<Property> c);

    int getSetCount();

    Comparator getComparator();
}
