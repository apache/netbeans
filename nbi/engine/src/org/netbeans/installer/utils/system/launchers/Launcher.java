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

package org.netbeans.installer.utils.system.launchers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.helper.JavaCompatibleProperties;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Dmitry Lipin
 */
public abstract class Launcher extends LauncherProperties {
    
    protected Launcher(LauncherProperties pr) {
        super(pr);
    }
    public abstract File create(Progress progress) throws IOException;
    public abstract void initialize() throws IOException;
    public abstract String [] getExecutionCommand();
    public abstract List <JavaCompatibleProperties> getDefaultCompatibleJava(Version version);
    public abstract String getExtension();
}
