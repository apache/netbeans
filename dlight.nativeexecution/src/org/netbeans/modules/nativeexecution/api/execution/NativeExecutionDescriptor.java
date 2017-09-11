/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
