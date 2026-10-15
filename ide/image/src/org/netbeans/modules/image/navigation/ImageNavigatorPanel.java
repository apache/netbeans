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
package org.netbeans.modules.image.navigation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.image.ImageDataLoader;
import org.netbeans.modules.image.ImageDataObject;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;

/**
 *
 * @author jpeska
 */
@NavigatorPanel.Registrations({
    @NavigatorPanel.Registration(mimeType=ImageDataLoader.PNG_MIME_TYPE, displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType=ImageDataLoader.JPEG_MIME_TYPE, displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType=ImageDataLoader.BMP_MIME_TYPE, displayName="#Navigator_DisplayName"),
    @NavigatorPanel.Registration(mimeType=ImageDataLoader.GIF_MIME_TYPE, displayName="#Navigator_DisplayName")
})
public class ImageNavigatorPanel implements NavigatorPanel {

    /**
     * holds UI of this panel
     */
    private ImagePreviewPanel panelUI;
    /**
     * template for finding data in given context. Object used as example,
     * replace with your own data source, for example JavaDataObject etc
     */
    private static final Lookup.Template<ImageDataObject> MY_DATA = new Lookup.Template<>(ImageDataObject.class);
    /**
     * current context to work on
     */
    private Lookup.Result<ImageDataObject> currentContext;
    /**
     * listener to context changes
     */
    private LookupListener contextListener;
    /**
     * Listens for changes on image file.
     */
    private FileChangeListener fileChangeListener;
    private long lastSaveTime = -1;
    private DataObject currentDataObject;
    private static final RequestProcessor WORKER = new RequestProcessor(ImageNavigatorPanel.class.getName());

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImageNavigatorPanel.class, "Navigator_DisplayName");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(ImageNavigatorPanel.class, "Navigator_DisplayHint");
    }

    @Override
    public JComponent getComponent() {
        if (lastSaveTime == -1) {
            lastSaveTime = System.currentTimeMillis();
        }
        if (panelUI == null) {
            panelUI = new ImagePreviewPanel();
        }
        return panelUI;
    }

    @Override
    public void panelActivated(Lookup context) {
        // lookup context and listen to result to get notified about context changes
        currentContext = context.lookup(MY_DATA);
        currentContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection<? extends ImageDataObject> data = currentContext.allInstances();
        currentDataObject = getDataObject(data);
        if (currentDataObject == null) {
            return;
        }
        if (fileChangeListener == null) {
            fileChangeListener = new ImageFileChangeAdapter();
        }
        currentDataObject.getPrimaryFile().addFileChangeListener(fileChangeListener);
        setNewContent(currentDataObject);
    }

    @Override
    public void panelDeactivated() {
        currentContext.removeLookupListener(getContextListener());
        currentContext = null;
        if (currentDataObject != null) {
            currentDataObject.getPrimaryFile().removeFileChangeListener(fileChangeListener);
        }
        currentDataObject = null;
    }

    @Override
    public Lookup getLookup() {
        // go with default activated Node strategy
        return null;
    }

    private void setNewContent(final DataObject dataObject) {
        if (dataObject == null) {
            return;
        }

        WORKER.post(() -> {
            try {
                FileObject fileObject = dataObject.getPrimaryFile();
                if (fileObject == null) {
                    return;
                }
                try (InputStream inputStream = fileObject.getInputStream()) {
                    if (inputStream == null) {
                        return;
                    }
                    if (panelUI == null) {
                        getComponent();
                    }
                    try {
                        BufferedImage image = ImageIO.read(inputStream);
                        SwingUtilities.invokeLater(() -> panelUI.setImage(image));
                    } catch (IllegalArgumentException iaex) {
                        Logger.getLogger(ImageNavigatorPanel.class.getName()).info(NbBundle.getMessage(ImageNavigatorPanel.class, "ERR_IOFile"));
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ImageNavigatorPanel.class.getName()).info(NbBundle.getMessage(ImageNavigatorPanel.class, "ERR_IOFile"));
            }
        });

    }

    private DataObject getDataObject(Collection<? extends ImageDataObject> data) {
        if(data.isEmpty()) {
            return null;
        } else {
            return data.iterator().next();
        }
    }

    /**
     * Accessor for listener to context
     */
    private LookupListener getContextListener() {
        if (contextListener == null) {
            contextListener = new ContextListener();
        }
        return contextListener;
    }

    /**
     * Listens to changes of context and triggers proper action
     */
    private class ContextListener implements LookupListener {
        @Override
        public void resultChanged(LookupEvent ev) {
            currentDataObject = getDataObject(currentContext.allInstances());
            setNewContent(currentDataObject);
        }
    }

    private class ImageFileChangeAdapter extends FileChangeAdapter {

        @Override
        public void fileChanged(final FileEvent fe) {
            if (fe.getTime() > lastSaveTime) {
                lastSaveTime = System.currentTimeMillis();

                // Refresh image viewer
                SwingUtilities.invokeLater(() -> {
                    try {
                        currentDataObject = DataObject.find(fe.getFile());
                        setNewContent(currentDataObject);
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(ImageNavigatorPanel.class.getName()).info(NbBundle.getMessage(ImageNavigatorPanel.class, "ERR_DataObject"));
                    }
                });
            }
        }
    }
}
