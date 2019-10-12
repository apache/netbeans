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

package org.netbeans.modules.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Top component providing a viewer for images.
 * @author Petr Hamernik, Ian Formanek, Lukas Tadial
 * @author Marian Petras
 */
public class ImageViewer extends CloneableTopComponent {

    /** Serialized version UID. */
    static final long serialVersionUID =6960127954234034486L;
    
    /** <code>ImageDataObject</code> which image is viewed. */
    private ImageDataObject storedObject;
    
    /** Viewed image. */
    private NBImageIcon storedImage;
    
    /** Component showing image. */
    private JPanel panel;
    
    /** Scale of image. */
    private double scale = 1.0D;
    
    /** On/off grid. */
    private boolean showGrid = false;
    
    /** Increase/decrease factor. */
    private final double changeFactor = Math.sqrt(2.0D);
    
    /** Grid color. */
    private final Color gridColor = Color.black;
    
    /** Listens for name changes. */
    private PropertyChangeListener nameChangeL;
    
    /** collection of all buttons in the toolbar */
    private final Collection<JButton> toolbarButtons = new ArrayList<JButton>(11);
    
    private Component view;
    
    private static final RequestProcessor RP = new RequestProcessor("Image loader", 1, true);
    
    private RequestProcessor.Task loadImageTask;
    private int imageHeight = 0;
    private int imageWidth = 0;
    private long imageSize = -1;
    private final DecimalFormat formatter = new DecimalFormat("#.##"); //NOI18N
    
    /** Default constructor. Must be here, used during de-externalization */
    public ImageViewer () {
        super();
    }
    
    /** Create a new image viewer.
     * @param obj the data object holding the image
     */
    public ImageViewer(ImageDataObject obj) {
        initialize(obj);
    }
    
    /** Overriden to explicitely set persistence type of ImageViewer
     * to PERSISTENCE_ONLY_OPENED */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    /** Reloads icon. */
    protected void reloadIcon() {
        resizePanel();
        panel.repaint();
    }
    
    /** Initializes member variables and set listener for name changes on DataObject. */
    private void initialize(ImageDataObject obj) {
        TopComponent.NodeName.connect (this, obj.getNodeDelegate ());
        setToolTipText(FileUtil.getFileDisplayName(obj.getPrimaryFile()));
        
        storedObject = obj;
        
        FileObject imageFile = storedObject.getPrimaryFile();
        
        if (imageFile.isValid()) {
            imageSize = imageFile.getSize();
        } else {
            imageSize = -1;
        }
            
        // force closing panes in all workspaces, default is in current only
        setCloseOperation(TopComponent.CLOSE_EACH);
        
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImageViewer.class, "ACS_ImageViewer"));        
        
        nameChangeL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) ||
                DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                    updateNameInEDT();
                }
            }
        };
        
        obj.addPropertyChangeListener(WeakListeners.propertyChange(nameChangeL, obj));        
        setFocusable(true);
         /* try to load the image: */
        loadImage(storedObject);
    }

    /**
     * Updates name in the Event Dispatch Thread.
     * @see Bug #181283
     */
    private void updateNameInEDT() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                updateName();
            }
        });
    }
    
    /**
     */
    private Component createImageView() {
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                    storedImage.getImage(),
                    0,
                    0,
                    (int)(getScale () * storedImage.getIconWidth ()),
                    (int)(getScale () * storedImage.getIconHeight ()),
                    0,
                    0,
                    storedImage.getIconWidth(),
                    storedImage.getIconHeight(),
                    this
                );

                if(showGrid) {
                    int x = (int)(getScale () * storedImage.getIconWidth ());
                    int y = (int)(getScale () * storedImage.getIconHeight ());

                    double gridDistance = getScale();

                    if(gridDistance < 2) 
                        // Disable painting of grid if no image pixels would be visible.
                        return;

                    g.setColor(gridColor);

                    double actualDistance = gridDistance;
                    for(int i = (int)actualDistance; i < x ;actualDistance += gridDistance, i = (int)actualDistance) {
                        g.drawLine(i,0,i,(y-1));
                    }

                    actualDistance = gridDistance;
                    for(int j = (int)actualDistance; j < y; actualDistance += gridDistance, j = (int)actualDistance) {
                        g.drawLine(0,j,(x-1),j);
                    }
                }

            }

        };
        // vlv: print
        panel.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        panel.putClientProperty("print.name", getToolTipText()); // NOI18N

        storedImage.setImageObserver(panel);
        panel.setPreferredSize(new Dimension(storedImage.getIconWidth(), storedImage.getIconHeight() ));

        final JScrollPane scrollPane = new JScrollPane(panel);
        
        MouseWheelListener mwl = new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldScale = getScale();
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
    
                double newScale = getScale();
                
                Point markedPointInRealSpace = new Point(
                        (int) (markedPoint.getX() * newScale),
                        (int) (markedPoint.getY() * newScale)
                        );
                
                Rectangle r = scrollPane.getViewport().getViewRect();
                
                r.setLocation(markedPointInRealSpace);
                r.translate(-r.width / 2, - r.height / 2);
                
                panel.scrollRectToVisible(r);

            }
        };
    
        panel.addMouseWheelListener(mwl);
        
        class DragHandler implements MouseListener, MouseMotionListener {
            Point startDragPos = null;
            Point scrollPaneStartPos = null;
            
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == 2) {
                    startDragPos = e.getPoint();
                    scrollPaneStartPos = scrollPane.getViewport().getViewPosition();
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               if(e.getButton() == 2) {
                    startDragPos = null;
                    scrollPaneStartPos = null;
                    panel.setCursor(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if(startDragPos != null) {
                    Point newPos = e.getPoint();
                    int delta_x = newPos.x - startDragPos.x;
                    int delta_y = newPos.y - startDragPos.y;
                    
                    Point scrollPanePos = scrollPaneStartPos.getLocation();
                    scrollPanePos.translate(-delta_x, -delta_y);
                    Dimension viewDim = scrollPane.getViewport().getViewRect().getSize();
                    Dimension contentsDim = scrollPane.getViewport().getViewSize();
                    int maxX = contentsDim.width - viewDim.width;
                    int maxY = contentsDim.height - viewDim.height;
                    if(scrollPanePos.x < 0) {
                        scrollPanePos.x = 0;
                    } else if(scrollPanePos.x > maxX) {
                        scrollPanePos.x = maxX;
                    }
                    if(scrollPanePos.y < 0) {
                        scrollPanePos.y = 0;
                    } else if(scrollPanePos.y > maxY) {
                        scrollPanePos.y = maxY;
                    }
                    scrollPane.getViewport().setViewPosition(scrollPanePos);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        }
        
        DragHandler dh = new DragHandler();
        
        panel.addMouseListener(dh);
        panel.addMouseMotionListener(dh);
        
        return scrollPane;
    }
    
    /**
     */
    private Component createMessagePanel(final String msg) {
        JPanel msgPanel = new JPanel(new java.awt.GridBagLayout());
        msgPanel.add(new JLabel(msg),
                     new java.awt.GridBagConstraints(
                             0,    0,                  //gridx, gridy
                             1,    1,                  //gridwidth, gridheight
                             1.0d, 1.0d,               //weightx, weighty
                             java.awt.GridBagConstraints.CENTER,   //anchor
                             java.awt.GridBagConstraints.NONE,     //fill
                             new java.awt.Insets(0, 0, 0, 0),      //insets
                             10,   10));               //ipadx, ipady
        return msgPanel;
    }
    
    /**
     */
    void updateView(final ImageDataObject imageObj) {
        loadImage(imageObj);
    }
    
    /**
     * Enables or disables all toolbar buttons.
     *
     * @param  enabled  <code>true</code> if all buttons should be enabled,
     *                  <code>false</code> if all buttons should be disabled
     */
    private void setToolbarButtonsEnabled(final boolean enabled) {
        assert toolbarButtons != null;
        
        final Iterator<JButton> it = toolbarButtons.iterator();
        while (it.hasNext()) {
            it.next().setEnabled(enabled);
        }
    }
    
    /**
     * Loads an image from the given <code>ImageDataObject</code>.
     * If the image is loaded successfully, it is stored
     * to field {@link #storedImage}. The field is <code>null</code>ed
     * in case of any failure.
     *
     * @param  imageObj  <code>ImageDataObject</code> to load the image from     
     */
    private void loadImage(final ImageDataObject imageObj) {
        loadImageTask = RP.create(new Runnable() {

            public void run() {
                try (ProgressHandle loadImageProgress = ProgressHandle.createHandle(NbBundle.getMessage(ImageViewer.class, "LBL_LoadingImage"))) {
                    loadImageProgress.start();
                    performLoadImage(imageObj);
                }
            }
        });
        loadImageTask.schedule(0);
      
    }
                
   

    protected void componentClosed() {
        loadImageTask.cancel();
        super.componentClosed();
    }
    
    private void performLoadImage(final ImageDataObject imageObj) {
        String errMsg;
        NBImageIcon image;
        try {            
            image = NBImageIcon.load(imageObj);
            if (image != null) {
                errMsg = null;
            } else {
                errMsg = NbBundle.getMessage(ImageViewer.class, "MSG_CouldNotLoad");       //NOI18N
            }
        } catch (IOException ex) {
            image = null;
            errMsg = NbBundle.getMessage(ImageViewer.class, "MSG_ErrorWhileLoading");      //NOI18N
        }
        assert (image == null) != (errMsg == null);        
        final NBImageIcon fImage = image;
        final String fErrMsg = errMsg;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showImage(fImage, fErrMsg);
            }
        });
    }

    private void initToolbar() {
        /* compose the whole panel: */
        JToolBar toolbar = createToolBar();
        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.NORTH);
    }
    
    private void showImage(NBImageIcon image, String errorMessage) {
        boolean wasValid = (storedImage != null);
        storedImage = image;
        boolean isValid = (storedImage != null);

        if (wasValid && isValid) {
            reloadIcon();
            return;
        }
        if (view != null) {
            remove(view);
        }
        if (isValid) {
            view = createImageView();

            imageWidth = storedImage.getIconWidth();
            imageHeight = storedImage.getIconHeight();
            
            initToolbar();
        } else {
            view = createMessagePanel(errorMessage);
        }
        add(view, BorderLayout.CENTER);
        if (wasValid != isValid) {
            setToolbarButtonsEnabled(isValid);
        }
        revalidate();
        repaint();        
    }

    /** Creates toolbar. */
    private JToolBar createToolBar() {
        // Definition of toolbar.
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        toolBar.setFloatable (false);
        toolBar.setName (NbBundle.getBundle(ImageViewer.class).getString("ACSN_Toolbar"));
        toolBar.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Toolbar"));
            JButton outButton = new JButton(SystemAction.get(ZoomOutAction.class));
            outButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut"));
            outButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Out_BTN_Mnem").charAt(0));
            outButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_Out_BTN"));
            outButton.setText("");
        toolBar.add(outButton);       
        toolbarButtons.add(outButton);
            JButton inButton = new JButton(SystemAction.get(ZoomInAction.class));
            inButton.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn"));
            inButton.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_In_BTN_Mnem").charAt(0));
            inButton.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACSD_In_BTN"));
            inButton.setText("");
        toolBar.add(inButton);
        toolbarButtons.add(inButton);
        toolBar.addSeparator(new Dimension(11, 0));
        
        JButton button;
        
        toolBar.add(button = getZoomButton(1,1));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getZoomButton(1,3));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(1,5));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(1,7));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getZoomButton(3,1));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(5,1));
        toolbarButtons.add(button);
        toolBar.add(button = getZoomButton(7,1));
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
//        SystemAction sa = SystemAction.get(CustomZoomAction.class);
//        sa.putValue (Action.SHORT_DESCRIPTION, NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        toolBar.add (button = getZoomButton ());
        toolbarButtons.add(button);
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(button = getGridButton());
        toolbarButtons.add(button);
        
        // Image Dimension
        toolBar.addSeparator(new Dimension(11, 0));
        toolBar.add(new JLabel(NbBundle.getMessage(ImageViewer.class, "LBL_ImageDimensions", imageWidth, imageHeight)));

        // Image File Size in KB, MB
        if (imageSize != -1) {
            toolBar.addSeparator(new Dimension(11, 0));

            double kb = 1024.0;
            double mb = kb * kb;

            final double size;
            final String label;

            if (imageSize >= mb) {
                size = imageSize / mb;
                label = "LBL_ImageSizeMb"; // NOI18N
            } else if (imageSize >= kb) {
                size = imageSize / kb;
                label = "LBL_ImageSizeKb"; // NOI18N
            } else {
                size = imageSize;
                label = "LBL_ImageSizeBytes"; //NOI18N
            }

            toolBar.add(new JLabel(NbBundle.getMessage(ImageViewer.class, label, formatter.format(size))));
        }

        for (JButton jb : toolbarButtons) {
            jb.setFocusable(false);
        }

        return toolBar;
    }
    
    /** Updates the name and tooltip of this top component according to associated data object. */
    private void updateName () {
        // update name
        String name = storedObject.getNodeDelegate().getDisplayName();
        setName(name);
        // update tooltip
        FileObject fo = storedObject.getPrimaryFile();
        setToolTipText(FileUtil.getFileDisplayName(fo));
    }

    /** 
     */
    public void open(){
        if (discard()) {
            return;
        }
        super.open();
    }
    
    /**
     */
    protected String preferredID() {
        return getClass().getName();
    }
    
    /** Gets HelpContext. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
        
    /** This component should be discarded if the associated environment
     *  is not valid.
     */
    private boolean discard () {
        return storedObject == null;
    }

    protected boolean closeLast() {
        ((ImageOpenSupport)storedObject.getCookie(ImageOpenSupport.class)).lastClosed();
        return true;
    }

    /** Serialize this top component. Serializes its data object in addition
     * to common superclass behaviour.
     * @param out the stream to serialize to
     */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal(out);
        out.writeObject(storedObject);
    }
    
    /** Deserialize this top component.
     * Reads its data object and initializes itself in addition
     * to common superclass behaviour.
     * @param in the stream to deserialize from
     */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        storedObject = (ImageDataObject)in.readObject();
        // to reset the listener for FileObject changes
        ((ImageOpenSupport)storedObject.getCookie(ImageOpenSupport.class)).prepareViewer();
        initialize(storedObject);
    }
    
    /** Creates cloned object which uses the same underlying data object. */
    protected CloneableTopComponent createClonedObject () {
        return new ImageViewer(storedObject);
    }
    
    /** Overrides superclass method. Gets actions for this top component. */
    public SystemAction[] getSystemActions() {
        SystemAction[] oldValue = super.getSystemActions();
        SystemAction fsa = null;
        try {
            ClassLoader l = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = getClass().getClassLoader();
            }
            Class c = Class.forName("org.openide.actions.FileSystemAction", true, l).asSubclass(SystemAction.class); // NOI18N
            fsa = (SystemAction) SystemAction.findObject(c, true);
        } catch (Exception ex) {
            // there are no filesystem actions
        }

        return SystemAction.linkActions(new SystemAction[] {
            SystemAction.get(ZoomInAction.class),
            SystemAction.get(ZoomOutAction.class),
            SystemAction.get(CustomZoomAction.class),
            fsa,
            null},
            oldValue);
    }
    
    /** Overrides superclass method. Gets <code>Icon</code>. */
    public Image getIcon () {
        return ImageUtilities.loadImage("org/netbeans/modules/image/imageObject.png"); // NOI18N
    }
    
    /** Draws zoom in scaled image. */
    public void zoomIn() {
        scaleIn();
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Draws zoom out scaled image. */
    public void zoomOut() {
        double oldScale = scale;
        
        scaleOut();
        
         // You can't still make picture smaller, but bigger why not?
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Resizes panel. */
    private void resizePanel() {
        panel.setPreferredSize(new Dimension(
            (int)(getScale () * storedImage.getIconWidth ()),
            (int)(getScale () * storedImage.getIconHeight()))
        );
        panel.revalidate();
    }
    
    /** Tests new size of image. If image is smaller than  minimum
     *  size(1x1) zooming will be not performed.
     */
    private boolean isNewSizeOK() {
        if (((getScale () * storedImage.getIconWidth ()) > 1) &&
            ((getScale () * storedImage.getIconWidth ()) > 1)
        ) return true;
        return false;
    }
    
    /** Perform zoom with specific proportion.
     * @param fx numerator for scaled
     * @param fy denominator for scaled
     */
    public void customZoom(int fx, int fy) {
        double oldScale = scale;
        
        scale = (double)fx/(double)fy;
        if(!isNewSizeOK()) {
            scale = oldScale;
            
            return;
        }
        
        resizePanel();
        panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
    }
    
    /** Return zooming factor.*/
    private double getScale () {
        return scale;
    }
    
    /** Change proportion "out"*/
    private void scaleOut() {
        scale = scale / changeFactor;
    }
    
    /** Change proportion "in"*/
    private void scaleIn() {
        double oldComputedScale = getScale ();
        
        scale = changeFactor * scale;
        
        double newComputedScale = getScale();
        
        if (newComputedScale == oldComputedScale)
            // Has to increase.
            scale = newComputedScale + 1.0D;
    }
    
    /** Gets zoom button. */
    private JButton getZoomButton(final int xf, final int yf) {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(""+xf+":"+yf); // NOI18N
        if (xf < yf)
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomOut") + " " + xf + " : " + yf);
        else
            button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ZoomIn") + " " + xf + " : " + yf);
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customZoom(xf, yf);
            }
        });
        
        return button;
    }
    
    private JButton getZoomButton() {
        // PENDING buttons should have their own icons.
        JButton button = new JButton(NbBundle.getBundle(CustomZoomAction.class).getString("LBL_XtoY")); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_CustomZoom"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Zoom_BTN"));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                CustomZoomAction sa = (CustomZoomAction) SystemAction.get(CustomZoomAction.class);
                sa.performAction ();
            }
        });
        
        return button;
    }
    
    /** Gets grid button.*/
    private JButton getGridButton() {
        // PENDING buttons should have their own icons.
        final JButton button = new JButton(" # "); // NOI18N
        button.setToolTipText (NbBundle.getBundle(ImageViewer.class).getString("LBL_ShowHideGrid"));
        button.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN"));
        button.setMnemonic(NbBundle.getBundle(ImageViewer.class).getString("ACS_Grid_BTN_Mnem").charAt(0));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showGrid = !showGrid;
                panel.repaint(0, 0, panel.getWidth(), panel.getHeight());
            }
        });
        
        return button;
    }
    
}
