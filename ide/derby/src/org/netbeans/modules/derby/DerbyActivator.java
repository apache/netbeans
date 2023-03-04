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

package org.netbeans.modules.derby;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Andrei Badea, Jiri Rechtacek
 */
public class DerbyActivator {

    private static final Logger LOGGER = Logger.getLogger(DerbyActivator.class.getName());
    private static final String FIRST_RUN = "first_run"; // NOI18N
    
    public static synchronized void activate() {
        // activate only for 1st time
        boolean firstTime = NbPreferences.forModule(DerbyActivator.class).getBoolean(FIRST_RUN, Boolean.TRUE);
        Logger.getLogger(DerbyActivator.class.getName()).finest("Is DerbyActivator.activate() called for the 1st time? " + firstTime);
        if (firstTime) {
            NbPreferences.forModule(DerbyActivator.class).putBoolean(FIRST_RUN, Boolean.FALSE);
            doActivate();
        }
    }

    private static final JDKDerbyHelper helper = JDKDerbyHelper.forDefault();

    private static void doActivate() {
        if (!helper.canBundleDerby()) {
            LOGGER.fine("Default platform cannot bundle Derby"); // NOI18N
            return;
        }

        ProgressHandle handle = ProgressHandleFactory.createSystemHandle(NbBundle.getMessage(DerbyActivator.class, "MSG_RegisterJavaDB"));
        handle.start();
        try {
            if (registerDerby()) {
                registerSampleDatabase();
            }
        } finally {
            handle.finish();
        }
    }

    private static boolean registerDerby() {
        String derbyLocation = helper.findDerbyLocation();
        if (derbyLocation != null) {
            LOGGER.log(Level.FINE, "Registering Derby at {0}", derbyLocation); // NOI18N
            return DerbyOptions.getDefault().trySetLocation(derbyLocation);
        }
        return false;
    }

    private static void registerSampleDatabase() {
        try {
            DerbyDatabases.createSampleDatabase();
        } catch (DatabaseException e) {
            LOGGER.log(Level.WARNING, null, e);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Failed to create sample database", e);
        }
    }
}
