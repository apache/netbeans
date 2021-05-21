<?php
function abc($membernames)
{
	if ( ($emails = array_keys($membernames)) )
	{
		for( $ct=0 ; ($slice = array_slice($emails, $ct, 250)) ; $ct += $cSlice )
		{
			$cSlice = count($slice);
		}
	}
}
?>

