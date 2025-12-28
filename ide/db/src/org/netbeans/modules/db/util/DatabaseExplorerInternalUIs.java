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

package org.netbeans.modules.db.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.driver.JDBCDriverSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public final class DatabaseExplorerInternalUIs {

    private DatabaseExplorerInternalUIs() {
    }

    public static void connect(JComboBox comboBox, JDBCDriverManager driverManager) {
        connect(comboBox, driverManager, null);
    }

    public static void connect(JComboBox comboBox, JDBCDriverManager driverManager, String driverClass) {
        DataComboBoxSupport.connect(comboBox, new DriverDataComboBoxModel(driverManager, driverClass), driverClass == null);
    }

    public static void connect(JComboBox comboBox, JDBCDriverManager driverManager, boolean withUrl) {
        if (withUrl) {
            connect(comboBox, driverManager, null);
        } else {
            DataComboBoxSupport.connect(comboBox, new SimpleDriverDataModel(driverManager), true);
        }
    }

    private static final class DriverDataComboBoxModel implements DataComboBoxModel {

        private final JDBCDriverManager driverManager;
        private final DriverComboBoxModel comboBoxModel;

        public DriverDataComboBoxModel(JDBCDriverManager driverManager, String driverClass) {
            this.driverManager = driverManager;
            this.comboBoxModel = new DriverComboBoxModel(driverManager, driverClass);
        }

        @Override
        public String getItemTooltipText(Object item) {
            JdbcUrl url = (JdbcUrl)item;
            if (url.getDriver() == null) {
                return "";
            } else {
                return url.getDriver().toString();
            }
        }

        @Override
        public String getItemDisplayName(Object item) {
            return ((JdbcUrl)item).getDisplayName();
        }

        @Override
        public void newItemActionPerformed() {
            Set<JDBCDriver> oldDrivers = new HashSet<JDBCDriver>(Arrays.asList(driverManager.getDrivers()));
            driverManager.showAddDriverDialog();

            // try to find the new driver
            JDBCDriver[] newDrivers = driverManager.getDrivers();
            if (newDrivers.length == oldDrivers.size()) {
                // no new driver, so...
                return;
            }
            for (int i = 0; i < newDrivers.length; i++) {
                if (!oldDrivers.contains(newDrivers[i])) {
                    comboBoxModel.addSelectedDriver(newDrivers[i]);
                    break;
                }
            }
        }

        @Override
        public String getNewItemDisplayName() {
            return NbBundle.getMessage(DatabaseExplorerInternalUIs.class, "LBL_NewDriver");
        }

        @Override
        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class DriverComboBoxModel extends AbstractListModel implements ComboBoxModel {

        private final ArrayList<JdbcUrl> driverList;

        private Object selectedItem; // can be anything, not just a database driver

        public DriverComboBoxModel(JDBCDriverManager driverManager, String driverClass) {
            driverList = new ArrayList<JdbcUrl>();
            JDBCDriver[] drivers;
            if (driverClass != null) {
                drivers = driverManager.getDrivers(driverClass);
            } else {
                drivers = driverManager.getDrivers();
            }
            for (int i = 0; i < drivers.length; i++) {
                JDBCDriver driver = drivers[i];
                if (JDBCDriverSupport.isAvailable(driver)) {
                    driverList.addAll(DriverListUtil.getJdbcUrls(driver));
                }
            }

            driverList.sort(new DriverTypeComparator());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
        }

        @Override
        public Object getElementAt(int index) {
            return driverList.get(index);
        }

        @Override
        public int getSize() {
            return driverList.size();
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        public void addSelectedDriver(JDBCDriver driver) {
            List<JdbcUrl> types = DriverListUtil.getJdbcUrls(driver);

            assert(! types.isEmpty());
            driverList.addAll(types);
            setSelectedItem(types.get(0));

            driverList.sort(new DriverTypeComparator());
            fireContentsChanged(this, 0, driverList.size());
        }

    }

    private static final class DriverTypeComparator implements Comparator<JdbcUrl> {
        @Override
        public int compare(JdbcUrl type1, JdbcUrl type2) {
            if (type1 == null) {
                return type2 == null ? 0 : -1;
            } else {
                if (type2 == null) {
                    return 1;
                }
            }

            String dispName1 = type1.getName();
            String dispName2 = type2.getName();

            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
    private static final class SimpleDriverDataModel implements DataComboBoxModel {

        private final JDBCDriverManager driverManager;
        private final SimpleDriverComboModel comboBoxModel;

        public SimpleDriverDataModel(JDBCDriverManager driverManager) {
            this.driverManager = driverManager;
            this.comboBoxModel = new SimpleDriverComboModel(driverManager);
        }

        @Override
        public String getItemTooltipText(Object item) {
            JDBCDriver drv = (JDBCDriver)item;
            return drv.toString();
        }

        @Override
        public String getItemDisplayName(Object item) {
            return ((JDBCDriver)item).getDisplayName();
        }

        @Override
        public void newItemActionPerformed() {
            Set<JDBCDriver> oldDrivers = new HashSet<JDBCDriver>(Arrays.asList(driverManager.getDrivers()));
            driverManager.showAddDriverDialog();

            // try to find the new driver
            JDBCDriver[] newDrivers = driverManager.getDrivers();
            if (newDrivers.length == oldDrivers.size()) {
                // no new driver, so...
                return;
            }
            for (int i = 0; i < newDrivers.length; i++) {
                if (!oldDrivers.contains(newDrivers[i])) {
                    comboBoxModel.addSelectedDriver(newDrivers[i]);
                    break;
                }
            }
        }

        @Override
        public String getNewItemDisplayName() {
            return NbBundle.getMessage(DatabaseExplorerInternalUIs.class, "LBL_NewDriver");
        }

        @Override
        public ComboBoxModel getListModel() {
            return comboBoxModel;
        }
    }

    private static final class SimpleDriverComboModel extends AbstractListModel implements ComboBoxModel {

        private List<JDBCDriver> driverList;
        private final JDBCDriverManager driverManager;

        private Object selectedItem; // can be anything, not just a database driver

        public SimpleDriverComboModel(JDBCDriverManager driverManager) {
            this.driverManager = driverManager;
            this.driverList = new ArrayList<JDBCDriver>();
            JDBCDriver[] drivers;
            drivers = driverManager.getDrivers();
            for (int i = 0; i < drivers.length; i++) {
                JDBCDriver driver = drivers[i];
                if (JDBCDriverSupport.isAvailable(driver)) {
                    driverList.add(driver);
                }
            }

            driverList.sort(new JDBCDriverComparator());
        }

        private void updateDriverList() {
            driverList = new ArrayList<JDBCDriver>();
            JDBCDriver[] drivers;
            drivers = driverManager.getDrivers();
            for (int i = 0; i < drivers.length; i++) {
                JDBCDriver driver = drivers[i];
                if (JDBCDriverSupport.isAvailable(driver)) {
                    driverList.add(driver);
                }
            }

            driverList.sort(new JDBCDriverComparator());
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedItem = anItem;
            updateDriverList();
        }

        @Override
        public Object getElementAt(int index) {
            return driverList.get(index);
        }

        @Override
        public int getSize() {
            return driverList.size();
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        public void addSelectedDriver(JDBCDriver driver) {
            driverList.add(driver);
            setSelectedItem(driver);

            driverList.sort(new JDBCDriverComparator());
            fireContentsChanged(this, 0, driverList.size());
        }

    }

    private static final class JDBCDriverComparator implements Comparator<JDBCDriver> {
        @Override
        public int compare(JDBCDriver drv1, JDBCDriver dvr2) {
            if (drv1 == null) {
                return dvr2 == null ? 0 : -1;
            } else {
                if (dvr2 == null) {
                    return 1;
                }
            }

            String dispName1 = drv1.getName();
            String dispName2 = dvr2.getName();

            if (dispName1 == null) {
                return dispName2 == null ? 0 : -1;
            } else {
                return dispName2 == null ? 1 : dispName1.compareToIgnoreCase(dispName2);
            }
        }
    }
}
