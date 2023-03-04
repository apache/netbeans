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

package org.netbeans.modules.hudson.spi;

import java.util.Collection;
import org.openide.windows.OutputListener;

public interface HudsonJobChangeItem {

    String getUser();

    String getMessage();

    Collection<? extends HudsonJobChangeFile> getFiles();

    interface HudsonJobChangeFile {

        enum EditType {
            add, edit, delete
        }

        String getName();

        EditType getEditType();

        /**
         * Provides the ability for an SCM to hyperlink the diff of a file.
         * @return a hyperlink implementation, or null
         */
        OutputListener hyperlink();

    }

}
