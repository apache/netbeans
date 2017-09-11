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

package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
//@ThreadSafe
final class BinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {        

    private static final String PROP_GENERATED_SOURCES = "build.generated.sources.dir";  //NOI18N
    
    private final Map<URL,BinaryForSourceQuery.Result>  cache =
            new ConcurrentHashMap<URL, Result>();
    private final SourceRoots src;
    private final SourceRoots test;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    private String[] sourceProps;
    private String[] testProps;
    
    /** Creates a new instance of BinaryForSourceQueryImpl */
    BinaryForSourceQueryImpl(
            @NonNull final SourceRoots src,
            @NonNull final SourceRoots test,
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String[] sourceProps,
            @NonNull final String[] testProps) {
        assert src != null;
        assert test != null;
        assert helper != null;
        assert eval != null;        
        assert sourceProps != null && sourceProps.length > 0;
        assert testProps != null && testProps.length > 0;
        this.src = src;
        this.test = test;
        this.eval = eval;
        this.helper = helper;
        this.sourceProps = sourceProps;
        this.testProps = testProps;
    }
    
    @Override
    @CheckForNull
    public Result findBinaryRoots(@NonNull final URL sourceRoot) {
        assert sourceRoot != null;
        BinaryForSourceQuery.Result result = cache.get(sourceRoot);
        if (result == null) {
            final String[] activeNames = getActivePropertyNames(sourceRoot);
            if (activeNames != null) {
                result = new R (sourceRoot, activeNames);
                cache.put (sourceRoot,result);
            }
        }
        return result;
    }

    @CheckForNull
    private String[] getActivePropertyNames(@NonNull final URL sourceRoot) {
        for (URL root : this.src.getRootURLs()) {
            if (root.equals(sourceRoot)) {
                return sourceProps;
            }
        }
        for (URL root : this.test.getRootURLs()) {
            if (root.equals(sourceRoot)) {
                return testProps;
            }
        }
        final String buildGeneratedDirS = eval.getProperty(PROP_GENERATED_SOURCES);
        if (buildGeneratedDirS != null) { // #105645
            final String parent = Utilities.toURI(helper.resolveFile(buildGeneratedDirS)).toString();
            if (sourceRoot.toString().startsWith(parent)) {
                return sourceProps;
            }
        }
        return null;
    }
    
    class R implements BinaryForSourceQuery.Result, PropertyChangeListener {
                
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final URL root;
        private volatile String[] activeNames;
        
        R ( @NonNull final URL root,
            @NonNull final String[] activeNames) {
            assert root != null;
            assert activeNames != null && activeNames.length > 0;
            this.root = root;
            this.activeNames = activeNames;
            eval.addPropertyChangeListener(this);
            src.addPropertyChangeListener(this);
            test.addPropertyChangeListener(this);
        }
        
        @Override
        @NonNull
        public URL[] getRoots() {
            String[] names = activeNames;
            if (names == null) {
                names = getActivePropertyNames(root);
                activeNames = names;
            }
            if (names == null) {
                //No more handled by this project, remove from cache.
                cache.remove(root);
                return new URL[0];
            }
            final List<URL> urls = new ArrayList<URL>();
            for (String propName : names) {
                String val = eval.getProperty(propName);
                if (val != null) {
                    final URL url = FileUtil.urlForArchiveOrDir(helper.resolveFile(val));
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }
            return urls.toArray(new URL[urls.size()]);
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            assert l != null;
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            assert l != null;
            changeSupport.removeChangeListener(l);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent event) {
            final String propName = event.getPropertyName();
            if (SourceRoots.PROP_ROOTS.equals(propName) ||
                PROP_GENERATED_SOURCES.equals(propName)) {
                activeNames = null;
            }
            changeSupport.fireChange();
        }
}

}
