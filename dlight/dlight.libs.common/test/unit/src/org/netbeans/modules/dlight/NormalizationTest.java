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
