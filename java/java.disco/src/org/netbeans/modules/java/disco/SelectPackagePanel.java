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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "SelectPackage.quick=Quick",
    "SelectPackage.advanced=Advanced",
    "SelectPackage.componentName=Connect to OpenJDK Discovery Service",
    "SelectPackage.loadingError=Could not load list. Please check network access or try again later."
})
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
        quickPanel = new QuickPanel(this);
        advancedPanel = new FooAdvancedPanel();

        //please wait message
        ((CardLayout) getLayout()).first(this);
        tabs.add(Bundle.SelectPackage_quick(), quickPanel);
        tabs.add(Bundle.SelectPackage_advanced(), advancedPanel);
        tabs.addChangeListener((ChangeEvent e) -> {
            SelectPackagePanel.this.fireValidityChange();
            if (tabs.getSelectedComponent() == quickPanel) {
                quickPanel.switchFocus(advancedPanel.getSelectedDistribution(),
                        advancedPanel.getSelectedVersion());
            } else {
                advancedPanel.switchFocus(quickPanel.getSelectedDistribution(),
                        quickPanel.getSelectedVersion());
            }
        });
    }

    @UIEffect
    private void init() {
        setName(Bundle.SelectPackage_componentName());
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
            final int current;

            public Result(List<Integer> versionNumbers, Map<Integer, TermOfSupport> versionNumberSupport, List<Distribution> distributions, int current) {
                this.versionNumbers = versionNumbers;
                this.versionNumberSupport = versionNumberSupport;
                this.distributions = distributions;
                this.current = current;
            }

        }

        if (initialLoad)
            return;
        initialLoad = true;

        //loading stuff when ui shown
        submit(() -> {
            int minVersion = 6;
            int maxVersion = discoClient.getLatestEAVersion().getAsInt();
            int current    = discoClient.getLatestGAVersion().getAsInt();

            // limit to LTS + current
            Map<Integer, TermOfSupport> maintainedVersions = discoClient.getAllMaintainedMajorVersions()
                    .filter(v -> v.getAsInt() >= minVersion && v.getAsInt() <= current)   // defensive filter, the API returned an EA JDK as released
                    .filter(v -> v.getAsInt() == current || v.getTermOfSupport() == TermOfSupport.LTS)
                    .collect(Collectors.toMap(MajorVersion::getAsInt, MajorVersion::getTermOfSupport));

            List<Integer> versionNumbers = IntStream.range(minVersion, maxVersion+1).boxed().collect(Collectors.toList());
            List<Distribution> distros = discoClient.getDistributions();

            return new Result(versionNumbers, maintainedVersions, distros, current);
        }).then((c) -> {
            //hide 'please wait' message, show tabs
            ((CardLayout) getLayout()).next(SelectPackagePanel.this);

            Distribution defaultDist = discoClient.getDistribution(DiscoPlatformInstall.defaultDistribution()).orElse(null);
            advancedPanel.updateDistributions(c.distributions);
            advancedPanel.setVersions(c.versionNumbers, c.versionNumberSupport, c.current);
            quickPanel.updateDistributions(c.distributions, defaultDist);
            quickPanel.setVersions(c.versionNumbers, c.versionNumberSupport, c.current);
            quickPanel.initFocus();
            fireValidityChange();
        }).handle(ex -> {
            loadingLabel.setText(Bundle.SelectPackage_loadingError());
            initialLoad = false;

            long currentTimeMillisStart = System.currentTimeMillis();
            //check connectivity
            submit(() -> {
                String body = Helper.get("https://www.example.com").body();
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

    void fireValidityChange() {
        firePropertyChange(PROP_VALIDITY_CHANGED, null, null);
    }

    class FooAdvancedPanel extends AdvancedPanel {

        FooAdvancedPanel() {
            ListSelectionModel selectionModel = table.getSelectionModel();
            selectionModel.addListSelectionListener(e -> {
                SelectPackagePanel.this.fireValidityChange();
            });
        }

        @UIEffect
        @Override
        protected void updateData(Distribution distribution, Integer featureVersion, Architecture architecture, Latest latest, PackageType bundleType, boolean ea) {
            if (distribution == null || featureVersion == null) {
                return;
            }
            OperatingSystem operatingSystem = getOperatingSystem();
            ArchiveType extension = ArchiveType.NONE;
            Boolean fx = false;
            this.setEnabled(false);
            submit(() -> {
                List<Pkg> bundles = discoClient.getPkgs(distribution, new VersionNumber(featureVersion), latest, operatingSystem, architecture, extension, bundleType, ea, fx);
                return bundles;
            }).then(this::setPackages)
              //TODO: Show something to user, offer reload, auto-reload in N seconds?
              .handle(Exceptions::printStackTrace)
              .execute();
        }

        @Override
        protected void updateDistributions(List<Distribution> distros) {
            super.updateDistributions(distros);
        }

        @UIEffect
        private void setPackages(List<Pkg> bundles) {
            SelectPackagePanel.this.setEnabled(true);
            tableModel.setBundles(bundles);
        }
    }

    @UIEffect
    public @Nullable PkgSelection getSelectedPackage() {
        if (!tabs.isVisible()) {
            return null;
        }

        Pkg pkg = null;
        switch (tabs.getSelectedIndex()) {
            case 0:
                pkg = quickPanel.getSelectedPackage();
                break;
            case 1:
                pkg = advancedPanel.getSelectedPackage();
                break;
            default:
                throw new IllegalStateException();
        }
        return pkg == null ? null : PkgSelection.of(pkg);
    }

}
