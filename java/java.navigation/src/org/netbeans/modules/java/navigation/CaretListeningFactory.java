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

package org.netbeans.modules.java.navigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
 * This factory creates tasks sensitive to the caret position in open Java editor.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public class CaretListeningFactory extends CaretAwareJavaSourceTaskFactory implements PropertyChangeListener {
    
    private static CaretListeningFactory INSTANCE;
    
    public CaretListeningFactory() {
        super(Phase.RESOLVED, Priority.LOW, TaskIndexingMode.ALLOWED_DURING_SCAN);
        INSTANCE = this;
        EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(this, EditorRegistry.class));
    }

    @Override
    public CancellableTask<CompilationInfo> createTask(FileObject fileObject) {
        return new CaretListeningTask(fileObject);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
            CaretListeningTask.resetLastEH();
        }
    }

    static void runAgain() {
        if (INSTANCE != null) {
            List<FileObject> fileObjects = INSTANCE.getFileObjects();
            CaretListeningTask.resetLastEH();
            if ( !fileObjects.isEmpty() ) {
                // System.out.println("Rescheduling for " + fileObjects.get(0));
                INSTANCE.reschedule(fileObjects.iterator().next());
            }
        }
    }
    
    
}
