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

package org.netbeans.modules.refactoring.spi;

import org.netbeans.modules.refactoring.api.ProgressListener;

/**
 * Refactoring plugins and transactions should implement this interface, if they
 * want to notify their progress of commit, preCheck, prepare and
 * checkParameters method.
 *
 * @author Jan Becicka
 */
public interface ProgressProvider {
    /**
     * Adds progress listener.
     * The listener is notified when long-lasting operation started/advanced/stopped .
     * @param listener
     */
    public void addProgressListener(ProgressListener listener);

    /**
     * Remove progress listener.
     * The listener is notified when long-lasting operation started/advanced/stopped .
     * @param listener
     */
    public void removeProgressListener(ProgressListener listener);
}
