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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.timers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInstall;

/**
 *
 * @author nenik
 */
public class Install extends  ModuleInstall {

    static final boolean ENABLED;

    static {
        boolean assertionsEnabled = false;

        assert assertionsEnabled = true;

        ENABLED = assertionsEnabled || Boolean.getBoolean("org.netbeans.modules.timers.enable");
    }
    
    private static Handler timers = new TimerHandler();
    private static PropertyChangeListener docTracker = new ActivatedDocumentListener();

    private static String INSTANCES = "Important instances";
    
    public @Override void restored() {
        if (!ENABLED) {
            return ;
        }
        
        Logger log = Logger.getLogger("TIMER"); // NOI18N
        log.setUseParentHandlers(false);
        log.setLevel(Level.FINE);
        log.addHandler(timers);
        
        EditorRegistry.addPropertyChangeListener(docTracker);
    }
    
    private static class TimerHandler extends Handler {
        TimerHandler() {}
    
        public void publish(LogRecord rec) {
            String message = rec.getMessage();
            if (rec.getResourceBundle() != null) {
                try {
                    message = rec.getResourceBundle().getString(rec.getMessage());
                    if (rec.getParameters() != null) {
                        message = MessageFormat.format(message, rec.getParameters());
                    }
                } catch (MissingResourceException ex) {
                    Logger.getAnonymousLogger().log(Level.INFO, null, ex);
                }
            }
        
            Object[] args = rec.getParameters();
            if (args == null || args[0] == null) return;
            
            if (args.length == 1) { // simplified instance logging
                TimesCollectorPeer.getDefault().reportReference(
                        INSTANCES, rec.getMessage(), message, args[0]);
                return;
            }
            
            if (args.length < 2) {
                return;
            }
            
            Object key = args[0];

            if (args[1] instanceof Number) { // time
                TimesCollectorPeer.getDefault().reportTime(
                        key, rec.getMessage(), message, ((Number)args[1]).longValue());
            } else if (args[1] instanceof Boolean) { // start/stop logic
                // XXX - start/stop support
            } else {
                String txt = message.startsWith("[M]") ? message : "[M] " + message;
                TimesCollectorPeer.getDefault().reportReference(
                        key, rec.getMessage(), txt, args[1]);
            }
        }
    
        public void flush() {}
        public void close() throws SecurityException {}
    }

    /**
     *
     * @author Jan Lahoda
     */
    private static class ActivatedDocumentListener implements PropertyChangeListener {
        ActivatedDocumentListener() {}

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc == null) return;
            
            Document active = jtc.getDocument();
            Object sourceProperty = active.getProperty(Document.StreamDescriptionProperty);
            if (!(sourceProperty instanceof DataObject)) return;

            FileObject activeFile = ((DataObject)sourceProperty).getPrimaryFile();
            TimesCollectorPeer.getDefault().select(activeFile);
        }
    }
   
}
