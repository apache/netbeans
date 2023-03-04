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
package org.netbeans.modules.project.ui.spi;

import java.util.List;
import org.openide.loaders.DataObject;

/**
 * post process the order of template categories for given project. Implementation to be included in project's lookup.
 * @author mkleint
 * @since 1.40
 */
public interface TemplateCategorySorter {
 
    /**
     * Sort the template categories. The same DataObjects should be returned, in same or different order, no filtering is to be done.
     * @param original
     * @return new ordering for the list.
     */
    List<DataObject> sort(List<DataObject> original);
}
