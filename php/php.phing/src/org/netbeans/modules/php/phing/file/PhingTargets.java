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
package org.netbeans.modules.php.phing.file;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.openide.util.RequestProcessor;

public final class PhingTargets implements ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PhingTargets.class.getName());

    public static final String DEFAULT_TARGET = "default"; // NOI18N

    private static final RequestProcessor RP = new RequestProcessor(PhingTargets.class);

    private final Project project;
    private final BuildXml buildXml;

    private volatile List<String> targets;


    private PhingTargets(Project project, BuildXml buildXml) {
        assert project != null;
        assert buildXml != null;
        this.project = project;
        this.buildXml = buildXml;
    }

    public static PhingTargets create(Project project, BuildXml buildXml) {
        assert project != null;
        assert buildXml != null;
        PhingTargets phingTargets = new PhingTargets(project, buildXml);
        // listeners
        buildXml.addChangeListener(phingTargets);
        return phingTargets;
    }

    @CheckForNull
    public List<String> getTargets() {
        List<String> targetsRef = targets;
        return targetsRef == null ? null : Collections.unmodifiableList(targetsRef);
    }

    public List<String> loadTargets(@NullAllowed Long timeout, @NullAllowed TimeUnit unit) throws ExecutionException, TimeoutException {
        List<String> targetsRef = targets;
        if (targetsRef != null) {
            return Collections.unmodifiableList(targetsRef);
        }
        assert !EventQueue.isDispatchThread();
        Future<List<String>> targetsJob = getTargetsJob();
        if (targetsJob == null) {
            // some error
            return null;
        }
        try {
            List<String> allTargets;
            if (timeout != null) {
                assert unit != null;
                allTargets = targetsJob.get(timeout, unit);
            } else {
                allTargets = targetsJob.get();
            }
            targets = new CopyOnWriteArrayList<>(allTargets);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return getTargets();
    }

    @CheckForNull
    private Future<List<String>> getTargetsJob() {
        PhingExecutable phing = PhingExecutable.getDefault(project, buildXml.getFile().getParentFile(), false);
        if (phing == null) {
            return null;
        }
        return phing.listTargets();
    }

    public void reset() {
        targets = null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        reset();
    }

}
