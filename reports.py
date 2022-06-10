import json

from class_diagram import ClassDiagram
from factory import Factory
from utils import File
import config
import networkx as nx
import pandas as pd

class Report:
    def __init__(self):
        pass

    @staticmethod
    def get_factory_report(java_project, sensitivity):
        report = {
            "java_project":java_project,
            "sensitivity":sensitivity,
            "cases":None
        }
        java_project_address = config.projects_info[java_project]['path']
        base_dirs = config.projects_info[java_project]['base_dirs']
        files = File.find_all_file(java_project_address, 'java')
        index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        cd = ClassDiagram(java_project_address, base_dirs, index_dic)
        cd.make_class_diagram()

        f = Factory()
        report["cases"] = f.refactor(sensitivity, index_dic, cd.class_diagram_graph, base_dirs)
        with open(java_project_address + "/factory_report.json", 'w') as f:
            json.dump(report, f, indent=4)

        files = File.find_all_file(java_project_address, 'java')
        index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        cd2 = ClassDiagram(java_project_address, base_dirs, index_dic)
        cd2.make_class_diagram()



if __name__ == "__main__":
    java_project = "10_water-simulator"
    Report.get_factory_report(
        java_project,
        0.1
    )