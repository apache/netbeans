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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.wizard.impl;


import java.util.HashMap;
import java.util.Map;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 * An ExternalReferenceDecorator is used to control the appearance of the
 * nodes in the ExternalReferenceCustomizer.
 *
 * @author  Nathan Fiedler
 */
public class ExternalReferenceDecorator {

    private SchemaImportGUI panel;
    private static String SCHEMA = "xsd";    
    private static int counter = 0;
    /** Prefix for the namespace prefix values (e.g. "ns"). */
    private static final String PREFIX_PREFIX = "ns"; // NOI18N
   /** Hashmap to keep track of prefixes */
    private Map prefixMap = new HashMap();
   
    public ExternalReferenceDecorator(SchemaImportGUI panel){
       this.panel = panel;
    }
    /**
     * Create an ExternalReferenceNode with the given delegate node.
     * Implementors may wish to delegate to the customizer.
     *
     * @param  node  delegate Node.
     * @return  new ExternalReferenceNode.
     */
    ExternalReferenceDataNode createExternalReferenceNode(Node original){
        return createExternalReferenceNode(original, false);
    }

    ExternalReferenceDataNode createExternalReferenceNode(Node original, boolean throughCatalog) {
        ExternalReferenceDataNode dn = panel.createExternalReferenceNode(original);
        dn.setResolveThroughCatalog(throughCatalog);
        return dn;
    }


    /**
     * Generate a unique prefix value for the document containing the
     * customized component. The selected node is provided, which permits
     * customizing the prefix based on the model represented by the node.
     *
     * @param  prefix   the desired prefix for the namespace prefix;
     *                  if null, a default of "ns" will be used.
     * @param  dobj    DataObject for which to find unique prefix.
     * @return  unique prefix value (e.g. "ns1"); must not be null.
     */
    String generatePrefix(String prefix, DataObject dobj){
       String prefixStr = prefix == null ? PREFIX_PREFIX : prefix;
       String existPrefix = (String)prefixMap.get(dobj);
       if(existPrefix != null)
           return existPrefix;
       
       String generated = prefixStr + counter++;
       prefixMap.put(dobj, generated);
       return generated;
    }

    /**
     * Return the document type that this decorator wants to show in the
     * file chooser.
     *
     * @return  the desired document type.
     */
    String getDocumentType(){
        return SCHEMA;
    }
    
 }
