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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class SourceLevelQueryImpl implements SourceLevelQueryImplementation  {

    private static final Logger LOG = Logger.getLogger(SourceLevelQueryImpl.class.getName());
    private static final String DEFAULT_SOURCE_LEVEL = "1.9";
    private static final Pattern JDK_PATTERN = Pattern.compile("jdk([0-9]+)");

    private final String sourceLevel;

    public SourceLevelQueryImpl(FileObject jdkRoot) {
        FileObject jcheckConf = jdkRoot.getFileObject(".jcheck/conf");
        String sl = DEFAULT_SOURCE_LEVEL;

        if (jcheckConf != null) {
            Properties props = new Properties();

            try (InputStream in = jcheckConf.getInputStream()) {
                props.load(in);
                String project = props.getProperty("project", "jdk9");
                Matcher m = JDK_PATTERN.matcher(project);

                if (m.find()) {
                    sl = m.group(1);
                }
            } catch (IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        this.sourceLevel = sl;
    }


    @Override
    public String getSourceLevel(FileObject javaFile) {
        return sourceLevel;
    }
    
}
