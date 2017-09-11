/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
