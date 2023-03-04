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

package org.netbeans.modules.maven;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.spi.PackagingProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.LookupMerger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * implementation of AuxiliaryProperties.
 * @author mkleint
 */
public class MavenProjectPropsImpl {

    private static final String NAMESPACE = "http://www.netbeans.org/ns/maven-properties-data/1"; //NOI18N
    private static final String ROOT = "properties"; //NOI18N

    private static final Logger LOG = Logger.getLogger(MavenProjectPropsImpl.class.getName());

    private final AuxiliaryConfiguration aux;
    private boolean sharedChanged;
    private final NbMavenProjectImpl nbprji;
    private Mutex mutex;

    MavenProjectPropsImpl(AuxiliaryConfiguration aux, NbMavenProjectImpl pr) {
        this.aux = aux;
        nbprji = pr;
    }

    private AuxiliaryConfiguration getAuxConf() {
        return aux;
    }

    public String get(String key, boolean shared) {
        return get(key, shared, true);
    }

    public String get(String key, boolean shared, boolean usePom) {
        return getMutex().readAccess((Mutex.Action<String>) () -> {
            TreeMap<String, String> props = readProperties(getAuxConf(), shared);
            //TODO optimize
            String ret =  props.get(key);
            if (ret != null) {
                return ret;
            }
            if (shared && usePom) {
                if(Constants.HINT_PACKAGING.equals(key) && !nbprji.isMavenProjectLoaded()) {
                    // issue #262646 
                    // due to unfortunate ProjectManager.findPorjetc calls in awt, 
                    // speed is essential during project init, so lets try to avoid
                    // maven project loading if we can get the info faster from raw model.
                    Model model;  
                    try {
                        model = nbprji.getProjectWatcher().getRawModel();
                    } catch (ModelBuildingException ex) {
                        // whatever happend, we can't use the model, 
                        // lets try to follow up with loading the maven project
                        model = null;
                        LOG.log(Level.FINE, null, ex);
                    }
                    String val = model != null ? model.getPackaging() : null;
                    // In the unfortunate case that the project packaging contains value reference, it's probably
                    // better not to provide anything: packaging affects Project Lookup and things may get screwed up.
                    if (val != null && !val.contains("${")) {
                        return val;
                    }   
                }
                String val = nbprji.getOriginalMavenProject().getProperties().getProperty(key);
                if (val != null) {
                    return val;
                }
            }
            return null;
        });                       
    }

    public void put(String key, String value, boolean shared) {
        getMutex().writeAccess((Mutex.Action<Void>) () -> {
            if (shared) {
                //TODO put props to project.. shall we actually do it here?
            }
            writeAuxiliaryData(getAuxConf(), key, value, shared);
            return null;
        });
    }

    public Iterable<String> listKeys(boolean shared) {
        return getMutex().readAccess((Mutex.Action<Iterable<String>>) () -> {
            TreeMap<String, String> props = readProperties(getAuxConf(), shared);
            if (shared) {
                Properties mvnprops =  nbprji.getOriginalMavenProject().getProperties();
                for (Object prop: mvnprops.keySet()) {
                    props.put((String)prop, "any"); //NOI18N
                }
            }
            return props.keySet();
        });    
    }

    private void writeAuxiliaryData(AuxiliaryConfiguration conf, String property, String value, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        NodeList list = el.getElementsByTagNameNS(NAMESPACE, property);
        if (list.getLength() > 0) {
            enEl = (Element)list.item(0);
        } else {
            try {
                enEl = el.getOwnerDocument().createElementNS(NAMESPACE, property);
            } catch (DOMException x) {
                LOG.log(Level.WARNING, "#200901: {0} from {1}", new Object[] {x.getMessage(), property});
                return;
            }
            el.appendChild(enEl);
        }
        if (value != null) {
            enEl.setTextContent(value);
        } else {
            el.removeChild(enEl);
        }
        if (el.getElementsByTagNameNS(NAMESPACE, "*").getLength() > 0) {
            conf.putConfigurationFragment(el, shared);
        } else {
            conf.removeConfigurationFragment(ROOT, NAMESPACE, shared);
        }
    }

    public static void writeAuxiliaryData(AuxiliaryConfiguration conf, TreeMap<String, String> props, boolean shared) {
        Element el = getOrCreateRootElement(conf, shared);
        Element enEl;
        for (String key : props.keySet()) {
            NodeList list = el.getElementsByTagNameNS(NAMESPACE, key);
            if (list.getLength() > 0) {
                enEl = (Element)list.item(0);
            } else {
                try {
                    enEl = el.getOwnerDocument().createElementNS(NAMESPACE, key);
                } catch (DOMException x) {
                    LOG.log(Level.WARNING, "#200901: {0} from {1}", new Object[] {x.getMessage(), key});
                    continue;
                }
                el.appendChild(enEl);
            }
            String value = props.get(key);
            if (value != null) {
                enEl.setTextContent(value);
            } else {
                el.removeChild(enEl);
            }
        }
        if (el.getElementsByTagNameNS(NAMESPACE, "*").getLength() > 0) {
            conf.putConfigurationFragment(el, shared);
        } else {
            conf.removeConfigurationFragment(ROOT, NAMESPACE, shared);
        }
    }

    private static Element getOrCreateRootElement(AuxiliaryConfiguration conf, boolean shared) {
        Element el = conf.getConfigurationFragment(ROOT, NAMESPACE, shared);
        if (el == null) {
            el = XMLUtil.createDocument(ROOT, NAMESPACE, null, null).getDocumentElement();
            if (shared) {
                Comment comment = el.getOwnerDocument().createComment("\nProperties that influence various parts of the IDE, especially code formatting and the like. \n" + //NOI18N
                        "You can copy and paste the single properties, into the pom.xml file and the IDE will pick them up.\n" + //NOI18N
                        "That way multiple projects can share the same settings (useful for formatting rules for example).\n" + //NOI18N
                        "Any value defined here will override the pom.xml file value but is only applicable to the current project.\n"); //NOI18N
                el.appendChild(comment);
            }
        }
        return el;
    }


    private TreeMap<String, String> readProperties(AuxiliaryConfiguration aux, boolean shared) {
        return getMutex().readAccess((Mutex.Action<TreeMap<String, String>>) () -> {
            TreeMap<String, String> props = new TreeMap<>();
            Element el = aux.getConfigurationFragment(ROOT, NAMESPACE, shared);
            if (el != null) {
                NodeList list = el.getChildNodes();
                if (list.getLength() > 0) {
                    for (int i = 0; i < list.getLength(); i++) {
                        Node nd = list.item(i);
                        if (nd instanceof Element) {
                            Element enEl = (Element)nd;
                            props.put(enEl.getNodeName(), enEl.getTextContent());
                        }
                    }
                }
            }
            return props;
        });    
    }

    public TreeMap<String, String> getRawProperties(boolean shared) {
        return readProperties(getAuxConf(), shared);
    }

    private synchronized Mutex getMutex() {
        if(mutex == null) {
            mutex = new Mutex();
        }
        return mutex;
    }

    static class Merger implements LookupMerger<AuxiliaryProperties> {
        private final MavenProjectPropsImpl primary;

        Merger(MavenProjectPropsImpl primary) {
            this.primary = primary;
        }

        public @Override Class<AuxiliaryProperties> getMergeableClass() {
            return AuxiliaryProperties.class;
        }

        public @Override AuxiliaryProperties merge(Lookup lookup) {
            return new MergedAuxProperties(lookup, primary);
        }

    }

    private static class MergedAuxProperties implements AuxiliaryProperties {
        Lookup.Result<AuxiliaryProperties> props;
        private final MavenProjectPropsImpl primary;
        private MergedAuxProperties(Lookup lookup, MavenProjectPropsImpl primary) {
             props = lookup.lookupResult(AuxiliaryProperties.class);
             this.primary = primary;
        }


        public @Override String get(String key, boolean shared) {
            String toRet = primary.get(key, shared);
            if (toRet == null) {
                for (AuxiliaryProperties prop : props.allInstances()) {
                    toRet = prop.get(key, shared);
                    if (toRet != null) {
                        break;
                    }
                }
            }
            return toRet;
        }

        public @Override void put(String key, String value, boolean shared) {
            primary.put(key, value, shared);
        }

        public @Override Iterable<String> listKeys(boolean shared) {
            Set<String> toRet = new TreeSet<String>();
            Iterator<String> s = primary.listKeys(shared).iterator();
            while (s.hasNext()) {
                toRet.add(s.next());
            }
            for (AuxiliaryProperties aux : props.allInstances()) {
                s = aux.listKeys(shared).iterator();
                while (s.hasNext()) {
                    toRet.add(s.next());
                }
            }
            return toRet;
        }

    }

    @ServiceProvider(service=PackagingProvider.class, position=1000)
    public static class PackagingProviderImpl implements PackagingProvider {
        @Override public String packaging(Project project) {
            return project.getLookup().lookup(MavenProjectPropsImpl.class).get(Constants.HINT_PACKAGING, true);
        }
    }

}
