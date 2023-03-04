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

package org.netbeans.modules.db.api.sql.execute;

import java.util.List;

/**
 * Provides information about the execution of one or more SQL statements.
 *
 * Note this initial implementation is quite simple, just doing as much
 * as I need.  It can be extended to provide more information.
 *
 * @author David Van Couvering
 */
public interface SQLExecutionInfo {
    /**
     * Determine if any statements had exceptions
     *
     * @return true if any statement executed had exceptions
     */
    public boolean hasExceptions();

    /**
     * Get all exceptions for all statements.  This is useful for quickly
     * reporting all exceptions.  Use getStatementInfos() for more complete
     * information about each statement
     *
     * @return the list of all exceptions encountered when executing the SQL, or
     * an empty list if none were encountered
     */
    public List<? extends Throwable> getExceptions();

    /**
     * Get the list of information about the statements that were executed
     *
     * @return list of information about statements executed
     */
    public List<StatementExecutionInfo> getStatementInfos();
}
