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

package org.netbeans.core.windows.persistence;

import org.openide.modules.SpecificationVersion;

/**
 * Class of internal config properties of window system elements.
 *
 * @author  Marek Slama
 */
public class InternalConfig {

    public SpecificationVersion specVersion;

    public String moduleCodeNameBase;
    public String moduleCodeNameRelease;
    public String moduleSpecificationVersion;

    /** Creates a new instance of InternalConfig */
    public InternalConfig () {
    }

    public void clear () {
        specVersion = null;
        moduleCodeNameBase = null;
        moduleCodeNameRelease = null;
        moduleSpecificationVersion = null;
    }
    
}
