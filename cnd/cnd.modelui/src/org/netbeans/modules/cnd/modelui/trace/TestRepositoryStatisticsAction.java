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

package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import java.util.Date;
import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 */

public class TestRepositoryStatisticsAction extends TestProjectActionBase {

    @Override
    public String getName() {
        return NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TestRepositoryStatistics"); //NOI18N
    }


    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {

        NotifyDescriptor nd = new NotifyDescriptor(
                NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TRS_Message"),
                NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TRS_Title"),
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION, NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.NO_OPTION);

        Object ret = DialogDisplayer.getDefault().notify(nd);
        if (ret.equals(JOptionPane.CANCEL_OPTION)) {
            return;
        }
        InputOutput io = IOProvider.getDefault().getIO("", false);
        io.select();
        OutputWriter out = io.getOut();
//        RepositoryStatistics.report(out, new Date().toString());
//        if (ret.equals(JOptionPane.YES_OPTION)) {
//            RepositoryStatistics.clear();
//        }
    }
}
