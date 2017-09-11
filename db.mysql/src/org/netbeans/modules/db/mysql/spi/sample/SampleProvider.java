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

package org.netbeans.modules.db.mysql.spi.sample;

import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;

/**
 * This interface defines an SPI for a module to provide support for
 * creating a sample database against a MySQL database
 *
 * @author David Van Couvering
 */
public interface SampleProvider {
    /**
     * The folder path to use when registering a sample provider
     */
    public static final String SAMPLE_PROVIDER_PATH = "Databases/MySQL/SampleProviders";

    /**
     * Create the sample database of the given name.  Note that the database connection
     * is for the sample database; this method need only create the tables and other database
     * objects, not the database itself.
     * 
     * @param sampleName the name of the sample to create
     * @param dbconn the connection to use when creating the sample
     *
     * @throws DatabaseException if some error occurred when creating the database objects.  It
     * is not guaranteed that the database is in a "clean" state after a failure; the caller
     * should either clean up or notify the user so they can clean up.
     */
    public void create(String sampleName, DatabaseConnection conn) throws DatabaseException;

    /**
     * Determine if this provider knows how to create a sample of the given name
     *
     * @param name
     *   The name of the sample
     *
     * @return true if this provider knows how to create a sample with the given name
     */
    public boolean supportsSample(String name);


    /**
     * Get the list of sample names this provider supports
     */
    public List<String> getSampleNames();
}
