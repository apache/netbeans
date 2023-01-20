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

package org.netbeans.insane.scanner;

import java.io.*;
import java.lang.reflect.Field;

/**
 * A visitor that stores the heap graph to a XML file in a simple format,
 * which was used by the original Insane implementation.
 *
 * Usage pattern:
 * <pre>
 * SimpleXmlVisitor visitor = new SimpleXmlVisitor(new File("/tmp/insane.xml"));
 * ScannerUtils.scan(null, visitor, rotset, true);
 * visitor.close();
 * </pre>
 *
 * @author  Nenik
 */
public final class SimpleXmlVisitor implements Visitor, AutoCloseable {
    private static final char[] pom = new char[0];
    private static final Class<?> CHAR_ARRAY = pom.getClass();

    private final Writer writer;
    private IOException storedException;
    
    /** Creates a new instance of SimpleXmlVisitor */
    public SimpleXmlVisitor(File to) throws IOException {
        writer = new OutputStreamWriter(new FileOutputStream(to));
        writer.write("<insane>\n");
    }
    
    
       
        
        

    @Override
    public void close() throws IOException {
        writer.write("</insane>\n");
        writer.close();
        if (storedException != null) {
            throw storedException;
        }
    }
        
    
    // ignore for this xml format
    @Override
    public void visitClass(Class cls) {}
        
    @Override
    public void visitObject(ObjectMap map, Object obj) {
        try {
            if (CHAR_ARRAY == obj.getClass()) {
                char[] copy = ((char[]) obj).clone();
                for (int i=0; i<copy.length; i++) {
                    if (copy[i]<0x20) copy[i] = '.';
                    if (copy[i]>=0x80) copy[i] = '.';
                    if (copy[i]=='\'') copy[i] ='"';
                    if (copy[i]=='<') copy[i] ='_';
                    if (copy[i]=='&') copy[i] ='_';
                }
            
                writer.write("<object id='");
                writer.write(map.getID(obj));
                writer.write("' type='");
                writer.write(obj.getClass().getName());
                writer.write("' size='");
                writer.write(String.valueOf(ScannerUtils.sizeOf(obj)));
                writer.write("' value='");
                writer.write(copy);
                writer.write("'/>\n");
            } else {
                writer.write("<object id='");
                writer.write(map.getID(obj));
                writer.write("' type='");
                writer.write(obj.getClass().getName());
                writer.write("' size='");
                writer.write(String.valueOf(ScannerUtils.sizeOf(obj)));
                writer.write("'/>\n");
            }
        } catch (IOException ioe) {
              storedException = ioe;
        }
    }
    
    @Override
    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
        try {
            writer.write("<ref from='" + map.getID(from) +
                                "' name='" + getFldName(ref) + 
                                "' to='" + map.getID(to) + "'/>\n");
        } catch (IOException ioe) {
            storedException = ioe;
        }
    }

    @Override
    public void visitStaticReference(ObjectMap map, Object to, Field ref) {
        try {
            writer.write("<ref name='" + getFldName(ref) + 
                                "' to='" + map.getID(to) + "'/>\n");
        } catch (IOException ioe) {
            storedException = ioe;
        }
    }

    @Override
    public void visitArrayReference(ObjectMap map, Object from, Object to, int index) {
        try {
            writer.write("<ref from='" + map.getID(from) +
                                "' name='" + index + 
                                "' to='" + map.getID(to) + "'/>\n");
        } catch (IOException ioe) {
            storedException = ioe;
        }
    }
    
    private static String getFldName(Field fld) {
        return fld.getDeclaringClass().getName() + "." + fld.getName();
    }


}
