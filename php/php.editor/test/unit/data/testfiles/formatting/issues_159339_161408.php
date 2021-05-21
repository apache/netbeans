<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
<title></title>
</head>
<body>
        <?php
        $query = sprintf(" select hour(fechaRecibido) hora, count(*)
num from sms where fechaRecibido
between '2009/02/12 00:00:00' and '2009/02/12 13:28:00'
group by hour(fechaRecibido)");

        ?>
    </body>
</html>