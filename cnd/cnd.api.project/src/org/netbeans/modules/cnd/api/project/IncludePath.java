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
package org.netbeans.modules.cnd.api.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileSystem;

/**
 * Represents compiler predefined or user include path.
 * 
 * Allows to differ ordinary path and path to framework.
 *
 */
public class IncludePath {

    private final FSPath fsPath;
    private final boolean isFramework;

    private IncludePath(FSPath fsPath, boolean isFramework) {
        this.fsPath = fsPath;
        this.isFramework = isFramework;
    }

    public IncludePath(FSPath fsPath) {
        this(fsPath, false);
    }

    public IncludePath(FileSystem fileSystem, String absPath) {
        this(new FSPath(fileSystem, absPath), false);
    }

    public IncludePath(FileSystem fileSystem, String absPath, boolean isFramework) {
        this(new FSPath(fileSystem, absPath), isFramework);
    }

    public FSPath getFSPath() {
        return fsPath;
    }

    public FileSystem getFileSystem() {
        return fsPath.getFileSystem();
    }

    public boolean isFramework() {
        return isFramework;
    }

    public boolean ignoreSysRoot() {
        // all our paths are absolute and already modified based on sysroot prefix;
        // we ignore any extra work with this include path related to sys roots
        return true;
    }

    @Override
    public String toString() {
        if (isFramework()) {
            return getFSPath().getPath() + Driver.FRAMEWORK;
        } else {
            return getFSPath().getPath();
        }
    }

    public static IncludePath toIncludePath(FileSystem fileSystem, String path) {
        if (path.endsWith(Driver.FRAMEWORK)) {
            return new IncludePath(fileSystem, path.substring(0, path.length()-Driver.FRAMEWORK.length()), true);
        } else {
            return new IncludePath(fileSystem, path, false);
        }
    }

    public static List<IncludePath> toIncludePathList(FileSystem fileSystem, Collection<String> paths) {
        if (paths != null && paths.size() > 0) {
            List<IncludePath> result = new ArrayList<>(paths.size());
            for (String path : paths) {
                result.add(toIncludePath(fileSystem, path));
            }
            return result;
        }
        return Collections.<IncludePath>emptyList();
    }

    public static  List<String> toStringList(List<IncludePath> list) {
        List<String> res = new ArrayList<>(list.size());
        for (IncludePath p : list) {
            res.add(p.toString());
        }
        return res;
    }
}
