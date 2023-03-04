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

import org.apache.tools.ant.* ;
import org.apache.tools.ant.taskdefs.* ;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/** This task runs the JHIndexer task multiple times, once for each
 * language, and automatically creates the appropriate regular
 * expressions to choose the files for each language.  This task
 * greatly reduces the amount of Ant code required to run JHIndexer
 * especially when there are helpsets for multiple languages in the
 * same directory tree.
 *
 * @author Jerry Huth (email: jerry@solidstep.com)
 */
public class LocJHIndexer extends MatchingTask {

  private Path classpath;
  protected File basedir = null ;
  protected String dbdir = null ;
  protected String locales = null ;
  protected String jhall = null ;

  /** Set the location of <samp>jhall.jar</samp> or <samp>jsearch.jar</samp> (JavaHelp tools library). */
  public Path createClasspath() {
      // JavaHelp release notes say jhtools.jar is enough, but class NoClassDefFoundError
      // on javax.help.search.IndexBuilder when I tried it...
      if (classpath == null) {
          classpath = new Path(getProject());
      }
      return classpath.createPath();
  }
  /** Specify a regular expression to find <samp>jhall.jar</samp>
   * (JavaHelp tools library).
   */
  public void setJhall( String jhall) {
    this.jhall = jhall;
  }

  /** Get the jhall jar file to use. */
  protected String getJhall() {
    String ret = null ;
    String prop = null ;

    // Use the attribute if specified. //
    if( jhall != null) {
      ret = jhall ;
    }
    else {

      // Else look for the global property. //
      prop = getProject().getProperty("locjhindexer.jhall");
      if( prop != null) {
	ret = prop ;
      }
    }

    return( ret) ;
  }

  /** Set the location of the docs helpsets' base dir. */
  public void setBasedir( File dir) {
    basedir = dir ;
  }

  /** Set the name of the search database directory (which is under
   * <samp>basedir/&lt;locale></samp>)
   */
  public void setDbdir( String dir) {
    dbdir = dir ;
  }

  /** Set a comma-separated list of locales which have helpsets. */
  public void setLocales( String s) {
    locales = s ;
  }

  /** Get the locales for which we'll look for helpsets. */
  protected String getLocales() {
    if( locales != null) {
      return( locales) ;
    }
    return(getProject().getProperty("locjhindexer.locales"));
  }

  public void execute() throws BuildException {
    String locs = getLocales() ;
    String helpset_locs = null ;
    StringTokenizer tokenizer = null ;
    String loc = null ;

    if( getJhall() == null)
      throw new BuildException( "Must specify the jhall attribute") ;
    if( dbdir == null || dbdir.trim().equals( ""))
      throw new BuildException( "Must specify the dbdir attribute") ;
    if( basedir == null)
      throw new BuildException( "Must specify the basedir attribute") ;
    if( locs == null || locs.trim().equals( ""))
      throw new BuildException( "Must specify the locales attribute") ;

    // I couldn't get it to work unless I explicitly added the task def here. //
    getProject().addTaskDefinition("jhindexer", JHIndexer.class);

    // For each locale. //
    tokenizer = new StringTokenizer( locs, ", ") ;
    while( tokenizer.hasMoreTokens()) {
      loc = tokenizer.nextToken() ;

      // If there's a helpset for this locale. //
      if( hasHelpset( loc)) {

	// Add it to the list of locales that have helpsets. //
	if( helpset_locs == null) {
	  helpset_locs = new String( loc) ;
	}
	else {
	  helpset_locs += "," + loc ;
	}
      }
    }

    // For each locale. //
    if( helpset_locs != null) {
      tokenizer = new StringTokenizer( helpset_locs, ", ") ;
      while( tokenizer.hasMoreTokens()) {
	loc = tokenizer.nextToken() ;

	// Run the JHIndexer for this locale. //
	RunForLocale( loc) ;
      }
    }
  }

  /** See if there's a helpset for this locale. */
  protected boolean hasHelpset( String loc) {
    boolean ret = false ;
    LocHelpsetFilter filter = new LocHelpsetFilter( loc) ;
    File files[] ;

    files = basedir.listFiles( filter) ;
    if( files != null && files.length > 0) {
      ret = true ;
    }

    return( ret) ;
  }

  // Run JHIndexer for the given locale. //
  protected void RunForLocale( String locale) throws BuildException {
    JHIndexer jhindexer ;

    jhindexer = (JHIndexer) getProject().createTask("jhindexer");
    jhindexer.init() ;

    jhindexer.setIncludes( locale + "/**/*.htm*") ;
    jhindexer.setExcludes( locale + "/" + dbdir + "/" + "," +
			   locale + "/credits.htm*") ;
    jhindexer.setBasedir( new File( basedir + "/")) ;
    jhindexer.setDb( new File( basedir + "/" + locale + "/" + dbdir)) ;
    jhindexer.setLocale( locale) ;
    jhindexer.createClasspath().add(classpath);
    setJHLib( jhindexer) ;

    jhindexer.execute() ;
  }

  protected void setJHLib( JHIndexer jhindexer) {
    String jhlib, dir, regexp ;
    int idx, i ;
    FileSet fs ;
    File file ;
    LinkedList<String> dirs, regexps ;
    StringTokenizer st ;
    Path path ;

    // For each regular expression. //
    dirs = new LinkedList<>() ;
    regexps = new LinkedList<>() ;
    jhlib = getJhall() ;
    st = new StringTokenizer( jhlib, " 	\n,") ;
    while( st.hasMoreTokens()) {
      regexp = st.nextToken() ;

      // Break the regular expression up into directory and file //
      // components.						 //
      idx = regexp.lastIndexOf( "/") ;
      dir = regexp.substring( 0, idx) ;
      file = new File( dir) ;
      if( file.exists()) {
	dirs.add( dir) ;
	regexps.add( regexp.substring( idx+1)) ;
      }
    }

    if( dirs.size() > 0) {
      path = jhindexer.createClasspath() ;
      for( i = 0; i < dirs.size(); i++) {
	dir = dirs.get( i) ;
	regexp = regexps.get( i) ;
	fs = new FileSet() ;
	fs.setDir( new File( dir)) ;
	fs.setIncludes( regexp) ;
	path.addFileset( fs) ;
      }
    }
    else {
      throw new BuildException( "jhall not found.") ;
    }
  }

  protected class LocHelpsetFilter implements FilenameFilter {
    protected String locale = null ;

    public LocHelpsetFilter( String loc) {
      locale = loc ;
    }

    public boolean accept(File dir,
			  String name) {
      return( name.endsWith( "_" + locale + ".hs")) ;
    }
  }

}

