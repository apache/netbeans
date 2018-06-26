/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.api.PhpConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 * Php Sources class.
 * Is a wrapper for Sources created using 'new SourcesHelper(AntProjectHelper, PropertyEvaluator).createSources()'.
 * Is created to add possibility to reload Sources object stored into Project's lookup.<br>
 * Implements ChangeListener to react on wrapped Sourses.<br>
 * Implements AntProjectListener to react on modified properties file.<br>
 * @author avk
 */
public class PhpSources implements Sources, ChangeListener, PropertyChangeListener {

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final SourceRoots seleniumRoots;

    private boolean dirty;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator,
            final SourceRoots sourceRoots, final SourceRoots testRoots, final SourceRoots seleniumRoots) {
        assert project != null;
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testRoots != null;
        assert seleniumRoots != null;

        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.seleniumRoots = seleniumRoots;

        this.evaluator.addPropertyChangeListener(this);
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
        this.seleniumRoots.addPropertyChangeListener(this);
    }

    @Override
    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            @Override
            public SourceGroup[] run() {
                Sources delegateCopy;
                synchronized (PhpSources.this) {
                    if (delegate == null) {
                        delegate = initSources();
                        delegate.addChangeListener(PhpSources.this);
                    }
                    if (dirty) {
                        delegate.removeChangeListener(PhpSources.this);
                        delegate = initSources();
                        delegate.addChangeListener(PhpSources.this);
                        dirty = false;
                    }
                    delegateCopy = delegate;
                }
                return delegateCopy.getSourceGroups(type);
            }
        });
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private Sources initSources() {
        SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);
        register(sourcesHelper, sourceRoots);
        register(sourcesHelper, testRoots);
        register(sourcesHelper, seleniumRoots);
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return sourcesHelper.createSources();
    }

    private void register(SourcesHelper sourcesHelper, SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            sourcesHelper.sourceRoot(loc).displayName(displayName)
                    .add() // adding as principal root, continuing configuration
                    .type(PhpConstants.SOURCES_TYPE_PHP).add(); // adding as typed root
         }
     }

    private void fireChange() {
        synchronized (this) {
            dirty = true;
        }
        changeSupport.fireChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            fireChange();
        }
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        fireChange();
    }
}
