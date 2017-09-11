/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
