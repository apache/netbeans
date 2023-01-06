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

package org.netbeans.modules.maven.hints.pom;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixBase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Pair;

/** Manages rules read from the system filesystem.
 *
 * @author Petr Hrebejk, Milos Kleint
 */
public class RulesManager  {

    // The logger
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.maven.hints"); // NOI18N

    // Extensions of files
    private static final String INSTANCE_EXT = ".instance";

    // Non GUI attribute for NON GUI rules
    private static final String NON_GUI = "nonGUI"; // NOI18N
    
    private static final String RULES_FOLDER = "org-netbeans-modules-maven-hints";  // NOI18N

    private RulesManager() {
    }

    public static TreeModel getHintsTreeModel() {
    // Tree models for the settings GUI
        TreeModel hintsTreeModel;
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        hintsTreeModel = new DefaultTreeModel( rootNode );
        FileObject folder = FileUtil.getConfigFile(RULES_FOLDER);
        List<Pair<POMErrorFixBase,FileObject>> rules = readRules(folder);
        categorizeTreeRules( rules, folder, rootNode );
        return hintsTreeModel;
    }


    /** Read rules from system filesystem */
    private static List<Pair<POMErrorFixBase, FileObject>> readRules( FileObject folder ) {
        List<Pair<POMErrorFixBase,FileObject>> rules = new LinkedList<Pair<POMErrorFixBase,FileObject>>();
        
        if (folder == null) {
            return rules;
        }

        Queue<FileObject> q = new LinkedList<FileObject>();
        
        q.offer(folder);
        
        while(!q.isEmpty()) {
            FileObject o = q.poll();
            
            if (o.isFolder()) {
                q.addAll(Arrays.asList(o.getChildren()));
                continue;
            }
            
            if (!o.isData()) {
                continue;
            }
            
            String name = o.getNameExt().toLowerCase();

            if ( o.canRead() ) {
                POMErrorFixBase r = null;
                if ( name.endsWith( INSTANCE_EXT ) ) {
                    r = instantiateRule(o);
                }
                if ( r != null ) {
                    rules.add( Pair.<POMErrorFixBase,FileObject>of( r, o ) );
                }
            }
        }
        return rules;
    }

    private static void categorizeTreeRules( List<Pair<POMErrorFixBase,FileObject>> rules,
                                             FileObject rootFolder,
                                             DefaultMutableTreeNode rootNode ) {
        Map<FileObject,DefaultMutableTreeNode> dir2node = new HashMap<FileObject, DefaultMutableTreeNode>();
        dir2node.put(rootFolder, rootNode);

        for( Pair<POMErrorFixBase,FileObject> pair : rules ) {
            POMErrorFixBase rule = pair.first();
            FileObject fo = pair.second();

                Object nonGuiObject = fo.getAttribute(NON_GUI);
                boolean toGui = true;
                
                if (nonGuiObject instanceof Boolean && ((Boolean)nonGuiObject).booleanValue()) {
                    toGui = false;
                }
                
                FileObject parent = fo.getParent();
                DefaultMutableTreeNode category = dir2node.get( parent );
                if ( category == null ) {
                    category = new DefaultMutableTreeNode( parent );
                    rootNode.add( category );
                    dir2node.put( parent, category );
                }
                if ( toGui ) {
                    category.add( new DefaultMutableTreeNode( rule, false ) );
                }

        }
    }


    private static POMErrorFixBase instantiateRule( FileObject fileObject ) {
        try {
            DataObject dobj = DataObject.find(fileObject);
            InstanceCookie ic = dobj.getLookup().lookup( InstanceCookie.class );
            Object instance = ic.instanceCreate();
            
            if (instance instanceof POMErrorFixBase) {
                return (POMErrorFixBase) instance;
            } else {
                return null;
            }
        } catch( IOException e ) {
            LOG.log(Level.INFO, null, e);
        } catch ( ClassNotFoundException e ) {
            LOG.log(Level.INFO, null, e);
        }

        return null;
    }

}
