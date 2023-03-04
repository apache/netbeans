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
package org.netbeans.modules.gsf.testrunner.ui.spi;

import org.openide.filesystems.FileObject;

/**
 * Implementations are expected to be found in the default lookup.
 *
 * @author Theofanis Oikonomou
 */
public interface TestCreatorConfigurationProvider {

    /**
     * Called when user invokes Create Tests action. This method may be called 
     * more times for the same <code>FileObject</code>s
     *
     * @param activatedFileObjects the activated FileObjects the "Create Tests"
     * dialog was invoked on
     * @return the configuration panel for tests creation
     */
    TestCreatorConfiguration createTestCreatorConfiguration(FileObject[] activatedFileObjects);

}
