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

package org.netbeans.spi.java.hints;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;

/**A factory for hint customizer.
 *
 * @author lahvac
 */
public interface CustomizerProvider {

    /**Create a customizer component. The hint settings are in the given
     * {@link Preferences}. The customizer can write into the provided {@link Preferences}
     * immediately, the values will be persisted or rolled-back automatically
     * based on the user's gesture.
     * 
     * <p>Be sure to set the default values for the options controlled by the customizer
     * into the provided {@link Preferences}. This should be done before returning the customizer. 
     * If you do not, the infrastructure will not be able to correctly enable/disable the Apply button in options window.
     *
     * @param prefs the hints preferences from which the data to show should be read,
     *              and to which the new settings should be written
     * @return a customizer component
     */
    public @NonNull JComponent getCustomizer(@NonNull Preferences prefs);

}
