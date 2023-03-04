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
package org.netbeans.modules.nativeexecution.api.execution;

import java.nio.charset.Charset;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.openide.windows.InputOutput;

/**
 * This is a wrapper over the <tt>ExecutionDescriptor</tt> to be used with the
 * <tt>NativeExecutionService</tt>
 *
 * @see ExecutionDescriptor
 * @see NativeExecutionService
 *
 * @author ak119685
 */
public final class NativeExecutionDescriptor {

    boolean controllable;
    boolean frontWindow;
    boolean requestFocus;
    boolean inputVisible;
    InputOutput inputOutput;
    boolean outLineBased;
    boolean showProgress;
    Runnable postExecution;
    LineConvertorFactory errConvertorFactory;
    LineConvertorFactory outConvertorFactory;
    boolean resetInputOutputOnFinish = true;
    boolean closeInputOutputOnFinish = true;
    Charset charset;
    PostMessageDisplayer postMessageDisplayer;

    public NativeExecutionDescriptor controllable(boolean controllable) {
        this.controllable = controllable;
        return this;
    }

    public NativeExecutionDescriptor frontWindow(boolean frontWindow) {
        this.frontWindow = frontWindow;
        return this;
    }

    public NativeExecutionDescriptor inputVisible(boolean inputVisible) {
        this.inputVisible = inputVisible;
        return this;
    }

    public NativeExecutionDescriptor inputOutput(InputOutput inputOutput) {
        this.inputOutput = inputOutput;
        return this;
    }

    public NativeExecutionDescriptor outLineBased(boolean outLineBased) {
        this.outLineBased = outLineBased;
        return this;
    }

    public NativeExecutionDescriptor showProgress(boolean showProgress) {
        this.showProgress = showProgress;
        return this;
    }

    /**
     * Passed Runnable will be executed after process is finished and all I/O is
     * done. Also it is guaranteed that executed process's exitValue() is
     * available at this point...
     *
     * @param postExecution
     * @return
     */
    public NativeExecutionDescriptor postExecution(Runnable postExecution) {
        this.postExecution = postExecution;
        return this;
    }

    public NativeExecutionDescriptor errConvertorFactory(LineConvertorFactory errConvertorFactory) {
        this.errConvertorFactory = errConvertorFactory;
        return this;
    }

    public NativeExecutionDescriptor outConvertorFactory(LineConvertorFactory outConvertorFactory) {
        this.outConvertorFactory = outConvertorFactory;
        return this;
    }

    public NativeExecutionDescriptor noReset(boolean noReset) {
        this.resetInputOutputOnFinish = !noReset;
        return this;
    }

    public NativeExecutionDescriptor keepInputOutputOnFinish() {
        this.closeInputOutputOnFinish = false;
        return this;
    }

    public NativeExecutionDescriptor charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public NativeExecutionDescriptor postMessageDisplayer(PostMessageDisplayer postMessageDisplayer) {
        this.postMessageDisplayer = postMessageDisplayer;
        return this;
    }

    public NativeExecutionDescriptor requestFocus(boolean requestFocus) {
        this.requestFocus = requestFocus;
        return this;
    }
}
