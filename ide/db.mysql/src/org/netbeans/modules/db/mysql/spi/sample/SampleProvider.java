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
