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

package org.netbeans.installer.utils.system.cleaner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Lipin
 */
public class JavaOnExitCleanerHandler extends OnExitCleanerHandler {
    private List <File> filesList = new ArrayList <File> ();

    @Override
    public void addDeleteOnExitFile(File file) {
        filesList.add(file);
    }
    @Override
    public void removeDeleteOnExitFile(File file) {
        filesList.remove(file);
    }
    public void run()  {
        for(File f : filesList) {
            f.delete();
        }
    }
}
