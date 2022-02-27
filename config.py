BASE_DIR = 'E:\\sadegh\\iust\\compiler\\compiler projects\\main_project\\'


projects_info = {}

#10_water_simulator
projects_info['10_water-simulator'] = {}
java_project_address = BASE_DIR + 'refactored_project\\10_water-simulator\\src\\main\\java'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\10_water-simulator\\src\\main\\java\\')
projects_info['10_water-simulator']['path'] = java_project_address
projects_info['10_water-simulator']['base_dirs'] = base_dirs

#commons-codec
projects_info['commons-codec'] = {}
java_project_address = BASE_DIR + 'refactored_project\\commons-codec\\src\\main\\java'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\commons-codec\\src\\main\\java\\')
projects_info['commons-codec']['path'] = java_project_address
projects_info['commons-codec']['base_dirs'] = base_dirs

# factory-pattern-example
projects_info['factory-pattern-example'] = {}
java_project_address = BASE_DIR + 'refactored_project\\factory-pattern-example\\'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\factory-pattern-example\\')
projects_info['factory-pattern-example']['path'] = java_project_address
projects_info['factory-pattern-example']['base_dirs'] = base_dirs

# xerces2j
projects_info['xerces2j'] = {}
java_project_address = BASE_DIR + 'refactored_project\\xerces2j\\'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\xerces2j\\src\\')
projects_info['xerces2j']['path'] = java_project_address
projects_info['xerces2j']['base_dirs'] = base_dirs

# simple_injection
projects_info['simple_injection'] = {}
java_project_address = BASE_DIR + 'refactored_project\\simple_injection'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\simple_injection\\')
projects_info['simple_injection']['path'] = java_project_address
projects_info['simple_injection']['base_dirs'] = base_dirs

# javaproject
projects_info['javaproject'] = {}
java_project_address = BASE_DIR + 'refactored_project\\javaproject'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\javaproject\\')
projects_info['javaproject']['path'] = java_project_address
projects_info['javaproject']['base_dirs'] = base_dirs

# nest_project
projects_info['nest_project'] = {}
java_project_address = BASE_DIR + 'refactored_project\\nest_project'
base_dirs = []
base_dirs.append(BASE_DIR + 'refactored_project\\nest_project\\')
projects_info['nest_project']['path'] = java_project_address
projects_info['nest_project']['base_dirs'] = base_dirs



# *************************************
# complexity
import networkx as nx
test_class_diagram = nx.DiGraph()
relationships_name = ['implements', 'extends', 'create', 'use_consult', 'use_def']

# extends path
test_class_diagram.add_edge(1, 0)
test_class_diagram[1][0]['relation_type'] = 'extends'

test_class_diagram.add_edge(2, 0)
test_class_diagram[2][0]['relation_type'] = 'extends'

test_class_diagram.add_edge(3, 0)
test_class_diagram[3][0]['relation_type'] = 'extends'

test_class_diagram.add_edge(4, 3)
test_class_diagram[4][3]['relation_type'] = 'extends'

test_class_diagram.add_edge(5, 3)
test_class_diagram[5][3]['relation_type'] = 'extends'

test_class_diagram.add_edge(6, 3)
test_class_diagram[6][3]['relation_type'] = 'extends'

test_class_diagram.add_edge(9, 8)
test_class_diagram[9][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(10, 8)
test_class_diagram[10][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(11, 8)
test_class_diagram[11][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(12, 8)
test_class_diagram[12][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(13, 8)
test_class_diagram[13][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(14, 8)
test_class_diagram[14][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(15, 8)
test_class_diagram[15][8]['relation_type'] = 'extends'

test_class_diagram.add_edge(9, 8)
test_class_diagram[9][8]['relation_type'] = 'extends'

# use paths
test_class_diagram.add_edge(7, 8)
test_class_diagram[7][8]['relation_type'] = 'use_def'

test_class_diagram.add_edge(8, 7)
test_class_diagram[8][7]['relation_type'] = 'use_def'

test_class_diagram.add_edge(7, 0)
test_class_diagram[7][0]['relation_type'] = 'use_def'

test_class_diagram.add_edge(0, 7)
test_class_diagram[0][7]['relation_type'] = 'use_def'

test_class_diagram.add_edge(8, 0)
test_class_diagram[8][0]['relation_type'] = 'use_def'