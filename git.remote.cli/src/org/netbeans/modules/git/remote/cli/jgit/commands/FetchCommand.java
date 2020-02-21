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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitConstants;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.jgit.GitFetchResult;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate;
import org.netbeans.modules.git.remote.cli.GitTransportUpdate.GitTransportUpdateContainer;
import org.netbeans.modules.git.remote.cli.jgit.GitClassFactory;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;

/**
 *
 */
public class FetchCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> refSpecs;
    private final String remote;
    private final RefPlaceholder ref = new RefPlaceholder();
    private Map<String, GitTransportUpdate> updates;
    private GitFetchResult result;
    
    public FetchCommand (JGitRepository repository, GitClassFactory gitFactory, String remoteName, ProgressMonitor monitor) {
        this(repository, gitFactory, remoteName, Collections.<String>emptyList(), monitor);
    }

    public FetchCommand (JGitRepository repository, GitClassFactory gitFactory, String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.refSpecs = fetchRefSpecifications;
    }

    public Map<String, GitTransportUpdate> getUpdates () {
        return Collections.unmodifiableMap(updates);
    }
    
    public GitFetchResult getResult () {
        return result;
    }
    
    @Override
    protected void prepare() throws GitException {
        setCommandsNumber(2);
        super.prepare();
        addArgument(0, "fetch"); //NOI18N
        addArgument(0, "-v"); //NOI18N
        addArgument(0, remote);
        for (String refSpec : refSpecs) {
            addArgument(0, refSpec);
        }
        
        addArgument(1, "show-ref"); //NOI18N
        addArgument(1, ref);
    }

    @Override
    protected void runTransportCommand () throws GitException.AuthorizationException, GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            result = new GitFetchResult();
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseFetchOutput(output);
                }

            }.runCLI();
            updates = new LinkedHashMap<>();
            for(GitTransportUpdateContainer c : result.result.values()) {
                if (c.type == GitTransportUpdate.Type.REFERENCE) {
                    ref.setContent(GitConstants.R_REFS+c.localBranch);
                    final GitTag.TagContainer container = new GitTag.TagContainer();
                    new Runner(canceled, 1){

                        @Override
                        public void outputParser(String output) throws GitException {
                            CreateTagCommand.parseShowRef(output, container);
                        }

                    }.runCLI();
                    c.newID = container.id;
                }
                updates.put(c.localBranch, getClassFactory().createTransportUpdate(c));
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
    
    static void parseSRef(String output, GitTag.TagContainer container) {
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

    private void parseFetchOutput(String output) {
        //From /export1/home/cnd-main/libs.git.remote/build/test/unit/work/o.n.l.g.r.j.c.B/dtb/repo2
        //* [new branch]      master     -> origin/master
        //From file:///export/home/tmp/git-upstream-repository
        // = [up to date]      master     -> refs/netbeans_tmp/master
        String url = null;
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("From")) {
                String[] s = line.split("\\s");
                url = s[s.length-1];
                continue;
            }
            if (line.startsWith("*") || line.startsWith(" ")) {
                GitTransportUpdateContainer details = new GitTransportUpdateContainer();
                line = line.trim();
                details.def = '*' == line.charAt(0);
                if (details.def) {
                    line = line.substring(1).trim();
                }
                int i = line.indexOf("->");
                if (i > 0) {
                    details.localBranch = line.substring(i+2).trim();
                    if (details.localBranch.startsWith(GitConstants.R_REFS)) {
                        details.localBranch = details.localBranch.substring(GitConstants.R_REFS.length());
                        details.type = GitTransportUpdate.Type.REFERENCE;
                    } else {
                        details.type = GitTransportUpdate.Type.BRANCH;
                    }
                }
                line = line.substring(0, i).trim();
                int typeBeg = line.indexOf('[');
                int typeEnd = line.indexOf(']');
                if (typeBeg >= 0 && typeEnd > typeBeg) {
                    details.operation = line.substring(typeBeg+1, typeEnd);
                    details.remoteBranch = line.substring(typeEnd+1).trim();
                } else {
                    String[] s = line.split("\\s+");
                    details.remoteBranch = s[s.length-1];
                }
                details.url = url;
                result.result.put(details.localBranch, details);
                continue;
            }
        }
    }
    
    protected static final class RefPlaceholder implements CharSequence {
        private String ref = "place-holder"; //NOI18N
        
        protected RefPlaceholder() {
        }

        protected void setContent(String revision) {
            ref = revision;
        }
        
        @Override
        public int length() {
            return ref.length();
        }

        @Override
        public char charAt(int index) {
            return ref.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return ref.subSequence(end, end);
        }

        @Override
        public String toString() {
            return ref;
        }
    }    
}
