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

package org.netbeans.modules.php.editor.codegen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrei Badea
 */
public abstract class DatabaseURL {

    public enum Server {
        MYSQL
    }

    public static DatabaseURL detect(String url) {
        MySQLURL mySQL = new MySQLURL(url);
        return mySQL.matches() ? mySQL : null;
    }

    abstract boolean matches();

    public abstract Server getServer();

    public abstract String getHost();

    public abstract String getPort();

    public abstract String getDatabase();

    private static final class MySQLURL extends DatabaseURL {

        private static final Pattern PATTERN = Pattern.compile("jdbc:mysql://([^/]+)(/(.*))?"); // NOI18N
        private final Matcher matcher;
        private String host;
        private String port;

        public MySQLURL(String url) {
            matcher = PATTERN.matcher(url);
        }

        @Override
        boolean matches() {
            boolean result = matcher.matches();
            if (result) {
                String hostAndPort = matcher.group(1);
                if (hostAndPort != null) {
                    String[] split = hostAndPort.split(":");
                    host = split[0].trim();
                    if (split.length > 1) {
                        port = split[1].trim();
                    }
                }
                if (host == null || host.length() == 0) {
                    result = false;
                }
            }
            return result;
        }

        @Override
        public Server getServer() {
            return Server.MYSQL;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public String getPort() {
            return port;
        }

        @Override
        public String getDatabase() {
            String database = matcher.group(3);
            if (database == null) {
                return null;
            }
            database = database.trim();
            if (database.length() == 0) {
                return null;
            }
            return database;
        }
    }
}
