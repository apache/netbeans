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

package org.openide.filesystems;

import java.util.Set;
import javax.swing.Action;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Stub for backwards compatibility. The real implementation which bridges
 * in system actions moved to openide.filesystems.compat8
 * 
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
 */
final class FileExtrasLkp extends AbstractLookup {
    final FileSystem fs;
    private final InstanceContent ic;
    final Set<FileObject> set;

    public FileExtrasLkp(FileSystem fs, Set<FileObject> set) {
        this(fs, new InstanceContent(), set);
    }
    private FileExtrasLkp(FileSystem fs, InstanceContent content, Set<FileObject> set) {
        super(content);
        this.fs = fs;
        this.ic = content;
        this.set = set;
    }

}
