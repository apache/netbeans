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
package org.netbeans.modules.mercurial.ui.branch;

import org.netbeans.modules.mercurial.ui.log.HgLogMessage;

/**
 *
 * @author ondra
 */
public class HgBranch {
    private final HgLogMessage revisionInfo;
    private final String name;
    private final boolean closed;
    private final boolean active;
    public static String DEFAULT_NAME = "default"; //NOI18N

    public HgBranch (String name, HgLogMessage revisionInfo, boolean closed, boolean active) {
        this.revisionInfo = revisionInfo;
        this.name = name;
        this.closed = closed;
        this.active = active;
    }

    public HgLogMessage getRevisionInfo () {
        return revisionInfo;
    }

    public String getName () {
        return name;
    }
    
    public boolean isClosed () {
        return closed;
    }
    
    public boolean isActive () {
        return active;
    }
}
