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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.util;

//import com.sun.tools.ws.processor.model.java.JavaParameter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.net.URL;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;

/**
 * Utility method taken from websvc.manager ManagerUtil.java
 */
public class TypeUtil {

    public static String typeToString(Type type) {

        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)type;
            if (paramType.getOwnerType() != null) return null;
            
            Type rawType = paramType.getRawType();
            if (!(rawType instanceof Class)) {
                return null;
            }
            Class rawClass = (Class)rawType;
            
            Type[] argTypes = paramType.getActualTypeArguments();
            if (argTypes == null || argTypes.length == 0) {
                return null;
            }
            
            StringBuffer arguments = new StringBuffer();
            for (int i = 0; i < argTypes.length; i++) {
                String argument = typeToString(argTypes[0]);
                if (argument == null) {
                    return null;
                }else {
                    arguments.append(argument);
                }
                
                if (i != argTypes.length - 1) {
                    arguments.append(',');
                }
            }
            
            return rawClass.getCanonicalName() + "<" + arguments.toString() + ">";
        }else if (type instanceof GenericArrayType) {
            String component = typeToString(((GenericArrayType)type).getGenericComponentType());
            if (component != null) {
                return component + "[]";
            }
        }else if (type instanceof Class) {
            return ((Class)type).getCanonicalName();
        }
        
        return null;
    }

    /**
     * Creates a classpath from a set of properties from the $userdir/build.properties file
     * 
     * @param srcPath
     * @param isJaxWS true if the classpath should include the JAX-WS jars, false if JAX-RPC should be used
     * @return a URL array containing the classpath jars and directories
     * @throws java.io.IOException 
     */
    public static List<URL> buildClasspath(File srcPath, boolean isJaxWS) throws IOException {
        // The classpath needs to be equivalent to (plus the ws client package root):
        // classpath="${java.home}/../lib/tools.jar:${libs.jaxrpc16.classpath}:${libs.jsf12-support.classpath}"
        ArrayList<URL> urls = new ArrayList<URL>();
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(System.getProperty("netbeans.user") + "/build.properties");
        try {
            properties.load(fis);
        } finally {
            fis.close();
        }
        
        if (srcPath != null) {
            urls.add(srcPath.toURI().toURL());
        }
        
        File toolsJar = new File(System.getProperty("java.home")).getParentFile();
        toolsJar = new File(toolsJar, "lib" + File.separator + "tools.jar");
        urls.add(toolsJar.toURI().toURL());
        
        String pathSeparator = System.getProperty("path.separator");
        String longCP = properties.getProperty("libs.jsf12-support.classpath");
        String libProperty = isJaxWS ? "libs.jaxws21.classpath" : "libs.jaxrpc16.classpath"; // NOI18N
        longCP = properties.getProperty(libProperty) + pathSeparator + longCP;
        
        StringTokenizer st = new StringTokenizer(longCP, pathSeparator);
        
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            File nextFile = new File(next);
            urls.add(nextFile.toURI().toURL());
        }
        return urls;
    }

    /**
     * Bug fix: 5059732
     * This method will return the correct type for the parameter.  If the JavaParameter is considered a "Holder"
     * the holder class name will be used.
     *
     * TODO: include in JAX-RPC API
     *
     * If the parameter is a "Holder" we need the holder type and not the JavaType.  This is
     * typically the case when there is no return type and the parameter's meant to be mutable, pass-by-reference
     * type parameters.  I took the code below directly from the JAX-RPC class:
     * "org.netbeans.modules.visualweb.xml.rpc.processor.generator.StubGenerator" except that I'm not checking the Operation for an Array type.
     * - David Botterill 6/8/2004
     * @param inParameter The JavaParameter to determine the type for.
     * @return String representing the class name for the type.  A null will be returned if the correct name cannot be resolved.
     */
    public static String getParameterType(JavaParameter inParameter) {
        String parameterType = null;
        if (inParameter.isHolder()) {
            parameterType = inParameter.getHolderName();
        } else {
            parameterType =inParameter.getType().getName();
        }
        return parameterType;

    }
}
