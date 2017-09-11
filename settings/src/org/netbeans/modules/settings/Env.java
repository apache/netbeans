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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.modules.settings;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.Lookup;

/** A provider for .settings files of a certain DTD.
 *  It creates a suitable convertor according to {@link #EA_CONVERTOR}.
 *
 * @author Jan Pokorsky
 */
public final class Env implements Environment.Provider {
    /** file attribute containing convertor object. Usage 
     * <code>&lt;attr name="settings.convertor" methodvalue="org.netbeans.modules.settings.XMLPropertiesConvertor.create"/>
     * </code>
     */
    public final static String EA_CONVERTOR = "settings.convertor"; //NOI18N
    /** file attribute containing path to the provider. Used by
     * InstanceDataObject.create or upgrade algorithm. Usage 
     * <code>&lt;attr name="settings.providerPath" stringvalue="xml/lookups/NetBeans/DTD_XML_Properties_1_0.instance"/>
     * </code>
     */
    public final static String EA_PROVIDER_PATH = "settings.providerPath"; // NOI18N
    /** file attribute containing PUBLIC attribute of xml header. Usage
     * <code>&lt;attr name="hint.originalPublicID" stringvalue="-//NetBeans//DTD XML Properties 1.0//EN"/>
     * </code>
     */
    public final static String EA_PUBLICID = "hint.originalPublicID"; // NOI18N
    /** file attribute containnig class name of the setting object. Usage
     * <code>&lt;attr name="settings.instanceClass" stringvalue="org.netbeans.modules.foo.Foo"/>
     * </code>
     */
    public final static String EA_INSTANCE_CLASS_NAME = "settings.instanceClass"; //NOI18N
    /** file attribute containnig class name and subclass names of the setting object. Use the
     * attribute for performance reasons. Usage
     * <code>&lt;attr name="settings.instanceOf" stringvalue="org.netbeans.modules.foo.Foo[, ...]"/>
     * </code>
     */
    public final static String EA_INSTANCE_OF = "settings.instanceOf"; //NOI18N
    /** file attribute containnig the setting object. Usage
     * <code>&lt;attr name="settings.instanceCreate" newvalue="org.netbeans.modules.foo.Foo"/>
     * </code> or
     * <code>&lt;attr name="settings.instanceCreate" methodvalue="org.netbeans.modules.foo.Foo.create"/>
     * </code>
     */
    public final static String EA_INSTANCE_CREATE = "settings.instanceCreate"; //NOI18N
    /** file attribute determining whether the registration works also for subclasses of the registering
     * class. Use of this attribute is optional. The default is false, the value must be boolean, example:
     * <code>&lt;attr name="settings.subclasses" boolvalue="true"/&gt;</code>
     */
    public static final String EA_SUBCLASSES = "settings.subclasses"; // NOI18N
    
    private final FileObject providerFO;
    
    /** create Environment.Provider */
    public static Environment.Provider create(FileObject fo) {
        return new Env(fo);
    }
    
    private Env(FileObject fo) {
        providerFO = fo;
    }
    
    public Lookup getEnvironment(DataObject dobj) {
        boolean recognize = false;
        if (dobj instanceof InstanceDataObject) {
            recognize = true;
        } else if (dobj instanceof XMLDataObject) {
            recognize = Boolean.TRUE.equals(dobj.getPrimaryFile().getParent().getAttribute("recognizeXML"));
        }

        if (!recognize) {
            return Lookup.EMPTY;
        }
        InstanceProvider icp = new InstanceProvider(dobj, providerFO);
        return icp.getLookup();
    }
    
    /** parse file attribute
     * @param attr String value can be null; used delimiter is ","
     * @return set of items
     */
    public static java.util.Set<String> parseAttribute(Object attr) {
        if (attr != null && attr instanceof String) {
            java.util.StringTokenizer s = 
                new java.util.StringTokenizer((String) attr, ","); //NOI18N
            java.util.Set<String> set = new java.util.HashSet<String>(10);
            while (s.hasMoreTokens()) {
                set.add(s.nextToken().trim());
            }
            return set;
        } else {
            return java.util.Collections.emptySet();
        }
    }
    
    /** look up appropriate provider according to clazz */
    public static FileObject findProvider(Class clazz) throws IOException {
        String prefix = "xml/memory/"; //NOI18N
        FileObject memContext = FileUtil.getConfigFile(prefix);
        if (memContext == null) throw new java.io.FileNotFoundException("SFS/xml/memory/"); //NOI18N
        Class c = clazz;
        while (c != null) {
            String name = c.getName().replace('.', '/');
            String convertorPath = new StringBuffer(200).append(prefix).
                append(name).toString(); // NOI18N
            FileObject fo = FileUtil.getConfigFile(convertorPath);
            if (fo != null) {
                String providerPath = (String) fo.getAttribute(EA_PROVIDER_PATH);
                if (providerPath != null) {
                    if (c.equals(clazz)) {
                        return FileUtil.getConfigFile(providerPath);
                    } else {
                        // check the special subclasses attribute
                        Object inheritAttribute = fo.getAttribute(EA_SUBCLASSES);
                        if (inheritAttribute instanceof Boolean) {
                            boolean subclasses = ((Boolean)inheritAttribute).booleanValue();
                            if (subclasses) {
                                return FileUtil.getConfigFile(providerPath);
                            }
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    private static String xmlLookupsPrefix = "xml/lookups"; // NOI18N
    private static String xmlEntitiesPrefix = "xml/entities"; // NOI18N
    
    /** find an entity registration according to passed provider
     * @param provider provider file object
     * @return entity file object
     */
    public static FileObject findEntityRegistration(FileObject provider) {
        String filename = provider.getPath();
        int i = filename.lastIndexOf('.');
        if (i != -1 && i > filename.lastIndexOf('/')) {
            filename = filename.substring(0, i);
        }
        String resource = xmlEntitiesPrefix +
            filename.substring(xmlLookupsPrefix.length(), filename.length());
        
        return FileUtil.getConfigFile(resource);
    }
}
