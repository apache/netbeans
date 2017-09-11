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
package org.netbeans.modules.xml.tools.java.generator;

import java.util.*;
import java.lang.reflect.Modifier;

//import org.openide.src.*;

/**
 * Holds declared parslets by parslet name.
 * <!ELEMENT parslet #EMPTY>
 * <!ATTLIST parslet id ID #REQUIRED>
 * <!ATTLIST parslet return CDATA #REQUIRED>  //primitive type or fully classified class
 */
public class ParsletBindings extends HashMap {

    //TODO: Retouche
    /** Serial Version UID */
    private static final long serialVersionUID =5328744032505397530L;


    // parameter names to generated methods
    
    static final String DATA = "data";  // NOI18N
    static final String META = "meta";  // NOI18N

    static final String STRING_TYPE = "java.lang.String";
//    
//    static final MethodParameter DEFAULT_DATA_PARAMETER = 
//        new MethodParameter(DATA, STRING_TYPE, true);
//    
//    static final MethodParameter[] DEFAULT_DATA_PARAMETERS = 
//        new MethodParameter[] { DEFAULT_DATA_PARAMETER };    
//        
    /** Create empty map. */
    public ParsletBindings() {            
    }

    /** 
      * Typed put.
      * @see java.util.Map#put(Object,Object)
      */
    public Entry put(String parslet, String returnType) {
        
            return (Entry) super.put(parslet, new Entry(parslet, returnType));
        
    }

    public Entry put(String parslet, Entry entry) {
        if (parslet == null) return null;
        if (parslet.equals(entry.getId()) == false) return null;
        
        return (Entry) super.put(parslet, entry);
    }
    
//    /** 
//      * Get a MethodParameter produced by given parslet.
//      * @param parslet id of parslet or null
//      * @return parslet return type as MethodParameter or DEFAULT_DATA_PARAMETER if null param
//      */
 //   public MethodParameter getReturnAsParameter(String parslet) {
//        Entry param = seek(parslet);
//        if (param == null) {
//            return DEFAULT_DATA_PARAMETER;
//        } else {
//            return new MethodParameter(DATA, param.type, true);
//        }
 //   }
//
    public Entry getEntry(String parslet) {
        return (Entry) super.get(parslet);
    }

    /** 
      * Get return Type produced by given parslet.
      * @param parslet id of parslet or null
      * @return parslet return Type or String Type if null param
      */        
    public String getReturnType(String parslet) {

        Entry param = seek(parslet);
        if (param == null) {
            return STRING_TYPE;
        } else {
            return param.type;
        }            
    }

//    /**
//      * Get Method element representing parslet method or null.
//      */
//    public MethodElement getMethod(String parslet) throws SourceException {
//        Entry param = seek(parslet);
//        if (param == null) {
//            return null;
//        } else {
//            MethodElement method = new MethodElement();
//            method.setName(Identifier.create(param.id));
//            method.setParameters(DEFAULT_DATA_PARAMETERS);
//            method.setReturn(param.type);
//            method.setModifiers(Modifier.PUBLIC);
//            method.setExceptions (new Identifier[] { Identifier.create("SAXException") }); // NOI18N
//            return method;
//        }
//    }
//
    /**
      * Get parslet from map or null.
      */
    private Entry seek(String parslet) {
        if (parslet == null) {
            return null;
        } else {
            Entry param = (Entry) super.get(parslet);
            if (param == null) {
                return null;
            } else {
                return param;
            }
        }            
    }
 
    /**
     * Holds information about a parslet.
     * Not used yet.
     */    
    public static final class Entry {

        /** Holds value of property parslet id. */
        private String id;
//
        /** Holds value of property type. */
        private String type;
//
        /** Creates new ParsletEntry */
        public Entry(String id, String type)  {
            this.id = id;
            this.type = type;
        }

        /** 
         * Getter for property id.
         * @return Value of property id.
         */
        public String getId() {
            return id;
        }

        /** 
         * Getter for property type.
         * @return Value of property type.
         */
        public String getType() {
            return type;
        }

        void setReturnType(String type) {
            this.type = type;
        }

        public String toString() {
            return "(" + id + " => " + type + ")"; // NOI18N
        }
    }
}
