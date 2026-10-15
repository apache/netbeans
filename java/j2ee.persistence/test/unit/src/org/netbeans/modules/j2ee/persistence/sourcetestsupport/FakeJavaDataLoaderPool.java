/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.persistence.sourcetestsupport;

import java.util.Enumeration;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.util.Enumerations;



// Copied from org.netbeans.modules.j2ee.common.source
public class FakeJavaDataLoaderPool extends DataLoaderPool {
    
    @Override
    public Enumeration<? extends DataLoader> loaders() {
        return Enumerations.singleton(new JavaDataLoader());
    }
}
