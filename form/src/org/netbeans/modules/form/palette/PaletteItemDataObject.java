/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.form.palette;

import java.util.*;
import java.io.*;
import java.beans.*;

import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.xml.XMLUtil;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import org.netbeans.modules.form.project.ClassSource;

/**
 * DataObject for palette item file. It reads the file and creates PaletteItem
 * and node from it.
 *
 * @author Tomas Pavek
 */
@MIMEResolver.ExtensionRegistration(
    position=50037, 
    displayName="org/netbeans/modules/form/resources/Bundle#Services/MIMEResolver/SwingPaletteItemResolver.xml",
    mimeType="text/x-palette-item",
    extension="palette_item"
)
public class PaletteItemDataObject extends MultiDataObject implements CookieSet.Factory {

    static final String XML_ROOT = "palette_item"; // NOI18N
    static final String ATTR_VERSION = "version"; // NOI18N
    static final String TAG_COMPONENT = "component"; // NOI18N
    static final String ATTR_CLASSNAME = "classname"; // NOI18N
    static final String ATTR_TYPE = "type"; // NOI18N
    static final String ATTR_INITIALIZER_ID = "initializer-id"; // NOI18N
//    static final String ATTR_IS_CONTAINER = "is-container"; // NOI18N
    static final String TAG_CLASSPATH = "classpath"; // NOI18N
    static final String TAG_RESOURCE= "resource"; // NOI18N
    static final String ATTR_NAME = "name"; // NOI18N
    static final String TAG_DESCRIPTION = "description"; // NOI18N
    static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N
    static final String TAG_ICON16 = "icon16"; // NOI18N
    static final String ATTR_URL = "urlvalue"; // NOI18N
    static final String TAG_ICON32 = "icon32"; // NOI18N
    // component types: "visual", "menu", "layout", "border"

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private boolean fileLoaded; // at least tried to load

    private PaletteItem paletteItem;

    // some raw data read from the file (other passed to PaletteItem)
    private String displayName_key;
    private String tooltip_key;
    private String bundleName;
    private String icon16URL;
    private String icon32URL;

    // resolved data (derived from raw data)
    String displayName;
    String tooltip;
    java.awt.Image icon16;
    java.awt.Image icon32;

    // --------

    PaletteItemDataObject(FileObject fo, MultiFileLoader loader)
        throws DataObjectExistsException
    {
        super(fo, loader);
        getCookieSet().add(PaletteItem.class, this);
    }

    boolean isFileRead() {
        return fileLoaded;
    }

    boolean isItemValid() {
        return paletteItem != null;
    }

    void reloadFile() {
        if (paletteItem != null) {
            paletteItem.reset(); // resets resolved data (but not raw data)

            paletteItem.componentClassSource = null;
//            paletteItem.isContainer_explicit = null;
            paletteItem.componentType_explicit = null;
            paletteItem.setComponentInitializerId(null);
        }

        displayName = null;
        tooltip = null;
        icon16 = null;
        icon32 = null;

        displayName_key = null;
        tooltip_key = null;
        bundleName = null;
        icon16URL = null;
        icon32URL = null;

        loadFile();
    }

    // ------

    @Override
    public Node createNodeDelegate() {
        return new ItemNode();
    }

    @Override
    public <T extends Node.Cookie> T createCookie(Class<T> cookieClass) {
        if (PaletteItem.class.equals(cookieClass)) {
            if (!fileLoaded)
                loadFile();
            return cookieClass.cast(paletteItem);
        }
        return null;
    }

    // -------

    private void loadFile() {
        fileLoaded = true;
        PaletteItem item = paletteItem;
        if (item == null)
            item = new PaletteItem(this);

        FileObject file = getPrimaryFile();
        if (file.getSize() == 0L) { // item file is empty
            // just derive the component class name from the file name
            item.setComponentClassSource(new ClassSource(file.getName().replace('-', '.')));
            paletteItem = item;
            return;
        }
        
        // parse the XML file
        try {
            XMLReader reader = XMLUtil.createXMLReader();
            PaletteItemHandler handler = new PaletteItemHandler();
            reader.setContentHandler(handler);
            InputSource input = new InputSource(getPrimaryFile().getURL().toExternalForm());
            reader.parse(input);
            // TODO report errors, validate using DTD?
            
            item.setComponentExplicitType(handler.componentExplicitType);
            item.setComponentInitializerId(handler.componentInitializerId);
            if (handler.componentClassName != null || displayName_key != null) {
                item.setComponentClassSource(new ClassSource(handler.componentClassName, handler.entries));
                paletteItem = item;
            }
        } catch (SAXException saxex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, saxex);
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioex);
        }
    }

    /**
     * @param folder folder of category where to create new file
     * @param classname name of the component class
     * @param source classpath source type - "jar", "library", "project"
     * @param classpath names of classpath roots - e.g. JAR file paths
     */
    public static FileObject createFile(FileObject folder, ClassSource classSource)
        throws IOException
    {
        String classname = classSource.getClassName();

        int idx = classname.lastIndexOf('.');
        String fileName = FileUtil.findFreeFileName(
            folder,
            idx >= 0 ? classname.substring(idx+1) : classname,
            PaletteItemDataLoader.ITEM_EXT);

        FileObject itemFile = folder.createData(fileName,
                                                PaletteItemDataLoader.ITEM_EXT);

        StringBuilder buff = new StringBuilder(512);
        buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
        buff.append("<palette_item version=\"1.0\">\n"); // NOI18N
        buff.append("  <component classname=\""); // NOI18N
        buff.append(classname);
        buff.append("\" />\n"); // NOI18N
        buff.append("  <classpath>\n"); // NOI18N
        for (ClassSource.Entry entry : classSource.getEntries()) {
            buff.append("      <resource type=\""); // NOI18N
            buff.append(entry.getPicklingType());
            buff.append("\" name=\""); // NOI18N
            buff.append(entry.getPicklingName());
            buff.append("\" />\n"); // NOI18N
        }
        buff.append("  </classpath>\n"); // NOI18N
        buff.append("</palette_item>\n"); // NOI18N

        FileLock lock = itemFile.lock();
        OutputStream os = itemFile.getOutputStream(lock);
        try {
            os.write(buff.toString().getBytes("UTF-8")); // NOI18N
        }
        finally {
            os.close();
            lock.releaseLock();
        }
        return itemFile;
    }

    // -------

    /** DataLoader for the palette item files. */
    public static final class PaletteItemDataLoader extends UniFileLoader {

        static final String ITEM_EXT = "palette_item"; // NOI18N
        static final String ITEM_MIME = "text/x-palette-item"; // NOI18N

        PaletteItemDataLoader() {
            super("org.netbeans.modules.form.palette.PaletteItemDataObject"); // NOI18N

            ExtensionList ext = new ExtensionList();
            ext.addMimeType(ITEM_MIME);
            setExtensions(ext);
        }
        
        /** Gets default display name. Overides superclass method. */
        @Override
        protected String defaultDisplayName() {
            return NbBundle.getBundle(PaletteItemDataObject.class)
            .getString("PROP_PaletteItemLoader_Name"); // NOI18N
        }
        

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile)
            throws DataObjectExistsException, IOException
        {
            return new PaletteItemDataObject(primaryFile, this);
        }
    }
    
    public static final class PaletteItemDataLoaderBeanInfo extends SimpleBeanInfo {
        private static String iconURL = "org/netbeans/modules/form/resources/palette_manager.png"; // NOI18N
        
        @Override
        public BeanInfo[] getAdditionalBeanInfo() {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo(UniFileLoader.class) };
            } catch (IntrospectionException ie) {
                org.openide.ErrorManager.getDefault().notify(ie);
                return null;
            }
        }
        
        @Override
        public java.awt.Image getIcon(final int type) {
            return ImageUtilities.loadImage(iconURL);
        }
        
    }

    // --------

    /** Node representing the palette item (node delegate for the DataObject). */
    class ItemNode extends DataNode {

        ItemNode() {
            super(PaletteItemDataObject.this, Children.LEAF);
        }

        @Override
        public String getDisplayName() {
            if (!fileLoaded)
                loadFile();

            if (displayName == null) {
                displayName = getExplicitDisplayName();
                if (displayName == null) { // no explicit name
                    if (isItemValid()) {
                        displayName = paletteItem.getDisplayName();
                        if (displayName == null) { // no name from BeanDescriptor
                            String classname = paletteItem.getComponentClassName();
                            if (classname != null) {
                                int i = classname.lastIndexOf('.'); // NOI18N
                                displayName = i >= 0 ?
                                    classname.substring(i+1) : classname;
                            }
                        }
                    }
                    if (displayName == null) // no name derived from the item
                        displayName = super.getDisplayName();
                }
            }
            return displayName;
        }

        @Override
        public String getShortDescription() {
            if (!fileLoaded)
                loadFile();

            if (tooltip == null) {
                tooltip = getExplicitTooltip();
                if (tooltip == null) { // no explicit tooltip
                    if (isItemValid()) {
                        tooltip = paletteItem.getTooltip();
                        if (tooltip == null) // no tooltip from BeanDescriptor
                            tooltip = paletteItem.getComponentClassName();
                    }
                    if (tooltip == null) // no tooltip derived from the item
                        tooltip = getDisplayName();
                }
            }
            return tooltip;
        }

        @Override
        public boolean canRename() {
            return false;
        }

        @Override
        public java.awt.Image getIcon(int type) {
            if (!fileLoaded)
                loadFile();

            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32)
            {
                if (icon32 == null) {
                    icon32 = getExplicitIcon(type);
                    if (icon32 == null && isItemValid())
                        icon32 = paletteItem.getIcon(type);
                    if (icon32 == null)
                        icon32 = ImageUtilities.loadImage("org/netbeans/modules/form/resources/palette/unknown32.gif"); // NOI18N
                }
                return icon32;
            }
            else { // small icon by default
                if (icon16 == null) {
                    icon16 = getExplicitIcon(type);
                    if (icon16 == null && isItemValid())
                        icon16 = paletteItem.getIcon(type);
                    if (icon16 == null)
                        icon16 = ImageUtilities.loadImage("org/netbeans/modules/form/resources/palette/unknown.gif"); // NOI18N
                }
                return icon16;
            }
            // TODO badged icon for invalid item?
        }

        // TODO properties
        @Override
        public Node.PropertySet[] getPropertySets() {
            return NO_PROPERTIES;
        }

        // ------

        private String getExplicitDisplayName() {
            String displayName = null;
            if (displayName_key != null) {
                if (bundleName != null) {
                    try {
                        displayName = NbBundle.getBundle(bundleName)
                                                .getString(displayName_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (displayName == null)
                    displayName = displayName_key;
            }
            return displayName;
        }

        private String getExplicitTooltip() {
            String tooltip = null;
            if (tooltip_key != null) {
                if (bundleName != null) {
                    try {
                        tooltip = NbBundle.getBundle(bundleName)
                                            .getString(tooltip_key);
                    }
                    catch (Exception ex) {} // ignore failure
                }
                if (tooltip == null)
                    tooltip = tooltip_key;
            }
            return tooltip;
        }

        private java.awt.Image getExplicitIcon(int type) {
            if (type == BeanInfo.ICON_COLOR_32x32
                    || type == BeanInfo.ICON_MONO_32x32)
            {
                if (icon32URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon32URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else if (getPrimaryFile().getAttribute("SystemFileSystem.icon32") != null) // NOI18N
                    return super.getIcon(type);
            }
            else { // get small icon in other cases
                if (icon16URL != null) { // explicit icon specified in file
                    try {
                        return java.awt.Toolkit.getDefaultToolkit().getImage(
                                                 new java.net.URL(icon16URL));
                    }
                    catch (java.net.MalformedURLException ex) {} // ignore
                }
                else if (getPrimaryFile().getAttribute("SystemFileSystem.icon") != null) // NOI18N
                    return super.getIcon(type);
            }
            return null;
        }
    }
    
    private class PaletteItemHandler extends DefaultHandler {
        List<ClassSource.Entry> entries;
        String componentClassName;
        String componentExplicitType;
        String componentInitializerId;
        
        @Override
        public void startDocument() throws SAXException {
            entries = new ArrayList<ClassSource.Entry>();
            componentClassName = null;
            componentExplicitType = null;
            componentInitializerId = null;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
            if (XML_ROOT.equals(qName)) {
                String version = attributes.getValue(ATTR_VERSION);
                if (version == null) {
                    String message = NbBundle.getBundle(PaletteItemDataObject.class)
                        .getString("MSG_UnknownPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                } else if (!version.startsWith("1.")) { // NOI18N
                    String message = NbBundle.getBundle(PaletteItemDataObject.class)
                        .getString("MSG_UnsupportedPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                }
                // TODO item ID (for now we take the class name as the ID)
            } else if (TAG_COMPONENT.equals(qName)) {
                String className = attributes.getValue(ATTR_CLASSNAME);
                componentClassName = className;
                componentExplicitType = attributes.getValue(ATTR_TYPE);
                componentInitializerId = attributes.getValue(ATTR_INITIALIZER_ID);
            } else if (TAG_CLASSPATH.equals(qName)) {
                // Content is processed in the next branch
            } else if (TAG_RESOURCE.equals(qName)) {
                String type = attributes.getValue(ATTR_TYPE);
                String name = attributes.getValue(ATTR_NAME);
                if ((type != null) && (name != null)) {
                    ClassSource.Entry entry = ClassSource.unpickle(type, name);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            } else if (TAG_DESCRIPTION.equals(qName)) {
                String bundle = attributes.getValue(ATTR_BUNDLE);
                if (bundle != null) {
                    PaletteItemDataObject.this.bundleName = bundle;
                }
                String displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
                if (displayNameKey != null) {
                    PaletteItemDataObject.this.displayName_key = displayNameKey;
                }
                String tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
                if (tooltipKey != null) {
                    PaletteItemDataObject.this.tooltip_key = tooltipKey;
                }
            } else if (TAG_ICON16.equals(qName)) {
                String url = attributes.getValue(ATTR_URL);
                if (url != null) {
                    PaletteItemDataObject.this.icon16URL = url;
                }
                // TODO support also class resource name for icons
            } else if (TAG_ICON32.equals(qName)) {
                String url = attributes.getValue(ATTR_URL);
                if (url != null) {
                    PaletteItemDataObject.this.icon32URL = url;
                }
                // TODO support also class resource name for icons
            }
        }
    }
    
    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
}
