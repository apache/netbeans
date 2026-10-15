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
package org.netbeans.modules.maven.grammar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.jdom2.Element;
import org.netbeans.modules.maven.grammar.catalog.MavenCatalog;
import org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;

/**
 * xml completion grammar based on xsd, additionally allowing more to be added.
 * @author mkleint
 */
public class MavenSettingsGrammar extends AbstractSchemaBasedGrammar {

    public static final String[] UPDATE_POLICIES = new String[]{
        "always", //NOI18N
        "daily", //NOI18N
        "never", //NOI18N
        "interval:10", //NOI18N
        "interval:60" //NOI18N
    };
    public static final String[] CHECKSUM_POLICIES = new String[]{
        "fail", //NOI18N
        "warn" //NOI18N
    };
    public static final String[] LAYOUTS = new String[]{
        "default", //NOI18N
        "legacy" //NOI18N
    };

    public MavenSettingsGrammar(GrammarEnvironment env) {
        super(env);
    }

    @Override
    protected InputStream getSchemaStream() {
        if (getEnvironment().getInputSource() != null && MavenCatalog.SETTINGS_1_1_0.equals(getEnvironment().getInputSource().getSystemId())) {
            return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/settings-1.1.0.xsd"); //NOI18N
        }       
        return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/settings-1.0.0.xsd"); //NOI18N
    }

    @Override
    protected List<GrammarResult> getDynamicCompletion(String path, HintContext hintCtx, Element lowestParent) {
        if ("/settings/proxies".equals(path)) { //NOI18N
            // doesn't work!!!'
//            if ("proxy".startsWith(hintCtx.getCurrentPrefix())) {
//                ArrayList lst = new ArrayList();
//                lst.add(new MyElement("host"));
//                lst.add(new MyElement("port"));
//                GrammarResult rootRes = new ComplexElement("proxy2", "Insert Proxy", new NodeListImpl(lst));
//                return Collections.singletonList(rootRes);
//            }
        }
        return Collections.<GrammarResult>emptyList();
    }

    @Override
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element el) {
        if (path.endsWith("releases/updatePolicy") || //NOI18N
                path.endsWith("snapshots/updatePolicy")) { //NOI18N
            return super.createTextValueList(UPDATE_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("releases/checksumPolicy") || //NOI18N
                path.endsWith("snapshots/checksumPolicy")) { //NOI18N
            return super.createTextValueList(CHECKSUM_POLICIES, virtualTextCtx);
        }
        if (path.endsWith("repository/layout") || //NOI18N
                path.endsWith("pluginRepository/layout")) { //NOI18N
            return super.createTextValueList(LAYOUTS, virtualTextCtx);
        }
        if (path.endsWith("repositories/repository/url") || //NOI18N
                path.endsWith("pluginRepositories/pluginRepository/url")) { //NOI18N
            List<String> repoIds = getRepoUrls();
            return super.createTextValueList(repoIds.toArray(new String[0]), virtualTextCtx);
        }

        if (path.endsWith("pluginGroups/pluginGroup")) { //NOI18N

            ArrayList<GrammarResult> texts = new ArrayList<GrammarResult>();
            Result<String> result = RepositoryQueries.filterPluginGroupIdsResult(virtualTextCtx.getCurrentPrefix(), RepositoryPreferences.getInstance().getRepositoryInfos());
            for (String elem : result.getResults()) {
                texts.add(new MyTextElement(elem, virtualTextCtx.getCurrentPrefix()));
            }
            if (result.isPartial()) {
                texts.add(new PartialTextElement());
            }
            return Collections.enumeration(texts);

        }

        return null;
    }

    /*Return repo url's*/
    private List<String> getRepoUrls() {
        List<String> repos = new ArrayList<String>();

        List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
        for (RepositoryInfo ri : ris) {
            if(ri.getRepositoryUrl()!=null){
                repos.add(ri.getRepositoryUrl());
            }
        }

        return repos;

    }
}
