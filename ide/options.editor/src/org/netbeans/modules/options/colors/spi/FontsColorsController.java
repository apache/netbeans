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
package org.netbeans.modules.options.colors.spi;

import org.netbeans.modules.options.colors.*;
import javax.swing.JComponent;

/**
 *
 * @author vita
 */
public interface FontsColorsController {

    public void update(ColorModel model);

    /**
     * Changes the profile to the provided profile for configuration. If the
     * profile name does not exist, the currently set profile will be copied
     * into a new profile with the provided name.
     *
     * @param profile The next profile which should be configured.
     */
    public void setCurrentProfile(String profile);

    public void deleteProfile(String profile);
    public void applyChanges();
    public void cancel();
    public boolean isChanged();
    public JComponent getComponent();

}
