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

package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 * Task to retrieve named files (generally large binaries such as ZIPs) from a repository.
 * Similar to a very simplified version of Ivy, but correctly handles binaries
 * with missing or irrelevant version numbers, since it is based on a hash of contents.
 * You keep one or more manifests under version control which enumerate files and their SHA-1 hashes.
 * Then just run this task to download any missing files.
 * Remember to specify the binaries as "ignorable" to your version control system.
 * You can also run it in a clean mode which will remove the binaries.
 * At the end of this source file is a sample CGI script and matching form which you can run on the server
 * to permit people to upload files to the correct repository paths.
 * Motivation: http://wiki.netbeans.org/wiki/view/HgMigration#section-HgMigration-Binaries
 */
public class DownloadBinaries extends Task {
    private static final String MAVEN_REPO = "https://repo.maven.apache.org/maven2/";

    private static final Pattern URL_PATTERN = Pattern.compile("((https?://|file:)\\S*[^/\\s]+)\\s+(\\S+)$");

    private File cache;
    /**
     * Location of per-user cache of already downloaded binaries.
     * Optional; no cache will be used if unset.
     * The directory will be created if it does not yet exist.
     */
    public void setCache(File cache) {
        this.cache = cache;
    }

    private String server;
    /**
     * URL prefix for the server repository.
     * Should generally include a trailing slash.
     * You may include multiple server URLs separated by spaces
     * in which case they will be tried in order.
     * To use a local repository, simply specify e.g. <code>file:/repo/</code> as the URL.
     */
    public void setServer(String server) {
        this.server = server;
    }

    private String repos = MAVEN_REPO;


    /**
     * Space separated URL prefixes for maven repositories.
     * Should generally include a trailing slash.
     * You may include multiple URLs separated by spaces
     * in which case they will be tried in order.
     */
    public void setRepos(String repos) {
        this.repos = repos;
    }

    private final List<FileSet> manifests = new ArrayList<>();
    /**
     * Add one or more manifests of files to download.
     * Each manifest is a text file; lines beginning with # are ignored.
     * All other lines must be entries of the form
     * <pre>
     * 0123456789ABCDEF something-1.0.jar
     * </pre>
     * consisting of an SHA-1 hash followed by a filename.
     * The filename is relative to the manifest, usually a simple basename.
     * If the file exists and has the specified hash, nothing is done.
     * If it has the wrong hash, the task aborts with an error message.
     * If it is missing, it is downloaded from the server (or copied from cache)
     * using a filename derived from the basename of the file in the manifest and its hash.
     * For example, the above line with a server of <code>http://nowhere.net/repo/</code>
     * would try to download
     * <pre>
     * http://nowhere.net/repo/0123456789ABCDEF-something-1.0.jar
     * </pre>
     * Any version number etc. in the filename is purely informational;
     * the "up to date" check is entirely based on the hash.
     */
    public void addManifest(FileSet manifest) {
        manifests.add(manifest);
    }

    private boolean clean;
    /**
     * If true, rather than creating binary files, will delete them.
     * Any cache is ignored in this case.
     * If a binary does not match its hash, the build is aborted:
     * the file might be a precious customized version and should not be blindly deleted.
     */
    public void setClean(boolean clean) {
        this.clean = clean;
    }

    @Override
    public void execute() throws BuildException {
        boolean success = true;
        for (FileSet fs : manifests) {
            DirectoryScanner scanner = fs.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String include : scanner.getIncludedFiles()) {
                File manifest = new File(basedir, include);
                log("Scanning: " + manifest, Project.MSG_VERBOSE);
                try {
                    try (InputStream is = new FileInputStream(manifest)) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line;
                        while ((line = r.readLine()) != null) {
                            if (line.startsWith("#")) {
                                continue;
                            }
                            if (line.trim().length() == 0) {
                                continue;
                            }
                            String[] hashAndFile = line.split(" ", 2);
                            if (hashAndFile.length < 2) {
                                throw new BuildException("Bad line '" + line + "' in " + manifest, getLocation());
                            }

                            Matcher urlMatcher = URL_PATTERN.matcher(hashAndFile[1]);
                            if (MavenCoordinate.isMavenFile(hashAndFile[1])) {
                                MavenCoordinate mc = MavenCoordinate.fromGradleFormat(hashAndFile[1]);
                                success &= fillInFile(hashAndFile[0], mc.toArtifactFilename(), manifest, () -> mavenFile(mc));
                            } else if (urlMatcher.matches()) {
                                success &= fillInFile(hashAndFile[0], urlMatcher.group(3), manifest, () -> downloadFromServer(this, new URL(urlMatcher.group(1))));
                            } else {
                                success &= fillInFile(hashAndFile[0], hashAndFile[1], manifest, () -> legacyDownload(hashAndFile[0] + "-" + hashAndFile[1]));
                            }
                        }
                    }
                } catch (IOException x) {
                    throw new BuildException("Could not open " + manifest + ": " + x, x, getLocation());
                }
            }
        }
        if(! success) {
            throw new BuildException("Failed to download binaries - see log message for the detailed reasons.", getLocation());
        }
    }

    public static InputStream downloadMaven(Task task, URI u) throws IOException {
        MavenCoordinate mc = MavenCoordinate.fromM2Url(u);
        byte[] arr = downloadMavenFile(task, mc, MAVEN_REPO);
        return new ByteArrayInputStream(arr);
    }

    private byte[] mavenFile(MavenCoordinate mc) throws IOException {
        try {
            return downloadMavenFile(this, mc, repos.split(" "));
        } catch (BuildException ex) {
            throw new BuildException(ex.getMessage() + " from maven and " + server, null, getLocation());
        }
    }

    private static byte[] downloadMavenFile(Task task, MavenCoordinate mc, String... m2Repositories) throws IOException {
        String cacheName = mc.toMavenPath();
        List<String> urls = new ArrayList<>();
        for (String prefix : m2Repositories) {
            urls.add(prefix + cacheName);
        }
        for (String url : urls) {
            try {
                if (url.startsWith("file:")) {
                    File file = new File(new URI(url));
                    if (!file.exists()) {
                        continue;
                    }
                }
                URL u = new URL(url);
                task.getProject().log("Trying: " + url);
                return downloadFromServer(task, u);
            } catch (IOException | URISyntaxException ex) {
                //Try the next URL
            }
        }
        throw new BuildException("Could not download " + cacheName);
    }

    private boolean fillInFile(String expectedHash, String baseName, File manifest, Downloader download) throws BuildException {
        File f = new File(manifest.getParentFile(), baseName);
        if (!clean) {
            if (!f.exists() || !hash(f).equals(expectedHash)) {
                log("Creating " + f);
                String cacheName = expectedHash + "-" + baseName;
                if (cache != null) {
                    cache.mkdirs();
                    File cacheFile = new File(cache, cacheName);
                    if (!cacheFile.exists()) {
                        doDownload(cacheName, cacheFile, expectedHash, download);
                    }
                    if (f.isFile() && !f.delete()) {
                        throw new BuildException("Could not delete " + f);
                    }
                    try {
                        FileUtils.getFileUtils().copyFile(cacheFile, f);
                    } catch (IOException x) {
                        throw new BuildException("Could not copy " + cacheFile + " to " + f + ": " + x, x, getLocation());
                    }
                } else {
                    doDownload(cacheName, f, expectedHash, download);
                }
            }
            if(! f.exists()) {
                return false;
            }
            String actualHash = hash(f);
            if (!actualHash.equals(expectedHash)) {
                log("File " + f + " requested by " + manifest + " to have hash " +
                        expectedHash + " actually had hash " + actualHash, Project.MSG_WARN);
                return false;
            }
            log("Have " + f + " with expected hash", Project.MSG_VERBOSE);
            return true;
        } else {
            if (f.exists()) {
                String actualHash = hash(f);
                if (!actualHash.equals(expectedHash)) {
                    log("File " + f + " requested by " + manifest + " to have hash " +
                            expectedHash + " actually had hash " + actualHash, Project.MSG_WARN);
                    return false;
                }
                log("Deleting " + f);
                f.delete();
            }
            return true;
        }
    }

    private boolean doDownload(String cacheName, File destination, String expectedHash, Downloader download) {
        try {
            byte[] downloaded = download.download();

            if (expectedHash != null) {
                String actualHash = hash(new ByteArrayInputStream(downloaded));
                if (!expectedHash.equals(actualHash)) {
                    this.log("Download of " + cacheName + " produced content with hash "
                        + actualHash + " when " + expectedHash + " was expected",
                        Project.MSG_WARN);
                    return false;
                }
            }
            OutputStream os = new FileOutputStream(destination);
            try {
                os.write(downloaded);
            } catch (IOException x) {
                os.close();
                destination.delete();
                throw x;
            }
            os.close();
            return true;
        } catch (IOException | RuntimeException x) {
            String msg = "Could not download " + cacheName + " to " + destination + ": " + x;
            log(msg, Project.MSG_WARN);
            return false;
        }
    }

    private byte[] legacyDownload(String cacheName) throws IOException {
        if (server == null) {
            throw new BuildException("Must specify a server to download files from", getLocation());
        }
        Throwable firstProblem = null;
        for (String prefix : server.split(" ")) {
            try {
                URL url = new URL(prefix + cacheName);
                log("Trying: " + url, Project.MSG_VERBOSE);
                return downloadFromServer(this, url);
            } catch (IOException ex) {
                // Saves the exception and tries the next URL, if present
                firstProblem = ex;
            }
        }
        throw new BuildException("Could not download " + cacheName + " from " + server + ": " + firstProblem, firstProblem, getLocation());
    }


    private static byte[] downloadFromServer(Task task, URL url) throws IOException {
        task.getProject().log("Downloading: " + url);
        URLConnection conn = ConfigureProxy.openConnection(task, url, null);
        int code = HttpURLConnection.HTTP_OK;
        if (conn instanceof HttpURLConnection) {
            code = ((HttpURLConnection) conn).getResponseCode();
        }
        if (code != HttpURLConnection.HTTP_OK) {
            throw new IOException("Skipping download from " + url + " due to response code " + code);
        }
        try {
            try (InputStream is = conn.getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                return baos.toByteArray();
            }
        } catch (IOException ex) {
            throw new IOException("Cannot download: " + url + " due to: " + ex, ex);
        }
    }

    interface Downloader {
        public byte[] download() throws IOException;
    }

    private String hash(File f) {
        try {
            try (FileInputStream is = new FileInputStream(f)) {
                return hash(is);
            }
        } catch (IOException x) {
            throw new BuildException("Could not get hash for " + f + ": " + x, x, getLocation());
        }
    }

    private String hash(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException x) {
            throw new BuildException(x, getLocation());
        }
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) {
            digest.update(buf, 0, r);
        }
        return String.format("%040X", new BigInteger(1, digest.digest()));
    }

}

/*

Sample upload script (edit repository location as needed):

#!/usr/bin/env ruby
repository = '/tmp/repository'
require 'cgi'
require 'digest/sha1'
require 'date'
cgi = CGI.new
begin
  if cgi.request_method == 'POST'
    value = cgi['file']
    content = value.read
    name = value.original_filename.gsub(/\.\.|[^a-zA-Z0-9._+-]/, '_')
    sha1 = Digest::SHA1.hexdigest(content).upcase
    open("#{repository}/#{sha1}-#{name}", "w") do |f|
      f.write content
    end
    open("#{repository}/log", "a") do |f|
      f << "#{DateTime.now.to_s} #{sha1}-#{name} #{cgi.remote_user}\n"
    end
    cgi.out do <<RESPONSE
<html>
<head>
<title>Uploaded #{name}</title>
</head>
<body>
<p>Uploaded. Add to your manifest:</p>
<pre>#{sha1} #{name}</pre>
</body>
</html>
RESPONSE
    end
  else
    cgi.out do <<FORM
<html>
<head>
<title>Upload a Binary</title>
</head>
<body>
<form method="POST" action="" enctype="multipart/form-data">
<input type="file" name="file">
<input type="submit" value="Upload">
</form>
</body>
</html>
FORM
    end
  end
rescue
  cgi.out do
    "Caught an exception: #{$!}"
  end
end

 */
