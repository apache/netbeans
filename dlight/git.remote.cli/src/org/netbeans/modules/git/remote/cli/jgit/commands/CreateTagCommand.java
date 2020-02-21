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
public class CreateTagCommand extends GitCommand {
    public static final boolean KIT = false;
    private final boolean forceUpdate;
    private final String tagName;
    private final String taggedObject;
    private final String message;
    private final boolean signed;
    private GitTag tag;
    private final Revision revisionPlaseHolder;
    private final ProgressMonitor monitor;

    public CreateTagCommand (JGitRepository repository, GitClassFactory gitFactory, String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.tagName = tagName;
        this.taggedObject = taggedObject;
        this.message = message;
        this.signed = signed;
        this.forceUpdate = forceUpdate;
        revisionPlaseHolder = new Revision();
    }
    
    public GitTag getTag () {
        return tag;
    }

    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(3);
        super.prepare();
        addArgument(0, "tag"); //NOI18N
        if (signed) {
            addArgument(0, "-s"); //NOI18N
        }
        if (forceUpdate) {
            addArgument(0, "-f"); //NOI18N
        }
        if (message != null && !message.isEmpty()) {
            addArgument(0, "-m"); //NOI18N
            addArgument(0, message.replace("\n", "\\n")); //NOI18N
        }
        addArgument(0, tagName);
        if (taggedObject != null) {
            addArgument(0, taggedObject);
        }
        
        addArgument(1, "show-ref"); //NOI18N
        addArgument(1, "--tags"); //NOI18N
        addArgument(1, tagName);

        addArgument(2, "show"); //NOI18N
        addArgument(2, "--raw"); //NOI18N
        addArgument(2, revisionPlaseHolder);
    }
    
    @Override
    protected void run () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            final TagContainer container = new TagContainer();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                }

            }.runCLI();
            
            new Runner(canceled, 1){

                @Override
                public void outputParser(String output) throws GitException {
                    parseShowRef(output, container);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    parseAddError(error);
                }
                
            }.runCLI();

            if (container.id != null) {
                revisionPlaseHolder.setContent(container.id);
                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseShowDetails(output, container);
                    }

                    @Override
                    protected void errorParser(String error) throws GitException {
                        parseAddError(error);
                    }

                }.runCLI();
                tag = getClassFactory().createTag(container);
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
    
    static void parseShowRef(String output, TagContainer container) {
        //751a4e1249c3588f7d75e223482603e9f6521063 refs/tags/tag-name
        for (String line : output.split("\n")) { //NOI18N
            String[] s = line.split(" ");
            if (s.length == 2) {
                String rev = s[0];
                String ref = s[1];
                int i = ref.lastIndexOf('/');
                if (i > 0) {
                    container.id = rev;
                    container.name = ref.substring(i+1);
                    container.ref = ref;
                }
            }
        }
    }
    
    static void parseShowDetails(String output, TagContainer container) {
        //tag tag-name
        //Tagger: 
        //Date:   Fri Feb 20 12:00:07 2015 +0300
        //
        //tag message
        //
        //commit 37684447393e088928aa256a61058303422b0462
        //Author: 
        //Date:   Fri Feb 20 12:00:07 2015 +0300
        //
        //    init commit
        //
        //:000000 100644 0000000... cdb8d0e... A  f
        //=================
        //commit 68f1985c7152d71a1b7296e5113feb88aeef7b80
        //Author: 
        //Date:   Fri Feb 20 17:40:43 2015 +0300
        //
        //    init commit
        //
        //:000000 100644 0000000... cdb8d0e... A	f        
        
        State state = State.header;
        for (String line : output.split("\n")) { //NOI18N
            if (state == State.header) {
                if (line.startsWith("tag ")) {
                    continue;
                }
                if (line.startsWith("Tagger:")) {
                    container.author = line.substring(7).trim();
                    continue;
                }
                if (line.startsWith("Date:")) {
                    container.time = line.substring(5).trim();
                    continue;
                }
                if (line.startsWith("commit")) {
                    state = State.object;
                } else {
                    state = State.message;
                    continue;
                }
            }
            if (state == State.message) {
                if (line.length() > 0) {
                    if (container.message == null) {
                        container.message = line.trim();
                    } else {
                        container.message += "\n"+line.trim();
                    }
                } else {
                    state = State.object;
                }
                continue;
            }
            if (state == State.object) {
                if (line.startsWith("commit")) {
                    container.type = GitObjectType.COMMIT;
                    container.objectId = line.substring(6).trim();
                    break;
                }
                if (line.startsWith("tree")) {
                    container.type = GitObjectType.TREE;
                    container.objectId = line.substring(4).trim();
                    break;
                }
                if (line.startsWith("tag")) {
                    container.type = GitObjectType.TAG;
                    container.objectId = line.substring(3).trim();
                    break;
                }
                continue;
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
    
    private enum State {
        header,
        message,
        object
    }
}
