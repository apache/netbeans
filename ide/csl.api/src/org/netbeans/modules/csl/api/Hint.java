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
package org.netbeans.modules.csl.api;

import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Wrapper around org.netbeans.spi.editor.hints.ErrorDescription
 *
 * @author Tor Norbye
 */
public class Hint {
    private final String description;
    private final List<HintFix> fixes;
    private final FileObject file;
    private final OffsetRange range;
    private final Rule rule;
    private int priority;
    
    //TODO: unclear whether range is @NonNull or @NullAllowed
    public Hint(@NonNull Rule rule, @NonNull String description, @NonNull FileObject file, OffsetRange range, @NullAllowed List<HintFix> fixes, int priority) {
        Parameters.notNull("rule", rule);
        Parameters.notNull("description", description);
        Parameters.notNull("file", file);
        
        this.rule = rule;
        this.description = description;
        this.file = file;
        this.range = range;
        this.fixes = fixes;
        this.priority = priority;
    }
    
    public @NonNull Rule getRule() {
        return this.rule;
    }

    public @NonNull String getDescription() {
        return description;
    }

    public @NonNull FileObject getFile() {
        return file;
    }

    public @CheckForNull List<HintFix> getFixes() {
        return fixes;
    }

    public OffsetRange getRange() {
        return range;
    }
    
    public int getPriority() {
        return priority;
    }
    
    @Override
    public String toString() {
        return "Description(desc=" + description + ",fixes=" + fixes + ")";
    }
}
