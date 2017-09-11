/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
