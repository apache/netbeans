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
package org.netbeans.modules.php.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.framework.PhpFrameworks;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Handler for PHP frameworks of the project.
 */
public final class Frameworks {

    private static final Logger LOGGER = Logger.getLogger(Frameworks.class.getName());

    private final PhpModule phpModule;
    private final LookupListener frameworksListener = new FrameworksListener();
    private final List<PhpFrameworkProvider> frameworks = new CopyOnWriteArrayList<>();
    final ChangeSupport changeSupport = new ChangeSupport(this);

    volatile boolean frameworksDirty = true;


    public Frameworks(PhpModule phpModule) {
        this.phpModule = phpModule;
    }

    public void projectOpened() {
        PhpFrameworks.addFrameworksListener(frameworksListener);
        resetFrameworks();
        // detect frameworks in a background thread
        getFrameworks();
        for (PhpFrameworkProvider frameworkProvider : PhpFrameworks.getFrameworks()) {
            frameworkProvider.phpModuleOpened(phpModule);
        }
    }

    public void projectClosed() {
        for (PhpFrameworkProvider frameworkProvider : PhpFrameworks.getFrameworks()) {
            frameworkProvider.phpModuleClosed(phpModule);
        }
        PhpFrameworks.removeFrameworksListener(frameworksListener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public List<PhpFrameworkProvider> getFrameworks() {
        if (phpModule.getSourceDirectory() == null) {
            // corrupted project
            return Collections.emptyList();
        }
        synchronized (frameworks) {
            if (frameworksDirty) {
                frameworksDirty = false;
                List<PhpFrameworkProvider> allFrameworks = PhpFrameworks.getFrameworks();
                List<PhpFrameworkProvider> newFrameworks = new ArrayList<>(allFrameworks.size());
                for (PhpFrameworkProvider frameworkProvider : allFrameworks) {
                    if (frameworkProvider.isInPhpModule(phpModule)) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("Adding framework %s for project %s", frameworkProvider.getIdentifier(), phpModule.getName()));
                        }
                        newFrameworks.add(frameworkProvider);
                    }
                }
                frameworks.clear();
                frameworks.addAll(newFrameworks);
            }
        }
        return new ArrayList<>(frameworks);
    }

    public void resetFrameworks() {
        frameworksDirty = true;
    }

    //~ Inner classes

    private final class FrameworksListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ev) {
            LOGGER.fine("frameworks change, frameworks back to null");
            resetFrameworks();
            changeSupport.fireChange();
        }
    }

}
