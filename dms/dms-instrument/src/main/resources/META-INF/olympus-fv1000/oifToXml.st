<?xml version="1.0" encoding="UTF-8"?>
<olympusFv1000 xmlns="http://www.acmm.sydney.edu.au/schemata/olympusfv1000">
  $sections.keys:{section|
   <section name="$section$">
     $sections.(section).keys:{key|
     <property name="$key$" value="$sections.(section).(key)$"/>
     }$
   </section>
  }$
</olympusFv1000>