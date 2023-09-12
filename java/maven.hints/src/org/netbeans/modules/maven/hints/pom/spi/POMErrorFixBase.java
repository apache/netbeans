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

package org.netbeans.modules.maven.hints.pom.spi;

import java.util.prefs.Preferences;
import javax.swing.JComponent;

/**
 *
 * @author mkleint
 */
public interface POMErrorFixBase {
    
    /** Gets the UI description for this rule. It is fine to return null
     * to get the default behavior. Notice that the Preferences node is a copy
     * of the node returned from {link:Configuration.getPreferences()}. This is in order to permit
     * canceling changes done in the options dialog.<BR>
     * It is fine to return null.
     * @param node Preferences node the customizer should work on.
     * @return Component which will be shown in the options dialog.
     */
    JComponent getCustomizer(Preferences preferences);

    /**
     * configuration for the error/hint
     * @return
     */
    Configuration getConfiguration();

}
