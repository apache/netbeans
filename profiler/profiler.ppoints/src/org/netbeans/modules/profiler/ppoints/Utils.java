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

package org.netbeans.modules.profiler.ppoints;

import javax.swing.Icon;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer;
import org.netbeans.modules.profiler.api.*;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "Utils_CannotOpenSourceMsg=Cannot show profiling point in source.\nCheck profiling point location.",
    "Utils_InvalidPPLocationMsg=<html><b>Invalid location of {0}.</b><br><br>Location of the profiling point does not seem to be valid.<br>Make sure it points inside method definition, otherwise<br>the profiling point will not be hit during profiling.</html>"
})
public class Utils {

    private static class ProfilingPointPresenterListRenderer extends DefaultListCellRenderer {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            renderer.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 5));

            if (value instanceof ProfilingPoint) {
                boolean enabled = ((ProfilingPoint) value).isEnabled();
                renderer.setText(((ProfilingPoint) value).getName());
                renderer.setIcon(enabled ? ((ProfilingPoint) value).getFactory().getIcon() :
                                           ((ProfilingPoint) value).getFactory().getDisabledIcon());
                renderer.setEnabled(enabled);
            } else if (value instanceof ProfilingPointFactory) {
                renderer.setText(((ProfilingPointFactory) value).getType());
                renderer.setIcon(((ProfilingPointFactory) value).getIcon());
                renderer.setEnabled(true);
            } else {
                renderer.setIcon(null);
                renderer.setEnabled(true);
            }

            return renderer;
        }
    }

    private static class ProfilingPointPresenterRenderer extends LabelRenderer  {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ProfilingPointPresenterRenderer() {
            //      setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 5)); // TODO: enable once Scope is implemented
            setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 5));
        }

        public void setValue(Object value, int row) {
            if (value instanceof ProfilingPoint) {
                ProfilingPoint ppoint = (ProfilingPoint)value;
                boolean enabled = ppoint.isEnabled();
                setText(ppoint.getName());
                setIcon(enabled ? ppoint.getFactory().getIcon() : ppoint.getFactory().getDisabledIcon());
                setEnabled(enabled);
            } else if (value instanceof ProfilingPointFactory) {
                ProfilingPointFactory factory = (ProfilingPointFactory)value;
                setText(factory.getType());
                setIcon(factory.getIcon());
                setEnabled(true);
            }
        }
    }

    private static class ProfilingPointScopeRenderer extends LabelRenderer {
        //~ Constructors ---------------------------------------------------------------------------------------------------------
        
        private Integer scope;

        public ProfilingPointScopeRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        }
        
        public void setValue(Object value, int row) {            
            Icon icon = null;

            if (value instanceof ProfilingPoint) {
                ProfilingPoint ppoint = (ProfilingPoint)value;
                ProfilingPointFactory factory = ppoint.getFactory();
                icon = factory.getScopeIcon();
                scope = factory.getScope();
                setEnabled(ppoint.isEnabled());
            } else if (value instanceof ProfilingPointFactory) {
                ProfilingPointFactory factory = (ProfilingPointFactory)value;
                icon = factory.getScopeIcon();
                scope = factory.getScope();
                setEnabled(true);
            }
            
            setText(""); // NOI18N
            setIcon(isEnabled() ? icon : disabledIcon(icon));
        }
        
        public String toString() {
            return Integer.toString(scope);
        }
    }
    
    private static Icon disabledIcon(Icon icon) {
        return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)icon).getImage()));
    }

    private static class ProjectPresenterListRenderer extends DefaultListCellRenderer {
        //~ Inner Classes --------------------------------------------------------------------------------------------------------

        private static class Renderer extends DefaultListCellRenderer {
            //~ Methods ----------------------------------------------------------------------------------------------------------

            public void setFont(Font font) {
            }

            public void setFontEx(Font font) {
                super.setFont(font);
            }
        }

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Renderer renderer = new Renderer();
        private boolean firstFontSet = false;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel rendererOrig = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            renderer.setComponentOrientation(rendererOrig.getComponentOrientation());
            renderer.setFontEx(rendererOrig.getFont());
            renderer.setOpaque(rendererOrig.isOpaque());
            renderer.setForeground(rendererOrig.getForeground());
            renderer.setBackground(rendererOrig.getBackground());
            renderer.setEnabled(rendererOrig.isEnabled());
            renderer.setBorder(rendererOrig.getBorder());

            if (value instanceof Lookup.Provider) {
                renderer.setText(ProjectUtilities.getDisplayName((Lookup.Provider)value));
                renderer.setIcon(ProjectUtilities.getIcon((Lookup.Provider)value));

                if (ProjectUtilities.getMainProject() == value) {
                    renderer.setFontEx(renderer.getFont().deriveFont(Font.BOLD)); // bold for main project
                } else {
                    renderer.setFontEx(renderer.getFont().deriveFont(Font.PLAIN));
                }
            } else {
                renderer.setText(rendererOrig.getText());
                renderer.setIcon(EMPTY_ICON);
            }

            return renderer;
        }
    }

    private static class ProjectPresenterRenderer extends LabelRenderer {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Font font;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ProjectPresenterRenderer() {
            setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
            font = getFont();
        }

        public void setValue(Object value, int row) {
            
            if (value instanceof Lookup.Provider || value instanceof ProfilingPoint) {
                if (value instanceof ProfilingPoint) {
                    ProfilingPoint ppoint = (ProfilingPoint)value;
                    value = ppoint.getProject();
                    setEnabled(ppoint.isEnabled());
                } else {
                    setEnabled(true);
                }
                
                Lookup.Provider project = (Lookup.Provider)value;
                setText(ProjectUtilities.getDisplayName(project));
                Icon icon = ProjectUtilities.getIcon(project);
                setIcon(isEnabled() ? icon : disabledIcon(icon));
                setFont(Objects.equals(ProjectUtilities.getMainProject(), value) ? font.deriveFont(Font.BOLD) : font); // bold for main project
            }
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String PROJECT_DIRECTORY_MARK = "{$projectDirectory}"; // NOI18N

    // TODO: Move to more "universal" location
    public static final ImageIcon EMPTY_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/profiler/resources/empty16.gif", false); // NOI18N
    private static final ProfilerRenderer projectRenderer = new ProjectPresenterRenderer();
    private static final ProjectPresenterListRenderer projectListRenderer = new ProjectPresenterListRenderer();
    private static final ProfilerRenderer scopeRenderer = new ProfilingPointScopeRenderer();
    private static final ProfilerRenderer presenterRenderer = new ProfilingPointPresenterRenderer();
    private static final ProfilingPointPresenterListRenderer presenterListRenderer = new ProfilingPointPresenterListRenderer();
    private static final DateFormat fullDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private static final DateFormat todayDateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    private static final DateFormat todayDateFormatHiRes = new SimpleDateFormat("HH:mm:ss.SSS"); // NOI118N
    private static final DateFormat dayDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static String getAbsolutePath(Lookup.Provider project, String sourceFileRelativePath) {
        if (project == null) { // no project context for file

            File file = new File(sourceFileRelativePath);

            return file.exists() ? sourceFileRelativePath : null; // return sourceFileRelativePath if absolute path, null otherwise
        }

        return new File(sourceFileRelativePath.replace(PROJECT_DIRECTORY_MARK,
                                                       FileUtil.toFile(ProjectUtilities.getProjectDirectory(project)).getAbsolutePath()))
                                                                                                                                                                                                                                                                                                                                                                       .getAbsolutePath(); // expand relative path to absolute
    }

    public static String getClassName(CodeProfilingPoint.Location location) {
        File file = FileUtil.normalizeFile(new File(location.getFile()));
        FileObject fileObject = FileUtil.toFileObject(file);

        if ((fileObject == null) || !fileObject.isValid()) {
            return null;
        }

        int documentOffset = getDocumentOffset(location);

        if (documentOffset == -1) {
            return null;
        }
        // FIXME - optimize
        JavaProfilerSource src = JavaProfilerSource.createFrom(fileObject);
        if (src == null) return null;
        SourceClassInfo sci = src.getEnclosingClass(documentOffset);
        if (sci == null) return null;
        return sci.getQualifiedName();
    }
    
    public static String getMethodName(CodeProfilingPoint.Location location) {
        File file = FileUtil.normalizeFile(new File(location.getFile()));
        FileObject fileObject = FileUtil.toFileObject(file);

        if ((fileObject == null) || !fileObject.isValid()) {
            return null;
        }

        int documentOffset = getDocumentOffset(location);

        if (documentOffset == -1) {
            return null;
        }
        // FIXME - optimize
        JavaProfilerSource src = JavaProfilerSource.createFrom(fileObject);
        if (src == null) return null;
        SourceMethodInfo smi = src.getEnclosingMethod(documentOffset);
        if (smi == null) return null;
        return smi.getName();
    }

    public static CodeProfilingPoint.Location getCurrentLocation(int lineOffset) {
        EditorContext mostActiveContext = EditorSupport.getMostActiveJavaEditorContext();

        if (mostActiveContext == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        FileObject mostActiveJavaSource = mostActiveContext.getFileObject();

        if (mostActiveJavaSource == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        File currentFile = FileUtil.toFile(mostActiveJavaSource);

        if (currentFile == null) {
            return CodeProfilingPoint.Location.EMPTY; // Happens for AbstractFileObject, for example JDK classes
        }

        String fileName = currentFile.getAbsolutePath();

        int lineNumber = EditorSupport.getLineForOffset(mostActiveJavaSource, mostActiveContext.getTextComponent().getCaret().getDot()) + 1;

        if (lineNumber == -1) {
            lineNumber = 1;
        }

        return new CodeProfilingPoint.Location(fileName, lineNumber,
                                               lineOffset /* TODO: get real line offset if lineOffset isn't OFFSET_START nor OFFSET_END */);
    }

    public static Lookup.Provider getCurrentProject() {
        Lookup.Provider currentProject = getMostActiveJavaProject();

        if (currentProject == null) {
            currentProject = ProjectUtilities.getMainProject();
        }

        return currentProject;
    }

    public static CodeProfilingPoint.Location getCurrentSelectionEndLocation(int lineOffset) {
        EditorContext mostActiveContext = EditorSupport.getMostActiveJavaEditorContext();

        if (mostActiveContext == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        FileObject mostActiveJavaSource = mostActiveContext.getFileObject();

        if (mostActiveJavaSource == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        JTextComponent mostActiveTextComponent = mostActiveContext.getTextComponent();

        if (mostActiveTextComponent.getSelectedText() == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        String fileName = FileUtil.toFile(mostActiveJavaSource).getAbsolutePath();
        int lineNumber = EditorSupport.getLineForOffset(mostActiveJavaSource, mostActiveTextComponent.getSelectionEnd()) + 1;

        if (lineNumber == -1) {
            lineNumber = 1;
        }

        return new CodeProfilingPoint.Location(fileName, lineNumber,
                                               lineOffset /* TODO: get real line offset if lineOffset isn't OFFSET_START nor OFFSET_END */);
    }

    public static CodeProfilingPoint.Location[] getCurrentSelectionLocations() {
        EditorContext mostActiveContext = EditorSupport.getMostActiveJavaEditorContext();

        if (mostActiveContext == null) {
            return new CodeProfilingPoint.Location[0];
        }

        FileObject mostActiveJavaSource = mostActiveContext.getFileObject();

        if (mostActiveJavaSource == null) {
            return new CodeProfilingPoint.Location[0];
        }

        JTextComponent mostActiveTextComponent = mostActiveContext.getTextComponent();

        if (mostActiveTextComponent.getSelectedText() == null) {
            return new CodeProfilingPoint.Location[0];
        }

        File file = FileUtil.toFile(mostActiveJavaSource);

        if (file == null) {
            return new CodeProfilingPoint.Location[0]; // Most likely Java source
        }

        String fileName = file.getAbsolutePath();

        int startLineNumber = EditorSupport.getLineForOffset(mostActiveJavaSource, mostActiveTextComponent.getSelectionStart()) + 1;

        if (startLineNumber == -1) {
            startLineNumber = 1;
        }

        // #211681
        int endLineNumber = EditorSupport.getLineForOffset(mostActiveJavaSource, mostActiveTextComponent.getSelectionEnd() - 1) + 1;
        endLineNumber = Math.max(startLineNumber, endLineNumber);

        return new CodeProfilingPoint.Location[] {
                   new CodeProfilingPoint.Location(fileName, startLineNumber,
                                                   CodeProfilingPoint.Location.OFFSET_START /* TODO: get real line offset if lineOffset isn't OFFSET_START nor OFFSET_END */),
                   new CodeProfilingPoint.Location(fileName, endLineNumber,
                                                   CodeProfilingPoint.Location.OFFSET_END /* TODO: get real line offset if lineOffset isn't OFFSET_START nor OFFSET_END */)
               };
    }

    public static CodeProfilingPoint.Location getCurrentSelectionStartLocation(int lineOffset) {
        EditorContext mostActiveContext = EditorSupport.getMostActiveJavaEditorContext();

        if (mostActiveContext == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        FileObject mostActiveJavaSource = mostActiveContext.getFileObject();

        if (mostActiveJavaSource == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        JTextComponent mostActiveTextComponent = mostActiveContext.getTextComponent();

        if (mostActiveTextComponent.getSelectedText() == null) {
            return CodeProfilingPoint.Location.EMPTY;
        }

        String fileName = FileUtil.toFile(mostActiveJavaSource).getAbsolutePath();
        int lineNumber = EditorSupport.getLineForOffset(mostActiveJavaSource, mostActiveTextComponent.getSelectionStart()) + 1;

        if (lineNumber == -1) {
            lineNumber = 1;
        }

        return new CodeProfilingPoint.Location(fileName, lineNumber,
                                               lineOffset /* TODO: get real line offset if lineOffset isn't OFFSET_START nor OFFSET_END */);
    }

    public static int getDocumentOffset(CodeProfilingPoint.Location location) {
        File file = FileUtil.normalizeFile(new File(location.getFile()));
        FileObject fileObject = FileUtil.toFileObject(file);

        if ((fileObject == null) || !fileObject.isValid()) return -1;

        int linePosition = EditorSupport.getOffsetForLine(fileObject, location.getLine() - 1); // Line is 1-based, needs to be 0-based
        if (linePosition == -1) return -1;
        
        int lineOffset;
        if (location.isLineStart()) {
            lineOffset = 0;
        } else if (location.isLineEnd()) {
            lineOffset = EditorSupport.getOffsetForLine(fileObject, location.getLine()) - linePosition - 1; // TODO: workaround to get line length, could fail at the end of last line!!!
            if (lineOffset == -1) return -1;
        } else {
            lineOffset = location.getOffset();
        }

        return linePosition + lineOffset;
    }

    public static double getDurationInMicroSec(long startTimestamp, long endTimestamp) {
        ProfilingSessionStatus session = Profiler.getDefault().getTargetAppRunner().getProfilingSessionStatus();
        double countsInMicroSec = session.timerCountsInSecond[0] / 1000000D;

        return (endTimestamp - startTimestamp) / countsInMicroSec;
    }

    //  public static DataObject getDataObject(CodeProfilingPoint.Location location) {
    //    // URL
    //    String url = location.getFile();
    //
    //    // FileObject
    //    FileObject file = null;
    //    try {
    //      file = URLMapper.findFileObject(new File(url).toURI().toURL());
    //    } catch (MalformedURLException e) {}
    //    if (file == null) return null;
    //
    //    // DataObject
    //    DataObject dao = null;
    //    try {
    //      dao = DataObject.find(file);
    //    } catch (DataObjectNotFoundException ex) {}
    //
    //    return dao;
    //  }
    public static Line getEditorLine(CodeProfilingPoint profilingPoint, CodeProfilingPoint.Annotation annotation) {
        return getEditorLine(profilingPoint.getLocation(annotation));
    }    
        
    public static Line getEditorLine(CodeProfilingPoint.Location location) {
        if (location == null) {
            return null;
        }
        
        // URL
        String url = location.getFile();
        if (url == null) {
            return null;
        }

        // FileObject
        FileObject file = null;

        try {
            file = URLMapper.findFileObject(new File(url).toURI().toURL());
        } catch (MalformedURLException e) {
        }

        if (file == null) {
            return null;
        }

        // DataObject
        DataObject dao = null;

        try {
            dao = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }

        // LineCookie of pp
        LineCookie lineCookie = (LineCookie) dao.getCookie(LineCookie.class);

        if (lineCookie == null) {
            return null;
        }

        // Line.Set of pp - real line where pp is defined
        Line.Set lineSet = lineCookie.getLineSet();

        if (lineSet == null) {
            return null;
        }

        try {
            return lineSet.getCurrent(location.getLine() - 1); // Line is 1-based, needs to be 0-based for Line.Set
        } catch (Exception e) {
        }

        return null;
    }
    
    public static boolean isValidLocation(CodeProfilingPoint.Location location) {
        // Fail if location not in method
        String methodName = Utils.getMethodName(location);
        if (methodName == null) return false;
        
        // Succeed if location in method body
        if (location.isLineStart()) return true;
        else if (location.isLineEnd()) {
            CodeProfilingPoint.Location startLocation = new CodeProfilingPoint.Location(
                    location.getFile(), location.getLine(), CodeProfilingPoint.Location.OFFSET_START);
            if (methodName.equals(Utils.getMethodName(startLocation))) return true;
        }

        Line line = getEditorLine(location); 
        if (line == null) return false;
        
        // #211135, line.getText() returns null for closed documents
        String lineText = line.getText();
        if (lineText == null) return false;
        
        // Fail if location immediately after method declaration - JUST A BEST GUESS!
        lineText = lineText.trim();
        if (lineText.endsWith("{") && lineText.indexOf('{') == lineText.lastIndexOf('{')) return false; // NOI18N
        
        return true;
    }
    
    public static void checkLocation(final CodeProfilingPoint.Single ppoint) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (!isValidLocation(ppoint.getLocation()))
                    ProfilerDialogs.displayWarning(
                            Bundle.Utils_InvalidPPLocationMsg(ppoint.getName()));
            }
        });
    }
    
    public static void checkLocation(final CodeProfilingPoint.Paired ppoint) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (!isValidLocation(ppoint.getStartLocation()))
                    ProfilerDialogs.displayWarning(
                            Bundle.Utils_InvalidPPLocationMsg(ppoint.getName()));
                else if (ppoint.usesEndLocation() && !isValidLocation(ppoint.getEndLocation()))
                    ProfilerDialogs.displayWarning(
                            Bundle.Utils_InvalidPPLocationMsg(ppoint.getName()));
            }
        });
    }

    public static Lookup.Provider getMostActiveJavaProject() {
        EditorContext mostActiveContext = EditorSupport.getMostActiveJavaEditorContext();

        if (mostActiveContext == null) {
            return null;
        }

        FileObject mostActiveFileObject = mostActiveContext.getFileObject();

        if (mostActiveFileObject == null) {
            return null;
        }

        return ProjectUtilities.getProject(mostActiveFileObject);
    }

    public static ListCellRenderer getPresenterListRenderer() {
        return presenterListRenderer;
    }

    public static ProfilerRenderer getPresenterRenderer() {
        return presenterRenderer;
    }

    public static CodeProfilingPoint[] getProfilingPointsOnLine(CodeProfilingPoint.Location location) {
        if ((location == null) || (location == CodeProfilingPoint.Location.EMPTY)) {
            return new CodeProfilingPoint[0];
        }

        File file = new File(location.getFile());
        int lineNumber = location.getLine();

        List<CodeProfilingPoint> lineProfilingPoints = new ArrayList<>();
        List<CodeProfilingPoint> profilingPoints = ProfilingPointsManager.getDefault()
                                                                         .getProfilingPoints(CodeProfilingPoint.class, null, false);

        for (CodeProfilingPoint profilingPoint : profilingPoints) {
            for (CodeProfilingPoint.Annotation annotation : profilingPoint.getAnnotations()) {
                CodeProfilingPoint.Location loc = profilingPoint.getLocation(annotation);

                if ((loc.getLine() == lineNumber) && new File(loc.getFile()).equals(file)) {
                    lineProfilingPoints.add(profilingPoint);

                    break;
                }
            }
        }

        return lineProfilingPoints.toArray(new CodeProfilingPoint[0]);
    }

    // TODO: should be moved to ProjectUtilities
    public static ListCellRenderer getProjectListRenderer() {
        return projectListRenderer;
    }

    // TODO: should be moved to ProjectUtilities
    public static ProfilerRenderer getProjectRenderer() {
        return projectRenderer;
    }

    public static String getRelativePath(Lookup.Provider project, String sourceFileAbsolutePath) {
        if (project == null) {
            return sourceFileAbsolutePath; // no project context for file
        }
        final FileObject projectDirectory = ProjectUtilities.getProjectDirectory(project);
        String projectDirectoryAbsolutePath = FileUtil.toFile(projectDirectory).getAbsolutePath();

        if (!sourceFileAbsolutePath.startsWith(projectDirectoryAbsolutePath)) {
            return sourceFileAbsolutePath; // file not placed in project directory
        }

        File file = FileUtil.normalizeFile(new File(sourceFileAbsolutePath));

        return PROJECT_DIRECTORY_MARK + "/" // NOI18N
               + FileUtil.getRelativePath(projectDirectory, FileUtil.toFileObject(file)); // file placed in project directory => relative path used
    }

    public static ProfilerRenderer getScopeRenderer() {
        return scopeRenderer;
    }

    public static String getThreadClassName(int threadID) {
        // TODO: get the thread class name for RuntimeProfilingPoint.HitEvent.threadId
        return null;
    }

    public static String getThreadName(int threadID) {
        // TODO: get the thread name for RuntimeProfilingPoint.HitEvent.threadId
        return "&lt;unknown thread, id=" + threadID + "&gt;"; // NOI18N (not used)
    }

    public static long getTimeInMillis(final long hiResTimeStamp) {
        ProfilingSessionStatus session = Profiler.getDefault().getTargetAppRunner().getProfilingSessionStatus();
        long statupInCounts = session.startupTimeInCounts;
        long startupMillis = session.startupTimeMillis;
        long countsInMillis = session.timerCountsInSecond[0] / 1000L;

        return startupMillis + ((hiResTimeStamp - statupInCounts) / countsInMillis);
    }

    public static String getUniqueName(String name, String nameSuffix, Lookup.Provider project) {
        List<ProfilingPoint> projectProfilingPoints = ProfilingPointsManager.getDefault().getProfilingPoints(project, false, true);
        Set<String> projectProfilingPointsNames = new HashSet<>();

        for (ProfilingPoint projectProfilingPoint : projectProfilingPoints) {
            projectProfilingPointsNames.add(projectProfilingPoint.getName());
        }

        int index = 0;
        String indexStr = ""; // NOI18N

        while (projectProfilingPointsNames.contains(name + indexStr + nameSuffix)) {
            indexStr = " " + Integer.toString(++index); // NOI18N
        }

        return name + indexStr + nameSuffix;
    }

    public static String formatLocalProfilingPointTime(long timestamp) {
        Date now = new Date();
        Date date = new Date(timestamp);

        if (dayDateFormat.format(now).equals(dayDateFormat.format(date))) {
            return todayDateFormat.format(date);
        } else {
            return fullDateFormat.format(date);
        }
    }

    public static String formatProfilingPointTime(long timestamp) {
        long timestampInMillis = getTimeInMillis(timestamp);
        Date now = new Date();
        Date date = new Date(timestampInMillis);

        if (dayDateFormat.format(now).equals(dayDateFormat.format(date))) {
            return todayDateFormat.format(date);
        } else {
            return fullDateFormat.format(date);
        }
    }

    public static String formatProfilingPointTimeHiRes(long timestamp) {
        long timestampInMillis = getTimeInMillis(timestamp);
        Date now = new Date();
        Date date = new Date(timestampInMillis);

        if (dayDateFormat.format(now).equals(dayDateFormat.format(date))) {
            return todayDateFormatHiRes.format(date);
        } else {
            return todayDateFormatHiRes.format(date)+" "+dayDateFormat.format(date);  // NOI18N
        }
    }
    
    public static Font getTitledBorderFont(TitledBorder tb) {
        Font font = tb.getTitleFont();
        if (font == null) font = UIManager.getFont("TitledBorder.font"); // NOI18N
        if (font == null) font = new JLabel().getFont();
        if (font == null) font = UIManager.getFont("Label.font"); // NOI18N
        return font;
    }

    public static void openLocation(CodeProfilingPoint.Location location) {
        File file = FileUtil.normalizeFile(new File(location.getFile()));
        final FileObject fileObject = FileUtil.toFileObject(file);

        if ((fileObject == null) || !fileObject.isValid()) {
            return;
        }

        final int documentOffset = getDocumentOffset(location);

        if (documentOffset == -1) {
            ProfilerDialogs.displayError(Bundle.Utils_CannotOpenSourceMsg());
            return;
        }

        GoToSource.openFile(fileObject, documentOffset);
    }
    
}
