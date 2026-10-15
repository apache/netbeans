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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.project.ant.ProjectLibraryProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakListeners;

/**
 * Standard impl of {@link SharabilityQueryImplementation2}.
 * @author Jesse Glick
 */
final class SharabilityQueryImpl implements SharabilityQueryImplementation2, PropertyChangeListener, AntProjectListener {

    private final AntProjectHelper h;
    private final PropertyEvaluator eval;
    private final String[] includes;
    private final String[] excludes;
    /** Absolute paths of directories or files to treat as sharable (except for the excludes). */
    private String[] includePaths;
    /** Absolute paths of directories or files to treat as not sharable. */
    private String[] excludePaths;
    
    SharabilityQueryImpl(AntProjectHelper h, PropertyEvaluator eval, String[] includes, String[] excludes) {
        this.h = h;
        this.eval = eval;
        this.includes = includes;
        this.excludes = excludes;
        computeFiles();
        eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        h.addAntProjectListener(this);
    }
    
    /** Compute the absolute paths which are and are not sharable. */
    private void computeFiles() {
        String[] _includePaths = computeFrom(includes, false);
        String[] _excludePaths = computeFrom(excludes, true);
        synchronized (this) {
            includePaths = _includePaths;
            excludePaths = _excludePaths;
        }
    }
    
    /** Compute a list of absolute paths based on some abstract names. */
    private String[] computeFrom(String[] list, boolean excludeProjectLibraryPrivate) {
        List<String> result = new ArrayList<String>(list.length);
        for (String s : list) {
            String val = eval.evaluate(s);
            if (val != null) {
                File f = h.resolveFile(val);
                result.add(f.getAbsolutePath());
            }
        }
        if (excludeProjectLibraryPrivate) {
            result.addAll(ProjectLibraryProvider.getUnsharablePathsWithinProject(h));
        }
        // XXX should remove overlaps somehow
        return result.toArray(new String[0]);
    }

    @Override public SharabilityQuery.Sharability getSharability(URI uri) {
        // XXX might be more efficient to precompute URIs for includePaths and excludePaths
        String path = BaseUtilities.toFile(uri).getAbsolutePath();
        if (contains(path, excludePaths, false)) {
            return SharabilityQuery.Sharability.NOT_SHARABLE;
        }
        return contains(path, includePaths, false) ?
            (contains(path, excludePaths, true) ? SharabilityQuery.Sharability.MIXED : SharabilityQuery.Sharability.SHARABLE) :
            SharabilityQuery.Sharability.UNKNOWN;
    }
    
    /**
     * Check whether a file path matches something in the supplied list.
     * @param a file path to test
     * @param list a list of file paths
     * @param reverse if true, check if the file is an ancestor of some item; if false,
     *                check if some item is an ancestor of the file
     * @return true if the file matches some item
     */
    private static boolean contains(String path, String[] list, boolean reverse) {
        for (String s : list) {
            if (path.equals(s)) {
                return true;
            } else {
                if (reverse ? s.startsWith(path + File.separatorChar) : path.startsWith(s + File.separatorChar)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        computeFiles();
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        computeFiles();
    }

    public void propertiesChanged(AntProjectEvent ev) {}
    
}
