/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class PythonPlatform implements Serializable, Comparable<PythonPlatform> {

    private final String id;
    private String name;
    private List<String> pythonPath = new ArrayList<>();
    private List<String> javaPath = new ArrayList<>();;
    private String interpreterCommand;
    private String interpreterConsoleComand;
    private String interpreterArgs;
    private String homeUrl;
    private String sourceLevel;
    private boolean dirty;
    // When adding properties, be sure to update the persistence code in PythonPlatformManager

    transient private List<FileObject> libraryRoots;
    transient private List<FileObject> uniqueLibraryRoots;

    public PythonPlatform(String id) {
        this.id = id;
    }

    public String getInterpreterArgs() {
        return interpreterArgs;
    }

    public void setInterpreterArgs(String interpreterArgs) {
        checkDirty(this.interpreterArgs, interpreterArgs);

        this.interpreterArgs = interpreterArgs;
    }

    public String getInterpreterCommand() {
        return interpreterCommand;
    }

    public void setInterpreterCommand(String interpreterCommand) {
        checkDirty(this.interpreterCommand, interpreterCommand);

        this.interpreterCommand = interpreterCommand;
    }

    public String getInterpreterConsoleComand() {
        return interpreterConsoleComand;
    }

    public void setInterpreterConsoleComand(String interpreterConsoleComand) {
        checkDirty(this.interpreterConsoleComand, interpreterConsoleComand);

        this.interpreterConsoleComand = interpreterConsoleComand;
    }

    public String getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(String sourceLevel) {
        checkDirty(this.sourceLevel, sourceLevel);
        this.sourceLevel = sourceLevel;
    }

    public List<String> getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(List<String> javaPath) {
        checkDirty(this.javaPath, javaPath);
        this.javaPath = new ArrayList<>(javaPath);
    }

    public void setJavaPath(String[] javaPath) {
        // Arrays.asList() creates a readonly list...
        List<String> newPath = new ArrayList<>(javaPath.length);
        for (String s : javaPath) {
            newPath.add(s);
        }

        checkDirty(this.javaPath, newPath);

        this.javaPath = newPath;
    }
    public void addJavaPath(String pathElement){
        dirty = true;
        getJavaPath().add(pathElement);
    }
    public void removeJavaPath(String pathElement){
        dirty = true;
        getJavaPath().remove(pathElement);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkDirty(this.name, name);

        this.name = name;
    }

    /** Internal-only name, usually identical to the name property, but not
     * user visible and in the case of the bundled Jython, something like "default".
     * This means that if your project is set to use the "default" Jython, it will
     * use whatever Jython is bundled (the names may change as we rev builds).
     */
    public String getId() {
        return id;
    }

    public List<String> getPythonPath() {
        return pythonPath;
    }

    public void setPythonPath(List<String> pythonPath) {
        checkDirty(this.pythonPath, pythonPath);
        this.pythonPath = new ArrayList<>(pythonPath); // defensive copy
    }

    void setPythonPath(String[] pythonPath) {
        // Arrays.asList() creates a readonly list...
        List<String> newPath = new ArrayList<>(pythonPath.length);
        for (String s : pythonPath) {
            newPath.add(s);
        }

        checkDirty(this.pythonPath, newPath);

        this.pythonPath = newPath;
    }
    public void addPythonPath(String pathElement){
        dirty = true;
        getPythonPath().add(pathElement);
    }

    public void removePythonPath(String pathElement){
        dirty = true;
        getPythonPath().remove(pathElement);
    }
//    public void moveUpPythonPath(String pathElement){
//        getPythonPath().indexOf(pathElement);
//    }
//    public void moveDownPythonPath(String pathElement){
//        getPythonPath().indexOf(pathElement);
//    }
    /**
     *Build a path string from arraylist
     * @param path
     * @return
     */
    public static String buildPath(List<String> path){
        StringBuilder pathString = new StringBuilder();
        int count = 0;
        for(String pathEle: path){
            pathString.append(pathEle);
            if (count++ < path.size()){
                pathString.append(File.pathSeparator);
            }
        }
        return pathString.toString();
    }

    void addPythonPath(String[] pathElements) {
        dirty = true;
        for (String pathElement : pathElements) {
            addPythonPath(pathElement);
        }
    }
    void addJavaPath(String[] pathElements) {
        dirty = true;
        for (String pathElement : pathElements) {
            addJavaPath(pathElement);
        }
    }

    public String getHomeUrl() {
        if (homeUrl == null) {
            try {
                String cmd = getInterpreterCommand();
                if (cmd != null) {
                    File binDir = new File(cmd).getParentFile();
                    if (binDir != null) {
                        File homeDir = binDir.getParentFile();
                        if (homeDir != null) {
                            homeDir = homeDir.getCanonicalFile();
                            homeUrl = homeDir.toURI().toURL().toExternalForm();
                        }
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return homeUrl;
    }

    /**
     * List of roots on library path; SORTED SUCH THAT SUB DIRECTORIES ARE LISTED BEFORE DIRECTORIES!
     *
     * In other words, if /tmp and /tmp/foo are both in the list, then /tmp/foo will appear before
     * /tmp. Thus, you can find the closest root to a file by searching from the beginning.
     */
    public List<FileObject> getLibraryRoots() {
        initRoots();
        return libraryRoots;
    }

    /**
     * Same as {@link getLibraryRoots()}, but this will only return the topmost file objects
     * instead of all the various nested file objects. Used to more quickly determine if a platform
     * can contain a given path.
     */
    public List<FileObject> getUniqueLibraryRoots() {
        initRoots();

        // Issue 186265
        if (uniqueLibraryRoots == null) {
            uniqueLibraryRoots = Collections.<FileObject>emptyList();
        }

        return uniqueLibraryRoots;
    }

    public List<URL> getUrls() {
        List<URL> urls = new ArrayList<>();

        for (FileObject root : getUniqueLibraryRoots()) {
            try {
                urls.add(root.getURL());
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return urls;
    }

    private void initRoots() {
        if (uniqueLibraryRoots != null) {
            // Already initialized
            return;
        }

        libraryRoots = new ArrayList<>();
        List<String> sortedPath = new ArrayList<>(pythonPath);
        // Ensure that subdirectories appear first: sort in reverse order.
        Collections.sort(sortedPath);
        Collections.reverse(sortedPath);

        for (String item : sortedPath) {
            File file = new File(item);
            if (file.getName().equals("python")) { // NOI18N
                // For some reason, Jython's load path includes our python NetBeans cluster directory.... why?
                // We shouldn't include it here, since pythonstubs is added manually.
                continue;
            }
            if (file.exists() && file.isDirectory()) { // There are some bogus items in some cases
                FileObject fo = FileUtil.toFileObject(file);
                if (fo != null) {
                    libraryRoots.add(fo);
                }
            }
        }

        List<FileObject> unique = Util.findUniqueRoots(libraryRoots);
        uniqueLibraryRoots = new ArrayList<>(unique);
    }

    public boolean isValid() {
        return new File(interpreterCommand).exists();
    }

    @Override
    public String toString() {
        return super.toString() + ":" + name + "; id=" + id + ",dirty=" + dirty + ",interpreter=" + interpreterCommand;
    }

    /** Has this platform been changed since the last load? */
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    // Implements Comparable
    @Override
    public int compareTo(PythonPlatform other) {
        // Sort default entry to the top
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        String defaultId = manager.getDefaultPlatform();
        if (id.equals(defaultId)) {
            return -1;
        } else if (other.id.equals(defaultId)) {
            return 1;
        }

        // Sort by name
        return name.compareTo(other.name);
    }

    private void checkDirty(String s1, String s2) {
        if (!stringEquals(s1, s2)) {
            dirty = true;
        }
    }

    private void checkDirty(List<String> l1, List<String> l2) {
        if (!listEquals(l1, l2)) {
            dirty = true;
        }
    }

    /**
     * Check to see if two strings are equal. Consider null and empty strings
     * identical.
     */
    private boolean stringEquals(String s1, String s2) {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        return s1.equals(s2);
    }

    /**
     * Check two string lists to see if they are identical.
     */
    private boolean listEquals(List<String> l1, List<String> l2) {
        if (l1 == null) {
            return l2 == null;
        }
        if (l2 == null) {
            return false;
        }
        if (l1.size() != l2.size()) {
            return false;
        }
        return l1.toString().equals(l2.toString());
    }

    /**
     * Tries to find a {@link PythonPlatform platform} for a given project. Might
     * return <tt>null</tt>.
     */
    public static PythonPlatform platformFor(final Project project) {
        PythonPlatformProvider rpp = project.getLookup().lookup(PythonPlatformProvider.class);
        return rpp == null ? null : rpp.getPlatform();
    }
}
