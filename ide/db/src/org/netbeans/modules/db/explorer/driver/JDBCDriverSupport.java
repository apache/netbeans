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

package org.netbeans.modules.db.explorer.driver;

import java.net.URL;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.filesystems.URLMapper;

/**
 * A helper class for working with JDBC Drivers.
 *
 * @author Andrei Badea
 */
public final class JDBCDriverSupport {

    private JDBCDriverSupport() {
    }

    /**
     * Return if the driver file(s) exists and can be loaded.
     * @return true if defined driver file(s) exists; otherwise false
     */
    public static boolean isAvailable(JDBCDriver driver) {
        URL[] urls = driver.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (URLMapper.findFileObject(urls[i]) == null) {
                return false;
            }
        }
        return true;
    }
}
