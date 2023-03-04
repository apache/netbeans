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

package org.netbeans.modules.apisupport.project;

import java.io.File;

/**
 * Interface to be implemented mainly by projects which are part of Module Suite
 * or are Suite themselves.
 *
 * @see org.netbeans.api.project.Project#getLookup
 * @author Martin Krauskopf
 */
public interface SuiteProvider {

    /**
     * Returns directory containing a regular suite or <code>null</code> if
     * a method implementation fails for some reason.
     */
    File getSuiteDirectory();

    /**
     * Returns directory (cluster) which suite modules are built into.
     */
    File getClusterDirectory();
}
