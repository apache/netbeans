/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
