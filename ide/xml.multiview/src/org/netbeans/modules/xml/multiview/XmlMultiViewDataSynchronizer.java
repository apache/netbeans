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
package org.netbeans.modules.xml.multiview;

import org.openide.filesystems.FileLock;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;

/**
 * Performs synchronization between model and binary data in data object
 * or rather in {@link XmlMultiViewEditorSupport.XmlEnv}.
 *
 * @author pfiala
 */
public abstract class XmlMultiViewDataSynchronizer {

    private final int updateDelay;
    private long timeStamp;
    private final XmlMultiViewDataObject dataObject;
    private int reloading = 0;
    private int updating = 0;

    protected final RequestProcessor requestProcessor =
            new RequestProcessor("XmlMultiViewDataSynchronizer RequestProcessor", 1);  // NOI18N
    private FileLock updateLock = null;

    private final RequestProcessor.Task updateTask = requestProcessor.create(new Runnable() {
        public void run() {
            if (isUpdateLock()) {
                finishUpdateTask.cancel();
                updateData(updateLock, true);
                synchronized (updateTask) {
                    if (updateTask.getDelay() <= 0) {
                        finishUpdateTask.schedule(1);
                    }
                }
            }
        }
    });

    private final RequestProcessor.Task finishUpdateTask = requestProcessor.create(new Runnable() {
        public void run() {
            synchronized (updateTask) {
                if (isUpdateLock()) {
                    updateLock.releaseLock();
                    updateLock = null;
                }
            }
        }
    });

    private final RequestProcessor.Task reloadTask = requestProcessor.create(new Runnable() {
        public void run() {
            reloadModel();
        }
    });
    private final XmlMultiViewDataObject.DataCache dataCache;

    /**
     * Creates synchronizer for given data object and sets default delay.
     * @param dataObject data object containing the binary data
     * @param delay default delay between model modification and data update
     */
    public XmlMultiViewDataSynchronizer(XmlMultiViewDataObject dataObject, int delay) {
        this.dataObject = dataObject;
        dataCache = dataObject.getDataCache();
        updateDelay = delay;
        this.dataObject.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (XmlMultiViewDataObject.PROPERTY_DATA_MODIFIED.equals(evt.getPropertyName())) {
                    dataModified(((Long) evt.getNewValue()).longValue());
                } else if (XmlMultiViewDataObject.PROPERTY_DATA_UPDATED.equals(evt.getPropertyName())) {
                    dataUpdated(((Long) evt.getNewValue()).longValue());
                }
            }
        });
    }

    protected void dataModified(long timeStamp) {
        if (this.timeStamp < timeStamp) {
            reloadTask.schedule(10);
        }
    }

    protected void dataUpdated(long timeStamp) {
        if (updating == 0 && this.timeStamp < timeStamp) {
            reloadTask.schedule(10);
        }
    }

    /**
     * Obtains data lock if possible.
     * @return the lock if success
     * @throws IOException
     */
    public FileLock takeLock() throws IOException {
        final FileLock lock = dataObject.waitForLock(1000);
        if (lock != null) {
            if (mayUpdateData(true)) {
                return lock;
            } else {
                lock.releaseLock();
            }
        }
        return null;
    }

    /**
     * Schedules update of binary data from model.
     * Property <I>modified</I> of the data object is true after update.
     */
    public final void requestUpdateData() {
        if (reloading == 0) {
            synchronized (updateTask) {
                finishUpdateTask.cancel();
                if (!isUpdateLock()) {
                    try {
                        updateLock = takeLock();
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                        return;
                    } 
                }
                updateTask.schedule(updateDelay);
            }
        }
    }

    private boolean isUpdateLock() {
        return updateLock != null && updateLock.isValid();
    }

    /**
     * Test whether the synchronizer may update the binary data.
     *
     * @return true if the synchronizer may update the binary data, otherwise false
     * (e.g. in case of invalid xml data)
     * @param allowDialog allows opening of dialog for confimation if taking a lock could lead to data loss
     */
    protected abstract boolean mayUpdateData(boolean allowDialog);

    /**
     * Updates data from model.
     * @param model a model
     * @param lock a lock of the data cache
     * @param modify indicator whether property <i>modified</i> of the data object
     * should change after update or not
     */
    protected abstract void updateDataFromModel(Object model, FileLock lock, boolean modify);

    /**
     * Returns model of the synchronizer
     * @return the model
     */
    protected abstract Object getModel();


    /**
     * Reloads model from data.
     */
    protected abstract void reloadModelFromData();

    final void reloadingStarted() {
        reloading++;
    }

    final void reloadingFinished() {
        reloading--;
    }

    public final RequestProcessor.Task getReloadTask() {
        return reloadTask;
    }

    /**
     * Reloads model from binary data in cache
     */
    protected void reloadModel() {
        long newTimeStamp = dataCache.getTimeStamp();
        if (timeStamp < newTimeStamp) {
            reloadingStarted();
            try {
                timeStamp = newTimeStamp;
                reloadModelFromData();
            } finally {
                reloadingFinished();
            }
        }
    }

    /**
     * Obtains a binary data lock and crates the {@link Transaction}
     * @return Transaction object
     */
    public Transaction openTransaction() {
        try {
            FileLock lock = takeLock();
            if (lock != null) {
                return new Transaction(lock);
            }
        } catch (IOException e) {
            ErrorManager.getDefault().annotate(e, NbBundle.getMessage(XmlMultiViewDataSynchronizer.class,
                    "START_TRANSACTION_FAILED"));
        }
        return null;
    }

    /**
     * Updates data from model and updates timeStamp field.
     * @param dataLock a lock of the data cache. Will be released after updating 
     * has finished (in case it wasn't released already).
     * @param modify indicator whether property <i>modified</i> of the data object
     * should change after update or not
     */
    public void updateData(FileLock dataLock, boolean modify) {
        updating++;
        try {
            updateDataFromModel(getModel(), dataLock, modify);
            timeStamp = dataCache.getTimeStamp();
        } finally {
            updating--;
            if (dataLock != null && dataLock.isValid()){
                dataLock.releaseLock();
            }
        }
    }

    /**
     * Serves to controlling complex model changes
     */
    public class Transaction {
        private FileLock lock;

        private Transaction(FileLock lock) {
            this.lock = lock;
        }

        /**
         * Rollback changes made during the transaction.
         */
        public void rollback() {
            if (lock != null) {
                reloadModel();
                lock.releaseLock();
                lock = null;
            }
        }

        /**
         * Commit changes - update binary data from model.
         */
        public void commit() throws IOException {
            dataCache.testLock(lock);
            updateData(lock, false);
            lock.releaseLock();
            lock = null;
        }
    }
}
