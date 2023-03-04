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
