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
package org.netbeans.modules.cloud.oracle.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.compute.ComputeInstanceItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public final class CloudAssets {

    private static final String SUGGESTED = "Suggested"; //NOI18N
    private static final String CLOUD_ASSETS_PATH = "CloudAssets"; //NOI18N
    private static final String CLOUD_ASSETS_FILE = "default.json"; //NOI18N
    private static CloudAssets instance = null;

    private boolean assetsLoaded = false;
    private Set<OCIItem> items = new HashSet<>();
    private Set<SuggestedItem> suggested = new HashSet<>();

    private final ChangeSupport changeSupport;
    private final Gson gson;

    CloudAssets() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(OCID.class, new OCIDDeserializer())
                .create();
        changeSupport = new ChangeSupport(this);
        ConnectionManager.getDefault().addConnectionListener(() -> {
            DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
            Set<String> ocids = new HashSet<>();
            for (int i = 0; i < connections.length; i++) {
                String ocid = (String) connections[i].getConnectionProperties().get("OCID"); //NOI18N
                if (ocid != null) {
                    ocids.add(ocid); 
                }
            }
            boolean update = false;
            for (Iterator it = items.iterator(); it.hasNext();) {
                OCIItem item = (OCIItem) it.next();
                if (!ocids.contains(item.getKey().getValue()) && "Databases".equals(item.getKey().getPath())) { //NOI18N
                    it.remove();
                    update = true;
                }
            }
            if (update) {
                update();
            }
        });
    }

    public static synchronized CloudAssets getDefault() {
        if (instance == null) {
            instance = new CloudAssets();
            instance.loadAssets();
        }
        return instance;
    }

    public void addItem(OCIItem newItem) {
        items.add(newItem);
        update();
        storeAssets();
    }

    void removeItem(OCIItem item) {
        if (items.remove(item)) {
            update();
            storeAssets();
        }
    }

    public void update() {
        OpenProjectsFinder.getDefault().findOpenProjects().thenAccept(projects -> {
            SuggestionAnalyzer analyzer = new DependenciesAnalyzer();
            Set<SuggestedItem> suggested = analyzer.findSuggestions(projects);
            setSuggestions(suggested);
        });
    }

    private synchronized void setSuggestions(Set<SuggestedItem> newSuggested) {
//        for (OCIItem item : new HashSet<OCIItem>(items)) {
//            if (item.getKey().getPath().equals(SUGGESTED)) { //NOI18N
//                items.remove(item);
//            }
//        }
        suggested.clear();
        Set<String> present = items.stream()
                .map(i -> i.getKey().getPath())
                .collect(Collectors.toSet());
        for (SuggestedItem s : newSuggested) {
            if (!present.contains(s.getPath())) {
                boolean add = true;
                for (String exclusivePath : s.getExclusivePaths()) {
                    if (present.contains(exclusivePath)) {
                        add = false;
                        continue;
                    }
                }
                if (add) {
                    suggested.add(s);
                }
            }
        }
        changeSupport.fireChange();
    }

    public List<OCIItem> getItems() {
        List<OCIItem> list = new ArrayList<>(suggested);
        list.addAll(items);
//        Collections.sort(list, (a, b) -> {
//            if (SUGGESTED.equals(a.getKey().getPath())) {
//                return Integer.MIN_VALUE;
//            }
//            if (SUGGESTED.equals(b.getKey().getPath())) {
//                return Integer.MAX_VALUE;
//            }
//            return a.getKey().getPath().compareTo(b.getKey().getPath());
//        });
        return list;
    }

    /**
     * Adds a <code>ChangeListener</code> to the listener list.
     *
     * @param listener the <code>ChangeListener</code> to be added.
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    /**
     * Removes a <code>ChangeListener</code> from the listener list.
     *
     * @param listener the <code>ChangeListener</code> to be removed.
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    synchronized void storeAssets() {
        if (!assetsLoaded) {
            return;
        }
        Set<OCIItem> toStore = items.stream()
                .filter(i -> !SUGGESTED.equals(i.getKey().getPath())) //NOI18N
                .collect(Collectors.toSet());
        FileObject file = null;
        try {
            FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), CLOUD_ASSETS_PATH);
            file = fo.getFileObject(CLOUD_ASSETS_FILE);
            if (file == null) {
                file = fo.createData(CLOUD_ASSETS_FILE);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (file != null) {
            try (FileLock lock = file.lock()) {
                    OutputStream os = file.getOutputStream(lock);
                    os.write(gson.toJson(toStore).getBytes());
                    os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    synchronized void loadAssets() {
        String content;
        try {
            FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), CLOUD_ASSETS_PATH);
            FileObject file = fo.getFileObject(CLOUD_ASSETS_FILE);
            if (file == null) {
                return;
            }
            content = new String(file.asBytes());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        } finally {
            assetsLoaded = true;
        }

        try (JsonReader reader = new JsonReader(new StringReader(content))) {
            Set<OCIItem> loaded = new HashSet<>();
            reader.beginArray();
            while (reader.hasNext()) {
                JsonElement element = JsonParser.parseReader(reader);
                String path = element.getAsJsonObject()
                        .get("id").getAsJsonObject() //NOI18N
                        .get("path").getAsString(); //NOI18N
                switch (path) {
                    case "Databases": //NOI18N
                        loaded.add(gson.fromJson(element, DatabaseItem.class)); 
                        break;
                    case "Bucket": //NOI18N
                        loaded.add(gson.fromJson(element, BucketItem.class)); 
                        break;
                    case "Cluster": //NOI18N
                        loaded.add(gson.fromJson(element, ClusterItem.class)); 
                        break;
                    case "ComputeInstance": //NOI18N
                        loaded.add(gson.fromJson(element, ComputeInstanceItem.class)); 
                        break;
                    case "Vault": //NOI18N
                        loaded.add(gson.fromJson(element, VaultItem.class)); 
                        break;
                }
            }
            reader.endArray();
            items = loaded;
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            Exceptions.printStackTrace(e);
        } finally {
            assetsLoaded = true;
        }
    }

    private static final class OCIDDeserializer implements JsonDeserializer<OCID> {

        @Override
        public OCID deserialize(JsonElement json, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String id = jsonObject.get("value").getAsString(); //NOI18N
            String path = jsonObject.get("path").getAsString(); //NOI18N
            return OCID.of(id, path);
        }

    }
}
