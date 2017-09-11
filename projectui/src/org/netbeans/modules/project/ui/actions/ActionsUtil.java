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

package org.netbeans.modules.project.ui.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/** Nice utility methods to be used in ProjectBased Actions
 * 
 * @author Pet Hrebejk 
 */
class ActionsUtil {

    private ActionsUtil() {}

    private static final HashMap<String,MessageFormat> pattern2format = new HashMap<String,MessageFormat>();
    
    /** Finds all projects in given lookup. If the command is not null it will check 
     * whther given command is enabled on all projects. If and only if all projects
     * have the command supported it will return array including the project. If there
     * is one project with the command disabled it will return empty array.
     */
    @NonNull
    public static Project[] getProjectsFromLookup( Lookup lookup, String command ) {    
        // First find out whether there is a project directly in the Lookup
        Set<Project> result = new LinkedHashSet<Project>(); // XXX or use OpenProjectList.projectByDisplayName?
        for (Project p : lookup.lookupAll(Project.class)) {
            // All projects have to have the command enabled
            if (command != null && !commandSupported(p, command, lookup)) {
                return new Project[0];
            }
            result.add(p);
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                // All projects have to have the command enabled
                if (command != null && !commandSupported(p, command, lookup)) {
                    return new Project[0];
                }
                result.add( p );
            }
        }
        
        Project[] projectsArray = result.toArray(new Project[result.size()]);
        return projectsArray;
    }
    
    /**
     * split getProjectsFromLookup( Lookup lookup, String command ) into 2 calls
     * to allow FOQ.getOwner to be called outside of AWT
     * @param lookup
     * @return 
     */
    public static Pair<List<Project>, List<FileObject>> mineFromLookup(Lookup lookup) {
        List<Project> result = new ArrayList<Project>(); // XXX or use OpenProjectList.projectByDisplayName?
        for (Project p : lookup.lookupAll(Project.class)) {
            result.add(p);
        }
        List<FileObject> result2 = new ArrayList<FileObject>();
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            result2.add(dObj.getPrimaryFile());
        }
        return Pair.of(result, result2);
    }
    
    /**
     * split getProjectsFromLookup( Lookup lookup, String command ) into 2 calls
     * to allow FOQ.getOwner to be called outside of AWT
     * @return 
     */
    @NonNull
    public static Project[] getProjects( Pair<List<Project>, List<FileObject>> data ) {    
        // First find out whether there is a project directly in the Lookup
        Set<Project> result = new LinkedHashSet<Project>(); // XXX or use OpenProjectList.projectByDisplayName?
        result.addAll(data.first());
        // Now try to guess the project from dataobjects
        for (FileObject fObj : data.second()) {
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                result.add( p );
            }
        }
        Project[] projectsArray = result.toArray(new Project[result.size()]);
        return projectsArray;
    }
    
    /** In given lookup will find all FileObjects owned by given project
     * with given command supported.
     */    
    public static FileObject[] getFilesFromLookup( Lookup lookup, Project project ) {
        HashSet<FileObject> result = new HashSet<FileObject>();
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null && p.equals( project ) ) {
                result.add( fObj );                                        
            }

        }
        
        FileObject[] fos = new FileObject[ result.size() ];
        result.toArray( fos );        
        return fos;
    }
    
    
    /** 
     * Tests whether given command is available on the project and whether
     * the action as to be enabled in current Context
     * @param project Project to test
     * @param command Command for test
     * @param context Lookup representing current context or null if context
     *                does not matter.
     */    
    public static boolean commandSupported( Project project, String command, Lookup context ) {
        //We have to look whether the command is supported by the project
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if ( ap != null ) {
            List<String> commands = Arrays.asList(ap.getSupportedActions());
            if ( commands.contains( command ) ) {
                try {
                if (context == null || ap.isActionEnabled(command, context)) {
                    //System.err.println("cS: true project=" + project + " command=" + command + " context=" + context);
                    return true;
                }
                } catch (IllegalArgumentException x) {
                    Logger.getLogger(ActionsUtil.class.getName()).log(Level.INFO, "#213589: possible race condition in MergedActionProvider", x);
                }
            }
        }            
        //System.err.println("cS: false project=" + project + " command=" + command + " context=" + context);
        return false;
    }
    
    
    
    public static String formatProjectSensitiveName( String namePattern, Project projects[] ) {
     
        // Set the action's name
        if ( projects == null || projects.length == 0 ) {
            // No project selected                 
            return ActionsUtil.formatName( namePattern, 0, null );
        }
        else {
            // Some project selected 
            // XXX what about passing an object that computes the name lazily
             return ActionsUtil.formatName( namePattern, projects.length, new Wrapper(projects[0]));
        }
    }
    
    private static class Wrapper {
        Wrapper(Project prj) {
            project = prj;
        }
        private Project project;

        @Override
        public String toString() {
            return ProjectUtils.getInformation( project ).getDisplayName();
        }
        
    }

    
    /** Good for formating names of actions with some two parameter pattern
     * {0} nuber of objects (e.g. Projects or files ) and {1} name of one
     * or first object (e.g. Project or file) or null if the number is == 0
     * {2} whats the type of the name 0 == normal, 1 == menu, 2 == popup
     */  
    public static String formatName( String namePattern, int numberOfObjects, Object firstObjectName ) {
        
        MessageFormat mf = null;
        
        synchronized ( pattern2format ) {
            mf = pattern2format.get(namePattern);
            if ( mf == null ) {
                mf = new MessageFormat( namePattern );
                pattern2format.put( namePattern, mf );
            }
        }
                
        StringBuffer result = new StringBuffer();
        
        mf.format( 
            new Object[] {
                numberOfObjects,
                firstObjectName == null ? "" : firstObjectName.toString(),
            }, 
            result, 
            null );            
            
        return result.toString();
    }
    
}
