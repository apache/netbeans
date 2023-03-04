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

package org.netbeans.modules.editor;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.modules.editor.impl.actions.clipboardhistory.ClipboardHistory;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.editor.lib.EditorPackageAccessor;
import org.netbeans.modules.editor.lib2.actions.EditorRegistryWatcher;
import org.netbeans.modules.editor.lib2.document.ReadWriteUtils;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationEvent.Copy;
import org.openide.loaders.OperationEvent.Move;
import org.openide.loaders.OperationEvent.Rename;
import org.openide.loaders.OperationListener;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class EditorModule extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(EditorModule.class.getName());
    
    private static final boolean debug = Boolean.getBoolean("netbeans.debug.editor.kits");

    private PropertyChangeListener topComponentRegistryListener;

    /** Module installed again. */
    public @Override void restored () {
        LocaleSupport.addLocalizer(new NbLocalizer(BaseKit.class));

        // register loader for annotation types
        AnnotationTypes.getTypes().registerLoader( new AnnotationTypes.Loader() {
                @Override
                public void loadTypes() {
                    AnnotationTypesFolder.getAnnotationTypesFolder();
                }
                @Override
                public void loadSettings() {
                    // AnnotationType properties are stored in BaseOption, so let's read them now
                    Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);

                    int i = prefs.getInt(AnnotationTypes.PROP_BACKGROUND_GLYPH_ALPHA, Integer.MIN_VALUE);
                    if (i != Integer.MIN_VALUE) {
                        AnnotationTypes.getTypes().setBackgroundGlyphAlpha(i);
                    }
                    
                    boolean b = prefs.getBoolean(AnnotationTypes.PROP_BACKGROUND_DRAWING, false);
                    AnnotationTypes.getTypes().setBackgroundDrawing(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_COMBINE_GLYPHS, true);
                    AnnotationTypes.getTypes().setCombineGlyphs(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS, true);
                    AnnotationTypes.getTypes().setGlyphsOverLineNumbers(b);
                    
                    b = prefs.getBoolean(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER, true);
                    AnnotationTypes.getTypes().setShowGlyphGutter(b);
                }
                @Override
                public void saveType(AnnotationType type) {
                    AnnotationTypesFolder.getAnnotationTypesFolder().saveAnnotationType(type);
                }
                @Override
                public void saveSetting(String settingName, Object value) {
                    // AnnotationType properties are stored to BaseOption
                    Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
                    if (value instanceof Integer) {
                        prefs.putInt(settingName, (Integer) value);
                    } else if (value instanceof Boolean) {
                        prefs.putBoolean(settingName, (Boolean) value);
                    } else if (value != null) {
                        prefs.put(settingName, value.toString());
                    } else {
                        prefs.remove(settingName);
                    }
                }
            } );

        // ------------------------------------------------------------
        // Autoregistration
            
        // First, initialize JDK's editor kit types registry
        initAndCheckEditorKitTypeRegistry("text/plain", null); //NOI18N
        initAndCheckEditorKitTypeRegistry("text/html", HTMLEditorKit.class.getName()); //NOI18N
        initAndCheckEditorKitTypeRegistry("text/rtf", RTFEditorKit.class.getName()); //NOI18N
        initAndCheckEditorKitTypeRegistry("application/rtf", RTFEditorKit.class.getName()); //NOI18N
            
        // Now hook up to the JDK's editor kit registry
        // XXX: This all should be removed, see IZ #80110
        try {
            Field keyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
            keyField.setAccessible(true);
            Object key = keyField.get(JEditorPane.class);

            Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
            Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
            Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
            Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N
            
            Object appContext = getAppContext.invoke(null);
            Hashtable<?,?> kitMapping = (Hashtable<?,?>) get.invoke(appContext, key);
            put.invoke(appContext, key, new HackMap(kitMapping));

// REMOVE: we should not depend on sun.* classes
//            Hashtable kitMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//            sun.awt.AppContext.getAppContext().put(key, new HackMap(kitMapping));
        } catch (Throwable t) {
            if (debug) {
                LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kits.", t);
            } else {
                LOG.log(Level.WARNING, "Can''t hack in to the JEditorPane''s registry for kits: {0}", new Object[] {t});
            }
        }
            
        // Registration of the editor kits to JEditorPane
//        for (int i = 0; i < replacements.length; i++) {
//            JEditorPane.registerEditorKitForContentType(
//                replacements[i].contentType,
//                replacements[i].newKitClassName,
//                getClass().getClassLoader()
//            );
//        }

        // ------------------------------------------------------------
        

        if (topComponentRegistryListener == null) {
            topComponentRegistryListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                        EditorRegistryWatcher.get().notifyActiveTopComponentChanged(TopComponent.getRegistry().getActivated());
                    }
                }
            };
            TopComponent.getRegistry().addPropertyChangeListener(topComponentRegistryListener);
        }
            
         if (GraphicsEnvironment.isHeadless()) {
             return;
         }
         
        final ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
        if (clipboard != null) {
            clipboard.addClipboardListener(ClipboardHistory.getInstance());
        }

         if (LOG.isLoggable(Level.FINE)) {
             WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    try {
                        Field kitsField = BaseKit.class.getDeclaredField("kits");
                        kitsField.setAccessible(true);
                        Map kitsMap = (Map) kitsField.get(null);
                        LOG.fine("Number of loaded editor kits: " + kitsMap.size());
                    } catch (Exception e) {
                        // ignore
                    }
                }
            });
         }
         
        DataLoaderPool.getDefault().addOperationListener(new OperationListener() {
            @Override public void operationPostCreate(OperationEvent ev) {}
            @Override public void operationCopy(Copy ev) {}
            @Override public void operationMove(Move ev) {}
            @Override public void operationDelete(OperationEvent ev) {}
            @Override public void operationRename(Rename ev) {}
            @Override public void operationCreateShadow(Copy ev) {}
            @Override public void operationCreateFromTemplate(Copy ev) {
                if (!ev.getOriginalDataObject().getPrimaryFile().canRevert()) {
                    // Reformat only files created from original templates.
                    reformat(ev.getObject());
                }
            }
        });
    }
    
    /** Called when module is uninstalled. Overrides superclass method. */
    public @Override void uninstalled() {

        /* [TEMP]
        if (searchSelectedPatternListener!=null){
            SearchHistory.getDefault().removePropertyChangeListener(searchSelectedPatternListener);
        }
        */
         
        if (topComponentRegistryListener != null) {
            TopComponent.getRegistry().removePropertyChangeListener(topComponentRegistryListener);
        }

        // unregister our registry
        try {
            Field keyField = JEditorPane.class.getDeclaredField("kitRegistryKey");  // NOI18N
            keyField.setAccessible(true);
            Object key = keyField.get(JEditorPane.class);
            
            Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
            Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
            Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
            Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N
            Method remove = appContextClass.getDeclaredMethod("remove", Object.class, Object.class); //NOI18N
            
            Object appContext = getAppContext.invoke(null);
            Hashtable<?,?> kitMapping = (Hashtable<?,?>) get.invoke(appContext, key);

            if (kitMapping instanceof HackMap) {
                if (((HackMap) kitMapping).getOriginal() != null) {
                    put.invoke(appContext, key, new HackMap(kitMapping));
                } else {
                    remove.invoke(appContext, key);
                }
            }
            
// REMOVE: we should not depend on sun.* classes
//            HackMap kitMapping = (HackMap)sun.awt.AppContext.getAppContext().get(key);
//            if (kitMapping.getOriginal() != null) {
//                sun.awt.AppContext.getAppContext().put(key, kitMapping.getOriginal());
//            } else {
//                sun.awt.AppContext.getAppContext().remove(key);
//            }
        } catch (Throwable t) {
            if (debug) {
                LOG.log(Level.WARNING, "Can't release the hack from the JEditorPane's registry for kits.", t);
            } else {
                LOG.log(Level.WARNING, "Can't release the hack from the JEditorPane's registry for kits.");
            }
        }

        // #42970 - Possible closing of opened editor top components must happen in AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                // issue #16110
                // close all TopComponents which contain editor based on BaseKit
                HashSet set = new HashSet();
                set.addAll(TopComponent.getRegistry().getOpened());

                for (Iterator it = set.iterator(); it.hasNext(); ) {
                    TopComponent topComp = (TopComponent)it.next();
                    // top components in which we are interested must be of type CloneableEditor
                    if (!(topComp instanceof CloneableEditor))
                        continue;
                    Node[] arr = topComp.getActivatedNodes();
                    if (arr == null)
                        continue;
                    for (int i=0; i<arr.length; i++) {
                        EditorCookie ec = (EditorCookie)arr[i].getCookie(EditorCookie.class);
                        if (ec == null)
                            continue;
                        JEditorPane[] pane = ec.getOpenedPanes();
                        if (pane == null) 
                            continue;
                        for (int j=0; j<pane.length; j++) {
                            if (pane[j].getEditorKit() instanceof BaseKit) {
                                topComp.close();
                            }
                        }
                    }
                }
                
            }
        });
    }

    
    private static class HackMap extends Hashtable {
        
        private final Object LOCK = new String("EditorModule.HackMap.LOCK"); //NOI18N
        
	private Hashtable delegate;

        HackMap(Hashtable h) {
            delegate = h;
            
            if (debug) {
                LOG.log(Level.INFO, "Original kit mappings: " + h); //NOI18N

                try {
                    Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                    keyField.setAccessible(true);
                    Object key = keyField.get(JEditorPane.class);
                    
                    Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
                    Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
                    Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N
                    Method put = appContextClass.getDeclaredMethod("put", Object.class, Object.class); //NOI18N

                    Object appContext = getAppContext.invoke(null);
                    Hashtable<?,?> kitTypeMapping = (Hashtable<?,?>) get.invoke(appContext, key);

                    if (kitTypeMapping != null) {
                        put.invoke(appContext, key, new DebugHashtable(kitTypeMapping));
                    }
                    
// REMOVE: we should not depend on sun.* classes
//                    Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//                    if (kitTypeMapping != null) {
//                        sun.awt.AppContext.getAppContext().put(key, new DebugHashtable(kitTypeMapping));
//                    }
                } catch (Throwable t) {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.", t);
                }
            }
        }

        private String getKitClassName(String type) {
            try {
                Field keyField = JEditorPane.class.getDeclaredField("kitTypeRegistryKey");  // NOI18N
                keyField.setAccessible(true);
                Object key = keyField.get(JEditorPane.class);
                
                Class<?> appContextClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.AppContext"); //NOI18N
                Method getAppContext = appContextClass.getDeclaredMethod("getAppContext"); //NOI18N
                Method get = appContextClass.getDeclaredMethod("get", Object.class); //NOI18N

                Object appContext = getAppContext.invoke(null);
                Hashtable<?,?> kitTypeMapping = (Hashtable<?,?>) get.invoke(appContext, key);

                if (kitTypeMapping != null) {
                    return (String) kitTypeMapping.get(type);
                }

// REMOVE: we should not depend on sun.* classes
//                Hashtable kitTypeMapping = (Hashtable)sun.awt.AppContext.getAppContext().get(key);
//                if (kitTypeMapping != null) {
//                    return (String)kitTypeMapping.get(type);
//                }
            } catch (Throwable t) {
                if (debug) {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.", t);
                } else {
                    LOG.log(Level.WARNING, "Can't hack in to the JEditorPane's registry for kit types.");
                }
            }
            
            return null;
        }
        
        public @Override Object get(Object key) {
            synchronized (LOCK) {
            if (debug) LOG.log(Level.INFO, "HackMap.get key=" + key); //NOI18N
            
            Object retVal = null;
            
            if (delegate != null) {
                retVal = delegate.get(key);
                if (debug && retVal != null) {
                    LOG.log(Level.INFO, "Found cached instance kit=" + retVal + " for mimeType=" + key); //NOI18N
                }
            }

            if (key instanceof String) {
                String mimeType = (String) key;
                if (retVal == null || shouldUseNbKit(retVal.getClass().getName(), mimeType)) {
                    // first check the type registry
                    String kitClassName = getKitClassName(mimeType);
                    if (debug) {
                        LOG.log(Level.INFO, "Found kitClassName=" + kitClassName + " for mimeType=" + mimeType); //NOI18N
                    }

                    if (kitClassName == null || shouldUseNbKit(kitClassName, mimeType)) {
                        Object kit = findKit(mimeType);
                        if (kit != null) {
                            retVal = kit;
                            if (debug) {
                                LOG.log(Level.INFO, "Found kit=" + retVal + " in xml layers for mimeType=" + mimeType); //NOI18N
                            }
                        }
                    }
                }
            }
            
            return retVal;
            } // synchronized (Settings.class)
        }
        
        public @Override Object put(Object key, Object value) {
            synchronized (LOCK) {
            if (debug) LOG.log(Level.INFO, "HackMap.put key=" + key + " value=" + value); //NOI18N
            
            if (delegate == null) {
                delegate = new Hashtable();
            }

            Object ret = delegate.put(key,value);
            
            if (debug) {
                LOG.log(Level.INFO, "registering mimeType=" + key //NOI18N
                    + " -> kitInstance=" + value // NOI18N
                    + " original was " + ret); // NOI18N
            }
             
            return ret;
            } // synchronized (Settings.class)
        }

        public @Override Object remove(Object key) {
            synchronized (LOCK) {
            if (debug) LOG.log(Level.INFO, "HackMap.remove key=" + key); //NOI18N
            
            Object ret = (delegate != null) ? delegate.remove(key) : null;
            
            if (debug) {
                LOG.log(Level.INFO, "removing kitInstance=" + ret //NOI18N
                    + " for mimeType=" + key); // NOI18N
            }
            
            return ret;
            } // synchronized (Settings.class)
        }
        
        Hashtable getOriginal() {
            return delegate;
        }

        private boolean shouldUseNbKit(String kitClass, String mimeType) {
            if (mimeType.startsWith("text/html") || //NOI18N
                mimeType.startsWith("text/rtf") || //NOI18N
                mimeType.startsWith("application/rtf")) //NOI18N
            {
                return false;
            } else {
                return kitClass.startsWith("javax.swing."); //NOI18N
            }
        }

        // Don't use CloneableEditorSupport.getEditorKit so that it can safely
        // fallback to JEP.createEKForCT if it doesn't find Netbeans kit.
        private EditorKit findKit(String mimeType) {
            if (!MimePath.validate(mimeType)) // #146276 - exclude invalid mime paths
                return null;
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            EditorKit kit = (EditorKit) lookup.lookup(EditorKit.class);
            return kit == null ? null : (EditorKit) kit.clone();
        }
    }
    
    private static final class DebugHashtable extends Hashtable {
        
        DebugHashtable(Hashtable h) {
            if (h != null) {
                putAll(h);
                LOG.log(Level.INFO, "Existing kit classNames mappings: " + this); //NOI18N
            }
        }
        
        public @Override Object put(Object key, Object value) {
            Object ret = super.put(key, value);
            LOG.log(Level.INFO, "registering mimeType=" + key //NOI18N
                + " -> kitClassName=" + value // NOI18N
                + " original was " + ret); // NOI18N
            return ret;
        }
        
        public @Override Object remove(Object key) {
            Object ret = super.remove(key);
            LOG.log(Level.INFO, "removing kitClassName=" + ret //NOI18N
                + " for mimeType=" + key); // NOI18N
            return ret;
        }
        
    }

    private void initAndCheckEditorKitTypeRegistry(String mimeType, String expectedKitClass) {
        String kitClass = JEditorPane.getEditorKitClassNameForContentType(mimeType);
        if (kitClass == null) {
            LOG.log(Level.WARNING, "Can't find JDK editor kit class for " + mimeType); //NOI18N
        } else if (expectedKitClass != null && !expectedKitClass.equals(kitClass)) {
            LOG.log(Level.WARNING, "Wrong JDK editor kit class for " + mimeType + //NOI18N
                ". Expecting: " + expectedKitClass + //NOI18N
                ", but was: " + kitClass); //NOI18N
        }
    }
    
    private void reformat(DataObject file) {
        try {
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);

            if (ec == null) return;
            
            final StyledDocument doc = ec.openDocument();
            final Reformat reformat = Reformat.get(doc);
            String defaultLineSeparator = (String) file.getPrimaryFile().getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
            if (defaultLineSeparator != null) {
                doc.putProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR, defaultLineSeparator);
            }
            
            reformat.lock();
            
            try {
                NbDocument.runAtomicAsUser(doc, new Runnable() {

                    @Override
                    public void run() {
                        doc.putProperty("code-template-insert-handler", true);
                        try {
                            EditorPackageAccessor.get().ActionFactory_reformat(reformat, doc, 0, doc.getLength(), new AtomicBoolean());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            doc.putProperty("code-template-insert-handler", null);
                        }
                    }
                });
                
            } finally {
                reformat.unlock();
                defaultLineSeparator = (String) doc.getProperty(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
                if (defaultLineSeparator != null) {
                    doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, defaultLineSeparator);
                } else {
                    doc.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, ReadWriteUtils.getSystemLineSeparator());
                }
                ec.saveDocument();
            }
            
            //clear the undo queue:
            ec.close();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
