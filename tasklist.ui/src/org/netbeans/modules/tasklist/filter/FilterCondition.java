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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;


/**
 * A condition (type and relation pair) in the filter.
 * Don't forget to override sameType if you extend this class!
 *
 * @author Tor Norbye
 */
abstract class FilterCondition {

    /**
     * Creates a condition.     
     */
    public FilterCondition() {
    }

    /**
     * Copy constructor. Use from subclassed clone.
     */
    protected FilterCondition(final FilterCondition rhs) {
    }

    /** 
     * Deep clone, please implement in subclass.
     */
    public abstract Object clone() ;
    
    
    /**
     * Compares two objects.
     *
     * @param obj value of the property
     */
    public abstract boolean isTrue(Object obj);

    /**
     * Creates a component that will represent a constant within the 
     * filter dialog. It should support {@link #PROP_VALUE_VALID}
     * client property.
     *
     * @return created component or null if no component 
     */
    public JComponent createConstantComponent() {
        return null;
    }

    /**
     * Gets constant from the specified component and save it.
     * This method should be also implemented if createConstantComponent()
     * is implemented.
     *
     * @param cmp with createConstantComponent() create component
     */
    public void getConstantFrom(JComponent cmp) {
        assert cmp != null : "getConstantFrom() is not implemented!"; //NOI18N
    }

    final boolean isValueValid(JComponent cmp) {
        Boolean valid = (Boolean) cmp.getClientProperty(PROP_VALUE_VALID);
        if (valid == null) {
            return true;
        } else {
            return valid.booleanValue();
        }
    }

    /**
     * Checks whether fc is of the same type.
     * This method will be used to replace a condition created with
     * Filter.getConditionsFor(Node.Property) with one contained in a filter.
     * This method should return true also if this and fc have different 
     * constants for comparing with property values.
     *
     * @param fc another condition
     * @return true fc is of the same type as this
     */
     public boolean sameType(FilterCondition fc) {
       return fc.getClass() == getClass();
     }
    
    public String toString() {
        return getClass().getName() + 
            "[name=" + getDisplayName() + "]"; // NOI18N
    }

    /** Use this client property on value/contant components to indicate valid user data.*/
    public static final String PROP_VALUE_VALID = "value-valid"; //NOI18N


    protected abstract String getDisplayName();
    
    abstract void load( Preferences prefs, String prefix ) throws BackingStoreException;
    
    abstract void save( Preferences prefs, String prefix ) throws BackingStoreException;
}

