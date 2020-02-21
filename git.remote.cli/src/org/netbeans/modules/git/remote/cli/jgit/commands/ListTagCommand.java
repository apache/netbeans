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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitObjectType;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.GitTag.TagContainer;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class ListTagCommand extends GitCommand {
    public static final boolean KIT = false;
    private Map<String, GitTag> allTags;
    private final ProgressMonitor monitor;
    private final boolean all;
    private final Revision revisionPlaseHolder;
    private final String onlyTagName;

    public ListTagCommand (JGitRepository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor, String tagName) {
        super(repository, gitFactory, monitor);
        this.all = all;
        this.monitor = monitor;
        revisionPlaseHolder = new Revision();
        onlyTagName = tagName;
    }
    
    public Map<String, GitTag> getTags () {
        return allTags;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "show-ref"); //NOI18N
        addArgument(0, "--tags"); //NOI18N
        addArgument(0, "-d"); //NOI18N
        if (onlyTagName != null) {
            addArgument(0, onlyTagName); //NOI18N
        }

        addArgument(1, "show"); //NOI18N
        addArgument(1, "--raw"); //NOI18N
        addArgument(1, revisionPlaseHolder);
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            allTags = new LinkedHashMap<>();
            final List<GitTag.TagContainer> list = new ArrayList<GitTag.TagContainer>();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseTagOutput(output, list);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseAddError(error);
                }
                
            }.runCLI();
            
            if (list.size() == 1) {
                for (final GitTag.TagContainer container : list) {
                    if (container.id != null) {
                        revisionPlaseHolder.setContent(container.id);
                        new Runner(canceled, 1){

                            @Override
                            public void outputParser(String output) throws GitException {
                                CreateTagCommand.parseShowDetails(output, container);
                            }

                            @Override
                            protected void errorParser(String error) throws GitException {
                                parseAddError(error);
                            }

                        }.runCLI();
                        allTags.put(container.name, getClassFactory().createTag(container));
                    }
                }
            } else {
                for (final GitTag.TagContainer container : list) {
                    allTags.put(container.name, getClassFactory().createTag(container));
                }
            }
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
    private void parseTagOutput(String output, List<GitTag.TagContainer> list) {
        //git show-ref --tags -d
        //b2eaccb05d0c3f22174824899c4fd796700e66c6 refs/tags/v2.3.0-rc2
        //15598cf41beed0d86cd2ac443e0f69c5a3b40321 refs/tags/v2.3.0-rc2^{}
        for (String line : output.split("\n")) { //NOI18N
            if (!line.isEmpty()) {
                int i = line.indexOf(' ');
                if (i > 0) {
                    String id = line.substring(0,i);
                    String rest = line.substring(i+1);
                    i = rest.indexOf('^');
                    String ref;
                    boolean first;
                    if (i < 0) {
                        ref = rest;
                        first = true;
                    } else {
                        ref = rest.substring(0,i);
                        first = false;
                    }
                    i = ref.lastIndexOf('/');
                    String tag;
                    if (i > 0) {
                        tag = ref.substring(i+1);
                    } else {
                        tag = ref;
                    }
                    if (first) {
                        GitTag.TagContainer container = new GitTag.TagContainer();
                        list.add(container);
                        container.name = tag;
                        container.id = id;
                        container.ref = ref;
                        container.type = GitObjectType.UNKNOWN;
                    } else if (list.size() > 0){
                        TagContainer container = list.get(list.size()-1);
                        assert container.name.equals(tag);
                        container.objectId = id;
                        container.type = GitObjectType.COMMIT;
                    }
                }
            }
        }
    }
    
    private void parseAddError(String error) {
        //The following paths are ignored by one of your .gitignore files:
        //folder2
        //Use -f if you really want to add them.
        //fatal: no files added
        processMessages(error);
    }
    
}
