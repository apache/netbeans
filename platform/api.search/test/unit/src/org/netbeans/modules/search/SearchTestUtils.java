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
package org.netbeans.modules.search;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.search.SearchPattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * Utilities for creation of search-related objects for testing.
 *
 * @author jhavlin
 */
public final class SearchTestUtils {

    /**
     * Create a result model containing a single matching object. The matching
     * file is an in-memory empty data file named test.txt.
     */
    public static ResultModel createResultModelWithOneMatch()
            throws IOException {

        ResultModel rm = new ResultModel(new BasicSearchCriteria(), null, null);
        FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData(
                "test.txt");                                            //NOI18N
        rm.objectFound(fo, Charset.defaultCharset(),
                Collections.<TextDetail>emptyList());
        return rm;
    }

    /**
     * Create result model with 10 matching files. The first matching file has 1
     * text detail, the second has 2 details, the third has 3 details etc.
     */
    public static ResultModel createResultModelWithSampleData(boolean replace)
            throws IOException {
        ResultModel rm = new ResultModel(new BasicSearchCriteria(),
                replace ? "replacement" : null, null);
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        SearchPattern sp = SearchPattern.create("test", false, false, false);
        for (int i = 0; i < 10; i++) {
            FileObject fo = root.createData(i + ".txt");                //NOI18N
            DataObject dob = DataObject.find(fo);
            List<TextDetail> details = new ArrayList<>(i);
            for (int j = 0; j < i + 1; j++) {
                TextDetail td = new TextDetail(dob, sp);
                details.add(td);
            }
            rm.objectFound(fo, Charset.defaultCharset(),
                    details);
        }
        return rm;
    }
}
