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

package org.netbeans.modules.cnd.utils.filters;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.util.NbBundle;

public class AllFileFilter extends FileAndFileObjectFilter {

    private static AllFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllFileFilter getInstance() {
        if (instance == null) {
            instance = new AllFileFilter();
        }
        return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AllFileFilter.class, "FILECHOOSER_All_FILEFILTER"); // NOI18N
    }

    @Override
    public String[] getSuffixes() {
        if (suffixes == null) {
            suffixes = getAllSuffixes();
        }
        return suffixes;
    }

    @Override
    public String getSuffixesAsString() {
        StringBuilder buf = new StringBuilder();
        buf.append(AllSourceFileFilter.getInstance().getSuffixesAsString()).append(' '); // NOI18N
        buf.append(ResourceFileFilter.getInstance().getSuffixesAsString()).append(' '); // NOI18N
        buf.append(QtFileFilter.getInstance().getSuffixesAsString());
        return buf.toString();
    }

    public static String[] getAllSuffixes() {
        List<String> allSuffixes = new ArrayList<String>();
        addSuffices(allSuffixes, AllSourceFileFilter.getInstance().getSuffixes());
        addSuffices(allSuffixes, ResourceFileFilter.getInstance().getSuffixes());
        addSuffices(allSuffixes, QtFileFilter.getInstance().getSuffixes());
        return allSuffixes.toArray(new String[allSuffixes.size()]);
    }

    private static void addSuffices(List<String> suffixes, String[] suffixes2) {
        suffixes.addAll(Arrays.asList(suffixes2));
    }
}
