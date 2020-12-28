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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.python.api;

import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class PythonPlatformProvider {

    private static final Logger LOGGER = Logger.getLogger(PythonPlatformProvider.class.getName());
    
    private final PropertyEvaluator evaluator;

    public PythonPlatformProvider(final PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public PythonPlatform getPlatform() {
        ensurePlatformsReady();
        String id = evaluator.getProperty("platform.active"); // NOI18N
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        if (id == null) {
            id = manager.getDefaultPlatform();
        }
        PythonPlatform platform = manager.getPlatform(id);
        if (platform == null) {
            LOGGER.info("Platform with id '" + id + "' does not exist. Using default platform.");
            platform = manager.getPlatform(manager.getDefaultPlatform());
        }
        return platform;
    }

    private void ensurePlatformsReady() {
        if (!Util.isFirstPlatformTouch()) {
            return;
        }
        String handleMessage = NbBundle.getMessage(PythonPlatformProvider.class, "PythonPlatformProvider.PythonPlatformAutoDetection");
        ProgressHandle ph = ProgressHandleFactory.createHandle(handleMessage);
        ph.start();
        try {
            Thread autoDetection = new Thread(new Runnable() {
                @Override
                public void run() {
                    PythonPlatformManager.getInstance().autoDetect();
                }
            }, "Python Platform AutoDetection"); // NOI18N
            autoDetection.start();
            autoDetection.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        ph.finish();
    }
}
