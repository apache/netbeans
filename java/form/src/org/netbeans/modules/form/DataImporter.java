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
package org.netbeans.modules.form;

import java.util.concurrent.*;

/**
 * Provider of component with data (for example collection of JPA entities).
 *
 * @author Jan Stola
 */
public interface DataImporter {

    /**
     * Determines if any data can be imported into the specified form.
     * 
     * @param form target form.
     * @return {@code true} if this importer is able to import any data
     * into the specified form, returns {@code false} otherwise.
     */
    boolean canImportData(FormModel form);

    /**
     * Imports the data. Usually opens a dialog allowing the user to customize
     * the data to import.
     * 
     * @param form form to import the data into.
     * @return the component encapsulating the imported data.
     */
    Future<RADComponent> importData(FormModel form);

}
