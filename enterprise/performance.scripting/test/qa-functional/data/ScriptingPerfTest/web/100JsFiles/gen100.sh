#!/bin/bash
for i in master.*
do
    for (( x=100; $x; $((x--)) ))
        do cp $i script$x.${i##*.}
    done
done

exit 0