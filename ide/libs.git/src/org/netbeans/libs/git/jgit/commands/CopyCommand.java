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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CopyCommand extends MoveTreeCommand {
    private final String description;

    public CopyCommand (Repository repository, GitClassFactory gitFactory, File source, File target, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, source, target, true, true, monitor, listener);
        this.description = new StringBuilder("git copy ").append("--after ").append(source).append(" ").append(target).toString(); //NOI18N
    }

    @Override
    protected String getCommandDescription() {
        return description;
    }
}
