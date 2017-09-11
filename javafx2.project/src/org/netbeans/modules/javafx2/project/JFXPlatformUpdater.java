/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
