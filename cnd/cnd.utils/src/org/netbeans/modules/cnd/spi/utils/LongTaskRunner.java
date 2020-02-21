/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.spi.utils;

import java.time.OffsetDateTime;
import org.openide.util.Cancellable;

/**
 *
 */
abstract public class LongTaskRunner {

    private final Runnable offEDTTask;
    private final Runnable postEDTTask;

    protected LongTaskRunner(Runnable offEDTTask, Runnable postEDTTask) {
        this.offEDTTask = offEDTTask;
        this.postEDTTask = postEDTTask;
    }

    protected Runnable getOffEDTRunner() {
        return offEDTTask;
    }

    protected Runnable getPostEDTRunner() {
        return postEDTTask;
    }

    abstract public void runLongTask(final String title, final String message, Cancellable canceller);

    /*package*/
    static class LongTaskRunnerImpl extends LongTaskRunner {

        public LongTaskRunnerImpl(Runnable offEDTTask, Runnable postEDTTask) {
            super(offEDTTask, postEDTTask);
        }

        @Override
        public void runLongTask(String title, String message, Cancellable canceller) {
            if (getOffEDTRunner() != null) {
                getOffEDTRunner().run();
            }
            if (getPostEDTRunner()!= null) {
                getPostEDTRunner().run();
            }            
        }

    }

}
