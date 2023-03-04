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

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

/**
 * Task for copying out icons from NetBeans projects. It creates paralel directory 
 * structure in destdir with only copied icons. It creates index.html page that 
 * lists all found icons and group them according to nb modules. Icons are considered 
 * images with dimensions 8x8, 16x16, 24x24, 32x32 of type PNG or GIF. 
 * Inclusion filter for images in each module is given by attribute 'iconincludes', 
 * default inclusion filter is all gifs and pngs under src folder. Exclusion filter
 * is defined by attribute 'iconexcludes'. Filters are regular ant inclusion and 
 * exclusion filters. Task tries to find all nb modules up to depth defined by 
 * 'depth' attribute (default is 4) in the nbsrcroot. Modules without icons are
 * not listed by default, they can be shown by setting attribute 'showempty' to true.
 * 
 * Required attributes are:
 *   nbsrcroot ... root of NetBeans Hg checkout
 *   destdir   ... dir for copying icons
 * 
 * @author Milan Kubec
 */
public class CopyIcons extends MatchingTask {
    
    private int depth = 0;
    private List<File> projectDirList = new ArrayList<>();
    private List<ProjectIconInfo> prjIconInfoList = new ArrayList<>();
    
    File baseDir = null;
    public void setNbsrcroot(File f) {
        baseDir = f;
    }
    
    File destDir = null;
    public void setDestdir(File f) {
        destDir = f;
    }
    
    int userDepth = 2;
    public void setDepth(int n) {
        userDepth = n;
    }
    
    String iconIncludes = "src/**/*.png,src/**/*.gif";
    public void setIconincludes(String s) {
        iconIncludes = s;
    }
    
    String iconExcludes = "";
    public void setIconexcludes(String s) {
        iconExcludes = s;
    }
    
    String prjIncludes = "";
    public void setPrjincludes(String s) {
        prjIncludes = s;
    }
    
    String prjExcludes = "";
    public void setPrjExcludes(String s) {
        prjExcludes = s;
    }
    
    boolean showEmpty = false;
    public void setShowempty(boolean b) {
        showEmpty = b;
    }
    
    public void execute() throws BuildException {
        if (baseDir == null || destDir == null) {
            log("Nbsrcroot or destdir are not specified.");
            return;
        }
        scanForProjectDirs(baseDir);
        for (Iterator<File> iter = projectDirList.iterator(); iter.hasNext(); ) {
            File f = iter.next();
            processProjectDir(f);
        }
        copyToDestDir(prjIconInfoList);
        dumpListToHTML(prjIconInfoList);
    }
    
    private ImageInfo readImageInfo(File fl) throws IOException {
        Dimension dim = null;
        ImageInfo imageInfo = null;
        try (ByteArrayInputStream bais = readSomeBytes(fl)) {
            if (isGIF(bais)) {
                imageInfo = new ImageInfo(readGIFDimension(bais), ImageInfo.GIF);
            } else if (isPNG(bais)) {
                imageInfo = new ImageInfo(readPNGDimension(bais), ImageInfo.PNG);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return imageInfo;
    }
    
    private ByteArrayInputStream readSomeBytes(File fl) throws IOException {
        byte buffer[] = new byte[30];
        FileInputStream fis = null;
        fis = new FileInputStream(fl);
        fis.read(buffer);
        fis.close();
        return new ByteArrayInputStream(buffer);
    }
    
    private boolean isGIF(InputStream is) throws IOException {
        is.reset();
        byte buf[];
        is.read(buf = new byte[3]);
        int signatureBuffer[] = parseUnsigned(buf);
        if (((char) signatureBuffer[0] == 'G') &&
            ((char) signatureBuffer[1] == 'I') &&
            ((char) signatureBuffer[2] == 'F')) {
            return true;
        }
        return false;
    }
    
    private boolean isPNG(InputStream is) throws IOException {
        is.reset();
        byte buf[];
        is.read(buf = new byte[8]);
        int signatureBuffer[] = parseUnsigned(buf);
        if ((signatureBuffer[0] == 137) && // PNG signature
            (signatureBuffer[1] == 80) && 
            (signatureBuffer[2] == 78) && 
            (signatureBuffer[3] == 71) && 
            (signatureBuffer[4] == 13) && 
            (signatureBuffer[5] == 10) && 
            (signatureBuffer[6] == 26) && 
            (signatureBuffer[7] == 10)) {
            return true;
        }
        return false;
    }
    
    private Dimension readGIFDimension(InputStream is) throws IOException {
        byte buf [];
        is.read(new byte[3]); // GIF version
        is.read(buf = new byte[2]); // width
        int widthBuf[] = parseUnsigned(buf);
        int width = (widthBuf[1] << 8) + widthBuf[0];
        is.read(buf = new byte[2]); // height
        int heightBuf[] = parseUnsigned(buf);
        int height = (heightBuf[1] << 8) + heightBuf[0];
        return new Dimension(width, height);
    }
    
    private Dimension readPNGDimension(InputStream is) throws IOException {
        byte buf[];
        is.read(new byte[4]); // length
        is.read(new byte[4]); // type
        is.read(buf = new byte[4]); // width
        int widthBuf[] = parseUnsigned(buf);
        int width = (widthBuf[0] << 24) + (widthBuf[1] << 16) + (widthBuf[2] << 8) + widthBuf[3];
        is.read(buf = new byte[4]); // height
        int heightBuf[] = parseUnsigned(buf);
        int height = (heightBuf[0] << 24) + (heightBuf[1] << 16) + (heightBuf[2] << 8) + heightBuf[3];
        return new Dimension(width, height);
    }
    
    private static int[] parseUnsigned(byte[] src) {
        int[] val = new int[src.length];
        for (int i = 0; i < src.length; i ++) {
            val[i] = (src[i] < 0) && (src[i] >= -128) ? 256 + src[i] : src[i];
        }
        return val;
    }
    
    // -------------------------------------------------------------------------
    
//    public static void main(String[] args) {
//        File buildFile = new File("e:/development/projects/CopyIcons/testscript.xml");
//        Project p = new Project();
//        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
//        p.init();
//        ProjectHelper helper = ProjectHelper.getProjectHelper();
//        p.addReference("ant.projectHelper", helper);
//        helper.parse(p, buildFile);
//        p.executeTarget(p.getDefaultTarget());
//    }
    
    // -------------------------------------------------------------------------
    
    private static class ImageInfo {
        
        public static final int GIF = 1;
        public static final int PNG = 2;
        
        private Dimension dim;
        private int type;
        private String path;
        private String ext;
        
        public ImageInfo(Dimension dm, int tp) {
            this(null, dm, tp);
        }
        
        public ImageInfo(String pth, Dimension dm, int tp) {
            path = pth;
            dim = dm;
            type = tp;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String pth) {
            path = pth;
        }
        
        public int getHeight() {
            return dim.height;
        }
        
        public int getWidth() {
            return dim.width;
        }
        
        public String getType() {
            if (type == GIF) {
                return "GIF";
            } else if (type == PNG) {
                return "PNG";
            }
            return "";
        }
        
        public String getExt() {
            return ext;
        }
        
        public String setExt(String ex) {
            return ext = ex;
        }
        
    }
    
    private static class ProjectIconInfo {
        
        public String prjPath;
        public List<ImageInfo> matchingIcons;
        public List<ImageInfo> notmatchingIcons;
        
        public ProjectIconInfo(String pth, List<ImageInfo> mi, List<ImageInfo> nmi) {
            prjPath = pth;
            matchingIcons = mi;
            notmatchingIcons = nmi;
        }
        
    }
    
    // -------------------------------------------------------------------------
        
    private void dumpListToHTML(List<ProjectIconInfo> lst) {
        File reportFile = new File(destDir, "index.html");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reportFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        try (PrintWriter pw = new PrintWriter(new BufferedOutputStream(fos))) {
            pw.println("<html>");
            pw.println("<head>");
            pw.println("<style type=\"text/css\">\nbody { font-family: Tahoma, Verdana, sans-serif }\n</style>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<h2>List of icons in NetBeans projects</h2>");
            pw.println("<h3>NetBeans Source Root: " + baseDir + "<br/>");
            pw.println("Destination Directory: " + destDir + "</h3>");
            pw.println("<p style=\"width: 70%\"><b>Description:</b><br/> New Image is image in the Destination Directory, Orig. Image is image in NetBeans Source Root. " +
                    "By replacing the image in Destination Directory under coresponding module and path you can prepare rebranded " +
                    "icons in paralel directory structure and then copy them over to NetBeans Source Root. Orig. Image is " +
                    "in the table just for comparison and reference what image was already changed.</p>");
            for (Iterator<ProjectIconInfo> iter = prjIconInfoList.iterator(); iter.hasNext(); ) {
                ProjectIconInfo info = iter.next();
                if (!showEmpty && info.matchingIcons.size() == 0) {
                    continue;
                }
                pw.println("<p>Module name: <b>" + info.prjPath + "</b></p>");
                pw.println("<p style=\"margin-left: 20px\">");
                if (info.matchingIcons.size() == 0) {
                    pw.println("<i>--- No icons ---</i>");
                    pw.println("</p>");
                    continue;
                }
                pw.println("<table width=\"80%\"border=\"1\" cellpadding=\"3\" cellspacing=\"0\">");
                pw.println("<tr><td><b>Resource Path</b></td>" +
                        "<td align=\"center\"><b>&nbsp;New Image&nbsp;</b></td>" +
                        "<td align=\"center\"><b>&nbsp;Orig. image&nbsp;</b></td>" +
                        "<td align=\"center\"><b>&nbsp;W x H&nbsp;</b></td>" +
                        "<td align=\"center\"><b>&nbsp;Extension&nbsp;</b></td>" +
                        "<td align=\"center\"><b>&nbsp;Real Type&nbsp;</b></td></tr>");
                for (Iterator<ImageInfo> goodIter = info.matchingIcons.iterator(); goodIter.hasNext(); ) {
                    ImageInfo goodInfo = goodIter.next();
                    String iconPath = goodInfo.getPath();
                    String copiedIconPath = info.prjPath + File.separator + iconPath;
                    String originalIconPath = baseDir.getAbsolutePath() + File.separator + info.prjPath + File.separator + iconPath;
                    pw.println("<tr>");
                    pw.println("<td>");
                    pw.println("<a href=\"" + copiedIconPath + "\">" + iconPath + "</a>");
                    pw.println("</td>");
                    pw.println("<td align=\"center\">");
                    pw.println("<img src=\"" + copiedIconPath + "\"/>"); // copied image
                    pw.println("</td>");
                    pw.println("<td align=\"center\">");
                    pw.println("<img src=\"file://" + originalIconPath + "\"/>"); // original image
                    pw.println("</td>");
                    pw.println("<td align=\"center\">" + goodInfo.getWidth() + " x " + goodInfo.getHeight() + "</td>");
                    pw.println("<td align=\"center\">" + goodInfo.getExt().toUpperCase() + "</td>");
                    if (!goodInfo.getExt().equalsIgnoreCase(goodInfo.getType())) {
                        pw.println("<td align=\"center\"><font color=\"Orange\">" + goodInfo.getType() + "</font></td>");
                    } else {
                        pw.println("<td align=\"center\">" + goodInfo.getType() + "</td>");
                    }
                    pw.println("</tr>");
                }
                pw.println("</table>");
                pw.println("</p>");
            }
            pw.println("</body>");
            pw.println("</html>");
            pw.flush();
        }
        log("---> Report was written to file: " + reportFile.getAbsolutePath());
    }
        
    private void copyToDestDir(List<ProjectIconInfo> prjInfoList) {
        FileSet fs = null;
        for (Iterator<ProjectIconInfo> iter = prjInfoList.iterator(); iter.hasNext(); ) {
            fs = new FileSet();
            log("Setting basedir for fileset: " + baseDir, Project.MSG_VERBOSE);
            ProjectIconInfo prjIconInfo = iter.next();
            fs.setDir(new File(baseDir, prjIconInfo.prjPath));
            int numFilesToCopy = prjIconInfo.matchingIcons.size() + prjIconInfo.notmatchingIcons.size();
            for (Iterator<ImageInfo> matchInfoIter = prjIconInfo.matchingIcons.iterator(); matchInfoIter.hasNext(); ) {
                ImageInfo info = matchInfoIter.next();
                log("Adding file to matching fileset: " + info.getPath(), Project.MSG_VERBOSE);
                fs.setIncludes(info.getPath());
            }
            for (Iterator<ImageInfo> notmatchInfoIter = prjIconInfo.notmatchingIcons.iterator(); notmatchInfoIter.hasNext(); ) {
                ImageInfo info = notmatchInfoIter.next();
                log("Adding file to notmatching fileset: " + info.getPath(), Project.MSG_VERBOSE);
                fs.setIncludes(info.getPath());
            }
            if (numFilesToCopy > 0) {
                Copy copy = (Copy) getProject().createTask("copy");
                copy.addFileset(fs);
                File dest = new File(destDir, prjIconInfo.prjPath);
                dest.mkdir();
                copy.setTodir(dest);
                copy.init();
                copy.setLocation(getLocation());
                copy.execute();
            }
        }
    }    
    
    // -------------------------------------------------------------------------
    
    private void scanForProjectDirs(File fl) {
        if (depth > userDepth) return;
        //if (isProjectDir(fl)) {
        //    projectDirList.add(fl);
        //}
        File allFiles[] = fl.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        depth++;
        for (File f : allFiles) {
            if (isProjectDir(f)) {
                // here could be some project exclusion logic
                projectDirList.add(f);
                log(f.toString(), Project.MSG_VERBOSE);
                scanForProjectDirs(f);
            } else {
                scanForProjectDirs(f);
            }
        }
        depth--;
    }
    
    private boolean isProjectDir(File fl) {
        File prjDirs[] = fl.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if ("nbproject".equals(name)) {
                    return true;
                }
                return false;
            }
        });
        if (prjDirs.length != 1) {
            return false;
        }
        String prjFiles[] = prjDirs[0].list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if ("project.xml".equals(name)) {
                    return true;
                }
                return false;
            }
        });
        if (prjFiles.length != 1) {
            return false;
        }
        return true;
    }

    private void processProjectDir(File f) {
        String prjPath = null;
        //if (f.getAbsolutePath().equals(baseDir.getAbsolutePath())) {
        //    prjPath = "";
        //} else {
            prjPath = f.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1);
        //}
        log("Processing project dir: " + prjPath);
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(f);
        ds.setIncludes(getAsArray(iconIncludes));
        ds.setExcludes(getAsArray(iconExcludes));
        ds.setCaseSensitive(false);
        ds.scan();
        String[] files = ds.getIncludedFiles();
        log("    Found " + files.length + " files in " + f);
        List<ImageInfo> goodIcons = new ArrayList<>();
        List<ImageInfo> badIcons = new ArrayList<>();
        for (String file : files) {
            String ext = file.substring(file.lastIndexOf('.') + 1);
            if (ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("png")) {
                File iconFile = new File(f, file);
                try {
                    ImageInfo imageInfo = null;
                    imageInfo = readImageInfo(iconFile);
                    if (imageInfo != null) {
                        imageInfo.setPath(file);
                        imageInfo.setExt(ext);
                        int w = imageInfo.getWidth();
                        int h = imageInfo.getHeight();
                        if ((w == 8 && h == 8) || (w == 16 && h == 16) ||
                            (w == 24 && h == 24) || (w == 32 && h == 32)) {
                            goodIcons.add(imageInfo);
                        } else {
                            badIcons.add(imageInfo);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        ProjectIconInfo prjIconInfo = new ProjectIconInfo(prjPath, goodIcons, badIcons);
        prjIconInfoList.add(prjIconInfo);
    }
    
    private String[] getAsArray(String s) {
        List<String> list = new ArrayList<>();
        for (StringTokenizer stok = new StringTokenizer(s, ","); stok.hasMoreTokens(); ) {
          String token = stok.nextToken().trim();
          list.add(token);
        }
        return list.toArray(new String[list.size()]);
    }
    
}
