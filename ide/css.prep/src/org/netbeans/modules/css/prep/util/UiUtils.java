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
