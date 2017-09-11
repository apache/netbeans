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
     * The syntax is the same as for Ant: <samp>${foo}</samp> means the value
     * of the property <samp>foo</samp>; <samp>$$</samp> is an escape for
     * <samp>$</samp>; references to undefined properties are left unsubstituted.
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
