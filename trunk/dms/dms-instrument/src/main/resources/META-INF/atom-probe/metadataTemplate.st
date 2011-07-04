<?xml version="1.0" encoding="UTF-8"?>
<atomProbe xmlns="http://www.acmm.sydney.edu.au/schemata/atomprobe">
$sections.keys:{section|
  <$section$>
  $sections.(section).keys:{key|
  <property name="$key$" value="$sections.(section).(key)$"/>
  }$
  </$section$>
}$
</atomProbe>