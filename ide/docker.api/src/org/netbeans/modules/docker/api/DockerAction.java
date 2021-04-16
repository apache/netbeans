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
package org.netbeans.modules.docker.api;

import org.netbeans.modules.docker.StreamItem;
import org.netbeans.modules.docker.ConnectionListener;
import org.netbeans.modules.docker.DockerRemoteException;
import org.netbeans.modules.docker.FolderUploader;
import org.netbeans.modules.docker.MuxedStreamResult;
import org.netbeans.modules.docker.ChunkedInputStream;
import org.netbeans.modules.docker.tls.ContextProvider;
import org.netbeans.modules.docker.IgnoreFileFilter;
import org.netbeans.modules.docker.HttpUtils;
import org.netbeans.modules.docker.DirectStreamResult;
import org.netbeans.modules.docker.Demuxer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.swing.SwingUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.docker.DockerActionAccessor;
import org.netbeans.modules.docker.DockerConfig;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.Endpoint;
import org.netbeans.modules.docker.StreamResult;
import static org.netbeans.modules.docker.api.DockerEntityType.Container;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.io.NullInputStream;
import org.openide.util.io.NullOutputStream;

/**
 *
 * @author Petr Hejl
 */
public class DockerAction {

    public static final String DOCKER_FILE = "Dockerfile"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(DockerAction.class.getName());

    private static final Pattern ID_PATTERN = Pattern.compile(".*([0-9a-f]{12}([0-9a-f]{52})?).*");

    private static final Pattern PORT_PATTERN = Pattern.compile("^(\\d+)/(tcp|udp)$");

    private static final Set<Integer> START_STOP_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_CONTAINER_CODES = new HashSet<>();

    private static final Set<Integer> REMOVE_IMAGE_CODES = new HashSet<>();

    private static final Pair<String, String> ACCEPT_JSON_HEADER = Pair.of("Accept", "application/json");

    static {
        DockerActionAccessor.setDefault(new DockerActionAccessor() {
            @Override
            public void events(DockerAction action, Long since, DockerEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
                action.events(since, listener, connectionListener);
            }
        });

        Collections.addAll(START_STOP_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_MODIFIED);
        Collections.addAll(REMOVE_CONTAINER_CODES, HttpURLConnection.HTTP_NO_CONTENT, HttpURLConnection.HTTP_NOT_FOUND);
        Collections.addAll(REMOVE_IMAGE_CODES, HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_NOT_FOUND);
    }

    private final DockerInstance instance;

    private final boolean emitEvents;

    public DockerAction(DockerInstance instance) {
        this(instance, true);
    }

    // not needed in the api for now
    private DockerAction(DockerInstance instance, boolean emitEvents) {
        this.instance = instance;
        this.emitEvents = emitEvents;
    }

    public List<DockerImage> getImages() {
        try {
            JSONArray value = (JSONArray) doGetRequest("/images/json",
                    Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerImage> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                JSONArray repoTags = (JSONArray) json.get("RepoTags");
                String id = (String) json.get("Id");
                long created = (long) json.get("Created");
                long size = (long) json.get("Size");
                long virtualSize = (long) json.get("VirtualSize");
                ret.add(new DockerImage(instance, repoTags, id, created, size, virtualSize));
            }
            return ret;
        } catch (DockerException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    public List<DockerContainer> getContainers() {
        try {
            JSONArray value = (JSONArray) doGetRequest("/containers/json?all=1",
                    Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerContainer> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                String id = (String) json.get("Id");
                String image = (String) json.get("Image");
                String name = null;
                JSONArray names = (JSONArray) json.get("Names");
                if (names != null && !names.isEmpty()) {
                    name = (String) names.get(0);
                }
                DockerContainer.Status status = DockerUtils.getContainerStatus((String) json.get("Status"));
                ret.add(new DockerContainer(instance, id, image, name, status));
            }
            return ret;
        } catch (DockerException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    public List<DockerRegistryImage> search(String searchTerm) {
        // the api does not allow this TAG and DIGEST separator characters
        if (searchTerm.contains(":") || searchTerm.contains("@")) { // NOI18N
            return Collections.emptyList();
        }

        try {
            JSONArray value = (JSONArray) doGetRequest(
                    "/images/search?term=" + HttpUtils.encodeParameter(searchTerm),
                    Collections.singleton(HttpURLConnection.HTTP_OK));
            List<DockerRegistryImage> ret = new ArrayList<>(value.size());
            for (Object o : value) {
                JSONObject json = (JSONObject) o;
                String name = (String) json.get("name");
                String description = (String) json.get("description");
                long stars = ((Number) getOrDefault(json, "star_count", 0)).longValue();
                boolean official = (boolean) getOrDefault(json, "is_official", false);
                boolean automated = (boolean) getOrDefault(json, "is_automated", false);
                ret.add(new DockerRegistryImage(name, description, stars, official, automated));
            }
            return ret;
        } catch (DockerException | UnsupportedEncodingException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return Collections.emptyList();
    }

    public DockerImage commit(DockerContainer container, String repository, String tag,
            String author, String message, boolean pause) throws DockerException {

        if (repository == null && tag != null) {
            throw new IllegalArgumentException("Repository can't be empty when using tag");
        }

        try {
            StringBuilder action = new StringBuilder("/commit");
            action.append("?");
            action.append("container=").append(container.getId());
            if (repository != null) {
                action.append("&repo=").append(HttpUtils.encodeParameter(repository));
                if (tag != null) {
                    action.append("&tag=").append(HttpUtils.encodeParameter(tag));
                }
            }
            if (author != null) {
                action.append("&author=").append(HttpUtils.encodeParameter(author));
            }
            if (message != null) {
                action.append("&comment=").append(HttpUtils.encodeParameter(message));
            }
            if (!pause) {
                action.append("&pause=0");
            }

            JSONObject value = (JSONObject) doPostRequest(action.toString(),
                    true, Collections.singleton(HttpURLConnection.HTTP_CREATED));

            String id = (String) value.get("Id");

            long time = System.currentTimeMillis() / 1000;
            // XXX we send it as older API does not have the commit event
            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.COMMIT,
                                id, container.getId(), time));
            }

            // FIXME image size and time parameters
            return new DockerImage(instance, Collections.singletonList(DockerUtils.getTag(repository, tag)),
                    (String) value.get("Id"), time, 0, 0);

        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public void rename(DockerContainer container, String name) throws DockerException {
        Parameters.notNull("name", name);

        try {
            doPostRequest("/containers/" + container.getId() + "/rename?name=" + HttpUtils.encodeParameter(name),
                    false, Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

            long time = System.currentTimeMillis() / 1000;
            // XXX we send it as older API does not have the commit event
            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.RENAME,
                                container.getId(), container.getId(), time));
            }
        } catch (UnsupportedEncodingException ex) {
            throw new DockerException(ex);
        }
    }

    public DockerTag tag(DockerTag source, String repository, String tag, boolean force) throws DockerException {
        if (repository == null) {
            throw new IllegalArgumentException("Repository can't be empty");
        }

        StringBuilder action = new StringBuilder("/images/");
        action.append(source.getId());
        action.append("/tag");
        action.append("?");
        action.append("repo=").append(repository);
        if (force) {
            action.append("&force=1");
        }
        if (tag != null) {
            action.append("&tag=").append(tag);
        }

        doPostRequest(action.toString(),
                false, Collections.singleton(HttpURLConnection.HTTP_CREATED));

        String tagResult = DockerUtils.getTag(repository, tag);
        long time = System.currentTimeMillis() / 1000;
        // XXX we send it as older API does not have the commit event
        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.TAG,
                            source.getId(), tagResult, time));
        }

        return new DockerTag(source.getImage(), tagResult);
    }

    public JSONObject getRawDetails(DockerEntityType entityType, String containerId) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest(entityType.getUrlPath() + containerId + "/json",
                Collections.singleton(HttpURLConnection.HTTP_OK));
        return value;
    }
    
    public DockerContainerDetail getDetail(DockerContainer container) throws DockerException {
        JSONObject value = getRawDetails(DockerEntityType.Container, container.getId());
        String name = (String) value.get("Name");
        DockerContainer.Status status = DockerContainer.Status.STOPPED;
        JSONObject state = (JSONObject) value.get("State");
        if (state != null) {
            boolean paused = (Boolean) getOrDefault(state, "Paused", false);
            if (paused) {
                status = DockerContainer.Status.PAUSED;
            } else {
                boolean running = (Boolean) getOrDefault(state, "Running", false);
                if (running) {
                    status = DockerContainer.Status.RUNNING;
                }
            }
        }

        boolean tty = false;
        boolean stdin = false;
        JSONObject config = (JSONObject) value.get("Config");
        if (config != null) {
            tty = (boolean) getOrDefault(config, "Tty", false);
            stdin = (boolean) getOrDefault(config, "OpenStdin", false);
        }
        JSONObject ports = (JSONObject) ((JSONObject) value.get("NetworkSettings")).get("Ports");
        if (ports == null || ports.isEmpty()) {
            return new DockerContainerDetail(name, status, stdin, tty);
        } else {
            List<PortMapping> portMapping = new ArrayList<>();
            for (String containerPortData : (Set<String>) ports.keySet()) {
                JSONArray hostPortsArray = (JSONArray) ports.get(containerPortData);
                if (hostPortsArray != null && !hostPortsArray.isEmpty()) {
                    Matcher m = PORT_PATTERN.matcher(containerPortData);
                    if (m.matches()) {
                        int containerPort = Integer.parseInt(m.group(1));
                        String type = m.group(2).toUpperCase(Locale.ENGLISH);
                        int hostPort = Integer.parseInt((String) ((JSONObject) hostPortsArray.get(0)).get("HostPort"));
                        String hostIp = (String) ((JSONObject) hostPortsArray.get(0)).get("HostIp");
                        portMapping.add(new PortMapping(ExposedPort.Type.valueOf(type), containerPort, hostPort, hostIp));
                    } else {
                        LOGGER.log(Level.FINE, "Unparsable port: {0}", containerPortData);
                    }
                }
            }
            return new DockerContainerDetail(name, status, stdin, tty, portMapping);
        }
    }

    public DockerImageDetail getDetail(DockerImage image) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest("/images/" + image.getId() + "/json",
                Collections.singleton(HttpURLConnection.HTTP_OK));
        List<ExposedPort> ports = new LinkedList<>();
        JSONObject config = (JSONObject) value.get("Config");
        if (config != null) {
            JSONObject portsObject = (JSONObject) config.get("ExposedPorts");
            if (portsObject != null) {
                for (Object k : portsObject.keySet()) {
                    String portStr = (String) k;
                    Matcher m = PORT_PATTERN.matcher(portStr);
                    if (m.matches()) {
                        int port = Integer.parseInt(m.group(1));
                        ExposedPort.Type type = ExposedPort.Type.valueOf(m.group(2).toUpperCase(Locale.ENGLISH));
                        ports.add(new ExposedPort(port, type));
                    } else {
                        LOGGER.log(Level.FINE, "Unparsable port: {0}", portStr);
                    }
                }
            }
        }
        return new DockerImageDetail(ports);
    }

    public DockerfileDetail getDetail(FileObject dockerfile) throws IOException {
        // Each ARG line looks like:
        // "(\w)*ARG(\w)*key=val(\w)*"

        // Filter this lines and remove ARG from the beginning
        List<String> argLines = dockerfile.asLines().stream()
                .filter((line) -> line.trim().matches("^(?i)arg(.*)$")) // NOI18N
                .map((argLine) -> argLine.trim().replaceFirst("^(?i)arg", "").trim()) // NOI18N
                .collect(Collectors.toList());

        // Now each line looks like: "key=val"
        Map<String, String> pairs = new HashMap<>();
        for (String line : argLines) {
            String[] split = line.split("=", 2); // NOI18N
            pairs.put(split[0], split.length == 2 ? split[1] : ""); //NOI18N
        }

        return new DockerfileDetail(pairs);
    }

    public void start(DockerContainer container) throws DockerException {
        doPostRequest("/containers/" + container.getId() + "/start", false, START_STOP_CONTAINER_CODES);

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.START,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void stop(DockerContainer container) throws DockerException {
        doPostRequest("/containers/" + container.getId() + "/stop", false, START_STOP_CONTAINER_CODES);

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.DIE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void pause(DockerContainer container) throws DockerException {
        doPostRequest("/containers/" + container.getId() + "/pause", false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.PAUSE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void unpause(DockerContainer container) throws DockerException {
        doPostRequest("/containers/" + container.getId() + "/unpause", false,
                Collections.singleton(HttpURLConnection.HTTP_NO_CONTENT));

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.UNPAUSE,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void remove(DockerTag tag) throws DockerException {
        String id = getImage(tag);
        doDeleteRequest("/images/" + id, true, REMOVE_IMAGE_CODES);

        // XXX to be precise we should emit DELETE event if we
        // delete the last image, but for our purpose this is enough
        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.UNTAG,
                            tag.getId(), null, System.currentTimeMillis() / 1000));
        }
    }

    public void remove(DockerContainer container) throws DockerException {
        doDeleteRequest("/containers/" + container.getId(), false,
                REMOVE_CONTAINER_CODES);

        if (emitEvents) {
            instance.getEventBus().sendEvent(
                    new DockerEvent(instance, DockerEvent.Status.DESTROY,
                            container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
        }
    }

    public void resizeTerminal(DockerContainer container, int rows, int columns) throws DockerException {
        // formally there should be restart so changes take place
        doPostRequest("/containers/" + container.getId() + "/resize?h=" + rows + "&w=" + columns,
                false, Collections.singleton(HttpURLConnection.HTTP_OK));
    }

    // this call is BLOCKING
    public ActionStreamResult attach(DockerContainer container, boolean stdin, boolean logs) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        DockerContainerDetail info = DockerAction.this.getDetail(container);
        Endpoint s = null;
        try {
            s = createEndpoint();

            OutputStream os = s.getOutputStream();
            os.write(("POST /containers/" + container.getId()
                    + "/attach?logs=" + (logs ? 1 : 0)
                    + "&stream=1&stdout=1&stdin=" + (stdin ? 1 : 0)
                    + "&stderr=1 HTTP/1.1\r\n").getBytes("ISO-8859-1"));
            HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                    getHostHeader(),
                    Pair.of("Connection", "Upgrade"),
                    Pair.of("Upgrade", "tcp"));
            os.write("\r\n".getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            int responseCode = response.getCode();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(responseCode, error != null ? error : response.getMessage());
            }

            if (emitEvents) {
                instance.getEventBus().sendEvent(
                        new DockerEvent(instance, DockerEvent.Status.ATTACH,
                                container.getId(), container.getImage(), System.currentTimeMillis() / 1000));
            }

            Charset ch = HttpUtils.getCharset(response);
            Integer length = HttpUtils.getLength(response.getHeaders());
            if (length != null && length <= 0) {
                closeEndpoint(s);
                return new ActionStreamResult(new EmptyStreamResult(info.isTty()));
            }
            is = HttpUtils.getResponseStream(is, response, true);

            if (info.isTty()) {
                return new ActionStreamResult(new DirectStreamResult(s, ch, is));
            } else {
                return new ActionStreamResult(new MuxedStreamResult(s, ch, is));
            }
        } catch (MalformedURLException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (IOException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (DockerException e) {
            closeEndpoint(s);
            throw e;
        }
    }

    // this call is BLOCKING
    public void pull(String imageName, StatusEvent.Listener listener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            DockerName parsed = DockerName.parse(imageName);
            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("POST /images/create?fromImage="
                        + HttpUtils.encodeParameter(imageName) + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
                Pair<String, String> authHeader = null;
                JSONObject auth = createAuthObject(CredentialsManager.getDefault().getCredentials(parsed.getRegistry()));
                authHeader = Pair.of("X-Registry-Auth", HttpUtils.encodeBase64(auth.toJSONString()));
                HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                        getHostHeader(), ACCEPT_JSON_HEADER, authHeader);
                os.write(("\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                is = HttpUtils.getResponseStream(is, response, false);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readContent(is, response);
                    throw codeToException(responseCode,
                            error != null ? error : response.getMessage());
                }

                String authFailure = null;
                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        StatusEvent e = parseStatusEvent(o);
                        if (e != null) {
                            if (authFailure == null) {
                                authFailure = getAuthenticationFailure(e);
                            }
                            listener.onEvent(e);
                        }
                        parser.reset();
                    }
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }

                if (authFailure != null) {
                    throw new DockerAuthenticationException(authFailure);
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    // this call is BLOCKING
    public void push(DockerTag tag, StatusEvent.Listener listener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            String name = tag.getTag();
            DockerName parsed = DockerName.parse(name);
            String tagString = parsed.getTag();
            StringBuilder action = new StringBuilder();
            action.append("/images/");
            if (tagString == null) {
                action.append(name);
            } else {
                action.append(name.substring(0, name.length() - tagString.length() - 1));
            }
            action.append("/push");
            if (tagString != null) {
                action.append("?tag=").append(HttpUtils.encodeParameter(tagString));
            }

            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("POST " + action.toString() + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
                Pair<String, String> authHeader = null;
                JSONObject auth = createAuthObject(CredentialsManager.getDefault().getCredentials(parsed.getRegistry()));
                authHeader = Pair.of("X-Registry-Auth", HttpUtils.encodeBase64(auth.toJSONString()));
                HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                        getHostHeader(), ACCEPT_JSON_HEADER, authHeader);
                os.write(("\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                is = HttpUtils.getResponseStream(is, response, false);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readContent(is, response);
                    throw codeToException(responseCode,
                            error != null ? error : response.getMessage());
                }

                String authFailure = null;
                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        StatusEvent e = parseStatusEvent(o);
                        if (e != null) {
                            if (authFailure == null) {
                                authFailure = getAuthenticationFailure(e);
                            }
                            listener.onEvent(e);
                        }
                        parser.reset();
                    }
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }

                if (authFailure != null) {
                    throw new DockerAuthenticationException(authFailure);
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    public FutureTask<DockerImage> createBuildTask(@NonNull FileObject buildContext, @NullAllowed FileObject dockerfile,
            @NullAllowed Map<String, String> buildargs,
            String repository, String tag, boolean pull, boolean noCache,
            final BuildEvent.Listener buildListener, final StatusEvent.Listener statusListener) {

        final CancelHandler handler = new CancelHandler();
        Callable<DockerImage> callable = new Callable<DockerImage>() {

            @Override
            public DockerImage call() throws Exception {
                assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

                if (!buildContext.isFolder()) {
                    throw new IllegalArgumentException("Build context has to be a directory");
                }
                if (dockerfile != null && !dockerfile.isData()) {
                    throw new IllegalArgumentException("Dockerfile has to be a file");
                }
                if (repository == null && tag != null) {
                    throw new IllegalArgumentException("Repository can't be empty when using tag");
                }

                String dockerfileName = null;
                if (dockerfile != null) {
                    dockerfileName = dockerfile.getName();
                }

                Endpoint s = null;
                try {
                    s = createEndpoint();
                    synchronized (handler) {
                        if (handler.isCancelled()) {
                            return null;
                        }
                        handler.setEndpoint(s);
                    }

                    StringBuilder request = new StringBuilder();
                    request.append("POST /build?");
                    request.append("pull=").append(pull ? 1 : 0);
                    request.append("&nocache=").append(noCache ? 1 : 0);
                    if (dockerfileName != null) {
                        request.append("&dockerfile=").append(HttpUtils.encodeParameter(dockerfileName));
                    }
                    if (repository != null) {
                        request.append("&t=").append(HttpUtils.encodeParameter(repository));
                        if (tag != null) {
                            request.append(":").append(tag);
                        }
                    }
                    if (buildargs != null && !buildargs.isEmpty()) {
                        request.append("&buildargs=").append((new JSONObject(buildargs)).toString());
                    }
                    request.append(" HTTP/1.1\r\n");

                    JSONObject registryConfig = new JSONObject();
                    JSONObject configs = new JSONObject();
                    registryConfig.put("configs", configs);
                    for (Credentials c : DockerConfig.getDefault().getAllCredentials()) {
                        configs.put(c.getRegistry(), createAuthObject(c));
                    }

                    Pair<String, String> configHeader = null;
                    if (!configs.isEmpty()) {
                        configHeader = Pair.of("X-Registry-Config", HttpUtils.encodeBase64(registryConfig.toJSONString()));
                    }
                    HttpUtils.configureHeaders(request, DockerConfig.getDefault().getHttpHeaders(),
                            configHeader,
                            getHostHeader(),
                            Pair.of("Transfer-Encoding", "chunked"),
                            Pair.of("Content-Type", "application/tar"),
                            Pair.of("Content-Encoding", "gzip"));
                    request.append("\r\n");

                    OutputStream os = s.getOutputStream();
                    os.write(request.toString().getBytes("ISO-8859-1"));
                    os.flush();

                    buildListener.onEvent(new BuildEvent(instance, request.toString(), false, null, false));

                    // FIXME should we allow \ as separator as that would be formally
                    // separator on windows without possibility to escape anything
                    // If we would allow that we have to use File comparison
                    Future<Void> task = new FolderUploader(instance, os).upload(buildContext,
                            new IgnoreFileFilter(buildContext, dockerfile, '/'), new FolderUploader.Listener() {
                        @Override
                        public void onUpload(String path) {
                            buildListener.onEvent(new BuildEvent(instance, path, false, null, true));
                        }
                    });

                    InputStream is = s.getInputStream();
                    HttpUtils.Response response = HttpUtils.readResponse(is);
                    int responseCode = response.getCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        task.cancel(true);
                        String error = HttpUtils.readContent(is, response);
                        throw codeToException(responseCode,
                                error != null ? error : response.getMessage());
                    }

                    try {
                        if (task.isDone()) {
                            task.get();
                        } else {
                            LOGGER.log(Level.INFO, "Server responded OK yet upload has not finished");
                            task.cancel(true);
                        }
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        throw new DockerException(ex.getCause());
                    }

                    is = HttpUtils.getResponseStream(is, response, false);

                    JSONParser parser = new JSONParser();
                    try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
                        String line;
                        String stream = null;
                        while ((line = readEventObject(r)) != null) {
                            JSONObject o = (JSONObject) parser.parse(line);
                            stream = (String) o.get("stream");
                            if (stream != null) {
                                buildListener.onEvent(new BuildEvent(instance, stream.trim(), false, null, false));
                            } else if (o.containsKey("status")) {
                                StatusEvent e = parseStatusEvent(o);
                                if (e != null) {
                                    statusListener.onEvent(e);
                                }
                            } else {
                                String error = (String) o.get("error");
                                if (error != null) {
                                    BuildEvent.Error detail = null;
                                    JSONObject detailObj = (JSONObject) o.get("errorDetail");
                                    if (detailObj != null) {
                                        long code = ((Number) getOrDefault(detailObj, "code", 0)).longValue();
                                        String mesage = (String) detailObj.get("message");
                                        detail = new BuildEvent.Error(code, mesage);
                                    }
                                    buildListener.onEvent(new BuildEvent(instance, error, true, detail, false));
                                } else {
                                    LOGGER.log(Level.INFO, "Unknown event {0}", o);
                                }
                            }
                            parser.reset();
                        }

                        if (stream != null) {
                            Matcher m = ID_PATTERN.matcher(stream.trim());
                            if (m.matches()) {
                                // the docker itself does not emit any event for built image
                                // we assume the last stream contains the built image id
                                // FIXME as there is no BUILD event we use PULL event
                                long time = System.currentTimeMillis() / 1000;
                                if (emitEvents) {
                                    instance.getEventBus().sendEvent(
                                            new DockerEvent(instance, DockerEvent.Status.PULL,
                                                    m.group(1), null, time));
                                }
                                // FIXME image size and time parameters
                                return new DockerImage(instance, Collections.singletonList(DockerUtils.getTag(repository, tag)),
                                        m.group(1), time, 0, 0);
                            }
                        }
                    } catch (ParseException ex) {
                        throw new DockerException(ex);
                    }
                    return null;
                } catch (MalformedURLException e) {
                    closeEndpoint(s);
                    throw new DockerException(e);
                } catch (IOException e) {
                    closeEndpoint(s);
                    if (!handler.isCancelled()) {
                        throw new DockerException(e);
                    }
                    return null;
                } catch (DockerException e) {
                    closeEndpoint(s);
                    throw e;
                }
            }
        };

        return new FutureTask<DockerImage>(callable) {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                super.cancel(false);
                if (mayInterruptIfRunning) {
                    handler.cancel();
                }
                return true;
            }
        };
    }

    // this call is BLOCKING
    public ActionChunkedResult logs(DockerContainer container) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        DockerContainerDetail info = getDetail(container);
        Endpoint s = null;
        try {
            s = createEndpoint();

            OutputStream os = s.getOutputStream();
            os.write(("GET /containers/" + container.getId() + "/logs?stderr=1&stdout=1&timestamps=1&follow=1 HTTP/1.1\r\n").getBytes("ISO-8859-1"));
            HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(), getHostHeader());
            os.write("\r\n".getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            int responseCode = response.getCode();
            if (responseCode != 101 && responseCode != HttpURLConnection.HTTP_OK) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(responseCode,
                        error != null ? error : response.getMessage());
            }

            is = HttpUtils.getResponseStream(is, response, true);

            StreamItem.Fetcher fetcher;
            Integer length = HttpUtils.getLength(response.getHeaders());
            // if there was no log it may return just standard reply with content length 0
            if (length != null && length == 0) {
                assert !(is instanceof ChunkedInputStream);
                LOGGER.log(Level.INFO, "Empty logs");
                fetcher = new StreamItem.Fetcher() {
                    @Override
                    public StreamItem fetch() {
                        return null;
                    }
                };
            } else if (info.isTty()) {
                fetcher = new DirectFetcher(is);
            } else {
                fetcher = new Demuxer(is);
            }
            return new ActionChunkedResult(s, fetcher, HttpUtils.getCharset(response));
        } catch (MalformedURLException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (IOException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (DockerException e) {
            closeEndpoint(s);
            throw e;
        }
    }

    // this call is BLOCKING
    public Pair<DockerContainer, ActionStreamResult> run(String name, JSONObject configuration) throws DockerException {
        Endpoint s = null;
        try {
            s = createEndpoint();

            byte[] data = configuration.toJSONString().getBytes("UTF-8");
            Map<String, String> defaultHeaders = DockerConfig.getDefault().getHttpHeaders();

            OutputStream os = s.getOutputStream();
            os.write(("POST " + (name != null ? "/containers/create?name=" + HttpUtils.encodeParameter(name) : "/containers/create") + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
            HttpUtils.configureHeaders(os, defaultHeaders,
                    getHostHeader(),
                    Pair.of("Content-Type", "application/json"),
                    Pair.of("Content-Length", Integer.toString(data.length)));
            os.write("\r\n".getBytes("ISO-8859-1"));
            os.write(data);
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            if (response.getCode() != HttpURLConnection.HTTP_CREATED) {
                String error = HttpUtils.readContent(is, response);
                throw new DockerRemoteException(response.getCode(),
                        error != null ? error : response.getMessage());
            }

            // we send a second request to the stream later
            InputStream nis = HttpUtils.getResponseStream(is, response, false);

            JSONObject value;
            try {
                JSONParser parser = new JSONParser();
                value = (JSONObject) parser.parse(HttpUtils.readContent(nis, response));
            } catch (ParseException ex) {
                throw new DockerException(ex);
            }

            String id = (String) value.get("Id");
            DockerContainer container = new DockerContainer(
                    instance,
                    id,
                    (String) configuration.get("Image"),
                    "/" + name,
                    DockerContainer.Status.STOPPED);
            ActionStreamResult r = attach(container, true, true);

            os.write(("POST /containers/" + id + "/start HTTP/1.1\r\n").getBytes("ISO-8859-1"));
            HttpUtils.configureHeaders(os, defaultHeaders, getHostHeader());
            os.write("\r\n".getBytes("ISO-8859-1"));
            os.flush();

            response = HttpUtils.readResponse(is);
            if (response.getCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                String error = HttpUtils.readContent(is, response);
                throw codeToException(response.getCode(),
                        error != null ? error : response.getMessage());
            }

            return Pair.of(container, r);
        } catch (MalformedURLException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (IOException e) {
            closeEndpoint(s);
            throw new DockerException(e);
        } catch (DockerException e) {
            closeEndpoint(s);
            throw e;
        }
    }

    public boolean pingWithExceptions() throws Exception {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";
        Endpoint s = createEndpoint();
        try {
            OutputStream os = s.getOutputStream();
            // FIXME should we use default headers ?
            os.write(("GET /_ping HTTP/1.1\r\n"
                    + "Host: " + getHostHeader().second() + "\r\n\r\n").getBytes("ISO-8859-1"));
            os.flush();

            InputStream is = s.getInputStream();
            HttpUtils.Response response = HttpUtils.readResponse(is);
            return response.getCode() == HttpURLConnection.HTTP_OK;
        } finally {
            closeEndpoint(s);
        }
    }
    
    public boolean ping() {      
        try {
            return pingWithExceptions();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return false;
    }

    // this call is BLOCKING
    private void events(Long since, DockerEvent.Listener listener, ConnectionListener connectionListener) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("GET " + (since != null ? "/events?since=" + since : "/events") + " HTTP/1.1\r\n"
                        + "Host: " + getHostHeader().second() + "\r\n"
                        + "Accept: application/json\r\n\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    String error = HttpUtils.readContent(is, response);
                    throw new DockerRemoteException(responseCode,
                            error != null ? error : response.getMessage());
                }

                is = HttpUtils.getResponseStream(is, response, false);

                if (connectionListener != null) {
                    connectionListener.onConnect(s);
                }

                JSONParser parser = new JSONParser();
                try (InputStreamReader r = new InputStreamReader(is, HttpUtils.getCharset(response))) {
                    String line;
                    while ((line = readEventObject(r)) != null) {
                        JSONObject o = (JSONObject) parser.parse(line);
                        DockerEvent.Status status = DockerEvent.Status.parse((String) o.get("status"));
                        String id = (String) o.get("id");
                        String from = (String) o.get("from");
                        long time = (Long) o.get("time");
                        if (status == null) {
                            LOGGER.log(Level.INFO, "Unknown event {0}", o);
                        } else {
                            listener.onEvent(new DockerEvent(instance, status, id, from, time));
                        }
                        parser.reset();
                    }
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                } finally {
                    if (connectionListener != null) {
                        connectionListener.onDisconnect();
                    }
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private Object doGetRequest(@NonNull String action, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("GET " + action + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
                HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                        getHostHeader(), ACCEPT_JSON_HEADER);
                os.write(("\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                if (!okCodes.contains(responseCode)) {
                    String error = HttpUtils.readContent(is, response);
                    throw codeToException(responseCode,
                            error != null ? error : response.getMessage());
                }

                is = HttpUtils.getResponseStream(is, response, false);

                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        is, HttpUtils.getCharset(response)))) {
                    JSONParser parser = new JSONParser();
                    return parser.parse(br);
                } catch (ParseException ex) {
                    throw new DockerException(ex);
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private Object doPostRequest(@NonNull String action, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("POST " + action + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
                HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                        getHostHeader(), Pair.of("Content-Type", "application/json"));
                os.write(("\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                if (!okCodes.contains(responseCode)) {
                    String error = HttpUtils.readContent(is, response);
                    throw codeToException(responseCode,
                            error != null ? error : response.getMessage());
                }

                if (output) {
                    is = HttpUtils.getResponseStream(is, response, false);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            is, HttpUtils.getCharset(response)))) {
                        JSONParser parser = new JSONParser();
                        return parser.parse(br);
                    } catch (ParseException ex) {
                        throw new DockerException(ex);
                    }
                } else {
                    return null;
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private Object doDeleteRequest(@NonNull String action, boolean output, Set<Integer> okCodes) throws DockerException {
        assert !SwingUtilities.isEventDispatchThread() : "Remote access invoked from EDT";

        try {
            Endpoint s = createEndpoint();
            try {
                OutputStream os = s.getOutputStream();
                os.write(("DELETE " + action + " HTTP/1.1\r\n").getBytes("ISO-8859-1"));
                HttpUtils.configureHeaders(os, DockerConfig.getDefault().getHttpHeaders(),
                        getHostHeader(), ACCEPT_JSON_HEADER);
                os.write(("\r\n").getBytes("ISO-8859-1"));
                os.flush();

                InputStream is = s.getInputStream();
                HttpUtils.Response response = HttpUtils.readResponse(is);
                int responseCode = response.getCode();

                if (!okCodes.contains(responseCode)) {
                    String error = HttpUtils.readContent(is, response);
                    throw codeToException(responseCode,
                            error != null ? error : response.getMessage());
                }

                if (output) {
                    is = HttpUtils.getResponseStream(is, response, false);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(
                            is, HttpUtils.getCharset(response)))) {
                        JSONParser parser = new JSONParser();
                        return parser.parse(br);
                    } catch (ParseException ex) {
                        throw new DockerException(ex);
                    }
                } else {
                    return null;
                }
            } finally {
                closeEndpoint(s);
            }
        } catch (MalformedURLException e) {
            throw new DockerException(e);
        } catch (IOException e) {
            throw new DockerException(e);
        }
    }

    private StatusEvent parseStatusEvent(JSONObject o) {
        boolean error = false;
        String id = (String) o.get("id");
        String status = (String) o.get("status");
        if (status == null) {
            status = (String) o.get("error");
            error = status != null;
        }
        if (status == null) {
            LOGGER.log(Level.INFO, "Unknown event {0}", o);
            return null;
        }

        String progress = (String) o.get("progress");
        StatusEvent.Progress detail = null;
        JSONObject detailObj = (JSONObject) o.get("progressDetail");
        if (detailObj != null) {
            long current = ((Number) getOrDefault(detailObj, "current", 1)).longValue();
            long total = ((Number) getOrDefault(detailObj, "total", 1)).longValue();
            detail = new StatusEvent.Progress(current, total);
        }
        return new StatusEvent(instance, id, status, progress, error, detail);
    }

    private Endpoint createEndpoint() throws IOException {
        URL realUrl = getUrl();
        try {
            if ("https".equals(realUrl.getProtocol())) { // NOI18N
                SSLContext context = ContextProvider.getInstance().getSSLContext(instance);
                return Endpoint.forSocket(context.getSocketFactory().createSocket(realUrl.getHost(), realUrl.getPort()));
            } else if ("http".equals(realUrl.getProtocol())) { // NOI18N
                Socket s = new Socket(ProxySelector.getDefault().select(realUrl.toURI()).get(0));
                int port = realUrl.getPort();
                if (port < 0) {
                    port = realUrl.getDefaultPort();
                }
                s.connect(new InetSocketAddress(realUrl.getHost(), port));
                return Endpoint.forSocket(s);
            } else if ("file".equals(realUrl.getProtocol())) {
                AFUNIXSocket s = AFUNIXSocket.newInstance();
                AFUNIXSocketAddress sockAdd = new AFUNIXSocketAddress(new File(realUrl.getFile()));
                s.connect(sockAdd);
                return Endpoint.forSocket(s);
            } else {
                throw new IOException("Unknown protocol: " + realUrl.getProtocol());
            }
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    private Pair<String, String> getHostHeader() throws MalformedURLException {
        URL url = getUrl();
        int port = url.getPort();
        if (port <= 0) {
            return Pair.of("Host", url.getHost()); // NOI18N
        } else {
            return Pair.of("Host", url.getHost() + ":" + port); // NOI18N
        }
    }

    private URL getUrl() throws MalformedURLException {
        String url = instance.getUrl();
        if (url.startsWith("tcp://")) { // NOI18N
            url = "http://" + url.substring(6); // NOI18N
        } else if (url.startsWith("unix://")) { // NOI18N
            url = "file://" + url.substring(7); // NOI18N
        }
        return new URL(url);
    }

    private static JSONObject createAuthObject(Credentials credentials) {
        JSONObject value = new JSONObject();
        if (credentials == null) {
            value.put("auth", ""); // NOI18N
            value.put("email", ""); // NOI18N
        } else {
            value.put("username", credentials.getUsername()); // NOI18N
            value.put("password", new String(credentials.getPassword())); // NOI18N
            value.put("email", credentials.getEmail()); // NOI18N
            value.put("serveraddress", credentials.getRegistry()); // NOI18N
            value.put("auth", ""); // NOI18N
        }
        return value;
    }

    private static String getAuthenticationFailure(StatusEvent e) {
        if (!e.isError()) {
            return null;
        }
        // this is how the docker client handles it
        // (as the server returns HTTP 200 anyway)
        if (e.getMessage().contains("Authentication is required")
                || e.getMessage().contains("Status 401")
                || e.getMessage().contains("401 Unauthorized")
                || e.getMessage().contains("status code 401")) {
            return e.getMessage();
        }
        return null;
    }

    private static DockerException codeToException(int code, String message) {
        if (code == HttpURLConnection.HTTP_CONFLICT) {
            return new DockerConflictException(message);
        } else if (code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            return new DockerAuthenticationException(message);
        }
        return new DockerRemoteException(code, message);
    }

    private static String readEventObject(Reader is) throws IOException {
        StringWriter sw = new StringWriter();
        int b;
        int balance = -1;
        while ((b = is.read()) != -1) {
            if (balance < 0) {
                if (b == '{') {
                    balance = 1;
                    sw.write(b);
                }
                continue;
            }
            if (b == '{') {
                balance++;
            } else if (b == '}') {
                balance--;
            }
            sw.write(b);
            if (balance == 0) {
                return sw.toString();
            }
        }
        return null;
    }

    private static void closeEndpoint(Endpoint s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    private static Object getOrDefault(Map map, Object key, Object def) {
        Object ret = map.get(key);
        if (ret == null) {
            ret = def;
        }
        return ret;
    }

    private static String getImage(DockerTag tag) {
        String id = tag.getTag();
        if (id.equals("<none>:<none>")) { // NOI18N
            id = tag.getImage().getId();
        }
        return id;
    }

    public JSONObject getRunningProcessesList(DockerContainer container) throws DockerException {
        JSONObject value = (JSONObject) doGetRequest(Container.getUrlPath() + container.getId() + "/top",
                Collections.singleton(HttpURLConnection.HTTP_OK));
        return value;
    }

    private static class DirectFetcher implements StreamItem.Fetcher {

        private final InputStream is;

        private final byte[] buffer = new byte[1024];

        public DirectFetcher(InputStream is) {
            this.is = is;
        }

        @Override
        public StreamItem fetch() {
            try {
                int count = is.read(buffer);
                if (count < 0) {
                    return null;
                }
                return new StreamItem(ByteBuffer.wrap(buffer, 0, count), false);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
                return null;
            }
        }

    }

    private static class EmptyStreamResult implements StreamResult {

        private final OutputStream os = new NullOutputStream();

        private final InputStream is = new NullInputStream();

        private final boolean tty;

        public EmptyStreamResult(boolean tty) {
            this.tty = tty;
        }

        @Override
        public OutputStream getStdIn() {
            return os;
        }

        @Override
        public InputStream getStdOut() {
            return is;
        }

        @Override
        public InputStream getStdErr() {
            return null;
        }

        @Override
        public boolean hasTty() {
            return tty;
        }

        @Override
        public Charset getCharset() {
            return Charset.forName("UTF-8");
        }

        @Override
        public void close() throws IOException {
            // noop
        }
    }

    private static class CancelHandler {

        private Endpoint endpoint;

        private boolean cancelled;

        public synchronized void setEndpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        public synchronized void cancel() {
            if (endpoint != null) {
                closeEndpoint(endpoint);
                endpoint = null;
                cancelled = true;
            }
        }

        public synchronized boolean isCancelled() {
            return cancelled;
        }

    }

}
