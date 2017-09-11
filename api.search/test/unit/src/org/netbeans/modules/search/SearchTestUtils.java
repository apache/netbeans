/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            List<TextDetail> details = new ArrayList<TextDetail>(i);
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
