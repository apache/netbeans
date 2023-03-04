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
package org.netbeans.modules.gradle.editor.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.editor.MimeTypes;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * Special care is needed as Gradle does not have a parser and gradle error hints are not
 * obtained from parsing the source, but rather from project reloads, so the standard
 * NB infrastructure does not apply well to this scenario. 
 * <p>
 * The GradleHintsInvoker is a factory in project's Lookup and it is activated when a Gradle
 * project loads, so when Gradle projects are not used none of this code will load.
 * <p>
 * The EditorInitializer hooks to the EditorRegistry and forces collection of GradleReports
 * and their conversion to annotations. Sadly the HintsCollector only works for opened visual
 * components as its implementation monitors EditorRegistry.
 * 
 * @author sdedic
 */
@LookupProvider.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleHintsInvoker implements LookupProvider {

    public GradleHintsInvoker() {
        // force initialization of the EDitorRegistry hook.
        Lookup.getDefault().lookup(EditorInitializer.class);
    }

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        NbGradleProject p = baseContext.lookup(NbGradleProject.class);
        if (p == null) {
            return Lookup.EMPTY;
        }
        return Lookups.fixed(new GradleHintsProvider(baseContext.lookup(Project.class)));
    }


    @ServiceProvider(service = EditorInitializer.class)
    public static class EditorInitializer implements PropertyChangeListener {

        public EditorInitializer() {
            EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                JTextComponent comp = EditorRegistry.focusedComponent();
                Document doc = comp.getDocument();
                if (doc == null) {
                    return;
                }
                FileObject fo = EditorDocumentUtils.getFileObject(doc);
                if (fo == null) {
                    return;
                }
                if (!(MimeTypes.GRADLE_FILE.equals(fo.getMIMEType()) || MimeTypes.GRADLE_KOTLIN_FILE.equals(fo.getMIMEType()))) {
                    return;
                }
                
                Project p = FileOwnerQuery.getOwner(fo);
                if (p == null) {
                    return;
                }
                GradleHintsProvider prov = p.getLookup().lookup(GradleHintsProvider.class);
                if (prov == null) {
                    return;
                }
                prov.updateProjectProblems();
            }
        }
    }
}
