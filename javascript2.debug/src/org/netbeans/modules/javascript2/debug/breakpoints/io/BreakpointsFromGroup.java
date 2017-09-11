/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.debug.breakpoints.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.GroupProperties;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Set of breakpoints from a group.
 * 
 * @author Martin
 */
public final class BreakpointsFromGroup implements Set<Breakpoint> {
    
    private final String groupName;
    private final TestGroupProperties testProperties;
    
    public BreakpointsFromGroup(String groupName) {
        this.groupName = groupName;
        this.testProperties = null;
    }
    
    public BreakpointsFromGroup(TestGroupProperties testProperties) {
        this.groupName = null;
        this.testProperties = testProperties;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public TestGroupProperties getTestGroupProperties() {
        return testProperties;
    }
    
    private List<Breakpoint> getBreakpointsFromGroup() {
        List<Breakpoint> breakpoints = new ArrayList<Breakpoint>();
        Breakpoint[] bps = DebuggerManager.getDebuggerManager().getBreakpoints();
        if (groupName != null) {
            for (Breakpoint b : bps) {
                if (groupName.equals(b.getGroupName())) {
                    breakpoints.add(b);
                }
            }
        } else if (testProperties != null) {
            for (Breakpoint b : bps) {
                GroupProperties groupProperties = b.getGroupProperties();
                if (groupProperties != null &&
                    !groupProperties.isHidden() &&
                    testProperties.accept(groupProperties)) {
                    
                    breakpoints.add(b);
                }
            }
        }
        return breakpoints;
    }
    
    @Override
    public int size() {
        return getBreakpointsFromGroup().size();
    }

    @Override
    public boolean isEmpty() {
        return getBreakpointsFromGroup().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getBreakpointsFromGroup().contains(o);
    }

    @Override
    public Iterator<Breakpoint> iterator() {
        return getBreakpointsFromGroup().iterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return getBreakpointsFromGroup().containsAll(c);
    }

    @Override
    public Object[] toArray() {
        return getBreakpointsFromGroup().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getBreakpointsFromGroup().toArray(a);
    }

    @Override
    public boolean add(Breakpoint e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(Collection<? extends Breakpoint> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    public static final class TestGroupProperties {
        
        private FileObject fo;
        private Project p;
        private String type;
        
        public TestGroupProperties(FileObject fo) {
            this.fo = fo;
        }
        
        public TestGroupProperties(Project p) {
            this.p = p;
        }
        
        public TestGroupProperties(String type) {
            this.type = type;
        }
        
        public FileObject getFileObject() {
            return fo;
        }
        
        public Project getProject() {
            return p;
        }
        
        public String getType() {
            return type;
        }
        
        boolean accept(GroupProperties gp) {
            if (fo != null) {
                FileObject[] files = gp.getFiles();
                if (files != null) {
                    for (FileObject f : files) {
                        if (fo.equals(f)) {
                            return true;
                        }
                    }
                }
            }
            if (p != null) {
                Project[] projects = gp.getProjects();
                if (projects != null) {
                    for (Project pp : projects) {
                        if (p.equals(pp)) {
                            return true;
                        }
                    }
                }
            }
            if (type != null) {
                if (type.equals(gp.getType())) {
                    return true;
                }
            }
            return false;
        }
    }
    
}
