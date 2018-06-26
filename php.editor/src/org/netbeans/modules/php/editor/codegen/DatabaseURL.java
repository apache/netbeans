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
