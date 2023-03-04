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

package org.netbeans.libs.git.jgit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.netbeans.junit.Filter;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitClientCallback;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;

/**
 *
 * @author ondra
 */
public class ConnectionTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public ConnectionTest (String testName) throws IOException {
        super(testName);
        if (Boolean.getBoolean("skip.git.integration.tests")) {
            Filter filter = new Filter();
            filter.setExcludes(new Filter.IncludeExclude[] {
                new Filter.IncludeExclude("testGitConnection", ""),
                new Filter.IncludeExclude("testHttpConnection", ""),
                new Filter.IncludeExclude("testHttpConnectionPublic", ""),
                new Filter.IncludeExclude("testHttpConnectionCredentialsInUri", ""),
                new Filter.IncludeExclude("testHttpConnectionEmptyPassword", ""),
                new Filter.IncludeExclude("testSshConnectionCredentialsInUri", ""),
                new Filter.IncludeExclude("testSshConnectionCredentialsFromCallback", ""),
                new Filter.IncludeExclude("testSshConnectionGITSSH_Issue213394", ""),
                new Filter.IncludeExclude("testSshConnectionPassphrase", ""),
                new Filter.IncludeExclude("testSshFetchCredentialsFromCallback", ""),
                new Filter.IncludeExclude("testSshConnectionUserInUriPasswordFromCallback", ""),
                new Filter.IncludeExclude("testSshConnectionCanceled", "")
            });
            setFilter(filter);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }
    
    public void testGitConnection () throws Exception {
        GitClient client = getClient(workDir);
        client.listRemoteBranches("git://vcs-test.cz.oracle.com/AnagramGameGit.git/", NULL_PROGRESS_MONITOR);
    }
    
    // start damon as git daemon --base-path=/srv/git --verbose --export-all /srv/git &
    public void testHttpConnectionCredentialsInUri () throws Exception {
        // UN and PWD in uri
        GitClient client = getClient(workDir);
        client.listRemoteBranches("http://user:heslo@vcs-test.cz.oracle.com/git/repo/", NULL_PROGRESS_MONITOR);
    }
    
    public void testHttpConnection () throws Exception {
        // UN and PWD provided by a callback
        GitClient client = getClient(workDir);
        try {
            client.listRemoteBranches("http://vcs-test.cz.oracle.com/git/repo/", NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            assertEquals("http://vcs-test.cz.oracle.com/git/repo/", ex.getRepositoryUrl());
        }
        GitClientCallback callback = new GitClientCallback() {
            @Override
            public String askQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getUsername (String uri, String prompt) {
                return "user";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "heslo".toCharArray();
            }

            @Override
            public char[] getPassphrase (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getIdentityFile (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            @Override
            public Boolean askYesNoQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("http://vcs-test.cz.oracle.com/git/repo/", NULL_PROGRESS_MONITOR);
    }
    
    public void testHttpConnectionPublic () throws Exception {
        GitClient client = getClient(workDir);
        // no username or password
        client.setCallback(null);
        try {
            client.listRemoteBranches("http://vcs-test.cz.oracle.com/git/repo/", NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            assertEquals("http://vcs-test.cz.oracle.com/git/repo/", ex.getRepositoryUrl());
        }
        client.listRemoteBranches("http://vcs-test.cz.oracle.com/git-public/repo/", NULL_PROGRESS_MONITOR);
        // callback should not be called at all
        client.setCallback(new DefaultCallback());
        client.listRemoteBranches("http://vcs-test.cz.oracle.com/git-public/repo/", NULL_PROGRESS_MONITOR);
    }
    
    public void testHttpConnectionEmptyPassword () throws Exception {
        GitClient client = getClient(workDir);
        // UN and EMPTY password provided by a callback
        GitClientCallback callback = new GitClientCallback() {
            @Override
            public String askQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getUsername (String uri, String prompt) {
                return "user2";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "".toCharArray();
            }

            @Override
            public char[] getPassphrase (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getIdentityFile (String uri, String prompt) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Boolean askYesNoQuestion (String uri, String prompt) {
                throw new UnsupportedOperationException();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("http://vcs-test.cz.oracle.com/git/repo/", NULL_PROGRESS_MONITOR);
    }

    public void testSshConnectionCredentialsInUri () throws Exception {
        GitClient client = getClient(workDir);
        client.setCallback(new DefaultCallback());
        client.listRemoteBranches("ssh://tester:tester@vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
    }

    public void testSshConnectionCanceled () throws Exception {
        GitClient client = getClient(workDir);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return null;
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return null;
            }
        };
        client.setCallback(callback);
        try {
            client.listRemoteBranches("ssh://vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
        } catch (GitException.AuthorizationException ex) {
            assertEquals("ssh://vcs-test.cz.oracle.com/srv/git/repo/", ex.getRepositoryUrl());
        }
    }

    public void testSshConnectionCredentialsFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return "tester";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "tester".toCharArray();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("ssh://vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
    }

    public void testSshConnectionUserInUriPasswordFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public char[] getPassword (String uri, String prompt) {
                return "tester".toCharArray();
            }
        };
        client.setCallback(callback);
        client.listRemoteBranches("ssh://tester@vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
    }

    public void testSshFetchCredentialsFromCallback () throws Exception {
        GitClient client = getClient(workDir);
        client.setRemote(new GitRemoteConfig("origin",
                Arrays.asList("ssh://vcs-test.cz.oracle.com/srv/git/repo/"),
                Collections.<String>emptyList(),
                Arrays.asList("+refs/heads/*:refs/remotes/origin/*"),
                Collections.<String>emptyList()), NULL_PROGRESS_MONITOR);
        GitClientCallback callback = new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return "tester";
            }

            @Override
            public char[] getPassword (String uri, String prompt) {
                return "tester".toCharArray();
            }
        };
        client.setCallback(callback);
        client.fetch("origin", NULL_PROGRESS_MONITOR);
    }
    
    public void testSshConnectionPassphrase () throws Exception {
        GitClient client = getClient(workDir);
        client.setCallback(new DefaultCallback() {
            @Override
            public String getUsername (String uri, String prompt) {
                return "gittester";
            }

            @Override
            public String getIdentityFile (String uri, String prompt) {
                return new File(getDataDir(), "testing_key").getAbsolutePath();
            }
            
            @Override
            public char[] getPassphrase (String uri, String prompt) {
                assertTrue("Expected passphrase prompt for testing_key, was " + prompt, prompt.contains(new File(getDataDir(), "testing_key").getAbsolutePath()));
                return "qwerty".toCharArray();
            }
        });
        client.listRemoteBranches("ssh://gittester@vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
    }
    
    public void testSshConnectionGITSSH_Issue213394 () throws Exception {
        SystemReader sr = SystemReader.getInstance();
        SystemReader.setInstance(new DelegatingSystemReader(sr) {

            @Override
            public String getenv (String string) {
                if ("GIT_SSH".equals(string)) {
                     return "/usr/bin/externalsshtool";
                }
                return super.getenv(string);
            }
            
        });
        try {
            GitClient client = getClient(workDir);
            client.setCallback(new DefaultCallback() {
                @Override
                public String getUsername (String uri, String prompt) {
                    return "gittester";
                }

                @Override
                public String getIdentityFile (String uri, String prompt) {
                    return new File(getDataDir(), "testing_key").getAbsolutePath();
                }

                @Override
                public char[] getPassphrase (String uri, String prompt) {
                    assertTrue("Expected passphrase prompt for testing_key, was " + prompt, prompt.contains(new File(getDataDir(), "testing_key").getAbsolutePath()));
                    return "qwerty".toCharArray();
                }
            });
            client.listRemoteBranches("ssh://gittester@vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
            // The minority of users really wanting to use external SSH still have the chance by using a commandline switch.
            // see issue #227161
            System.setProperty("versioning.git.library.useSystemSSHClient", "true");
            try {
                client.listRemoteBranches("ssh://gittester@vcs-test.cz.oracle.com/srv/git/repo/", NULL_PROGRESS_MONITOR);
            } catch (GitException ex) {
                assertTrue(ex.getMessage().contains("Cannot run program \"/usr/bin/externalsshtool\""));
            }
        } finally {
            SystemReader.setInstance(sr);
            System.setProperty("versioning.git.library.useSystemSSHClient", "false");
        }
    }
    
    /**
     * When starts failing then consider rewriting callbacks to return passwords only in getPassword
     * For this test to pass, keyboard-interactive ssh authentication must be enabled on localhost
     */
    public void testSshLocalhostConnection () throws Exception {
        GitClient client = getClient(workDir);
        final AtomicBoolean asked = new AtomicBoolean(false);
        client.setCallback(new DefaultCallback() {
            @Override
            public String askQuestion (String uri, String prompt) {
                assertTrue("Expected question prompt for password", prompt.startsWith("Password"));
                asked.set(true);
                return null;
            }

            @Override
            public String getUsername (String uri, String prompt) {
                return "gittester2";
            }

            @Override
            public String getIdentityFile (String uri, String prompt) {
                return new File(getDataDir(), "testing_key").getAbsolutePath();
            }
            
            @Override
            public char[] getPassphrase (String uri, String prompt) {
                return null;
            }
        });
        try {
            client.listRemoteBranches("ssh://gittester2@127.0.0.1/" + workDir.getAbsolutePath(), NULL_PROGRESS_MONITOR);
        } catch (GitException ex) {
            
        }
        // this depends on the server setup
//        assertTrue(asked.get());
    }
    
    private static class DefaultCallback extends GitClientCallback {
        @Override
        public String askQuestion (String uri, String prompt) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getUsername (String uri, String prompt) {
            return null;
        }

        @Override
        public char[] getPassword (String uri, String prompt) {
            return null;
        }

        @Override
        public String getIdentityFile (String uri, String prompt) {
            return null;
        }

        @Override
        public Boolean askYesNoQuestion (String uri, String prompt) {
            if (prompt.contains("RSA key fingerprint")) {
                return true;
            }
            return null;
        }

        @Override
        public char[] getPassphrase (String uri, String prompt) {
            return "".toCharArray();
        }
        
    }

    private static class DelegatingSystemReader extends SystemReader {
        private final SystemReader instance;

        public DelegatingSystemReader (SystemReader sr) {
            this.instance = sr;
        }

        @Override
        public String getHostname () {
            return instance.getHostname();
        }

        @Override
        public String getenv (String string) {
            return instance.getenv(string);
        }

        @Override
        public String getProperty (String string) {
            return instance.getProperty(string);
        }

        @Override
        public FileBasedConfig openUserConfig (Config config, FS fs) {
            return instance.openUserConfig(config, fs);
        }

        @Override
        public FileBasedConfig openSystemConfig (Config config, FS fs) {
            return instance.openSystemConfig(config, fs);
        }

        @Override
        public long getCurrentTime () {
            return instance.getCurrentTime();
        }

        @Override
        public int getTimezone (long l) {
            return instance.getTimezone(l);
        }

        @Override
        public FileBasedConfig openJGitConfig(Config config, FS fs) {
            return instance.openJGitConfig(config, fs);
        }
    }
}
