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

package org.netbeans.api.progress.aggregate;

/**
 * Interface allowing to monitor the progress within the agrregated handle.
 * @see AggregateProgressHandle#setMonitor(ProgressMonitor)
 * @author mkleint
 */
public interface ProgressMonitor {

    /**
     * the given contributor started it's work.
     * @param contributor the part of the progress indication that started
     */
    void started(ProgressContributor contributor);
    /**
     * the given contributor finished it's work.
     * @param contributor the part of the progress indication that finished
     */
    void finished(ProgressContributor contributor);
    
    /**
     * the given contributor progressed in it's work.
     * @param contributor the part of the progress indication that showed progress
     */
    void progressed(ProgressContributor contributor);
    
}
