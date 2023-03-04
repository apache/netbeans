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
package org.netbeans.modules.web.browser.spi;

/**
 * A handle that allows page inspection to notify the inspected browser pane
 * about changes in page inspection options.
 *
 * @author Jan Stola
 */
public interface PageInspectionHandle {

    /**
     * Set selection mode.
     *
     * @param selectionMode new value of selection mode.
     */
    void setSelectionMode(boolean selectionMode);

    /**
     * Set selection synchronization.
     *
     * @param synchronizeSelection new value of selection synchronization.
     */
    void setSynchronizeSelection(boolean synchronizeSelection);

}
