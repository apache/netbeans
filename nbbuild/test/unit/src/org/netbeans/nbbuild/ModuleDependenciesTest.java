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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Check the behaviour ModuleDependencies task that prints out info about
 * module dependencies, etc.
 *
 * @author Jaroslav Tulach
 */
public class ModuleDependenciesTest extends TestBase {
    public ModuleDependenciesTest (String name) {
        super (name);
    }
    /*
    public void testJustMakeSureWeAreAbleToParseTheStructure () throws Exception {
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"${nb_all}/nbbuild\" > " +
            "        <include name=\"nbantext.jar\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"file.txt\" />" +
            "  </deps >" +
            "  " +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { });
    }*/
    
    public void testPublicPackagesOfAModuleThatDoesNotDefineThem () throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "is/X.class", "is/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "is.there.*, is.recursive.**");
        File withPkgs = generateJar (new String[] { "is/there/A.class", "not/there/B.class", "is/recursive/Root.class", "is/recursive/sub/Under.class", "not/res/X.jpg"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.very.public.module/10");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        File allPkgs = generateJar (new String[] { "not/very/A.class", "not/very/B.class", "not/very/sub/Root.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        assertEquals ("All parents are the same 3", parent, allPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "        <include name=\"" + allPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
        assertEquals ("No not package", -1, res.indexOf ("not"));
        assertTrue ("Some of is pkgs: " + res, res.indexOf ("is\n") >= 0);
        assertEquals ("No default pkg", -1, res.indexOf ("\n\n"));
        assertTrue ("is there resursive: " + res, res.indexOf ("is.recursive\n") >= 0);
        assertTrue ("is there too: " + res, res.indexOf ("is.too\n") >= 0);
    }

    public void testPublicPackagesInAModuleThatDeclaresFriendsAreNotCounted() throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "just/friend/X.class", "just/friend/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "friend.there.*, friend.recursive.**");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module");
        File withPkgs = generateJar (new String[] { "friend/there/A.class", "not/there/B.class", "friend/recursive/Root.class", "friend/recursive/sub/Under.class", "not/res/X.jpg"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.very.public.module/10");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        File allPkgs = generateJar (new String[] { "not/very/A.class", "not/very/B.class", "not/very/sub/Root.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        assertEquals ("All parents are the same 3", parent, allPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        File friendPkg = extractString ("");
        friendPkg.delete ();
	assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "        <include name=\"" + allPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"" + output + "\" />" +
            "    <output type=\"friend-packages\" file=\"" + friendPkg + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
	if (!res.isEmpty()) {
	    fail("No public packages:\n'" + res + "'");
	}
	
	// now friend packages
	res = readFile(friendPkg);
	
	Matcher match = Pattern.compile("MODULE ([^ ]*)").matcher(res);
	assertTrue("One MODULE is there: " + res, match.find());
	int fst = match.start();
	assertEquals("my.another.module", match.group(1));
	
	assertTrue("Second MODULE is there: " + res, match.find());
	int snd = match.start();
	assertEquals("my.module", match.group(1));

	match = Pattern.compile("  FRIEND my.very.public.module \\(ahoj\\)").matcher(res);
	assertTrue("One FRIEND is there: " + res, match.find());
	assertTrue("Second FRIEND is there: " + res, match.find());
	
	assertTrue("FriendPkg1\n" + res, res.indexOf("just.friend") >= snd);
	assertTrue("FriendPkg2\n" + res, res.indexOf("friend.there") >= fst);
	
    }
    public void testThereCanBeLimitOnNumberOfFriends() throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "just/friend/X.class", "just/friend/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "friend.there.*, friend.recursive.**");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module, my.module");
        File withPkgs = generateJar (new String[] { "friend/there/A.class", "not/there/B.class", "friend/recursive/Root.class", "friend/recursive/sub/Under.class", "not/res/X.jpg"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.very.public.module/10");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        File allPkgs = generateJar (new String[] { "not/very/A.class", "not/very/B.class", "not/very/sub/Root.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        assertEquals ("All parents are the same 3", parent, allPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        File friendPkg = extractString ("");
        friendPkg.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <property name='deps.max.friends' value='1'/>" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "        <include name=\"" + allPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"" + output + "\" />" +
            "    <output type=\"friend-packages\" file=\"" + friendPkg + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        // this should succeed now, as the limit applies only to intercluster relations - #87076
        execute (f, new String[] { "-verbose" });
    }

    public void testThereCanBeLimitOnNumberOfFriendsAmongGroups() throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module");
        File myModule = generateJar (new String[] { "DefaultPkg.class", "just/friend/X.class", "just/friend/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "friend.there.*, friend.recursive.**");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module, my.module");
        File myAnotherModule = generateJar (new String[] { "friend/there/A.class", "not/there/B.class", "friend/recursive/Root.class", "friend/recursive/sub/Under.class", "not/res/X.jpg"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.very.public.module/10");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        File myVeryPublicModule = generateJar (new String[] { "not/very/A.class", "not/very/B.class", "not/very/sub/Root.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, myModule.getParentFile ());
        assertEquals ("All parents are the same 2", parent, myAnotherModule.getParentFile ());
        assertEquals ("All parents are the same 3", parent, myVeryPublicModule.getParentFile ());
        
        
        File friendPkg = extractString ("");
        friendPkg.delete ();
    	assertFalse ("Is gone", friendPkg.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <property name='deps.max.friends' value='${limit}'/>" +
            "  <deps>" +
            "    <input name=\"base\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + myAnotherModule.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"extra\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + myModule.getName () + "\" />" +
            "        <include name=\"" + myVeryPublicModule.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"group-friend-packages\" file=\"" + friendPkg + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );

        execute (f, new String[] { "-verbose", "-Dlimit=1" });
        
        String res = readFile (friendPkg);

        assertEquals(
            "MODULE my.another.module (base)\n" +
            "  FRIEND my.module (extra)\n" +
            "  FRIEND my.very.public.module (extra)\n" +
            "  WARNING: excessive number of intercluster friends (2)\n" +
            "  PACKAGE friend.recursive\n" +
            "  PACKAGE friend.recursive.sub\n" +
            "  PACKAGE friend.there\n" +
            "", res);
    }
    
    public void testThereExternalsAreCountedAsWell() throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module");
        File myModule = generateJar (new String[] { "DefaultPkg.class", "just/friend/X.class", "just/friend/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "friend.there.*, friend.recursive.**");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Friends", "my.very.public.module, my.module");
        File myAnotherModule = generateJar (new String[] { "friend/there/A.class", "not/there/B.class", "friend/recursive/Root.class", "friend/recursive/sub/Under.class", "not/res/X.jpg"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.very.public.module/10");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "-");
        File myVeryPublicModule = generateJar (new String[] { "not/very/A.class", "not/very/B.class", "not/very/sub/Root.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, myModule.getParentFile ());
        assertEquals ("All parents are the same 2", parent, myAnotherModule.getParentFile ());
        assertEquals ("All parents are the same 3", parent, myVeryPublicModule.getParentFile ());
        
        
        File friendPkg = extractString ("");
        friendPkg.delete ();
    	assertFalse ("Is gone", friendPkg.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <property name='deps.max.friends' value='${limit}'/>" +
            "  <deps>" +
            "    <input name=\"base\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + myAnotherModule.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"group-friend-packages\" file=\"" + friendPkg + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );

        execute (f, new String[] { "-verbose", "-Dlimit=1" });
        
        String res = readFile (friendPkg);

        assertEquals(
            "MODULE my.another.module (base)\n" +
            "  EXTERNAL my.module\n" +
            "  EXTERNAL my.very.public.module\n" +
            "  WARNING: excessive number of intercluster friends (2)\n" +
            "  PACKAGE friend.recursive\n" +
            "  PACKAGE friend.recursive.sub\n" +
            "  PACKAGE friend.there\n" +
            "", res);
    }
    
    public void testPublicPackagesOfAModuleThatDoesNotDefineThemButTheyAreProvidedByModuleTheModuleDependsOn () throws Exception {
        File notAModule = generateJar (new String[] { "is/cp/X.class", "not/cp/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("Class-Path", notAModule.getName ());
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "is/X.class", "is/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir='" + parent + "' > " +
            "        <include name='" + notAModule.getName () + "' />" +
            "        <include name='" + withoutPkgs.getName () + "' />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
        assertEquals ("No not package", -1, res.indexOf ("not"));
        assertTrue ("Some of is pkgs: " + res, res.indexOf ("is\n") >= 0);
        assertTrue ("is/too pkgs: " + res, res.indexOf ("is.too\n") >= 0);
        assertEquals ("No default pkg", -1, res.indexOf ("\n\n"));
        assertTrue ("is/cp is there as well as the withoutPkgs module depends on notAModule: " + res, res.indexOf ("is.cp\n") >= 0);
    }
    
    public void testPublicPackagesForOneCluster() throws Exception {
        Manifest m1 = createManifest ();
        m1.getMainAttributes ().putValue ("OpenIDE-Module", "my.ignored.module/3");
        File ignoreModule = generateJar (new String[] { "DefaultPkg.class", "is2/X.class", "is2/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m1);
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "is/X.class", "is/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        File parent = ignoreModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps generate='ahoj'>" +
            "    <input name=\"ignore\" >" +
            "      <jars dir='" + parent + "' > " +
            "        <include name='" + ignoreModule.getName () + "' />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir='" + parent + "' > " +
            "        <include name='" + withoutPkgs.getName () + "' />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"public-packages\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
        assertEquals ("No not package", -1, res.indexOf ("not"));
        assertEquals ("No is2 package", -1, res.indexOf ("is2"));
        assertTrue ("Some of is pkgs: " + res, res.indexOf ("is\n") >= 0);
        assertTrue ("is/too pkgs: " + res, res.indexOf ("is.too\n") >= 0);
        assertEquals ("No default pkg", -1, res.indexOf ("\n\n"));
    }

    public void testSharedPackagesForOneCluster() throws Exception {
        Manifest m0 = createManifest ();
        m0.getMainAttributes ().putValue ("OpenIDE-Module", "my.huge.module/3");
        File hugeModule = generateJar (new String[] { "not/X.class", "is/too/MyClass.class", }, m0);
        
        
        Manifest m1 = createManifest ();
        m1.getMainAttributes ().putValue ("OpenIDE-Module", "my.ignored.module/3");
        File ignoreModule = generateJar (new String[] { "not/X.class" }, m1);
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "is/too/MyClass.class" }, m);
        
        File parent = ignoreModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps generate='ahoj'>" +
            "    <input name=\"ignore\" >" +
            "      <jars dir='" + parent + "' > " +
            "        <include name='" + ignoreModule.getName () + "' />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir='" + parent + "' > " +
            "        <include name='" + hugeModule.getName () + "' />" +
            "        <include name='" + withoutPkgs.getName () + "' />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"shared-packages\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
        assertEquals ("No not package:\n" + res, -1, res.indexOf ("not"));
        assertTrue ("is/too pkgs: " + res, res.indexOf ("is.too\n") >= 0);
        assertEquals ("No default pkg:\n" + res, -1, res.indexOf ("\n\n"));
    }
    
    public void testNameOfModuleWithoutMajorVersionDoesNotContainSlash () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module");
        File module = generateJar (new String[] { "something" }, m);
        
        File parent = module.getParentFile ();
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + module.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"modules\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);

        assertEquals ("Starts with MODULE", 0, res.indexOf ("MODULE"));
        assertTrue ("module name is there" + res, res.indexOf ("my.module") > 0);
        assertEquals ("no slash is there", -1, res.indexOf ('/'));
    }
    
    
    public void testGenerateListOfModules () throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "is/X.class", "is/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "is.there.*, is.recursive.**");
        File withPkgs = generateJar (new String[] { "is/there/A.class", "not/there/B.class", "is/recursive/Root.class", "is/recursive/sub/Under.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"modules\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        StringTokenizer tok = new StringTokenizer (res, "\n\r");
        
        assertEquals ("We have two modules: " + res, 2, tok.countTokens ());
        assertEquals ("First contains another module, as it is sooner in alphabet\n" + res, "MODULE my.another.module/3 (ahoj)", tok.nextToken ());
        assertEquals ("Second the next one" + res, "MODULE my.module/3 (ahoj)", tok.nextToken ());
        assertFalse ("No next tokens", tok.hasMoreElements ());
    }
    
    public void testGenerateListOfForOneCluster () throws Exception {
        File notAModule = generateJar (new String[] { "not/X.class", "not/resource/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "DefaultPkg.class", "is/X.class", "is/too/MyClass.class", "not/as/it/is/resource/X.xml" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Public-Packages", "is.there.*, is.recursive.**");
        File withPkgs = generateJar (new String[] { "is/there/A.class", "not/there/B.class", "is/recursive/Root.class", "is/recursive/sub/Under.class", "not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps generate='ahoj'>" +
            "    <input name=\"ignore\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"modules\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        StringTokenizer tok = new StringTokenizer(res, "\n\r");
        assertEquals("Should be empty:\n" + res, 0, tok.countTokens());
    }
    
    public void testGenerateModuleDependencies () throws Exception {
        Manifest openideManifest = createManifest ();
        openideManifest.getMainAttributes ().putValue ("OpenIDE-Module", "org.openide/1");
        File openide = generateJar (new String[] { "notneeded" }, openideManifest);
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openide/1 > 4.17");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3, org.openide/1 > 4.17");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = openide.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int y = res.indexOf ("MODULE", 1);
        if (y <= 0) {
            fail ("There is another module: " + y + " res: " + res);
        }
        assertEquals ("No other", -1, res.indexOf ("MODULE", y + 1));

        // f1 is later due to algebraic sorting of modules!!!!
        StringTokenizer f2 = new StringTokenizer (res.substring (0, y), "\r\n");
        StringTokenizer f1 = new StringTokenizer (res.substring (y), "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        f1.nextToken ();
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("org.openide") >= 0);
        
        assertEquals ("One line + two dep for f2", 3, f2.countTokens ());
        f2.nextToken ();
        String dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on my " + dep2, dep2.indexOf ("my.module") >= 0);
        dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep2, dep2.indexOf ("org.openide") >= 0);
        
    }
    
    public void testGenerateModuleDependenciesInOneClusterOnly() throws Exception {
        Manifest openideManifest = createManifest ();
        openideManifest.getMainAttributes ().putValue ("OpenIDE-Module", "org.openide/1");
        File openide = generateJar (new String[] { "notneeded" }, openideManifest);
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openide/1 > 4.17");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3, org.openide/1 > 4.17");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = openide.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps generate='ahoj'>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int y = res.indexOf ("MODULE");
        if (y < 0) {
            fail ("There is one module: " + y + " res: " + res);
        }
        assertEquals ("No other", -1, res.indexOf ("MODULE", y + 1));

        StringTokenizer f2 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + two dep for f2", 3, f2.countTokens ());
        f2.nextToken ();
        String dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on my " + dep2, dep2.indexOf ("my.module") >= 0);
        dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep2, dep2.indexOf ("org.openide") >= 0);
        
    }

    public void testImplementationModuleDependenciesAreRegularDepsAsWell () throws Exception {
        Manifest openideManifest = createManifest ();
        openideManifest.getMainAttributes ().putValue ("OpenIDE-Module", "org.openide/1");
        File openide = generateJar (new String[] { "notneeded" }, openideManifest);
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openide/1 > 4.17");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3 = Ahoj, org.openide/1 > 4.17");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = openide.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("MODULE");
        assertEquals ("The file starts with MODULE", 0, x);
        int y = res.indexOf ("MODULE", 1);
        if (y <= 0) {
            fail ("There is another module: " + y + " res:\n" + res);
        }
        assertEquals ("No other", -1, res.indexOf ("MODULE", y + 1));

        // f1 is later due to algebraic sorting of modules!!!!
        StringTokenizer f2 = new StringTokenizer (res.substring (0, y), "\r\n");
        StringTokenizer f1 = new StringTokenizer (res.substring (y), "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        f1.nextToken ();
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("org.openide") >= 0);
        
        assertEquals ("One line + two dep for f2", 3, f2.countTokens ());
        f2.nextToken ();
        String dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on my " + dep2, dep2.indexOf ("my.module") >= 0);
        dep2 = f2.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep2, dep2.indexOf ("org.openide") >= 0);
        
    }

    public void testGenerateImplementationModuleDependencies () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3 = Ahoj");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"implementation-dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("MODULE");
        assertEquals ("The file starts with MODULE", 0, x);
        int y = res.indexOf ("MODULE", 1);
        assertEquals ("No other:\n" + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        String modulename = f1.nextToken ();
        assertTrue ("module is ", modulename.indexOf ("my.another.module") >= 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("my.module") >= 0);
    }
    
    
    public void testCanOutputJustDependenciesBetweenClusters () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.openidex/1");
        File openide = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openidex/1");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3 = Ahoj, org.openidex/1");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"others\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"group-dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("GROUP");
        assertEquals ("The file starts with GROUP", 0, x);
        int y = res.indexOf ("GROUP", 1);
        assertEquals ("No other:\n" + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + dep on openide\n" + res, 2, f1.countTokens ());
        String groupname = f1.nextToken ();
        assertTrue ("group is " + res, groupname.indexOf ("others") >= 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on openide module" + dep1, dep1.indexOf ("org.openide") >= 0);
    }
    
    public void testCanOutputJustDependenciesBetweenClustersForOneCluster() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.openidex/1");
        File openide = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openidex/1");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3 = Ahoj, org.openidex/1");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps generate='others'>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"ide\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"others\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"group-dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("GROUP");
        assertEquals ("The file starts with GROUP", 0, x);
        int y = res.indexOf ("GROUP", 1);
        assertEquals ("No other:\n" + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + dep on openide and the other module\n" + res, 3, f1.countTokens ());
        String groupname = f1.nextToken ();
        assertTrue ("group is " + res, groupname.indexOf ("others") >= 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on my module" + dep1, dep1.indexOf ("my.module") >= 0);
        String dep2 = f1.nextToken ();
        assertTrue (dep2, dep2.startsWith ("  REQUIRES"));
        assertTrue ("on openide module" + dep2, dep2.indexOf ("org.openide") >= 0);
    }
    

    public void testCanOutputJustImplDependenciesBetweenClusters () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.openidex/1");
        File openide = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.netbeans.core/1");
        File core = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openidex/1");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/3 = Ahoj, org.openidex/1");
        File withPkgs = generateJar (new String[] { "some content"}, m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module.dependingoncore/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.netbeans.core/1 = Ahoj, my.module/3 > 4.17");
        File coredepender = generateJar (new String[] { "notneeded" }, m);
        
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"platform\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + openide.getName () + "\" />" +
            "        <include name=\"" + core.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"others\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <input name=\"rest\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + coredepender.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"group-implementation-dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("GROUP");
        assertEquals ("The file starts with GROUP", 0, x);
        int y = res.indexOf ("GROUP", 1);
        assertEquals ("No other:\n" + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + dep on openide\n" + res, 2, f1.countTokens ());
        String groupname = f1.nextToken ();
        assertTrue ("group is " + res, groupname.indexOf ("rest") >= 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on openide module" + dep1, dep1.indexOf ("org.netbeans.core") >= 0);
    }
    
    public void testRangeDependencyNeedsToBeParsed () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "org.openidex/1");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Module-Dependencies", "my.module/2-3 = Ahoj, org.openidex/1-2 > 4.17");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"implementation-dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("MODULE");
        assertEquals ("The file starts with MODULE", 0, x);
        int y = res.indexOf ("MODULE", 1);
        assertEquals ("No other:\n" + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        String modulename = f1.nextToken ();
        assertTrue ("module is ", modulename.indexOf ("my.another.module") >= 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("my.module") >= 0);
    }

    public void testGenerateProvidesRequiresDependencies () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Provides", "my.token");
        File withoutPkgs = generateJar (new String[] { "notneeded" }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/7");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Requires", "my.token");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withoutPkgs.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"dependencies\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("MODULE");
        assertEquals ("The file starts with MODULE", 0, x);
        int y = res.indexOf ("MODULE", 1);
        assertEquals ("No other module: " + res, -1, y);

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        String modulename = f1.nextToken ();
        assertTrue ("module name contains another " + modulename, modulename.indexOf ("my.another.module") > 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("my.module") >= 0);
    }

    public void testSpecialDependenciesArePrintedWithoutSearchingForAppropriateModule () throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Requires", "org.openide.modules.os.MacOSX");
        File withPkgs = generateJar (new String[] { "some content"}, m);
        
        File parent = withPkgs.getParentFile ();
        
        
        File output = extractString ("");
        File output2 = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"dependencies\" file=\"" + output + "\" />" +
            "    <output type=\"group-dependencies\" file=\"" + output2 + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        int x = res.indexOf ("MODULE");
        assertEquals ("The file starts with MODULE:" + res, 0, x);
        assertEquals ("No other", -1, res.indexOf ("MODULE", x + 1));

        StringTokenizer f1 = new StringTokenizer (res, "\r\n");
        
        assertEquals ("One line + one dep for f1\n" + res, 2, f1.countTokens ());
        String modname = f1.nextToken ();
        assertTrue ("Name: " + modname, modname.indexOf ("my.another.module") > 0);
        String dep1 = f1.nextToken ();
        assertTrue (dep1, dep1.startsWith ("  REQUIRES"));
        assertTrue ("on " + dep1, dep1.indexOf ("org.openide.modules.os.MacOSX") >= 0);
        
    }
    
    public void testPrintNamesOfPackagesSharedBetweenMoreModules() throws Exception {
        File notAModule = generateJar (new String[] { "org/shared/not/X.class", "org/shared/yes/X.html" }, createManifest ());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.module/3");
        File withoutPkgs = generateJar (new String[] { "org/shared/yes/Bla.class", "org/shared/not/sub/MyClass.class", }, m);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "my.another.module/3");
        m.getMainAttributes ().putValue ("Class-Path", notAModule.getName ());
        File withPkgs = generateJar (new String[] { "org/shared/not/res/X.jpg"}, m);
        
        File parent = notAModule.getParentFile ();
        assertEquals ("All parents are the same 1", parent, withoutPkgs.getParentFile ());
        assertEquals ("All parents are the same 2", parent, withPkgs.getParentFile ());
        
        
        File output = extractString ("");
        output.delete ();
        assertFalse ("Is gone", output.exists ());
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"deps\" classname=\"org.netbeans.nbbuild.ModuleDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <deps>" +
            "    <input name=\"ahoj\" >" +
            "      <jars dir=\"" + parent + "\" > " +
            "        <include name=\"" + notAModule.getName () + "\" />" +
            "        <include name=\"" + withoutPkgs.getName () + "\" />" +
            "        <include name=\"" + withPkgs.getName () + "\" />" +
            "      </jars>" +
            "    </input>" +
            "    <output type=\"shared-packages\" file=\"" + output + "\" />" +
            "  </deps >" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Result generated", output.exists ());
        
        String res = readFile (output);
        
        assertEquals("No not package: '" + res + "'", -1, res.indexOf("org.shared.not"));
        assertEquals ("No default pkg", -1, res.indexOf ("\n\n"));
        assertEquals ("No ork pkg", -1, res.indexOf ("org\n"));
        assertEquals ("No ork pkg", -1, res.indexOf ("org/shared\n"));
        assertTrue ("Shared is there: " + res, res.indexOf ("org.shared.yes\n") >= 0);
        assertEquals ("No META-INF pkg", -1, res.indexOf ("META"));
    }

    private File createNewJarFile() throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists()) {
                return f;
            }
        }
    }
    
    private File generateJar(String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        try (JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest)) {
            for (int i = 0; i < content.length; i++) {
                os.putNextEntry(new JarEntry (content[i]));
                os.closeEntry();
            }
            os.closeEntry ();
        }
        
        return f;
    }
    
}
