graph [
  directed 1
  node [
    id 0
    label "2"
  ]
  node [
    id 1
    label "1"
  ]
  node [
    id 2
    label "3"
  ]
  node [
    id 3
    label "4"
  ]
  node [
    id 4
    label "5"
  ]
  edge [
    source 0
    target 1
    relation_type "create"
  ]
  edge [
    source 0
    target 2
    relation_type "create"
  ]
  edge [
    source 3
    target 1
    relation_type "implements"
  ]
  edge [
    source 3
    target 2
    relation_type "create"
  ]
  edge [
    source 4
    target 1
    relation_type "implements"
  ]
  edge [
    source 4
    target 2
    relation_type "create"
  ]
]
