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

package org.netbeans.modules.parsing.spi;

import java.util.Collection;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;


/**
 * Creates a list of tasks ({@link EmbeddingProvider}, 
 * {@link ParserBasedEmbeddingProvider} or {@link ParserResultTask}) for given source. 
 * @see MimeRegistration
 * @author Jan Jancura
 */
public abstract class TaskFactory {
    
    /**
     * Creates new <code>SchedulerTask</code>s for a <code>Snapshot</code>.
     * 
     * @param snapshot The {@link Snapshot} to create tasks for.
     *
     * @return New {@link SchedulerTask}s for the given {@link Snapshot}.
     */
    public abstract Collection<? extends SchedulerTask> create (Snapshot snapshot);

}




