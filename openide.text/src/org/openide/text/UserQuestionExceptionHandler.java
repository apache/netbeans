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
package org.openide.text;

import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.UserQuestionException;

/**
 * Extensible handler of UQEs.
 *
 * @author Miloslav Metelka
 */
class UserQuestionExceptionHandler implements Runnable {
    
    private final CloneableEditorSupport ces;
    
    private UserQuestionException uqe;
    
    private StyledDocument doc;
    
    UserQuestionExceptionHandler(CloneableEditorSupport ces, UserQuestionException uqe) {
        this.ces = ces;
        this.uqe = uqe;
    }

    void runInEDT() {
        Mutex.EVENT.readAccess(this);
    }

    @Override
    public void run() {
        handleUserQuestionException();
    }
    
    /**
     * @return true if document was opened successfully or false otherwise.
     */
    boolean handleUserQuestionException() {
        handleStart();
        try {
            while (true) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(uqe.getLocalizedMessage(), NotifyDescriptor.YES_NO_OPTION);
                nd.setOptions(new Object[]{NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION});
                Object res = DialogDisplayer.getDefault().notify(nd);
                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    try {
                        uqe.confirmed();
                        uqe = null;
                        doc = openDocument();
                        opened(doc);
                        return true;
                        
                    } catch (UserQuestionException ex) {
                        uqe = ex;
                    } catch (IOException ex1) {
                        handleIOException(ex1);
                        return false;
                    } catch (RuntimeException ex) {
                        handleRuntimeException(ex);
                        return false;
                    }
                } else {
                    openRefused();
                    return false;
                }
            }
        }finally {
            handleEnd();
        }
    }
    
    protected StyledDocument openDocument() throws IOException {
        return ces.openDocument();
    }
    
    protected void handleStart() {
        // Do nothing by default - subclasses may override
    }
    
    protected void handleEnd() {
        // Do nothing by default - subclasses may override
    }

    protected void opened(StyledDocument openDoc) {
        // Do nothing by default - subclasses may override
    }
    
    protected void openRefused() {
        // Do nothing by default - subclasses may override
    }
    
    protected void handleIOException(IOException ex) {
        // Print exception by default - subclasses may override
        Exceptions.printStackTrace(ex);
    }
    
    protected void handleRuntimeException(RuntimeException ex) {
        // Print exception by default - subclasses may override
        Exceptions.printStackTrace(ex);
    }
    
    public final StyledDocument getDocument() {
        return doc;
    }
    
}
