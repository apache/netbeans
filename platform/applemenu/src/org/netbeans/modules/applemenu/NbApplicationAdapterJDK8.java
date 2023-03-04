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
package org.netbeans.modules.applemenu;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import com.apple.eawt.OpenFilesHandler;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Hurka
 * 
 * Uses old com.apple.eawt.* API. 
 * This class can be deleted once NetBeans is built by JDK 9.
 */
public class NbApplicationAdapterJDK8 extends NbApplicationAdapter implements AboutHandler, OpenFilesHandler, PreferencesHandler, QuitHandler {

    static void install() {
        try {
            Application app = Application.getApplication();
            NbApplicationAdapterJDK8 al = new NbApplicationAdapterJDK8();

            app.setAboutHandler(al);
            app.setOpenFileHandler(al);
            app.setPreferencesHandler(al);
            app.setQuitHandler(al);
        } catch (Throwable ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
        } finally {
        }
        NbApplicationAdapter.install();
    }

    static void uninstall() {
        Application app = Application.getApplication();

        app.setAboutHandler(null);
        app.setOpenFileHandler(null);
        app.setPreferencesHandler(null);
        app.setQuitHandler(null);
    }

    @Override
    public void handleAbout(AppEvent.AboutEvent e) {
        handleAbout();
    }

    @Override
    public void openFiles(AppEvent.OpenFilesEvent e) {
        openFiles(e.getFiles());
    }

    @Override
    public void handlePreferences(AppEvent.PreferencesEvent e) {
        handlePreferences();
    }

    @Override
    public void handleQuitRequestWith(AppEvent.QuitEvent e, QuitResponse response) {
        handleQuit();
        //need to do this otherwise the user will never be able to quit again
        response.cancelQuit();
    }
}
