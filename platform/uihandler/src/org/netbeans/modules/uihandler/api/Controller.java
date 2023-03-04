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
package org.netbeans.modules.uihandler.api;

import java.beans.PropertyChangeListener;
import java.net.URL;
import org.netbeans.modules.uihandler.Installer;
import org.netbeans.modules.uihandler.UIHandler;

/** Class that allows other modules to control the behavior of the UI
 * Gestures submit process.
 *
 * @author Jaroslav Tulach
 */
public final class Controller {
    private static final Controller INSTANCE = new Controller();

    private Controller() {
    }
    
    /** @return the controller instance */
    public static Controller getDefault() {
        return INSTANCE;
    }

    /** Controls exception reporting. Either enables or disables it.
     * @param enable enable or disable.
     * @since 2.0
     */
    public void setEnableExceptionHandler(boolean enable) {
        UIHandler.registerExceptionHandler(enable);
    }
    
    /** Getter for the number of collected log records
     * @return the number of currently
     * @since 2.0
     */
    public int getLogRecordsCount() {
        return Installer.getLogsSize();
    }
    
    /** Are logs automatically send to server when the local buffer gets full?
     * @return true if automatic submit is enabled
     */
    public boolean isAutomaticSubmit() {
        return Installer.isHintsMode();
    }
    
    /** If the automatic mode is on, this method returns the URL that has been
     * returned when data were transmitted to the server last time.
     * @return null or URL with "hints"
     */
    public URL getHintsURL() {
        return Installer.hintsURL();
    }
    
    /**
     * Adds listener for various properties
     * @param l 
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        UIHandler.SUPPORT.addPropertyChangeListener(l);
    }
    
    /**
     * Removes property change listener. 
     *
     * @param l 
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        UIHandler.SUPPORT.removePropertyChangeListener(l);
    }
    
    /** Explicitly invoke the submit data procedure. The method returns
     * immediately, then it opens a dialog and asks the user whether he
     * wants to submit the data.
     * @since 2.0
     */
    public void submit() {
        new ExplicitSubmit();
    }

    private static class ExplicitSubmit implements Runnable {
        @SuppressWarnings("LeakingThisInConstructor")
        public ExplicitSubmit() {
            Installer.RP.post(this);
        }
        
        @Override
        public void run() {
            Installer.displaySummary("WELCOME_URL", true, false, true); // NOI18N
        }
    }
}
