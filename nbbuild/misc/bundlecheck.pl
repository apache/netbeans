#!/usr/bin/env perl
# -*- perl -*-
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

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
