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

import java.awt.EventQueue;
import java.awt.Image;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 * @author Petr Somol
 */
@ServiceProvider(service=ProjectIconAnnotator.class, position=10)
public class JFXProjectIconAnnotator implements ProjectIconAnnotator {

    private static final String JFX_BADGE_PATH = "org/netbeans/modules/javafx2/project/ui/resources/jfx_overlay.png";    //NOI18N
    private final AtomicReference<Image> badgeCache = new AtomicReference<Image>();
    private final ChangeSupport cs = new ChangeSupport(this);
    private final Map<Project,Boolean> projectType = Collections.synchronizedMap(new WeakHashMap<Project,Boolean>());

    @Override
    @NonNull
    public Image annotateIcon(
            @NonNull final Project p,
            @NonNull Image original,
            final boolean openedNode) {
        Boolean type = projectType.get(p);
        if (type != null) {
            if(type.booleanValue() == true) {
                final Image badge = getJFXBadge();
                if (badge != null) {
                    original = ImageUtilities.mergeImages(original, badge, 8, 8);
                }
            }
        } else {
            evaluateProjectType(p);
        }
        return original;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
        assert cs.hasListeners();
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
    
    public void fireChange(@NonNull final Project p, boolean type) {
        projectType.put(p, type);
        cs.fireChange();
    }


    /**
     * Gets the badge
     * @return badge or null if badge icon does not exist
     */
    @NullUnknown
    private Image getJFXBadge() {
        Image img = badgeCache.get();
        if (img == null) {
            if(!EventQueue.isDispatchThread()) {
                img = ImageUtilities.loadImage(JFX_BADGE_PATH);
                badgeCache.set(img);
            } else {
                final Runnable runLoadIcon = new Runnable() {
                    @Override
                    public void run() {            
                        badgeCache.set(ImageUtilities.loadImage(JFX_BADGE_PATH));
                        cs.fireChange();
                    }
                };
                final RequestProcessor RP = new RequestProcessor(JFXProjectIconAnnotator.class.getName());
                RP.post(runLoadIcon);
            }
        }
        return img;
    }
    
    /**
     * Evaluate project properties fo find out whether it is a FX project
     * and record the info weakly in projectType map, then fire change to reannotate icon
     */
    private void evaluateProjectType(@NonNull final Project prj) {
        final Runnable runEvaluateProject = new Runnable() {
            @Override
            public void run() {
                boolean type = isFXProject(prj);
                projectType.put(prj, type);
                if(type == true) {
                    cs.fireChange();
                }
            }
        };
        if(!EventQueue.isDispatchThread()) {
            runEvaluateProject.run();
        } else {
            final RequestProcessor RP = new RequestProcessor(JFXProjectIconAnnotator.class.getName());
            RP.post(runEvaluateProject);
        }
    }

    private static boolean isFXProject(@NonNull final Project prj) {
        final J2SEPropertyEvaluator eval = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (eval == null) {
            return false;
        }
        //Don't use JFXProjectProperties.isTrue to prevent JFXProjectProperties from being loaded
        //JFXProjectProperties.JAVAFX_ENABLED is inlined by compliler
        return isTrue(eval.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

    private static boolean isTrue(@NullAllowed final String value) {
        return  value != null && (
           "true".equalsIgnoreCase(value) ||    //NOI18N
           "yes".equalsIgnoreCase(value) ||     //NOI18N
           "on".equalsIgnoreCase(value));       //NOI18N
    }

}
