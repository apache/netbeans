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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/** Pseudo-task to unpack a set of modules.
 * Causes the containing target to both depend on the building of the modules in
 * the first place; and then to unpack them all to a certain location.
 *
 * @author various people
 *
 * 2002-07-31: Rudolf Balada Added build success granularity (Issue 9701),
 *             fixed modules can't fail, "modules" can fail
 */
public class NbMerge extends Task {
    
    private File dest;
    private Vector<String> modules = new Vector<> (); // list of modules defined by build.xml
    private Vector<String> buildmodules = new Vector<> (); // list of modules which will be built
    private Vector<String> fixedmodules = new Vector<> (); // list of fixed modules defined in build.xml
    private Vector<String> buildfixedmodules = new Vector<> (); // List of fixed modules which will be built
    private Vector<String> failedmodules = new Vector<> (); // List of failed modules
    private Vector<String> builtmodules = new Vector<> (); // list of successfully built modules
    private Vector<String> mergemodules = new Vector<> (); // list of successfully built modules
    private Vector<String> builttargets = new Vector<> (); // list of successfully built targets
    private String targetprefix = "all-";    
    private List<File> topdirs = new ArrayList<> ();
    private List<Suppress> suppress = new LinkedList<> ();
    private boolean failonerror = true; // false = enable build success granularity
    private boolean mergedependentmodules = false; // merge also dependent modules
    private String dummyName;
    private Target dummy;
    private Hashtable<String,Target> targets;
    private String builtmodulesproperty = ""; // if set, update property of the name
                                         // to list of successfuly built modules
    
    /** Target directory to unpack to (top of IDE installation). */
    public void setDest (File f) {
        dest = f;
    }

    /** Enable/disable build failing */
    public void setFailOnError (boolean b) {
        failonerror = b;
    }

    /** At the end of task, set system property to the list of successfuly
     *  built modules
     */
    public void setBuiltModulesProperty (String s) {
        builtmodulesproperty = s;
    }
    
    /** Enable/Disable merging also dependencies */
    public void setMergeDependentModules (boolean b) {
        mergedependentmodules = b;
    }
    
    /** Comma-separated list of fixed modules to include. */
    public void setFixedModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        fixedmodules = new Vector<> ();
        while (tok.hasMoreTokens ())
            fixedmodules.addElement (tok.nextToken ());
    }
    
    /** Comma-separated list of modules to include. */
    public void setModules (String s) {
        StringTokenizer tok = new StringTokenizer (s, ", ");
        modules = new Vector<> ();
        while (tok.hasMoreTokens ())
            modules.addElement (tok.nextToken ());
    }
    
    /** String which will have a module name appended to it.
     * This will form a target in the same project which should
     * create the <samp>netbeans/</samp> subdirectory.
     */
    public void setTargetprefix (String s) {
        targetprefix = s;
    }
    
    /** Set the top directory.
     * There should be subdirectories under this for each named module.
     */
    public void setTopdir (File t) {
        topdirs.add (t);
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }
    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }

    /** Locale to suppress. */
    public class Suppress {
        // [PENDING] also support branding here
        String locale;
        String iftest;
        String unlesstest;
        /** Name of the locale, e.g. <samp>ja</samp>. */
        public void setLocale (String l) {
            locale = l;
        }
        /** Property which if set will enable the suppression. */
        public void setIf (String p) {
            iftest = p;
        }
        /** Property which if set will disable the suppression. */
        public void setUnless (String p) {
            unlesstest = p;
        }
    }
    /** Add a locale to suppress.
     * Files matching this locale suffix will not be merged in.
     * E.g. for the locale <samp>ja</samp>, this will exclude
     * all files and directories ending in <samp>_ja</samp> as well
     * as files ending in <samp>_ja.</samp> plus some extension.
     */
    public Suppress createSuppress () {
        Suppress s = new Suppress ();
        suppress.add (s);
        return s;
    }

    
    /** Execute targets which cannot fail and though throw BuildException */
    private void fixedModulesBuild() throws BuildException {
        // Somewhat convoluted code because Project.executeTargets does not
        // eliminate duplicates when analyzing dependencies! Ecch.
        // build fixed modules first
        dummy = new Target ();
        dummyName = "nbmerge-" + getOwningTarget().getName();
        targets = getProject().getTargets();
        while (targets.contains (dummyName))
            dummyName += "-x";
        dummy.setName (dummyName);
        for (String fixedmodule : buildfixedmodules) {
            dummy.addDependency (targetprefix + fixedmodule);
        }
        getProject().addTarget(dummy);

        getProject().setProperty("fixedmodules-built",  "1" );
        @SuppressWarnings("unchecked")
        Vector<Target> fullList = getProject().topoSort(dummyName, targets);
        // Now remove earlier ones: already done.
        Vector<Target> doneList = getProject().topoSort(getOwningTarget().getName(), targets);
        List<Target> todo = new ArrayList<>(fullList.subList(0, fullList.indexOf(dummy)));
        todo.removeAll(doneList.subList(0, doneList.indexOf(getOwningTarget())));
        log("Going to execute targets " + todo);
        for (Target nexttargit: todo) {
            String targetname = nexttargit.getName();
            if ( builttargets.indexOf(targetname) < 0 ) {
                // XXX poor replacement for Project.fireTargetStarted etc.
                System.out.println(""); System.out.println(targetname + ":");
                try {
                    nexttargit.execute();
                } catch (BuildException ex) {
                    log("Failed to build target: " + targetname, Project.MSG_ERR);
                    throw ex;
                }
                builttargets.addElement(targetname);
            }
        }

        builtmodules.addAll(buildfixedmodules); // add already built fixed modules to the list
        log("fixedmodules=" + buildfixedmodules, Project.MSG_DEBUG);
        log("builtmodules=" + builtmodules, Project.MSG_VERBOSE);
    }
    
    /** Execute targets which can fail _without_ throwing BuildException */
    private void modulesBuild() throws BuildException {
        if ( ! failonerror ) {
            // build the rest of modules
            for (String module : buildmodules) {
                dummy = new Target ();
                dummyName = "nbmerge-" + getOwningTarget().getName() + "-" + module;
                while (targets.contains (dummyName))
                    dummyName += "-x";
                dummy.setName (dummyName);
                dummy.addDependency (targetprefix + module);
                getProject().addTarget(dummy);
                @SuppressWarnings("unchecked")
                Vector<Target> fullList = getProject().topoSort(dummyName, targets);
                // Now remove earlier ones: already done.
                @SuppressWarnings("unchecked")
                Vector<Target> doneList = getProject().topoSort(getOwningTarget().getName(), targets);
                List<Target> todo = new ArrayList<>(fullList.subList(0, fullList.indexOf(dummy)));
                todo.removeAll(doneList.subList(0, doneList.indexOf(getOwningTarget())));
                
                Iterator<Target> targit = todo.iterator();
                try {
                    while (targit.hasNext()) {
                        Target nexttargit = targit.next();
                        String targetname = nexttargit.getName();
                        if ( builttargets.indexOf(targetname) < 0 ) {
                            System.out.println(); System.out.println(targetname + ":");
                            nexttargit.execute();
                            builttargets.addElement(targetname);
                        }
                        
                    }
                    builtmodules.addElement(module);
                } catch (BuildException BE) {
                        log(BE.toString(), Project.MSG_WARN);
                        BE.printStackTrace();
                        failedmodules.addElement(module);
                }
            }
            log("builtmodules=" + builtmodules, Project.MSG_VERBOSE);
            log("failedmodules=" + failedmodules, Project.MSG_VERBOSE);
        }
    }
    
    public void execute () throws BuildException {
        if (topdirs.isEmpty ()) {
            throw new BuildException("You must set at least one topdir attribute", getLocation());
        }

        buildfixedmodules.addAll(fixedmodules);
        buildmodules.addAll(modules);
        
        if (( modules.size() > 0 ) && ( fixedmodules.size() == 0 ) && (! failonerror)) {
            log("Unable to build without fixedmodules set", Project.MSG_WARN);
            log("Swapping modules list with fixedmodules list", Project.MSG_WARN);
            buildfixedmodules.addAll(modules);
            buildmodules.removeAllElements();
        }

        if (( failonerror ) && ( modules.size() > 0 )) {
            // failonerror is enabled => build success granularity is disabled
            // though move all modules to fixedmodules
            buildfixedmodules.addAll(modules);
            buildmodules.removeAllElements();
        } 

        // build of fixed modules
        fixedModulesBuild();
        
        // build of the rest of modules
        modulesBuild();
        
        // final data merging
        dataMerge();

        // display build success status
        if (builtmodules.size() > 0 ) {
            log("builtmodules=" + builtmodules);
            log("builttargets=" + builttargets);
            if (failedmodules.size() > 0 ) {
                log("SOME MODULES FAILED TO BUILD, BUT THEIR BuildException WAS CAUGHT.", Project.MSG_WARN);
                log("failedmodules=" + failedmodules, Project.MSG_WARN);
            }
            
            if ( mergemodules.size() > 0 ) {
                if ( builtmodulesproperty.length() > 0 ) {
                    Vector<String> setmodules = new Vector<>();
                    // add all successfuly built modules
                    setmodules.addAll(mergemodules);
                    // remove all fixed modules (don't put fixed modules to modules list)
                    setmodules.removeAll(fixedmodules);
                    // check if the modules list is equal to mergemodules without fixedmodules
                    if (( ! modules.containsAll(setmodules)) || ( ! setmodules.containsAll(modules))) {
                        String bm = setmodules.toString();
                        bm = bm.substring( 1, bm.length() - 1);
                        if (bm.length() > 0 ) {
                            log("Setting property \"" + builtmodulesproperty + "\" to new value " + bm); //, Project.MSG_VERBOSE);
                            getProject().setUserProperty(builtmodulesproperty, bm);
                        }
                    }
                }
            }
            
        } else {
            throw new BuildException("No modules were built", getLocation());
        }
        
    }

    /** Do final data merge */
    private void dataMerge() throws BuildException {
        List<String> suppressedlocales = new LinkedList<> ();
        for (Suppress s: suppress) {
            if (s.iftest != null && getProject().getProperty(s.iftest) == null) {
                continue;
            } else if (s.unlesstest != null && getProject().getProperty(s.unlesstest) != null) {
                continue;
            }
            log ("Suppressing locale: " + s.locale);
            suppressedlocales.add (s.locale);
        }

        UpdateTracking tr = new UpdateTracking( dest.getAbsolutePath() );
        log ( dest.getAbsolutePath() );
        for(String locale: suppressedlocales) {
          tr.removeLocalized(locale);
        }
    }
}
