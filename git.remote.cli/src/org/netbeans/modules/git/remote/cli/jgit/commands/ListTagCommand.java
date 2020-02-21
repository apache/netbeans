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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
