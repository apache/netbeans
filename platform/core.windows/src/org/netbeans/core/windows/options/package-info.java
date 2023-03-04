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
@OptionsPanelController.ContainerRegistration(id = "Appearance", 
        position = 750,
        categoryName = "#OptionsCategory_Name_Appearance", 
        iconBase = "org/netbeans/core/windows/options/appearance32.png", 
        keywords = "#OptionsCategory_Keywords_Appearance", 
        keywordsCategory = "Appearance")
@NbBundle.Messages(value = {"OptionsCategory_Name_Appearance=Appearance", "OptionsCategory_Keywords_Appearance=look and feel, windows, document tab"})
package org.netbeans.core.windows.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
