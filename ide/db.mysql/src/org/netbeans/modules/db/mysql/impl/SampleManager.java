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

package org.netbeans.modules.db.mysql.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.mysql.spi.sample.SampleProvider;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.util.NbBundle;

/**
 * A utility class for creating sample databases 
 * 
 * @author David Van Couvering
 */
public class SampleManager {
    /**
     * Get the list of sample names that the manager knows how to create
     *
     * @return
     */
    public static List<String> getSampleNames() {
        Collection<SampleProvider> providers = SampleProviderHelper.getProviders();

        List<String> sampleNames = new ArrayList<String>();
        for (SampleProvider provider : providers) {
            sampleNames.addAll(provider.getSampleNames());
        }
        return sampleNames;
    }

    /**
     * Determine if a give database name is for a sample database
     *
     * @param name the name of the database
     * @return true if this is the name of a samlpe database
     */
    public static boolean isSample(String name) {
        Collection<SampleProvider> providers = SampleProviderHelper.getProviders();

        List<String> sampleNames = new ArrayList<String>();
        for (SampleProvider provider : providers) {
            sampleNames.addAll(provider.getSampleNames());
        }
        return sampleNames.contains(name);
    }

    private static SampleProvider getProvider(String sampleName) {
        Collection<SampleProvider> providers = SampleProviderHelper.getProviders();

        for (SampleProvider provider : providers) {
            if (provider.supportsSample(sampleName)) {
                return provider;
            }
        }

        return null;
    }

    /**
     * Create the tables and other database objects for a MySQL sample database.
     * This method talks to the database synchronously and therefore can not
     * be called on the AWT event thread.
     *
     * @param sampleName the name of the sample to create
     * @param dbconn the connection to use when creating the sample
     * @throws org.netbeans.api.db.explorer.DatabaseException if something goes wrong.
     *   If an error occurs, it is more than likely that the databases objects were partially
     *   created.  The caller will need to clean up if it knows how, or ask the user to clean up.
     */
    public static void createSample(String sampleName, DatabaseConnection dbconn) 
            throws DatabaseException {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("You can not call this method from the event dispatch thread");
        }

        SampleProvider provider = getProvider(sampleName);

        if (provider == null) {
                throw new DatabaseException(Utils.getMessage("MSG_NoSuchSample", sampleName));
        }
        
        provider.create(sampleName, dbconn);
    }
}
