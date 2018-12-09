# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2004, 2016 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
# Contributor(s):


# Set the name of the automatically included spec files. #
# This used to be called ".erpm".			 #
$gbl_erpm_fname = "erpm_common.spec" ;

# This holds the partial spec files that we'll use to construct the #
# complete spec file.						    #
@gbl_spec_files = () ;

# This holds the .erpm files (or whatever they're called). #
@gbl_erpm_files = () ;

# Save the current dir. #
$gbl_start_dir = `pwd` ;
chop( $gbl_start_dir) ;
chdir( $gbl_start_dir) ;
$gbl_start_dir = `pwd` ;
chop( $gbl_start_dir) ;

# Save the dir that this script is in. #
$gbl_script_dir = `dirname $0` ;
chop( $gbl_script_dir) ;
if( $gbl_script_dir eq ".") {
  $gbl_script_dir = $gbl_start_dir ;
}
chdir( $gbl_script_dir) ;
$gbl_script_dir = `pwd` ;
chop( $gbl_script_dir) ;
chdir( $gbl_start_dir) ;

# Save the name of the script. #
$gbl_script_name = `basename $0` ;
chop( $gbl_script_name) ;

# If this is 1, then this script writes the spec file but doesn't run #
# the RPM build.						      #
$gbl_nobuild = 0 ;

# This is a temporary directory that is populated with the files in #
# the RPM.							    #
$gbl_build_dir = "" ;

# If this is 1, then the build dir will not be deleted at the end. #
$gbl_noclean = 0 ;

# This is a global status value that is set to non-zero if a problem #
# occurs.							     #
$gbl_status = 0 ;

# This is the RPM build root.  Typically this is in the "build dir", #
# the temporary directory this script populates with the files.	     #
# However, very simple RPM builds, in which you already have a	     #
# directory that is populated with all the files, can set this	     #
# explicitly with the -buildroot option.			     #
$gbl_build_root = "" ;

# This is the directory where the RPM will be put after its built. #
$gbl_target_dir = "" ;

# This holds the name/value pairs that the user specifies on the #
# command line with the -define option.				 #
%gbl_defines = () ;

# This holds the directory/name pairs that are specified in the spec #
# files with the %erpm_map macro.				     #
%gbl_dir_map = () ;

# This is set to 1 if the %erpm_map macro is used. #
$gbl_have_dir_map = 0 ;

# This holds the old & new filenames specified by %erpm_rename macros. #
%gbl_file_renames = () ;

# These are directories for which all files below are automatically #
# included in the RPM.						    #
%gbl_whole_dirs = () ;

# If this is 1, then if a file entry has no 'attr' specified, then one #
# is added to set the owner & group to 'root'.			       #
$gbl_add_root_attr = 1 ;

# If 1, then we'll search up the directory tree for the .erpm files. #
$gbl_find_parent_dir_erpm_files = 1 ;

# If 1, then we'll include the .erpm file in this script's directory. #
$gbl_include_global_erpm_file = 1 ;

# If 1, then we'll set read permissions on all files/dirs, and remove #
# write permissions for group/other on all files/dirs.  %attr can     #
# still be used in spec files to modify permissions though.	      #
$gbl_set_basic_perms = 1 ;

exit( &main) ;

#******************************************************************************
#
#	NAME: main
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 10:17:46 PDT 2003
#
#	ABSTRACT: Main routine.
#
#******************************************************************************

sub main {
  die "&main() requires 0 parameter(s).\n" if @_ != 0 ;

  # Read the command-line options. #
  if( !&parse_args()) {
    &usage() ;
    return( 1) ;
  }

  # Look up the directory tree for the automatically-included spec #
  # files.							   #
  &find_erpm_files() ;

  # Check if any spec files were specified. #
  if( @gbl_spec_files == 0) {
    print "\nERROR: No spec file(s) specified.\n" ;
    &usage() ;
    return( 2) ;
  }

  # Print info about what we're going to do. #
  &print_info() ;

  # Setup the temporary build directory. #
  &setup_build_area() ;

  # Read the spec files, merge them together and write the real spec  #
  # file that we'll use to run the RPM build.  During this process,   #
  # the temporary build directory will be populated with the files to #
  # be included in the RPM (except in the case of simple RPM builds   #
  # when the user specifies the -buildroot option).		      #
  if( !&write_spec_file()) {
    return( 3) ;
  }

  # Run the RPM build. #
  &run_rpm_build() ;

  # Delete our temporary files. #
  &cleanup() ;

  return( $gbl_status) ;
}

#******************************************************************************
#
#	NAME: run_rpm_build
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 12:59:06 PDT 2003
#
#	ABSTRACT: Run the rpm build.
#
#******************************************************************************

sub run_rpm_build {
  die "&run_rpm_build() requires 0 parameter(s).\n" if @_ != 0 ;

  # Write .rpmmacros file, which we use to specify the "topdir". #
  &write_rpm_macros_file() ;

  # Build the RPM. #
  &build_rpm() ;
}

#******************************************************************************
#
#	NAME: setup_build_area
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 13:23:31 PDT 2003
#
#	ABSTRACT: Setup the RPM build area.
#
#******************************************************************************

sub setup_build_area {
  die "&setup_build_area() requires 0 parameter(s).\n" if @_ != 0 ;

  # Create the temporary directories. #
  $gbl_build_dir = &get_tmpname( "$gbl_start_dir/easy_rpm") ;
  mkdir( $gbl_build_dir, 0777) ;
  mkdir( "$gbl_build_dir/BUILD", 0777) ;
  mkdir( "$gbl_build_dir/RPMS", 0777) ;
  mkdir( "$gbl_build_dir/SOURCES", 0777) ;
  mkdir( "$gbl_build_dir/SPECS", 0777) ;
  mkdir( "$gbl_build_dir/SRPMS", 0777) ;

  # If no build root was specified, then we'll copy the files into the #
  # BUILD directory and use that as the build root.		       #
  if( $gbl_build_root eq "") {
    $gbl_build_root = "$gbl_build_dir/BUILD" ;
    print "Build root: $gbl_build_root\n" ;
  }
}

#******************************************************************************
#
#	NAME: write_rpm_macros_file
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 13:24:17 PDT 2003
#
#	ABSTRACT: Write the .rpmmacros file, which we use to specify
#	the "topdir".
#
#******************************************************************************

sub write_rpm_macros_file {
  die "&write_rpm_macros_file() requires 0 parameter(s).\n" if @_ != 0 ;

  local( $file, *OUTFILE) ;
  $file = "$gbl_build_dir/.rpmmacros" ;
  if( open( OUTFILE, ">$file")) {
    print OUTFILE "%_topdir $gbl_build_dir\n" ;
    close( OUTFILE) ;
  }
  else {
    print "ERROR: Can't write $file\n" ;
    $gbl_status = 11 ;
  }
}

#******************************************************************************
#
#	NAME: build_rpm
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 13:24:33 PDT 2003
#
#	ABSTRACT: Build the RPM.
#
#******************************************************************************

sub build_rpm {
  die "&build_rpm() requires 0 parameter(s).\n" if @_ != 0 ;

  local( $script, *OUTFILE, $ret, $rpm_exec) ;

  if( -f "/usr/bin/rpmbuild" || -f "/bin/rpmbuild" | -f "/opt/sfw/bin/rpmbuild") {
    $rpm_exec = "rpmbuild --target noarch" ;
  }
  else {
    $rpm_exec = "rpm --target noarch" ;
  }

  # Write a little shell script to actually run the build.  Set HOME #
  # so the RPM build will use our .rpmmacros file.		     #
  $script = "$gbl_build_dir/run_build.sh" ;
  if( open( OUTFILE, ">$script")) {
    print OUTFILE <<EOF
      HOME=$gbl_build_dir
      export HOME
      stat=0
EOF
    ;
    if( $gbl_set_basic_perms) {
      print OUTFILE <<EOF

      ( cd $gbl_build_root ; find . -type f | xargs chmod ugo+r )
      ( cd $gbl_build_root ; find . -type d | xargs chmod ugo+r )
      ( cd $gbl_build_root ; find . -type f | xargs chmod go-w )
      ( cd $gbl_build_root ; find . -type d | xargs chmod go-w )

EOF
    ;
    }
      print OUTFILE <<EOF
      $rpm_exec -bb $gbl_spec_file || stat=\$?
      if [ \$stat -eq 0 ]; then
        cp $gbl_build_dir/RPMS/*/* $gbl_target_dir || stat=\$?
      fi
      exit \$stat
EOF
    ;
    close( OUTFILE) ;
  }
  else {
    print "ERROR: Can't write to $script\n" ;
    $gbl_status = 12 ;
    return ;
  }

  # Run the RPM build if desired. #
  if( ! $gbl_nobuild && $gbl_status == 0) {
    $ret = system( "sh -x $script") ;
    $ret /= 256 ;
    if( $ret != 0) {
      print "ERROR: The RPM build returned bad status: $ret\n" ;
      $gbl_status = $ret ;
    }
  }
}

#******************************************************************************
#
#	NAME: cleanup
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 13:24:54 PDT 2003
#
#	ABSTRACT: Delete temporary directories.
#
#******************************************************************************

sub cleanup {
  die "&cleanup() requires 0 parameter(s).\n" if @_ != 0 ;

  if( ! $gbl_nobuild && ! $gbl_noclean) {
    system( "rm -rf $gbl_build_dir") ;
  }
}

#******************************************************************************
#
#	NAME: write_spec_file
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 11:53:24 PDT 2003
#
#	ABSTRACT: Read the partial spec files and write the real
#	spec file that we'll use to run the RPM build.
#
#******************************************************************************

sub write_spec_file {
  die "&write_spec_file() requires 0 parameter(s).\n" if @_ != 0 ;

  local( @erpm_files, *SPECFILE, $section, $d) ;
  chop( $d = `date`) ;

  # Open the real spec file that we'll write to. #
  $gbl_spec_file = &get_tmpname( "$gbl_start_dir/easy_rpm.spec") ;
  unlink( $gbl_spec_file) ;
  if( open( SPECFILE, ">$gbl_spec_file")) {

    # Write some info into the spec file. #
    print SPECFILE "# Created by $gbl_script_name - $d\n" ;

    # Write the build root directory. #
    if( $gbl_build_root ne "") {
      print SPECFILE "BuildRoot: $gbl_build_root\n" ;
    }
    print SPECFILE "\n" ;

    # Reverse the order of the automatically-included spec files so #
    # that values in files in lower-level directories will override #
    # the values in higher-level files.				    #
    @erpm_files = reverse( @gbl_erpm_files) ;

    # Read and write the preamble section, then all the other #
    # sections.						      #
    &write_section( "", @erpm_files) ;
    &write_section( "%prep", @erpm_files) ;
    &write_section( "%build", @erpm_files) ;
    &write_section( "%install", @erpm_files) ;
    &write_section( "%files", @erpm_files) ;
    &write_section( "%clean", @erpm_files) ;
    &write_section( "%pre", @erpm_files) ;
    &write_section( "%post", @erpm_files) ;
    &write_section( "%preun", @erpm_files) ;
    &write_section( "%postun", @erpm_files) ;

    close( SPECFILE) ;
  }
  else {
    print "ERROR: Can't create $gbl_spec_file\n" ;
    return( 0) ;
  }

  return( 1) ;
}

#******************************************************************************
#
#	NAME: write_section
#
#	AUTHOR: Jerry Huth	DATE: Sep  5 11:02:53 PDT 2003
#
#	ABSTRACT: Read the partial spec files and write the given
#	section into the real spec file that we'll use when we
#	build the RPM.
#
#******************************************************************************

sub write_section {

  local( $section, @erpm_files) = @_ ;

  local( $wrote_section) = 0 ;

  $wrote_section = 0 ;
  $wrote_section = &write_section_from_files( $section, $wrote_section, @erpm_files) ;
  $wrote_section = &write_section_from_files( $section, $wrote_section, @gbl_spec_files) ;
}

#******************************************************************************
#
#	NAME: write_section_from_files
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 12:24:13 PDT 2003
#
#	ABSTRACT: Copy the given section of the given partial spec
#	files into the complete spec file.  If the section is an
#	empty string, then the preamble section is copied.
#
#******************************************************************************

sub write_section_from_files {

  local( $section, $wrote_section, @files) = @_ ;

  local( $file, *INSPEC) ;

  # For each file. #
  foreach $file ( @files) {
    if( open( INSPEC, "$file")) {

      # Write the section from the file into the real spec. #
      $wrote_section = &copy_section( $section, $wrote_section) ;
      close( INSPEC) ;
    }
    else {
      print "ERROR: Couldn't open $file\n" ;
    }
  }

  return( $wrote_section) ;
}

#******************************************************************************
#
#	NAME: copy_section
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 12:27:43 PDT 2003
#
#	ABSTRACT: Copy the given section into the complete spec file.
#
#******************************************************************************

sub copy_section {
  die "&copy_section() requires 2 parameter(s).\n" if @_ != 2 ;

  local( $section, $wrote_section) = @_ ;
  local( $line, $in_section, $is_files) ;

  # If the section is preamble, then we're already in this section #
  # (it's at the top of the file.)				   #
  $in_section = 0 ;
  if( $section eq "") {
    $in_section = 1 ;
  }

  # Remember if this the "%files" section. #
  $is_files = 0 ;
  if( $section eq "%files") {
    $is_files = 1 ;
  }

  # For each line of the file. #
  while( $line = <INSPEC>) {

    # If we're in the section we're copying. #
    if( $in_section) {

      # If we're just leaving the section we're copying. #
      if( &is_section_header( $line)) {

	# Break out if the section we care about is the preamble. #
	$in_section = 0 ;
	if( $section eq "") {
	  last ;
	}
	next ;
      }

      # Copy this line into the real spec file (and process our #
      # predefined macros, such as %erpm_ln_s).			#
      $wrote_section = &copy_one_line( $section, $line, $is_files, $wrote_section) ;
    }

    # Else if we're just entering the section we care about. #
    elsif( $line =~ m|^$section|) {
      $in_section = 1 ;
    }
  }

  return( $wrote_section) ;
}

#******************************************************************************
#
#	NAME: copy_one_line
#
#	AUTHOR: Jerry Huth	DATE: Aug 20 12:33:06 PDT 2003
#
#	ABSTRACT: Copy one line into the spec file.
#
#******************************************************************************

sub copy_one_line {
  die "&copy_one_line() requires 4 parameter(s).\n" if @_ != 4 ;

  local( $section, $line, $is_files, $wrote_section) = @_ ;
  local( $from, $to, $real_to, $linkto, $linkfrom, $real_linkfrom) ;
  local( $cmd, $ret, $link_from_base, $link_from_dir, $dir) ;
  local( $newdir, $mode, $attr1, $attr2, $attr3, $have_attr) ;
  local( $attr_val) ;

  # If the section we're copying is the "%files" section, then look #
  # for our predefined macros and process them.			    #
  if( $section eq "%files") {

    # The %erpm_map macro.  Add the directory/name mapping to our #
    # table.							  #
    if( $line =~ m|^%erpm_map|) {
      if( $line =~ m|^%erpm_map\s+(\S+)\s+(\S+)|) {
	$from = $1 ;
	$to = $2 ;
	$real_to = $gbl_defines{ $to} ;
	if( $real_to eq "") {
	  print "ERROR: $to isn't defined for mapping.\n" ;
	  $gbl_status = 1 ;
	  return( $wrote_section) ;
	}
	$gbl_dir_map{ $from} = $real_to ;
	$gbl_have_dir_map = 1 ;
      }
      else {
	print "ERROR: Can't read line: $line\n" ;
	$gbl_status = 1 ;
      }
      return( $wrote_section) ;
    }

    # The %erpm_unmap macro.  Clear out the mapping table. #
    elsif( $line =~ m|^%erpm_unmap|) {
      %gbl_dir_map = () ;
      $gbl_have_dir_map = 0 ;
      return( $wrote_section) ;
    }

    # The %erpm_rename macro.  Store the old & new file names. #
    elsif( $line =~ m|^%erpm_rename\s+(\S*)\s+(\S*)|) {
      $gbl_file_renames{ $1} = $2 ;
      return( $wrote_section) ;
    }

    # The %erpm_ln and %erpm_ln_s macros.  Create the link, and #
    # include it and it's parent directories in the file list.	#
    elsif( $line =~ m|^%erpm_ln_s| || $line =~ m|^%erpm_ln|) {
      if( $line =~ m|^%erpm_ln.*\s+(\S+)\s+(\S+)|) {
	$linkto = $1 ;
	$linkfrom = $2 ;
	$real_linkfrom = "$gbl_build_root$linkfrom" ;
	$cmd = "ln " ;
	if( $line =~ m|^%erpm_ln_s|) {
	  $cmd .= "-s " ;
	}
	chop( $link_from_dir = `dirname $real_linkfrom`) ;
	chop( $link_from_base = `basename $real_linkfrom`) ;
	chop( $dir = `dirname $linkfrom`) ;
	&mkdirs_and_include( $dir) ;
	chdir( $link_from_dir) ;
	$cmd .= "$linkto $link_from_base" ;
#print "Running cmd: $cmd\n" ;
	$ret = system( $cmd) ;
	$ret /= 256 ;
	if( $ret != 0) {
	  print "ERROR: Couldn't create link: $line" ;
	  $gbl_status = 1 ;
	}
	chdir( $gbl_start_dir) ;
	if( !&already_in_rpm( $linkfrom)) {
	  $line = "$linkfrom\n" ;
	}
	else {
	  $line = "" ;
	}
      }
      else {
	print "ERROR: Couldn't read line: $line" ;
      }
    }

    # The %erpm_dir macro.  Add the directory and include it in the #
    # RPM.							    #
    elsif( $line =~ m|%erpm_dir\s+(\S+)|) {
      $newdir = $1 ;
      $mode = "0755" ;
      $have_attr = 0 ;
      $attr_val = "" ;
      if( $gbl_add_root_attr) {
	$attr_val = "%attr(-,root,root) " ;
      }
      if( $line =~ m|%attr\(([^,]+),([^,]+),([^,]+)\)|) {
	$have_attr = 1 ;
	$attr1 = $1 ;
	$attr2 = $2 ;
	$attr3 = $3 ;
	if( $attr1 !~ m|\s*-\s*|) {
	  $mode = $attr1 ;
	}
	if( $line =~ m|(%attr[^\)]+\))|) {
	  $attr_val = "$1 " ;
	}
      }
      mkdir( "$gbl_build_dir$newdir", $mode) ;
      if( !$wrote_section && $section ne "") {
	print SPECFILE "\n$section\n" ;
	$wrote_section = 1 ;
      }
      print SPECFILE "$attr_val" . "%dir $newdir\n" ;
      return( $wrote_section) ;
    }

    # Else if it's not a macro call, and we're populating our      #
    # temporary directory with the files in the RPM, then copy the #
    # file or directory into our temporary directory.		   #
    elsif( $line !~ m|^\s*#| && $line !~ m|^\s*$| && $gbl_have_dir_map) {
      &copy_file( $line) ;
    }
  }

  # Now copy the line into the real spec file.  If needed, add "%attr" #
  # to the line so 'root' will be the owner of the file.	       #
  if( $line !~ m|^\s*$|) {
    if( $is_files && $gbl_add_root_attr &&
       $line !~ m|\%attr| &&
       $line !~ m|^\s*#|) {
       if( !$wrote_section && $section ne "") {
	 print SPECFILE "\n$section\n" ;
	 $wrote_section = 1 ;
       }
       $line = &rename_file( $line) ;
       print SPECFILE "%attr(-,root,root) $line" ;
    }
    else {
      if( !$wrote_section && $section ne "") {
	print SPECFILE "\n$section\n" ;
	$wrote_section = 1 ;
      }
      $line = &rename_file( $line) ;
      print SPECFILE "$line" ;
    }
  }

  return( $wrote_section) ;
}

#******************************************************************************
#
#	NAME: already_in_rpm
#
#	AUTHOR: Jerry Huth	DATE: Feb 10 09:50:29 PST 2004
#
#	ABSTRACT: Return 1 if this file is already in the RPM because
#	it's below a whole directory that is in the RPM.
#
#******************************************************************************


sub already_in_rpm {
  die "&already_in_rpm() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $file) = @_ ;

  local( $ret) = 0 ;

  while( $file ne "/") {
    if( $gbl_whole_dirs{ $file}) {
      $ret = 1 ;
      last ;
    }
    $file = `dirname $file` ;
    chop( $file) ;
  }

  return( $ret) ;
}

#******************************************************************************
#
#	NAME: rename_file
#
#	AUTHOR: Jerry Huth	DATE: Sep 22 09:43:33 PDT 2003
#
#	ABSTRACT: If a file that is supposed to be renamed is
#	referenced by the given line, then rename the file and
#	put the new filename into the line.
#
#******************************************************************************

sub rename_file {
  die "&rename_file() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $line) = @_ ;

  local( $from, $to, $new_file) ;

  foreach $from ( keys( %gbl_file_renames)) {
    if( $line =~ m|\b$from\b| ||
       $line =~ m|^$from$| ||
       $line =~ m|\b$from$| ||
       $line =~ m|^$from\b|) {
      $to = $gbl_file_renames{ $from} ;
      delete( $gbl_file_renames{ $from}) ;
      chop( $new_file = `dirname $from`) ;
      $new_file .= "/$to" ;
      $line =~ s|\b$from\b|$new_file| ||
	  $line =~ s|^$from$|$new_file| ||
	      $line =~ s|\b$from$|$new_file| ||
		  $line =~ s|^$from\b|$new_file| ;
      rename( "$gbl_build_root$from", "$gbl_build_root$new_file") ;
    }
  }

  return( $line) ;
}

#******************************************************************************
#
#	NAME: copy_file
#
#	AUTHOR: Jerry Huth	DATE: Aug 20 14:41:09 PDT 2003
#
#	ABSTRACT: Copy the file or dir into our build root.
#
#******************************************************************************

sub copy_file {
  die "&copy_file() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $line) = @_ ;
  local( $tofile, $fromfile, $todir, $fromdir, $dirname) ;
  local( $ret, $dir, $cmd, $mapped_the_file, $todirname) ;
  local( $real_from) ;

  ( $mapped_the_file, $fromfile, $tofile, $real_from) = 
      &map_a_file( $line) ;
  if( !$mapped_the_file) {
    return( 0) ;
  }

  # Use cpio for directories and symlinks. #
  if( -d $fromfile || -l $fromfile) {
    if( -d $fromfile && $line !~ m|%dir|) {
      $gbl_whole_dirs{ $real_from} = 1 ;
    }
    $todir = `dirname $tofile` ;
    chop( $todir) ;
    $fromdir = `dirname $fromfile` ;
    chop( $fromdir) ;
    $dirname = `basename $fromfile` ;
    chop( $dirname) ;
    $todirname = `basename $tofile` ;
    chop( $todirname) ;
    chdir( $fromdir) ;
    if( system( "mkdir -p $todir") != 0) {
      print "ERROR: Couldn't create dir: $todir\n" ;
    }
    $cmd = "find $dirname " ;
    if( $line =~ m|%dir\s+|) {
      $cmd .= "-prune " ;
    }
    $cmd .= "| cpio -pdum $todir" ;
#print "Running cmd: $cmd\n" ;
    $ret = system( "$cmd") ;
    $ret /= 256 ;
    if( $ret != 0) {
      print "ERROR: Couldn't copy: $fromfile\n" ;
      $gbl_status = 1 ;
    }
    if( $dirname ne $todirname) {
      chdir( $todir) ;
      rename( $dirname, $todirname) ;
    }
    chdir( $gbl_start_dir) ;
  }

  # Else use cp for regular files. #
  else {
    chop( $dir = `dirname $tofile`) ;
    if( system( "mkdir -p $dir") != 0) {
      print "ERROR: Couldn't create dir: $dir\n" ;
    }
    $cmd = "cp -p $fromfile $tofile" ;
#print "Running cmd: $cmd\n" ;
    $ret = system( "$cmd") ;
    $ret /= 256 ;
    if( $ret != 0) {
      print "ERROR: Couldn't copy: $fromfile\n" ;
      $gbl_status = 1 ;
    }
  }

  return( 1) ;
}

#******************************************************************************
#
#	NAME: map_a_file
#
#	AUTHOR: Jerry Huth	DATE: Sep  5 11:29:24 PDT 2003
#
#	ABSTRACT: See if the given line specifies a file or
#	directory that starts with one of the directories in
#	our directory mapping table.  Return the filename this
#	file maps to.
#
#******************************************************************************

sub map_a_file {
  die "&map_a_file() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $line) = @_ ;

  local( $from, $to, $regexp, $start_of_line) ;
  local( $tofile, $rest_of_line, $fromfile) ;
  local( $real_tofile) ;

  # For each mapping. #
  foreach $from ( keys( %gbl_dir_map)) {

    # See if we have a match. #
    $to = $gbl_dir_map{ $from} ;
    $regexp = "(" . $from . "\\S*)" ;
    if( $line =~ m|^$regexp(.*)|) {
      $start_of_line = "" ;
      $tofile = $1 ;
      $rest_of_line = $2 ;
    }
    elsif( $line =~ m|^(.*)$regexp(.*)|) {
      $start_of_line = $1 ;
      $tofile = $2 ;
      $rest_of_line = $3 ;
    }

    # If no match, then try the next mapping. #
    else {
      next ;
    }

    # If there is a match, then return the file and the file it maps #
    # to.							     #
    $real_tofile = "$gbl_build_root$tofile" ;
    $fromfile = $tofile ;
    $fromfile =~ s|$from|$to| ;
    return( 1, $fromfile, $real_tofile, $tofile) ;
  }

  return( 0, "", "", "") ;
}

#******************************************************************************
#
#	NAME: mkdirs_and_include
#
#	AUTHOR: Jerry Huth	DATE: Aug 21 09:27:26 PDT 2003
#
#	ABSTRACT: Make the directories as needed and write %dir commands
#	to include the directories in the RPM.
#
#******************************************************************************

sub mkdirs_and_include {
  die "&mkdirs_and_include() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $rpm_dir) = @_ ;
  local( @dirs, $dir, $real_dir, $cur_dir, $attr) ;

  $real_dir = "$gbl_build_root" ;
  $cur_dir = "" ;
  @dirs = split( '/', $rpm_dir) ;
  foreach $dir ( @dirs) {
    next if $dir =~ m|^\s*$| ;

    $cur_dir .= "/$dir" ;
    $real_dir .= "/$dir" ;

    if( ! -d $real_dir) {
      if( system( "mkdir $real_dir") != 0) {
	print "ERROR: Couldn't create dir: $real_dir\n" ;
	$gbl_status = 1 ;
      }
      $attr = "" ;
      if( $gbl_add_root_attr) {
	$attr = "%attr(-,root,root) " ;
      }
      if( !&already_in_rpm( $cur_dir)) {
	print SPECFILE "$attr%dir $cur_dir\n" ;
      }
    }
  }
}

#******************************************************************************
#
#	NAME: is_section_header
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 12:35:45 PDT 2003
#
#	ABSTRACT: See if this line is a section header.
#
#******************************************************************************

sub is_section_header {
  die "&is_section_header() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $line) = @_ ;
  local( $ret) = 0 ;

  if( $line =~ m|^%prep\s*$| ||
      $line =~ m|^%build\s*$| ||
      $line =~ m|^%install\s*$| ||
      $line =~ m|^%files\s*$| ||
      $line =~ m|^%clean\s*$| ||
      $line =~ m|^%pre\s*$| ||
      $line =~ m|^%post\s*$| ||
      $line =~ m|^%preun\s*$| ||
      $line =~ m|^%postun\s*$|) {
    $ret = 1 ;
  }

  return( $ret) ;
}

#******************************************************************************
#
#	NAME: print_info
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 10:54:53 PDT 2003
#
#	ABSTRACT: Print info about what this script is doing.
#
#******************************************************************************

sub print_info {
  die "&print_info() requires 0 parameter(s).\n" if @_ != 0 ;

  local( $file, $first) ;

  print "\n$gbl_script_name Info:\n\n" ;
  print "Start dir: $gbl_start_dir\n" ;
  print "Script dir: $gbl_script_dir\n" ;

  $first = 1 ;
  foreach $file ( @gbl_erpm_files) {
    if( $first) {
      print "$gbl_erpm_fname files:\n" ;
    }
    $first = 0 ;
    print "\t$file\n" ;
  }

  $first = 1 ;
  foreach $file ( @gbl_spec_files) {
    if( $first) {
      print "Spec files:\n" ;
    }
    $first = 0 ;
    print "\t$file\n" ;
  }

  print "\n" ;
}

#******************************************************************************
#
#	NAME: find_erpm_files
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 10:36:49 PDT 2003
#
#	ABSTRACT: Look up the directory tree and in this script's directory
#	for the .erpm files (or whatever they're called).
#
#******************************************************************************

sub find_erpm_files {
  die "&find_erpm_files() requires 0 parameter(s).\n" if @_ != 0 ;

  local( $dir, $file, %files) ;

  # Look upwards, but stop if we get to '/', '/set', etc. #
  $dir = $gbl_start_dir ;
  while( 1) {
    last if &is_too_high( $dir) ;

    # If there's a .erpm file, save it. #
    $file = "$dir/$gbl_erpm_fname" ;
#print "Checking for file: $file\n" ;
    if( -r $file) {
      $gbl_erpm_files[ @gbl_erpm_files] = $file ;
      $files{ $file} = 1 ;
    }

    # Stop if we're not supposed to look upwards. #
    last if !$gbl_find_parent_dir_erpm_files ;

    # Move to the parent directory. #
    if( $dir =~ m|^(.*)/[^/]+| || $dir =~ m|^(.*)/[^/]+/|) {
      $dir = $1 ;
    }
    else {
      last ;
    }
  }

  # Include the .erpm file in this script's directory, if not already #
  # included. 							      #
  if( $gbl_include_global_erpm_file) {
    $file = "$gbl_script_dir/$gbl_erpm_fname" ;
#print "Checking $file\n" ;
    if( -r $file && ! $files{ $file}) {
      $gbl_erpm_files[ @gbl_erpm_files] = $file ;
    }
  }
}

#******************************************************************************
#
#	NAME: is_too_high
#
#	AUTHOR: Jerry Huth	DATE: Oct  2 11:59:53 PDT 2003
#
#	ABSTRACT: See if this dir is '/', '/set', etc.
#
#******************************************************************************

sub is_too_high {
  die "&is_too_high() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $dir) = @_ ;

  return( $dir =~ m|^/$| ||
	 $dir =~ m|^/set$| ||
	 $dir =~ m|^/home$| ||
	 $dir =~ m|^/export$|) ;
}

#******************************************************************************
#
#	NAME: parse_args
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 10:18:44 PDT 2003
#
#	ABSTRACT: Parse the args to this script.
#
#******************************************************************************

sub parse_args {
  die "&parse_args() requires 0 parameter(s).\n" if @_ != 0 ;

  local( $argnum, $numargs, $arg, $name, $value) ;

  # For each arg. #
  $argnum = 0 ;
  $numargs = @ARGV + 0 ;
  while( $argnum < $numargs) {
    $arg = $ARGV[ $argnum] ;

    # Save the spec file names. #
    if( $arg =~ m|^[^-]|) {
      $gbl_spec_files[ @gbl_spec_files] = $arg ;
    }

    elsif( $arg eq "-nobuild") {
      $gbl_nobuild = 1 ;
    }

    elsif( $arg eq "-buildroot") {
      $argnum++ ;
      $gbl_build_root = $ARGV[ $argnum] ;
    }

    elsif( $arg eq "-targetdir") {
      $argnum++ ;
      $gbl_target_dir = $ARGV[ $argnum] ;
    }

    elsif( $arg eq "-noclean") {
      $gbl_noclean = 1 ;
    }

    elsif( $arg eq "-define") {
      $argnum++ ;
      $name = $ARGV[ $argnum] ;
      $argnum++ ;
      $value = $ARGV[ $argnum] ;
      $gbl_defines{ $name} = $value ;
    }

    elsif( $arg eq "-no_parent_dir_files") {
      $gbl_find_parent_dir_erpm_files = 0 ;
    }

    elsif( $arg eq "-no_global_file") {
      $gbl_include_global_erpm_file = 0 ;
    }

    elsif( $arg eq "-help") {
      return( 0) ;
    }

    # All other options are unknown. #
    else {
      print "ERROR: Unknown option: $arg\n" ;
      return( 0) ;
    }

    $argnum++ ;
  }

  if( $gbl_target_dir eq "") {
    $gbl_target_dir = $gbl_start_dir ;
  }

  return( 1) ;
}

#******************************************************************************
#
#	NAME: usage
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 10:27:25 PDT 2003
#
#	ABSTRACT: Print the usage statement.
#
#******************************************************************************

sub usage {
  die "&usage() requires 0 parameter(s).\n" if @_ != 0 ;

  print <<EOF

Usage: perl $gbl_script_name [options] <spec_file(s)>

This script will create an RPM given 1 or more partial spec files.
The spec files will be combined to create the complete spec file,
and any "$gbl_erpm_fname" files that are found will also be included.
By default, this script will look in all directories up the
directory tree for "$gbl_erpm_fname" files, and in the directory
that contains this script.

Options:

  -nobuild		Setup, but do not actually run, the RPM build,

  -buildroot dir	Specifies the root of the directory tree that
			contains the files.  If not specified, then
			a temporary build root will be used.

  -targetdir dir	Specifies the directory to put the RPM file in.
			Defaults to the current directory.

  -noclean		Do not clean up after the RPM build.

  -no_parent_dir_files	Do not search for "$gbl_erpm_fname" files up
			the directory tree.

  -no_global_file	Do not include the "$gbl_erpm_fname" file from
			the script's directory.

  -define name value	Define a name/value pair, which allows the name
			to be used in %erpm_map commands in spec files.

This script predefines the following macros, which can be used in the
%files section of the spec files:

%erpm_map <directory> <name>

  Makes this script look in the directory associated with 'name'
  when looking for files that start with the given directory.

%erpm_unmap

  Clears out the mapping tables.

%erpm_ln <link_target> <link_source>

  Creates a hard link in the directory tree and includes it
  in the RPM.

%erpm_ln_s <link_target> <link_source>

  Creates a symbolic link in the directory tree and includes
  it in the RPM.

%erpm_dir <directory>

  Creates a directory and includes it in the RPM.

%erpm_rename <from_fullpath> <to_basename>

  Changes the name of a file in the directory tree (must be
  used BEFORE the file is explicitly referenced by any other
  lines of the spec file).


Here is an example spec file that could be used as input to this
script:

-----------------------------------------------------------------

%define global_product_name Sun ONE Studio 8
%define global_product_version 8.0
%define global_product_release 1

Version: %{global_product_version}
Release: %{global_product_release}
Group: Applications
Copyright: commercial
Vendor: Sun Microsystems
URL: http://www.sun.com/
Prefix: /usr/SUNWspro
AutoReqProv: no

Name: SPROidext
Summary: Branding files

%description
%{global_product_name} Branding files

%files

%erpm_map /usr/SUNWspro nb_extra_dir

%dir /usr/SUNWspro
%dir /usr/SUNWspro/prod
%dir /usr/SUNWspro/prod/bin
%dir /usr/SUNWspro/prod/lib
%dir /usr/SUNWspro/prod/lib/locale
%dir /usr/SUNWspro/prod/modules
%dir /usr/SUNWspro/prod/modules/autoload
%dir /usr/SUNWspro/prod/modules/autoload/locale
%dir /usr/SUNWspro/prod/modules/locale
%dir /usr/SUNWspro/prod/scripts
%dir /usr/SUNWspro/prod/system
%dir /usr/SUNWspro/prod/system/Modules

/usr/SUNWspro/prod/bin/checkj2sdk
/usr/SUNWspro/prod/bin/sunstudio
/usr/SUNWspro/prod/lib/locale/core_sunstudio.jar
/usr/SUNWspro/prod/modules/autoload/locale/core-compiler_sunstudio.jar
/usr/SUNWspro/prod/modules/autoload/locale/openide-compiler_sunstudio.jar
/usr/SUNWspro/prod/modules/javadisabler.jar
/usr/SUNWspro/prod/modules/locale/cpp_sunstudio.jar
/usr/SUNWspro/prod/modules/locale/usersguide_sunstudio.jar
/usr/SUNWspro/prod/modules/locale/welcome_sunstudio.jar
/usr/SUNWspro/prod/modules/sunstudio.jar
/usr/SUNWspro/prod/scripts/jdk_chooser
/usr/SUNWspro/prod/system/Modules/com-sun-tools-swdev-sunstudio.xml
/usr/SUNWspro/prod/system/Modules/com-sun-tools-swdev-javadisabler.xml

%erpm_ln_s ../prod/bin/checkj2sdk /usr/SUNWspro/bin/checkj2sdk
%erpm_ln_s ../prod/bin/sunstudio /usr/SUNWspro/bin/sunstudio
%erpm_ln_s ../prod/lib/locale /usr/SUNWspro/lib/locale

-----------------------------------------------------------------

The RPM could be built with this command:

perl $gbl_script_name -define nb_extra_dir /set/rainier/builds.intel-Linux/latest/intel-Linux/nb_extra/opt/SUNWspro example.spec

EOF

}

#******************************************************************************
#
#	NAME: get_tmpname
#
#	AUTHOR: Jerry Huth	DATE: Aug 19 14:17:18 PDT 2003
#
#	ABSTRACT: Get the name of a dir or file that doesn't
#	currently exist, by adding a number to the end of
#	the given filename prefix.
#
#******************************************************************************

sub get_tmpname {
  die "&get_tmpname() requires 1 parameter(s).\n" if @_ != 1 ;

  local( $prefix) = @_ ;
  local( $n, $file) ;
  $n = 0 ;

  do {
    $n++ ;
    $file = "$prefix.$n" ;
  } while( -e $file) ;

  return( "$file") ;
}

