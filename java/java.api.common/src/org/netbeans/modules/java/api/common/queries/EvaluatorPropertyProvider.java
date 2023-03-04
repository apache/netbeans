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
package org.netbeans.modules.java.api.common.queries;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Tomas Zezula
 */
final class EvaluatorPropertyProvider implements PropertyProvider {
    private final PropertyEvaluator base;
    private final ChangeSupport listeners;

    EvaluatorPropertyProvider(@NonNull final PropertyEvaluator base) {
        this.base = base;
        this.listeners = new ChangeSupport(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return base.getProperties();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        this.listeners.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        this.listeners.removeChangeListener(l);
    }

    void update() {
        final Runnable act = () -> listeners.fireChange();
        if (ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess()) {
            act.run();
        } else {
            ProjectManager.mutex().readAccess(act);
        }
    }
}
