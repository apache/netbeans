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

package org.netbeans.modules.java.api.common.project.ui.customizer;

import org.netbeans.spi.project.ui.CustomizerProvider;

/**
 * CustomizerProvider enhanced with ability to open customizer on given
 * category and/or subcategory.
 * 
 * @deprecated since 1.51, use org.netbeans.spi.project.ui.CustomizerProvider2 instead
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
@Deprecated
public interface CustomizerProvider2 extends CustomizerProvider {

    /**
     * Show customizer and preselect a category.
     */
    void showCustomizer(String preselectedCategory, String preselectedSubCategory);

}
