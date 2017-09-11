/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
