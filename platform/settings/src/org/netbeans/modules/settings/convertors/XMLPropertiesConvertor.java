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

package org.netbeans.modules.settings.convertors;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import org.openide.filesystems.FileObject;

import org.netbeans.spi.settings.Convertor;
import org.netbeans.spi.settings.Saver;

import org.netbeans.modules.settings.Env;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;

/** Implementation of xml properties format described by
 * /org/netbeans/modules/settings/resources/properties.dtd
 *
 * @author  Jan Pokorsky
 */
public final class XMLPropertiesConvertor extends Convertor implements PropertyChangeListener {
    /** file attribute containnig value whether the setting object will be
     * stored automaticaly (preventStoring==false) or SaveCookie will be provided.
     * Default value is <code>preventStoring==false</code>. Usage
     * <code>&lt;attr name="xmlproperties.preventStoring" boolvalue="[true|false]"/>
     * </code>
     */
    public static final String EA_PREVENT_STORING = "xmlproperties.preventStoring"; //NOI18N
    /** file attribute containnig list of property names their changes will be ignored. Usage
     * <code>&lt;attr name="xmlproperties.ignoreChanges" stringvalue="name[, ...]"/>
     * </code>
     */
    public static final String EA_IGNORE_CHANGES = "xmlproperties.ignoreChanges"; //NOI18N
    private FileObject providerFO;
    /** cached property names to be filtered */
    private java.util.Set ignoreProperites;
    
    /** create convertor instance; should be used in module layers
     * @param providerFO provider file object
     */
    public static Convertor create(org.openide.filesystems.FileObject providerFO) {
        return new XMLPropertiesConvertor(providerFO);
    }
    
    public XMLPropertiesConvertor(org.openide.filesystems.FileObject fo) {
        this.providerFO = fo;
    }
    
    public Object read(java.io.Reader r) throws IOException, ClassNotFoundException {
        Object def = defaultInstanceCreate();
        return readSetting(r, def);
    }
    
    public void write(java.io.Writer w, Object inst) throws IOException {
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+XMLSettingsSupport.LINE_SEPARATOR); // NOI18N
        w.write("<!DOCTYPE properties PUBLIC \""); // NOI18N
        
        FileObject foEntity = Env.findEntityRegistration(providerFO);
        if (foEntity == null) foEntity = providerFO;
        Object publicId = foEntity.getAttribute(Env.EA_PUBLICID);
        if (!(publicId instanceof String)) {
            throw new IOException("missing or invalid attribute: " + //NOI18N
                Env.EA_PUBLICID + ", provider: " + foEntity); //NOI18N
        }
        
        w.write((String) publicId);
        w.write("\" \"http://www.netbeans.org/dtds/properties-1_0.dtd\">"+XMLSettingsSupport.LINE_SEPARATOR); // NOI18N
        w.write("<properties>"+XMLSettingsSupport.LINE_SEPARATOR); // NOI18N
        Properties p = getProperties(inst);
        if (p != null && !p.isEmpty()) writeProperties(w, p);
        w.write("</properties>"+XMLSettingsSupport.LINE_SEPARATOR); // NOI18N
    }
    
    /** an object listening on the setting changes */
    private Saver saver;
    public void registerSaver(Object inst, Saver s) {
        if (saver != null) {
            XMLSettingsSupport.err.log(Level.WARNING, "Already registered Saver: {0} for settings object: {1}", new Object[]{s.getClass().getCanonicalName(), inst.getClass().getCanonicalName()});
            return;
        }
        
        // add propertyChangeListener
        try {
            java.lang.reflect.Method method = inst.getClass().getMethod(
                "addPropertyChangeListener", // NOI18N
                new Class[] {PropertyChangeListener.class});
            method.invoke(inst, new Object[] {this});
            this.saver = s;
//System.out.println("XMLPropertiesConvertor.registerPropertyListener...ok " + inst);
        } catch (NoSuchMethodException ex) {
            XMLSettingsSupport.err.warning(
            "ObjectChangesNotifier: NoSuchMethodException: " + // NOI18N
            inst.getClass().getName() + ".addPropertyChangeListener"); // NOI18N
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void unregisterSaver(Object inst, Saver s) {
        if (saver == null) return;
        if (saver != s) {
            XMLSettingsSupport.err.log(Level.WARNING, "Unregistering unknown Saver: {0} for settings object: {1}", new Object[]{s.getClass().getCanonicalName(), inst.getClass().getCanonicalName()});
            return;
        }
        try {
            java.lang.reflect.Method method = inst.getClass().getMethod(
                "removePropertyChangeListener", // NOI18N
                new Class[] {PropertyChangeListener.class});
            method.invoke(inst, new Object[] {this});
            this.saver = null;
//System.out.println("XMLPropertiesConvertor.unregisterPropertyListener...ok " + inst);
        } catch (NoSuchMethodException ex) {
            XMLSettingsSupport.err.fine(
            "ObjectChangesNotifier: NoSuchMethodException: " + // NOI18N
            inst.getClass().getName() + ".removePropertyChangeListener"); // NOI18N
            // just changes done through gui will be saved
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
            // just changes done through gui will be saved
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
            // just changes done through gui will be saved
        }
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (saver == null || ignoreChange(evt)) return;
        if (acceptSave()) {
            try {
                saver.requestSave();
            } catch (IOException ex) {
                Logger.getLogger(XMLPropertiesConvertor.class.getName()).log(Level.WARNING, null, ex);
            }
        } else {
            saver.markDirty();
        }
    }
    
    
    // Private implementation
    /** filtering of Property Change Events */
    private boolean ignoreChange(java.beans.PropertyChangeEvent pce) {
        if (pce == null || pce.getPropertyName() == null) return true;
        
        if (ignoreProperites == null) {
            ignoreProperites = Env.parseAttribute(
                providerFO.getAttribute(EA_IGNORE_CHANGES));
        }
        if (ignoreProperites.contains(pce.getPropertyName())) return true;
        
        return ignoreProperites.contains("all"); // NOI18N
    }
    
    private boolean acceptSave() {
        Object storing = providerFO.getAttribute(EA_PREVENT_STORING);
        if (storing == null) return true;
        if (storing instanceof Boolean)
            return !((Boolean) storing).booleanValue();
        if (storing instanceof String)
            return !Boolean.valueOf((String) storing).booleanValue();
        return true;
    }
    
    private static final String INDENT = "    "; // NOI18N
    private String instanceClass = null;
    

    private Object defaultInstanceCreate() throws IOException, ClassNotFoundException {
        Object instanceCreate = providerFO.getAttribute(Env.EA_INSTANCE_CREATE);
        if (instanceCreate != null) return instanceCreate;
        
        Class c = getInstanceClass();
        try {
            return XMLSettingsSupport.newInstance(c);
        } catch (Exception ex) { // IllegalAccessException, InstantiationException
            IOException ioe = new IOException("Cannot create instance of " + c.getName()); // NOI18N
            ioe.initCause(ex);
            throw ioe;
        }
    }

    private Class getInstanceClass() throws IOException, ClassNotFoundException {
        if (instanceClass == null) {
            Object name = providerFO.getAttribute(Env.EA_INSTANCE_CLASS_NAME);
            if (!(name instanceof String)) {
                throw new IllegalStateException(
                    "missing or invalid ea attribute: " +
                    Env.EA_INSTANCE_CLASS_NAME); //NOI18N
            }
            instanceClass = (String) name;
        }
        return ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass(instanceClass);
    }
    
    private Object readSetting(java.io.Reader input, Object inst) throws IOException {
        try {
            java.lang.reflect.Method m = inst.getClass().getDeclaredMethod(
                "readProperties", new Class[] {Properties.class}); // NOI18N
            m.setAccessible(true);
            XMLPropertiesConvertor.Reader r = new XMLPropertiesConvertor.Reader();
            r.parse(input);
            m.setAccessible(true);
            Object ret = m.invoke(inst, new Object[] {r.getProperties()});
            if (ret == null) {
                ret = inst;
            }
            return ret;
        } catch (NoSuchMethodException ex) {
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } catch (IllegalAccessException ex) {
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(t);
            throw ioe;
        }
    }
    
    private static void writeProperties(java.io.Writer w, Properties p) throws IOException {
        java.util.Iterator it = p.keySet().iterator();
        String key;
        while (it.hasNext()) {
            key = (String) it.next();
            w.write(INDENT);
            w.write("<property name=\""); // NOI18N
            w.write(key);
            w.write("\" value=\""); // NOI18N
            w.write(XMLUtil.toAttributeValue( p.getProperty(key) ));
            w.write("\"/>"+XMLSettingsSupport.LINE_SEPARATOR); // NOI18N
        }
    }

    private static Properties getProperties (Object inst) throws IOException {
        try {
            java.lang.reflect.Method m = inst.getClass().getDeclaredMethod(
                "writeProperties", new Class[] {Properties.class}); // NOI18N
            m.setAccessible(true);
            Properties prop = new Properties();
            m.invoke(inst, new Object[] {prop});
            return prop;
        } catch (NoSuchMethodException ex) {
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } catch (IllegalAccessException ex) {
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } catch (java.lang.reflect.InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(t);
            throw ioe;
        }
    }
    
    /** support for reading xml/properties format */
    private static class Reader extends org.xml.sax.helpers.DefaultHandler implements org.xml.sax.ext.LexicalHandler {
        Reader() {}

        private static final String ELM_PROPERTY = "property"; // NOI18N
        private static final String ATR_PROPERTY_NAME = "name"; // NOI18N
        private static final String ATR_PROPERTY_VALUE = "value"; // NOI18N

        private Properties props = new Properties();
        private String publicId;

        @Override
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
            if (this.publicId != null && this.publicId.equals (publicId)) {
                return new org.xml.sax.InputSource (new java.io.ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attribs) throws SAXException {
            if (ELM_PROPERTY.equals(qName)) {
                String propertyName = attribs.getValue(ATR_PROPERTY_NAME);
                String propertyValue = attribs.getValue(ATR_PROPERTY_VALUE);
                props.setProperty(propertyName, propertyValue);
            }
        }

        public void parse(java.io.Reader src) throws IOException {
            try {
                org.xml.sax.XMLReader reader = org.openide.xml.XMLUtil.createXMLReader(false, false);
                reader.setContentHandler(this);
                reader.setEntityResolver(this);
                org.xml.sax.InputSource is =
                    new org.xml.sax.InputSource(src);
                try {
                    reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);  //NOI18N
                } catch (SAXException sex) {
                    XMLSettingsSupport.err.warning(
                    "Warning: XML parser does not support lexical-handler feature.");  //NOI18N
                }
                reader.parse(is);
            } catch (SAXException ex) {
                IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            }
        }
        
        public Properties getProperties() {
            return props;
        }
        
        public String getPublicID() {
            return publicId;
        }

        // LexicalHandler implementation
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            this.publicId = publicId;
        }
        
        public void endDTD() throws SAXException {}
        public void startEntity(String str) throws SAXException {}
        public void endEntity(String str) throws SAXException {}
        public void comment(char[] values, int param, int param2) throws SAXException {}
        public void startCDATA() throws SAXException {}
        public void endCDATA() throws SAXException {}
    }
}
