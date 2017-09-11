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
package org.netbeans.modules.css.prep.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.css.live.LiveUpdater;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

// XXX copied & adjusted from PHP
public final class UiUtils {
    
    static final RequestProcessor RP = new RequestProcessor(UiUtils.class);

    
    private UiUtils() {
    }

    /**
     * Display Options dialog with Miscellaneous &lt; CSS Preprocessors panel preselected.
     */
    public static void showOptions() {
        OptionsDisplayer.getDefault().open(CssPreprocessorsUI.OPTIONS_PATH);
    }

    /**
     * Display a dialog with the message and then open IDE options.
     * @param message message to display before IDE options are opened
     */
    public static void invalidScriptProvided(String message) {
        Parameters.notNull("message", message); // NOI18N

        informAndOpenOptions(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    /**
     * Show a dialog that informs user about exception during running an external process.
     * Opens IDE options.
     * @param exc {@link ExecutionException} thrown
     */
    @NbBundle.Messages({
        "# {0} - error message",
        "UiUtils.running.exception=Script invocation failed with the error message:\n{0}"
    })
    public static void processExecutionException(ExecutionException exc) {
        Parameters.notNull("exc", exc); // NOI18N

        final Throwable cause = exc.getCause();
        assert cause != null;
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                informAndOpenOptions(new NotifyDescriptor.Message(
                        Bundle.UiUtils_running_exception(cause.getLocalizedMessage()),
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        });
    }

    public static void refreshCssInBrowser(File cssFile) {
        final LiveUpdater liveUpdater = Lookup.getDefault().lookup(LiveUpdater.class);
        if (liveUpdater != null) {
            FileObject fob = FileUtil.toFileObject(cssFile);
            if (fob != null) {
                try {
                    DataObject dob = DataObject.find(fob);
                    EditorCookie cookie = dob.getLookup().lookup(EditorCookie.class);
                    if (cookie != null) {
                        final Document doc = cookie.openDocument();
                        
                        //Schedule the document reading after existing EDT tasks finishes.
                        //One of the tasks should be the document's content reload triggered 
                        //by the FileObject's content change:
                        //https://netbeans.org/bugzilla/show_bug.cgi?id=213141#c4
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                RP.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        liveUpdater.update(doc);
                                    }
                                });
                            }
                        });
                        
                    }
                } catch (IOException donfex) {
                    //no-op
                }
            }
        }
    }

    static void informAndOpenOptions(NotifyDescriptor descriptor) {
        assert descriptor != null;

        DialogDisplayer.getDefault().notify(descriptor);
        showOptions();
    }

}
