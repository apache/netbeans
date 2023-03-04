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

import java.awt.Desktop;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import org.openide.ErrorManager;

/**
 *
 * @author Tomas Hurka
 * 
 * Uses new 'JEP 272: Platform-Specific Desktop Features' API.
 * This class can be merged with superclass, once NetBeans is built by JDK 9.
 */
public class NbApplicationAdapterJDK9 extends NbApplicationAdapter implements AboutHandler, OpenFilesHandler, PreferencesHandler, QuitHandler {

    static void install() {
        try {
            Desktop app = Desktop.getDesktop();
            NbApplicationAdapterJDK9 al = new NbApplicationAdapterJDK9();

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
        Desktop app = Desktop.getDesktop();

        app.setAboutHandler(null);
        app.setOpenFileHandler(null);
        app.setPreferencesHandler(null);
        app.setQuitHandler(null);
    }

    @Override
    public void handleAbout(AboutEvent e) {
        handleAbout();
    }

    @Override
    public void openFiles(OpenFilesEvent e) {
        openFiles(e.getFiles());
    }

    @Override
    public void handlePreferences(PreferencesEvent e) {
        handlePreferences();
    }

    @Override
    public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
        handleQuit();
        //need to do this otherwise the user will never be able to quit again
        response.cancelQuit();
    }
}
