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

package org.netbeans.modules.j2ee.persistence.unit;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 * Base class for persistence multiview editor tests.
 *
 * @author Erno Mononen
 */
public abstract class PersistenceEditorTestBase extends PUDataObjectTestBase {
    
    protected static final String PATH = "/persistence.xml";
    protected PUDataObject dataObject;
    protected FileObject ddFile;
    protected PersistenceToolBarMVElement mvElement;
    
    
    public PersistenceEditorTestBase(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initDataObject();
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(dataObject != null) {
            dataObject.save();
        }
        ddFile.refresh();
        ddFile.delete();
    }
    
    private void initDataObject() throws IOException{
        String persistenceFile = getDataDir().getAbsolutePath() + PATH;
        FileObject original = FileUtil.toFileObject(new File(persistenceFile));
        
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        this.ddFile = FileUtil.copyFile(original, workDirFO, "persistence_copy");
        this.dataObject = (PUDataObject) DataObject.find(ddFile);
        MockLookup.setInstances(dataObject);
        this.mvElement = new PersistenceToolBarMVElement(MockLookup.getDefault());
        
        Persistence persistence = dataObject.getPersistence();
        assertSame(2, persistence.getPersistenceUnit().length);
        assertEquals("em", persistence.getPersistenceUnit(0).getName());
        assertEquals("em2", persistence.getPersistenceUnit(1).getName());
    }
    
    /**
     * @return true if given FileObject contains given String, false
     * otherwise.
     */
    protected boolean fileObjectContains(FileObject fo, String str){
        return readFileObject(fo).indexOf(str) >= 0 ;
    }
    
    /**
     * Waits for the model synchronizer to update the data and
     * then checks whether data object's data cache contains
     * given string.
     * @return true if data object's data cache contains given str,
     * false otherwise.
     * @throws InterruptedException if waiting has been interrupted or if
     *    the wait cannot succeed due to possible deadlock collision
     */
    protected boolean dataCacheContains(String str) throws InterruptedException{
        RequestProcessor.Task updateTask = getUpdateTask();
        
        assertNotNull("Could not get updateTask", updateTask); //NOI18N
        
        updateTask.waitFinished(20000);
        return dataObject.getDataCache().getStringData().contains(str);
    }
    
    /**
     * Gets the task that takes care of updating the data from 
     * the data object's model synchronizer. Relies heavily on reflection and
     * will break down when there are changes in class hierarchy or field names. 
     * Needed for now since the current API doesn't give access to the task. 
     */
    private RequestProcessor.Task getUpdateTask(){
        RequestProcessor.Task updateTask = null;
        try {
            // get PUDataObject's model synchronizer (PUDataObject.ModelSynchronizer)
            Field puSynchronizerField = dataObject.getClass().getDeclaredField("modelSynchronizer");
            puSynchronizerField.setAccessible(true);
            XmlMultiViewDataSynchronizer puSynchronizer = 
                    (XmlMultiViewDataSynchronizer) puSynchronizerField.get(dataObject);
            // get the update task from the XmlMultiViewDataSynchronizer that is 
            // puSynchronizer's super class
            Field updateTaskField = puSynchronizer.getClass().getSuperclass().getDeclaredField("updateTask");
            updateTaskField.setAccessible(true);
            updateTask = (RequestProcessor.Task) updateTaskField.get(puSynchronizer);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
        return updateTask;
    }
    
    protected String readFileObject(FileObject fo){
        StringBuilder sb = new StringBuilder();
        int i;
        try (InputStream stream = fo.getInputStream()) {
            while ((i = stream.read()) != -1) {
                sb.append((char) i);
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        return sb.toString();
    }
    
    
    protected boolean containsUnit(PersistenceUnit persistenceUnit){
        for (int i = 0; i < dataObject.getPersistence().getPersistenceUnit().length; i++) {
            if (dataObject.getPersistence().getPersistenceUnit()[i].equals(persistenceUnit)){
                return true;
            }
        }
        return false;
    }
    
}
