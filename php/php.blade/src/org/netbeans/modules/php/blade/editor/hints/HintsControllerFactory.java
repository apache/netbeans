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
package org.netbeans.modules.php.blade.editor.hints;

import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author bogdan
 */
public class HintsControllerFactory {

    public HintsControllerFactory() {
    }

    @OptionsPanelController.SubRegistration(
            id = "BladeHints",
            location = "Blade/Hints",
            displayName = "#HintsControllerFactory.name"
    )
    @NbBundle.Messages("HintsControllerFactory.name=Blade Hints")
    public static OptionsPanelController createOptions() {
        HintsProvider.HintsManager manager = HintsProvider.HintsManager.getManagerForMimeType(BladeLanguage.MIME_TYPE);
        assert manager != null;

        return manager.getOptionsController();
    }
}
