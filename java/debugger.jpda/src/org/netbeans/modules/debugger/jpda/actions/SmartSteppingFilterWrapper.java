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
package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.SmartSteppingFilter;

/**
 * A wrapper over {@link JPDADebugger#getSmartSteppingFilter()}. A new instance
 * of this class is created for every step to store step's specific patterns.
 */
public final class SmartSteppingFilterWrapper implements SmartSteppingFilter {

    private final SmartSteppingFilter delegate;
    private Set<String> addedPatterns;
    private Set<String> removedPatterns;

    public SmartSteppingFilterWrapper(SmartSteppingFilter delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void addExclusionPatterns(Set<String> patterns) {
        if (addedPatterns == null) {
            addedPatterns = new LinkedHashSet<>();
        }
        addedPatterns.addAll(patterns);
    }

    @Override
    public synchronized void removeExclusionPatterns(Set<String> patterns) {
        if (removedPatterns == null) {
            removedPatterns = new LinkedHashSet<>();
        }
        removedPatterns.addAll(patterns);
    }
    
    @Override
    public String[] getExclusionPatterns() {
        String[] patterns = delegate.getExclusionPatterns();
        synchronized (this) {
            if (addedPatterns == null && removedPatterns == null) {
                return patterns;
            }
            Set<String> newPatterns = new LinkedHashSet<>(Arrays.asList(patterns));
            if (removedPatterns != null) {
                newPatterns.removeAll(removedPatterns);
            }
            if (addedPatterns != null) {
                newPatterns.addAll(addedPatterns);
            }
            return newPatterns.toArray(new String[0]);
        }
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        // No listening on the temporary wrapper
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        // No listening on the temporary wrapper
        throw new UnsupportedOperationException();
    }
}
