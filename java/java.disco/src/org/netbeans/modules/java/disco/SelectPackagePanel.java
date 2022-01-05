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
package org.netbeans.modules.java.disco;

import com.google.common.collect.Maps;
import eu.hansolo.jdktools.Architecture;
import io.foojay.api.discoclient.pkg.Pkg;
import eu.hansolo.jdktools.PackageType;
import io.foojay.api.discoclient.pkg.Distribution;
import eu.hansolo.jdktools.OperatingSystem;
import eu.hansolo.jdktools.versioning.VersionNumber;
import eu.hansolo.jdktools.ArchiveType;
import eu.hansolo.jdktools.Latest;
import io.foojay.api.discoclient.pkg.MajorVersion;
import eu.hansolo.jdktools.TermOfSupport;
import io.foojay.api.discoclient.util.Helper;

import static org.netbeans.modules.java.disco.OS.getOperatingSystem;
import static org.netbeans.modules.java.disco.SwingWorker2.submit;

import java.awt.CardLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openide.util.Exceptions;

@SuppressWarnings("initialization")
public class SelectPackagePanel extends FirstPanel {
    private static final Logger log = Logger.getLogger(SelectPackagePanel.class.getName());

    public static final String PROP_VALIDITY_CHANGED = "panelValidityChanged";

    private final Client discoClient;

    @UIEffect
    public static SelectPackagePanel create() {
        SelectPackagePanel f = new SelectPackagePanel();
        f.init();
        return f;
    }
    private final QuickPanel quickPanel;
    private final FooAdvancedPanel advancedPanel;

    @SuppressWarnings("initialization")
    @UIEffect
    private SelectPackagePanel() {
        // Setup disco client
        discoClient = Client.getInstance();
        quickPanel = new QuickPanel();
        advancedPanel = new FooAdvancedPanel();

        //please wait message
        ((CardLayout) getLayout()).first(this);
        tabs.add("Quick", quickPanel);
        tabs.add("Advanced", advancedPanel);
        tabs.addChangeListener((ChangeEvent e) -> {
            SelectPackagePanel.this.firePropertyChange(PROP_VALIDITY_CHANGED, false, true);
        });
    }

    @UIEffect
    private void init() {
        setName("Connect to OpenJDK Discovery Service");
    }

    private boolean initialLoad = false; //track the async load in addNotify

    @Override
    @UIEffect
    public void addNotify() {
        super.addNotify();
        
        class Result {
            final List<Integer> versionNumbers;
            final Map<Integer, TermOfSupport> versionNumberSupport;
            final List<Distribution> distributions;

            public Result(List<Integer> versionNumbers, Map<Integer, TermOfSupport> versionNumberSupport, List<Distribution> distributions) {
                this.versionNumbers = versionNumbers;
                this.versionNumberSupport = versionNumberSupport;
                this.distributions = distributions;
            }
            
        }

        if (initialLoad)
            return;
        initialLoad = true;

        //loading stuff when ui shown
        submit(() -> {
                    List<Distribution> distros = discoClient.getDistributions();
                    // Get release infos
                    Map<Integer, TermOfSupport> majorVersions = discoClient.getAllMajorVersions().stream()
                            .collect(Collectors.toMap(MajorVersion::getAsInt, MajorVersion::getTermOfSupport));

                    MajorVersion nextRelease = discoClient.getLatestSts(true);
                    Integer nextFeatureRelease = nextRelease.getAsInt();

                    List<Integer> versionNumbers = new ArrayList<>();
                    for (Integer i = 6; i <= nextFeatureRelease; i++) {
                        versionNumbers.add(i);
                    }
                    Map<Integer, TermOfSupport> versionNumberSupport = new HashMap<>(Maps.filterKeys(majorVersions, v -> versionNumbers.contains(v)));
//                    return new AbstractMap.SimpleEntry<>(versionNumbers, versionNumberSupport);
                    return new Result(versionNumbers, versionNumberSupport, distros);
        }).then((c) -> {
            //hide 'please wait' message, show tabs
            ((CardLayout) getLayout()).next(SelectPackagePanel.this);

            advancedPanel.updateDistributions(c.distributions);
            advancedPanel.setVersions(c.versionNumbers, c.versionNumberSupport);
            quickPanel.setVersions(c.versionNumbers, c.versionNumberSupport);

            SelectPackagePanel.this.firePropertyChange(PROP_VALIDITY_CHANGED, false, true);
        }).handle(ex -> {
            loadingLabel.setText("Could not load list due to an error. Please try again later.");
            initialLoad = false;

            long currentTimeMillisStart = System.currentTimeMillis();
            //check connectivity
            submit(() -> {
                String body = Helper.get("http://www.example.com").body();
                return body != null && !"".equals(body);
            }).then(isOnline -> {
                long now = System.currentTimeMillis();
                //if we are online, but still got an error, let's show it to the user if our ping didn't take forever
                if (isOnline && (now - currentTimeMillisStart <= 300)) {
                    Exceptions.printStackTrace(ex);
                } else {
                    log.log(Level.INFO, "Could not load initial list", ex);
                }
            }).handle(ex2 -> {
                //the ping itself got an error, log everything
                log.log(Level.INFO, "Could not load initial list", ex);
                log.log(Level.INFO, "Could not check network connectivity", ex2);
            })
            .execute();
        }).execute();
    }

    class FooAdvancedPanel extends AdvancedPanel {

        FooAdvancedPanel() {
            ListSelectionModel selectionModel = table.getSelectionModel();
            selectionModel.addListSelectionListener(e -> {
                SelectPackagePanel.this.firePropertyChange(PROP_VALIDITY_CHANGED, false, true);
            });
        }

    @UIEffect
    @Override
    protected void updateData(Distribution distribution, Integer featureVersion, Latest latest, PackageType bundleType) {
        if (distribution == null)
            return;
        if (featureVersion == null)
            return;
        OperatingSystem operatingSystem = getOperatingSystem();
        Architecture architecture = Architecture.NONE;
        ArchiveType extension = ArchiveType.NONE;
        Boolean fx = false;
        this.setEnabled(false);
        submit(() -> {
                List<Pkg> bundles = discoClient.getPkgs(distribution, new VersionNumber(featureVersion), latest, operatingSystem, architecture, extension, bundleType, fx);
                return bundles;
        }).then(this::setPackages)
                //TODO: Show something to user, offer reload, auto-reload in N seconds?
                .handle(Exceptions::printStackTrace)
                .execute();
    }

    @Override
    protected void updateDistributions(List<Distribution> distros) {
        super.updateDistributions(distros); //To change body of generated methods, choose Tools | Templates.
    }

    @UIEffect
    private void setPackages(List<Pkg> bundles) {
        SelectPackagePanel.this.setEnabled(true);
        tableModel.setBundles(bundles);
    }
    }

    @UIEffect
    public @Nullable PkgSelection getSelectedPackage() {
        if (!tabs.isVisible())
            return null;

        switch (tabs.getSelectedIndex()) {
            case 0:
                return new QuickPkgSelection(quickPanel.getSelectedPackage());
            case 1:
                Pkg pkg = advancedPanel.getSelectedPackage();
                if (pkg == null)
                    return null;
                return PkgSelection.of(pkg);
            default:
                throw new IllegalStateException();
        }
    }

}
