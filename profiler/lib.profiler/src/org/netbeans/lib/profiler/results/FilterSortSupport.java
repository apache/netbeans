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
package org.netbeans.lib.profiler.results;

import java.util.ResourceBundle;
import org.netbeans.lib.profiler.global.CommonConstants;

/**
 *
 * @author Jiri Sedlacek
 */
public final class FilterSortSupport implements CommonConstants {
    
    public static final String FILTERED_OUT_LBL;
    
    static {
        ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.Bundle"); // NOI18N
        FILTERED_OUT_LBL = messages.getString("FilteringSupport_FilteredOutLbl"); //NOI18N
    }
    
    public static boolean passesFilter(Configuration info, String nodeName) {
        return passesFilter(info.getFilterString(), info.getFilterType(), nodeName);
    }
    
    public static boolean passesFilter(String filter, int filterType, String nodeName) {
        switch (filterType) {
            case FILTER_NONE:
                return true;
            case FILTER_CONTAINS:
                return nodeName.toLowerCase().contains(filter);
            case FILTER_NOT_CONTAINS:
                return !nodeName.toLowerCase().contains(filter);
            case FILTER_REGEXP:
                try {
                    return nodeName.matches(filter); // case sensitive!
                } catch (java.util.regex.PatternSyntaxException e) {
                    return false;
                }
        }
        return false;
    }
    
    
    public static final class Configuration {
        
        private int sortBy;
        private boolean sortOrder;
        private String filterString = ""; // NOI18N
        private int filterType = CommonConstants.FILTER_CONTAINS;
        
        
        public int getSortBy() {
            return sortBy;
        }
        
        public boolean getSortOrder() {
            return sortOrder;
        }
        
        public String getFilterString() {
            return filterString;
        }
        
        public int getFilterType() {
           return filterType;
        }
        
        
        public void setSortInfo(int sortBy, boolean sortOrder) {
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
        }
        
        public void setFilterInfo(String filterString, int filterType) {
            this.filterString = filterString;
            this.filterType = filterType;
        }
        
    }
   
}
