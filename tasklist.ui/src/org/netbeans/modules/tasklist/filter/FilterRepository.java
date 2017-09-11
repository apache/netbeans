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

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Set of filters
 */
public final class FilterRepository {
    
    private static FilterRepository theInstance;
    
    /** the set of filters of this repository **/
    private LinkedList<TaskFilter> filters = new LinkedList<TaskFilter>();
    private int active = -1;   // index of the active filter
    
    /**
     * Constructor, default.
     */
    FilterRepository() {
    }
    
    public static FilterRepository getDefault() {
        if( null == theInstance ) {
            theInstance = new FilterRepository();
        }
        return theInstance;
    }
    
    public void assign( final FilterRepository fr ) {
        if( fr != this ) {
            filters.clear();
            Iterator<TaskFilter> it = fr.filters.iterator();
            while( it.hasNext() ) {
                filters.add( (TaskFilter)it.next().clone() );
            }
            
            active = fr.active;
        }
    }
    
    @Override
    public Object clone() {
        FilterRepository ret = new FilterRepository();
        ret.assign(this);
        return ret;
    }
    
    public List<TaskFilter> getAllFilters() {
        return new ArrayList<TaskFilter>( filters );
    }
    
    
    // Implementation of java.util.Set
    
    /**
     * Adds a new filter to the collection, if it was not present
     * already.
     * @param f the Filter to be added
     * @return true iff it was not member before and was added
     */
    void add( TaskFilter f ) {
        filters.add( f );
    }
    
    /**
     * Remove the filter specified by parameter from the collection.
     *
     * @param filter the Filter to remove
     * @return true iff the filter was found and removed
     */
    void remove( TaskFilter f ) {
        if( f == getActive() ) 
            setActive( null );
        filters.remove( f );
    }
    
    Iterator<TaskFilter> iterator() {
        return filters.iterator();
    }
    
    int size() { 
        return filters.size();
    }
    
    /**
     * Returns a filter with the given name or null if not found.
     * @param name name of the filter to look up
     * @return Filter with name or null
     */
    TaskFilter getFilterByName( String name ) {
        Iterator<TaskFilter> it = filters.iterator();
        while (it.hasNext()) {
            TaskFilter f = it.next();
            if( f.getName().equals( name ) ) 
                return f;
        }
        return null;
    }
    
    public TaskFilter getActive() {
        return (active == -1 || filters.isEmpty()) ? null : filters.get( active );
    }
    
    public void setActive( TaskFilter newactive ) {
        if( newactive == null ) {
            this.active = -1;
        } else {
            int i = filters.indexOf( newactive );
            if( i != -1 ) {
                this.active = i;
            }
        }
    }
    
    public void load() throws IOException {
        filters.clear();
        active = -1;
        Preferences prefs = NbPreferences.forModule( FilterRepository.class );
        prefs = prefs.node( "Filters" ); //NOI18N
        active = prefs.getInt( "active", -1 );
        
        int count = prefs.getInt( "count", 0 ); //NOI18N
        for( int i=0; i<count; i++ ) {
            TaskFilter filter = new TaskFilter();
            try {
                filter.load( prefs, "Filter_" + i ); //NOI18N
            } catch( BackingStoreException bsE ) {
                IOException ioE = new IOException( "Cannot load filter repository" ); //NOI18N
                ioE.initCause( bsE );
                throw ioE;
            }
            filters.add( filter );
        }
        boolean shouldSave = false;
        //TODO remove this filter after dahboard is introduced
        if( prefs.getBoolean( "firstTimeStartWithIssue", true ) ) { //NOI18N
            prefs.putBoolean( "firstTimeStartWithIssue", false ); //NOI18N
            TaskFilter filter = createNewFilter();
            filter.setName( NbBundle.getMessage( FilterRepository.class, "LBL_TodoFilter" ) ); //NOI18N
            TypesFilter types = new TypesFilter();
            types.clear();
            types.setEnabled("org.netbeans.modules.tasklist.todo.TodoTaskScanner", true); //NOI18N
            types.setTaskCountLimit( 100 );
            filter.setTypesFilter( types );
            filter.setKeywordsFilter( new KeywordsFilter() );
            filters.add( filter );
            shouldSave = true;
        }
        if( shouldSave )
            save();
    }
    
    public void save() throws IOException {
        try {
            Preferences prefs = NbPreferences.forModule( FilterRepository.class );
            prefs = prefs.node( "Filters" ); //NOI18N
            prefs.clear();
            prefs.putBoolean( "firstTimeStart", false ); //NOI18N
            prefs.putBoolean( "firstTimeStartWithIssue", false ); //NOI18N

            prefs.putInt( "count", filters.size() ); //NOI18N
            prefs.putInt( "active", active ); //NOI18N
            for( int i=0; i<filters.size(); i++ ) {
                TaskFilter filter = filters.get( i );
                filter.save( prefs, "Filter_" + i ); //NOI18N
            }
        } catch( BackingStoreException bsE ) {
            IOException ioE = new IOException( "Cannot save filter repository" ); //NOI18N
            ioE.initCause( bsE );
            throw ioE;
        }
    }
    
    TaskFilter createNewFilter() {
        return new TaskFilter( NbBundle.getMessage( FilterRepository.class, "LBL_NewFilter" ) ); //NOI18N
    }
    
    void clear() {
        filters.clear();
        setActive( null );
    }
    
    List<TaskFilter> getFilters() {
        return new ArrayList<TaskFilter>( filters );
    }
}

