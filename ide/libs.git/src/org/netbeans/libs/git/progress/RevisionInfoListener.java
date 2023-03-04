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

package org.netbeans.libs.git.progress;

import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitRevisionInfo;

/**
 * The listener interface for receiving notifications triggered when a git
 * revision is processed by a certain git commands (e.g. the log command).
 * The class that is interested in further processing such a revision
 * implements this interface and registers itself with an instance 
 * of {@link GitClient}. 
 * When a revision is processed by a git command, that object's 
 * <code>notifyRevisionInfo</code> method is invoked.
 * 
 * @see NotificationListener
 */
public interface RevisionInfoListener extends NotificationListener {
    /**
     * Invoked when a revision is processed by a certain git command.
     * @param revisionInfo git revision info
     */
    public void notifyRevisionInfo (GitRevisionInfo revisionInfo);
}
