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
package org.netbeans.modules.web.jsf.editor.facelets;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.index.JsfIndex;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.LibraryType;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author marekfukala
 */
public class CompositeComponentLibrary extends FaceletsLibrary {

    //https://javaserverfaces.java.net/docs/2.2/vdldocs/facelets/ui/component.html
    private static final String[] DEFAULT_COMPONENT_ATTRS = new String[]{
        "id", "binding", "rendered"}; //NOI18N

    /**
     * Name of the folder/s where the composite library components are located. 
     * It's usually placed under the META-INF/resources folder inside the library archive.
     */
    private final String compositeLibraryResourceFolderName;
    private final String defaultPrefix;
    private Map<String, CompositeComponent> compositeComponentsMap;

    //for cc libraries with facelets library descriptor, the constructor is called by Mojarra
    public CompositeComponentLibrary(FaceletsLibrarySupport support, String compositeLibraryName, Set<String> namespace, URL libraryDescriptorURL) {
        super(support, namespace, libraryDescriptorURL);

        this.compositeLibraryResourceFolderName = compositeLibraryName;

        //the default prefix is always computed from the composite library location
        //since even if there's a descriptor for the library, it doesn't contain
        //such information
        this.defaultPrefix = generateVirtualLibraryPrefix();

        index().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                synchronized(CompositeComponentLibrary.this) {
                    compositeComponentsMap = null;
                }
            }
        });
    }

    @Override
    protected LibraryDescriptor getFaceletsLibraryDescriptor() throws LibraryDescriptorException {
        //return a composite (merged) descriptor from the xml descriptor and the composite components themselves
        return new CompositeLibraryDescriptor(super.getFaceletsLibraryDescriptor(), new CCVirtualLibraryDescriptor());
    }

    @Override
    public LibraryType getType() {
        return LibraryType.COMPOSITE;
    }

    @Override
    public String getDefaultNamespace() {
        return getNamespace();
    }

    @Override
    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public String getLibraryName() {
        return compositeLibraryResourceFolderName;
    }

    @Override
    public Map<String, ? extends NamedComponent> getComponentsMap() {
        //add the composite components to the class components
        Map<String, NamedComponent> all = new HashMap<>(super.getComponentsMap());
        all.putAll(getCompositeComponentsMap());
        return all;
    }

    private synchronized Map<String, CompositeComponent> getCompositeComponentsMap() {
        if (compositeComponentsMap == null) {
            compositeComponentsMap = new HashMap<>();
            Collection<String> componentNames = index().getCompositeLibraryComponents(getLibraryName());
            for (String compName : componentNames) {
                CompositeComponent comp = new CompositeComponent(compName);
                compositeComponentsMap.put(compName, comp);
            }
        }
        return compositeComponentsMap;
    }

    private JsfIndex index() {
        return support.getJsfSupport().getIndex();
    }

    private String generateVirtualLibraryPrefix() {
        StringTokenizer st = new StringTokenizer(getLibraryName(), "/"); //NOI18N
        LinkedList<String> tokens = new LinkedList<>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }

        //one or more tokens left
        if (tokens.size() == 1) {
            //just library folder
            return tokens.peek();
        } else {
            //more folders
            StringBuilder sb = new StringBuilder();
            for (String folderName : tokens) {
                sb.append(folderName.charAt(0)); //add first char
            }
            return sb.toString();
        }

    }

    public class CompositeComponent extends NamedComponent {

        public CompositeComponent(String name) {
            super(name);
        }

        public CompositeComponentModel getComponentModel() {
            return index().getCompositeComponentModel(getLibraryName(), name);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamedComponent other = (NamedComponent) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }
    }

    protected class CCVirtualLibraryDescriptor implements LibraryDescriptor, ChangeListener {

        protected Map<CompositeComponent, CompositeComponentModel> modelsCache = new HashMap<>();

        public CCVirtualLibraryDescriptor() {
            index().addChangeListener(WeakListeners.change(this, null));
        }

        @Override
        public Map<String, Tag> getTags() {
            Map<String, Tag> map = new HashMap<>();
            Collection<CompositeComponent> components = getCompositeComponentsMap().values();
            for (CompositeComponent cc : components) {
                map.put(cc.getName(), new LazyLoadingTag(cc));
            }
            return map;
        }

        @Override
        public String getNamespace() {
            return CompositeComponentLibrary.this.getNamespace();
        }

        @Override
        public String getPrefix() {
            return null;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            modelsCache.clear();
        }
        
        private class LazyLoadingTag extends GenericTag {

            private final CompositeComponent cc;
            private Map<String, Attribute> attrs;
            private String description;

            public LazyLoadingTag(CompositeComponent cc) {
                this.cc = cc;
            }

            @Override
            protected Map<String, Attribute> getAdditionalGenericAttributes() {
                Map<String, Attribute> result = new HashMap<>();
                for (String name : DEFAULT_COMPONENT_ATTRS) {
                    result.put(name,
                            new Attribute.DefaultAttribute(name,
                                    NbBundle.getMessage(CompositeComponentLibrary.class, new StringBuilder().append("HELP_").append(name).toString()), false));
                }
                return result;
            }

            private synchronized void load() {
                CompositeComponentModel model = modelsCache.get(cc);
                if (model == null) {
                    model = cc.getComponentModel();
                    if (model != null) {
                        modelsCache.put(cc, model);
                    } else {
                        return;
                    }
                }
                String relativePath = model.getRelativePath();

                attrs = new HashMap<>();
                String msgNoTld = NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_NO_DESCRIPTOR"); //NOI18N
                for (Map<String, String> attrsMap : model.getExistingInterfaceAttributes()) {
                    String attrname = attrsMap.get("name"); //NOI18N
                    boolean required = Boolean.parseBoolean(attrsMap.get("required")); //NOI18N
                    String attributeDescription = attrsMap.get("shortDescription"); //NOI18N
                    String defaultValue = attrsMap.get("default"); //NOI18N
                    String attributeType = attrsMap.get("type"); //NOI18N
                    String methodSignature = attrsMap.get("method-signature"); //NOI18N
                    attrs.put(attrname, new Attribute.DefaultAttribute(attrname, attributeDescription, attributeType, required, methodSignature, defaultValue));
                }

                StringBuilder sb = new StringBuilder();
                
                if(model.getDisplayName() != null) {
                    sb.append("<p>").append("<b>")
                    .append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_DISPLAYNAME"))
                    .append("</b>").append("&nbsp;")
                    .append(model.getDisplayName())
                    .append("</p>"); //NOI18N
                }
                
                if(model.getShortDescription() != null) {
                    sb.append("<p>").append("<b>")
                    .append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_SHORTDESCRIPTION"))
                    .append("</b>").append("&nbsp;")
                    .append(model.getShortDescription())
                    .append("</p>"); //NOI18N
                }
                
                sb.append("<p>"); //NOI18N
                sb.append("<b>");//NOI18N
                sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_COMPOSITE_COMPONENT_SOURCE"));//NOI18N
                sb.append("</b>");//NOI18N
                sb.append("&nbsp;");//NOI18N
                sb.append(relativePath);
                sb.append("</p>");//NOI18N
                
                sb.append("<p>");//NOI18N
                sb.append(getAttributesDescription(model, false));
                sb.append("</p>");//NOI18N
                sb.append("<p style=\"color: red\">").append(msgNoTld).append("</p>"); //NOI18N

                description = sb.toString();
            }

            private String getAttributesDescription(CompositeComponentModel model, boolean includeNoDescriptorMsg) {
                if (model.getExistingInterfaceAttributes().isEmpty()) {
                    return NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_NO_TAG_ATTRS");//NOI18N
                }

                StringBuilder sb = new StringBuilder();
                sb.append("<b>");//NOI18N
                sb.append(NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_TAG_ATTRS"));//NOI18N
                sb.append("</b>");//NOI18N
                sb.append("<table border=\"1\">"); //NOI18N

                for (Map<String, String> descr : model.getExistingInterfaceAttributes()) {
                    //first generate entry for the attribute name
                    sb.append("<tr>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                    String attrname = descr.get("name"); //NOI18N);
                    sb.append(attrname);
                    sb.append("</div>"); //NOI18N
                    sb.append("</td>"); //NOI18N

                    //then for the rest of the attributes, except the "name" atttribute
                    if (descr.size() > 1) {
                        sb.append("<td>"); //NOI18N
                        sb.append("<table border=\"0\" padding=\"0\" margin=\"0\" spacing=\"2\">"); //NOI18N
                        for (Map.Entry<String, String> entry : descr.entrySet()) {
                            String key = entry.getKey();
                            if ("name".equals(key)) {//NOI18N
                                continue; //skip name
                            }
                            String val = entry.getValue();
                            sb.append("<tr><td><b>");//NOI18N
                            sb.append(key);
                            sb.append("</b></td><td>");//NOI18N
                            sb.append(val);
                            sb.append("</td></tr>");//NOI18N
                        }
                        sb.append("</table>"); //NOI18N


                        sb.append("</td>"); //NOI18N
                    }
                    sb.append("</tr>"); //NOI18N
                }
                sb.append("</table>"); //NOI18N

                if (includeNoDescriptorMsg) {
                    String msgNoDescriptor = NbBundle.getMessage(CompositeComponentLibrary.class, "MSG_NO_DESCRIPTOR"); //NOI18N
                    sb.append("<p style=\"color: red\">").append(msgNoDescriptor).append("</p>");
                } //NOI18N

                return sb.toString();
            }

            @Override
            public String getName() {
                return cc.getName();
            }

            @Override
            public String getDescription() {
                load();
                return description;
            }

            @Override
            public boolean hasNonGenenericAttributes() {
                load();
                return !attrs.isEmpty();
            }

            @Override
            public Collection<Attribute> getAttributes() {
                load();
                //merge with default attributes
                Collection<Attribute> all = new ArrayList<>(super.getAttributes());
                all.addAll(attrs.values());
                return all;
            }

            @Override
            public Attribute getAttribute(String name) {
                load();
                Attribute superA = super.getAttribute(name);
                return superA != null ? superA : attrs.get(name);
            }
        }
    }
}
