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
package org.netbeans.modules.profiler.api;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.netbeans.modules.profiler.spi.ActionsSupportProvider;
import org.openide.awt.Actions;
import org.openide.util.Lookup;

/**
 * Allows to customize key bindings for profiler actions.
 *
 * @author Jiri Sedlacek
 */
public final class ActionsSupport {
    
    public static final KeyStroke NO_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_UNDEFINED, 0);

    public static String keyAcceleratorString(KeyStroke keyStroke) {
        if (keyStroke == null || NO_KEYSTROKE.equals(keyStroke)) {
            return null;
        }
        return Actions.keyStrokeToString(keyStroke);
    }
    
    public static KeyStroke registerAction(String actionKey, Action action, ActionMap actionMap, InputMap inputMap) {
        for (ActionsSupportProvider provider : Lookup.getDefault().lookupAll(ActionsSupportProvider.class)) {
            KeyStroke ks = provider.registerAction(actionKey, action, actionMap, inputMap);
            if (ks != null) return ks;
        }
        return null;
    }
    
}
