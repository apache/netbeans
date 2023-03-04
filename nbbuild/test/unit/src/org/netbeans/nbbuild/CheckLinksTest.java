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

/** Check the behaviour of CheckLinks.
 *
 * @author Jaroslav Tulach
 */
public class CheckLinksTest extends TestBase {
    public CheckLinksTest (String name) {
        super (name);
    }

    public void testByDefaultAllURLsAreAllowed () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // success
        execute (f, new String[] { });
    }

    
    public void testForbiddenExternalURLsAreCorrectlyReported () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='http://www.netbeans.org/download/[a-zA-Z0-9\\.]*/javadoc/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        try {
            execute (f, new String[] { });
            fail ("This should fail as the URL is forbidden");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testForbiddenURLsInLinkElements() throws Exception {
        File html = extractHTMLFile("<html><head><link rel=\"stylesheet\" href=\"http://www.netbeans.org/netbeans.css\" type=\"text/css\"></head>\n");
        File f = extractString(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\">" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\">" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "'>" +
            "    <include name=\"" + html.getName() + "\"/>" +
            "    <filter accept='false' pattern='http://www\\.netbeans\\.org/netbeans\\.css'/>" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        try {
            execute(f, new String[] {});
            fail();
        } catch (ExecutionError ex) {}
    }
  
    public void testAnyURLCanBeForbidden () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='http://www.sex.org/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        try {
            execute (f, new String[] { });
            fail ("This should fail as the URL is forbidden");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testIfAcceptedFirstThenItDoesNotMatterThatItIsForbiddenLater () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='true' pattern='.*sex.*' /> " +
            "    <filter accept='false' pattern='http://www.sex.org/.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // passes as .*sex.* is acceptable
        execute (f, new String[] { });
    }
    
    
    public void testSkipCommentedOutLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            " <!-- This is commented out \n" + 
            "<a href=\"http://www.sex.org/index.hml\">Forbidden link</a>\n" +
            "  here ends the comment -->" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "    <filter accept='false' pattern='.*sex.*' /> " +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // passes as the forbidden URL is commented out
        execute (f, new String[] { });
    }
    
    
    public void testDocFilesRelativeLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"#RelativeLink\">This link should pass the checking</a>\n" +
	    "<a name=\"RelativeLink\"/>\n" + 
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // success
        execute (f, new String[] { });
    }
    
    
    public void testDocFilesInvalidLinks () throws Exception {
        java.io.File html = extractHTMLFile (
            "<head></head><body>\n" +
            "<a href=\"#InvalidLink\">This link should NOT pass the checking</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html.getParent() + "' >" +
            "    <include name=\"" + html.getName () + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        // failure
        try {
            execute (f, new String[] { });
            fail ("This should fail as the link is broken");
        } catch (ExecutionError ex) {
            // ok, this should fail on exit code
        }
    }

    public void testQueryComponent() throws Exception {
        File html1 = extractHTMLFile(
            "<head></head><body></body>"
        );
        File html2 = extractHTMLFile(
            "<head></head><body>\n" +
            "<a href=\"" + html1.toURI() + "?is-external=true\">ought to be OK</a>\n" +
            "</body>"
        );
        File f = extractString(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checklinks\" classname=\"org.netbeans.nbbuild.CheckLinks\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checklinks checkexternal='false' basedir='" + html1.getParent() + "' >" +
            "    <include name=\"" + html1.getName() + "\" />" +
            "    <include name=\"" + html2.getName() + "\" />" +
            "  </checklinks>" +
            "</target>" +
            "</project>"
        );
        execute(f, new String[] {});
    }
    
    
    private File extractHTMLFile (String s) throws Exception {
        File f = extractString (s);
        File n = new File (f.getParentFile (), f.getName () + ".html");
        assertTrue ("Rename succeeded", f.renameTo (n));
        return n;
    }
    
}
