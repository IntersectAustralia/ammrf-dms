<?xml version="1.0" encoding="UTF-8"?>
<microCT xmlns="http://www.acmm.sydney.edu.au/schemata/microct">
   $sections.keys:{section|
   <section name="$section$">
     $sections.(section).keys:{key|
     <property name="$key$" value="$sections.(section).(key)$"/>
     }$
   </section>
   }$
</microCT>