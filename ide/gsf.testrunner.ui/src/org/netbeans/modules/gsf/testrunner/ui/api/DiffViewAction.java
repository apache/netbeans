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
package org.netbeans.modules.gsf.testrunner.ui.api;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * An action for viewing differences between two strings, e.g. an assert_equals
 * failure.
 *
 * @author Erno Mononen
 */
public final class DiffViewAction extends AbstractAction {

    private final Trouble.ComparisonFailure comparisonFailure;

    public DiffViewAction(Trouble.ComparisonFailure comparisonFailure) {
        this.comparisonFailure = comparisonFailure;
        if (this.comparisonFailure == null) {
            setEnabled(false);
        }
    }

    public DiffViewAction(Testcase testcase) {
        this(testcase.getTrouble() != null ? testcase.getTrouble().getComparisonFailure() : null);
    }

    @Override
    @NbBundle.Messages("LBL_ViewDiff=&View Difference")
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return Bundle.LBL_ViewDiff();
        }
        return super.getValue(key);
    }

    @NbBundle.Messages({
            "LBL_Expected=Expected",
            "LBL_Actual=Actual",
            "LBL_OK=OK",
            "LBL_Diff=Differences"})
    @Override
    public void actionPerformed(ActionEvent e) {

        StringComparisonSource expected =
                new StringComparisonSource(
                Bundle.LBL_Expected(),
                comparisonFailure.getExpected(),
                comparisonFailure.getMimeType());

        StringComparisonSource actual =
                new StringComparisonSource(
                Bundle.LBL_Actual(),
                comparisonFailure.getActual(),
                comparisonFailure.getMimeType());


        try {
            JComponent diffComponent = DiffController.create(expected, actual).getJComponent();
            diffComponent.setPreferredSize(new Dimension(500, 250));
            JButton ok = new JButton(Bundle.LBL_OK());
            final DialogDescriptor descriptor = new DialogDescriptor(
                    diffComponent,
                    Bundle.LBL_Diff(),
                    true,
                    new Object[]{ok},
                    ok,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);

            Dialog dialog = null;
            try {
                dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
            } finally {
                if (dialog != null) {
                    dialog.dispose();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static class StringComparisonSource extends StreamSource {

        private final String name;
        private final String source;
        private final String mimeType;

        public StringComparisonSource(String name, String source, String mimeType) {
            this.name = name;
            this.source = source;
            this.mimeType = mimeType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return getName();
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }

        @Override
        public Reader createReader() throws IOException {
            return new StringReader(source);
        }

        @Override
        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}

