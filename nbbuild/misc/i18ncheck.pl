#!/usr/bin/env perl
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

# XXX rewrite in Java: http://netbeans.org/bugzilla/show_bug.cgi?id=25859

#
# i18ncheck.pl - checks java source for internationalizable strings
#                not 100% foolproof !!
#
#
require 5.005;
use File::Find;

my $fixmode = 0;
my @files = ();
my @modifiedfiles = ();
my @lines = ();

if ($#ARGV < 0) {
    die "usage: $0 [-f] file | directory ...\n";
}

if ($ARGV[0] eq "-f") {
    $fixmode = 1;
    shift @ARGV;
}

if ($#ARGV < 0) {
    die "usage: $0 [-f] file | directory ...\n";
}

$SIG{'INT'} = sub {
    print_summary() if $fixmode;
    exit 255;
};

foreach my $name (@ARGV) {
    if (-f $name) {
	push @files, $name;
    } elsif (-d $name) {
        find(sub {
                 if (-f && m,\.java$,) {
                     push @files, $File::Find::name;
                 }
             },
             $name);
    }
}

foreach my $name (@files) {
    checkfile($name);
}

print_summary() if $fixmode;
exit 0;

#
# subroutines
#

sub checkfile {
    my $fname = shift;
    @lines = ();

    if (! open(FH, "< $fname")) {
	warn "cannot open file '$fname': $!";
	return;
    }
    @lines = <FH>;
    close FH;
    
    my $lineno = 1;
    my $modified = 0;
    
LOOP:
    while ($lineno <= $#lines) {
	$_ = $lines[$lineno - 1];
        
        if (m,/\*,,) {
            while ($lineno <= $#lines) {
                $_ = $lines[$lineno - 1];
                if (! m,\*/,) {
                    $lineno++;
                    next;
                } else {
                    last;
                }
            }
        }

        if (m,\@Messages\(\{,) {
            while ($lineno <= $#lines) {
                $_ = $lines[$lineno - 1];
                if (! m,\}\),) {
                    $lineno++;
                    next;
                } else {
                    last;
                }
            }
        }

        # skip line comment
        if (m,(^ *//.*$),) {
            $_ = $`;
        }
        
        if (checkline($_)) {
            if ($fixmode) {
                print "$fname:$lineno:\n";
                if (fixline($fname, $lineno)) {
                    $modified = 1;
                }
            } else {
                print "$fname:$lineno: $_";
            }
        }

        $lineno++;
    }


    if ($fixmode && $modified) {
        savefile($fname);
        push @modifiedfiles, $fname;
    }
}

sub fixline {
    my $fname = shift;
    my $lineno = shift;
    my $answer;
    
    print "\n";
    print "   " . $lines[$lineno - 4] if $lineno >= 4;
    print "   " . $lines[$lineno - 3] if $lineno >= 3;
    print "   " . $lines[$lineno - 2] if $lineno >= 2;
    print " =>" . $lines[$lineno - 1] if $lineno >= 1;
    print "   " . $lines[$lineno] if $lineno <= $#lines;;
    print "   " . $lines[$lineno + 1] if $lineno + 1 <= $#lines;;
    print "   " . $lines[$lineno + 2] if $lineno + 2 <= $#lines;;
    print "\n";
    
    do {
        print "** [M]ark this line with NOI18N -- [S]kip [m]: ";
        flush;
        $answer = <STDIN>;
        chomp $answer;
    } while (uc($answer) ne "M" && uc($answer) ne "S" && $answer ne "");

    if (uc($answer) eq "S") {
        return 0;
    } else {
        $lines[$lineno - 1] =~ s,\s*$,,;
        $lines[$lineno - 1] = $lines[$lineno - 1] . " // NOI18N\n";
        return 1;
    }
    
}

sub savefile {
    my $fname = shift;

    if (!rename $fname, "$fname.bak") {
        warn "** Cannot create backup for $fname, changes have not been saved\n";
        return;
    }

    if (! open(FH, "> $fname")) {
        warn "** Cannot save changes in $fname\n";
    }

    print FH @lines;
    close FH;
}

sub checkline {
    shift;

    return (! m,NOI18N,
            && ! m,getString\s*\(,
            && ! m,System\. ?err\. ?print,
            && ! m,getProperty,
            && ! m,getBoolean,
            && ! m,assert,
            && ! m,AssertionError,
            && ! m,/\*\*,
            && ! m,//.*",
            && ! m,get(Message|LocalizedFile),
            && ! m,\. ?(info|warning|fine|finer|finest)\s*\(,
            && ! m,\. ?log\s*\(\s*Level\s*\.\s*(SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST)\s*,
            && ! m,\@SuppressWarnings\s*\(,
            && ! m,new HelpCtx\s*\(,
            && ! m,new PropertyDescriptor\s*\(,
            && ! m,setIconBase\s*\(,
            && ! m,setIconBaseWithExtension\s*\(,
            && ! m,setPrototypeDisplayValue\s*\(,
            && ! m,loadImage\s*\(,
            && ! m,getResource(AsStream)?\s*\(,
            && ! m,findResource ?\s*\(,
            && ! m,Parameters\.\s*not,
            && ! m,Parameters\.\s*java,
            && ! m,^\s*\@,
            && m,".+", 
           ); 
}

sub print_summary {
    if ($#modifiedfiles < 0) {
        print "\n\n** No file has been modified\n";
    } else {
        print "\n\n** The following files have been modified:\n\n";
        foreach my $fname (@modifiedfiles) {
            print "      $fname\n";
        }
    }
}
