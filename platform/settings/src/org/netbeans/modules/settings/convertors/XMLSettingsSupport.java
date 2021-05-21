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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.settings.FactoryMethod;

import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** Class provides support for storing and reading session settings.
 *
 * @see SerialDataConvertor
 * @author  Jan Pokorsky
 */
final class XMLSettingsSupport {
    
    public static final String INSTANCE_DTD_ID = "-//NetBeans//DTD Session settings 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_WWW = "http://www.netbeans.org/dtds/sessionsettings-1_0.dtd"; // NOI18N
    /** File extension for xml settings. */
    public static final String XML_EXT = "settings"; //NOI18N
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /** Logging for events in XML settings system. */
    static final Logger err = Logger.getLogger(XMLSettingsSupport.class.getName()); // NOI18N

    static Object newInstance(Class<?> clazz) throws IllegalArgumentException, 
    InstantiationException, NoSuchMethodException, InvocationTargetException, 
    IllegalAccessException {
        FactoryMethod fm = clazz.getAnnotation(FactoryMethod.class);
        if (fm != null) {
            Method method;
            try {
                method = clazz.getMethod(fm.value());
            } catch (NoSuchMethodException ex) {
                method = clazz.getDeclaredMethod(fm.value());
            }
            method.setAccessible(true);
            return method.invoke(null);
        }
        Constructor<?> c = clazz.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    
    /** Store instanceof elements.
     * @param classes everything what class extends or implements
     * @param pw output
     */
    private static void storeInstanceOf (Set<Class> classes, PrintWriter pw) throws IOException {
        SortedSet<String> clazzNames = new TreeSet<String>(); // sort them: #24661
        for (Class clz: classes) {
            clazzNames.add(clz.getName());
        }
        StringBuilder sb = new StringBuilder (200);  // XXX estimate right capacity
        for (String s: clazzNames) {
            sb.append("    <instanceof class=\""). // NOI18N
            append(s).
            append("\"/>").append(LINE_SEPARATOR); // NOI18N
        }
        pw.print(sb.toString());
    }
    
    /** Store settings version 1.0
     * @param inst settings instance
     * @param os output
     */
    public static void storeToXML10 (Object inst, Writer os, ModuleInfo mi)
    throws IOException {
        PrintWriter pw = new PrintWriter (os);
        
        pw.println ("<?xml version=\"1.0\"?>"); // NOI18N
        pw.println ("<!DOCTYPE settings PUBLIC \""+INSTANCE_DTD_ID+ // NOI18N
            "\" \""+INSTANCE_DTD_WWW+"\">"); // NOI18N
        pw.println ("<settings version=\"1.0\">"); // NOI18N
        storeModule(mi, pw);
        storeInstanceOf(getSuperClasses(inst.getClass(), null), pw);
        // default storage has been implemented by serialization
        storeSerialData(inst, pw);
        
        pw.println ("</settings>"); // NOI18N
        pw.flush ();
    }
    
    /** Store a default instance. Ensure compatibility for settings declared in
     * a manifest.
     * @param clazz class of instance
     * @param os output
     */
    private static void storeToXML10 (Class clazz, Writer os, ModuleInfo mi)
    throws IOException {
        
        PrintWriter pw = new PrintWriter (os);
        pw.println ("<?xml version=\"1.0\"?>"); // NOI18N
        pw.println ("<!DOCTYPE settings PUBLIC \""+INSTANCE_DTD_ID+ // NOI18N
            "\" \""+INSTANCE_DTD_WWW+"\">"); // NOI18N
        pw.println ("<settings version=\"1.0\">"); // NOI18N
        storeModule(mi, pw);
        storeInstanceOf(getSuperClasses(clazz, null), pw);
        pw.println ("    <instance class=\""+clazz.getName()+"\"/>"); // NOI18N
        pw.println ("</settings>"); // NOI18N
        pw.flush ();
    }
    
    private static void storeModule(ModuleInfo mi, PrintWriter pw)
    throws IOException {
        if (mi == null) return;
        
        String modulName = mi.getCodeName();
        SpecificationVersion spec = mi.getSpecificationVersion();
        StringBuffer sb = new StringBuffer (80);
        sb.append("    <module"); // NOI18N
        if (modulName != null && modulName.length() != 0) {
            sb.append(" name=\"").append(modulName).append('"');// NOI18N
        }
        if (spec != null) {
            sb.append(" spec=\"").append(spec.toString()).append('"');// NOI18N
        }
        sb.append("/>"); // NOI18N
        pw.println(sb.toString());
    }
    
    
    /** This object output stream subclass is used for storing InstanceDataObject.
     * More details in bug #15563
     */
    private static class SpecialObjectOutputStream extends org.openide.util.io.NbObjectOutputStream {
        /** Is the stream expecting the first object in stream? */
        private boolean first;
        
        public SpecialObjectOutputStream(OutputStream os) throws IOException {
            super (os);
            first = true;
        }

        /** Check if the first object in the stream is <CODE>null</CODE>.
         * If so, throw InvalidObjectException.
         */
        public Object replaceObject (Object obj) throws IOException {
            if (first) {
                if (obj == null)
                    // Object doesn't want to be serialized.
                    throw new NotSerializableException();
                first = false;
            }
            return super.replaceObject(obj);
        }
        

    }
    
    /** Stream allowing upgrade to a new class inside the origin .settings file
     */
    private static class SpecialObjectInputStream extends java.io.ObjectInputStream {
        
        public SpecialObjectInputStream(InputStream is) throws IOException {
            super(is);
            try {
                enableResolveObject (true);
            } catch (SecurityException ex) {
                throw new IOException (ex.toString ());
            }
        }

        /* Uses NetBeans module classloader to load the class.
         * @param v description of the class to load
         */
        protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
            ClassLoader cl = getNBClassLoader();
            try {
                return Class.forName(v.getName(), false, cl);
            } catch (ClassNotFoundException cnfe) {
                String msg = "Offending classloader: " + cl; // NOI18N
                Exceptions.attachMessage(cnfe, msg);
                throw cnfe;
            }
        }
        
        /** use Utilities.translate to try to upgrade to new setting's class.
         * If the old class exists the origin descriptor is used and upgrade is
         * postponed to readResolve;
         * otherwise the same implementation like NbObjectInputStream.readClassDescriptor
         */
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass ose = super.readClassDescriptor();

            String name = ose.getName();
            String newN = org.openide.util.Utilities.translate(name);

            if (name == newN) {
                // no translation
                return ose;
            }

            ClassLoader cl = getNBClassLoader();
            try {
                Class origCl = Class.forName(name, false, cl);
                // translation postponed to readResolve
                return ObjectStreamClass.lookup(origCl);
            } catch (ClassNotFoundException ex) {
                // ok look up new descriptor
            }
            
            Class<?> clazz = Class.forName(newN, false, cl);
            ObjectStreamClass newOse = ObjectStreamClass.lookup(clazz);

            // #28021 - it is possible that lookup return null. In that case the conversion
            // table contains class which is not Serializable or Externalizable.
            if (newOse == null) {
                throw new java.io.NotSerializableException(newN);
            }
            
            return newOse;
        }
    
        /** Lazy create default NB classloader for use during deserialization. */
        private static ClassLoader getNBClassLoader() {
            ClassLoader c = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
            return c != null ? c : ClassLoader.getSystemClassLoader();
        }
        
    }
    
    
    // enlarged to not need do the test for negative byte values
    private final static char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
                                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                                             '0', '1', '2', '3', '4', '5', '6', '7'};
    
    private static final int INDENT = 8;
    private static final int BLOCK = 100;
    private static final int BUFFSIZE = INDENT + BLOCK;
    private static void storeSerialData (Object inst, PrintWriter pw) throws IOException {
        pw.println ("    <serialdata class=\""+inst.getClass().getName()+"\">"); // NOI18N

        ByteArrayOutputStream baos = new ByteArrayOutputStream (1024);
        ObjectOutput oo = new SpecialObjectOutputStream (baos);
        oo.writeObject (inst);
        byte[] bdata = baos.toByteArray ();
        
        char[] cdata = new char[BUFFSIZE];
        for (int i=0; i < INDENT; i++ ) cdata[i] = ' ';
        
        int i = 0; // byte array pointer
        int j; // char array pointer
        int blen = bdata.length;
        
        while (i < blen) {
            int mark = INDENT + Math.min( 2*(blen-i), BLOCK );
            for (j=INDENT; j < mark; j += 2) {
                int b = ((int)bdata[i++]) + 256;
                cdata[j]   = HEXDIGITS[b >> 4];
                cdata[j+1] = HEXDIGITS[b & 15];
            }
            pw.write(cdata, 0, j);
            pw.println();
        }
        pw.println ("    </serialdata>"); // NOI18N
        pw.flush();
    }
    
    /** Get everything what class extends or implements. */
    private static Set<Class> getSuperClasses(Class clazz, Set<Class> classes) {
        if (classes == null) {
            classes = new HashSet<Class>();
        }
        
        if (clazz == null || !classes.add(clazz)) {
            return classes;
        }
        
        Class[] cs = clazz.getInterfaces();
        
        // XXX following validation should help to identify a wrong IBM's
        // implementation of Class.getInterfaces(). The implementation for some
        // classes returns null. See issue #16257.
        if (cs != null) {
            for (int i = 0; i < cs.length; i++) {
                getSuperClasses(cs[i], classes);
            }
        } else {
            err.severe(
                "Error: if you encounter this message, please attach " + //NOI18N
                "the class name to the issue http://www.netbeans.org/issues/show_bug.cgi?id=16257. " + //NOI18N
                "Class.getInterfaces() == null for the class: " + clazz); // NOI18N
        }
        
        return getSuperClasses(clazz.getSuperclass(), classes);
    }
    
    /** Class must be subclass of org.openide.ServiceType. */
    private static Class getServiceTypeClass(Class type) {
        if (!org.openide.ServiceType.class.isAssignableFrom(type))
            throw new IllegalArgumentException();
        // finds direct subclass of service type
        while (type.getSuperclass () != org.openide.ServiceType.class) {
            type = type.getSuperclass();
        }
        return type;
    }
    
    /** Settings parser. */
    final static class SettingsRecognizer extends org.xml.sax.helpers.DefaultHandler {
        
        private static final String ELM_SETTING = "settings"; // NOI18N
        private static final String ATR_SETTING_VERSION = "version"; // NOI18N
        
        private static final String ELM_MODULE = "module"; // NOI18N
        private static final String ATR_MODULE_NAME = "name"; // NOI18N
        private static final String ATR_MODULE_SPEC = "spec"; // NOI18N
        private static final String ATR_MODULE_IMPL = "impl"; // NOI18N
        
        private static final String ELM_INSTANCE = "instance"; // NOI18N
        private static final String ATR_INSTANCE_CLASS = "class"; // NOI18N
        private static final String ATR_INSTANCE_METHOD = "method"; // NOI18N
        
        private static final String ELM_INSTANCEOF = "instanceof"; // NOI18N
        private static final String ATR_INSTANCEOF_CLASS = "class"; // NOI18N
        
        private static final String ELM_SERIALDATA = "serialdata"; // NOI18N
        private static final String ATR_SERIALDATA_CLASS = "class"; // NOI18N
        
        //private static final String VERSION = "1.0"; // NOI18N
        
        private boolean header;
        private Stack<String> stack;
        
        private String version;
        private String instanceClass;
        private String instanceMethod;
        private Set<String> instanceOf = new HashSet<String>();
        
        private byte[] serialdata;
        private CharArrayWriter chaos = null;
        
        private String codeName;
        private String codeNameBase;
        private int codeNameRelease;
        private SpecificationVersion moduleSpec;
        private String moduleImpl;
        /** file with stored settings */
        private final FileObject source;
        
        /** XML handler recognizing settings.
         * @param header if true read just elements instanceof, module and attr classname.
         * @param source file with stored settings
         */
        public SettingsRecognizer (boolean header, FileObject source) {
            this.header = header;
            this.source = source;
        }
        
        public boolean isAllRead() {
            return !header;
        }
        
        public void setAllRead(boolean all) {
            if (!header) return;
            header = all;
        }
        
        public String getSettingsVerison() {
            return version;
        }
        
        public String getCodeName() {
            return codeName;
        }
        
        public String getCodeNameBase() {
            return codeNameBase;
        }
        
        public int getCodeNameRelease() {
            return codeNameRelease;
        }
        
        public SpecificationVersion getSpecificationVersion() {
            return moduleSpec;
        }
        
        public String getModuleImpl() {
            return moduleImpl;
        }
        
        /** Set of names. */
        public Set<String> getInstanceOf() {
            return instanceOf;
        }
        
        /** Method attribute from the instance element. */
        public String getMethodName() {
            return instanceMethod;
        }
        
        /** Serialized instance, can be null. */
        private  InputStream getSerializedInstance() {
            if (serialdata == null) return null;
            return new ByteArrayInputStream(serialdata);
        }
        
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID.equals (publicId) || "-//NetBeans//DTD XML beans 1.0//EN".equals(publicId)) {
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }
        
        public void characters(char[] values, int start, int length) throws SAXException {
            if (header) return;
            String element = stack.peek();
            if (ELM_SERIALDATA.equals(element)) {
                // [PENDING] should be optimized to do not read all chars to memory
                if (chaos == null) chaos = new CharArrayWriter(length);
                chaos.write(values, start, length);
            }
        }
        
        public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException {
            stack.push(qName);
            if (ELM_SETTING.equals(qName)) {
                version = attribs.getValue(ATR_SETTING_VERSION);
            } else if (ELM_MODULE.equals(qName)) {
                codeName = attribs.getValue(ATR_MODULE_NAME);
                resolveModuleElm(codeName);
                moduleImpl = attribs.getValue(ATR_MODULE_IMPL);
                try {
                    String spec = attribs.getValue(ATR_MODULE_SPEC);
                    moduleSpec = spec == null ? null : new SpecificationVersion(spec);
                } catch (NumberFormatException nfe) {
                    throw new SAXException(nfe);
                }
            } else if (ELM_INSTANCEOF.equals(qName)) {
                String instanceOfName = org.openide.util.Utilities.translate(attribs.getValue(ATR_INSTANCEOF_CLASS));
                if (instanceOfName.length() > 0) {
                    instanceOf.add(instanceOfName);
                }
            } else if (ELM_INSTANCE.equals(qName)) {
                instanceClass = attribs.getValue(ATR_INSTANCE_CLASS);
                if (instanceClass == null) {
                    System.err.println("Hint: NPE is caused by broken settings file: " + source ); // NOI18N
                }
                instanceClass = org.openide.util.Utilities.translate(instanceClass);
                instanceMethod = attribs.getValue(ATR_INSTANCE_METHOD);
            } else if (ELM_SERIALDATA.equals(qName)) {
                instanceClass = attribs.getValue(ATR_SERIALDATA_CLASS);
                instanceClass = org.openide.util.Utilities.translate(instanceClass);
                if (header) throw new StopSAXException();
            }
        }
        
        /** reade codenamebase + revision */
        private void resolveModuleElm (String codeName) {
            if (codeName != null) {
                int slash = codeName.indexOf ("/"); // NOI18N
                if (slash == -1) {
                    codeNameBase = codeName;
                    codeNameRelease = -1;
                } else {
                    codeNameBase = codeName.substring (0, slash);
                    try {
                        codeNameRelease = Integer.parseInt(codeName.substring(slash + 1));
                    } catch (NumberFormatException ex) {
                        Exceptions.attachLocalizedMessage(ex,
                                                          "Content: \n" +
                                                          getFileContent(source)); // NOI18N
                        Exceptions.attachLocalizedMessage(ex,
                                                          "Source: " + source); // NOI18N
                        Logger.getLogger(XMLSettingsSupport.class.getName()).log(Level.WARNING, null, ex);
                        codeNameRelease = -1;
                    }
                }
            } else {
                codeNameBase = null;
                codeNameRelease = -1;
            }
        }
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //if (header) return;
            String element = stack.pop();
            if (ELM_SERIALDATA.equals(element)) {
                if (chaos != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(chaos.size() >> 1);
                    try {
                        chars2Bytes(baos, chaos.toCharArray(), 0, chaos.size());
                        serialdata = baos.toByteArray();
                    } catch (IOException ex) {
                        Logger.getLogger(XMLSettingsSupport.class.getName()).log(Level.WARNING, null, ex);
                    } finally {
                        chaos = null; // don't keep the info twice
                        try {
                            baos.close();
                        } catch (IOException ex) {
                            // doesn't matter
                        }
                    }
                }
            }
       }
       
        /** Tries to deserialize instance saved in is.
         * @param is    stream with stored object, can be null
         * @return deserialized object or null
         */
        private Object readSerial(InputStream is) throws IOException, ClassNotFoundException {
            if (is == null) return null;
            try {
                ObjectInput oi = new SpecialObjectInputStream (is);
                try {
                    Object o = oi.readObject ();
                    return o;
                } finally {
                    oi.close();
                }
            } catch (IOException ex) {
                Exceptions.attachLocalizedMessage(ex,
                                                  "Content: \n" +
                                                  getFileContent(source)); // NOI18N
                Exceptions.attachLocalizedMessage(ex, "Source: " + source); // NOI18N
                Exceptions.attachLocalizedMessage(ex,
                                                  "Cannot read class: " +
                                                  instanceClass); // NOI18N
                throw ex;
            } catch (ClassNotFoundException ex) {
                Exceptions.attachLocalizedMessage(ex,
                                                  "Content: \n" +
                                                  getFileContent(source)); // NOI18N
                Exceptions.attachLocalizedMessage(ex, "Source: " + source); // NOI18N
                throw ex;
            }
        }
        
        /** Create an instance.
         * @return the instance of type {@link #instanceClass}
         * @exception IOException if an I/O error occured
         * @exception ClassNotFoundException if a class was not found
         */
        public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
            Object inst = null;
            
            // deserialize
            inst = readSerial(getSerializedInstance());

            // default instance
            if (inst == null) {
                if (instanceMethod != null) {
                    inst = createFromMethod(instanceClass, instanceMethod);
                } else {
                    // use default constructor
                    Class<?> clazz = instanceClass();
                    if (SharedClassObject.class.isAssignableFrom(clazz)) {
                        inst = SharedClassObject.findObject(clazz.asSubclass(SharedClassObject.class), false);
                        if (null != inst) {
                            // instance already exists -> reset it to defaults
                            try {
                                Method method = SharedClassObject.class.getDeclaredMethod("reset", new Class[0]); // NOI18N
                                method.setAccessible(true);
                                method.invoke(inst, new Object[0]);
                            } catch (Exception e) {
                                Exceptions.printStackTrace(e);
                            }
                        } else {
                            inst = SharedClassObject.findObject(clazz.asSubclass(SharedClassObject.class), true);
                        }
                    } else {
                        try {
                            inst = newInstance(clazz);
                        } catch (Exception ex) {
                            IOException ioe = new IOException();
                            ioe.initCause(ex);
                            Exceptions.attachMessage(ioe,
                                                              "Content: \n" +
                                                              getFileContent(source)); // NOI18N
                            Exceptions.attachMessage(ioe,
                                                              "Class: " + clazz); // NOI18N
                            Exceptions.attachMessage(ioe,
                                                              "Source: " +
                                                              source); // NOI18N
                            throw ioe;
                        }
                    }
                }
            }
            
            return inst;
        }
        
        /** Get file content as String. If some exception occures its stack trace 
          is return instead. */
        private static String getFileContent (FileObject fo) {
            try {
                InputStreamReader isr = new InputStreamReader(fo.getInputStream());
                char[] cbuf = new char[1024];
                int length;
                StringBuffer sbuf = new StringBuffer(1024);
                while (true) {
                    length = isr.read(cbuf);
                    if (length > 0) {
                        sbuf.append(cbuf, 0, length);
                    } else {
                        return sbuf.toString();
                    }
                }
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                return sw.toString();
            }
        }
        
        /** create instance by invoking class method */
        private Object createFromMethod(String srcClazz, String srcMethod)
        throws ClassNotFoundException, IOException {
            int dotIndex = instanceMethod.lastIndexOf('.');
            String targetClass;
            String targetMethod;
            if (dotIndex > 0) {
                targetClass = srcMethod.substring(0, dotIndex);
                targetMethod = srcMethod.substring(dotIndex + 1);
            } else {
                targetClass = srcClazz;
                targetMethod = srcMethod;
            }

            Class<?> clazz = loadClass(targetClass);

            try {
                Object instance;
                try {
                    Method method = clazz.getDeclaredMethod(targetMethod, new Class[]{FileObject.class});
                    method.setAccessible(true);
                    instance = method.invoke(null, source);
                } catch (NoSuchMethodException ex) {
                    Method method = clazz.getDeclaredMethod(targetMethod);
                    method.setAccessible(true);
                    instance = method.invoke(null, new Object[0]);
                }
                if (instance == null) {
                    // Strictly verboten. Cf. BT #4827173 for example.
                    throw new IOException("Null return not permitted from " + targetClass + "." + targetMethod); // NOI18N
                }
                return instance;
            } catch (Exception ex) {
                IOException ioe = new IOException("Error reading " + source +
                                                  ": " + ex);

                ioe.initCause(ex);
                Exceptions.attachMessage(ioe,
                                         "Content:\n" + getFileContent(source));
                Exceptions.attachMessage(ioe, "Method: " + srcMethod);
                Exceptions.attachMessage(ioe, "Class: " + clazz);
                throw ioe;
            }
        }
        
        /** The representation type that may be created as instances.
         * Can be used to test whether the instance is of an appropriate
         * class without actually creating it.
         *
         * @return the representation class of the instance
         * @exception IOException if an I/O error occurred
         * @exception ClassNotFoundException if a class was not found
         */
        public Class instanceClass() throws java.io.IOException, ClassNotFoundException {
            if (instanceClass == null) {
                throw new ClassNotFoundException(source +
                    ": missing 'class' attribute in 'instance' element"); //NOI18N
            }
            
            return loadClass(instanceClass);
        }
        
        /** try to load class from system and current classloader. */
        private Class<?> loadClass(String clazz) throws ClassNotFoundException {
            return Lookup.getDefault().lookup(ClassLoader.class).loadClass(clazz);
        }
        
        /** get class name of instance */
        public String instanceName() {
            if (instanceClass == null) {
                return ""; // NOI18N
            } else {
                return instanceClass;
            }
        }
        
        private int tr(char c) {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            return -1;
        }
        
        /** Converts array of chars to array of bytes. All whitespaces and
         * unknown chars are skipped.
         */
        private void chars2Bytes (OutputStream os, char[] chars, int off, int length)
        throws IOException {
            byte rbyte;
            int read;
            
            for (int i = off; i < length; ) {
                read = tr(chars[i++]);
                if (read >= 0) rbyte = (byte) (read << 4); // * 16;
                else continue;
                
                while (i < length) {
                    read = tr(chars[i++]);
                    if (read >= 0) {
                        rbyte += (byte) read;
                        os.write(rbyte);
                        break;
                    }
                }
            }
        }
         
        /** Parse settings file. */
        public void parse() throws IOException {
            InputStream in = null;
            
            try {
                if (header) {
                    if (err.isLoggable(Level.FINE) && source.getSize () < 12000) {
                        // log the content of the stream 
                        byte[] arr = new byte[(int)source.getSize ()];
                        InputStream temp = source.getInputStream ();
                        int len = temp.read (arr);
                        if (len != arr.length) {
                            throw new IOException ("Could not read " + arr.length + " bytes from " + source + " just " + len); // NOI18N
                        }
                        
                        err.finer("Parsing:" + new String (arr));
                        
                        temp.close ();
                        
                        in = new ByteArrayInputStream (arr);
                    } else {
                        in = source.getInputStream ();
                    }
                    Set<String> iofs = quickParse(new BufferedInputStream(in));
                    if (iofs != null) {
                        instanceOf = iofs;
                        return;
                    }
                }
            } catch (IOException ioe) {
                // ignore - fallback to XML parser follows
            } finally {
                if (in != null) in.close();
            }
            stack = new Stack<String>();
            try {
                in = source.getInputStream();
                XMLReader reader = org.openide.xml.XMLUtil.createXMLReader();
                reader.setContentHandler(this);
                reader.setErrorHandler(this);
                reader.setEntityResolver(this);
                reader.parse(new org.xml.sax.InputSource(new BufferedInputStream(in)));
            } catch (XMLSettingsSupport.StopSAXException ex) {
                // Ok, header is read
            } catch (SAXException ex) {
                IOException ioe = new IOException(source.toString()); // NOI18N
                ioe.initCause(ex);
                Exceptions.attachMessage(ioe,
                                                  "Content: \n" +
                                                  getFileContent(source)); // NOI18N
                Exceptions.attachMessage(ioe, "Source: " + source); // NOI18N
                throw ioe;
            } finally {
                stack = null;
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore already closed
                }
            }
        }
        
        /** Parse setting from source. */
        public void parse(Reader source) throws IOException {
            stack = new Stack<String>();
            
            try {
                XMLReader reader = org.openide.xml.XMLUtil.createXMLReader();
                reader.setContentHandler(this);
                reader.setErrorHandler(this);
                reader.setEntityResolver(this);
                reader.parse(new org.xml.sax.InputSource(source));
            } catch (XMLSettingsSupport.StopSAXException ex) {
                // Ok, header is read
            } catch (SAXException ex) {
                IOException ioe = new IOException(source.toString()); // NOI18N
                ioe.initCause(ex);
                throw ioe;
            } finally {
                stack = null;
            }
        }

        // Encoding irrelevant for these getBytes() calls: all are ASCII...
        // (unless someone has their system encoding set to UCS-16!)
        private static final byte[] MODULE_SETTINGS_INTRO = "<?xml version=\"1.0\"?> <!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\"> <settings version=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_INTRO_END = "> <".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_MODULE_NAME = "odule name=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_MODULE_SPEC = "spec=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_MODULE_IMPL = "impl=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_TAG_END = "> <".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_INSTANCE = "nstance".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_INSTANCE_CLZ = "class=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_INSTANCE_MTD = "method=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_OF = "f class=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_SERIAL = "erialdata class=\"".getBytes(); // NOI18N
        private static final byte[] MODULE_SETTINGS_END = "settings>".getBytes(); // NOI18N
        
        /** Attempts to read the stream in the same way as SAX parser but avoids using it.
         * If it does not manage to parse it this way, it returns null, in which case
         * you have to use a real parser.
         * @see "#36718"
         */
        private Set<String> quickParse(InputStream is) throws IOException {
            Set<String> iofs = new HashSet<String>();

            if (!expect(is, MODULE_SETTINGS_INTRO)) {
                err.fine("Could not read intro "+source); // NOI18N
                return null;
            }
            version = readTo(is, '"');
            if (version == null) {
                err.fine("Could not read version "+source); // NOI18N
                return null;
            }
            if (!expect(is, MODULE_SETTINGS_INTRO_END)) {
                err.fine("Could not read stuff after cnb "+source); // NOI18N
                return null;
            }
            // Now we have (module?, instanceof*, (instance | serialdata)).
            int c;
        PARSE:
            while (true) {
                c = is.read();
                switch (c) {
                case 'm':
                    // <module />
                    if (!expect(is, MODULE_SETTINGS_MODULE_NAME)) {
                        err.fine("Could not read up to <module name=\" "+source); // NOI18N
                        return null;
                    }
                    String codeName = readTo(is, '"');
                    if (codeName == null) {
                        err.fine("Could not read module name value "+source); // NOI18N
                        return null;
                    }
                    codeName = codeName.intern();
                    resolveModuleElm(codeName);
                    c = is.read();
                    if (c == '/') {
                        if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                            err.fine("Could not read up to end of module tag "+source); // NOI18N
                            return null;
                        }
                        break;
                    }
                    else if (c != ' ') {
                        err.fine("Could not space after module name "+source); // NOI18N
                        return null;
                    }
                    // <module spec/>
                    if (!expect(is, MODULE_SETTINGS_MODULE_SPEC)) {
                        err.fine("Could not read up to spec=\" "+source); // NOI18N
                        return null;
                    }
                    String mspec = readTo(is, '"');
                    if (mspec == null) {
                        err.fine("Could not read module spec value "+source); // NOI18N
                        return null;
                    }
                    try {
                        moduleSpec = new SpecificationVersion(mspec);
                    } catch (NumberFormatException nfe) {
                        return null;
                    }
                    c = is.read();
                    if (c == '/') {
                        if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                            err.fine("Could not read up to end of <module name spec/> tag "+source); // NOI18N
                            return null;
                        }
                        break;
                    }
                    else if (c != ' ') {
                        err.fine("Could not read space after module name "+source); // NOI18N
                        return null;
                    }
                    // <module impl/>
                    if (!expect(is, MODULE_SETTINGS_MODULE_IMPL)) {
                        err.fine("Could not read up to impl=\" "+source); // NOI18N
                        return null;
                    }
                    moduleImpl = readTo(is, '"');
                    if (moduleImpl == null) {
                        err.fine("Could not read module impl value "+source); // NOI18N
                        return null;
                    }
                    moduleImpl = moduleImpl.intern();
                    // /> >
                    if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                        err.fine("Could not read up to /> < "+source); // NOI18N
                        return null;
                    }
                    break;
                case 'i':
                    // <instanceof> or <instance>
                    if (!expect(is, MODULE_SETTINGS_INSTANCE)) {
                        err.fine("Could not read up to instance "+source); // NOI18N
                        return null;
                    }
                    // Now we need to check which one
                    c = is.read();
                    if (c == 'o') {
                        if (!expect(is, MODULE_SETTINGS_OF)) {
                            err.fine("Could not read up to instance"); // NOI18N
                            return null;
                        }
                        String iof = readTo(is, '"');
                        if (iof == null) {
                            err.fine("Could not read instanceof value "+source); // NOI18N
                            return null;
                        }
                        iof = org.openide.util.Utilities.translate(iof).intern();
                        iofs.add (iof);
                        if (is.read() != '/') {
                            err.fine("No / at end of <instanceof> " + iof+" "+source); // NOI18N
                            return null;
                        }
                        if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                            err.fine("Could not read up to next tag after <instanceof> " + iof+" "+source); // NOI18N
                            return null;
                        }
                    }
                    else if (c == ' ') {
                        // read class and optional method
                        if (!expect(is, MODULE_SETTINGS_INSTANCE_CLZ)) {
                            err.fine("Could not read up to class=\" "+source); // NOI18N
                            return null;
                        }
                        instanceClass = readTo(is, '"');
                        if (instanceClass == null) {
                            err.fine("Could not read instance class value "+source); // NOI18N
                            return null;
                        }
                        instanceClass = org.openide.util.Utilities.translate(instanceClass).intern();
                        c = is.read();
                        if (c == '/') {
                            if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                                err.fine("Could not read up to end of instance tag "+source); // NOI18N
                                return null;
                            }
                            break;
                        }
                        else if (c != ' ') {
                            err.fine("Could not space after instance class "+source); // NOI18N
                            return null;
                        }
                        // <instance method/>
                        if (!expect(is, MODULE_SETTINGS_INSTANCE_MTD)) {
                            err.fine("Could not read up to method=\" "+source); // NOI18N
                            return null;
                        }
                        instanceMethod = readTo(is, '"');
                        if (instanceMethod == null) {
                            err.fine("Could not read method value "+source); // NOI18N
                            return null;
                        }
                        instanceMethod = instanceMethod.intern();
                        c = is.read();
                        if (c == '/') {
                            if (!expect(is, MODULE_SETTINGS_TAG_END)) {
                                err.fine("Could not read up to end of instance tag "+source); // NOI18N
                                return null;
                            }
                            break;
                        }
                        err.fine("Strange stuff after method attribute "+source); // NOI18N
                        return null;
                    }
                    else {
                        err.fine("Could not read after to instance "+source); // NOI18N
                        return null;
                    }
                    break;
                case 's':
                    // <serialdata class
                    if (!expect(is, MODULE_SETTINGS_SERIAL)) {
                        err.fine("Could not read up to <serialdata class=\" "+source); // NOI18N
                        return null;
                    }
                    instanceClass = readTo(is, '"');
                    if (instanceClass == null) {
                        err.fine("Could not read serialdata class value "+source); // NOI18N
                        return null;
                    }
                    instanceClass = org.openide.util.Utilities.translate(instanceClass).intern();
                    // here we are complete for header, otherwise we would need to go through serialdata stream
                    c = is.read();
                    if (c != '>') {
                        err.fine("Could not read up to end of serialdata tag "+source); // NOI18N
                        return null;
                    }
                    break PARSE;
                case '/':
                    // </settings
                    // XXX do not read further is neader is set
                    if (!expect(is, MODULE_SETTINGS_END)) {
                        err.fine("Could not read up to end of settings tag "+source); // NOI18N
                        return null;
                    }
                    break PARSE;
                default:
                    err.fine("Strange stuff after <" + (char)c+" "+source); // NOI18N
                    return null;
                }
            }
            if (instanceClass != null && !iofs.isEmpty()) {
                return iofs;
            }
            return null;
        }

        /** Read some stuff from a stream and skip over it.
         * Newlines conventions and whitespaces are normalized to one space.
         * @return true upon success, false if stream contained something else
         */
        private boolean expect(InputStream is, byte[] stuff) throws IOException {
            int len = stuff.length;
            boolean inWhitespace = false;
            for (int i = 0; i < len; ) {
                int c = is.read();
                if (c == 10 || c == 13 || c == ' ' || c == '\t') {
                    // Normalize: s/[\t \r\n]+/\n/g
                    if (inWhitespace) {
                        continue;
                    } else {
                        inWhitespace = true;
                        c = ' ';
                    }
                } else {
                    inWhitespace = false;
                }
                if (c != stuff[i++]) {
                    return false;
                }
            }
            if (stuff[len - 1] == 10) {
                // Expecting something ending in a \n - so we have to
                // read any further \r or \n and discard.
                if (!is.markSupported()) throw new IOException("Mark not supported"); // NOI18N
                is.mark(1);
                int c = is.read();
                if (c != -1 && c != 10 && c != 13) {
                    // Got some non-newline character, push it back!
                    is.reset();
                }
            }
            return true;
        }
        /** Read a maximal string until delim is encountered (which will be removed from stream).
         * This impl reads only ASCII, for speed.
         * Newline conventions are normalized to Unix \n.
         * @return the read string, or null if the delim is not encountered before EOF.
         */
        private String readTo(InputStream is, char delim) throws IOException {
            if (delim == 10) {
                // Not implemented - stream might have "foo\r\n" and we would
                // return "foo" and leave "\n" in the stream.
                throw new IOException("Not implemented"); // NOI18N
            }
            CharArrayWriter caw = new CharArrayWriter(100);
            boolean inNewline = false;
            while (true) {
                int c = is.read();
                if (c == -1) return null;
                if (c > 126) return null;
                if (c == 10 || c == 13) {
                    // Normalize: s/[\r\n]+/\n/g
                    if (inNewline) {
                        continue;
                    } else {
                        inNewline = true;
                        c = 10;
                    }
                } else if (c < 32 && c != 9) {
                    // Random control character!
                    return null;
                } else {
                    inNewline = false;
                }
                if (c == delim) {
                    return caw.toString();
                } else {
                    caw.write(c);
                }
            }
        }

    }
    
    final static class StopSAXException extends SAXException {
        public StopSAXException() {
            super("Parser stopped"); // NOI18N
        }
    }
    
    public static final class Convertor extends org.netbeans.spi.settings.Convertor {
        
        public Object read(java.io.Reader r) throws java.io.IOException, ClassNotFoundException {
            // XXX should be passed also FileObject to ensure full functionality
            // depends on #26076
            XMLSettingsSupport.SettingsRecognizer rec = new XMLSettingsSupport.SettingsRecognizer(false, null);
            rec.parse(r);
            return rec.instanceCreate();
        }
        
        public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        }
        
        public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        }
        
        public void write(java.io.Writer w, Object inst) throws java.io.IOException {
            XMLSettingsSupport.storeToXML10(inst, w, Modules.getDefault().ownerOf(inst.getClass()));
        }
        
    }
}
