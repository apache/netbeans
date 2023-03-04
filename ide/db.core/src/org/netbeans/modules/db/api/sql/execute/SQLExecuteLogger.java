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

/**
 * This interface defines a logger that can log information about each
 * statement as it is executed
 *
 * @author David Van Couvering
 */
public interface SQLExecuteLogger {

    /**
     * A statement has completed executing, and this method can be used
     * to log information about the statement
     * 
     * @param info information about the statement that was executed
     */
    public void log(StatementExecutionInfo info);

    /**
     * This method is called when all statements have been executed.
     *
     * @param executionTime the time it took in milliseconds for all statements
     *   to be executed.
     */
    public void finish(long executionTime);


    /**
     * This method is called when execution is cancelled.
     */
    public void cancel();

}
