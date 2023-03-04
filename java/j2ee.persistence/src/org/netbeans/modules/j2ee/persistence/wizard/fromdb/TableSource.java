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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;

/**
 * Describes a source for tables and can save and retrieve the source
 * for a given project.
 *
 * <p>The source for tables consists of the type (data source,
 * database connection or dbschema file) and the name of the source,
 * whose meaning is: the JNDI name for the data source, the
 * {@link org.netbeans.api.db.explorer.DatabaseConnection#getName() name}
 * of the database connection or the absolute path of the dbschema file.</p>
 *
 * @author Andrei Badea
 */
public class TableSource {

    public enum Type { DATA_SOURCE, CONNECTION, SCHEMA_FILE };

    private static final Map<Project, TableSource> PROJECT_TO_SOURCE = new WeakHashMap<>();

    private final Type type;
    private final String name;

    public static TableSource get(Project project) {
        synchronized (TableSource.class) {
            return PROJECT_TO_SOURCE.get(project);
        }
    }

    public static void put(Project project, TableSource tableSource) {
        synchronized (TableSource.class) {
            PROJECT_TO_SOURCE.put(project, tableSource);
        }
    }

    public TableSource(String name, Type type) {
        assert name != null;
        assert type != null;

        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
