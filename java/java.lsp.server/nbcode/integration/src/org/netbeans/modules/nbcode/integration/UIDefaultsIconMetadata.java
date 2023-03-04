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
package org.netbeans.modules.nbcode.integration;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import org.openide.modules.OnStart;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Goes through UIDefaults and replaces Icons and Images with variants that
 * return image's URL from the 'url' property. 
 * <b>Imporant note:</b> the class is <b>not a part of NetBeans IDE runtime</b>, it is
 * packaged only to a LSP "nbcode" headless server.
 * <p>
 * The class processes resource
 * {@code org/netbeans/modules/nbcode/integration/resources/uimanager-icons.properties}:
 * keys define {@link UIManager} keys that will be replaced. If the key has a value, it will
 * be replaced by an Icon {@link ImageUtilities#loadImage(java.lang.String) loaded} from that resource.
 * If the key has no value, the image will be copied from a stub resource ({@code org/netbeans/modules/nbcode/integration/resources/empty.png})
 * at build time and will get URL {@code org/netbeans/modules/nbcode/integration/resources/uidefaults&lt;property-key>}.
 * 
 * @author sdedic
 */
@OnStart
public class UIDefaultsIconMetadata implements Runnable {
    private static final Logger LOG = Logger.getLogger(UIDefaultsIconMetadata.class.getName());
    private static final String RESOURCE_PREFIX = "org/netbeans/modules/nbcode/integration/resources/uidefaults/"; // NOI18N
    @Override
    public void run() {
        EventQueue.invokeLater(() -> {
            // force LaF initialization before the UIDefaults are read/patched.
            UIManager.get("force.laf.initialization");
            // force initialization of ImageUtilities class, otherwise its clinit could recursively call
            // the lazy value here, which results in call into an (still) uninitialized ImageUtilities -> ClassDefNotFoundError.
            ImageUtilities.loadImage("org/netbeans/modules/nbcode/integration/resources/empty.png");
            replaceIconsAndImages(UIManager.getDefaults());
        });
    }
    
    static void replaceIconsAndImages(UIDefaults defs) {
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        try (InputStream is = UIDefaultsIconMetadata.class.getResourceAsStream("resources/uimanager-icons.properties")) { // NOI18N
            Properties p = new Properties();
            p.load(is);
            // UIDefaults.keySet() is so lazy implemented that it does not enumerate any items ... but entrySet
            // works and even allows to see raw LazyValue and ActiveValue instances...
            Set<Map.Entry<Object,Object>> entries = defs.entrySet();
            Set<Object> uiDefaultsKeys = new HashSet<>();
            for (Map.Entry<Object,Object> e : entries) {
                uiDefaultsKeys.add(e.getKey());
            }
            // defs.keySet() is empty for some reason 
            for (String uiKey : p.stringPropertyNames()) {
                if (!uiDefaultsKeys.contains(uiKey)) {
                    LOG.log(Level.INFO, "Icon not used: {0}", uiKey);
                    continue;
                }
                String v = p.getProperty(uiKey);
                String resImage;
                URL u = null;
                if (v == null || v.trim().isEmpty()) {
                    String r = RESOURCE_PREFIX + uiKey + ".png"; // NOI18N
                    u = l.getResource(r);
                    if (u == null) {
                        r = RESOURCE_PREFIX + uiKey + ".gif"; // NOI18N
                    }
                    resImage = r;
                } else {
                    resImage = v;
                    u = l.getResource(resImage);
                }
                 
                if (u == null) {
                    LOG.log(Level.WARNING, "Resource missing: {0}", resImage);
                    continue;
                }
                LOG.log(Level.INFO, "Lazy-replacing icon: {0}", uiKey);
                // verify the resource exists
                defs.put(uiKey, new LazyValue() {
                    @Override
                    public Object createValue(UIDefaults table) {
                        return ImageUtilities.image2Icon(ImageUtilities.loadImage(resImage));
                    }
                });
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Could not load image replacements", ex);
        }
    }
}
