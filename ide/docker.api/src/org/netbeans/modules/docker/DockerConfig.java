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
package org.netbeans.modules.docker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.docker.api.Credentials;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Petr Hejl
 */
public final class DockerConfig {

    private static final String DOCKER_HUB_DOMAIN = "index.docker.io";

    private static final String DOCKER_HUB_REGISTRY = "https://" + DOCKER_HUB_DOMAIN + "/v1/";

    private static DockerConfig instance;

    private final ConfigListener listener = new ConfigListener();

    // GuardedBy("this")
    private JSONObject auths;

    // GuardedBy("this")
    private Map<String, String> httpHeaders;

    private DockerConfig() {
        super();
    }

    public static synchronized DockerConfig getDefault() {
        if (instance == null) {
            instance = new DockerConfig();
            instance.init();
        }
        return instance;
    }

    public List<Credentials> getAllCredentials() throws IOException {
        JSONObject currentAuths;
        synchronized (this) {
            loadCache();
            currentAuths = auths;
        }

        List<Credentials> ret = new ArrayList<>(currentAuths.size());
        for (Iterator<Map.Entry> it = currentAuths.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = it.next();
            if (!(e.getKey() instanceof String)) {
                continue;
            }
            String registry = (String) e.getKey();
            JSONObject value = (JSONObject) e.getValue();
            if (value == null) {
                continue;
            }
            ret.add(createCredentials(registry, value)); // NOI18N
        }

        return ret;
    }

    public Credentials getCredentials(String registry) throws IOException {
        JSONObject currentAuths;
        synchronized (this) {
            loadCache();
            currentAuths = auths;
        }

        Set<String> names = generateRegistryNames(registry);
        JSONObject value = null;
        Iterator<String> it = names.iterator();
        while (value == null && it.hasNext()) {
            value = (JSONObject) currentAuths.get(it.next());
        }
        if (value == null) {
            return null;
        }

        return createCredentials(registry, value);
    }

    public Map<String, String> getHttpHeaders() throws IOException {
        synchronized (this) {
            loadCache();
            return new HashMap<>(httpHeaders);
        }
    }

    public void setCredentials(Credentials credentials) throws IOException {
        StringBuilder sb = new StringBuilder(credentials.getUsername());
        sb.append(':');
        sb.append(credentials.getPassword());
        String auth = Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));

        JSONObject value = new JSONObject();
        value.put("auth", auth); // NOI18N
        value.put("email", credentials.getEmail()); // NOI18N

        Pair<File, Boolean> fileDesc = getConfigFile();

        synchronized (this) {
            try (RandomAccessFile f = new RandomAccessFile(fileDesc.first(), "rw")) {
                try (FileChannel ch = f.getChannel()) {
                    try (FileLock lock = ch.lock()) {
                        JSONObject current = null;
                        if (f.length() > 0) {
                            current = (JSONObject) new JSONParser().parse(Channels.newReader(ch, "UTF-8"));
                        }
                        if (current == null) {
                            current = new JSONObject();
                        }

                        JSONObject currentAuths = current;
                        if (!fileDesc.second()) {
                            currentAuths = (JSONObject) current.get("auths");
                            if (currentAuths == null) {
                                currentAuths = new JSONObject();
                                current.put("auths", currentAuths);
                            }
                        }
                        currentAuths.put(credentials.getRegistry(), value);
                        ch.truncate(0);

                        Writer w = Channels.newWriter(ch, "UTF-8");
                        current.writeJSONString(w);
                        w.flush();
                    } catch (ParseException ex) {
                        throw new IOException(ex);
                    }
                }
            } finally {
                clearCache();
            }
        }
    }

    public void removeCredentials(Credentials credentials) throws IOException {
        Pair<File, Boolean> fileDesc = getConfigFile();

        synchronized (this) {
            try (RandomAccessFile f = new RandomAccessFile(fileDesc.first(), "rw")) {
                try (FileChannel ch = f.getChannel()) {
                    try (FileLock lock = ch.lock()) {
                        JSONObject current = (JSONObject) new JSONParser().parse(Channels.newReader(ch, "UTF-8"));
                        if (f.length() <= 0) {
                            return;
                        }

                        JSONObject currentAuths = current;
                        if (!fileDesc.second()) {
                            currentAuths = (JSONObject) current.get("auths");
                            if (currentAuths == null) {
                                return;
                            }
                        }
                        currentAuths.remove(credentials.getRegistry());
                        ch.truncate(0);

                        Writer w = Channels.newWriter(ch, "UTF-8");
                        current.writeJSONString(w);
                        w.flush();
                    } catch (ParseException ex) {
                        throw new IOException(ex);
                    }
                }
            } finally {
                clearCache();
            }
        }
    }

    private void init() {
        FileUtil.addFileChangeListener(listener, getNewConfigFile());
        FileUtil.addFileChangeListener(listener, getOldConfigFile());
    }

    private void loadCache() throws IOException {
        synchronized (this) {
            if (auths == null || httpHeaders == null) {
                Pair<JSONObject, Boolean> parsed = parse();
                if (parsed == null) {
                    auths = new JSONObject();
                    httpHeaders = new JSONObject();
                    return;
                }

                JSONObject currentAuths;
                currentAuths = parsed.first();
                if (!parsed.second()) {
                    // using the new config.json
                    currentAuths = (JSONObject) currentAuths.get("auths"); // NOI18N
                }
                if (currentAuths == null) {
                    currentAuths = new JSONObject();
                }
                auths = currentAuths;

                JSONObject currentHeaders = null;
                if (!parsed.second()) {
                    currentHeaders = (JSONObject) parsed.first().get("HttpHeaders"); // NOI18N
                }
                if (currentHeaders == null) {
                    currentHeaders = new JSONObject();
                }
                httpHeaders = new HashMap<>();
                for (Iterator<Map.Entry> it = currentHeaders.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry e = it.next();
                    httpHeaders.put((String) e.getKey(), (String) e.getValue());
                }
            }
        }
    }

    private void clearCache() {
        synchronized (this) {
            auths = null;
            httpHeaders = null;
        }
    }

    private Pair<JSONObject, Boolean> parse() throws IOException {
        Pair<File, Boolean> fileDesc = getConfigFile();
        synchronized (this) {
            if (fileDesc.first().isFile()) {
                try (FileInputStream is = new FileInputStream(fileDesc.first())) {
                    try (FileLock lock = is.getChannel().lock(0, Long.MAX_VALUE, true)) {
                        Reader r = new InputStreamReader(new BufferedInputStream(is), StandardCharsets.UTF_8);
                        JSONObject current = null;
                        if (fileDesc.first().length() > 0) {
                            current = (JSONObject) new JSONParser().parse(r);
                        }
                        if (current == null) {
                            return null;
                        }
                        return Pair.of(current, fileDesc.second());
                    } catch (ParseException ex) {
                        throw new IOException(ex);
                    }
                }
            }
            return null;
        }
    }

    private static Credentials createCredentials(String registry, JSONObject value) throws IOException {
        if (value == null) {
            return null;
        }

        byte[] auth = Base64.getDecoder().decode((String) value.get("auth")); // NOI18N
        CharBuffer chars = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(auth));
        int index = -1;
        for (int i = 0; i < chars.length(); i++) {
            if (chars.get(i) == ':') {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IOException("Malformed registry authentication record");
        }
        String username = new String(chars.array(), 0, index);
        char[] password = new char[chars.length() - index - 1];
        if (password.length > 0) {
            System.arraycopy(chars.array(), index + 1, password, 0, password.length);
        }
        return new Credentials(registry, username, password, (String) value.get("email")); // NOI18N
    }

    private static Pair<File, Boolean> getConfigFile() {
        File newFile = getNewConfigFile();
        boolean oldConfig = false;
        File file = newFile;
        if (!file.isFile()) {
            file = getOldConfigFile();
            oldConfig = true;
        }
        if (!file.isFile()) {
            // what to do now ?
            // FIXME test the client version; since 1.7 use the new format
            file = newFile;
            oldConfig = false;
        }
        return Pair.of(file, oldConfig);
    }

    private static File getNewConfigFile() {
        String configPath = System.getenv("DOCKER_CONFIG");
        if (configPath == null) {
            configPath = System.getProperty("user.home") + File.separatorChar + ".docker";
        }

        return new File(configPath, "config.json");
    }

    private static File getOldConfigFile() {
        return new File(System.getProperty("user.home"), ".dockercfg");
    }

    private static Set<String> generateRegistryNames(String registry) {
        Set<String> result = new LinkedHashSet<>();

        if (registry == null) {
            result.add(DOCKER_HUB_REGISTRY);
            result.addAll(generateRegistryNames(DOCKER_HUB_DOMAIN));
            return result;
        }

        result.add(registry);
        generatePaths(registry, result);

        if (!registry.contains("://")) {
            String https = "https://" + registry;
            result.add(https);
            generatePaths(https, result);
            String http = "http://" + registry;
            result.add(http);
            generatePaths(http, result);
        }
        return result;
    }

    private static void generatePaths(String registry, Set<String> result) {
        StringBuilder extended = new StringBuilder(registry);
        if (registry.endsWith("/")) {
            extended.append("v1");
        } else {
            extended.append("/v1");
        }
        result.add(extended.toString());
        extended.append("/");
        result.add(extended.toString());
    }

    private class ConfigListener implements FileChangeListener {

        @Override
        public void fileDataCreated(FileEvent fe) {
            clearCache();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            clearCache();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            clearCache();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            clearCache();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            // noop
        }
    }
}
