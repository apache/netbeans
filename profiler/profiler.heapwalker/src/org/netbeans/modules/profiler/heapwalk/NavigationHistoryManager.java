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

package org.netbeans.modules.profiler.heapwalk;

import org.netbeans.lib.profiler.ProfilerLogger;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Jiri Sedlacek
 */
public class NavigationHistoryManager {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface NavigationHistoryCapable {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Configuration getCurrentConfiguration();

        public void configure(Configuration configuration);
    }

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class Configuration {
    }

    private static class NavigationHistoryItem {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Configuration configuration;
        private NavigationHistoryCapable source;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public NavigationHistoryItem(NavigationHistoryCapable source, Configuration configuration) {
            this.source = source;
            this.configuration = configuration;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Configuration getConfiguration() {
            return configuration;
        }

        public NavigationHistoryCapable getSource() {
            return source;
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HeapFragmentWalker heapFragmentWalker;
    private List<NavigationHistoryItem> backHistory;
    private List<NavigationHistoryItem> forwardHistory;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public NavigationHistoryManager(HeapFragmentWalker heapFragmentWalker) {
        this.heapFragmentWalker = heapFragmentWalker;
        backHistory    = new ArrayList<NavigationHistoryItem>();
        forwardHistory = new ArrayList<NavigationHistoryItem>();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isNavigationBackAvailable() {
        return !backHistory.isEmpty();
    }

    public boolean isNavigationForwardAvailable() {
        return !forwardHistory.isEmpty();
    }

    public void createNavigationHistoryPoint() {
        NavigationHistoryItem item = createCurrentHistoryItem();

        if (item != null) {
            backHistory.add(item);
        }

        NavigationHistoryCapable source = heapFragmentWalker.getNavigationHistorySource();

        if (source != null) {
            forwardHistory.clear();
        }
    }

    public void navigateBack() {
        if (!isNavigationBackAvailable()) {
            return;
        }

        NavigationHistoryItem item = backHistory.remove(backHistory.size() - 1);
        NavigationHistoryItem reverseItem = createCurrentHistoryItem();

        if (reverseItem != null) {
            forwardHistory.add(reverseItem);
        }

        try {
            item.getSource().configure(item.getConfiguration());
        } catch (Exception ex) {
            ProfilerLogger.log(ex);
        }
    }

    public void navigateForward() {
        if (!isNavigationForwardAvailable()) {
            return;
        }

        NavigationHistoryItem item = forwardHistory.remove(forwardHistory.size() - 1);
        NavigationHistoryItem reverseItem = createCurrentHistoryItem();

        if (reverseItem != null) {
            backHistory.add(reverseItem);
        }

        try {
            item.getSource().configure(item.getConfiguration());
        } catch (Exception ex) {
            ProfilerLogger.log(ex);
        }
    }

    private NavigationHistoryItem createCurrentHistoryItem() {
        NavigationHistoryCapable source = heapFragmentWalker.getNavigationHistorySource();

        if (source != null) {
            Configuration configuration = source.getCurrentConfiguration();

            if (configuration != null) {
                return new NavigationHistoryItem(source, configuration);
            }
        }

        return null;
    }
}
