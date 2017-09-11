/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class IgnoreFileFilter implements FileFilter {

    private final FileObject buildContext;

    private final FileObject dockerfile;

    private final FileObject dockerignore;

    private final List<IgnorePattern> patterns = new ArrayList<>();

    public IgnoreFileFilter(FileObject buildContext, FileObject dockerfile, char separator) throws IOException {
        this.buildContext = buildContext;
        if (dockerfile != null) {
            this.dockerfile = dockerfile;
        } else {
            this.dockerfile = buildContext.getFileObject(DockerUtils.DOCKER_FILE);
        }
        this.dockerignore = buildContext.getFileObject(".dockerignore");
        if (dockerignore != null && dockerignore.isData()) {
            patterns.addAll(load(dockerignore, separator));
        }
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.equals(dockerfile) || pathname.equals(dockerignore)) {
            return true;
        }
        assert pathname.getAbsolutePath().startsWith(buildContext.getPath())
                && !pathname.equals(buildContext);
        String path = pathname.getAbsolutePath().substring(buildContext.getPath().length());
        if (path.startsWith(File.separator)) {
            path = path.substring(1);
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        return accept(path);
    }

    public boolean accept(String relativePath) {
        assert !relativePath.startsWith("/") && !relativePath.endsWith("/") && !relativePath.isEmpty();
        String path = relativePath;
        boolean include = true;
        for (IgnorePattern p : patterns) {
            if (p.matches(path)) {
                include = p.isNegative();
            }
        }
        return include;
    }

    private List<IgnorePattern> load(FileObject dockerignore, char separator) throws IOException {
        List<IgnorePattern> ret = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(dockerignore.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                // it seems dockerignore supports # comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                // FIXME exclusion supported since 1.7.0
                ret.add(IgnorePattern.compile(line, separator, true));
            }
        }
        return ret;
    }
}
