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
package org.netbeans.modules.versioning.annotate;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Encapsulates set of VcsAnnotation lines.
 * 
 * @author Maros Sandor
 */
public abstract class VcsAnnotations {

    private static final String ANNOTATIONS_CHANGED = "AnnotationsProvider.annotationsChanged";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    protected VcsAnnotations() {
    }

    public VcsAnnotation [] getAnnotations() {
        return new VcsAnnotation[0];
    }
    
    public Action[] getActions(VcsAnnotation annotation) {
        return new Action[0];
    }
    
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    protected final void fireAnnotationsChanged() {
        support.firePropertyChange(ANNOTATIONS_CHANGED, null, null);
    }
}
