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
@OptionsPanelController.ContainerRegistration(
        id = "Rust",
        categoryName = "#OptionsCategory_Name_Rust",
        iconBase = "org/netbeans/modules/rust/options/rust-logo-big.png", 
        keywords = "#OptionsCategory_Keywords_Rust", 
        keywordsCategory = "Rust",
        position = 10000)
@NbBundle.Messages(value = {"OptionsCategory_Name_Rust=Rust", "OptionsCategory_Keywords_Rust=rust cargo"})
package org.netbeans.modules.rust.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
