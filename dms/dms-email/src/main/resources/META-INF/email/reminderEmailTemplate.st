<html>

<head>
<style type="text/css">
table
{
    border-collapse: collapse;
}
table, td, th
{
    border: 1px solid black;
    padding: 5px;
}
</style>
</head>

<body>
Dear $userDetails.firstName$ $userDetails.lastName$,
<p>
The AMMRF are encouraging researchers like yourself to consider promoting your research data and enabling its reuse. 
Many researchers from the AMMRF and other institutions are showcasing their research data through <a href="http://services.ands.org.au/home/orca/rda/">Research Data Australia</a>. 
Have you considered joining them? To advertise your research data follow the links below.
<p>
<table>
<thead>
<tr>
<th>Project</th>
<th>URL</th>
</tr>
</thead>
<tbody>
$datasets:{dataset|
<tr>
<td>$dataset.projectTitle$</td>
<td>$dmsLocation$$dataset.encodedURL$&back=index</td>
</tr>
}$
</tbody>
</table>
<p>
Many thanks.
</body>
</html>