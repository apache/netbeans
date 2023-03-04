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

package org.netbeans.modules.bugzilla.kenai;

import java.util.List;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.query.BugzillaQuery;
import org.netbeans.modules.bugzilla.query.QueryController;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiQueryController extends QueryController {
    private final String product;
    private final boolean predefinedQuery;

    public KenaiQueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, String product, boolean predefinedQuery) {
        super(repository, query, urlParameters, false, false);
        this.product = product;
        this.predefinedQuery = predefinedQuery;
        postPopulate(urlParameters, false);
    }

    @Override
    public boolean providesMode(QueryMode mode) {
        // can't edit predefined quries, otherwise all posible modes accepted
        return !predefinedQuery || mode != QueryMode.EDIT;
    }
    
    @Override
    public void populate(String urlParameters) {
        if(BugzillaUtil.isNbRepository(getRepository())) {
            if(urlParameters == null) { // is new
                OwnerInfo ownerInfo = query.getOwnerInfo();
                if(ownerInfo == null) {
                    // XXX not sure why we need this - i'm going to keep it for now,
                    // doesn't seem to harm
                    Node[] selection = WindowManager.getDefault().getRegistry().getActivatedNodes();
                    ownerInfo = getRepository().getOwnerInfo(selection);
                }
                if(ownerInfo != null) {
                    StringBuilder sb = new StringBuilder();
                    String owner = ownerInfo.getOwner();
                    if(owner == null || !owner.equals(product) ) {
                        // XXX is this even possible?
                    } else {
                        sb.append("product=");                                  // NOI18N
                        sb.append(owner);
                        List<String> data = ownerInfo.getExtraData();
                        if(data != null && data.size() > 0) {
                            sb.append("&component=");                           // NOI18N
                            sb.append(data.get(0));
                        }
                        urlParameters = sb.toString();

                        // select only if owner awailable
                        selectFirstProduct();
                    }
                }
                if(urlParameters == null) {
                    urlParameters = "product=" + product;                       // NOI18N
                }
            }

            super.populate(urlParameters);
        } else {
            super.populate(urlParameters);
            disableProduct();
            selectFirstProduct();
        }
    }

    @Override
    protected void enableFields(boolean bl) {
        super.enableFields(bl);

        if(predefinedQuery) {
            // override - for predefined kenai queries are those always disabled
            panel.modifyButton.setEnabled(false);
            panel.removeButton.setEnabled(false);
        }
    }

    @Override
    protected void openIssue(BugzillaIssue issue) {
        if(issue != null) {
            if(!checkIssueProduct(issue)) {
                return;
            }
        }
        super.openIssue(issue);
    }

    @Override
    protected void onCloneQuery () {
        String p = getUrlParameters(false);
        BugzillaQuery q = new KenaiQuery(null, getRepository(), p, product, false, false);
        BugzillaUtil.openQuery(q);
    }

    private boolean checkIssueProduct(BugzillaIssue issue) {
        String issueProduct = issue.getRepositoryFieldValue(IssueField.PRODUCT);
        if(!issueProduct.equals(product)) {
            Confirmation dd = new DialogDescriptor.Confirmation(
                                NbBundle.getMessage(
                                    KenaiQueryController.class,
                                    "MSG_WrongProjectWarning",
                                    new Object[] {issue.getID(), issueProduct}),
                                Confirmation.YES_NO_OPTION);
            return DialogDisplayer.getDefault().notify(dd) ==  Confirmation.YES_OPTION;
        }
        return true;
    }

}
