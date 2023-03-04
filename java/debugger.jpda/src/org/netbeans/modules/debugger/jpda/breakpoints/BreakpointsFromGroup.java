/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.debugger.jpda.breakpoints;

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
