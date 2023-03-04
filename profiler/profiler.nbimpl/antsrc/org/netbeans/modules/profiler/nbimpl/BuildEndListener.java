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
package org.netbeans.modules.profiler.nbimpl;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@oracle.com>
 */
class BuildEndListener implements BuildListener {
    private final AtomicBoolean cancel;

    BuildEndListener(AtomicBoolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public void buildStarted(BuildEvent be) {
    }

    @Override
    public void targetStarted(BuildEvent be) {
    }

    @Override
    public void targetFinished(BuildEvent be) {
    }

    @Override
    public void taskStarted(BuildEvent be) {
    }

    @Override
    public void taskFinished(BuildEvent be) {
    }

    @Override
    public void messageLogged(BuildEvent be) {
    }

    @Override
    public void buildFinished(BuildEvent be) {
        this.cancel.set(true);
    }
    
}
