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
package org.netbeans.modules.php.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;

public final class MessDetectorParams {

    private List<String> ruleSets;
    private FileObject ruleSetFile;
    private String options;

    public List<String> getRuleSets() {
        return Collections.unmodifiableList(ruleSets);
    }

    MessDetectorParams setRuleSets(List<String> ruleSets) {
        this.ruleSets = new ArrayList<>(ruleSets);
        return this;
    }

    public FileObject getRuleSetFile() {
        return ruleSetFile;
    }

    MessDetectorParams setRuleSetFile(FileObject ruleSetFile) {
        this.ruleSetFile = ruleSetFile;
        return this;
    }

    public String getOptions() {
        return options;
    }

    MessDetectorParams setOptions(String options) {
        this.options = options;
        return this;
    }
}
