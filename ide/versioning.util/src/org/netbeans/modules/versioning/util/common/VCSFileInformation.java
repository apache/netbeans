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

package org.netbeans.modules.versioning.util.common;

import java.awt.Color;
import java.util.Comparator;

/**
 *
 * @author Tomas Stupka
 */
public abstract class VCSFileInformation {

    public abstract String getStatusText();
    public abstract int getComparableStatus();
    public abstract String annotateNameHtml(String name);    

    public Color getAnnotatedColor () {
        return null;
    }

    /**
     * Compares two {@link FileInformation} objects by importance of statuses they represent.
     */
    static class ByImportanceComparator<T> implements Comparator<VCSFileInformation> {
        @Override
        public int compare(VCSFileInformation i1, VCSFileInformation i2) {
            return i1.getComparableStatus() - i2.getComparableStatus();
        }
    }    
}
