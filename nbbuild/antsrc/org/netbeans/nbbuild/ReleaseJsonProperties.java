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
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

    /**
     * current branch we works with
     */
    private String branch;
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
        try ( FileReader reader = new FileReader(jsonreleaseinfoFile)) {
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
            Element releasexml = (Element) releasesxml.appendChild(doc.createElement("release"));
            populatexml(releasexml, releaseInfo);
            if (releaseInfo.getKey().equals(branch)) {
                requiredbranchinfo = releaseInfo;
                releasesxml.setAttribute("position", Integer.toString(releaseInfo.position));
                // attribute to know position of the requested current branch in the set of release
            }
        }

        if (requiredbranchinfo == null) {
            throw new BuildException("No Release Information found for branch '" + branch + "', update json file section");
        }
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
        try ( OutputStream config = new FileOutputStream(propertiesFile)) {
            config.write(("metabuild.DistributionURL=" + requiredbranchinfo.updateurl + "\n").getBytes());
            config.write(("metabuild.PluginPortalURL=" + requiredbranchinfo.pluginsurl + "\n").getBytes());
            config.write(("metabuild.RawVersion=" + requiredbranchinfo.version + "\n").getBytes());

            if (branch.equals("master")) {
                config.write(("metabuild.ComputedSplashVersion=DEV (Build {0})\n").getBytes());
                config.write(("metabuild.ComputedTitleVersion=DEV {0}\n").getBytes());
                config.write(("metabuild.logcli=-J-Dnetbeans.logger.console=true -J-ea\n").getBytes());
            } else {
                config.write(("metabuild.ComputedSplashVersion=" + requiredbranchinfo.version + "})\n").getBytes());
                config.write(("metabuild.ComputedTitleVersion=" + requiredbranchinfo.version + "\n").getBytes());
                config.write(("metabuild.logcli=\n").getBytes());
            }
        } catch (IOException ex) {
            throw new BuildException("Properties File for release cannot be created");
        }

        log("Writing releasinfo file " + xmlFile);

        xmlFile.getParentFile().mkdirs();
        try ( OutputStream config = new FileOutputStream(xmlFile)) {
            XMLUtil.write(doc, config);
        } catch (IOException ex) {
            throw new BuildException("XML File for release cannot be created");
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
    }

    private ReleaseInfo manageRelease(String key, Object arelease) {
        ReleaseInfo ri = new ReleaseInfo(key);
        JSONObject jsonrelease = (JSONObject) arelease;
        ri.setPosition(Integer.parseInt((String) jsonrelease.get("position")));
        JSONObject previousrelease = (JSONObject) jsonrelease.get("previousreleasedate");
        ri.setPreviousRelease((String) previousrelease.get("day"), (String) previousrelease.get("month"), (String) previousrelease.get("year"));
        JSONObject releasedate = (JSONObject) jsonrelease.get("releasedate");
        ri.setReleaseDate((String) releasedate.get("day"), (String) releasedate.get("month"), (String) releasedate.get("year"));
        ri.setMaturity((String) jsonrelease.get("tlp"));
        ri.setVersion((String) jsonrelease.get("versionName"));
        ri.setApidocurl((String) jsonrelease.get("apidocurl"));
        ri.setJavaApiDocurl((String) jsonrelease.get("jdk_apidoc"));
        ri.setUpdateUrl((String) jsonrelease.get("update_url"));
        ri.setPluginsUrl((String) jsonrelease.get("plugin_url"));
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

        public ReleaseInfo(String key) {
            this.key = key;
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

        private void setPreviousRelease(String day, String month, String year) {
            try {

                previousReleaseDate = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 12, 0);
            } catch (NumberFormatException e) {
                // not a date, use now we should be on master
                previousReleaseDate = LocalDateTime.now();
            }
        }

        private void setReleaseDate(String day, String month, String year) {
            try {
                releaseDate = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 12, 0);
            } catch (NumberFormatException e) {
                // not a date, use now we should be on master
                releaseDate = LocalDateTime.now();
            }
        }

        private void setMaturity(String tlp) {
            this.maturity = tlp;
        }

        private void setVersion(String version) {
            if (version.equals("-")) {
                this.version = "dev";
            } else {
                this.version = version;
            }
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

    }

}
