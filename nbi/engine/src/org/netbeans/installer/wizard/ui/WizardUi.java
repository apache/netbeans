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

package org.netbeans.installer.wizard.ui;

import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 * This class represents the UI of a wizard component. It is an abstraction over the
 * set of possible UI modes for the wizard and provides factory methods which create
 * objects representing component's UI for a concrete wizard UI mode, such as 
 * {@link SwingUi}.
 * 
 * @see org.netbeans.installer.utils.helper.UiMode
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface WizardUi {
    /**
     * Creates an instance of {@link SwingUi} and initializes it with the specified 
     * {@link SwingContainer} object, thus initializaing the component's UI for 
     * {@link org.netbeans.installer.utils.helper.UiMode#SWING}.
     * 
     * @param container Instance of {@link SwingContainer} which will "contain" the
     *      resulting UI.
     * @return Instance of {@link SwingUi} which represents the component's UI for 
     *      the swing UI mode.
     */
    SwingUi getSwingUi(final SwingContainer container);
}
