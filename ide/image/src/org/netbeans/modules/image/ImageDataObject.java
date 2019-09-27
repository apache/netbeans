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


import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.ErrorManager;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;


/** 
 * Object that represents one file containing an image.
 * @author Petr Hamernik, Jaroslav Tulach, Ian Formanek, Michael Wever
 * @author  Marian Petras
 */
public class ImageDataObject extends MultiDataObject implements CookieSet.Factory {
    
    @MIMEResolver.ExtensionRegistration(
        displayName="#ImageMimeResolverJPG",
        mimeType=ImageDataLoader.JPEG_MIME_TYPE,
        extension={"jpg", "JPG", "jpe", "JPE", "jpeg", "JPEG"},
        showInFileChooser={"#ResourceFiles"},
        position=110
    )
    @Messages("ImageMimeResolverJPG=JPEG Image Files")
    /** Generated serialized version UID. */
    static final long serialVersionUID = -6035788991669336965L;

    @MIMEResolver.ExtensionRegistration(
        displayName="#ImageMimeResolverGIF",
        mimeType=ImageDataLoader.GIF_MIME_TYPE,
        extension={"gif", "GIF"},
        showInFileChooser={"#ResourceFiles"},
        position=111
    )
    @Messages("ImageMimeResolverGIF=GIF Image Files")
    /** Base for image resource. */
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/image/imageObject.png"; // NOI18N
    
    @MIMEResolver.ExtensionRegistration(
        displayName="#ImageMimeResolverPNG",
        mimeType=ImageDataLoader.PNG_MIME_TYPE,
        extension={"png", "PNG"},
        showInFileChooser={"#ResourceFiles"},
        position=112
    )
    @Messages("ImageMimeResolverPNG=PNG Image Files")
    /** Open support for this image data object. */
    private transient ImageOpenSupport openSupport;
    @MIMEResolver.ExtensionRegistration(
        displayName="#ImageMimeResolverBMP",
        mimeType=ImageDataLoader.BMP_MIME_TYPE,
        extension={"bmp", "BMP"},
        showInFileChooser={"#ResourceFiles"},
        position=113
    )
    @Messages("ImageMimeResolverBMP=BMP Image Files")
    /** Print support for this image data object **/
    private transient ImagePrintSupport printSupport;
 
    /** Constructor.
     * @param pf primary file object for this data object
     * @param loader the data loader creating it
     * @exception DataObjectExistsException if there was already a data object for it 
     */
    public ImageDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        
        getCookieSet().add(ImageOpenSupport.class, this);
        getCookieSet().add(ImagePrintSupport.class, this);
    }


    /** Implements <code>CookieSet.Factory</code> interface. */
    public Node.Cookie createCookie(Class clazz) {
        if(clazz.isAssignableFrom(ImageOpenSupport.class))
            return getOpenSupport();
        else if( clazz.isAssignableFrom(ImagePrintSupport.class))
            return getPrintSupport();        
        else
            return null;
    }

    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    /** Gets image open support. */
    private synchronized ImageOpenSupport getOpenSupport() {
        if(openSupport == null) {
            openSupport = new ImageOpenSupport(getPrimaryEntry());
        }
        return openSupport;
    }

    protected synchronized ImagePrintSupport getPrintSupport(){
        if(printSupport == null) {
            printSupport = new ImagePrintSupport( this );
        }
        return printSupport;
    }
    
    /** Help context for this object.
     * @return the help context
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Get a URL for the image.
     * @return the image url
     */
    URL getImageURL() {
        try {
            return getPrimaryFile().getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
    }

    /** Gets image data for the image object.
     * @return the image data
     * @deprecated use getImage() instead
     */
    @Deprecated
    private byte[] getImageData() {
        try {
            FileObject fo = getPrimaryFile();
            byte[] imageData = new byte[(int)fo.getSize()];
            BufferedInputStream in = new BufferedInputStream(fo.getInputStream());
            in.read(imageData, 0, (int)fo.getSize());
            in.close();
            return imageData; 
        } catch(IOException ioe) {
            return new byte[0];
        }
    }

    // Michael Wever 26/09/2001
    /** Gets image for the image data 
     * @return the image or <code>null</code> if image could not be created
     * @return  java.io.IOException  if an error occurs during reading
     */
    public Image getImage() throws IOException {
        InputStream input = getPrimaryFile().getInputStream();
        try {
            return javax.imageio.ImageIO.read(input);
        } catch (IndexOutOfBoundsException ioobe) {
            return null;
        } finally {
            input.close();
        }
    }


    /** Create a node to represent the image. Overrides superclass method.
     * @return node delegate */
    protected Node createNodeDelegate () {
        return new ImageNode(this);
    }
    
    
    /** Node representing <code>ImageDataObject</code>. */
    private static final class ImageNode extends DataNode {
        /** Constructs image node. */
        public ImageNode(ImageDataObject obj) {
            super(obj, Children.LEAF);
            //setIconBase(IMAGE_ICON_BASE);
            setIconBaseWithExtension(IMAGE_ICON_BASE);
            setDefaultAction (SystemAction.get (OpenAction.class));
        }
        
        /** Creates property sheet. Ovrrides superclass method. */
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            Sheet.Set ss = s.get(Sheet.PROPERTIES);
            if (ss == null) {
                ss = Sheet.createPropertiesSet();
                s.put(ss);
            }
            ss.put(new ImageWidthProperty());
            ss.put(new ImageHeightProperty());
            ss.put(new ThumbnailProperty(getDataObject()));
            return s;
        }
        

        /** Property representing for thumbanil property in the sheet. */
        private static final class ThumbnailProperty extends PropertySupport.ReadOnly {
            /** (Image) data object associated with. */
            private final DataObject obj;
            
            /** Constructs property. */
            public ThumbnailProperty(DataObject obj) {
                super("thumbnail", Icon.class, // NOI18N
                    NbBundle.getMessage(ImageDataObject.class, "PROP_Thumbnail"),
                    NbBundle.getMessage(ImageDataObject.class, "HINT_Thumbnail"));
                this.obj = obj;
            }
            
            /** Gets value of property. Overrides superclass method. */
            public Object getValue() throws InvocationTargetException {
                try {
                    return new ImageIcon(obj.getPrimaryFile().getURL());
                } catch (FileStateInvalidException fsie) {
                    throw new InvocationTargetException(fsie);
                }
            }
            
            /** Gets property editor. */
            public PropertyEditor getPropertyEditor() {
                return new ThumbnailPropertyEditor();
            }
            
            
            /** Property editor for thumbnail property. */
            private final class ThumbnailPropertyEditor extends PropertyEditorSupport {
                /** Overrides superclass method.
                 * @return <code>true</code> */
                public boolean isPaintable() {
                    return true;
                }
                
                /** Patins thumbanil of the image. Overrides superclass method. */
                public void paintValue(Graphics g, Rectangle r) {
                    ImageIcon icon = null;
                    
                    try {
                        icon = (ImageIcon)ThumbnailProperty.this.getValue();
                    } catch(InvocationTargetException ioe) {
                        if(Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                    
                    if(icon != null) {
                        int iconWidth = icon.getIconWidth();
                        int iconHeight = icon.getIconHeight();
                        

                        // Shrink image if necessary.
                        double scale = (double)iconWidth / iconHeight;
                        
                        if(iconWidth > r.width) {
                            iconWidth = r.width;
                            iconHeight = (int) (iconWidth / scale);
                        }

                        if(iconHeight > r.height) {
                            iconHeight = r.height;
                            iconWidth = (int) (iconHeight * scale);
                        }
                        
                        // Try to center it if it fits, else paint as much as possible.
                        int x;
                        if(iconWidth < r.x) {
                            x = (r.x - iconWidth) / 2;
                        } else {
                            x = 5; // XXX Indent.
                        }
                        
                        int y;
                        if(iconHeight < r.y) {
                            y = (r.y - iconHeight) / 2;
                        } else {
                            y = 0;
                        }
                        
                        Graphics g2 = g.create(r.x, r.y, r.width, r.height);
                        g.drawImage(icon.getImage(), x, y, iconWidth, iconHeight, null);
                    }
                }

                /** Overrides superclass method.
                 * @return <code>null</code> */
                public String getAsText() {
                    return null;
                }
            } // End of class ThumbnailPropertyEditor.
        } // End of class ThumbnailProperty.

       /** Property representing for image width property in the sheet. */
       private final class ImageWidthProperty extends PropertySupport.ReadOnly {
          /** Constructs property. */
          public ImageWidthProperty() {
             super("width", Integer.class, // NOI18N
                   NbBundle.getMessage(ImageDataObject.class, "PROP_Image_Width"), // NOI18N
                   NbBundle.getMessage(ImageDataObject.class, "HINT_Image_Width")); // NOI18N
          }

          /** Gets value of property. Overrides superclass method. */
          public Object getValue() throws InvocationTargetException {
             try {
                final Icon icon = new ImageIcon(getDataObject().getPrimaryFile().
                      getURL());
                return Integer.valueOf(icon.getIconWidth());
             } catch (FileStateInvalidException fsie) {
                throw new InvocationTargetException(fsie);
             }
          }
       } // End of class ImageWidthProperty.

       /** Property representing for image height property in the sheet. */
       private final class ImageHeightProperty extends PropertySupport.ReadOnly {
          /** Constructs property. */
          public ImageHeightProperty() {
             super("height", Integer.class, // NOI18N
                   NbBundle.getMessage(ImageDataObject.class,
                   "PROP_Image_Height"), // NOI18N
                   NbBundle.getMessage(ImageDataObject.class,
                   "HINT_Image_Height")); // NOI18N
          }

          /** Gets value of property. Overrides superclass method. */
          public Object getValue() throws InvocationTargetException {
             try {
                final Icon icon = new ImageIcon(getDataObject().getPrimaryFile().
                      getURL());
                return Integer.valueOf(icon.getIconHeight());
             } catch (FileStateInvalidException fsie) {
                throw new InvocationTargetException(fsie);
             }
          }
       } // End of class ImageHeightProperty.
    } // End of class ImageNode.

}
