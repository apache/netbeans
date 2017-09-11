/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
