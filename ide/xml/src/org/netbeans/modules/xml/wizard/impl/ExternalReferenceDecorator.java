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
