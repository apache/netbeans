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

package org.netbeans.installer.product;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.NbiProperties;
import org.netbeans.installer.utils.helper.PropertyContainer;
import org.netbeans.installer.utils.helper.UiMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class RegistryNode implements PropertyContainer {
    protected RegistryNode parent;
    
    protected RegistryType registryType;
    
    protected String uid;
    
    protected ExtendedUri iconUri;
    protected Icon icon;
    
    protected long offset;
    protected boolean expand;
    protected boolean initialVisible;
    protected boolean currentVisible;
    
    protected Date built;
    
    protected Map<Locale, String> displayNames;
    protected Map<Locale, String> descriptions;
    
    protected List<RegistryNode> children;
    
    protected NbiProperties properties;
    
    protected RegistryNode() {
        initialVisible = true;
        currentVisible = true;
        built = new Date();
        
        displayNames = new HashMap<Locale, String>();
        descriptions = new HashMap<Locale, String>();
        
        children = new ArrayList<RegistryNode>();
        
        properties = new NbiProperties();
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }
    
    public String getDisplayName(final Locale locale) {
        return StringUtils.getLocalizedString(displayNames,locale);
    }
    
    public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }
    
    public void setDisplayName(final String displayName) {
        setDisplayName(Locale.getDefault(), displayName);
    }
    
    public void setDisplayName(final Locale locale, final String displayName) {
        displayNames.put(locale, displayName);
    }
    
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return StringUtils.getLocalizedString(descriptions,locale);
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public void setDescription(final String description) {
        setDescription(Locale.getDefault(), description);
    }
    
    public void setDescription(final Locale locale, final String description) {
        descriptions.put(locale, description);
    }
    
    public ExtendedUri getIconUri() {
        return iconUri;
    }
    
    public Icon getIcon() {
        if (icon==null && UiMode.getCurrentUiMode() != UiMode.SILENT) {
            if(getIconUri()!=null && getIconUri().getLocal()!=null) {        
                icon = new ImageIcon(getIconUri().getLocal().getPath());
            }
        }
        return icon;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public boolean isVisible() {
        return currentVisible;
    }
    
    public void setVisible(final boolean visible) {
        this.currentVisible = visible;
    }
    
    public boolean getExpand() {
        return expand;
    }
    
    public Date getBuildDate() {
        return built;
    }
    
    public RegistryType getRegistryType() {
        return registryType;
    }
    
    public void setRegistryType(final RegistryType registryType) {
        this.registryType = registryType;
    }
    
    // tree /////////////////////////////////////////////////////////////////////////
    public RegistryNode getParent() {
        return parent;
    }
    
    public void setParent(final RegistryNode parent) {
        this.parent = parent;
    }
    
    public List<RegistryNode> getChildren() {
        return children;
    }
    
    public List<RegistryNode> getVisibleChildren() {
        List<RegistryNode> visibleChildren = new LinkedList<RegistryNode>();
        
        for (RegistryNode child: children) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        
        return visibleChildren;
    }
    
    public void addChild(final RegistryNode child) {
        child.setParent(this);
        
        int i;
        for (i = 0; i < children.size(); i++) {
            if (children.get(i).getOffset() > child.getOffset()) {
                break;
            }
        }
        children.add(i, child);
    }
    
    public void removeChild(final RegistryNode child) {
        children.remove(child);
    }
    
    public void attachRegistry(Registry registry) {
        for (RegistryNode node: registry.getRegistryRoot().getChildren()) {
            addChild(node);
        }
    }
    
    public boolean isAncestor(final RegistryNode candidate) {
        for (RegistryNode node: getChildren()) {
            if ((node == candidate) || node.isAncestor(candidate)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isAncestor(final List<? extends RegistryNode> candidates) {
        for (RegistryNode node: candidates) {
            if (isAncestor(node)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean hasChildren() {
        return hasChildren(new TrueFilter());
    }
    
    public boolean hasChildren(RegistryFilter filter) {
        for (RegistryNode child: children) {
            if (filter.accept(child)) {
                return true;
            }
            
            if (child.hasChildren(filter)) {
                return true;
            }
        }
        
        return false;
    }
    
    public TreePath getTreePath() {
        List<RegistryNode> nodes = new LinkedList<RegistryNode>();
        
        RegistryNode node = this;
        while (node != null) {
            nodes.add(0, node);
            node = node.getParent();
        }
        
        return new TreePath(nodes.toArray());
    }
    
    // properties ///////////////////////////////////////////////////////////////////
    public Properties getProperties() {
        return properties;
    }
    
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }
    
    public void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    public Element saveToDom(Document document, RegistryFilter filter) throws FinalizationException {
        final boolean hasChilren = hasChildren(filter);
        
        if (filter.accept(this) || hasChilren) {
            Element element = saveToDom(document.createElement(getTagName()));
            
            if (hasChilren) {
                element.appendChild(saveChildrenToDom(document, filter));
            }
            
            return element;
        } else {
            return null;
        }
    }
    
    public Element saveChildrenToDom(Document document, RegistryFilter filter) throws FinalizationException {
        Element components = null;
        
        if (hasChildren(filter)) {
            components = document.createElement("components");
            
            for (RegistryNode child: children) {
                if (filter.accept(child) || child.hasChildren(filter)) {
                    components.appendChild(child.saveToDom(document, filter));
                }
            }
        }
        
        return components;
    }
    
    protected String getTagName() {
        return "node";
    }
    
    protected Element saveToDom(
            final Element element) throws FinalizationException {
        Document document = element.getOwnerDocument();
        
        element.setAttribute("uid", uid);
        element.setAttribute("offset", Long.toString(offset));
        element.setAttribute("expand", Boolean.toString(expand));
        element.setAttribute("visible", Boolean.toString(initialVisible));
        
        element.setAttribute("built", Long.toString(built.getTime()));
        
        element.appendChild(XMLUtils.saveLocalizedString(
                displayNames,
                document.createElement("display-name")));
        
        element.appendChild(XMLUtils.saveLocalizedString(
                descriptions,
                document.createElement("description")));
        
        element.appendChild(XMLUtils.saveExtendedUri(
                iconUri,
                document.createElement("icon")));
        
        if (properties.size() > 0) {
            element.appendChild(XMLUtils.saveNbiProperties(
                    properties,
                    document.createElement("properties")));
        }
        
        return element;
    }
    
    public RegistryNode loadFromDom(
            final Element element) throws InitializationException {
        try {
            uid = element.getAttribute("uid");
            
            iconUri = XMLUtils.parseExtendedUri(
                    XMLUtils.getChild(element, "icon"));
            
            if (!Boolean.getBoolean(Registry.LAZY_LOAD_ICONS_PROPERTY)) {
                final File iconFile =
                        FileProxy.getInstance().getFile(iconUri.getRemote());
                iconUri.setLocal(iconFile.toURI());
            }
            
            offset = Long.parseLong(element.getAttribute("offset"));
            currentVisible =
                    initialVisible =
                    Boolean.parseBoolean(element.getAttribute("visible"));
            expand = Boolean.parseBoolean(element.getAttribute("expand"));
            
            built = new Date(Long.parseLong(element.getAttribute("built")));
            
            displayNames = XMLUtils.parseLocalizedString(
                    XMLUtils.getChild(element, "display-name"));
            
            descriptions = XMLUtils.parseLocalizedString(
                    XMLUtils.getChild(element, "description"));
            
            properties = XMLUtils.parseNbiProperties(
                    XMLUtils.getChild(element, "properties"));
        } catch (ParseException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        } catch (DownloadException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        } catch (NumberFormatException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        }
        
        return this;
    }
    
    // node -> string ///////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return getDisplayName();
    }
}
