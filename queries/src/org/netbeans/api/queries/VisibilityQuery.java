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

package org.netbeans.api.queries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;

/**
 * Determine whether files should be hidden in views presented to the user.
 * <p>
 * This query should be considered only as a recommendation. Particular views
 * may decide to display all files and ignore this query.
 * </p>
 * @see org.netbeans.spi.queries.VisibilityQueryImplementation
 * @author Radek Matous
 */
public final class VisibilityQuery {
    private static final VisibilityQuery INSTANCE = new VisibilityQuery();

    private final ResultListener resultListener = new ResultListener();
    private final VqiChangedListener vqiListener = new VqiChangedListener ();

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private Lookup.Result<VisibilityQueryImplementation> vqiResult = null;
    private List<VisibilityQueryImplementation> cachedVqiInstances = null;

    /**
     * Get default instance of VisibilityQuery.
     * @return instance of VisibilityQuery
     */
    public static final VisibilityQuery getDefault() {
        return INSTANCE;
    }

    private VisibilityQuery() {
    }

    /**
     * Check whether a file is recommended to be visible.
     * Default return value is visible unless at least one VisibilityQueryImplementation
     * provider says hidden.
     * @param file a file which should be checked
     * @return true if it is recommended to show this file
     */
    public boolean isVisible(FileObject file) {
        Parameters.notNull("file", file);
        for (VisibilityQueryImplementation vqi : getVqiInstances()) {
            if (!vqi.isVisible(file)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check whether a file is recommended to be visible.
     * Default return value is visible unless at least one VisibilityQueryImplementation
     * provider says hidden.
     * @param file a file which should be checked
     * @return true if it is recommended to show this file
     * @since org.netbeans.modules.queries/1 1.12
     */
    public boolean isVisible(File file) {
        Parameters.notNull("file", file);
        for (VisibilityQueryImplementation vqi : getVqiInstances()) {
            if (vqi instanceof VisibilityQueryImplementation2) {
                if (!((VisibilityQueryImplementation2)vqi).isVisible(file)) {
                    return false;
                }
            } else {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    if (!vqi.isVisible(fo)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * Stop listening to changes.
     * @param l a listener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange(ChangeEvent event) {
        assert event != null;
        ArrayList<ChangeListener> lists;
        synchronized (listeners) {
            lists = new ArrayList<ChangeListener>(listeners);
        }
        for (ChangeListener listener : lists) {
            try {
                listener.stateChanged(event);
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

    private synchronized List<VisibilityQueryImplementation> getVqiInstances() {
        if (cachedVqiInstances == null) {
            vqiResult = Lookup.getDefault().lookupResult(VisibilityQueryImplementation.class);
            vqiResult.addLookupListener(resultListener);
            setupChangeListeners(null, new ArrayList<VisibilityQueryImplementation>(vqiResult.allInstances()));
        }
        return cachedVqiInstances;
    }

    private synchronized void setupChangeListeners(final List<VisibilityQueryImplementation> oldVqiInstances, final List<VisibilityQueryImplementation> newVqiInstances) {
        if (oldVqiInstances != null) {
            Set<VisibilityQueryImplementation> removed = new HashSet<VisibilityQueryImplementation>(oldVqiInstances);
            removed.removeAll(newVqiInstances);
            for (VisibilityQueryImplementation vqi : removed) {
                vqi.removeChangeListener(vqiListener);
            }
        }

        Set<VisibilityQueryImplementation> added = new HashSet<VisibilityQueryImplementation>(newVqiInstances);
        if (oldVqiInstances != null) {
            added.removeAll(oldVqiInstances);
        }
        for (VisibilityQueryImplementation vqi : added) {
            vqi.addChangeListener(vqiListener);
        }

        cachedVqiInstances = newVqiInstances;
    }

    private class ResultListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            setupChangeListeners(cachedVqiInstances, new ArrayList<VisibilityQueryImplementation>(vqiResult.allInstances()));
            fireChange(new ChangeEvent(this));
        }
    }

    private class VqiChangedListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireChange(e);
        }
    }

}
