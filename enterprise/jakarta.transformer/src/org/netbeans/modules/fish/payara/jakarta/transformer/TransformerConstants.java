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
package org.netbeans.modules.fish.payara.jakarta.transformer;

/**
 *
 * @author alfon
 */
public class TransformerConstants {
    
    public static final String PAYARA_TRANSFORMER_GROUP_ID = "fish.payara.transformer";
    
    public static final String PAYARA_TRANSFORMER_ARTIFACT_ID = "fish.payara.transformer.maven";
    
    public static final String PAYARA_TRANSFORMER_VERSION = "0.2.13";
    
    public static final String PAYARA_TRANSFORMER_GOAL = "run";
           
    public static final String SELECTED_SOURCE_PROPERTY_NAME = "selectedSource";
    
    public static final String SELECTED_TARGET_PROPERTY_NAME = "selectedTarget";
    
    public static final String PROJECT_NAME = "jakartaee-transformed-sample";
    
    public static final String TARGET_FOLDER_NAME = "target";
    
    public static final String POM_FILE_NAME = "pom.xml";
    
    public static final String PACKAGE_PHASE = "package";

    public static final String BANNER_IMAGE = "org/netbeans/modules/fish/payara/jakarta/transformer/BannerJakarta.png";
    
    public static final String INFORMATION_MESSAGE = "After transforming "
            + "application you should need to apply pom configuration "
            + "files manually. The suggested dependencies for Jakarta EE 10 are:";
    
    public static final String ERROR_MESSAGE_DIALOG_TARGET = "The selected target folder is not valid, " +
            "please try again with a valid folder.";
    
    public static final String ERROR_MESSAGE_DIALOG_SOURCE = "The selected source is not valid, " +
            "please try again with a valid source folder or file.";
    
    public static final String JAKARTA_10_DEPENDENCY_EE_API =
            "\n <dependency>"+
            "\n \t \t <groupId>jakarta.platform</groupId>"+
            "\n \t \t <artifactId>jakarta.jakartaee-api</artifactId>"+
            "\n \t \t <version>10.0.0</version>"+
            "\n \t \t <scope>provided</scope>"+
            "\n</dependency> \n";
    public static final String JAKARTA_10_DEPENDENCY_WEB_API =
            "\n <dependency>"+
            "\n \t \t <groupId>jakarta.platform</groupId>"+
            "\n \t \t <artifactId>jakarta.jakartaee-web-api</artifactId>"+
            "\n \t \t <version>10.0.0</version>"+
            "\n \t \t <scope>provided</scope>"+
            "\n </dependency>";
}
