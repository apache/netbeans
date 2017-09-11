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
public final class SimpleXmlVisitor implements Visitor {
    private static char[] pom = new char[0];
    private static Class CHAR_ARRAY = pom.getClass();

    private Writer writer;
    private IOException storedException;
    
    /** Creates a new instance of SimpleXmlVisitor */
    public SimpleXmlVisitor(File to) throws IOException {
        writer = new OutputStreamWriter(new FileOutputStream(to));
        writer.write("<insane>\n");
    }
    
    
       
        
        

    public void close() throws IOException {
        writer.write("</insane>\n");
        writer.close();
        if (storedException != null) throw storedException;
    }
        
    
    // ignore for this xml format
    public void visitClass(Class cls) {}
        
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
            
                writer.write("<object id='" + map.getID(obj) +
                    "' type='" + obj.getClass().getName() + 
                    "' size='" + ScannerUtils.sizeOf(obj) +
                    "' value='" + new String(copy) + "'/>\n");
            } else {
                writer.write("<object id='" + map.getID(obj) +
                    "' type='" + obj.getClass().getName() + 
                    "' size='" + ScannerUtils.sizeOf(obj) +
                    "'/>\n");
            }
        } catch (IOException ioe) {
              storedException = ioe;
        }
    }
    
    public void visitObjectReference(ObjectMap map, Object from, Object to, Field ref) {
        try {
            writer.write("<ref from='" + map.getID(from) +
                                "' name='" + getFldName(ref) + 
                                "' to='" + map.getID(to) + "'/>\n");
        } catch (IOException ioe) {
            storedException = ioe;
        }
    }

    public void visitStaticReference(ObjectMap map, Object to, Field ref) {
        try {
            writer.write("<ref name='" + getFldName(ref) + 
                                "' to='" + map.getID(to) + "'/>\n");
        } catch (IOException ioe) {
            storedException = ioe;
        }
    }

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
