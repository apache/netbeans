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
