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
package org.netbeans.modules.java.hints.friendapi;

import com.sun.source.util.TreePath;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;

/**
 * The interface is an optional mixing which can be present on the ErrorRule implementation and takes care of potential
 * transformation of the standard message to something more precise. If present, it will be called as part of error
 * conversion to annotations to produce a different error message. If more rules matching diagnostic key respond
 * to the createMessage call, the infrastructure will choose a message or several messages to appear. All fixes will be available from that
 * message.
 * <p/>
 * Note that the customized message does not propagate into the task list; the task list will show the original javac message.
 * @since 1.82
 */
public interface OverrideErrorMessage<T> extends ErrorRule<T> {
    /**
     * Provides a custom error message to replace the one in Diagnostic. If the implementation does not want to produce
     * a custom message, it should return {@code null}, the default message will be used. If the Rule stores a data 
     * into the 'data' holder, that data will be available later, in the call to run() method. 
     * 
     * @param info context
     * @param diagnosticKey error message key
     * @param offset offset in the Source
     * @param treePath path to the error, if available
     * @return an override message to be displayed instead of the standard one or {@code null} to use the standard one.
     */
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, Data<T> data);
}
