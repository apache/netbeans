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

package org.netbeans.modules.bugzilla.query;

import javax.swing.ListModel;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugzilla.TestConstants;
import org.netbeans.modules.bugzilla.TestUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 *
 * @author tomas
 */
public class QueryTestUtil implements TestConstants, QueryConstants {
    public static void selectTestProject(final BugzillaQuery q) {
        QueryPanel qp = (QueryPanel) q.getController().getComponent(QueryMode.EDIT);
        ListModel model = qp.productList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            QueryParameter.ParameterValue pv = (ParameterValue) model.getElementAt(i);
            if (pv.getValue().equals(TEST_PROJECT)) {
                qp.productList.setSelectedIndex(i);
                break;
            }
        }
    }

    public static BugzillaRepository getRepository() {
        BugzillaRepository repo = TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
        repo.ensureCredentials();
        return repo;
    }

}
