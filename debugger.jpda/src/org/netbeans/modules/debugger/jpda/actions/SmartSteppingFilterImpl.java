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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.WeakListeners;


/**
 *
 * @author  Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession", types={SmartSteppingFilter.class})
public class SmartSteppingFilterImpl implements SmartSteppingFilter {

    private final HashSet<String> filter = new HashSet<String>();
    private final ArrayList<String> exact = new ArrayList<String>();
    private final ArrayList<String> start = new ArrayList<String>();
    private final ArrayList<String> end = new ArrayList<String>();
    private final PropertyChangeSupport pcs;
    {pcs = new PropertyChangeSupport (this);}
    private final Properties options = Properties.getDefault().
            getProperties("debugger.options.JPDA");
    private final Properties classFiltersProperties = Properties.getDefault().
            getProperties("debugger").getProperties("sources").
            getProperties("class_filters");
    private final PropertyChangeListener exclusionPatternsListener;

    {
        exclusionPatternsListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                setExclusionPatterns ();
            }
        };
        options.addPropertyChangeListener(WeakListeners.propertyChange(
                exclusionPatternsListener, options));
        classFiltersProperties.addPropertyChangeListener(WeakListeners.propertyChange(
                exclusionPatternsListener, classFiltersProperties));
        setExclusionPatterns ();
    }

    private void setExclusionPatterns () {
        Set<String> patterns;
        if (options.getBoolean("UseStepFilters", true)) {
            patterns = (Set<String>) classFiltersProperties.getCollection (
                    "enabled",
                    Collections.EMPTY_SET);
        } else {
            patterns = Collections.EMPTY_SET;
        }
        synchronized (filter) {
            filter.clear();
            exact.clear();
            start.clear();
            end.clear();
            filter.addAll (patterns);
            refreshFilters (patterns);
        }
        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, null, patterns);
    }
    
    
    /**
     * Adds a set of new class exclusion filters. Filter is 
     * {@link java.lang.String} containing full class name. Filter can 
     * begin or end with '*' to define more than one class, for example 
     * "*.Ted", or "examples.texteditor.*".
     *
     * @param patterns a set of class exclusion filters to be added
     */
    @Override
    public void addExclusionPatterns (Set<String> patterns) {
        Set<String> reallyNew = new HashSet<String>(patterns);
        reallyNew.removeAll (filter);
        if (reallyNew.size () < 1) return;

        synchronized (filter) {
            filter.addAll (reallyNew);
            refreshFilters (reallyNew);
        }

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, null, reallyNew);
    }

    /**
     * Removes given set of class exclusion filters from filter.
     *
     * @param patterns a set of class exclusion filters to be added
     */
    @Override
    public void removeExclusionPatterns (Set<String> patterns) {
        synchronized (filter) {
            filter.removeAll (patterns);
            exact.clear();
            start.clear();
            end.clear();
            refreshFilters (filter);
        }

        pcs.firePropertyChange (PROP_EXCLUSION_PATTERNS, patterns, null);
    }
    
    /**
     * Returns list of all exclusion patterns.
     */
    @Override
    public String[] getExclusionPatterns () {
        synchronized (filter) {
            String[] ef = new String [filter.size ()];
            return filter.toArray (ef);
        }
    }

    public boolean stopHere (String className) {
        synchronized (filter) {
            int i, k = exact.size ();
            for (i = 0; i < k; i++) {
                if (exact.get (i).equals (className)) return false;
            }
            k = start.size ();
            for (i = 0; i < k; i++) {
                if (className.startsWith (start.get (i))) return false;
            }
            k = end.size ();
            for (i = 0; i < k; i++) {
                if (className.endsWith (end.get (i))) return false;
            }
        }
        return true;
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    @Override
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }

    /**
     * Updates exact, start and end filter lists.
     */
    private void refreshFilters (Set<String> newFilters) {
        Iterator<String> i = newFilters.iterator ();
        while (i.hasNext ()) {
            String p = i.next ();
            if (p.startsWith ("*"))
                end.add (p.substring (1));
            else
            if (p.endsWith ("*"))
                start.add (p.substring (0, p.length () - 1));
            else
                exact.add (p);
        }
    }
}
