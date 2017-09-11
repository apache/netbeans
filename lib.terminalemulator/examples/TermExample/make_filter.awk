# "make_filter.awk"
# Convert errors and warnings to Term hyperlinks

/^.*:[[:digit:]]+: warning: / {
	printf "]10;WARNING;"
	printf $0
	printf "\07"
	printf "\n"
	next
}

/^.*:[[:digit:]]+: / {
	printf "]10;ERROR;"
 	printf $0
	printf "\07"
	printf "\n"
	next
}

{
	print
}
