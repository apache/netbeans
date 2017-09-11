/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.test.ide;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Interface for BlacklistedClassHandlerSingleton
 * Obtain using BlacklistedClassHandlerSingleton.getInstance or
 * BlacklistedClassHandlerSingleton.getBlacklistedClassHandler methods
 * This guarantees that only one instance is used across the different
 * possible classloaders
 */
public interface BlacklistedClassesHandler {

    /**
     * Registers BlacklistedClassesHandler as handler for 
     * org.netbeans.ProxyClassLoader logger
     */
    public void register();

    /**
     * @return true if BlacklistedClassesHandler is in whitelist generation mode
     */
    boolean isGeneratingWhitelist();

    /**
     * Lists violations with captions
     * @return list of all violations
     */
    String listViolations();

    /**
     * Lists violations
     * @param printCaptions if true prints caption and summary information
     * @return list of all violations
     */
    String listViolations(boolean printCaptions);

    /**
     * Prints list of all violations to the specified PrintStream
     * @param out PrintStream
     * @param printCaptions if true prints caption and summary information
     */
    void listViolations(PrintStream out, boolean printCaptions);

    /**
     * Prints list of all violations using specified PrintWriter
     * @param out PrintWriter
     * @param printCaptions if true prints caption and summary information
     */
    void listViolations(PrintWriter out, boolean printCaptions);

    /**
     * Prints list of all violations to the specified PrintStream
     * @param out PrintStream
     * @param listExceptions if true all exceptions are printed
     * @param printCaptions if true prints caption and summary information
     */
    void listViolations(PrintStream out, boolean listExceptions, boolean printCaptions);

    /**
     * Prints list of all violations using specified PrintWriter
     * @param out PrintWriter
     * @param listExceptions if true all exceptions are printed
     * @param printCaptions if true prints caption and summary information
     */
    void listViolations(PrintWriter out, boolean listExceptions, boolean printCaptions);

    /**
     * Logs list of all violations using Logger
     */
    void logViolations();

    /**
     * @return true if there were any violations
     */
    boolean noViolations();

    /** @rreturn the number of violations */
    int getNumberOfViolations();

    /**
     * 
     * @param listViolations if true outputs list of all violations to the System.out
     * @return true if there were any violations
     */
    boolean noViolations(boolean listViolations);

    /**
     * 
     * @param out if true outputs list of all violations to the specified PrintStream
     * @return true if there were any violations
     */
    boolean noViolations(PrintStream out);

    /**
     * Resets violations information
     */
    void resetViolations();

    /**
     * Saves whitelist
     */
    void saveWhiteList();

    /**
     * Prints whitelist to the specified PrintStream
     */
    void saveWhiteList(PrintStream out);

    /**
     * Saves whitelist to the specified file
     */
    void saveWhiteList(String filename);

    /**
     * Prints whitelist using specified PrintWriter
     */
    void saveWhiteList(PrintWriter out);

    /**
     * 
     * @return true if BlacklistedClassesHandler was initialized properly
     */
    boolean isInitialized();

    /**
     * Initializes the BlacklistedClassesHandler.
     * @param blacklistFileName If null blacklist checking is disabled
     * @param whitelistFileName If null whitelist checking is disabled
     * @param generateWhitelist If true whitelist checking is disabled 
     *                          and all loaded classes are being added to whitelist
     * @return true if Singleton was correctly initialized
     */
    boolean initSingleton(String blacklistFileName, String whitelistFileName, boolean generateWhitelist);

    /**
     * Initializes the BlacklistedClassesHandler.
     * @param configFileName configuration file name
     * @return true if Singleton was correctly initialized
     */
    boolean initSingleton(String configFileName);

    /**
     * Removes BlacklistedClassesHandler from logger
     */
    void unregister();

    /**
     * @return true if whitelist storage is being used
     */
    public boolean hasWhitelistStorage();

    /**
     * Outputs difference between collected list of classes and the last
     * one from the whitelist storage
     * @param out PrintStream
     */
    public void reportDifference(PrintStream out);

    /**
     * Outputs difference between collected list of classes and the last
     * one from the whitelist storage
     * @param out PrintWriter
     */
    public void reportDifference(PrintWriter out);

    /**
     * Returns difference between collected list of classes and the last
     * one from the whitelist storage
     * @return difference report
     */
    public String reportDifference();

    /**
     * Returns only list of violators but prints all the exceptions to out
     * @param out PrintStream
     * @return list of violators
     */
    public String reportViolations(PrintStream out);

    /**
     * Returns only list of violators but prints all the exceptions to out
     * @param out PrintWriter
     * @return list of violators
     */
    public String reportViolations(PrintWriter out);
    
    /**
     * Allows for reinitialization of the handler
     */
    public void resetInitiated();

    /**
     * writes list of violators in NPSS snapshot file
     * @param file File NPSS output file
     */
    public void writeViolationsSnapshot(File file);
        
    /**
     * filters out all violators not containing any of the strings contained in parameter list
     * @param list list of all filters
     */
    public void filterViolators(String[] list);
}
