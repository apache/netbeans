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
