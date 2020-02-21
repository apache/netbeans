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
package org.netbeans.modules.remote.impl.fs.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.openfile.OpenFile;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.ExternalDropHandler;
import org.openide.windows.TopComponent;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=ExternalDropHandler.class, position = 500)
public class RemoteExternalDropHandler extends ExternalDropHandler {

    private static final RequestProcessor RP = new RequestProcessor(RemoteExternalDropHandler.class);

    private final DataFlavor daoFlavour;

    public RemoteExternalDropHandler() {
        daoFlavour = createDataObjectFlavuor();
    }

    private static DataFlavor createDataObjectFlavuor() {
        DataFlavor flavour;
        try {
            flavour = new DataFlavor("application/x-java-openide-dataobjectdnd;class=org.openide.loaders.DataObject"); // NOI18N
        } catch (ClassNotFoundException ex) {
            flavour = null;
            RemoteLogger.info(ex);
        }
        return flavour;
    }

    @Override
    public boolean canDrop(DropTargetDragEvent e) {
        if (canDrop(e.getCurrentDataFlavors())) {
            Transferable t = e.getTransferable();
            if (t != null) {
                FileObject fo = getFileObject(t);
                return fo instanceof RemoteFileObject;
            }
        }
        return false;
    }

    @Override
    public boolean canDrop(DropTargetDropEvent e) {
        if (canDrop(e.getCurrentDataFlavors())) {
            Transferable t = e.getTransferable();
            if (t != null) {
                FileObject fo = getFileObject(t);
                return fo instanceof RemoteFileObject;
            }
        }
        return false;
    }

    private boolean canDrop(DataFlavor[] flavours) {
        for (DataFlavor f : flavours) {
            if (f.equals(daoFlavour)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handleDrop(DropTargetDropEvent e) {
        Transferable t = e.getTransferable();
        if (t != null) {
            FileObject fo = getFileObject(t);
            //URI uri = fo.toURI();
            //if (uri != null && uri.getScheme().equals("rfs")) { //NOI18N
            //if (fo != null && fo.getAttribute(RemoteFileObject.IMPLEMENTOR_ATTRIBUTE) instanceof RemoteFileObjectBase) {
            if (fo instanceof RemoteFileObject) {
                Component c = e.getDropTargetContext().getComponent();
                while (c != null) {
                    if (c instanceof TopComponent) {
                        ((TopComponent) c).requestActive();
                        break;
                    }
                    c = c.getParent();
                }
                Object errMsg = openFile(fo);
                if (errMsg == null) {
                    return true;
                } else {
                    showWarningMessageFileNotOpened(errMsg);
                    return false;
                }
            }
        }
        return false;
    }

    static void showWarningMessageFileNotOpened(Object errMsg) {
        NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                errMsg, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }

    private FileObject getFileObject(Transferable t) {
        if (t.isDataFlavorSupported(daoFlavour)) {
            try {
                Object d = t.getTransferData(daoFlavour);
                if (d instanceof DataObject) {
                    DataObject dao = (DataObject) d;
                    return dao.getPrimaryFile();
                }
            } catch (InvalidDnDOperationException | UnsupportedFlavorException | IOException ex) {
                RemoteLogger.info(ex);
            }
        }
        return null;
    }

    /**
     * Opens the given file.
     *
     * If the file doesn't open in a reasonable time (2 seconds), let's assume
     * it will open successfully later (return null).
     *
     * @param file file to be opened
     * @return {@code null} if the file was successfully opened; or a localized
     * error message in case of failure
     */
    String openFile(final FileObject fo) {

        Callable<String> task = new Callable<String>() {
            @Override
            public String call() {
                return OpenFile.open(fo, -1);
            }
        };
        Future<String> future = RP.submit(task);
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // It seems the file is still opening, let's assume it'll succeed.
            return null;
        } catch (InterruptedException | ExecutionException e) {
            RemoteLogger.info(e);
            return null;
        }
    }
}
