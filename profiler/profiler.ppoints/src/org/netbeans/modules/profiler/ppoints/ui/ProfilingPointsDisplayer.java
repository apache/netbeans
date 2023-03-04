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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilingPointsDisplayer_PpActiveMsg=Profiling Points Active in {0}",
    "ProfilingPointsDisplayer_NoActivePpsString=<No profiling points active for current configuration>",
    "ProfilingPointsDisplayer_ListAccessName=Active profiling points for the configuration"
})
public class ProfilingPointsDisplayer extends JPanel implements HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "ProfilingPointsDisplayer.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    
    private static ProfilingPointsDisplayer defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private DefaultListModel listModel;
    private JList list;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private ProfilingPointsDisplayer() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public static void displayProfilingPoints(Lookup.Provider project, ProfilingSettings settings) {
        ProfilingPointsDisplayer ppd = getDefault();
        ppd.setupDisplay(project, settings);

        final DialogDescriptor dd = new DialogDescriptor(ppd,
                                                         Bundle.ProfilingPointsDisplayer_PpActiveMsg(settings.getSettingsName()), 
                                                         true,
                                                         new Object[] { DialogDescriptor.OK_OPTION }, DialogDescriptor.OK_OPTION,
                                                         0, null, null);
        final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.pack();
        d.setVisible(true);

        ppd.cleanup();
    }

    private static ProfilingPointsDisplayer getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new ProfilingPointsDisplayer();
        }

        return defaultInstance;
    }

    private void cleanup() {
        listModel.removeAllElements();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.getAccessibleContext().setAccessibleName(Bundle.ProfilingPointsDisplayer_ListAccessName());
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(6);
        list.setCellRenderer(org.netbeans.modules.profiler.ppoints.Utils.getPresenterListRenderer());

        JScrollPane listScroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroll.setPreferredSize(new Dimension(405, listScroll.getPreferredSize().height));

        add(listScroll, BorderLayout.CENTER);
    }

    private void setupDisplay(Lookup.Provider project, ProfilingSettings settings) {
        List<ProfilingPoint> compatibleProfilingPoints = ProfilingPointsManager.getDefault()
                                                                               .getCompatibleProfilingPoints(project, settings,
                                                                                                             true);
        listModel.removeAllElements();

        if (compatibleProfilingPoints.size() == 0) {
            listModel.addElement(Bundle.ProfilingPointsDisplayer_NoActivePpsString());
        } else {
            for (ProfilingPoint profilingPoint : compatibleProfilingPoints) {
                listModel.addElement(profilingPoint);
            }
        }
    }
}
