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
package org.netbeans.modules.autoupdate.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

final class LocallDownloadDnD implements DropTargetListener {

    private final LocalDownloadSupport localDownloadSupport;
    private final LocallyDownloadedTableModel model;
    private final PluginManagerUI outer;
    private DataFlavor accept;
    private Object value;

    LocallDownloadDnD(
        LocalDownloadSupport localDownloadSupport, 
        LocallyDownloadedTableModel model,
        PluginManagerUI outer
    ) {
        this.model = model;
        this.outer = outer;
        this.localDownloadSupport = localDownloadSupport;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        accept = null;
        value = null;
        for (DataFlavor dataFlavor : dtde.getCurrentDataFlavors()) {
            Object obj = null;
            try {
                obj = dtde.getTransferable().getTransferData(dataFlavor);
            } catch (Exception ex) {
                continue;
            }
            if (dataFlavor.isFlavorJavaFileListType()) {
                accept = dataFlavor;
                value = obj;
                break;
            }
            if ("text".equals(dataFlavor.getPrimaryType()) && "uri-list".equals(dataFlavor.getSubType()) && dataFlavor.getRepresentationClass() == String.class) {
                accept = dataFlavor;
                value = (String) obj;
                break;
            }
        }
        if (accept != null) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (accept == null) {
            dtde.dropComplete(false);
            return;
        }
        try {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            List<File> files = null;
            if (value instanceof String) {
                files = new ArrayList<File>();
                for (String v : ((String) value).split("\n")) {
                    File f = Utilities.toFile(new URI(((String) v).trim()));
                    assert f.exists() : "File shall exist: " + f;
                    files.add(f);
                }
            }
            if (value instanceof List) {
                files = NbCollections.checkedListByCopy((List) value, File.class, true);
            }
            if (files != null) {
                UnitTab lt = outer.findTabForModel(model);
                assert lt != null;
                final Map<String, Boolean> state = UnitCategoryTableModel.captureState(model.getUnits());
                localDownloadSupport.addUpdateUnits(files.toArray(new File[0]));
                lt.updateTab(state);
                outer.setSelectedTab(lt);
            }
            dtde.dropComplete(true);
        } catch (Exception ex) {
            dtde.dropComplete(false);
            Exceptions.printStackTrace(ex);
        }
    }
}
