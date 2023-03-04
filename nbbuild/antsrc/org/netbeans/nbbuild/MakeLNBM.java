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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;

/** Makes a localized <code>.nbm</code> (<b>N</b>et<b>B</b>eans <b>M</b>odule) file.
 * This version is temporary, intended to be used only until 
 * the functionality added to this version since rev 1.29 
 * of MakeNBM.java can be added into MakeNBM.java
 * 
 * @author Jerry Huth (email: jerry@solidstep.com)
 */
public class MakeLNBM extends MatchingTask {

    /** The same syntax may be used for either <samp>&lt;license&gt;</samp> or
     * <samp>&lt;description&gt;</samp> subelements.
     * <p>By setting the property <code>makenbm.nocdata</code> to <code>true</code>,
     * you can avoid using XML <code>CDATA</code> (for compatibility with older versions
     * of Auto Update which could not handle it).
     */
    public class Blurb {
        /** You may embed a <samp>&lt;file&gt;</samp> element inside the blurb.
         * If there is text on either side of it, that will be separated
         * with a line of dashes automatically.
         * But use nested <samp>&lt;text&gt;</samp> for this purpose.
         */
	public class FileInsert {
            /** File location. */
	    public void setLocation (File file) throws BuildException {
                log("Including contents of " + file, Project.MSG_VERBOSE);
		long lmod = file.lastModified ();
		if (lmod > mostRecentInput) mostRecentInput = lmod;
		addSeparator ();
		try {
		    try (InputStream is = new FileInputStream (file)) {
			Reader r = new InputStreamReader (is, "UTF-8"); //NOI18N
			char[] buf = new char[4096];
			int len;
			while ((len = r.read (buf)) != -1)
			    text.append (buf, 0, len);
		    }
		} catch (IOException ioe) {
                    throw new BuildException ("Exception reading blurb from " + file, ioe, getLocation ());
		}
	    }
	}
	private StringBuffer text = new StringBuffer ();
	private String name = null;
        /** There may be freeform text inside the element. Prefer to use nested elements. */
	public void addText (String t) {
	    addSeparator ();
	    // Strips indentation. Needed because of common style:
	    // <description>
	    //   Some text here.
	    //   And another line.
	    // </description>
	    t = getProject().replaceProperties(t.trim());
	    int min = Integer.MAX_VALUE;
	    StringTokenizer tok = new StringTokenizer (t, "\n"); //NOI18N
	    boolean first = true;
	    while (tok.hasMoreTokens ()) {
		String line = tok.nextToken ();
		if (first) {
		    first = false;
		} else {
		    int i;
		    for (i = 0;
			 i < line.length () &&
			     Character.isWhitespace (line.charAt (i));
			 i++)
			;
		    if (i < min) min = i;
		}
	    }
	    if (min == 0) {
		text.append (t);
	    } else {
		tok = new StringTokenizer (t, "\n"); //NOI18N
		first = true;
		while (tok.hasMoreTokens ()) {
		    String line = tok.nextToken ();
		    if (first) {
			first = false;
		    } else {
			text.append ('\n'); //NOI18N
			line = line.substring (min);
		    }
		    text.append (line);
		}
	    }
	}
        /** Contents of a file to include. */
	public FileInsert createFile () {
	    return new FileInsert ();
	}
        /** Text to include literally. */
        public class Text {
            public void addText(String t) {
                Blurb.this.addText(t);
            }
        }
        // At least on Ant 1.3, mixed content does not work: all the text is added
        // first, then all the file inserts. Need to use subelements to be sure.
        /** Include nested literal text. */
        public Text createText() {
            return new Text();
        }
	private void addSeparator () {
	    if (text.length () > 0) {
		// some sort of separator
		if (text.charAt (text.length () - 1) != '\n') //NOI18N
		    text.append ('\n'); //NOI18N
		text.append ("-----------------------------------------------------\n"); //NOI18N
	    }
	}
	public String getText () {
            String nocdata = getProject().getProperty("makenbm.nocdata"); //NOI18N
            if (nocdata != null && Project.toBoolean(nocdata)) {
                return xmlEscape(text.toString());
            } else {
                return "<![CDATA[" + text.toString () + "]]>"; //NOI18N
            }
	}
        /** You can either set a name for the blurb, or using the <code>file</code> attribute does this.
         * The name is mandatory for licenses, as this identifies the license in
         * an update description.
         */
	public void setName (String name) {
	    this.name = name;
	}
	public String getName () {
	    return name;
	}
        /** Include a file (and set the license name according to its basename). */
	public void setFile (File file) {
	    // This actually adds the text and so on:
	    new FileInsert ().setLocation (file);
	    // Default for the name too, as a convenience.
	    if (name == null) name = file.getName ();
	}
    }

    public class ExternalPackage {
	String name = null;
	String targetName = null;
	String startUrl = null;
	String description = null;

	public void setName(String n) {
	    this.name = n;
	}

	public void setTargetName(String t) {
	    this.targetName = t;
	}

	public void setStartURL(String u) {
	    this.startUrl = u;
	}
	
	public void setDescription(String d) {
	    this.description = d;
	}

    }

    // Similar to org.openide.xml.XMLUtil methods.
    private static String xmlEscape(String s) {
        int max = s.length();
        StringBuffer s2 = new StringBuffer((int)(max * 1.1 + 1));
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': //NOI18N
                    s2.append("&lt;"); //NOI18N
                    break;
                case '>': //NOI18N
                    s2.append("&gt;"); //NOI18N
                    break;
                case '&': //NOI18N
                    s2.append("&amp;"); //NOI18N
                    break;
                case '"': //NOI18N
                    s2.append("&quot;"); //NOI18N
                    break;
                default:
                    s2.append(c);
                    break;
            }
        }
        return s2.toString();
    }

    /** <samp>&lt;signature&gt;</samp> subelement for signing the NBM. */
    public /*static*/ class Signature {
	public File keystore;
	public String storepass, alias;
        /** Path to the keystore (private key). */
	public void setKeystore (File f) {
	    keystore = f;
	}
        /** Password for the keystore.
         * If a question mark (<samp>?</samp>), the NBM will not be signed
         * and a warning will be printed.
         */
	public void setStorepass (String s) {
	    storepass = s;
	}
        /** Alias for the private key. */
	public void setAlias (String s) {
	    alias = s;
	}
    }

    private File file = null;
    private File topdir = null;
    private File manifest = null;
    /** see #13850 for explanation */
    private File module = null;
    private String homepage = null;
    private String distribution = null;
    private String needsrestart = null;
    private Blurb license = null;
    private Blurb description = null;
    private Blurb notification = null;    
    private Signature signature = null;
    private long mostRecentInput = 0L;
    private boolean isStandardInclude = true;
    private Vector<ExternalPackage> externalPackages = null;
    private boolean manOrModReq = true ;
    private boolean manOrModReqSet = false ;
    private String langCode = null ;
    private String brandingCode = null ;
    private String modInfo = null ;
    private File locBundle = null ; // Localizing Bundle

    /** Include netbeans directory - default is true */
    public void setIsStandardInclude(boolean isStandardInclude) {
	this.isStandardInclude = isStandardInclude;
    }

    /** Name of resulting NBM file. */
    public void setFile (File file) {
	this.file = file;
    }
    /** Top directory.
     * Expected to contain a subdirectory <samp>netbeans/</samp> with the
     * desired contents of the NBM.
     * Will create <samp>Info/info.xml</samp> with metadata.
     */
    public void setTopdir (File topdir) {
	this.topdir = topdir;
    }
    /** Module manifest needed for versioning.
     * @deprecated Use {@link #setModule} instead.
     */
    @Deprecated
    public void setManifest (File manifest) {
	this.manifest = manifest;
	long lmod = manifest.lastModified ();
	if (lmod > mostRecentInput) mostRecentInput = lmod;
        log(getLocation() + "The 'manifest' attr on <makenbm> is deprecated, please use 'module' instead", Project.MSG_WARN);
    }
    /** Module JAR needed for generating the info file.
     * Information may be gotten either from its manifest,
     * or if it declares OpenIDE-Module-Localizing-Bundle in its
     * manifest, from that bundle.
     * The base locale variant, if any, is also checked if necessary
     * for the named bundle.
     * Currently no other locale variants of the module are examined;
     * the information is available but there is no published specification
     * of what the resulting variant NBMs (or variant information within
     * the NBM) should look like.
     */
    public void setModule(File module) {
        this.module = module;
        // mostRecentInput updated below...
    }
    /** URL to a home page describing the module. */
    public void setHomepage (String homepage) {
	this.homepage = homepage;
    }
    /** Does module need IDE restart to be installed? */
    public void setNeedsrestart (String needsrestart) {
        this.needsrestart = needsrestart;
    }
    /** URL where this NBM file is expected to be downloadable from. */
    public void setDistribution (String distribution) throws BuildException {
        if (distribution.startsWith("http://")) { //NOI18N
            this.distribution = distribution;
        } else  if (!(distribution.equals(""))) {
            // workaround for typical bug in build script
            this.distribution = "http://" + distribution; //NOI18N
        } else {
            throw new BuildException("Distribution URL is empty, check build.xml file", getLocation());
        }
        // check the URL
        try {
            URI uri = java.net.URI.create(this.distribution);
        } catch (IllegalArgumentException ile) {
            throw new BuildException("Distribution URL \"" + this.distribution + "\" is not a valid URI", ile, getLocation());
        }
    }
    public Blurb createLicense () {
	return (license = new Blurb ());
    }
    public Blurb createNotification () {
	return (notification = new Blurb ());
    }    
    public Blurb createDescription () {
        log(getLocation() + "The <description> subelement in <makenbm> is deprecated except for emergency patches, please ensure your module has an OpenIDE-Module-Long-Description instead", Project.MSG_WARN);
	return (description = new Blurb ());
    }
    public Signature createSignature () {
	return (signature = new Signature ());
    }

    public ExternalPackage createExternalPackage(){
	ExternalPackage externalPackage = new ExternalPackage ();
	if (externalPackages == null)
	    externalPackages = new Vector<>();
	externalPackages.add( externalPackage );
	return externalPackage;
    }

    public void execute () throws BuildException {
	if (file == null) {
	    throw new BuildException("must set file for makenbm", getLocation());
        }
        if (manifest == null && module == null && reqManOrMod()) {
            throw new BuildException("must set module for makenbm", getLocation());
        }
        if (manifest != null && module != null) {
            throw new BuildException("cannot set both manifest and module for makenbm", getLocation());
        }
	// Will create a file Info/info.xml to be stored alongside netbeans/ contents.
	File infodir = new File (topdir, "Info"); //NOI18N
	infodir.mkdirs ();
	File infofile = new File (infodir, "info.xml"); //NOI18N
	if (infofile.exists ()) {
            infofile.delete();
        }
        Attributes attr = null;
        if (module != null) {
            // The normal case; read attributes from its manifest and maybe bundle.
            long mMod = module.lastModified();
            if (mostRecentInput < mMod) mostRecentInput = mMod;
            try {
                try (JarFile modulejar = new JarFile(module)) {
                    attr = modulejar.getManifest().getMainAttributes();
                    String bundlename = attr.getValue("OpenIDE-Module-Localizing-Bundle"); //NOI18N
                    if (bundlename != null) {
                        Properties p = new Properties();
                        ZipEntry bundleentry = modulejar.getEntry(bundlename);
                        if (bundleentry != null) {
                            try (InputStream is = modulejar.getInputStream(bundleentry)) {
                                p.load(is);
                            }
                        } else {
                            // Not found in main JAR, check locale variant JAR.
                            File variant = new File(new File(module.getParentFile(), "locale"), module.getName()); //NOI18N
                            if (!variant.isFile()) throw new BuildException(bundlename + " not found in " + module, getLocation());
                            long vmMod = variant.lastModified();
                            if (mostRecentInput < vmMod) mostRecentInput = vmMod;
                            try (ZipFile variantjar = new ZipFile(variant)) {
                                bundleentry = variantjar.getEntry(bundlename);
                                if (bundleentry == null) throw new BuildException(bundlename + " not found in " + module + " nor in " + variant, getLocation());
                                InputStream is = variantjar.getInputStream(bundleentry);
                                try {
                                    p.load(is);
                                } finally {
                                    is.close();
                                }
                            }
                        }
                        // Now pick up attributes from the bundle.
                        for(String key: p.stringPropertyNames()) {
                            if(key.startsWith("OpenIDE-Module-")) {
                                attr.putValue(key, p.getProperty(key));
                            }
                        }
                    } // else all loc attrs in main manifest, OK
                }
            } catch (IOException ioe) {
                throw new BuildException("exception while reading " + module, ioe, getLocation());
            }
        } // else we will read attr later if info file is out of date
	boolean skipInfo = false;
	if (infofile.exists ()) {
	    // Check for up-to-date w.r.t. manifest and maybe license file.
	    long iMod = infofile.lastModified ();
	    if (mostRecentInput < iMod)
		skipInfo = true;
	}
	if (! skipInfo) {
	    log ("Creating NBM info file " + infofile);
            if (manifest != null) {
                // Read module manifest for main attributes.
                try {
                    try (InputStream manifestStream = new FileInputStream (manifest)) {
                        attr = new Manifest (manifestStream).getMainAttributes ();
                    }
                } catch (IOException e) {
                    throw new BuildException("exception when reading manifest " + manifest, e, getLocation());
                }
            } // else we read attr before
	    try {
		try (OutputStream infoStream = new FileOutputStream (infofile)) {
                    PrintWriter ps = new PrintWriter(new OutputStreamWriter(infoStream, "UTF-8"));
		    // Begin writing XML.
                    ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //NOI18N
                    ps.println("<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Autoupdate Module Info 2.4//EN\" \"http://www.netbeans.org/dtds/autoupdate-info-2_4.dtd\">"); //NOI18N
		    if( attr != null) {
		      String codenamebase = attr.getValue ("OpenIDE-Module"); //NOI18N
		      if (codenamebase == null)
			throw new BuildException("invalid manifest, does not contain OpenIDE-Module", getLocation());
		      // Strip major release number if any.
		      codenamebase = getCodenameBase( codenamebase) ;
		      ps.println ("<module codenamebase=\"" + xmlEscape(codenamebase) + "\""); //NOI18N
		    }
		    else {
		      ps.print( "<module "); //NOI18N
		      if( modInfo != null && !modInfo.trim().equals( "")) {
			String codenamebase = getCodenameBase( modInfo) ;
			ps.println( "codenamebase=\"" + xmlEscape(codenamebase) + "\""); //NOI18N
		      }
		      else {
			ps.println( "") ; //NOI18N
		      }
		    }
		    if (homepage != null)
                        ps.println ("        homepage=\"" + xmlEscape(homepage) + "\""); //NOI18N
		    if (distribution != null) {
                        ps.println ("        distribution=\"" + xmlEscape(distribution) + "\""); //NOI18N
                    } else {
                        throw new BuildException("NBM distribution URL is not set", getLocation());
                    }
		    // Here we only write a name for the license.
		    if (license != null) {
			String name = license.getName ();
			if (name == null)
			    throw new BuildException("Every license must have a name or file attribute", getLocation());
                        ps.println ("        license=\"" + xmlEscape(name) + "\""); //NOI18N
		    }
		    ps.println ("        downloadsize=\"0\""); //NOI18N
                    if (needsrestart != null)
                        ps.println ("        needsrestart=\"" + xmlEscape(needsrestart) + "\""); //NOI18N
		    ps.println (">"); //NOI18N
		    if (description != null) {
			ps.print ("  <description>"); //NOI18N
			ps.print (description.getText ());
			ps.println ("</description>"); //NOI18N
                    }

		    // Write manifest attributes.
		    if( attr != null) {
		        ps.print ("  <manifest "); //NOI18N
			boolean firstline = true;
		        List<String> attrNames = new ArrayList<>(attr.size());
			Iterator<Object> it = attr.keySet().iterator();
			while (it.hasNext()) {
			    attrNames.add(((Attributes.Name)it.next()).toString());
			}
			Collections.sort(attrNames);
                        for (String name: attrNames) {
			    // Ignore irrelevant attributes (cf. www/www/dtds/autoupdate-catalog-2_0.dtd
			    //  and www/www/dtds/autoupdate-info-1_0.dtd):
			    if (! name.startsWith("OpenIDE-Module")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Localizing-Bundle")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Install")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Layer")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Description")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Package-Dependency-Message")) continue; //NOI18N
			    if (name.equals("OpenIDE-Module-Public-Packages")) continue; //NOI18N
			    if (firstline)
			        firstline = false;
			    else
			      ps.print ("            "); //NOI18N
			    ps.println(name + "=\"" + xmlEscape(attr.getValue(name)) + "\""); //NOI18N
			}
			ps.println ("  />"); //NOI18N
		    }
		    else if( modInfo != null && !modInfo.trim().equals( "")) { //NOI18N
		      String specver, majorver ;

		      // Write the l10n tag and lang/branding codes. //
		      ps.println("  <l10n "); //NOI18N
		      if( langCode != null && !langCode.trim().equals( "")) { //NOI18N
			ps.println( "        langcode=\"" + xmlEscape(langCode) + "\"") ; //NOI18N
		      }
		      if( brandingCode != null && !brandingCode.trim().equals( "")) { //NOI18N
			ps.println( "        brandingcode=\"" + xmlEscape(brandingCode) + "\"") ; //NOI18N
		      }

		      // Write the major version if possible. //
		      majorver = getMajorVer( modInfo) ;
		      if( majorver != null && !majorver.trim().equals( "")) { //NOI18N
			ps.println( "        module_major_version=\"" + xmlEscape(majorver) + "\"") ; //NOI18N
		      }

		      // Write the spec version if possible. //
		      specver = getSpecVer( modInfo) ;
		      if( specver != null && !specver.trim().equals( "")) { //NOI18N
			ps.println( "        module_spec_version=\"" + xmlEscape(specver) + "\"") ; //NOI18N
		      }

		      // Read localizing bundle and write relevant attr's. //
		      if( locBundle != null) {
			writeLocBundleAttrs( ps) ;
		      }

		      ps.println( "  />") ; //NOI18N
		    }

		    // Maybe write out license text.
		    if (license != null) {
                        ps.print ("  <license name=\"" + xmlEscape(license.getName ()) + "\">"); //NOI18N
			ps.print (license.getText ());
			ps.println ("</license>"); //NOI18N
		    }
                    if (notification != null) {
                        ps.print("  <module_notification>"); //NOI18N
                        ps.print(notification.getText());
                        ps.println("</module_notification>"); //NOI18N
		    }
		    if (externalPackages != null) {
			Enumeration<ExternalPackage> exp = externalPackages.elements();
			while (exp.hasMoreElements()) {
			    ExternalPackage externalPackage = exp.nextElement();
			    if (externalPackage.name == null || 
				externalPackage.targetName == null ||
				externalPackage.startUrl == null)
				throw new BuildException("Must define name, targetname, starturl for external package");
			    ps.print("  <external_package "); //NOI18N
			    ps.print("name=\""+xmlEscape(externalPackage.name)+"\" "); //NOI18N
			    ps.print("target_name=\""+xmlEscape(externalPackage.targetName)+"\" "); //NOI18N
			    ps.print("start_url=\""+xmlEscape(externalPackage.startUrl)+"\""); //NOI18N
			    if (externalPackage.description != null)
				ps.print(" description=\""+xmlEscape(externalPackage.description)+"\""); //NOI18N
			    ps.println("/>"); //NOI18N
			}
		    }
		    ps.println ("</module>"); //NOI18N
                    ps.flush();
		}
	    } catch (IOException e) {
		throw new BuildException("exception when creating Info/info.xml", e, getLocation());
	    }
	}

	// JAR it all up together.
	long jarModified = file.lastModified (); // may be 0
	//log ("Ensuring existence of NBM file " + file);
	Jar jar = (Jar) getProject().createTask("jar"); //NOI18N
        jar.setDestFile(file);
	//jar.setBasedir (topdir.getAbsolutePath ());
        jar.setCompress(true);
	//jar.createInclude ().setName ("netbeans/"); //NOI18N
	//jar.createInclude ().setName ("Info/info.xml"); //NOI18N
        jar.addFileset (getFileSet());
	jar.setLocation(getLocation());
	jar.init ();
	jar.execute ();
	// Maybe sign it.
	if (signature != null && file.lastModified () != jarModified) {
	    if (signature.keystore == null)
		throw new BuildException ("must define keystore attribute on <signature/>");
	    if (signature.storepass == null)
		throw new BuildException ("must define storepass attribute on <signature/>");
	    if (signature.alias == null)
		throw new BuildException ("must define alias attribute on <signature/>");
            if (signature.storepass.equals ("?") || !signature.keystore.exists()) {
                log ("Not signing NBM file " + file + "; no stored-key password provided or keystore (" 
		     + signature.keystore.toString() + ") doesn't exist", Project.MSG_WARN);
            } else {
                log ("Signing NBM file " + file);
                SignJar signjar = (SignJar) getProject().createTask("signjar"); //NOI18N
                //I have to use Reflection API, because there was changed API in ANT1.5
                try {
                    try {
                        Class<?>[] paramsT = {String.class};
                        Object[] paramsV1 = {signature.keystore.getAbsolutePath()};
                        Object[] paramsV2 = {file.getAbsolutePath()};
                        signjar.getClass().getDeclaredMethod( "setKeystore", paramsT ).invoke( signjar, paramsV1 ); //NOI18N
                        signjar.getClass().getDeclaredMethod( "setJar", paramsT ).invoke( signjar, paramsV2 ); //NOI18N
                    } catch (NoSuchMethodException ex1) {
                        //Probably ANT 1.5
                        try {
                            Class<?>[] paramsT = {File.class};
                            Object[] paramsV1 = {signature.keystore};
                            Object[] paramsV2 = {file};
                            signjar.getClass().getDeclaredMethod( "setKeystore", paramsT ).invoke( signjar, paramsV1 ); //NOI18N
                            signjar.getClass().getDeclaredMethod( "setJar", paramsT ).invoke( signjar, paramsV2 ); //NOI18N
                        } catch (NoSuchMethodException ex2) {
			    //Probably ANT1.5.3
			    try {
				Class<?>[] paramsT1 = {File.class};
				Class<?>[] paramsT2 = {String.class};
				Object[] paramsV1 = {signature.keystore.getAbsolutePath()};
				Object[] paramsV2 = {file};
				signjar.getClass().getDeclaredMethod( "setKeystore", paramsT2 ).invoke( signjar, paramsV1 ); //NOI18N
				signjar.getClass().getDeclaredMethod( "setJar", paramsT1 ).invoke( signjar, paramsV2 ); //NOI18N
			    }   catch (NoSuchMethodException ex3) {
                                throw new BuildException("Unknown Ant version, only Ant 1.6.5+ is currently supported.");
                            }
                        }
                    }
                } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException ex4) {
                    throw new BuildException(ex4);
                }
                signjar.setStorepass (signature.storepass);
                signjar.setAlias (signature.alias);
                signjar.setLocation(getLocation());
                signjar.init ();
                signjar.execute ();
            }
	}
    }
   
    // Reflection access from MakeListOfNBM:
    
    public FileSet getFileSet() {
        FileSet fs = fileset;		//makes in apperance to excludes and includes files defined in XML
        fs.setDir (topdir);

	if (isStandardInclude) {
	  fs.createInclude ().setName ("netbeans/"); //NOI18N
	  fs.createExclude ().setName ("netbeans/update_tracking/*.xml"); //NOI18N
	}

	fs.createInclude ().setName ("Info/info.xml"); //NOI18N
        return fs;
    }

    public Attributes getAttributes() throws IOException {
        if (manifest != null) {
            try (InputStream is = new FileInputStream(manifest)) {
                return new Manifest(is).getMainAttributes();
            }
        } else if (module != null) {
            try (JarFile jar = new JarFile(module)) {
                return jar.getManifest().getMainAttributes();
            }
        } else {
            throw new IOException(getLocation() + "must give either 'manifest' or 'module' on <makenbm>");
        }
    }

  protected String getCodenameBase( String openide_module) {
    String ret = openide_module ;
    int idx = ret.indexOf ('/'); //NOI18N
    if (idx != -1) {
      ret = ret.substring (0, idx);
    }
    return( ret) ;
  }

  protected String getSpecVer( String mod_info) {
    String ret = null ;
    int first_idx, second_idx ;

    // If there are 2 slashes. //
    first_idx = mod_info.indexOf( '/') ; //NOI18N
    if( first_idx != -1) {
      second_idx = mod_info.indexOf( '/', first_idx+1) ; //NOI18N
      if( second_idx != -1) {

	// Return the string after the second slash. //
	ret = mod_info.substring( second_idx+1, mod_info.length()) ;
      }
    }

    // Return null rather than an empty string. //
    if( ret != null && ret.trim().equals( "")) { //NOI18N
      ret = null ;
    }
    return( ret) ;
  }

  protected String getMajorVer( String mod_info) {
    String ret = null ;
    int first_idx, second_idx ;

    // If there are 2 slashes. //
    first_idx = mod_info.indexOf( '/') ; //NOI18N
    if( first_idx != -1) {
      second_idx = mod_info.indexOf( '/', first_idx+1) ; //NOI18N
      if( second_idx != -1) {

	// Return the string between the slashes. //
	ret = mod_info.substring( first_idx+1, second_idx) ;
      }

      // Else return the string after the first slash. //
      else {
	ret = mod_info.substring( first_idx+1, mod_info.length()) ;
      }
    }

    // Return null rather than an empty string. //
    if( ret != null && ret.trim().equals( "")) { //NOI18N
      ret = null ;
    }
    return( ret) ;
  }

  /** For l10n NBM's, this is the localizing bundle file 
   * that we'll look in to get module name, description, etc.
   */
  public void setLocBundle( File f) {
    locBundle = f ;
  }

  /** See reqManOrMod() */
  public void setManOrModReq( boolean b) {
    manOrModReq = b ;
    manOrModReqSet = true ;
  }

  /** If the manifest and module aren't required, use this 
   * to set the module codename, major version and spec version.
   */
  public void setModInfo( String s) {
    modInfo = s ;
  }

  /** Set the language code for localized NBM's. */
  public void setLangCode( String s) {
    langCode = s ;
  }

  /** Set the branding code for branded NBM's. */
  public void setBrandingCode( String s) {
    brandingCode = s ;
  }

  /** Returns true if either a manifest or a module must be specified.
   * This is true unless either the global property
   * makenbm.manOrModReq is false, or the manOrModReq attribute of
   * this task is false.  The attribute, if set, has priority over the
   * global property.
   */
  public boolean reqManOrMod() {
    boolean req = true ;

    if( manOrModReqSet) {
      req = manOrModReq ;
    }
    else {
      String s = getProject().getProperty("makenbm.manOrModReq"); //NOI18N
      if( s != null && !s.equals( "")) { //NOI18N
	req = Project.toBoolean(s);
      }
    }

    return( req) ;
  }

  protected void writeLocBundleAttrs( PrintWriter ps) {
    FileInputStream fis ;
    Properties p = new Properties() ;
    String s ;
    boolean hadone = false ;

    try {
      fis = new FileInputStream( locBundle) ;
      p.load( fis);
      fis.close();
    }
    catch( Exception e) {
      System.out.println( "ERROR: " + e.getMessage()) ;
      e.printStackTrace() ;
      throw new BuildException() ;
    }

    s = p.getProperty( "OpenIDE-Module-Name") ; //NOI18N
    if( writeProp( "OpenIDE-Module-Name", s, ps)) { //NOI18N
      hadone = true ;
    }

    s = p.getProperty( "OpenIDE-Module-Long-Description") ; //NOI18N
    if( writeProp( "OpenIDE-Module-Long-Description", s, ps)) { //NOI18N
      hadone = true ;
    }

    if( !hadone) {
      log( "WARNING: Localizing bundle had neither property: " + locBundle) ;
    }
  }

  protected boolean writeProp( String name,
			       String val,
			       PrintWriter ps) {
    boolean ret = false ;
    if( val != null) {
      ps.println( name + "=\"" + xmlEscape(val) +"\"") ; //NOI18N
      ret = true ;
    }
    return( ret) ;
  }

}
