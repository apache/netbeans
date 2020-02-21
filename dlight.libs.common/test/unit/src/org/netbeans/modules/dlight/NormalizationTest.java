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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 */
public class NormalizationTest {
    
    private class RefData {
        public final String path;
        public final String normalized;
        public RefData(String path, String normalized) {
            this.path = path;
            this.normalized = normalized;
        }        
    }
    
    private void doTest(RefData[] refData) throws Exception {
        for (RefData rd : refData) {
            String norm = PathUtilities.normalizeUnixPath(rd.path);
            //assertEquals("normalizing " + rd.path, rd.normalized, norm);
            if (Utilities.isUnix() && rd.normalized.startsWith("/")) {
                String normalizedByFileUtils = FileUtil.normalizePath(rd.path);
//                // uncomment below for debugging
//                if (!normalizedByFileUtils.equals(rd.normalized)) {
//                    normalizedByFileUtils = FileUtil.normalizePath(rd.path);
//                    norm = PathUtilities.normalizeUnixPath(rd.path);
//                }                
                if (!normalizedByFileUtils.equals(rd.normalized)) {
                    String canonical = new File(rd.path).getCanonicalFile().getAbsolutePath();
                    String message = String.format("orig.path: %s, ref.data: %s, FileUtil.normalize: %s, canonical: %s",
                            rd.path, rd.normalized, normalizedByFileUtils, canonical);
                    assertTrue(message, false);
                }
            }
        }
    }
    
    @Test
    public void testNormalizeUnixPath() throws Exception {
        RefData[] data = new RefData[] {
            new RefData("/xxx/.../yyyy/..../zz//./../.yy", "/xxx/.../yyyy/..../.yy"),
            // the perverted one below  is normalized by FileUtil differenly on Mac on Solaris/Linux
            // new RefData("/xx/.../../../../....yy", "/....yy"),
            new RefData("/xx/../../../../..yy", "/..yy"),
            new RefData("/xx/../../../../....yy", "/....yy"),
            new RefData("/tmp/tmp..PaGjC", "/tmp/tmp..PaGjC"),
            new RefData("/aaa/bbb/../ccc", "/aaa/ccc"),
            new RefData("/aaa/bbb/../ccc/", "/aaa/ccc"),
            new RefData("/aaa/bbb/../../ddd", "/ddd"),
            new RefData("/xx/./././yy", "/xx/yy"),
            new RefData("/xx/../../../../yy", "/yy"),
            new RefData("/aa/bb/cc/dd/../../cc/dd", "/aa/bb/cc/dd"),
            new RefData("/x/y/z/.", "/x/y/z"),
            new RefData("../../xx/yy/.././.", "../../xx"),
            new RefData(".", ""),
            new RefData("..", ".."),
            new RefData("../..", "../.."),
        };
        doTest(data);
    }
}
