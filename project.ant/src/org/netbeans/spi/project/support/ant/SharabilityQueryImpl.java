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
        return result.toArray(new String[result.size()]);
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
