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

package org.netbeans.modules.openfile;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.ExternalDropHandler;
import org.openide.windows.TopComponent;

/**
 *
 * @author S. Aubrecht
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.windows.ExternalDropHandler.class, position = 1000)
public class DefaultExternalDropHandler extends ExternalDropHandler {
    
    private static final Logger LOG =
            Logger.getLogger(DefaultExternalDropHandler.class.getName());
    private static final RequestProcessor RP
            = new RequestProcessor(DefaultExternalDropHandler.class);

    public boolean canDrop(DropTargetDragEvent e) {
        return canDrop( e.getCurrentDataFlavors() );
    }

    public boolean canDrop(DropTargetDropEvent e) {
        return canDrop( e.getCurrentDataFlavors() );
    }

    boolean canDrop( DataFlavor[] flavors ) {
        for( int i=0; null != flavors && i<flavors.length; i++ ) {
            if( DataFlavor.javaFileListFlavor.equals( flavors[i] )
                || getUriListDataFlavor().equals( flavors[i] ) ) {

                return true;
            }
        }
        return false;
    }

    public boolean handleDrop(DropTargetDropEvent e) {
        Transferable t = e.getTransferable();
        if( null == t )
            return false;
        List<File> fileList = getFileList( t );
        if ((fileList == null) || fileList.isEmpty()) {
            return false;
        }

        //#158473: Activate target TC to inform winsys in which mode new editor
        //component should be opened. It assumes that openFile opens new editor component
        //in some editor mode. If there would be problem with activating another TC first
        //then another way how to infrom winsys must be used.
        Component c = e.getDropTargetContext().getComponent();
        while (c != null) {
            if (c instanceof TopComponent) {
                ((TopComponent) c).requestActive();
                break;
            }
            c = c.getParent();
        }

        Object errMsg = null;
        if (fileList.size() == 1) {
            errMsg = openFile(fileList.get(0));
        } else {
            boolean hasSomeSuccess = false;
            List<String> fileErrs = null;
            for (File file : fileList) {
                String fileErr = openFile(file);
                if (fileErr == null) {
                    hasSomeSuccess = true;
                } else {
                    if (fileErrs == null) {
                        fileErrs = new ArrayList<String>(fileList.size());
                    }
                    fileErrs.add(fileErr);
                }
            }
            if (fileErrs != null) {         //some file could not be opened
                String mainMsgKey;
                if (hasSomeSuccess) {
                    mainMsgKey = "MSG_could_not_open_some_files";       //NOI18N
                } else {
                    mainMsgKey = "MSG_could_not_open_any_file";         //NOI18N
                }
                String mainMsg = NbBundle.getMessage(OpenFile.class, mainMsgKey);
                if (fileErrs == null) {
                    errMsg = mainMsg;
                } else {
                    JComponent msgPanel = new JPanel();
                    msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
                    msgPanel.add(new JLabel(mainMsg));
                    msgPanel.add(Box.createVerticalStrut(12));
                    for (String fileErr : fileErrs) {
                        msgPanel.add(new JLabel(fileErr));
                    }
                    errMsg = msgPanel;
                }
            }
        }
        if (errMsg != null) {
            showWarningMessageFileNotOpened(errMsg);
            return false;
        }
        return true;
    }

    static void showWarningMessageFileNotOpened(Object errMsg) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                        errMsg,
                        NotifyDescriptor.WARNING_MESSAGE));
    }

    List<File> getFileList( Transferable t ) {
        try {
            if( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) ) {
                //windows & mac
                try {
                    return (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (InvalidDnDOperationException ex) { // #212390
                    LOG.log(Level.FINE, null, ex);
                }
            }
            if (t.isDataFlavorSupported(getUriListDataFlavor())) {
                //linux
                String uriList = (String)t.getTransferData( getUriListDataFlavor() );
                return textURIListToFileList( uriList );
            }
        } catch( UnsupportedFlavorException ex ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ex );
        } catch( IOException ex ) {
            // Ignore. Can be just "Owner timed out" from sun.awt.X11.XSelection.getData.
            LOG.log(Level.FINE, null, ex);
        }
        return null;
    }

    /**
     * Opens the given file.
     *
     * If the file doesn't open in a reasonable time (2 seconds), let's assume
     * it will open successfully later (return null).
     *
     * @param  file  file to be opened
     * @return  {@code null} if the file was successfully opened;
     *          or a localized error message in case of failure
     */
    String openFile(final File file) {

        Callable<String> task = new Callable<String>() {
            @Override
            public String call() {
                File normalized = FileUtil.normalizeFile(file);
                FileObject fo = FileUtil.toFileObject(normalized);
                if (fo == null) {
                    return NbBundle.getMessage(OpenFile.class,
                            "MSG_FilePathTypeNotSupported", //NOI18N
                            file.toString());
                }
                return OpenFile.open(fo, -1);
            }
        };
        Future<String> future = RP.submit(task);
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // It seems the file is still opening, let's assume it'll succeed.
            return null;
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, null, e);
            return null;
        } catch (ExecutionException e) {
            // Should not happen, error message string should be returned.
            LOG.log(Level.INFO, null, e);
            return null;
        }
    }

    private static DataFlavor uriListDataFlavor;

    DataFlavor getUriListDataFlavor() {
        if( null == uriListDataFlavor ) {
            try {
                uriListDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
            } catch( ClassNotFoundException cnfE ) {
                //cannot happen
                throw new AssertionError(cnfE);
            }
        }
        return uriListDataFlavor;
    }

    List<File> textURIListToFileList( String data ) {
        List<File> list = new ArrayList<File>(1);
        for( StringTokenizer st = new StringTokenizer(data, "\r\n\u0000");
            st.hasMoreTokens();) {
            String s = st.nextToken();
            if( s.startsWith("#") ) {
                // the line is a comment (as per the RFC 2483)
                continue;
            }
            try {
                URI uri = new URI(s);
                File file = new File(uri);
                list.add( file );
            } catch( java.net.URISyntaxException e ) {
                // malformed URI
            } catch( IllegalArgumentException e ) {
                // the URI is not a valid 'file:' URI
            }
        }
        return list;
    }
}
