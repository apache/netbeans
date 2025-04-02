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

package org.netbeans.modules.xml.wizard.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.wizard.SchemaParser;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;

/**
 * Represents a collection of external references, or a single file.
 *
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class ExternalReferenceDataNode extends FilterNode {
    /** Name of the 'selected' property. */
    public static final String PROP_SELECTED = "selected";
    /** Name of the 'prefix' property. */
    public static final String PROP_PREFIX = "prefix";
    /** Set of PropertySets. */
    private Sheet sheet;
    /** True if selected, false otherwise. */
    private boolean selected;
    /** The namespace prefix, if specified. */
    private String prefix=null;
    private static int counter=0;
    private ExternalReferenceDecorator decorator;
    private boolean resolveThroughCatalog;

    /**
     * Creates a new instance of ExternalReferenceDataNode.
     *
     * @param  original   the delegate Node.
     * @param  decorator  the external reference decorator.
     */
    public ExternalReferenceDataNode(Node original,ExternalReferenceDecorator dec) {
        super(original, new Children(original, dec));
        this.decorator=dec;
       
    }
    
    public void setResolveThroughCatalog(boolean resolve) {
        this.resolveThroughCatalog = resolve;
    }

    public boolean canRename() {
        // Disable rename as it serves no purpose here and makes the
        // single-click-select-toggle difficult to use.
        return false;
    }

    /**
     * Indicates if this node allows setting it selected.
     *
     * @return  true if this node can be selected, false otherwise.
     */
    public boolean canSelect() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        return dobj != null && !dobj.getPrimaryFile().isFolder() ;
    }

    /**
     * Creates a node property of the given key (same as the column keys)
     * and specific getter/setter methods on the given object.
     *
     * @param  key     property name (same as matching column).
     * @param  type    Class of the property (e.g. String.class).
     * @param  inst    object on which to reflect.
     * @param  getter  name of getter method for property value.
     * @param  setter  name of setter method for property value (may be null).
     * @return  new property.
     */
    private Node.Property createProperty(String key, Class type, Object inst,
            String getter, String setter) {
        Property prop = null;
        try {
            prop = new Reflection(inst, type, getter, setter);
            prop.setName(key);
            prop.setDisplayName(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_SchemaPanel_Column_Name_" + key));
            prop.setShortDescription(NbBundle.getMessage(
                    ExternalReferenceDataNode.class,
                    "CTL_SchemaPanel_Column_Desc_" + key));
        }  catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
        }
        return prop;
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, String.class, this,
                "getHtmlDisplayName", null));
        if (canSelect()) {
            set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
                    "isSelected", "setSelected"));
       //     Node.Property prop = createProperty(PROP_PREFIX, String.class,
           //         this, "getPrefix", "setPrefix");
            // Suppress the [...] button because it is not needed.
        //    prop.setValue("suppressCustomEditor", Boolean.TRUE);
        //    set.put(prop);
        } /*else {
            // Do not include this property so the checkbox is not shown.
            //set.put(createProperty(PROP_SELECTED, Boolean.TYPE, this,
            //        "isSelected", null));
            Node.Property prop = createProperty(PROP_PREFIX, String.class,
                    this, "getPrefix", null);
            // Suppress the [...] button because it is not needed.
            prop.setValue("suppressCustomEditor", Boolean.TRUE);
            set.put(prop);
        }*/
        return sheet;
    }

    protected final synchronized Sheet getSheet() {
        if (sheet != null) {
            return sheet;
        }
        sheet = createSheet();
        firePropertySetsChange(null, null);
        return sheet;
    }

    public PropertySet[] getPropertySets() {
        Sheet s = getSheet();
        return s.toArray();
    }

    public String getHtmlDisplayName() {
        String name = getOriginal().getHtmlDisplayName();
        return name;
    }

    public String getPrefix() {
        
        if (prefix == null) {
            DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
            if( dobj !=null && !(dobj.getPrimaryFile().isFolder()) )
                prefix = decorator.generatePrefix(prefix, dobj);
            else
                prefix ="";
        }
        return prefix;
    }

    public boolean isSelected() {
        return selected;
    }


    public void setDisplayName(String s) {
        super.disableDelegation(DELEGATE_GET_DISPLAY_NAME|DELEGATE_SET_DISPLAY_NAME);
        super.setDisplayName(s);
    }

    /**
     * Set the namespace prefix for this node.
     *
     * @param  prefix  new namespace prefix.
     */
    public void setPrefix(String prefix) {
        String old = this.prefix;
        this.prefix = prefix;
        firePropertyChange(PROP_PREFIX, old, prefix);
    }

    /**
     * Mark this node as selected.
     *
     * @param  selected  true to select, false to unselect.
     */
    public void setSelected(boolean selected) {
        if (!canSelect()) {
            throw new IllegalStateException("node cannot be selected");
        }
        boolean old = this.selected;
        this.selected = selected;
        firePropertyChange(PROP_SELECTED, old, selected);
    }
    
    public String getNamespace() {
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
             FileObject fobj = dobj.getPrimaryFile();
             return SchemaParser.getNamespace(fobj);
        }
        return null;
    }

    public boolean isResolveThroughCatalog() {
        return resolveThroughCatalog;
    }
    
    public String getSchemaFileName(){
        DataObject dobj = (DataObject) getLookup().lookup(DataObject.class);
        if (dobj != null) {
             FileObject fobj = dobj.getPrimaryFile();
             File file = FileUtil.toFile(fobj);
             String uri = file.getPath();
             if (uri != null) {
               try {
                    // escape the non-ASCII characters
                    uri = new URI(uri).toASCIIString();
                } catch (URISyntaxException e) {
                  // the specified uri is not valid, it is too late to fix it now
                }
             }
             return uri;
        }
        
        return null;
        
    }
    
      private static class Children extends FilterNode.Children {
        /** Controls the appearance of child nodes. */
        ExternalReferenceDecorator decorator;
        
        public Children(Node original, ExternalReferenceDecorator dec) {
            super(original);
            this.decorator=dec;
        }

        protected Node[] createNodes(Node n) {
            DataObject dobj = (DataObject) n.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                FileObject fobj = dobj.getPrimaryFile();
                if (fobj.isFolder() && fobj.getNameExt().equals("nbproject") &&
                        fobj.getFileObject("project.xml") != null) {
                    // It is the NetBeans project folder, ignore it.
                    return new Node[0];
                }
                String fname = fobj.getNameExt();
                String ext = decorator.getDocumentType();
                if (fobj.isFolder() || fname.endsWith(ext)) {
                    return super.createNodes(n);
                }
            }
            return new Node[0];
        }
        
        protected Node copyNode(Node node) {
            return decorator.createExternalReferenceNode(node);
        }

    }
}
