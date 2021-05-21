function do_submit() {
                if (!disallowBlank(document.form1.period, 'Pls. select Period.'))
                    return false;
                if (!disallowBlank(document.form1.mon_yr, 'Pls. select Month / Year.'))
                    return false;
                var mon_yr = getInputValue(document.form1.mon_yr), period = getInputValue(document.form1.period);
                $.getJSON('json_txt.php', { mon_yr: mon_yr, period: period}, function(j) {
                    if (j == 'n') {
                        alert('No record found to generate text file.');
                    } else if (j == 'nc') {
                        if (confirm('Not all employee entry completed. Are you sure to generate text file.')) {
                            document.form1.submit();
                        }
                    } else {
                        document.form1.submit();
                    }
                })
                        .error(function() {
                    alert('Some problem arises. Pls. try again.');
                });
                return false;
            }