#!/usr/bin/env perl
# -*- perl -*-
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.
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

require 5.005;
use File::Find;

my @files = ();
my $quiet = 0;

if ($#ARGV < 0) {
    die "usage: $0 [-q] file | directory ...\n";
}

if ($ARGV[0] eq "-q") {
    $quiet = 1;
    shift @ARGV;
}

foreach my $name (@ARGV) {
    if (-f $name) {
	push @files, $name;
    } elsif (-d $name) {
        find(sub {
                 if (-f && m,\.java$, || -f && m,\bBundle.properties$,) {
                     push @files, $File::Find::name;
                 }
             },
             $name);
    }
}

#
# read Bundle.properties
#

@props = ();

foreach $f (@files) {
    next if $f !~ m,\bBundle.properties$,i;

    print STDERR "*** $f\n" unless $quiet;
    
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    @lines = split /\r\n|\n|\r/, $all;
    for ($lineno = 0; $lineno <= $#lines; $lineno++) {
	$_ = $lines[$lineno];
        
        next if /^\s*#/;
        next if /^\s*$/;

        if (m,^([^=]+)=(.*)$,) {
            $k = $1;
            $k =~ s,\\ , ,g;
            push @props, { key => $k,
                           file => $f,
                           lineno => $lineno + 1,
                           line => $_,
                           used => 0
                         };
        }

        while (m,\\$, && $lineno <= $#lines) {
            $lineno++;
            $_ = $lines[$lineno];
        }
    }
}

#
# go through *.java
#
  
  
foreach $f (@files) {
    next if $f =~ m,\bBundle.properties$,i;

    print STDERR "*** $f\n" unless $quiet;
    
    open FH, "< $f" or die;
    {
        local $/ = undef;
        $all = <FH>;
    }
    close FH;

    foreach $p (@props) {
        next if $p->{used} > 0;
        $pat = $p->{key};
        $pat = quotemeta $pat;
        $p->{used}++ if $all =~ m,\"$pat\",;
    }
}

foreach $p (@props) {
    next if $p->{line} =~ m!/!; # probably a filename localization, not in Java code
    next if $p->{line} =~ m!^OpenIDE-Module-!; # manifest localization, not in Java code
    print "$p->{file}:$p->{lineno}: $p->{line}\n" if $p->{used} == 0;
}
