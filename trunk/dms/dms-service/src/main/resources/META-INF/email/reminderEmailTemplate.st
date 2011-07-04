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
table th {
	background: lightgray;
}
</style>
</head>

<body>
Dear $userDetails.firstName$ $userDetails.lastName$
<p>
You have following $length(datasets)$ unpublished datasets:
<p>
<table>
<thead>
<tr>
<th>Project</th>
<th>URL</th>
<th>Ingestion Date</th>
</tr>
</thead>
<tbody>
$datasets:{dataset|
<tr>
<td>$dataset.projectTitle$</td>
<td>$dmsLocation$$dataset.encodedURL$&back=index</td>
<td>$dataset.creationDate$</td>
</tr>
}$
</tbody>
</table>

<p>
Please publish them into RDA.
</body>
</html>