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
package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.UpdateType;
import org.openide.filesystems.FileObject;

/**
 * This interface describes the tables used to generate
 * classes and these classes. It contains a set of tables
 * and the locations of the classes generated for these tables
 * (the root folder, the package name and the class name).
 *
 * @author Andrei Badea
 */
public interface GeneratedTables {

    /**
     * Returns the catalog of the tables
     */
    public String getCatalog();
    
    /**
     * Returns the schema of the tables
     */
    public String getSchema();
    
    /**
     * Returns the names of the tables which should be used to generate classes.
     */
    public Set<String> getTableNames();

    /**
     * Returns the root folder of the class which will be generated for
     * the specified table.
     */
    public FileObject getRootFolder(String tableName);

    /**
     * Returns the package of the class which will be generated for
     * the specified table.
     */
    public String getPackageName(String tableName);

    /**
     * Returns the name of the class to be generated for the specified table.
     */
    public String getClassName(String tableName);

    /**
     * Returns the type of the update the class to be generated for the specified table.
     */
    public UpdateType getUpdateType(String tableName);

    /**
     * Returns the unique constraints defined on the table
     */
    public Set<List<String>> getUniqueConstraints(String tableName);
}
