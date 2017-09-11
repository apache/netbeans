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
                
                if ( nonGuiObject != null && 
                     nonGuiObject instanceof Boolean &&
                     ((Boolean)nonGuiObject).booleanValue() ) {
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
