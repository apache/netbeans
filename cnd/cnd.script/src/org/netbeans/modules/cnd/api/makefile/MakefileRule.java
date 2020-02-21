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
package org.netbeans.modules.cnd.api.makefile;

import java.util.Collections;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 */
public final class MakefileRule extends MakefileElement {

    private final List<String> targets;
    private final List<String> prereqs;

    /*package*/ MakefileRule(
            FileObject fileObject, int startOffset, int endOffset,
            List<String> targets, List<String> prereqs) {
        super(Kind.RULE, fileObject, startOffset, endOffset);
        this.targets = Collections.unmodifiableList(targets);
        this.prereqs = Collections.unmodifiableList(prereqs);
    }

    public List<String> getTargets() {
        return targets;
    }

    public List<String> getPrerequisites() {
        return prereqs;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String target : targets) {
            buf.append(target).append(' ');
        }
        buf.append(':');
        for (String prereq : prereqs) {
            buf.append(' ').append(prereq);
        }
        return buf.toString();
    }
}
