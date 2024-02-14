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

package org.netbeans.modules.diff.builtin.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.diff.DiffModuleConfig;

/**
 *
 * @author  Martin Entlicher
 */
public class BuiltInDiffProvider extends DiffProvider implements java.io.Serializable {

    private Options options = DiffModuleConfig.getDefault().getOptions();
    
    static final long serialVersionUID = 1L;

    /** Creates a new instance of BuiltInDiffProvider */
    public BuiltInDiffProvider() {
    }
    
    /**
     * Get the display name of this diff provider.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.displayName");
    }
    
    /**
     * Get a short description of this diff provider.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(BuiltInDiffProvider.class, "BuiltInDiffProvider.shortDescription");
    }
    
    /**
     * Create the differences of the content two streams.
     * @param r1 the first source
     * @param r2 the second source to be compared with the first one.
     * @return the list of differences found, instances of {@link Difference};
     *        or <code>null</code> when some error occured.
     */
    public Difference[] computeDiff(Reader r1, Reader r2) throws IOException {
        if (options == null) {
            // blind fix of #144033, probably a deserialization issue?
            options = DiffModuleConfig.getDefault().getOptions();
        }
        return HuntDiff.diff(getLines(r1), getLines(r2), options);   
    }
    
    private String[] getLines(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        String line;
        List<String> lines = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return lines.toArray(new String[0]);
    }


    /**
     * @param options a new set of diff options
     */
    public void setOptions(Options options) {
        this.options = options;
    }

    public static class Options {
        /**
         * True to ignore leading and trailing whitespace when computing diff, false otherwise.
         */
        public boolean ignoreLeadingAndtrailingWhitespace;

        /**
         * True to ignore inner (not leading or trailing) whitespace when computing diff, false otherwise.
         */
        public boolean ignoreInnerWhitespace;

        /**
         * True to ignore changes in case.
         */
        public boolean ignoreCase;
    }
    
}
