import json
import pandas as pd

from class_diagram import ClassDiagram
from factory import Factory
from utils import File
import config


class Report:
    def __init__(self, java_project):
        self.java_project = java_project
        self.java_project_address = config.projects_info[java_project]['path']
        self.base_dirs = config.projects_info[java_project]['base_dirs']
        self.files = File.find_all_file(self.java_project_address, 'java')
        self.index_dic = File.indexing_files_directory(self.files, 'class_index.json', self.base_dirs)
        self.cd = ClassDiagram(self.java_project_address, self.base_dirs, self.index_dic)
        self.cd.make_class_diagram()

    def reload_from_disk(self):
        self.java_project_address = config.projects_info[self.java_project]['path']
        self.base_dirs = config.projects_info[self.java_project]['base_dirs']
        self.files = File.find_all_file(self.java_project_address, 'java')
        self.index_dic = File.indexing_files_directory(self.files, 'class_index.json', self.base_dirs)
        self.cd = ClassDiagram(self.java_project_address, self.base_dirs, self.index_dic)
        self.cd.make_class_diagram()

    def get_json_factory_report(self, sensitivity, save=True, edit=True):
        report = {
            "java_project":java_project,
            "sensitivity":sensitivity,
            "cases":None
        }

        f = Factory()
        report["cases"] = f.refactor(
            sensitivity,
            self.index_dic,
            self.cd.class_diagram_graph,
            self.base_dirs,
            edit=edit
        )

        if save:
            with open(self.java_project_address + "/factory_report.json", 'w') as f:
                json.dump(report, f, indent=4)
        return report

    def get_pandas_factory_report(self, json_report, save=True):
        pandas_report = {
            "package": [],
            "path": [],
            "class": [],
            "sensitivity": []
        }

        for case in json_report["cases"]:
            factory = case["factory"]
            pandas_report["package"].append(factory["package"])
            pandas_report["path"].append(factory["path"])
            pandas_report["class"].append(factory["class_name"])
            pandas_report["sensitivity"].append(json_report["sensitivity"])

        df = pd.DataFrame(pandas_report)

        if save:
            df.to_csv(self.java_project_address + "/factory_report.csv", index=False)
        return df






if __name__ == "__main__":
    java_project = "10_water-simulator"
    r = Report(java_project)
    json_report = r.get_json_factory_report(0.1, edit=False)
    pandas_report = r.get_pandas_factory_report(json_report)