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
package org.netbeans.modules.java.source.parsing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

/**
 *
 * @author sdedic
 */
final class MutableCp implements ClassPathImplementation {
    

    private final PropertyChangeSupport support;
    private List<? extends PathResourceImplementation> impls;


    public MutableCp () {
         this (Collections.<PathResourceImplementation>emptyList());
    }

    public MutableCp (final List<? extends PathResourceImplementation> impls) {
        assert impls != null;
        support = new PropertyChangeSupport (this);
        this.impls =impls;
    }

    public List<? extends PathResourceImplementation> getResources() {
        return impls;
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        assert listener != null;
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        assert listener != null;
        this.support.removePropertyChangeListener(listener);
    }


    void setImpls (final List<? extends PathResourceImplementation> impls) {
        assert impls != null;
        this.impls = impls;
        this.support.firePropertyChange(PROP_RESOURCES, null, null);
    }

}
