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

package org.netbeans.lib.editor.codetemplates;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.modules.ModuleInstall;


/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplatesModule extends ModuleInstall {

    public @Override void restored () {
        EditorRegistry.addPropertyChangeListener(editorsTracker);
    }
    
    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public @Override void close() {
        finish();
    }
    
    /**
     * Called when module is uninstalled.
     */
    public @Override void uninstalled() {
        finish();
    }
    
    private PropertyChangeListener editorsTracker = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null || EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                AbbrevDetection.get((JTextComponent) evt.getNewValue());
            }
        }
    };
    
    private void finish() {
        EditorRegistry.removePropertyChangeListener(editorsTracker);
        for(JTextComponent jtc : EditorRegistry.componentList()) {
            AbbrevDetection.remove(jtc);
        }
    }

}
