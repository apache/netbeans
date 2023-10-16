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
package org.netbeans.modules.java.lsp.server.singlesourcefile;

import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.hints.spi.preview.PreviewEnabler;
import org.netbeans.modules.java.lsp.server.Utils;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.UpdateConfigParams;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handle error rule "compiler.err.preview.feature.disabled.plural" and provide
 * the fix for Single Source Java File.
 *
 * @author Arunava Sinha
 */
public class EnablePreviewSingleSourceFile implements PreviewEnabler {

    private static final String ENABLE_PREVIEW_FLAG = "--enable-preview";   // NOI18N
    private static final String SOURCE_FLAG = "--source";   // NOI18N
    private static final Pattern SOURCE_FLAG_PATTERN = Pattern.compile(SOURCE_FLAG + "[ \t]+[0-9]+");

    private FileObject file;

    private EnablePreviewSingleSourceFile(@NonNull FileObject file) {
        Parameters.notNull("file", file); //NOI18N
        this.file = file;
    }

    @Override
    public void enablePreview(String newSourceLevel) throws Exception {
        NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
        if (client == null) {
            return ;
        }

        ConfigurationItem conf = new ConfigurationItem();
        conf.setScopeUri(Utils.toUri(file));
        conf.setSection(client.getNbCodeCapabilities().getAltConfigurationPrefix() + "runConfig.vmOptions");
        client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenApply(c -> {
            String compilerArgs = ((JsonPrimitive) ((List<Object>) c).get(0)).getAsString();
            if (compilerArgs == null) {
                compilerArgs = "";
            }

            Matcher m = SOURCE_FLAG_PATTERN.matcher(compilerArgs);
            String realNewSourceLevel = newSourceLevel;

            if (realNewSourceLevel == null) {
                realNewSourceLevel = getJdkRunVersion();
            }

            if (compilerArgs.contains(SOURCE_FLAG)) {
                compilerArgs = m.replaceAll(ENABLE_PREVIEW_FLAG + " " + SOURCE_FLAG + " " + realNewSourceLevel);
            } else {
                compilerArgs += (compilerArgs.isEmpty() ? "" : " ") + ENABLE_PREVIEW_FLAG + " " + SOURCE_FLAG + " " + realNewSourceLevel;
            }
            client.configurationUpdate(new UpdateConfigParams(client.getNbCodeCapabilities().getAltConfigurationPrefix() + "runConfig", "vmOptions", compilerArgs));
            return null;
        });
    }

    private static String getJdkRunVersion() {
        String javaVersion = System.getProperty("java.specification.version"); //NOI18N 
        if (javaVersion.startsWith("1.")) { //NOI18N
            javaVersion = javaVersion.substring(2);
        }

        return javaVersion;
    }

    @ServiceProvider(service=Factory.class, position=10_000_000)
    public static final class FactoryImpl implements Factory {

        @Override
        public PreviewEnabler enablerFor(FileObject file) {
            if (file != null) {
                NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);

                if (client == null) {
                    return null;
                }

                Project prj = FileOwnerQuery.getOwner(file);

                if (prj == null) {
                    return new EnablePreviewSingleSourceFile(file);
                }
            }

            return null;
        }

    }

}
