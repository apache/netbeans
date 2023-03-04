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

import org.netbeans.modules.parsing.api.Task;


/**
 * Abstract ascendant of all types of SPI tasks ({@link EmbeddingProvider}, 
 * {@link ParserBasedEmbeddingProvider} and {@link ParserResultTask}). 
 * Do not extend this class directly!
 *
 * @author Jan Jancura
 */
public abstract class SchedulerTask extends Task {

    SchedulerTask () {}
    
    /**
     * A priority. Less number wins.
     * 
     * @return              Priority of this listener.
     */
    public abstract int getPriority ();
    
    /**
     * Returns {@link Scheduler} class for this SchedulerTask. See
     * {@link Scheduler} documentation for a list of default schedulers,
     * or your your own implementation.
     * 
     * @return              {@link Scheduler} for this SchedulerTask.
     */
    public abstract Class<? extends Scheduler> getSchedulerClass ();
    
    /**
     * Called by infrastructure when the task was interrupted by the
     * infrastructure. 
     * todo: Shouldn't be replaced by Result.isCanceled?
     */
    public abstract void cancel ();
}




