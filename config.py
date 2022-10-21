"""
The configuration module

"""
import os

import networkx as nx
from utils import dir_total_size


__version__ = '0.1.2'
__author__ = 'Sadegh Jafari, Morteza Zakeri'

BASE_DIR = 'C:/Users/Zakeri/Desktop/SadeghJafari/SF110/'
UBD_DIR = 'C:/Users/Zakeri/Desktop/SadeghJafari/SF110_UDB/'
D4T_LOG_DIR = 'C:/Users/Zakeri/Desktop/SadeghJafari/d4t_log/'
projects_info = dict()


# SF110 projects
SF110_projects = [f for f in os.listdir(BASE_DIR) if not os.path.isfile(os.path.join(BASE_DIR, f))]

for project_name in SF110_projects:
    projects_info[project_name] = dict()
    java_project_address = f'{BASE_DIR}{project_name}/src/main/java'
    base_dirs = list()
    base_dirs.append(f'{BASE_DIR}{project_name}/src/main/java/')
    projects_info[project_name]['size'] = dir_total_size(java_project_address)
    projects_info[project_name]['path'] = java_project_address
    projects_info[project_name]['base_dirs'] = base_dirs
    projects_info[project_name]['db_path'] = f'{UBD_DIR}{project_name}.und'
    projects_info[project_name]['log_path'] = f'{D4T_LOG_DIR}{project_name}/{project_name}_log.csv'
    if not os.path.exists(f'{D4T_LOG_DIR}{project_name}'):
        os.mkdir(f'{D4T_LOG_DIR}{project_name}')

projects_info = dict(sorted(projects_info.items(), key=lambda item: item[1]['size']))
# for project_name in projects_info:
#     print(project_name)
#     if project_name == '6_jnfe':
#         quit()
# -----------------------------------------------
# tabula-java
projects_info['tabula-java'] = dict()
java_project_address = BASE_DIR + 'tabula-java/src/main/java'
base_dirs = list()
base_dirs.append(BASE_DIR + 'tabula-java/src/main/java/')
projects_info['tabula-java']['path'] = java_project_address
projects_info['tabula-java']['base_dirs'] = base_dirs

# commons-codec
projects_info['commons-codec2'] = dict()
java_project_address = BASE_DIR + 'commons-codec/src/main/java'
base_dirs = list()
base_dirs.append(BASE_DIR + 'commons-codec/src/main/java/')
projects_info['commons-codec2']['path'] = java_project_address
projects_info['commons-codec2']['base_dirs'] = base_dirs

# jfreechart
projects_info['jfreechart'] = dict()
java_project_address = BASE_DIR + 'jfreechart/src/main/java/org'
base_dirs = list()
base_dirs.append(BASE_DIR + 'jfreechart/src/main/java/org/')
projects_info['jfreechart']['path'] = java_project_address
projects_info['jfreechart']['base_dirs'] = base_dirs

#ant
projects_info['ant'] = {}
java_project_address = BASE_DIR + 'ant/src/main'
base_dirs = []
base_dirs.append(BASE_DIR + 'ant/src/main/')
projects_info['ant']['path'] = java_project_address
projects_info['ant']['base_dirs'] = base_dirs

# factory-pattern-example
projects_info['factory-pattern-example'] = {}
java_project_address = BASE_DIR + 'factory-pattern-example/'
base_dirs = []
base_dirs.append(BASE_DIR + 'factory-pattern-example/')
projects_info['factory-pattern-example']['path'] = java_project_address
projects_info['factory-pattern-example']['base_dirs'] = base_dirs

# xerces2j
projects_info['xerces2j'] = {}
java_project_address = BASE_DIR + 'xerces2j/'
base_dirs = []
base_dirs.append(BASE_DIR + 'xerces2j/src/')
projects_info['xerces2j']['path'] = java_project_address
projects_info['xerces2j']['base_dirs'] = base_dirs


# simple_injection
projects_info['simple_injection'] = {}
java_project_address = BASE_DIR + 'simple_injection'
base_dirs = []
base_dirs.append(BASE_DIR + 'simple_injection/')
projects_info['simple_injection']['path'] = java_project_address
projects_info['simple_injection']['base_dirs'] = base_dirs

# javaproject
projects_info['javaproject'] = {}
java_project_address = BASE_DIR + 'javaproject'
base_dirs = []
base_dirs.append(BASE_DIR + 'javaproject/')
projects_info['javaproject']['path'] = java_project_address
projects_info['javaproject']['base_dirs'] = base_dirs

# nest_project
projects_info['nest_project'] = {}
java_project_address = BASE_DIR + 'nest_project'
base_dirs = []
base_dirs.append(BASE_DIR + 'nest_project/')
projects_info['nest_project']['path'] = java_project_address
projects_info['nest_project']['base_dirs'] = base_dirs

# JSON
projects_info['JSON'] = {}
java_project_address = BASE_DIR + 'JSON/src/main/java'
base_dirs = []
base_dirs.append(BASE_DIR + 'JSON/src/main/java/')
projects_info['JSON']['path'] = java_project_address
projects_info['JSON']['base_dirs'] = base_dirs

# rhino-Rhino1_7_12_Release
projects_info['rhino-Rhino1_7_12_Release'] = {}
java_project_address = BASE_DIR + 'rhino-Rhino1_7_12_Release/src/'
base_dirs = []
base_dirs.append(BASE_DIR + 'rhino-Rhino1_7_12_Release/src/')
projects_info['rhino-Rhino1_7_12_Release']['path'] = java_project_address
projects_info['rhino-Rhino1_7_12_Release']['base_dirs'] = base_dirs

# crona warn up project 1
projects_info['crona_warn_up1'] = {}
java_project_address = BASE_DIR + 'cwa-server/common/persistence/src/main/java'
base_dirs = []
base_dirs.append(BASE_DIR + 'cwa-server/common/persistence/src/main/java/')
projects_info['crona_warn_up1']['path'] = java_project_address
projects_info['crona_warn_up1']['base_dirs'] = base_dirs

# ======================================
# Complexity

test_class_diagram = nx.DiGraph()
relationships_name = ['implements', 'extends', 'create', 'use_consult', 'use_def']

# nodes
for i in range(16):
    test_class_diagram.add_node(i)
    test_class_diagram.nodes[i]['type'] = 'normal'

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
