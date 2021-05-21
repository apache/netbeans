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

package org.netbeans.modules.gradle.api.output;

import java.net.URL;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class OutputListeners {

    private OutputListeners() {
    }

    public static Runnable openFileAt(final FileObject file, final int line, final int col) {
        return new Runnable() {

            @Override
            public void run() {
                if (file != null) {
                    try {
                        DataObject data = DataObject.find(file);
                        if (data != null) {
                            EditorCookie cookie = data.getLookup().lookup(EditorCookie.class);
                            if (cookie != null) {
                                try {
                                    cookie.getLineSet().getOriginal(line - 1).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, col - 1);
                                } catch (IndexOutOfBoundsException ex) {
                                    cookie.open();
                                }

                            }

                        }
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            }

        };
    }

    public static Runnable openURL(final URL url) {
        return new Runnable() {

            @Override
            public void run() {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }

        };
    }

    public static Runnable displayStatusText(final String text) {
        return new Runnable() {

            @Override
            public void run() {
                StatusDisplayer.getDefault().setStatusText(text);
            }
        };
    }
}
