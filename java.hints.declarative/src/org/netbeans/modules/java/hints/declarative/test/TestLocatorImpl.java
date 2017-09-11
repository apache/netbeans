/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative.test;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.spi.gototest.TestLocator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=TestLocator.class)
public class TestLocatorImpl implements TestLocator {

    public boolean appliesTo(FileObject fo) {
        String mimeType = FileUtil.getMIMEType(fo);

        if ("text/x-javahints".equals(mimeType)) {
            return true;
        }
        
        if ("text/x-javahintstest".equals(mimeType)) {
            return true;
        }

        return false;
    }

    public boolean asynchronous() {
        return false;
    }

    public LocationResult findOpposite(FileObject fo, int caretOffset) {
        String mimeType = FileUtil.getMIMEType(fo);

        if ("text/x-javahints".equals(mimeType)) {
            FileObject test = findOpposite(fo, true);

            if (test != null) {
                return new LocationResult(test, -1);
            } else {
                return new LocationResult("Test not found.");
            }
        }

        if ("text/x-javahintstest".equals(mimeType)) {
            FileObject rule = findOpposite(fo, false);

            if (rule != null) {
                return new LocationResult(rule, -1);
            } else {
                return new LocationResult("Rule file not found.");
            }
        }

        throw new IllegalArgumentException();
    }

    public void findOpposite(FileObject fo, int caretOffset, LocationListener callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FileType getFileType(FileObject fo) {
        String mimeType = FileUtil.getMIMEType(fo);

        if ("text/x-javahints".equals(mimeType)) {
            return FileType.TESTED;
        }

        if ("text/x-javahintstest".equals(mimeType)) {
            return FileType.TEST;
        }

        return FileType.NEITHER;
    }

    static @CheckForNull FileObject findOpposite(FileObject rule, boolean toTest) {
        ClassPath cp = ClassPath.getClassPath(rule, ClassPath.SOURCE);
        String resourceName = cp != null ? cp.getResourceName(rule) : null;

        if (resourceName == null) {
            Logger.getLogger(TestLocatorImpl.class.getName()).log(Level.FINE, "cp==null or rule file cannot be found on its own source cp");
            return null;
        }

        String testFileName = resourceName.substring(0, resourceName.lastIndexOf('.')) + (toTest ? ".test" : ".hint");

        FileObject testFile = cp.findResource(testFileName);

        if (testFile == null) {
            URL[] sr;

            if (toTest) {
                sr = UnitTestForSourceQuery.findUnitTests(cp.findOwnerRoot(rule));
            } else {
                sr = UnitTestForSourceQuery.findSources(cp.findOwnerRoot(rule));
            }
            
            for (URL testRoot : sr) {
                FileObject testRootFO = URLMapper.findFileObject(testRoot);

                if (testRootFO != null) {
                    testFile = testRootFO.getFileObject(testFileName);

                    if (testFile != null) {
                        break;
                    }
                }
            }
        }

        return testFile;
    }

}
