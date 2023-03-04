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


package org.netbeans.core.windows.view;



import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.dnd.*;
import org.openide.util.*;
import org.openide.windows.*;


/**
 * Class which represents model of editor area element for GUI hierarchy.
 *
 * @author  Peter Zavadsky
 */
public class EditorView extends ViewElement {

    private static final boolean IS_GTK = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    
    private ViewElement editorArea;
    
    private EditorAreaComponent editorAreaComponent;
    
    // XXX PENDING
    private final WindowDnDManager windowDnDManager;
    
    
    public EditorView(Controller controller, WindowDnDManager windowDnDManager,
    double resizeWeight, ViewElement editorArea) {
        super(controller, resizeWeight);
        
        this.editorArea = editorArea;
        this.windowDnDManager = windowDnDManager;
    }
    
    
    // XXX
    Rectangle getPureBounds() {
        Component comp = getEditorAreaComponent();
        Rectangle bounds = comp.getBounds();
        Point location = new Point(0, 0);
        javax.swing.SwingUtilities.convertPointToScreen(location, comp);
        bounds.setLocation(location);
        return bounds;
    }
    
    private EditorAreaComponent getEditorAreaComponent() {
        if(editorAreaComponent == null) {
            editorAreaComponent = new EditorAreaComponent(this, windowDnDManager);
        }

        // Workaround for #42640
        if (EditorView.IS_GTK && !editorAreaComponent.isValid()) {
            editorAreaComponent.repaint();
        }
        return editorAreaComponent;
    }
    
    /** Handles special border policy - scroll pane like border only 
     * if editor area is null.
     */
    private void manageBorder (JPanel panel) {
        if (editorArea != null) {
            panel.setBorder(null);
        } else {
            if (Utilities.isMac()) {
               //#64701 on macosx the nb.scrollpane.border draws ugly line on top
                panel.setBorder(BorderFactory.createEmptyBorder());
            } else {
                Border border = UIManager.getBorder( "Nb.EmptyEditorArea.border"); //NOI18N
                if( null == border ) {
                    // special border installed into UI manager by netbeans
                    border = UIManager.getBorder("Nb.ScrollPane.border"); //NOI18N
                }
                panel.setBorder(border);
	    }
        }
    }
    
    public ViewElement getEditorArea() {
        return editorArea;
    }
    
    public void setEditorArea(ViewElement editorArea) {
        this.editorArea = editorArea;
    }
    
    @Override
    public Component getComponent() {
//        assureComponentInEditorArea();
        return getEditorAreaComponent();
    }
    
    @Override
    public boolean updateAWTHierarchy(Dimension availableSpace) {
//        System.out.println("EditorView:updateAWTHierarchy=" + availableSpace);
        boolean result = false;
        EditorAreaComponent comp = getEditorAreaComponent();
        Dimension d = (Dimension) comp.getClientProperty ("lastAvailableSpace"); //NOI18N
        Dimension currDim = comp.getPreferredSize();
        if (!availableSpace.equals(d) || !availableSpace.equals(currDim)) {
            //We will only return true if we actually did something
            comp.setPreferredSize(availableSpace);
//            comp.setMinimumSize(availableSpace);
            comp.putClientProperty("lastAvailableSpace", availableSpace); //NOI18N
            result = true;
        }
        assureComponentInEditorArea();      
        if (editorArea != null) {
            result |= editorArea.updateAWTHierarchy(new Dimension(availableSpace.width - 5, availableSpace.height - 5));
        }
        return result;
    }
    
    void assureComponentInEditorArea() {
        EditorAreaComponent eac = getEditorAreaComponent();
        if(editorArea == null) {
            eac.setAreaComponent(null);
        } else {
            eac.setAreaComponent(editorArea.getComponent());
        }
        manageBorder(eac);
        
//        // XXX #36885 When in maximixed and compact mode, we cannot add the components
//        // into the editor area, it would remove it from the screen.
//        if(addingAllowed) {
//            if(this.editorArea != null) {
//                editorAreaComp.add(this.editorArea.getComponent(), BorderLayout.CENTER);
//            }
//
//            editorAreaComp.validate();
//            editorAreaComp.repaint();
//        }
    } 

    private static DataFlavor URI_LIST_DATA_FLAVOR;
    static {
        try {
            URI_LIST_DATA_FLAVOR = new DataFlavor("text/uri-list;class=java.lang.String");
        } catch( ClassNotFoundException cnfE ) {
            cnfE.printStackTrace();
        }
    }
    
    private static class EditorAreaComponent extends JPanel
    implements TopComponentDroppable {
        
        private final EditorView editorView;
        
        // XXX PENDING
        private final WindowDnDManager windowDnDManager;
        
        private Component areaComponent;
        
        
        public EditorAreaComponent(EditorView editorView, WindowDnDManager windowDnDManager) {
            this.editorView = editorView;
            this.windowDnDManager = windowDnDManager;
            
            init();
        }

        
        private void init() {
            setLayout(new BorderLayout());
            // special background for XP style
            String lfID = UIManager.getLookAndFeel().getID();
//            if (lfID.equals("Windows")) {
//                setBackground((Color)UIManager.get("nb_workplace_fill"));
//            }
            
            // PENDING Adding image into empty area.
            String imageSource = Constants.SWITCH_IMAGE_SOURCE; // NOI18N
            if(imageSource != null) {
                Image image = ImageUtilities.loadImage(imageSource);
                if(image != null) {
                    JLabel label = new JLabel(ImageUtilities.image2Icon(image));
                    label.setMinimumSize(new Dimension(0, 0)); // XXX To be able shrink the area.
                    add(label, BorderLayout.CENTER);
                } else {
                    Logger.getLogger(EditorView.class.getName()).log(Level.WARNING, null,
                                      new java.lang.NullPointerException("Image not found at " +
                                                                         imageSource)); // NOI18N
                }
            }
            //listen to files being dragged over the editor area
            DropTarget dropTarget = new DropTarget( this, new DropTargetListener() {
                @Override
                public void dragEnter(DropTargetDragEvent dtde) {
                }
                @Override
                public void dragExit(DropTargetEvent dte) {
                }
                @Override
                public void dragOver(DropTargetDragEvent dtde) {
                    for( ExternalDropHandler handler : Lookup.getDefault().lookupAll( ExternalDropHandler.class ) ) {
                        //check if a file is being dragged over and if anybody can process it
                        if( handler.canDrop( dtde ) ) {
                            dtde.acceptDrag( DnDConstants.ACTION_COPY );
                            return;
                        }
                    }
                    dtde.rejectDrag();
                }
                @Override
                public void drop(DropTargetDropEvent dtde) {
                    boolean dropRes = false;
                    try {
                        for( ExternalDropHandler handler : Lookup.getDefault().lookupAll( ExternalDropHandler.class ) ) {
                            if( handler.canDrop( dtde ) ) {
                                //file is being dragged over
                                dtde.acceptDrop( DnDConstants.ACTION_COPY );
                                //let the handler to take care of it
                                dropRes = handler.handleDrop( dtde );
                                break;
                            }
                        }
                    } finally {
                        dtde.dropComplete( dropRes );
                    }
                }
                @Override
                public void dropActionChanged(DropTargetDragEvent dtde) {
                }
            } );
            setDropTarget( dropTarget );
            if( UIManager.getBoolean( "NbMainWindow.showCustomBackground" )
                    || "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
                setOpaque( false);
        }

        @Override
        public void updateUI() {
            super.updateUI();

            Color background = UIManager.getColor("Nb.EmptyEditorArea.background");
            if (background == null) {
                // restore to default (on LaF switch)
                background = UIManager.getColor("Panel.background");
            }
            setBackground(background);
        }
        
        public void setAreaComponent(Component areaComponent) {
            if(this.areaComponent == areaComponent) {
                // XXX PENDING revise how to better manipulate with components
                // so there don't happen unneeded removals.
                if(areaComponent != null
                && !Arrays.asList(getComponents()).contains(areaComponent)) {
                    add(areaComponent, BorderLayout.CENTER);
                }
                
                return;
            }
            
            if(this.areaComponent != null) {
                remove(this.areaComponent);
            }
            
            this.areaComponent = areaComponent;
            
            if(this.areaComponent != null) {
                add(this.areaComponent, BorderLayout.CENTER);
            }

            repaint();
        }
        
        @Override
        public Shape getIndicationForLocation(Point location) {
            int kind = windowDnDManager.getStartingTransfer().getKind();
                        
            if(kind == Constants.MODE_KIND_EDITOR) {
                Rectangle rect = getBounds();
                rect.setLocation(0, 0);
                return rect;
            } else {
                Rectangle rect = getBounds();
                rect.setLocation(0, 0);

                String side = getSideForLocation(location);

                double ratio = Constants.DROP_AROUND_EDITOR_RATIO;
                if(Constants.TOP.equals(side)) {
                    return new Rectangle(0, 0, rect.width, (int)(rect.height * ratio));
                } else if(side == Constants.LEFT) {
                    return new Rectangle(0, 0, (int)(rect.width * ratio), rect.height);
                } else if(side == Constants.RIGHT) {
                    return new Rectangle(rect.width - (int)(rect.width * ratio), 0, (int)(rect.width * ratio), rect.height);
                } else if(side == Constants.BOTTOM) {
                    return new Rectangle(0, rect.height - (int)(rect.height * ratio), rect.width, (int)(rect.height * ratio));
                } else if(windowDnDManager.getStartingTransfer().isAllowedToMoveAnywhere()) {
                    return rect;
                } else {
                    return null;
                }
            }
        };
        
        @Override
        public Object getConstraintForLocation(Point location) {
            int kind = windowDnDManager.getStartingTransfer().getKind();
                        
            if(kind == Constants.MODE_KIND_EDITOR) {
                return null;
            } else {
                return getSideForLocation(location);
            }
        }
        
        private String getSideForLocation(Point location) {
            Rectangle bounds = getBounds();
            bounds.setLocation(0, 0);

            // Size of area which indicates creation of new split.
            int delta = Constants.DROP_AREA_SIZE;

            Rectangle top = new Rectangle(0, 0, bounds.width, delta);
            if(top.contains(location)) {
                return Constants.TOP;
            }

            Rectangle left = new Rectangle(0, delta, delta, bounds.height - 2 * delta);
            if(left.contains(location)) {
                return Constants.LEFT;
            }

            Rectangle right = new Rectangle(bounds.width - delta, delta, delta, bounds.height - 2 * delta);
            if(right.contains(location)) {
                return Constants.RIGHT;
            }

            Rectangle bottom = new Rectangle(0, bounds.height - delta, bounds.width, delta);
            if(bottom.contains(location)) {
                return Constants.BOTTOM;
            }

            return null;
        }
        
        @Override
        public Component getDropComponent() {
            return this;
        }
        
        @Override
        public ViewElement getDropViewElement() {
            return editorView;
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            if(transfer.isAllowedToMoveAnywhere()) {
                return true;
            }
            
            int kind = transfer.getKind();

            if(kind == Constants.MODE_KIND_EDITOR) {
                return true;
            } else {
                if(WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED
                && getSideForLocation(location) != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            return true;
        }

        @Override
        public int getKind() {
            return Constants.MODE_KIND_EDITOR;
        }
        
    } // End of EditorAreaComponent class.
    
    
}

