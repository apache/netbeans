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
package org.netbeans.modules.db.dataview.util;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DBTestUtil {

    /**
     * Disable logging of logging messages from DatabaseMetaDataQuoter.
     *
     * This method is a workaround of problems in the database modules and can
     * be removed, when the problem is really fixed.
     *
     */
    public static void suppressSuperfluousLogging() {
        // TODO: Remove this code and fix the core problem
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setFilter(new Filter() {
                @Override
                public boolean isLoggable(LogRecord lr) {
                    if (lr.getSourceClassName().equals(
                            "org.netbeans.api.db.sql.support.SQLIdentifiers$DatabaseMetaDataQuoter")) {
                        if (lr.getSourceMethodName().equals("getExtraNameChars")
                                && lr.getLevel() == Level.WARNING
                                && lr.getMessage().startsWith(
                                "DatabaseMetaData.getExtraNameCharacters() failed")) {
                            return false;
                        } else if (lr.getSourceMethodName().equals("needToQuote")
                                && lr.getLevel().intValue() <= Level.INFO.intValue()) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            });
        }
    }
}
