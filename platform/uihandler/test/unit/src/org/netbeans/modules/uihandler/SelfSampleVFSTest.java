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
package org.netbeans.modules.uihandler;

import java.io.File;
import org.openide.filesystems.FileSystem;

/** Checks that the SelfSampleVFS in this module is similar to the one in sampler
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SelfSampleVFSTest extends org.netbeans.modules.sampler.SelfSampleVFSTest {
    
    public SelfSampleVFSTest(String s) {
        super(s);
    }
    
    @Override
    protected FileSystem createVFS(String[] names, File[] files) {
        return new SelfSampleVFS(names, files);
    }    
}
