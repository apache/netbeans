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

package org.netbeans.modules.ide.ergonomics;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.ide.ergonomics.fod.ConfigurationPanel;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Set of static useful methods.
 *
 * @author Pavel Flaska
 */
public class Utilities {

    private Utilities() {
    }

    public static final boolean featureNotFoundDialog(final FeatureInfo featureInfo, final String featureName) {
        String notFound = NbBundle.getMessage(Utilities.class, "LBL_FeatureNotFound");
        return featureDialog(featureInfo, notFound, featureName);
    }
    public static final boolean featureDialog(final FeatureInfo featureInfo, final String notFoundMessage, final String featureName) {
        final CountDownLatch called = new CountDownLatch(1);
        final boolean[] result = new boolean[] { false };
        final DialogDescriptor[] descriptor = new DialogDescriptor[1];
        final Callable<JComponent> call = () -> {
            result[0] = true;
            called.countDown();
            descriptor[0].setValue(DialogDescriptor.CLOSED_OPTION);
            return new JPanel();
        };
        descriptor[0] = Mutex.EVENT.readAccess(() -> {
            ConfigurationPanel panel = new ConfigurationPanel(featureName, call, featureInfo);
            return new DialogDescriptor(panel, notFoundMessage);
        });
        descriptor[0].setOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        if (!GraphicsEnvironment.isHeadless()) {
            Dialog d = DialogDisplayer.getDefault().createDialog(descriptor[0]);
            descriptor[0].addPropertyChangeListener((PropertyChangeEvent e) -> {
                d.setVisible(false);
                d.dispose();
            });
            d.setVisible(true);
        } else {
            try {
                called.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result[0];
    }
}
