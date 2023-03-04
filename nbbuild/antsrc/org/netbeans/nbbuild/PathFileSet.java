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

package org.netbeans.nbbuild;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.AbstractSelectorContainer;
import org.apache.tools.ant.types.selectors.AndSelector;
import org.apache.tools.ant.types.selectors.ContainsRegexpSelector;
import org.apache.tools.ant.types.selectors.ContainsSelector;
import org.apache.tools.ant.types.selectors.DateSelector;
import org.apache.tools.ant.types.selectors.DependSelector;
import org.apache.tools.ant.types.selectors.DepthSelector;
import org.apache.tools.ant.types.selectors.DifferentSelector;
import org.apache.tools.ant.types.selectors.ExtendSelector;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.selectors.MajoritySelector;
import org.apache.tools.ant.types.selectors.NoneSelector;
import org.apache.tools.ant.types.selectors.NotSelector;
import org.apache.tools.ant.types.selectors.OrSelector;
import org.apache.tools.ant.types.selectors.PresentSelector;
import org.apache.tools.ant.types.selectors.SelectSelector;
import org.apache.tools.ant.types.selectors.SelectorContainer;
import org.apache.tools.ant.types.selectors.SizeSelector;
import org.apache.tools.ant.types.selectors.TypeSelector;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;

/**
 *
 * @author Richard Michalsky
 */
public class PathFileSet extends DataType implements ResourceCollection, SelectorContainer {
    private Path clusterPath;
    private String include;
    private AbstractSelectorContainer selectors = new AbstractSelectorContainer() {};
    private ArrayList<Resource> files;

    /**
     * Optional include pattern for filtering files. The same as fileset's nested &gt;include&lt; tag.
     *
     * Note that pattern is matched against <b>relative</b> path starting at each pathelement.
     * E.g. when there is file <tt>/a/b/c/d.txt</tt>, it will be included in result
     * of:
     * <pre>&lt;pathfileset include="c/**.txt property="output"&gt;
     *      &lt;path path="/a/b"/&gt;
     * &lt;/pathfileset&gt;</pre>
     * and it won't be included in result of:
     * <pre>&lt;pathfileset include="b/**.txt property="output"&gt;
     *      &lt;path path="/a/b"/&gt;
     * &lt;/pathfileset&gt;</pre>
     * @param include
     */
    public void setInclude(String include) {
        this.include = include;
    }

    private List<Path> paths = new ArrayList<>();

    /**
     * Adds path in classpath notation. Searched in addition
     * to any nested paths.
     * @param stringPath
     */
    public void setPath(Path stringPath) {
        addPath(stringPath);
    }

    /**
     * Elements of nested paths are used as basedirs for fileset.
     * @param clusterPath
     */
    public void addPath(Path clusterPath) {
        paths.add(clusterPath);
    }

    // SelectorContainer impl.

    public Enumeration<FileSelector> selectorElements() {
        return selectors.selectorElements();
    }

    public int selectorCount() {
        return selectors.selectorCount();
    }

    public boolean hasSelectors() {
        return selectors.hasSelectors();
    }

    public FileSelector[] getSelectors(Project p) {
        return selectors.getSelectors(p);
    }

    public void appendSelector(FileSelector selector) {
        selectors.appendSelector(selector);
    }

    public void addType(TypeSelector selector) {
        selectors.addType(selector);
    }

    public void addSize(SizeSelector selector) {
        selectors.addSize(selector);
    }

    public void addSelector(SelectSelector selector) {
        selectors.addSelector(selector);
    }

    public void addPresent(PresentSelector selector) {
        selectors.addPresent(selector);
    }

    public void addOr(OrSelector selector) {
        selectors.addOr(selector);
    }

    public void addNot(NotSelector selector) {
        selectors.addNot(selector);
    }

    public void addNone(NoneSelector selector) {
        selectors.addNone(selector);
    }

    public void addModified(ModifiedSelector selector) {
        selectors.addModified(selector);
    }

    public void addMajority(MajoritySelector selector) {
        selectors.addMajority(selector);
    }

    public void addFilename(FilenameSelector selector) {
        selectors.addFilename(selector);
    }

    public void addDifferent(DifferentSelector selector) {
        selectors.addDifferent(selector);
    }

    public void addDepth(DepthSelector selector) {
        selectors.addDepth(selector);
    }

    public void addDepend(DependSelector selector) {
        selectors.addDepend(selector);
    }

    public void addDate(DateSelector selector) {
        selectors.addDate(selector);
    }

    public void addCustom(ExtendSelector selector) {
        selectors.addCustom(selector);
    }

    public void addContainsRegexp(ContainsRegexpSelector selector) {
        selectors.addContainsRegexp(selector);
    }

    public void addContains(ContainsSelector selector) {
        selectors.addContains(selector);
    }

    public void addAnd(AndSelector selector) {
        selectors.addAnd(selector);
    }

    public void add(FileSelector selector) {
        selectors.add(selector);
    }

    // ResourceCollection impl.

    public Iterator<Resource> iterator() {
        initFiles();
        return files.iterator();
    }

    public int size() {
        initFiles();
        return files.size();
    }

    public boolean isFilesystemOnly() {
        return true;
    }

    private void initFiles() throws BuildException {
        if (files != null) return;
        try {
            files = new ArrayList<>();
            log("ClusterPathSet: scanning " + paths.size() + " paths.", Project.MSG_VERBOSE);
            DirectoryScanner scanner = new DirectoryScanner();
            if (paths.size() == 0) {
                throw new BuildException("No path specified");
            }
            for (Path path : paths) {
                String[] includedClusters = path.list();
                for (String clusterName : includedClusters) {
                    log("ClusterPathSet: scanning pathelement '" + clusterName + "'.", Project.MSG_VERBOSE);
                    scanner.setErrorOnMissingDir(false);
                    scanner.setBasedir(clusterName);
                    scanner.setSelectors(selectors.getSelectors(getProject()));
                    if (include != null) {
                        scanner.setIncludes(new String[]{include});
                    }
                    scanner.scan();
                    log("ClusterPathSet: " + scanner.getIncludedFilesCount() + " files found.", Project.MSG_VERBOSE);
                    for (String relFile : scanner.getIncludedFiles()) {
                        files.add(new FileResource(scanner.getBasedir(), relFile));
                    }
                }
            }
        } catch (BuildException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

}
