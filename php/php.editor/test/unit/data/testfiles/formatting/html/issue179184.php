<?php

if ($here) {
	echo 'here';
} else {
	?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>Home</title>
	</head>
	<body>

						<form name="form1" method="get" action="#">
							<table>
									<tr valign="top">
										<?php
										echo '<td>something</td>';
										?>
										<td><input id="submit" type="submit" name="Submit" value="Search"/></td>
									</tr>
							</table>
						</form>

                    <?php if ($something){
                        echo 'something else';
                    } ?>



	</body>
</html>
<?php } ?>
