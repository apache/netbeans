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

import org.netbeans.modules.xml.tools.java.generator.ParsletBindings;
import org.netbeans.modules.xml.tools.java.generator.ElementDeclarations;
import org.netbeans.modules.xml.tools.java.generator.ElementBindings;
import java.util.*;

/**
 * Holder of generator settings.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class SAXGeneratorModel implements java.io.Serializable {

    /** Serial Version UID */
    private static final long serialVersionUID =-3982410888926831459L;

    public static final int JAXP_1_0 = 1;

    public static final int JAXP_1_1 = 2;

    public static final int SAX_1_0 = 1;

    public static final int SAX_2_0 = 2;

    /** Holds value of property handler. */
    private java.io.File parentFolder;
    
    /** Holds value of property handler. */
    private String handler;
    
    /** Holds value of property stub. */
    private String stub;
    
    /** Holds value of property parslet. */
    private String parslet;
    
    /** Holds value of property version. */
    private int SAXversion;
    
    /** Holds value of property JAXPversion. */
    private int JAXPversion;
    
    /** Holds value of property parsletImpl. */
    private String parsletImpl;
    
    /** Holds value of property handlerImpl. */
    private String handlerImpl;
    
    /** Holds value of property elementBindings. */
    private ElementBindings elementBindings;
    
    /** Holds value of property parsletBindings. */
    private ParsletBindings parsletBindings;
    
    /** Holds value of property elementDeclarations. */
    private ElementDeclarations elementDeclarations;
    
    /** Holds value of property propagateSAX. */
    private boolean propagateSAX;
    
    private String bindings;
    
    private String packageName;
   
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<SAXGeneratorModel propagateSax=" + propagateSAX + "sax/jaxp=" + SAXversion + "/" + JAXPversion + "\n"); // NOI18N
        sb.append(elementBindings.toString());
        sb.append("  --"); // NOI18N
        sb.append(parsletBindings.toString());
        sb.append("/>\n"); // NOI18N
        return sb.toString();
    }
 
    public SAXGeneratorModel(java.io.File folder, String prefix,
            ElementDeclarations eld, ElementBindings elb,
            ParsletBindings pab, String packageName) {
        this.parentFolder = folder;
        this.handler = getValidName(prefix + "Handler", ".java"); // NOI18N
        this.stub = getValidName(prefix + "Parser", ".java"); // NOI18N
        this.parslet = getValidName(prefix + "Parslet", ".java"); // NOI18N
        this.parsletImpl = getValidName(prefix + "ParsletImpl", ".java"); // NOI18N
        this.handlerImpl = getValidName(prefix + "HandlerImpl", ".java"); // NOI18N
        this.SAXversion = SAX_1_0;
        this.JAXPversion = JAXP_1_1;
        this.elementBindings = elb;
        this.parsletBindings = pab;
        this.elementDeclarations = eld;
        this.bindings = getValidName(prefix + "SAXBindings", ".xml"); // NOI18N
        this.packageName = packageName;
    }
    
    /**
     * Returns the package name.
     * @return Value of package.
     */
    public String getJavaPackage() {
        return packageName;
    }
    
    /** Getter for property handler.
     * @return Value of property handler.
     */
    public String getHandler() {
        return handler;
    }
    
    /** Setter for property handler.
     * @param handler New value of property handler.
     */
    public void setHandler(String handler) {
        this.handler = handler;
    }
    
    /** Getter for property stub.
     * @return Value of property stub.
     */
    public String getStub() {
        return stub;
    }
    
    /** Setter for property stub.
     * @param stub New value of property stub.
     */
    public void setStub(String stub) {
        this.stub = stub;
    }
    
    /** Getter for property parslet.
     * @return Value of property parslet.
     */
    public String getParslet() {
        return parslet;
    }
    
    /** Setter for property parslet.
     * @param parslet New value of property parslet.
     */
    public void setParslet(String parslet) {
        this.parslet = parslet;
    }
    
    /** Getter for property version.
     * @return Value of property version.
     */
    public int getSAXversion() {
        return SAXversion;
    }
    
    /** Setter for property version.
     * @param version New value of property version.
     */
    public void setSAXversion(int version) {
        this.SAXversion = version;
    }
    
    /** Getter for property JAXPversion.
     * @return Value of property JAXPversion (1 for JaXP !.0; 2 for JaXP 1.1).
     */
    public int getJAXPversion() {
        return JAXPversion;
    }
    
    /** Setter for property JAXPversion.
     * @param JAXPversion New value of property JAXPversion.
     */
    public void setJAXPversion(int JAXPversion) {
        this.JAXPversion = JAXPversion;
    }
    
    /** Getter for property parsletImpl.
     * @return Value of property parsletImpl.
     */
    public String getParsletImpl() {
        return parsletImpl;
    }
    
    /** Setter for property parsletImpl.
     * @param parsletImpl New value of property parsletImpl.
     */
    public void setParsletImpl(String parsletImpl) {
        this.parsletImpl = parsletImpl;
    }
    
    /** Getter for property handlerImpl.
     * @return Value of property handlerImpl.
     */
    public String getHandlerImpl() {
        return handlerImpl;
    }
    
    /** Setter for property handlerImpl.
     * @param handlerImpl New value of property handlerImpl.
     */
    public void setHandlerImpl(String handlerImpl) {
        this.handlerImpl = handlerImpl;
    }
    
    /** Getter for property elementBindings.
     * @return Value of property elementBindings.
     */
    public ElementBindings getElementBindings() {
        return elementBindings;
    }
    
    /** Setter for property elementBindings.
     * @param elementBindings New value of property elementBindings.
     */
    public void setElementBindings(ElementBindings elementBindings) {
        this.elementBindings = elementBindings;
    }
    
    /** Getter for property parsletBindings.
     * @return Value of property parsletBindings.
     */
    public ParsletBindings getParsletBindings() {
        return parsletBindings;
    }
    
    /** Setter for property parsletBindings.
     * @param parsletBindings New value of property parsletBindings.
     */
    public void setParsletBindings(ParsletBindings parsletBindings) {
        this.parsletBindings = parsletBindings;
    }
    
    /** Getter for property elementDeclarations.
     * @return Value of property elementDeclarations.
     */
    public ElementDeclarations getElementDeclarations() {
        return elementDeclarations;
    }
    
    /** Setter for property elementDeclarations.
     * @param elementDeclarations New value of property elementDeclarations.
     */
    public void setElementDeclarations(ElementDeclarations elementDeclarations) {
        this.elementDeclarations = elementDeclarations;
    }
    
    /** Getter for property propagateSAX.
     * @return Value of property propagateSAX.
     */
    public boolean isPropagateSAX() {
        return propagateSAX;
    }
    
    /** Setter for property propagateSAX.
     * @param propagateSAX New value of property propagateSAX.
     */
    public void setPropagateSAX(boolean propagateSAX) {
        this.propagateSAX = propagateSAX;
    }

    
    public String getBindings() {
        return bindings;
    }
    
    public void setBindnings(String val) {
        bindings = val;
    }
    
    /**
     * @return true is some parslet mapping exists
     */
    public boolean hasParslets() {
        return parsletBindings.keySet().isEmpty() == false;
    }
    
    /*
     * Load passed bindings into this model for all declared elements.
     */
    public void loadElementBindings(ElementBindings bindings) {
        if (bindings == null) return;
        
        Iterator it = bindings.values().iterator();
        while (it.hasNext()) {
            ElementBindings.Entry next = (ElementBindings.Entry) it.next();
            
            if (elementDeclarations.getEntry(next.getElement()) != null) {
                elementBindings.put(next.getElement(), next);
            }
        }
    }
    
    /*
     * Load passed parslets into this model if a parslet is used.
     * Should be called after loadElementBindings().
     */
    public void loadParsletBindings(ParsletBindings parslets) {
       
        if (parslets == null) return;
        
        Iterator it = parslets.values().iterator();
        while (it.hasNext()) {
            ParsletBindings.Entry next = (ParsletBindings.Entry) it.next();
            
            if (elementBindings.containsParslet(next.getId())) {
                parsletBindings.put(next.getId(), next);
            }
        }
    }
    
    private String getValidName(String name, String extension) {
        java.io.File file = new java.io.File(parentFolder, name + extension); //NOI18N
        while(file.exists()) {
            int suffix = 1;
            int index = name.lastIndexOf("_"); //NOI18N
            if(index != -1) {
                //find the suffix integer value
                String str = name.substring(index+1);
                try {
                    suffix = Integer.valueOf(str).intValue()+1;
                } catch (NumberFormatException ex) {
                    //str was a not an integer
                }
                //trim the last integer
                name = name.substring(0, index);
            }
            //new name = old name (without the last integer) + new integer.
            name =  name + "_" + suffix; //NOI18N
            file = new java.io.File(parentFolder, name + extension);
        }
        return name;
    }
    
}
