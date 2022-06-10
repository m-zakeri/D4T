import json
import pandas as pd
import matplotlib.pyplot as plt

from class_diagram import ClassDiagram
from factory import Factory
from utils import File
import config

class Report:
    def reload_from_disk(self):
        pass

    def get_json_report(self, sensitivity, save=True, edit=True):
        pass

    def get_pandas_report(self, json_report, save=True):
        pass



class FactoryReport(Report):
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

    def get_json_report(self, sensitivity, save=True, edit=True):
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

    def get_pandas_report(self, json_report, save=True):
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

    def show_cases_vs_sensitivity_chart(self, show=True, save=False):
        sensitivity_list = list()
        no_cases_list = list()
        for sensitivity in range(10):
            json_report = self.get_json_report(sensitivity / 10, edit=False, save=False)
            pandas_report = self.get_pandas_report(json_report, save=False)
            sensitivity_list.append(sensitivity / 10)
            no_cases_list.append(pandas_report.shape[0])

        plt.plot(sensitivity_list, no_cases_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('number of cases')
        if save:
            plt.savefig(self.java_project_address + "/cases_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_avg_of_common_methods_vs_sensitivity_chart(self, show=True, save=False):
        sensitivity_list = list()
        avg_of_common_methods_list = list()
        for sensitivity in range(10):
            json_report = self.get_json_report(sensitivity / 10, edit=False, save=False)
            sensitivity_list.append(sensitivity / 10)
            avg_of_common_methods_list.append(self.__get_avg_no_methods(json_report))

        plt.plot(sensitivity_list, avg_of_common_methods_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('average number of common methods')
        if save:
            plt.savefig(self.java_project_address + "/avg_of_common_methods_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_avg_no_of_products_vs_sensitivity_chart(self, show=True, save=False):
        sensitivity_list = list()
        avg_of_common_methods_list = list()
        for sensitivity in range(10):
            json_report = self.get_json_report(sensitivity / 10, edit=False, save=False)
            sensitivity_list.append(sensitivity / 10)
            avg_of_common_methods_list.append(self.__get_avg_no_products(json_report))

        plt.plot(sensitivity_list, avg_of_common_methods_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('average number of products')
        if save:
            plt.savefig(self.java_project_address + "/avg_number_of_products_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def __get_avg_no_methods(self, json_report):
        a = 0
        b = 0
        for case in json_report["cases"]:
            b += 1
            a += len(case["products"]["methods"])
        return round(a / b, 2)

    def __get_avg_no_products(self, json_report):
        a = 0
        b = 0
        for case in json_report["cases"]:
            b += 1
            a += len(case["products"]["classes"])
        return round(a / b, 2)


if __name__ == "__main__":
    java_project = "10_water-simulator"
    fr = FactoryReport(java_project)
    #json_report = fr.get_json_report(0.1, edit=False)
    #pandas_report = fr.get_pandas_report(json_report)
    #fr.show_cases_vs_sensitivity_chart()
    #fr.show_avg_of_common_methods_vs_sensitivity_chart(show=False, save=True)
    fr.show_avg_no_of_products_vs_sensitivity_chart(show=False, save=True)