import os

import pandas as pd

import config
from class_diagram import ClassDiagram
from injection import Injection
from utils import File


base_path = '/media/sadegh/Data/sadegh/iust/Bachlour/compiler/compiler projects/main_project'

def restore_java_project(project_name):
    os.chdir(f"{config.BASE_DIR}{project_name}")
    os.popen("git restore .").close()
    os.popen("git clean -f -d").close()
    path = os.getcwd()
    path = os.path.abspath(os.path.join(path, os.pardir))
    path = os.path.abspath(os.path.join(path, os.pardir))
    os.chdir(path)

number_of_projects = 50
result = {
    'project': [],
    'case1': [],
    'case2': [],
}
for index, java_project in enumerate(config.projects_info):
    try:
        restore_java_project(java_project)
        java_project_address = config.projects_info[java_project]['path']
        base_dirs = config.projects_info[java_project]['base_dirs']
        files = File.find_all_file(java_project_address, 'java')
        index_dic_ = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        cd = ClassDiagram(java_project_address, base_dirs, files, index_dic_)
        cd.make_class_diagram()
        g = cd.class_diagram_graph
        injection = Injection(base_dirs, index_dic_, files, cd.class_diagram_graph)
        reports = injection.refactor()
        for case in ['case1', 'case2']:
            result[case].append(reports[case])
        result['project'].append(java_project)
        df = pd.DataFrame(result)
        df.to_csv(base_path+'/injection_cases.csv')
    except Exception as e:
        print(e)

df = pd.read_csv(base_path+'/injection_cases.csv')
sorted_indices = (df["case1"] + df["case2"]).sort_values(ascending=False).index
df = df.loc[sorted_indices, :]
for column in df.columns:
    if 'Unnamed' in column:
        df.drop(column, axis=1, inplace=True)
df.to_csv(base_path+'/injection_cases.csv')
