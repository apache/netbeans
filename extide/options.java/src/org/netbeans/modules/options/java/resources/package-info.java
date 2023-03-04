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
@OptionsPanelController.ContainerRegistration(
    id = JavaOptions.JAVA,
    position = 610, //Keymap Container is at position 600 and PHP Container is at position 650
    categoryName = "#OptionsCategory_Name_Java",
    iconBase = "org/netbeans/modules/options/java/resources/java_logo.png",
    keywords = "#OptionsCategory_Keywords_Java",
    keywordsCategory = JavaOptions.JAVA)
@Messages({"OptionsCategory_Keywords_Java=Java Options","OptionsCategory_Name_Java=Java"})
package org.netbeans.modules.options.java.resources;

import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle.Messages;
