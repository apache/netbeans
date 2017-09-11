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
