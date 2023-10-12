/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE jsonreleaseinfoFile
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this jsonreleaseinfoFile
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this jsonreleaseinfoFile except in compliance
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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author skygo
 */
public class ReleaseJsonProperties extends Task {

    // how many previous versions to suggest importing configuration from
    private static final int PREVIOUS_VERSIONS_LIMIT = 3;

    /**
     * current branch we works with
     */
    private String branch;
    /**
     * current hash we works with
     */
    private String hash;
    /**
     * cache of json file
     */
    private File jsonreleaseinfoFile;
    /**
     * xml file containing release information
     */
    private File xmlFile;

    /**
     * xml file containing release information
     */
    private File propertiesFile;

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setFile(File file) {
        this.jsonreleaseinfoFile = file;
    }

    public void setXmloutput(File file) {
        this.xmlFile = file;
    }

    public void setPropertiesoutput(File file) {
        this.propertiesFile = file;
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        JSONParser jsonParser = new JSONParser();
        ReleaseInfo requiredbranchinfo = null;
        List<ReleaseInfo> ri = new ArrayList<>();
        //Prepare a xml document containg all the release information */
        Document doc = XMLUtil.createDocument("releases");
        Element releasesxml = doc.getDocumentElement();

        //branch parameter
        if (branch.equals("master") || branch.startsWith("release")) {
            log("Taking release info from json");
        } else {
            log("Branch '" + branch + "' is not having good pattern defaulting to 'master'");
            branch = "master";
        }
        // read all information and store each release in Rel
        try (FileReader reader = new FileReader(jsonreleaseinfoFile)) {
            JSONObject releaseList = (JSONObject) jsonParser.parse(reader);
            log("Processing release: " + releaseList.keySet().toString());
            for (Object object : releaseList.keySet()) {
                ri.add(manageRelease(object.toString(), releaseList.get(object)));
            }
        } catch (ParseException | IOException ex) {
            throw new BuildException(ex);
        }

        // sort all information
        Collections.sort(ri);
        // build a sorted xml

        for (ReleaseInfo releaseInfo : ri) {
            log(releaseInfo.toString());
            for (Object milestone : releaseInfo.milestones) {
                log(milestone.toString());
            }
            Element releasexml = (Element) releasesxml.appendChild(doc.createElement("release"));
            populatexml(releasexml, releaseInfo);
            if (releaseInfo.getKey().equals(branch)) {
                requiredbranchinfo = releaseInfo;
                releasesxml.setAttribute("position", Integer.toString(releaseInfo.position));
                // attribute to know position of the requested current branch in the set of release
            }
        }

        if (requiredbranchinfo == null) {
            throw new BuildException("No Release Information found for branch '" + branch + "', update json file section with ant -Dneedjsondownload=true");
        }

        int reqBranchPosition = requiredbranchinfo.position;
        List<String> updateValues = ri.stream()
                .sorted(Comparator.reverseOrder())
                .filter(r -> r.position < reqBranchPosition)
                .filter(r -> r.publishapi) // not unpublished / VSCode-only releases
                .limit(PREVIOUS_VERSIONS_LIMIT)
                .map(r -> r.version)
                .collect(Collectors.toList());

// populate properties for api changes
        getProject().setProperty("previous.release.year", Integer.toString(requiredbranchinfo.previousReleaseDate.getYear()));
        getProject().setProperty("previous.release.month", String.format("%02d", requiredbranchinfo.previousReleaseDate.getMonthValue()));
        getProject().setProperty("previous.release.day", String.format("%02d", requiredbranchinfo.previousReleaseDate.getDayOfMonth()));
// version branding + incubating status management for Apache NetBeans 9.0 10 11.0
        getProject().setProperty("json.maturity", requiredbranchinfo.maturity);
        getProject().setProperty("json.version", requiredbranchinfo.version);
        getProject().setProperty("modules-javadoc-date", ReleaseJsonProperties.makeDate(requiredbranchinfo.releaseDate));
        getProject().setProperty("atom-date", ReleaseJsonProperties.makeAtomDate(requiredbranchinfo.releaseDate));
        getProject().setProperty("javaapidocurl", requiredbranchinfo.javaapidocurl);
        log("Writing releasinfo file " + propertiesFile);
        propertiesFile.getParentFile().mkdirs();
        try (OutputStream config = new FileOutputStream(propertiesFile)) {
            String optionalversion = "";
            boolean found = false;
            for (MileStone m : requiredbranchinfo.milestones) {
                if (m.hash.equals(hash)) {
                    found = true;
                    log("found hash" + hash + "-" + m.vote);
                    // found a milestone
                    if (m.vote != -1) {
                        // vote is set we want the full version
                    } else {
                        optionalversion = "-" + m.version;
                    }

                }
            }
            if (!found && !branch.equals("master")) {
                // hash no match we are building a dev version of specific branch
                optionalversion = "-dev";
            }
            config.write(("metabuild.DistributionURL=" + requiredbranchinfo.updateurl + "\n").getBytes());
            config.write(("metabuild.PluginPortalURL=" + requiredbranchinfo.pluginsurl + "\n").getBytes());
            // used for cache and user dir
            config.write(("metabuild.RawVersion=" + requiredbranchinfo.version + optionalversion + "\n").getBytes());
            config.write(("metabuild.apachepreviousversion=" + String.join(",", updateValues) + "\n").getBytes());
            if (branch.equals("master")) {
                config.write(("metabuild.ComputedSplashVersion=DEV (Build {0})\n").getBytes());
                config.write(("metabuild.ComputedTitleVersion=DEV {0}\n").getBytes());
                config.write(("metabuild.logcli=-J-Dnetbeans.logger.console=true -J-ea\n").getBytes());
            } else {
                config.write(("metabuild.ComputedSplashVersion=" + requiredbranchinfo.version + optionalversion + "\n").getBytes());
                config.write(("metabuild.ComputedTitleVersion=" + requiredbranchinfo.version + optionalversion + "\n").getBytes());
                config.write(("metabuild.logcli=\n").getBytes());
            }
        } catch (IOException ex) {
            throw new BuildException("Properties File for release cannot be created");
        }

        log("Writing release info file " + xmlFile);

        xmlFile.getParentFile().mkdirs();
        try (OutputStream config = new FileOutputStream(xmlFile)) {
            XMLUtil.write(doc, config);
        } catch (IOException ex) {
            throw new BuildException("XML File for release cannot be created");
        }
        String configline;
        try (FileReader config = new FileReader(propertiesFile); BufferedReader configStream = new BufferedReader(config);) {
            while ((configline = configStream.readLine()) != null) {
                log("Branding computed info: " + configline);
            }
        } catch (IOException ex) {
            throw new BuildException("propertiesFile for release cannot be read");
        }
    }

// add attribute for xml building apidoc enhancement
    private void populatexml(Element releasesxml, ReleaseInfo releaseInfo) throws DOMException {
        releasesxml.setAttribute("year", Integer.toString(releaseInfo.releaseDate.getYear()));
        releasesxml.setAttribute("month", Integer.toString(releaseInfo.releaseDate.getMonthValue()));
        releasesxml.setAttribute("day", Integer.toString(releaseInfo.releaseDate.getDayOfMonth()));
        releasesxml.setAttribute("tlp", releaseInfo.maturity);
        releasesxml.setAttribute("position", Integer.toString(releaseInfo.position));
        releasesxml.setAttribute("version", releaseInfo.version);
        releasesxml.setAttribute("apidocurl", releaseInfo.apidocurl);
        releasesxml.setAttribute("pubapidoc", Boolean.toString(releaseInfo.publishapi));
    }

    private ReleaseInfo manageRelease(String key, Object arelease) {
        ReleaseInfo ri = new ReleaseInfo(key);
        // mandatory element
        JSONObject jsonrelease = (JSONObject) arelease;
        ri.setPosition(Integer.parseInt((String) getJSONInfo(jsonrelease, "position", "Order of release starting")));
        // previous release date
        JSONObject previousrelease = (JSONObject) getJSONInfo(jsonrelease, "previousreleasedate", "Apidoc: Date of previous Release");
        ri.setPreviousRelease(
                (String) getJSONInfo(previousrelease, "day", "Apidoc: day of previous Release"),
                (String) getJSONInfo(previousrelease, "month", "Apidoc: month of previous Release"),
                (String) getJSONInfo(previousrelease, "year", "Apidoc: year of previous Release"));
        // date of release
        JSONObject releasedate = (JSONObject) getJSONInfo(jsonrelease, "releasedate", "Apidoc: Date of Release vote");
        ri.setReleaseDate(
                (String) getJSONInfo(releasedate, "day", "Apidoc: day of previous Release"),
                (String) getJSONInfo(releasedate, "month", "Apidoc: month of previous Release"),
                (String) getJSONInfo(releasedate, "year", "Apidoc: year of previous Release"));

        // tlp or not
        ri.setMaturity((String) getJSONInfo(jsonrelease, "tlp", "Statut of release - TLP or not"));
        // version name
        ri.setVersion((String) getJSONInfo(jsonrelease, "versionName", "Version name"));
        ri.setApidocurl((String) getJSONInfo(jsonrelease, "apidocurl", "Apidoc: URL"));
        ri.setJavaApiDocurl((String) getJSONInfo(jsonrelease, "jdk_apidoc", "Apidoc: javadoc for java jdk"));
        ri.setUpdateUrl((String) getJSONInfo(jsonrelease, "update_url", "Update catalog"));
        ri.setPluginsUrl((String) getJSONInfo(jsonrelease, "plugin_url", "Plugin URL"));
        //
        ri.setPublishApi(Boolean.parseBoolean((String) getJSONInfo(jsonrelease, "publish_apidoc", "Should we publish this Apidoc")));
        // optional section
        JSONObject milestone = (JSONObject) jsonrelease.get("milestones");
        if (milestone != null) {
            for (Object object : milestone.keySet()) {
                // ri.add(manageRelease(object.toString(), releaseList.get(object)));
                JSONObject milestonedata = (JSONObject) milestone.get(object);
                MileStone m = new MileStone((String) object);
                // mandatory
                m.setPosition(Integer.parseInt((String) getJSONInfo(milestonedata, "position", "Order of milestone in release")));
                // optional
                Object vote = milestonedata.get("vote");
                if (vote != null) {
                    m.setVote(Integer.parseInt((String) vote));
                }
                m.setVersion((String) milestonedata.get("version"));
                ri.addMileStone(m);
            }
            Collections.sort(ri.milestones);
        }

        return ri;
    }

    private static String makeDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US);
        return date.format(formatter);
    }

    private static String makeAtomDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return date.format(formatter);
    }

    private Object getJSONInfo(JSONObject json, String key, String info) {
        Object result = json.get(key);
        if (result == null) {
            throw new BuildException("Cannot retrieve key " + key + ", this is for" + info);
        }
        return result;
    }

    private static class MileStone implements Comparable<MileStone> {

        private int position;
        private int vote = -1;
        private final String hash;
        private String version;

        public MileStone(String hash) {
            this.hash = hash;
        }

        @Override
        public int compareTo(MileStone o) {
            return (this.position - o.position);
        }

        private void setPosition(int position) {
            this.position = position;
        }

        private void setVote(int vote) {
            this.vote = vote;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "(" + hash + "," + position + "," + vote + "," + version + ")";
        }
    }

    /**
     *
     * Comparable on position. Avoir randomness.
     */
    private static class ReleaseInfo implements Comparable<ReleaseInfo> {

        private int position;
        private final String key;
        private LocalDateTime releaseDate;
        private LocalDateTime previousReleaseDate;
        private String maturity;
        private String version;
        private String apidocurl;
        private String javaapidocurl;
        private String updateurl;
        private String pluginsurl;
        private boolean publishapi;
        private List<MileStone> milestones;

        public ReleaseInfo(String key) {
            this.key = key;
            this.milestones = new ArrayList<>();
        }

        @Override
        public int compareTo(ReleaseInfo o) {
            return (this.position - o.position);
        }

        private void setPosition(int position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "(" + key + "," + position + ")";
        }

        private String getKey() {
            return key;
        }

        private LocalDateTime setDate(String day, String month, String year) {
            LocalDateTime tmp = LocalDateTime.now();
            try {
                tmp = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 12, 0);
            } catch (NumberFormatException e) {
            }
            return tmp;
        }

        private void setPreviousRelease(String day, String month, String year) {
            previousReleaseDate = setDate(day, month, year);
        }

        private void setReleaseDate(String day, String month, String year) {
            releaseDate = setDate(day, month, year);
        }

        private void setMaturity(String tlp) {
            this.maturity = tlp;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        private void setApidocurl(String apidocurl) {
            this.apidocurl = apidocurl;
        }

        private void setJavaApiDocurl(String javaapidocurl) {
            this.javaapidocurl = javaapidocurl;
        }

        private void setUpdateUrl(String url) {
            this.updateurl = url;
        }

        private void setPluginsUrl(String url) {
            this.pluginsurl = url;
        }

        private void addMileStone(MileStone milestone) {
            this.milestones.add(milestone);
        }

        private void setPublishApi(boolean publishok) {
            this.publishapi = publishok;
        }

    }

}
