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

package org.netbeans.modules.settings.convertors;

import java.beans.ExceptionListener;
import java.beans.PropertyChangeListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.netbeans.spi.settings.Convertor;
import org.netbeans.spi.settings.Saver;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.io.ReaderInputStream;

/** Convertor using {@link  java.beans.XMLEncoder} and
 * {@link  java.beans.XMLDecoder}.
 *
 * @author  Jaroslav Tulach
 */
public final class XMLBeanConvertor extends Convertor implements PropertyChangeListener {
    /**
     * Create convertor instance; should be used in module layers
     * @return convertor instance
     */
    public static Convertor create() {
        return new XMLBeanConvertor();
    }
    
    public XMLBeanConvertor() {
    }
    
    public Object read(java.io.Reader r) throws IOException, ClassNotFoundException {
        java.io.
        BufferedReader buf = new BufferedReader(r, 4096);
        CharBuffer arr = CharBuffer.allocate(2048);
        buf.mark(arr.capacity());
        buf.read(arr);
        arr.flip();

        Matcher m = Pattern.compile("<java").matcher(arr);
        if (m.find()) {
            buf.reset();
            buf.skip(m.start());
        } else {
            buf.reset();
        }
        XMLDecoder d = new XMLDecoder(new ReaderInputStream(buf, "UTF-8"));
        return d.readObject();
    }
    
    public @Override void write(java.io.Writer w, final Object inst) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLEncoder e = new XMLEncoder(out);
        e.setExceptionListener(new ExceptionListener() {
            public @Override void exceptionThrown(Exception x) {
                Logger.getLogger(XMLBeanConvertor.class.getName()).log(Level.INFO, "Problem writing " + inst, x);
            }
        });
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try {
            // XXX would inst.getClass().getClassLoader() be more appropriate?
            ClassLoader ccl2 = Lookup.getDefault().lookup(ClassLoader.class);
            if (ccl2 != null) {
                Thread.currentThread().setContextClassLoader(ccl2);
            }
            e.writeObject(inst);
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
        e.close();
        String data = new String(out.toByteArray(), StandardCharsets.UTF_8);
        data = data.replaceFirst("<java", "<!DOCTYPE xmlbeans PUBLIC \"-//NetBeans//DTD XML beans 1.0//EN\" \"http://www.netbeans.org/dtds/xml-beans-1_0.dtd\">\n<java");
        w.write(data);
    }
    
    /** an object listening on the setting changes */
    private Saver saver;
    public void registerSaver(Object inst, Saver s) {
        if (saver != null) {
            XMLSettingsSupport.err.warning("[Warning] Saver already registered");
            return;
        }
        
        // add propertyChangeListener
        try {
            java.lang.reflect.Method method = inst.getClass().getMethod(
                "addPropertyChangeListener", // NOI18N
                new Class[] {PropertyChangeListener.class});
            method.invoke(inst, new Object[] {this});
            this.saver = s;
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
            XMLSettingsSupport.err.warning("[Warning] trying unregistered unknown Saver");
            return;
        }
        try {
            java.lang.reflect.Method method = inst.getClass().getMethod(
                "removePropertyChangeListener", // NOI18N
                new Class[] {PropertyChangeListener.class});
            method.invoke(inst, new Object[] {this});
            this.saver = null;
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
        if (saver == null) {
            return;
        }
        if (acceptSave()) {
            try {
                saver.requestSave();
            } catch (IOException ex) {
                Logger.getLogger(XMLBeanConvertor.class.getName()).log(Level.WARNING, null, ex);
            }
        } else {
            saver.markDirty();
        }
    }
    
    private boolean acceptSave() {
        return true;
    }
}
