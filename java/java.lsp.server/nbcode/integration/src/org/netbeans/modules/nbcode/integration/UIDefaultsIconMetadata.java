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
package org.netbeans.modules.nbcode.integration;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.ActiveValue;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import org.openide.modules.OnStart;
import org.openide.util.ImageUtilities;

/**
 * Goes through UIDefaults and replaces Icons and Images with variants that
 * return image's URL from the 'url' property.
 * @author sdedic
 */
@OnStart
public class UIDefaultsIconMetadata implements Runnable {
    private static final Logger LOG = Logger.getLogger(UIDefaultsIconMetadata.class.getName());
    
    // for diagnostic purposes only
    public static final String PROP_ORIGINAL_ICON = "originalIcon"; // NOI18N
    // for diagnostic purposes only
    public static final String PROP_ORIGINAL_IMAGE = "originalImage"; // NOI18N
    
    private static final String URN_DEFAULTS_PREFIX = "uidefaults:"; // NOI18N
    
    private static Object wrapImage(String key, Image image, Icon icon, Object... meta) {
        Object o = image.getProperty(ImageUtilities.PROPERTY_ID, null); // NOI18N
        if (o instanceof URL) {
            return image;
        }
        
        o = image.getProperty(ImageUtilities.PROPERTY_ID, null);
        if (o instanceof String || o instanceof URI) {
            return image;
        } else {
            
            Hashtable props = new Hashtable();
            for (int i = 0; i < meta.length; i+= 2) {
                props.put(meta[i], meta[i+1]);
            }
            props.put(ImageUtilities.PROPERTY_ID, URN_DEFAULTS_PREFIX + key);
            return createNew(image, icon, props);
        }
    }
    
    @Override
    public void run() {
        EventQueue.invokeLater(() -> {
            // force LaF initialization before the UIDefaults are read/patched.
            UIManager.get("force.laf.initialization");
            replaceIconsAndImages(UIManager.getDefaults());
        });
    }
    
    static void replaceIconsAndImages(UIDefaults defs) {
        refreshUIDefaults(defs);
        defs.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (null == evt.getPropertyName() || "UIDefaults".equals(evt.getPropertyName())) { // NOI18N
                    LOG.log(Level.INFO, "Refreshing all UIDefaults");
                    refreshUIDefaults(defs);
                } else {
                    String k = evt.getPropertyName();
                    Object o = evt.getNewValue();
                    if (o == null || o instanceof W) {
                        return;
                    }
                    if (o instanceof LazyValue) {
                        LOG.log(Level.INFO, "Patched lazy value {0}", k);
                        defs.put(k, new LazyWrapper(k, (LazyValue)o));
                    } else if (o instanceof ActiveWrapper) {
                        LOG.log(Level.INFO, "Patched active value {0}", k);
                        defs.put(k, new ActiveWrapper(k, (ActiveWrapper)o));
                    } else if (o instanceof Icon || o instanceof Image) {
                        LOG.log(Level.INFO, "Patched Icon/Image value {0}", k);
                        defs.put(k, new SimpleWrapper(k, o));
                    }
                }
            }
        });
    }
    
    public static void refreshUIDefaults(UIDefaults defs) {
        for (Enumeration en = defs.keys(); en.hasMoreElements(); ) {
            Object ok = en.nextElement();
            
            // Note: this actually instantiates the object, if it implements LazyValue.
            Object o = defs.get(ok);
            if (o instanceof W) {
                continue;
            }
            if (o instanceof Icon || o instanceof Image) {
                LOG.log(Level.INFO, "Replaced {0} with wrapped", ok);
                defs.put(ok, new SimpleWrapper(ok.toString(), o));
            }
        }
    }
    
    private static Object wrapInstance(Object ok, Object o) {
        if (o instanceof W) {
            return o;
        }
        try {
            if (o instanceof ImageIcon) {
                ImageIcon ii = (ImageIcon)o;
                Object wrapper = wrapImage(ok.toString(), ii.getImage(), ii,
                        PROP_ORIGINAL_IMAGE, ii.getImage(), PROP_ORIGINAL_ICON, ii);
                if (wrapper != ii.getImage()) {
                    return (Icon)wrapper;
                } else {
                    return o;
                }
            }

            if (o instanceof Icon) {
                final Icon ico = (Icon)o;
                Image converted = ImageUtilities.icon2Image(ico);
                Object wrapper = wrapImage(ok.toString(), converted, ico, PROP_ORIGINAL_ICON, ico);
                if (wrapper != converted) {
                    return (Icon)wrapper;
                } else {
                    return o;
                }
            }

            if (o instanceof Image) {
                Object wrapper = wrapImage(ok.toString(), (Image)o, null, PROP_ORIGINAL_IMAGE, o);
                return wrapper;
            }
        } catch (RuntimeException ex) {
            // setting to FINE as some icons cannot be image-converted (ClassCastEx during paintIcon).
            LOG.log(Level.FINE, "Error wrapping default {0}: ", ok.toString());
            LOG.log(Level.FINE, "Exception thrown", ex);
        }
        return o;
    }
    
    private static void replace(UIDefaults defs, Object key, String type, Object replacement) {
        LOG.log(Level.INFO, "Replaced {0}", key);
        defs.put(key, replacement);
    }
    
    public static final class MetadataImageIcon extends ImageIcon {
        private final Icon delegateIcon;

        public MetadataImageIcon(Icon delegateIcon, Image original) {
            super(original);
            this.delegateIcon = delegateIcon;
        }

        /**
         * Get an {@link Icon} instance representing a scalable version of this {@code Image}.
         *
         * @return may be null
         */
        public Icon getDelegateIcon() {
            return delegateIcon;
        }

        @Override
        public int getIconHeight() {
            return delegateIcon.getIconHeight();
        }

        @Override
        public int getIconWidth() {
            return delegateIcon.getIconWidth();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            delegateIcon.paintIcon(c, g, x, y);
        }
    }
    
    /**
     * Delegating Image which adds metadata to the original image's {@link Image#getProperty}.
     * @author sdedic
     */
    public static class MetadataImage extends BufferedImage implements W {
        private final Image original;

        public MetadataImage(Image original, ColorModel cm,
                          WritableRaster raster,
                          boolean isRasterPremultiplied,
                          Hashtable<?,?> properties) {
            super(cm, raster, isRasterPremultiplied, properties);
            this.original = original;
        }

        @Override
        public Object getProperty(String name, ImageObserver observer) {
            Object o = super.getProperty(name, observer);
            return o != UndefinedProperty ? o : original.getProperty(name, observer);
        }
    }
    
    public static Object createNew(Image image, Icon icon, Hashtable props) {
        ensureLoaded(image);
        boolean bitmask = (image instanceof Transparency) && ((Transparency) image).getTransparency() != Transparency.TRANSLUCENT;
        ColorModel model = colorModel(bitmask ? Transparency.BITMASK : Transparency.TRANSLUCENT);
        int w = Math.max(1, image.getWidth(null));
        int h = Math.max(1, image.getHeight(null));
        if (icon == null && image instanceof Icon) {
            icon = (Icon)image;
        }
        WritableRaster raster = model.createCompatibleWritableRaster(w, h);
        BufferedImage newImage = new MetadataImage(image, model, raster, bitmask, props);

        java.awt.Graphics g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        if (icon != null) {
            return new MetadataImageIcon(icon, newImage);
        } else {
            return newImage;
        }
    }
    
    private interface W {}
    
    private static final class LazyWrapper implements LazyValue, W {
        private final String key;
        private final LazyValue original;

        public LazyWrapper(String key, LazyValue original) {
            this.key = key;
            this.original = original;
        }
        
        @Override
        public Object createValue(UIDefaults table) {
            LOG.log(Level.INFO, "Resolved lazy wrapped for {0}", key);
            return wrapInstance(key, original.createValue(table));
        }
    }
    
    private static final class ActiveWrapper implements ActiveValue, W {
        private final String key;
        private final ActiveValue original;

        public ActiveWrapper(String key, ActiveValue original) {
            this.key = key;
            this.original = original;
        }

        @Override
        public Object createValue(UIDefaults table) {
            LOG.log(Level.INFO, "Resolved active wrapped for {0}", key);
            return wrapInstance(key, original.createValue(table));
        }
    }
    
    private static final class SimpleWrapper implements LazyValue, W {
        private final String key;
        private final Object original;

        public SimpleWrapper(String key, Object original) {
            this.key = key;
            this.original = original;
        }
        
        @Override
        public Object createValue(UIDefaults table) {
            LOG.log(Level.INFO, "Resolved wrapped for {0}", key);
            return wrapInstance(key, original);
        }
    }

    // ---------- Copied from ImageUtilities - needed to render the image --------
    
    private static final Component component = new Component() {};

    private static final MediaTracker tracker = new MediaTracker(component);

    private static int mediaTrackerID;

    private static void ensureLoaded(Image image) {
        if (
            (Toolkit.getDefaultToolkit().checkImage(image, -1, -1, null) &
                (ImageObserver.ALLBITS | ImageObserver.FRAMEBITS)) != 0
        ) {
            return;
        }

        synchronized (tracker) {
            int id = ++mediaTrackerID;
            tracker.addImage(image, id);

            try {
                tracker.waitForID(id, 0);
            } catch (InterruptedException e) {
                System.out.println("INTERRUPTED while loading Image");
            }

            // #262804 assertation disabled because of error, when using ImageFilter
            // assert (tracker.statusID(id, false) == MediaTracker.COMPLETE) : "Image loaded";
            tracker.removeImage(image, id);
        }
    }

    static private ColorModel colorModel(int transparency) {
        ColorModel model;
        try {
            model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .getColorModel(transparency);
        }
        catch(ArrayIndexOutOfBoundsException | HeadlessException aioobE) {
            //#226279
            model = ColorModel.getRGBdefault();
        }
        return model;
    }

    // ---------- End copy
}
