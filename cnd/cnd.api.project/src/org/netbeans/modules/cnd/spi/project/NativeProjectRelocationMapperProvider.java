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

package org.netbeans.modules.cnd.spi.project;

import org.netbeans.modules.cnd.api.project.NativeProject;

/**
 * If we had a project on some machine with paths to the project :
 * /export1/tmp/MYPROJECT and than we have parsed and and got repository copied
 * repository to the local machine (and source file also) by path
 * /export/home/MYPROJECT than in this particlar method we will get
 * /export/home/MYPROJECT as NativeProject, /export1/tmp/MYPROJECT/src/test.cc
 * as source file path (this is how it was stored in the repository originally)
 * and it should return /export/home/MYPROJECT/src/test.cc as a result
 *
 */
public interface NativeProjectRelocationMapperProvider {
    /**
     * If we had a project on some machine with paths to the project : 
     * /export1/tmp/MYPROJECT and than we have parsed and and got repository
     * copied repository to the local machine (and source file also) by path /export/home/MYPROJECT
     * than in this particlar method 
     * we will get /export/home/MYPROJECT  as NativeProject, /export1/tmp/MYPROJECT/src/test.cc as source file path
     * (this is how it was stored in the repository originally) and it should return 
     * /export/home/MYPROJECT/src/test.cc as a result
     * @param project
     * @param sourceFilePath
     * @return 
     */
    public CharSequence getDestinationPath(NativeProject project, CharSequence sourceFilePath);
    
    public CharSequence getSourceProjectName(NativeProject project);
    
    public NativeProject findDestinationProject(CharSequence sourceProjectName);
    
}
