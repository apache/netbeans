
BEGIN {
    R["called:"]=0; 
    R["gcc"]=0; 
    R["BuildTrace"]=0; 
    R["--version"]=0; 
    S["exit"]=0
}
/called:/{ R["called:"]++; next }
/BuildTrace/{ R["BuildTrace"]++; next }
/gcc/{ R["gcc"]++; next }
/--version/{ R["--version"]++; next }
NF == 0 { next }
{ print "Unexpected output " $0 }
END {
    for (r in R) { 
        if (R[r] != 3) { 
            print "Expected 3 for #" r " but " R[r] " found"
            S["exit"]=1
        } 
    } 
    exit S["exit"]
}
