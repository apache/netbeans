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
package org.netbeans.modules.cnd.highlight.hints;

import javax.swing.text.BadLocationException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Allows to perform a change when the user selects the hint if file can be modified,
 * displays warning otherwise
 */
public abstract class SafeFix implements Fix {

    @Override
    public final ChangeInfo implement() throws Exception {
        try {
            return performFix();
        } catch (BadLocationException ex) {
            NotifyDescriptor descriptor = new NotifyDescriptor(NbBundle.getMessage(SafeFix.class, "SafeFix.message")  // NOI18N
                                                              ,NbBundle.getMessage(SafeFix.class, "SafeFix.title")  // NOI18N
                                                              ,NotifyDescriptor.DEFAULT_OPTION
                                                              ,NotifyDescriptor.ERROR_MESSAGE
                                                              ,new Object[]{NotifyDescriptor.OK_OPTION}
                                                              ,NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(descriptor);
        }
        return null;
    }
    
    /**
     * Actual implementation of the fix actions
     * @return A ChangeInfo instance if invoking the hint caused changes
     *  that should change the editor selection/caret position, or null
     *  if no such change was made, or proper caret positioning cannot be
     *  determined.
     * @throws BadLocationException
     * @throws Exception 
     */
    public abstract ChangeInfo performFix() throws BadLocationException, Exception;
}
