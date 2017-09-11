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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.java.project.support;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.project.classpath.ClassPathModifierLookupMerger;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2.Result;
import org.netbeans.spi.java.queries.support.SourceForBinaryQueryImpl2Base;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex.Action;


/**
 * Factory class for creation of {@link org.netbeans.spi.project.LookupMerger} instances.
 * @author mkleint
 * @since org.netbeans.modules.java.project 1.14
 */
public final class LookupMergerSupport {

    /**
     * Create a simple instance of LookupMerger for SourceForBinaryQueryImplementation. It takes
     * all implemntations it finds inthe provided lookup and iterates them until a result
     * is found.
     * @return
     */
    public static LookupMerger<SourceForBinaryQueryImplementation> createSFBLookupMerger() {
        return new SFBLookupMerger();
    }

    /**
     * Create a simple instance of LookupMerger for JavadocForBinaryQueryImplementation. It takes
     * all implemntations it finds inthe provided lookup and iterates them until a result
     * is found.
     * @return
     */
    public static LookupMerger<JavadocForBinaryQueryImplementation> createJFBLookupMerger() {
        return new JFBLookupMerger();
    }
    
    /**
     * Creates a LookupMerger for ClassPathProviders, allowing multiple instances of ClassPathProviders to reside
     * in project's lookup. The merger makes sure the classpaths are merged together. 
     * When ClassPathProviders appear or disappear in project's lookup, the classpath is updated accordingly.
     * @param defaultProvider the default project ClassPathProvider that will always be asked first for classpath.
     * @return LookupMerger instance to be put in project's lookup.
     * @since org.netbeans.modules.java.project 1.18
     * @see LookupMerger
     */
    public static LookupMerger<ClassPathProvider> createClassPathProviderMerger(ClassPathProvider defaultProvider) {
        return new ClassPathProviderMerger(defaultProvider);
    }    

    /**
     * Creates a merger of class path modifiers.
     * All supported source groups and classpath types are unified.
     * The first modifier implementation to return true (or throw {@link IOException}) is accepted.
     * False is returned if all of the implementations do so.
     * {@link UnsupportedOperationException} is thrown only if all of the implementations do so.
     * @return a merger
     * @since 1.41
     */
    public static LookupMerger<ProjectClassPathModifierImplementation> createClassPathModifierMerger() {
        return new ClassPathModifierLookupMerger();
    }

    /**
     * Creates a merger of {@link CompilerOptionsQueryImplementation}.
     * It takes all {@link CompilerOptionsQueryImplementation} implementations
     * in the provided lookup and merges their results into a single one.
     * @return the {@link CompilerOptionsQueryImplementation} merger
     * @since 1.68
     */
    @NonNull
    public static LookupMerger<CompilerOptionsQueryImplementation> createCompilerOptionsQueryMerger() {
        return new CompilerOptionsQueryMerger();
    }

    private static class SFBLookupMerger implements LookupMerger<SourceForBinaryQueryImplementation> {

        public Class<SourceForBinaryQueryImplementation> getMergeableClass() {
            return SourceForBinaryQueryImplementation.class;
        }

        public SourceForBinaryQueryImplementation merge(Lookup lookup) {
            return new SFBIMerged(lookup);
        }
        
    }
    
    private static class SFBIMerged extends SourceForBinaryQueryImpl2Base {
        private Lookup lookup;
        private Map<URL, Result> url2Result = new HashMap<URL, Result>();
        
        public SFBIMerged(Lookup lkp) {
            lookup = lkp;
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            return findSourceRoots2(binaryRoot);
        }

        public Result findSourceRoots2(final URL binaryRoot) {
            return ProjectManager.mutex().readAccess(new Action<Result>() {
                public Result run() {
                    return findSourceRoots2Impl(binaryRoot);
                }
            });
        }
        
        private synchronized  Result findSourceRoots2Impl(URL binaryRoot) {
            Result result = url2Result.get(binaryRoot);
            
            if (result != null) {
                return result;
            }
            
            Collection<? extends SourceForBinaryQueryImplementation> col = lookup.lookupAll(SourceForBinaryQueryImplementation.class);
            List<Result> queryResults = new LinkedList<Result>();
            for (SourceForBinaryQueryImplementation impl : col) {
                if (impl instanceof SourceForBinaryQueryImplementation2) {
                    SourceForBinaryQueryImplementation2.Result res = ((SourceForBinaryQueryImplementation2)impl).findSourceRoots2(binaryRoot);
                    if (res != null) {
                        queryResults.add(res);
                    }
                }
                else {
                    SourceForBinaryQuery.Result res = impl.findSourceRoots(binaryRoot);
                    if (res != null) {
                        queryResults.add(asResult(res));
                    }
                }
            }

            if (queryResults.isEmpty()) {
                return null;
            }

            url2Result.put(binaryRoot, result = new ResultImpl(queryResults));
            
            return result;
        }
        
        private static final class ResultImpl implements Result, ChangeListener {

            private final List<Result> delegateTo;
            private final ChangeSupport cs = new ChangeSupport(this);

            public ResultImpl(List<Result> delegateTo) {
                this.delegateTo = delegateTo;
                
                for (Result r : delegateTo) {
                    r.addChangeListener(this);
                }
            }
            
            public boolean preferSources() {
                for (Result r : delegateTo) {
                    if (r.preferSources())
                        return true;
                }
                
                return false;
            }

            public FileObject[] getRoots() {
                List<FileObject> result = new LinkedList<FileObject>();
                
                for (Result r : delegateTo) {
                    result.addAll(Arrays.asList(r.getRoots()));
                }
                
                return result.toArray(new FileObject[result.size()]);
            }

            public void addChangeListener(ChangeListener l) {
                cs.removeChangeListener(l);
                cs.addChangeListener(l);
            }

            public void removeChangeListener(ChangeListener l) {
                cs.removeChangeListener(l);
            }

            public void stateChanged(ChangeEvent e) {
                cs.fireChange();
            }
            
        }
        
    }
    
    private static class JFBLookupMerger implements LookupMerger<JavadocForBinaryQueryImplementation> {

        public Class<JavadocForBinaryQueryImplementation> getMergeableClass() {
            return JavadocForBinaryQueryImplementation.class;
        }

        public JavadocForBinaryQueryImplementation merge(Lookup lookup) {
            return new JFBIMerged(lookup);
        }
        
    }
    
    private static class JFBIMerged implements JavadocForBinaryQueryImplementation {
        private Lookup lookup;
        
        public JFBIMerged(Lookup lkp) {
            lookup = lkp;
        }
        
        public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
            Collection<? extends JavadocForBinaryQueryImplementation> col = lookup.lookupAll(JavadocForBinaryQueryImplementation.class);
            for (JavadocForBinaryQueryImplementation impl : col) {
                JavadocForBinaryQuery.Result res = impl.findJavadoc(binaryRoot);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }
        
    }
    
}
