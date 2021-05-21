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
package org.netbeans.modules.groovy.editor.api.completion;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 * Base class for all groovy CC tests, providing various helper methods.
 * 
 * @author Martin Janicek
 */
public abstract class GroovyCCTestBase extends GroovyTestBase {

    protected final String BASE;

    
    protected GroovyCCTestBase(String testName) {
        super(testName);
        Logger.getLogger(CompletionHandler.class.getName()).setLevel(Level.FINEST);

        BASE = getExpandedSourcePath() + "/"; //NOI18N
    }

    /**
     * This method should return concrete test type which will be used for ClassPath initialization. 
     * For example if method CC tests are located under completion/method this method should return
     * "method" for MethodCCTest.java test case
     *
     * @return concrete test type
     */
    protected abstract String getTestType();

    @Override
    protected Set<String> additionalSourceClassPath() {
        HashSet<String> sourceClassPath = new HashSet<String>();
        sourceClassPath.add(getExpandedSourcePath());

        return sourceClassPath;
    }

    private String getExpandedSourcePath() {
        return getBasicSourcePath() + "/" + firstLetterToLowerCase(getClassName()); //NOI18N
    }

    private String firstLetterToLowerCase(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1, className.length());
    }

    protected String getBasicSourcePath() {
        return "testfiles/completion/" + getTestType(); //NOI18N
    }

    protected String getTestPath() {
        return getExpandedSourcePath() + "/" + getClassName() + ".groovy"; //NOI18N
    }

    /*
     * This method returns simple test class name. For example when test method uses SomeTestMethod.groovy as a
     * class for code completion test, this test method is typically named testSomeNameMethod_1 (and if there
     * is more tests for the same SomeTestMethod.groovy class then the number behind '_' is typically incremented).
     * In this case simple test class name is SomeTestMethod.
     */
    private String getClassName() {
        String name = getName();
        String nameWithoutPrefix = name.substring(4); // Removing 'test' prefix

        int indexOf = nameWithoutPrefix.indexOf("_");
        if (indexOf != -1) {
            nameWithoutPrefix = nameWithoutPrefix.substring(0, indexOf); // Removing _someNumber sufix
        }
        return nameWithoutPrefix;
    }
    
    @Override
    protected void assertDescriptionMatches(String relFilePath, 
            String description, boolean includeTestName, String ext, boolean checkFileExistence) throws Exception {
        super.assertDescriptionMatches(relFilePath, removeSpuriousCompletionItemsFromDescription(description), includeTestName, ext, checkFileExistence);            
    }    
    
    private String removeSpuriousCompletionItemsFromDescription(String description) {
        return description.replaceAll("PACKAGE\\s+apple\\s+null\n", "")
                .replaceAll("PACKAGE\\s+oracle\\s+null\n", "");
    }
}
