# line comment

for name in $@;do
  if [ -n $name ]; then
     echo "delete the file $name (y/n/q)?"
  fi
done