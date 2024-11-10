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
import com.google.gson.stream.JsonWriter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import static org.netbeans.modules.cloud.oracle.NotificationUtils.showWarningMessage;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.compute.ClusterItem;
import org.netbeans.modules.cloud.oracle.compute.ComputeInstanceItem;
import org.netbeans.modules.cloud.oracle.database.DatabaseItem;
import org.netbeans.modules.cloud.oracle.developer.ContainerRepositoryItem;
import org.netbeans.modules.cloud.oracle.developer.MetricsNamespaceItem;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Maintains a set of OCI resources that are either assigned or recommended for
 * assignment to a workspace.
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "MSG_TenancyNotCompatible=Tenancy must be the same accross all selected Cloud Assets",
    "MSG_AddItemsAgain=Missing information. Please remove than add following items again: {0}",  
})
public final class CloudAssets {

    private static final Logger LOG = Logger.getLogger(CloudAssets.class.getName());

    private static final String SUGGESTED = "Suggested"; //NOI18N
    private static final String CLOUD_ASSETS_PATH = "CloudAssets"; //NOI18N
    private static final String CLOUD_ASSETS_FILE = "default.json"; //NOI18N
    private static CloudAssets instance = null;

    private boolean assetsLoaded = false;
    private Set<OCIItem> items = new HashSet<>();
    private final Set<SuggestedItem> suggested = new HashSet<>();

    private final Map<OCIItem, String> refNames = new HashMap<>();

    private final ChangeSupport changeSupport;
    private final Gson gson;
    
    private final ItemsChangeListener itemsListener = new ItemsChangeListener();

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
                if (!ocids.contains(item.getKey().getValue()) && "Database".equals(item.getKey().getPath())) { //NOI18N
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

    public synchronized boolean addItem(OCIItem newItem) {
        Parameters.notNull("newItem cannot be null", newItem);
        long presentCount = items.stream()
                .filter(i -> i.getKey().getPath().equals(newItem.getKey().getPath()))
                .count();
        if (newItem.maxInProject() > presentCount && isTenancyCompatible(newItem, true)) {
            if (newItem instanceof Validator) {
                Validator.Result result = ((Validator) newItem).validate();
                if (result.status == Validator.ValidationStatus.WARNING) {
                    showWarningMessage(result.message);
                }
                if (result.status == Validator.ValidationStatus.ERROR) {
                    showWarningMessage(result.message);
                    return false;
                }
            }
            items.add(newItem);
            newItem.addChangeListener(itemsListener);
            update();
            storeAssets();
        }
        return true;
    }

    synchronized void removeItem(OCIItem item) {
        boolean update = false;
        item.removeChangeListener(itemsListener);
        if (refNames.remove(item) != null) {
            update = true;
        }
        if (items.remove(item)) {
            update = true;
        }
        if (update) {
            storeAssets();
            update();
        }
    }

    void update() {
        OpenProjectsFinder.getDefault().findOpenProjects().thenAccept(projects -> {
            SuggestionAnalyzer analyzer = new DependenciesAnalyzer();
            setSuggestions(analyzer.findSuggestions(projects));
        });
    }
    
    public synchronized String getTenancyId() {
        Optional<OCIItem> ociItem = items.stream().findFirst();
        return ociItem == null || ociItem.isEmpty() ? null : ociItem.get().getTenancyId();
    }
    
    public synchronized boolean isTenancyCompatible(OCIItem toCheck) {
        return isTenancyCompatible(toCheck, false);
    }
    
    public synchronized boolean isTenancyCompatible(OCIItem toCheck, boolean showWarning) {
        List<OCIItem> itemsMissingInfo = new ArrayList<> ();
        for(OCIItem item: items) {
            if (item != null && item.getTenancyId() == null) {
                itemsMissingInfo.add(item);
            } else if (itemsMissingInfo.isEmpty() && item != null && !toCheck.getTenancyId().equals(item.getTenancyId())) {
                if (showWarning) {
                    showWarningMessage(Bundle.MSG_TenancyNotCompatible());
                }
                return false;
            }
        }
        if (!itemsMissingInfo.isEmpty()) {
            suggestToAddItemsAgain(itemsMissingInfo);
            return false;
        }
        
        return true;
    }

    private void suggestToAddItemsAgain(List<OCIItem> itemsForRemoval) {
        String itemNames = itemsForRemoval.stream()
                .map(OCIItem::getName)
                .collect(Collectors.joining(", "));
        showWarningMessage(Bundle.MSG_AddItemsAgain(itemNames));
    }

    private synchronized void setSuggestions(Set<SuggestedItem> newSuggested) {
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
                        break;
                    }
                }
                if (add) {
                    suggested.add(s);
                }
            }
        }
        changeSupport.fireChange();
    }

    /**
     * Returns a <code>Collection</code> of all items, including suggested
     * items.
     *
     * @return {@link Collection} of {@link OCIItem}
     */
    public Collection<OCIItem> getItems() {
        List<OCIItem> list = new ArrayList<>(suggested);
        list.addAll(items);
        return list;
    }
    
    /**
     * Returns a <code>Collection</code> of items assigned by user. This doesn't
     * include suggested items.
     *
     * @return {@link Collection} of {@link OCIItem}
     */
    public Collection<OCIItem> getAssignedItems() {
        return Collections.unmodifiableCollection(items);
    }
    
    public <T extends OCIItem> T getItem(Class<T> clazz) {
        for (OCIItem item : items) {
            if (clazz.isInstance(item)) {
                return (T) item;
            } 
        }
        return null;
    }
    
    public boolean itemExistWithoutReferanceName(Class<? extends OCIItem> cls) {
        return getReferenceNamesByClass(cls).isEmpty() && 
                CloudAssets.getDefault().getItems().stream().anyMatch(item -> cls.isInstance(item));
    }
    
    public boolean referenceNameExist(String itemPath, String refName) {
        for (Entry<OCIItem, String> refEntry : refNames.entrySet()) {
            if (refEntry.getKey().getKey().getPath().equals(itemPath)
                    && refName.equals(refEntry.getValue())) {
                return true;
            }
        }
        return false;
    }
    
    public void removeReferenceNameFor(OCIItem item) {
        refNames.remove(item);
    }

    public boolean setReferenceName(OCIItem item, String refName) {
        Parameters.notNull("refName", refName); //NOI18N
        Parameters.notNull("OCIItem", item); //NOI18N   
        if (referenceNameExist(item.getKey().getPath(), refName)) {
            return false;
        }
        String oldRefName = refNames.get(item);
        refNames.put(item, refName);
        storeAssets();
        item.fireRefNameChanged(oldRefName, refName);
//        item.fireRefNameChanged(null, refName);
        return true;
    }

    public String getReferenceName(OCIItem item) {
        return refNames.get(item);
    }

    public List<String> getReferenceNamesByClass(Class<? extends OCIItem> cls) {
        return refNames.entrySet().stream().filter(entry -> cls.isInstance(entry.getKey()))
                .map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    private void setReferenceName(String ocid, String refName) {
        for (OCIItem item : items) {
            if (item.getKey().getValue().equals(ocid)) {
                refNames.put(item, refName);
                storeAssets();
                item.fireRefNameChanged(null, refName);
                return;
            }
        }
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
                JsonWriter jsonWriter = gson.newJsonWriter(new OutputStreamWriter(os));
                jsonWriter.beginObject();
                jsonWriter.name("items").beginArray(); //NOI18N
                for (OCIItem item : items) {
                    gson.toJson(item, item.getClass(), jsonWriter);
                }
                jsonWriter.endArray();
                jsonWriter.name("referenceNames").beginArray(); //NOI18N
                for (Entry<OCIItem, String> entry : refNames.entrySet()) {
                    jsonWriter.beginObject();
                    jsonWriter.name("ocid").value(entry.getKey().getKey().getValue()); //NOI18N
                    jsonWriter.name("referenceName").value(entry.getValue()); //NOI18N
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
                jsonWriter.endObject();
                jsonWriter.close();
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
            LOG.log(Level.INFO, "Unable to load assets", ex);
            return;
        } finally {
            assetsLoaded = true;
        }

        try (JsonReader reader = new JsonReader(new StringReader(content))) {
            Set<OCIItem> loaded = new HashSet<>();
            Map<String, String> loadingRefNames = new HashMap<>();
            reader.beginObject();
            while (reader.hasNext()) {
                String rootObjName = reader.nextName();
                switch (rootObjName) {
                    case "items": //NOI18N
                        reader.beginArray();
                        while (reader.hasNext()) {
                            JsonElement element = JsonParser.parseReader(reader);
                            String path = element.getAsJsonObject()
                                    .get("id").getAsJsonObject() //NOI18N
                                    .get("path").getAsString(); //NOI18N
                            switch (path) {
                                case "Database": //NOI18N
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
                                case "ContainerRepository": //NOI18N
                                    loaded.add(gson.fromJson(element, ContainerRepositoryItem.class));
                                    break;
                                case "MetricsNamespace": //NOI18N
                                    loaded.add(gson.fromJson(element, MetricsNamespaceItem.class));
                                    break;
                            }
                        }
                        reader.endArray();
                        break;
                    case "referenceNames": //NOI18N
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginObject();
                            String refOcid = null;
                            String refName = null;
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                switch (name) {
                                    case "ocid":
                                        refOcid = reader.nextString();
                                        break;
                                    case "referenceName":
                                        refName = reader.nextString();
                                        break;
                                    default:
                                        reader.skipValue();
                                        break;
                                }
                                if (refOcid != null && refName != null) {
                                    loadingRefNames.put(refOcid, refName);
                                }
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                        break;
                }
            }
            reader.endObject();
            for (OCIItem oCIItem : loaded) {
                oCIItem.addChangeListener(itemsListener);
            }
            items = loaded;
            for (Entry<String, String> entry : loadingRefNames.entrySet()) {
                setReferenceName(entry.getKey(), entry.getValue());
            }

        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            LOG.log(Level.INFO, "Unable to load assets", e);
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
    
    private final class ItemsChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            CloudAssets.this.storeAssets();
        }
    }
    
}
