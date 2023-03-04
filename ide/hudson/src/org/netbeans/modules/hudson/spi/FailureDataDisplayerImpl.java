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
package org.netbeans.modules.hudson.spi;

import org.netbeans.modules.hudson.api.ui.FailureDataDisplayer.Suite;

/**
 * Displays data from {@link BuilderConnector.FailureDataProvider}.
 *
 * @author jhavlin
 */
public abstract class FailureDataDisplayerImpl {

    /**
     * Prepare the displayer.
     */
    public abstract void open();

    /**
     * Display result of a test suite.
     *
     * @param suite Test suite data.
     */
    public abstract void showSuite(Suite suite);

    /**
     * Finish writing to the displayer.
     */
    public abstract void close();
}
