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

import java.io.*;
import java.util.*;
import java.util.Map; // override org.apache.tools.ant.Map
import java.util.zip.*;
import java.util.jar.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.*;

/** Create a JAR file with locale variants.
 * Whenever files are found which should be localized, or are the result
 * of localization, places them in separate JAR files named according to the locale,
 * in a <samp>locale/</samp> subdirectory of the directory containing the master JAR.
 * Each sub-JAR gets a manifest which just has some informational tags
 * indicating its purpose (locale and branding):
 * <code>X-Informational-Archive-Locale</code> and/or <code>X-Informational-Archive-Branding</code>.
 * The values may be e.g. <code>ja</code> or <code>f4j_ce</code>; or <code>-</code>
 * if there is no suffix for this JAR.
 * You can control the available locales; brandings; and set of files which should
 * always be considered part of the localizable base kit.
 * You can use the "branding" and "locale" subelements to control the branded
 * and localized .jar files that will be produced.  Also, you can set the global
 * properties "locjar.brands" and "locjar.locales" to comma-separated
 * lists of branding or locale identifiers so that NetBeans-based projects can
 * brand or localize NetBeans without having to maintain modified versions of all
 * the individual Ant scripts
 * Originally this Ant task didn't recognize files below a directory with the
 * same name as a locale as being localized.  Now it does so by default.
 * <p>Based on <code>&lt;zip&gt;</code> and <code>&lt;jar&gt;</code> tasks in Ant,
 * but not feasible to simply subclass or compose them.
 * @see <a href="http://www.netbeans.org/devhome/docs/i18n/index.html">NetBeans I18N documentation</a>
 * @author Jesse Glick
 */
public class LocalizedJar extends MatchingTask {

    private List<FileSet> localeKits = new LinkedList<> ();
    private List<LocaleOrB> locales = new LinkedList<> ();
    private List<LocaleOrB> brandings = new LinkedList<> ();
    private File jarFile;
    private File baseDir;
    private boolean doCompress = false;
    private static long emptyCrc = new CRC32 ().getValue ();
    private List<FileSet> filesets = new LinkedList<> ();
    private File manifest;
    private boolean checkPathLocale = true ;
    private boolean warnMissingDir = false ;
    private boolean warnMissingDirSet = false ;
    private boolean preserveModuleJar = true;
    private boolean alwaysIncludeManifest = false;

    /** Locale or branding specifier.
     * Represents a complete locale or branding suffix,
     * e.g. <code>ja</code> or <code>ja_JP</code>
     * (but not simply <code>JP</code>).
     * For branding, e.g. <code>f4j</code> or <code>f4j_ce</code>.
     * You must include all relevant suffixes if you expect them to match
     * (but the task will handle a branding suffix combined with a locale
     * suffix, in that order).
     */
    public class LocaleOrB {
        String n;
        /** The suffix. */
        public void setName (String n) {
            this.n = n;
        }
    }

    /** Distinguish particular files that will be considered localizable.
     * This nested <samp>&lt;localekit&gt;</samp> has the same syntax as a FileSet.
     * So typically you will build a JAR from a fileset containing all build
     * products within a given directory; the "locale kit" should be a fileset
     * matching all properties files, for example, and maybe icons which you would
     * expect someone to try to localize--in general, anything which will be referenced
     * in code using a localized lookup (<code>NbBundle</code>).
     * While files with recognized localized (or branded) suffixes automatically go into
     * marked JARs in the <samp>locale/</samp> subdirectory, files considered part of the
     * locale kit also always go into this subdirectory; if they have no suffix, they will
     * be placed in a suffixless locale JAR.
     * So localizers can simply look at this JAR file and brand/localize it as they see fit.
     */
    public void addLocalekit (FileSet fs) {
        localeKits.add (fs);
    }

    /** Add a recognized locale suffix. */
    public LocaleOrB createLocale () {
        LocaleOrB l = new LocaleOrB ();
        locales.add (l);
        return l;
    }

    /** Add a recognized branding suffix. */
    public LocaleOrB createBranding () {
        LocaleOrB l = new LocaleOrB ();
        brandings.add (l);
        return l;
    }

    /** JAR file to create.
     * In fact this is the location of the "base" JAR;
     * locale-specific JARs may be created in the <samp>locale/</samp> subdirectory
     * of the directory containing this JAR, and will be named according to the name
     * of this JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setJarfile (File jarFile) {
        if (! jarFile.getName ().endsWith (".jar")) {
            throw new BuildException ("jarfile attribute must be a file with *.jar extension");
        }
        if (jarFile.getParentFile () == null) {
            throw new BuildException ("jarfile attribute must have a containing directory");
        }
        this.jarFile = jarFile;
    }

    /** Base directory to JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setBasedir (File baseDir) {
        this.baseDir = baseDir;
    }

    /** Turn on or off compression (default off).
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setCompress(boolean compress) {
        doCompress = compress;
    }

    /** Turn on/off preserving original module jars with 'code'
     *  Set it to 'true' to not allow overwriting of module jars
     *  with localization/branding stuff
     */
    public void setPreserveModuleJar(boolean pmj) {
        preserveModuleJar = pmj;
    }

    /** Turn on/off inclusion of manifest even for localized and/or branded
     *  jars. Set it to true if you want to add special manifest tags
     *  to all produced jarfiles
     */
    public void setAlwaysIncludeManifest(boolean aim) {
        alwaysIncludeManifest = aim;
    }

    /** A set of files to JAR up.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void addFileset (FileSet set) {
        filesets.add (set);
    }

    /** Manifest file for the JAR.
     * Compare Ant's <samp>&lt;jar&gt;</samp> task.
     */
    public void setManifest (File manifest) {
	this.manifest = manifest;
    }

    /** By default this is true.  If set to false, then this task will
     * not recognize files below a directory with the same name as a
     * locale as being localized (unless the simple filename also
     * includes the locale).
     */
    public void setCheckPathLocale( boolean doit) {
      checkPathLocale = doit ;
    }

    /** This is false by default, in which case missing dirs in the
     * filesets cause a BuildException to be thrown.  If true, then
     * a warning is printed but the build will continue.
     * This task will also look for a global property 
     * "locjar.warnMissingDir" if this attribute isn't set.
     */
    public void setWarnMissingDir( boolean b) {
      warnMissingDir = b ;
      warnMissingDirSet = true ;
    }

    public void execute () throws BuildException {

        // Sanity checks:
        if (baseDir == null && filesets.size () == 0) {
            throw new BuildException ("basedir attribute must be set, or at least one fileset must be given!");
        }
        if (jarFile == null) {
            throw new BuildException ("You must specify the JAR file to create!");
        }
        if (manifest != null && ! manifest.isFile ()) {
            throw new BuildException ("The specified manifest does not actually exist.");
        }

	// If needed, warn that directories are missing. //
	if( shouldWarnMissingDir() && warnIfMissingDir()) {

	  // Stop if dirs were missing. //
	  return ;
	}

	// Look for global locales or brandings to use. //
	addGlobalLocaleAndBranding() ;

        //System.err.println ("Stage #1");
        // First find out which files need to be archived.
        Map<String, File> allFiles = new HashMap<> (); // all files to do something with; Map<String,File> from JAR path to actual file
        // Populate it.
        {
            List<FileScanner> scanners = new ArrayList<> (filesets.size () + 1);
            if (baseDir != null) {
                scanners.add (getDirectoryScanner (baseDir));
            }
            for (FileSet fs: filesets) {
                scanners.add(fs.getDirectoryScanner(getProject()));
            }
            for (FileScanner scanner: scanners) {
                File thisBaseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                for (int i = 0; i < files.length; i++) {
                    String name = files[i].replace (File.separatorChar, '/');
                    if (name.equalsIgnoreCase ("META-INF/MANIFEST.MF")) {
                        log ("Warning: ignoring META-INF/MANIFEST.MF found among scanned files", Project.MSG_WARN);
                        continue;
                    }
                    allFiles.put (name, new File (thisBaseDir, files[i]));
                }
            }
        }

        //System.err.println ("Stage #2");
        // Now find all files which should always be put into a locale
        // kit (e.g. dir/locale/name.jar, no special locale or
        // branding, but distinguished as localizable/brandable).
        Set<File> localeKitFiles = new HashSet<> (); // all locale-kit files
        // Populate this one.
        {
            for (FileSet fs: localeKits) {
                FileScanner scanner = fs.getDirectoryScanner(getProject());
                File thisBaseDir = scanner.getBasedir ();
                String[] files = scanner.getIncludedFiles ();
                for (int i = 0; i < files.length; i++) {
                    localeKitFiles.add (new File (thisBaseDir, files[i]));
                }
            }
        }

        //System.err.println ("Stage #3");
        // Compute list of supported locales and brandings.
        List<String> locales2 = new LinkedList<> ();
        List<String> brandings2 = new LinkedList<> (); // all brandings
        // Initialize above two.
        
        for (LocaleOrB lob: locales) {
            locales2.add (lob.n);
        }
        for (LocaleOrB lob: brandings) {
            brandings2.add (lob.n);
        }
        class InverseLengthComparator implements Comparator<String> {
            public int compare (String s1, String s2) {
                return s2.length () - s1.length ();
            }
        }
        Collections.sort (locales2, new InverseLengthComparator ());
        Collections.sort (brandings2, new InverseLengthComparator ());

        //System.err.println ("Stage #4");
        // Analyze where everything goes.
        Set<File> jars = new HashSet<> (); //JAR files to build
        Map<File,String> localeMarks = new HashMap<> (); // JAR files to locale (or null for basic JAR, "-" for blank)
        Map<File,String> brandingMarks = new HashMap<> (); // JAR files to branding (or null for basic JAR, "-" for blank)
        Map<File,Map<String,File>> router = new HashMap<> (); // JAR files to map of JAR path to actual file (file may be null for dirs)
        {
	    String localeDir ;
            for (Map.Entry<String, File> entry: allFiles.entrySet()) {
                String path = entry.getKey ();

log( "==> Examining file: " + path, Project.MSG_DEBUG) ;

                File file = entry.getValue ();
                // First see if it matches a known branding, locale, or pair of one of each.
                String testpath = path;
                int idx = testpath.lastIndexOf ('/');
                if (idx != -1) testpath = testpath.substring (idx + 1);
                idx = testpath.lastIndexOf ('.');
                if (idx != -1) testpath = testpath.substring (0, idx);
                String thisLocale = null;
                for(String tryLocale: locales2) {
                    if (testpath.endsWith ("_" + tryLocale)) {
                        thisLocale = tryLocale;
                        testpath = testpath.substring (0, testpath.length () - 1 - tryLocale.length ());
                        break;
                    }
                }
                String thisBranding = null;
                for(String tryBranding: brandings2) {
                    if (testpath.endsWith ("_" + tryBranding)) {
                        thisBranding = tryBranding;
                        break;
                    }
                }
                File thisjar = null; // JAR to send this file to

		// Check if this file has a parent directory with the //
		// same name as one of the locales.		      //
		localeDir = checkInLocaleDir( file, locales2) ;
		if( localeDir != null) {
		  thisLocale = localeDir ;
		}


                if( thisLocale != null) {
                    log( "    Locale: " + thisLocale, Project.MSG_DEBUG) ;
                } else {
                    log( "    Locale not set", Project.MSG_DEBUG) ;
                }
                if( thisBranding != null) {
                    log( "    Branding: " + thisBranding, Project.MSG_DEBUG) ;
                } else {
                    log( "    Branding not set", Project.MSG_DEBUG) ;
                }
                if( localeKitFiles.contains( file)) {
                    log( "    Localizable file.", Project.MSG_DEBUG) ;
                } 


                if (thisLocale != null || thisBranding != null || localeKitFiles.contains (file)) {
                    String name = jarFile.getName ();
                    // We know jarFile is a *.jar so this is safe:
                    name = name.substring (0, name.length () - 4);
                    if (thisBranding != null) {
                        name += '_' + thisBranding;
                    }
                    if (thisLocale != null) {
                        name += '_' + thisLocale;
                    }
                    name += ".jar";
                    if ((preserveModuleJar) && (thisBranding == null) && (thisLocale == null)) {
                        thisjar = null;
                        log("    Preserving module file (1): " + jarFile.getName(), Project.MSG_DEBUG);
                    } else {
                        thisjar = new File (new File (jarFile.getParentFile (), "locale"), name);
                        localeMarks.put (thisjar, ((thisLocale != null) ? thisLocale : "-"));
                        brandingMarks.put (thisjar, ((thisBranding != null) ? thisBranding : "-"));
                    }
                } else {
 		    if (preserveModuleJar) {
                        thisjar = null;
                        log("    Preserving module file (2): " + jarFile.getName(), Project.MSG_DEBUG);
                    } else {
                        thisjar = jarFile;
                        localeMarks.put (thisjar, null);
                        brandingMarks.put (thisjar, null);
                    }
                }
                if (thisjar != null) {
  	            log("    Adding file " + thisjar.getName() + " to 'jars' HashSet", Project.MSG_DEBUG);
                    jars.add (thisjar);
                    Map<String, File> files = router.get (thisjar);
                    if (files == null) {
                        files = new TreeMap<> ();
                        router.put (thisjar, files);
                    }
                    files.put (path, file);
                }
            }
        }

        //System.err.println ("Stage #5");
        // Go through JARs one by one, and build them (if necessary).
        {
            List<File> jars2 = new ArrayList<> (jars);
            class FileNameComparator implements Comparator<File> {
                public int compare (File f1, File f2) {
                    return f1.toString ().compareTo (f2.toString ());
                }
            }
            Collections.sort (jars2, new FileNameComparator ());
            for (File jar: jars2) {
                Map<String, File> files = router.get (jar);
                if (jar.exists ()) {
                    // Do an up-to-date check first.
                    long time = jar.lastModified ();
                    if (manifest == null || manifest.lastModified () <= time) {
                        boolean upToDate = true;
                        for(File f: files.values()) {
                            if (f.lastModified () > time) {
                                upToDate = false;
                                break;
                            }
                        }
                        if (upToDate) {
                            // Skip this JAR.
                            continue;
                        }
                    }
                }
                log ("Building localized/branded jar: " + jar);
                IOException closing = null;
                try {
                    jar.getParentFile ().mkdirs ();
                    ZipOutputStream out = new ZipOutputStream (new FileOutputStream (jar));
                    try {
                        out.setMethod (doCompress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
                        String localeMark = localeMarks.get (jar);
                        String brandingMark = brandingMarks.get (jar);
                        Set<String> addedDirs = new HashSet<> ();
                        // Add the manifest.
                        InputStream is;
                        long time;
                        java.util.jar.Manifest mani;
                        if (manifest != null && localeMark == null && brandingMark == null) {
                            // Master JAR, and it has a manifest.
                            is = new FileInputStream (manifest);
                            time = manifest.lastModified ();
                            try {
                                mani = new java.util.jar.Manifest (is);
                            } finally {
                                is.close ();
                            }
                        } else if ((manifest != null) && (alwaysIncludeManifest)) {
                            // always include specified manifest in localized/branded jars
                            // such manifest must not contain attribute OpenIDE-Module
                            // check supplied manifest and if contains key OpenIDE-Module, issue warning
                            // and fallback to default Ant's manifest boilerplate (like no manifest was supplied)
                            is = new FileInputStream (manifest);
                            time = manifest.lastModified ();
                            try {
                                mani = new java.util.jar.Manifest (is);
                            } finally {
                                is.close ();
                            }
                            Attributes attr = mani.getMainAttributes ();
                            //check if it's not module manifest for jarfile with localized/branded resources
                            if ((attr.containsKey ("OpenIDE-Module")) && ((localeMark != null) || (brandingMark != null))){
                            	String lbmsg = "";
                            	if (localeMark != null) {
                            	    lbmsg = "locale: '"+localeMark+"' ";
                            	}
                            	if (brandingMark != null) {
                            	    lbmsg = "branding: '"+brandingMark+"' ";
                            	}
                                log("WARNING: Ignoring supplied NetBeans module manifest for "+lbmsg+"jarfile '"+jar+"'. Using default Ant manifest bolilerplate. "
                                   +"Use -verbose option to see more details.", Project.MSG_INFO);
                                log("WARNING(verbose): Supplied manifest file '"+manifest.getAbsolutePath()+"' contains "
                                   +"key OpenIDE-Module, which cannot be included in manifest of localized "
                                   +"and/or branded jar. Ignoring whole manifest for now. To fix this you have "
                                   +"to avoid using NetBeans module manifest file together with attribute "
                                   +"'allwaysincludemanifest' set to 'true' and non-empty properties 'locjar.locales' "
                                   +"and 'locjar.brands'. You can accomplish that by i.e. using Ant's <jar> task "
                                   +"for regular NetBeans module jarfile packaging and use NetBeans' Ant extension "
                                   +"task <"+this.getTaskName()+"> for localized and/or branded jars. Using default "
                                   +"Ant's manifest boilerplate instead.", Project.MSG_VERBOSE);
                                try {
                                    is.close();
                                } finally {
                                    is = MatchingTask.class.getResourceAsStream ("/org/apache/tools/ant/defaultManifest.mf");
                                    time = System.currentTimeMillis ();
                                    try {
                                        mani = new java.util.jar.Manifest (is);
                                    } finally {
                                       is.close ();
                                    }
                                }
                            }
                        } else {
                            // Some subsidiary JAR.
                            is = MatchingTask.class.getResourceAsStream ("/org/apache/tools/ant/defaultManifest.mf");
                            time = System.currentTimeMillis ();
                            try {
                                mani = new java.util.jar.Manifest (is);
                            } finally {
                                is.close ();
                            }
                        }
                        Attributes attr = mani.getMainAttributes ();
                        if (! attr.containsKey (Attributes.Name.MANIFEST_VERSION)) {
                            attr.put (Attributes.Name.MANIFEST_VERSION, "1.0");
                        }
                        if (localeMark != null) {
                            attr.putValue ("X-Informational-Archive-Locale", localeMark);
                        }
                        if (brandingMark != null) {
                            attr.putValue ("X-Informational-Archive-Branding", brandingMark);
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                        mani.write (baos);
                        byte[] bytes = baos.toByteArray ();
                        addToJar (new ByteArrayInputStream (bytes), new ByteArrayInputStream (bytes),
                                  out, "META-INF/MANIFEST.MF", time, addedDirs);
                        // Now regular files.
                        for (Map.Entry<String, File> entry: files.entrySet()) {
                            String path = entry.getKey ();
                            File file = entry.getValue ();
                            addToJar (new FileInputStream (file), new FileInputStream (file),
                                      out, path, file.lastModified (), addedDirs);
                        }

			// If desired, write the root of the srcDir to a file. //
			writeSrcDir() ;
                    } finally {
                        try {
                            out.close ();
                        } catch (IOException ex) {
                            closing = ex;
                        }
                    }

                    if (closing != null) {
                        // if there was a closing exception and no other one
                        throw closing;
                    }
                } catch (IOException ioe) {
                    String msg = "Problem creating JAR: " + ioe.getMessage ();
                    if (! jar.delete ()) {
                        msg += " (and the JAR is probably corrupt but I could not delete it)";
                    }
                    throw new BuildException(msg, ioe, getLocation());
                }
            }
        }

    } // end execute()

    private void addToJar (InputStream in1, InputStream in2, ZipOutputStream out,
                           String path, long lastModified, Set<String> addedDirs) throws IOException {
        try {
            if (path.endsWith ("/")) {
                throw new IOException ("Bad path: " + path);
            }
            // Add parent dirs as needed:
            int pos = -1;
            while ((pos = path.indexOf ('/', pos + 1)) != -1) {
                String dir = path.substring (0, pos + 1);
                if (! addedDirs.contains (dir)) {
                    addedDirs.add (dir);
                    ZipEntry ze = new ZipEntry (dir);
                    ze.setSize (0);
                    ze.setMethod (ZipEntry.STORED);
                    ze.setCrc (emptyCrc);
                    ze.setTime (lastModified);
                    out.putNextEntry (ze);
                }
            }
            // Add the file itself:
            ZipEntry ze = new ZipEntry (path);
            ze.setMethod (doCompress ? ZipEntry.DEFLATED : ZipEntry.STORED);
            ze.setTime (lastModified);
            long size = 0;
            CRC32 crc = new CRC32 ();
            byte[] buf = new byte[4096];
            int read;
            while ((read = in1.read (buf)) != -1) {
                crc.update (buf, 0, read);
                size += read;
            }
            in1.close ();
            ze.setCrc (crc.getValue ());
            ze.setSize (size);
            out.putNextEntry (ze);
            while ((read = in2.read (buf)) != -1) {
                out.write (buf, 0, read);
            }
        } finally {
            in2.close ();
            in1.close ();
        }
    } // end addToJar()


  // If the name of any parent directory of this file is the same as //
  // one of the locales, return the locale.			     //
  protected String checkInLocaleDir( File file,
				     List<String> locales) {

    // See if this functionality is disabled. //
    if( !checkPathLocale) {
      return null ;
    }

    int idx ;
    String locale_dir, ret = null ;
    String path = file.getPath() ;

    // For each locale. //
    for (String loc: locales) {

      // If the path contains a dir with the same name as the //
      // locale.					      //
      locale_dir = File.separator + loc + File.separator;
      idx = path.indexOf( locale_dir) ;
      if( idx != -1) {

	// Stop and return this locale. //
	ret = loc ;
	break ;
      }
    }

    return( ret) ;
  }

  ////////////////////////////////////////////////////////////////////
  // This section of code supports the feature that this class will //
  // look for global properties that specify locales and brandings  //
  // that should be used.					    //
  protected void addGlobalLocaleAndBranding() {
    addGlobals( getGlobalLocaleVarName(), locales) ;
    addGlobals( getOldGlobalLocaleVarName(), locales) ;
    addGlobals( getGlobalBrandingVarName(), brandings) ;
    addGlobals( getOldGlobalBrandingVarName(), brandings) ;
  }

  protected String getGlobalLocaleVarName() {
    return( new String( "locjar.locales")) ;
  }

  protected String getGlobalBrandingVarName() {
    return( new String( "locjar.brands")) ;
  }

  // For backwards compatibility. //
  protected String getOldGlobalLocaleVarName() {
    return( new String( "locjar_global_locales")) ;
  }

  // For backwards compatibility. //
  protected String getOldGlobalBrandingVarName() {
    return( new String( "locjar_global_brands")) ;
  }

  protected void addGlobals( String var_name,
			     List<LocaleOrB> list) {
    String prop = null ;
    StringTokenizer tokenizer = null ;
    String tok = null ;
    LocaleOrB lorb = null ;

    // Foreach string in the global list. //
    prop = getProject().getProperty( var_name) ;
    if( prop != null && !prop.equals( "")) {
      tokenizer = new StringTokenizer( prop, ", ") ;
      while( tokenizer.hasMoreTokens()) {
	tok = tokenizer.nextToken() ;

	// Add a new entry in the given list. //
	lorb = new LocaleOrB() ;
	lorb.setName( tok) ;
	list.add( lorb) ;
      }
    }
  }
  //////////////////////////////////////////////////////////////////////

  protected boolean shouldWarnMissingDir() {
    String s ;
    boolean ret = false ;	// Default false. //

    // If the attribute is set, use its value. //
    if( warnMissingDirSet) {
      ret = warnMissingDir ;
    }

    // Otherwise use the global property value, if set. //
    else {
      s = getProject().getProperty("locjar.warnMissingDir");
      if( s != null && !s.trim().equals( "")) {
	ret = Project.toBoolean(s);
      }
    }

    return( ret) ;
  }

  // If any dir's don't exist, warn the user and return true. //
  protected boolean warnIfMissingDir() {
    File dir ;
    boolean ret = false ;

    // Print warning if the basedir doesn't exist. //
    if( baseDir != null && !baseDir.exists()) {
      ret = true ;
      printMissingDirWarning( baseDir) ;
    }

    // For each fileset. //
    for (FileSet fileset: filesets) {
	// Print warning if the dir doesn't exist. //
	dir = fileset.getDir(getProject());
	if( dir != null && !dir.exists()) {
	  ret = true ;
	  printMissingDirWarning( dir) ;
	}
      }
    return( ret) ;
  }

  // Warn the user that the given dir doesn't exist. //
  protected void printMissingDirWarning( File dir) {
    log( "WARNING: Skipping this task: Directory " + dir.getPath() + " doesn't exist.") ;
  }

  protected boolean shouldWriteSrcDir() {
    boolean ret = false ;
    String s = getProject().getProperty("locjar.writeSrcDir");
    if( s != null && Project.toBoolean(s)) {
      ret = true ;
    }
    return( ret) ;
  }

  protected void writeSrcDir() {
    String name ;
    int idx, fromIdx ;
    OutputStreamWriter osw ;
    FileOutputStream fos ;
    File file ;

    if( shouldWriteSrcDir() && jarFile != null && baseDir != null) {
      name = jarFile.getPath() ;
      fromIdx = getNetbeansStartIdx() ;
      idx = name.indexOf( File.separator+"netbeans"+File.separator, fromIdx) ;
      if( idx != -1) {
	try {
	  file = new File( name.substring( 0, idx) + File.separator + "srcdir.properties") ;
	  fos = new FileOutputStream( file) ;
	  osw = new OutputStreamWriter( fos) ;
	  osw.write( "srcdir=" + baseDir + "\n") ;
	  osw.close() ;
	  fos.close() ;
	}
	catch( Exception e) {
	  System.out.println( "ERROR: " + e.getMessage()) ;
	  e.printStackTrace() ;
	  throw new BuildException() ;
	}
      }
      else {
	throw new BuildException( "ERROR: Couldn't find netbeans dir to write srcdir.properties to.") ;
      }
    }
  }

  // Return the index to start searching from to find the "netbeans"
  // directory into which the "srcdir.properties" file will be
  // written.
  protected int getNetbeansStartIdx() {
    int startIdx = 0 ;
    int idx ;

    idx = baseDir.getPath().lastIndexOf( File.separator+
					 "netbeans"+File.separator) ;
    if( idx != -1) {
      startIdx = idx + 1 ;
    }
    return( startIdx) ;
  }
}
