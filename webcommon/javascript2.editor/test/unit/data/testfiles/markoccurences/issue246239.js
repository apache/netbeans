function UnusedParameter(ctl)
{
	var type = ctl.options.selectedIndex,
		ctl = document.getElementById('reuseControlVariable');

	ctl.value = type;
}