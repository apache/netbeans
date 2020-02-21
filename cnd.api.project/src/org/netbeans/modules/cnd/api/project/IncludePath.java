/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
