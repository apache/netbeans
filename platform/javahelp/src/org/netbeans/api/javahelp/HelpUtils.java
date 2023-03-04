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
package org.netbeans.api.javahelp;

import javax.help.HelpSet;
import org.netbeans.modules.javahelp.HelpConstants;

/**
 * Utility methods for helpsets.
 *
 * @since javahelp/2.34
 * @author jhavlin
 */
public final class HelpUtils {

    /**
     * Merge a custom helpset to the master helpset.
     * <p>
     * Merge helpsets registered using e.g.
     * {@code @ServiceProvider(service=HelpSet.class)} to the master helpset.
     * </p>
     * <p>
     * You may need this method if you create helpsets that use some special
     * sources of data, e.g. the web. See bug 234144.
     * </p>
     *
     * @param customHelpSet The custom help set.
     */
    public static void mergeCustomHelpset(HelpSet customHelpSet) {
        customHelpSet.setKeyData(
                HelpConstants.HELPSET_MERGE_CONTEXT,
                HelpConstants.HELPSET_MERGE_ATTR,
                true);
    }
}
