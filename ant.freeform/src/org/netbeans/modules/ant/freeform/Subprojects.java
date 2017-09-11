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

package org.netbeans.modules.ant.freeform;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Manages list of subprojects.
 * @author Jesse Glick
 * @see "#46713"
 */
final class Subprojects implements SubprojectProvider {
    
    private final FreeformProject project;
    
    public Subprojects(FreeformProject project) {
        this.project = project;
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        return new LazySubprojectsSet();
    }
    
    /**Analyzes subprojects element.
     * Works in two modes:
     * 1. Lazy mode, detects only if the set of subprojects would be empty.
     *    Enabled if the provided subprojects parameter is null.
     *    The subprojects set is empty if createSubprojects(null) == null.
     *    Used by the LazySubprojectsSet, see #58639. Works as fast as possible.
     * 2. Full mode, creates the set of projects.
     *    Enabled if the subprojects parameter is not-null.
     *    The provided instance of Set is filled by the projects and returned.
     *
     * This method never allocates a new set.
     */
    private Set<Project> createSubprojects(Set<Project> subprojects) {
        Element config = project.getPrimaryConfigurationData();
        Element subprjsEl = XMLUtil.findElement(config, "subprojects", FreeformProjectType.NS_GENERAL); // NOI18N
        if (subprjsEl != null) {
            for (Element prjEl : XMLUtil.findSubElements(subprjsEl)) {
                assert prjEl.getLocalName().equals("project") : "Bad element " + prjEl + " in <subprojects> for " + project;
                String rawtext = XMLUtil.findText(prjEl);
                assert rawtext != null : "Need text content for <project> in " + project;
                String evaltext = project.evaluator().evaluate(rawtext);
                if (evaltext == null) {
                    continue;
                }
                FileObject subprjDir = project.helper().resolveFileObject(evaltext);
                if (subprjDir == null) {
                    continue;
                }
                try {
                    Project p = ProjectManager.getDefault().findProject(subprjDir);
                    if (p != null) {
                        if (subprojects == null) {
                            return Collections.emptySet();
                        }
                        else {
                            subprojects.add(p);
                        }
                    }
                } catch (IOException e) {
                    org.netbeans.modules.ant.freeform.Util.err.notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }

        return subprojects;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        // XXX
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
    /**Fix for #58639: the subprojects should be loaded lazily, so invoking the popup menu
     * with "Open Required Projects" is fast.
     */
    private final class LazySubprojectsSet implements Set<Project> {
        
        private Set<Project> delegateTo = null;
        
        private synchronized Set<Project> getDelegateTo() {
            if (delegateTo == null) {
                delegateTo = createSubprojects(new HashSet<Project>());
            }
            
            return delegateTo;
        }
        
        @Override
        public boolean contains(Object o) {
            return getDelegateTo().contains(o);
        }
        
        @Override
        public boolean add(Project p) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean addAll(Collection<? extends Project> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean containsAll(Collection c) {
            return getDelegateTo().containsAll(c);
        }
        
        @Override
        public <T> T[] toArray(T[] a) {
            return getDelegateTo().toArray(a);
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int size() {
            return getDelegateTo().size();
        }
        
        @Override
        public synchronized boolean isEmpty() {
            if (delegateTo == null) {
                return createSubprojects(null) == null;
            } else {
                return delegateTo.isEmpty();
            }
        }
        
        @Override
        public Iterator<Project> iterator() {
            return getDelegateTo().iterator();
        }
        
        @Override
        public Object[] toArray() {
            return getDelegateTo().toArray();
        }

        @Override
        public int hashCode() {
            return getDelegateTo().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return getDelegateTo().equals(obj);
        }

        @Override
        public String toString() {
            return getDelegateTo().toString();
        }
        
    }
    
}
