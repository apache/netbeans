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

package org.netbeans.modules.j2ee.dd.api.web.model;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.j2ee.dd.impl.web.metadata.FilterInfoAccessor;

/**
 * Data object that holds information about filter.
 * @author Petr Slechta
 */
public class FilterInfo {

    private final String name;
    private final String filterClass;
    private final List<String> urlPatterns;

    static {
        FilterInfoAccessor.setDefault(new FilterInfoAccessor() {

            @Override
            public FilterInfo createFilterInfo(String name, String servletClass, List<String> urlPatterns) {
                return new FilterInfo(name, servletClass, urlPatterns);
            }
        });
    }
    
    private FilterInfo(String name, String filterClass, List<String> urlPatterns) {
        this.name = name;
        this.filterClass = filterClass;
        this.urlPatterns = urlPatterns;
    }

    /**
     * @return name of the servlet
     */
    public String getName() {
        return name;
    }

    /**
     * @return class that implements the servlet
     */
    public String getFilterClass() {
        return filterClass;
    }

    /**
     * @return URL patterns that are associated with the servlet
     */
    public List<String> getUrlPatterns() {
        return Collections.unmodifiableList(urlPatterns);
    }

}
