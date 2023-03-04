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
package org.netbeans.modules.javafx2.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Service class to help synchronization between JFXProjectOpenHook and JFXPRojectProblems
 * when platform property needs to be updated (removal of DefaultJavaFXPlatform in NB7.4)
 * 
 * @author psomol
 */
@ProjectServiceProvider(service=JFXPlatformUpdater.class, projectType={"org-netbeans-modules-java-j2seproject"}) // NOI18N
public class JFXPlatformUpdater {
    
    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    private final Project prj;
    private final J2SEPropertyEvaluator eval;
    volatile PropertyChangeListener listener;
    volatile boolean updated;
    
    public JFXPlatformUpdater(final Lookup lkp) {
        this.updated = false;
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
    }
    
    public void updateFXPlatform() {
        try {
            JFXProjectUtils.updateClassPathExtension(prj);
        } catch(IllegalArgumentException ex) {
            // missing platform; ignore here, will be detected in collectProblems() below
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Can't update project properties: {0}", ex); // NOI18N
        }
        updated = true;
        fireChange();
    }
    
    public void addListener(@NonNull PropertyChangeListener listener) {
        this.listener = listener;
    }
    
    public void removeListener() {
        this.listener = null;
    }
    
    public boolean hasUpdated() {
        return updated;
    }
    
    public void resetUpdated() {
        updated = false;
    }
    
    private void fireChange() {
        if(listener != null) {
            listener.propertyChange(new PropertyChangeEvent(this, null, null, null));
        }
    }
    
}
