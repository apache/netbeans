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
package org.netbeans.modules.git.ui.actions;

import java.util.concurrent.Callable;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.client.ProgressDelegate;

/**
 *
 * @author Ondra
 */
public abstract class ActionProgressSupport {

    private final GitProgressSupportDelegate delegate;

    protected ActionProgressSupport  (GitProgressSupportDelegate delegate) {
        this.delegate = delegate;
    }

    public final void execute () throws GitException {
        Callable<ActionProgress> nextAction = getNextAction();
        if (nextAction == null) {
            delegate.getProgress().cancel();
        } else {
            try {
                ActionProgress p = nextAction.call();
                if (p.isCanceled()) {
                    delegate.getProgress().cancel();
                } else if (p.isError()) {
                    delegate.getProgress().setError(true);
                }
            } catch (GitException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new GitException(ex);
            }
        }
    }

    protected abstract Callable<ActionProgress> getNextAction ();

    public static interface GitProgressSupportDelegate {

        ProgressDelegate getProgress ();

    }
}
