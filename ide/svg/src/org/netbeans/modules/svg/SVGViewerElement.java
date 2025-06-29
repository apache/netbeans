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
package org.netbeans.modules.svg;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.svg.toolbar.SVGViewerToolbar;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Christian Lenz
 */
@MultiViewElement.Registration(
    displayName = "#LBL_SVGViewer",
    iconBase = "org/netbeans/modules/svg/resources/svgLogo.png",
    mimeType = "image/svg+xml",
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "SVGViewer",
    position = 1100
)
@Messages("LBL_SVGViewer=Preview")
public class SVGViewerElement implements MultiViewElement {

    static {
        // JSVG loader/parser is logging with SEVERE level which would land in the NB exception reporter
        Logger.getLogger("com.github.weisj.jsvg").setLevel(Level.OFF);
    }

    private static final Logger LOG = Logger.getLogger(SVGViewerElement.class.getName());

    private final SVGDataObject dataObject;
    private final Lookup lookup;
    private transient JToolBar toolbar;
    private transient JComponent component;
    private transient MultiViewElementCallback callback;

    /**
     * Component showing SVG.
     */
    private transient JPanel viewer;

    private final SVGLoader svgLoader = new SVGLoader();
    private final SVGPanel svgPanel = new SVGPanel();
    private SVGDocument svgDocument = null;

    private int imageHeight = 0;
    private int imageWidth = 0;
    private long imageSize = -1;

    /**
     * Scale of SVG.
     */
    private double scale = 1.0D;

    /**
     * Increase/decrease factor.
     */
    private final double changeFactor = Math.sqrt(2.0D);

    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override
        public void fileChanged(FileEvent fe) {
            updateView();
        }
    };

    @SuppressWarnings({"this-escape", "LeakingThisInConstructor"})
    public SVGViewerElement(Lookup lookup) {
        this.dataObject = lookup.lookup(SVGDataObject.class);
        this.lookup = new ProxyLookup(
            Lookups.proxy(() -> dataObject.getLookup()),
            Lookups.singleton(this)
        );
    }

    @Override
    public JComponent getVisualRepresentation() {
        if (component == null) {
            viewer = new JPanel();
            component = viewer;
        }

        return component;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = new SVGViewerToolbar().createToolbar(this);
        }

        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return callback.createDefaultActions();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {
        dataObject.getPrimaryFile().addFileChangeListener(fcl);

        updateView();
    }

    @Override
    public void componentClosed() {
        dataObject.getPrimaryFile().removeFileChangeListener(fcl);
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    private void addMouseWheelListenerToViewer(JScrollPane scrollPane) {
        svgPanel.addMouseWheelListener(e -> {
            double oldScale = scale;

            // Point in scrolled pane
            Point visiblePoint = e.getPoint();

            // "Picturepixel"
            Point markedPoint = new Point(
                (int) (visiblePoint.getX() / oldScale),
                (int) (visiblePoint.getY() / oldScale));

            int clicks = e.getWheelRotation();
            int clicks_abs = Math.abs(clicks);
            for (int i = 0; i < clicks_abs; i++) {
                if (clicks < 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }

            double newScale = scale;

            Point markedPointInRealSpace = new Point(
                (int) (markedPoint.getX() * newScale),
                (int) (markedPoint.getY() * newScale)
            );

            Rectangle r = scrollPane.getViewport().getViewRect();

            r.setLocation(markedPointInRealSpace);
            r.translate(-r.width / 2, -r.height / 2);

            svgPanel.scrollRectToVisible(r);
        });
    }

    private void showEmptyMessage(String message) {
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(Color.RED);

        viewer.removeAll();
        viewer.setLayout(new BorderLayout());
        viewer.add(label, BorderLayout.CENTER);
        viewer.revalidate();
        viewer.repaint();
    }

    private void updateView() {
        FileObject fo = dataObject.getPrimaryFile();

        if (fo == null) {
            return;
        }

        if (viewer == null) {
            return;
        }

        svgDocument = svgLoader.load(fo.toURL());

        if (svgDocument == null) {
            showEmptyMessage(NbBundle.getMessage(SVGViewerElement.class, "ERR_SVGDocument"));

            return;
        }

        FloatSize size = svgDocument.size();
        int width = (int) size.width;
        int height = (int) size.height;

        imageWidth = width;
        imageHeight = height;

        imageSize = fo.getSize();

        if (width <= 0 || height <= 0) {
            LOG.log(Level.WARNING, "Invalid SVG dimensions: width={0}, height={1}", new Object[]{width, height});

            return;
        }

        svgPanel.setPreferredSize(new Dimension(width, height));
        svgPanel.setSVGDocument(svgDocument);

        JScrollPane scrollPane = new JScrollPane(svgPanel);

        addMouseWheelListenerToViewer(scrollPane);

        viewer.removeAll();
        viewer.setLayout(new BorderLayout());
        viewer.add(scrollPane, BorderLayout.CENTER);
        viewer.revalidate();
        viewer.repaint();
    }

    public long getImageSize() {
        return imageSize;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * Perform zoom with specific proportion.
     *
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */
    public void customZoom(int fx, int fy) {
        double oldScale = scale;

        scale = (double) fx / (double) fy;
        if (!isNewSizeOK()) {
            scale = oldScale;

            return;
        }

        svgPanel.setScale(scale);

        resizePanel();
    }

    /**
     * Draws zoom in scaled image.
     */
    public void zoomIn() {
        scale *= changeFactor;

        if (isNewSizeOK()) {
            svgPanel.setScale(scale);

            resizePanel();
        } else {
            scale /= changeFactor;
        }
    }

    /**
     * Draws zoom out scaled image.
     */
    public void zoomOut() {
        scale /= changeFactor;

        if (isNewSizeOK()) {
            svgPanel.setScale(scale);

            resizePanel();
        } else {
            scale *= changeFactor;
        }
    }

    public void changeBackground(BackgroundMode bgMode) {
        svgPanel.setBackgroundMode(bgMode);

        viewer.revalidate();
        viewer.repaint();
    }

    /**
     * Resizes panel.
     */
    private void resizePanel() {
        int newWidth = (int) (svgDocument.size().getWidth() * scale);
        int newHeight = (int) (svgDocument.size().getHeight() * scale);

        svgPanel.setBounds(0, 0, newWidth, newHeight);
        svgPanel.setPreferredSize(new Dimension(newWidth, newHeight));

        viewer.revalidate();
        viewer.repaint();
    }

    /**
     * Tests new size of image. If image is smaller than minimum size(1x1)
     * zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        return (scale * svgDocument.size().getWidth() > 1)
            && (scale * svgDocument.size().getHeight() > 1);
    }
}
