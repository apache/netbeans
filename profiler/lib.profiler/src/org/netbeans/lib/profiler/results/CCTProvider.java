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

package org.netbeans.lib.profiler.results;


/**
 *
 * @author Jaroslav Bachorik
 */
public interface CCTProvider {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface Listener {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        /* void cctEstablished(RuntimeCCTNode appRootNode);
         *
         * in order to fix the issue #114638 i need to introduce the "empty" flag
         * it should be removed once this code is cleaned up
         */
        void cctEstablished(RuntimeCCTNode appRootNode, boolean empty);

        void cctReset();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    void addListener(Listener listener);

    void removeAllListeners();

    void removeListener(Listener listener);
}
