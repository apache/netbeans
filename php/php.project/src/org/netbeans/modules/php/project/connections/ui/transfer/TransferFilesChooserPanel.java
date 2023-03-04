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

package org.netbeans.modules.php.project.connections.ui.transfer;

import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;

@SuppressWarnings("serial")
public abstract class TransferFilesChooserPanel extends JPanel {

    /**
     * @param listener listener for changes in transfer files selection
     */
    public abstract void addChangeListener(TransferFilesChangeListener listener);

    /**
     * @param listener listener for changes in transfer files selection
     */
    public abstract void removeChangeListener(TransferFilesChangeListener listener);

    /**
     * @return selected transfer files
     */
    public abstract Set<TransferFile> getSelectedFiles();

    /**
     * @return panel for the transfer files
     */
    public abstract TransferFilesChooserPanel getEmbeddablePanel();

    /**
     * @return {@code true} if the panel has any files to transfer/show
     */
    public abstract boolean hasAnyTransferableFiles();

    public interface TransferFilesChangeListener {

        void selectedFilesChanged();

        /**
         * @throws UnsupportedOperationException if it is not supported by the panel
         */
        void filterChanged();
    }
}
