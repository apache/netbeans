f=2
while test "$f" -le "100" ;
do
if test "$f" -le "9" ;
then
  f=0$f
fi
cp jsp01.jsp jsp$f.jsp
f=`expr $f + 1`;
done