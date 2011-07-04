$sections.keys:{section|
  $sections.(section).keys:{key|
  <property name="$key$" value="$sections.(section).(key)$"/>
  }$
}$
