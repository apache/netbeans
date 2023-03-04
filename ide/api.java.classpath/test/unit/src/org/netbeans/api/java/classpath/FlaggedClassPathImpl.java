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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class FlaggedClassPathImpl implements FlaggedClassPathImplementation {

    private final PropertyChangeSupport support;
    //@GuardedBy("this")
    private Set<ClassPath.Flag> flags;
    //@GuardedBy("this")
    private List<PathResourceImplementation> resources;

    FlaggedClassPathImpl() {
        this.support = new PropertyChangeSupport(this);
        this.flags = EnumSet.noneOf(ClassPath.Flag.class);
        this.resources = Collections.emptyList();
    }

    @Override
    public synchronized Set<ClassPath.Flag> getFlags() {
        return flags;
    }

    @Override
    public synchronized List<? extends PathResourceImplementation> getResources() {
        return resources;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.support.removePropertyChangeListener(listener);
    }

    void setResources(@NonNull final List<PathResourceImplementation> resources) {
        Parameters.notNull("resources", resources); //NOI18N
        synchronized (this) {
            this.resources = resources;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    void setFlags(@NonNull final Set<ClassPath.Flag> flags) {
        Parameters.notNull("flags", flags); //NOI18N
        synchronized (this) {
            this.flags = flags;
        }
        support.firePropertyChange(PROP_FLAGS, null, null);
    }
}
