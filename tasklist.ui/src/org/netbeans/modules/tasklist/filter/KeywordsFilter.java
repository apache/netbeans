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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.spi.tasklist.Task;
import org.openide.util.NbBundle;


/**
 * This class implements a filter for a tasklist
 *
 * @author Tor Norbye
 */
class KeywordsFilter {
    /** If true, all conditions in the filter must be true.
     *  If false, any condition in the filter can be true to
     *  make the node pass.
     */
    private boolean allTrue = false;
    
    /** List of conditions to evaluate the task with */
    private List<AppliedFilterCondition> appliedConditions = null;
    
    private static final String NO_FILTER = NbBundle.getMessage( KeywordsFilter.class, "no-filter" ); //NOI18N
    
    private static final TaskProperty[] PROPS = new TaskProperty[] {
        TaskProperties.PROP_DESCRIPTION,
        TaskProperties.PROP_GROUP,
        TaskProperties.PROP_FILE,
        TaskProperties.PROP_LOCATION
    };
    
    /**
     * Creates an empty filter
     *
     * @param name name of the filter
     */
    public KeywordsFilter() {
        this(true, new ArrayList<AppliedFilterCondition>());
    }
    
    /**
     * Create a new filter.
     *
     * @param name (User visible) name of the filter
     * @param allTrue When true, all conditions must be true, when false, any
     *  condition can be true, to make a task pass through the filter.
     * @param conditions List of AppliedFilterCondition objects to use when filtering a task.
     */
    public KeywordsFilter(boolean allTrue, List<AppliedFilterCondition> conditions) {
        this.allTrue = allTrue;
        this.appliedConditions = conditions;
    }
    
    /**
     * Copy constructor.
     */
    protected KeywordsFilter(final KeywordsFilter rhs) {
        this(rhs.allTrue, cloneConditions(rhs.appliedConditions));
    }
    
    
    private static List<AppliedFilterCondition> cloneConditions(List<AppliedFilterCondition> conditions) {
        LinkedList<AppliedFilterCondition> l = new LinkedList<AppliedFilterCondition>();
        Iterator<AppliedFilterCondition> it = conditions.iterator();
        while (it.hasNext()) {
            l.add( (AppliedFilterCondition)it.next().clone() );
        }
        
        return l;
    }
    
    public Object clone() {
        return new KeywordsFilter( this );
    }
    
    /**
     * Creates filter conditions (options) for the specified property
     * applied to the given property.
     *
     * @param property the property to get options for
     */
    public AppliedFilterCondition[] createConditions(TaskProperty property) {
        if( property.equals( TaskProperties.PROP_GROUP ) ) {
            return applyConditions(property, TaskGroupCondition.createConditions() );
        } else if( property.equals( TaskProperties.PROP_DESCRIPTION ) ) {
            return applyConditions(property, StringFilterCondition.createConditions() );
        } else if( property.equals( TaskProperties.PROP_FILE ) ) {
            return applyConditions(property, StringFilterCondition.createConditions() );
        } else if( property.equals( TaskProperties.PROP_LOCATION ) ) {
            return applyConditions(property, StringFilterCondition.createConditions() );
        }
	
        throw new IllegalArgumentException("wrong property"); //NOI18N
    }
    
    /**
     * Returns properties used for filtering by this filter.
     * <p>
     * Versioning consideration: you may not remove
     * any value to retain backward compatability.
     *
     * @return properties for searching
     */
    public TaskProperty[] getProperties() {
        return PROPS;
    }
    
    
    /**
     * Removes all conditions from this filter
     */
    public void clear() {
        getConditions().clear();
    }
    
    /**
     * Return true iff the filter lets the task through
     *
     * @param node object to be filtered
     */
    public boolean accept( Task t ) {
        if (!hasConstraints()) {
            return true; // No need to create iterator object...
        }
        Iterator it = appliedConditions.iterator();
        boolean b = true;
        while (it.hasNext()) {
            AppliedFilterCondition acond = (AppliedFilterCondition)it.next();
            b = acond.isTrue( t );
            if (b && !allTrue) {
                return true;
            } else if (!b && allTrue) {
                return false;
            }
        }
        return b;
    }
    
    /**
     * Return true iff all conditions should be matched.
     *
     * @return true iff all conditions should be matched.
     */
    public boolean matchAll() {
        return allTrue;
    }
    
    /**
     * Return true iff the filter is not "empty" (meaning that
     * there are constraints on the view)
     */
    public boolean hasConstraints() {
        return (appliedConditions != null) && (appliedConditions.size() > 0);
    }
    
    /**
     * Should all conditions match?
     *
     * @param b true = all conditions should be matched, false = any
     */
    public void setMatchAll(boolean b) {
        if (this.allTrue != b) {
            this.allTrue = b;
        }
    }
    
    /**
     * Return the list of conditions actually used for this filter.
     *
     * @return the list of AppliedFilterConditions for this filter.
     */
    public final List getConditions() {
        return appliedConditions;
    }
    
    /**
     * Sets new applied conditions used with this filter.
     *
     * @param new List[AppliedFilterCondition]
     */
    public final void setConditions(List<AppliedFilterCondition> conditions) {
        this.appliedConditions = conditions;
    }
    
    /**
     * Print out the filter for debugging purposes.
     * Do NOT depend on its format or contents, it may change arbitrarily.
     * It is not localized.
     *
     * @return string representation of this object
     */
    public String toString() {
        Iterator it = appliedConditions.iterator();
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName() + "["); // NOI18N
        sb.append(allTrue ?
            "ALL of the following conditions" : // NOI18N
            "ANY of the following conditions"); // NOI18N
        sb.append(", "); // NOI18N
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append(", "); // NOI18N
        }
        sb.append("]"); // NOI18N
        return sb.toString();
    }
    
    
    /**
     * Lift of AppliedFilterCondition(property, - ) to list of
     * FilterConditions.
     *
     * @param property SuggestionProperty to apply to every
     *                 FilterCondition in the list
     * @param conds    list of FilterConditions
     * @return list of AppliedFilterConditions , each applied to
     *         property and the corresponding element of conds.
     */
    protected static AppliedFilterCondition [] applyConditions(TaskProperty property, FilterCondition [] conds) {
        if (conds == null) return null;
        else {
            AppliedFilterCondition [] applied = new AppliedFilterCondition[conds.length];
            for (int i = 0; i < conds.length; i++) {
                applied[i] = new AppliedFilterCondition(property, conds[i]);
            }
            return applied;
        }
    }
    
    static FilterCondition createCondition( TaskProperty property ) {
        if( property.getID().equals( TaskProperties.PROP_GROUP.getID() ) ) {  
            return new TaskGroupCondition();
        }
        return new StringFilterCondition();
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        allTrue = prefs.getBoolean( prefix+"_allTrue", false ); //NOI18N
        appliedConditions.clear();
        
        int count = prefs.getInt( prefix+"_count", 0 ); //NOI18N
        for( int i=0; i<count; i++ ) {
            AppliedFilterCondition cond = new AppliedFilterCondition();
            cond.load( prefs, prefix+"_condition_" + i  ); //NOI18N
            appliedConditions.add( cond );
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putBoolean( prefix+"_allTrue", allTrue ); //NOI18N
        prefs.putInt( prefix+"_count", appliedConditions.size() ); //NOI18N
        for( int i=0; i<appliedConditions.size(); i++ ) {
            appliedConditions.get( i ).save( prefs, prefix+"_condition_" + i ); //NOI18N
        }
    }
}
