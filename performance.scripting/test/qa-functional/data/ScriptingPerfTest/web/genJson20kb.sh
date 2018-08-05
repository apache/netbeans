#!/bin/bash

file="json20kb.json"

echo "{" > $file
for ((x=0; x<65; x++))
do
    cat >> $file << EOF
    "firstName$x": "John",
    "lastName$x": "Smith",
    "address$x": {
        "streetAddress": "21 2nd Street",
        "city": "New York",
        "state": "\"NY\"",
        "postalCode": 10021
    },
    "phoneNumbers$x": [
        "212 732-1234",
        "646 123-4567"
    ],
    "height$x": 10.345e-3,
EOF
done

cat >> $file  << EOF
    "end": "end"
}
EOF
