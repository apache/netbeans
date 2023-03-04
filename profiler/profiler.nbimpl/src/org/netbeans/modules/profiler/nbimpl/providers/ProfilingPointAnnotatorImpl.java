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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointAnnotator;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.text.Annotatable;
import org.openide.text.Line;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=ProfilingPointAnnotator.class)
public final class ProfilingPointAnnotatorImpl extends ProfilingPointAnnotator.Basic {
    
    @Override
    public void annotate(CodeProfilingPoint profilingPoint) {
        annotate(profilingPoint, profilingPoint.getAnnotations());
    }

    @Override
    public void deannotate(CodeProfilingPoint profilingPoint) {
        deannotate(profilingPoint.getAnnotations());
    }

    @Override
    public void annotationChanged(PropertyChangeEvent evt) {
        ProfilingPoint profilingPoint = (ProfilingPoint) evt.getSource();
        deannotate((CodeProfilingPoint.Annotation[]) evt.getOldValue());
        annotate((CodeProfilingPoint) profilingPoint, (CodeProfilingPoint.Annotation[]) evt.getNewValue());
    }
    
    
    private void annotate(final CodeProfilingPoint profilingPoint, final CodeProfilingPoint.Annotation[] annotations) {
        ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
            public void run() {
                for (CodeProfilingPoint.Annotation cppa : annotations) {
                    // --- Code for saving dirty profiling points on document save instead of IDE closing ----------------
                    //          DataObject dataObject = Utils.getDataObject(profilingPoint.getLocation(cppa));
                    //          if (dataObject != null) dataObject.addPropertyChangeListener(ProfilingPointsManager.this);
                    // ---------------------------------------------------------------------------------------------------
                    Line editorLine = Utils.getEditorLine(profilingPoint, cppa);

                    if (editorLine != null) {
                        editorLine.addPropertyChangeListener(ProfilingPointsManager.getDefault());
                        cppa.attach(editorLine);
                    }
                }
            }
        });
    }
    
    private void deannotate(CodeProfilingPoint.Annotation[] annotations) {
        for (CodeProfilingPoint.Annotation cppa : annotations) {
            // --- Code for saving dirty profiling points on document save instead of IDE closing ----------------
            //      DataObject dataObject = Utils.getDataObject(profilingPoint.getLocation(cppa));
            //      if (dataObject != null) dataObject.removePropertyChangeListener(ProfilingPointsManager.this);
            // ---------------------------------------------------------------------------------------------------
            Annotatable cppaa = cppa.getAttachedAnnotatable();

            if (cppaa != null) {
                cppaa.removePropertyChangeListener(ProfilingPointsManager.getDefault());
            }

            cppa.detach();
        }
    }
    
}
