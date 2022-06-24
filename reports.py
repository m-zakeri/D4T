import json
import pandas as pd
import matplotlib.pyplot as plt
import os

from class_diagram import ClassDiagram
from factory import Factory, FastFactory
from complexity import Complexity
from utils import File
import config

class Report:
    def __init__(self, java_project):
        self.java_project = java_project
        self.java_project_address = config.projects_info[java_project]['path']
        self.base_dirs = config.projects_info[java_project]['base_dirs']
        self.reload_from_disk()

    def reload_from_disk(self):
        self.files = File.find_all_file(self.java_project_address, 'java')
        self.index_dic = File.indexing_files_directory(self.files, 'class_index.json', self.base_dirs)
        self.cd = ClassDiagram(self.java_project_address, self.base_dirs, self.index_dic)
        self.cd.make_class_diagram()
        self.cdg = self.cd.get_CDG()

    def restore_java_project(self):
        os.chdir(f"{config.BASE_DIR}{self.java_project}")
        os.popen("git restore .").close()
        os.popen("git clean -f -d").close()
        path = os.getcwd()
        path = os.path.abspath(os.path.join(path, os.pardir))
        path = os.path.abspath(os.path.join(path, os.pardir))
        os.chdir(path)

    def get_code_changes_rate(self):
        os.chdir(f"{config.BASE_DIR}{self.java_project}")
        tmp = os.popen("git diff --shortstat").read()
        tmp = tmp.split()
        path = os.getcwd()
        path = os.path.abspath(os.path.join(path, os.pardir))
        path = os.path.abspath(os.path.join(path, os.pardir))
        os.chdir(path)
        return {"insertion":int(tmp[3]), "deletion":int(tmp[5])}

    def get_json_report(self, sensitivity, save=True, edit=True):
        pass

    def get_pandas_report(self, json_report, save=True):
        pass



class FactoryReport(Report):
    def get_single_report(self, sensitivity, edit=True):
        report = {
                "java_project":self.java_project,
                "sensitivity":sensitivity,
                "complexity": {"before": None, "after": None},
                "code_changes_rate": 0,
                "cases":None
            }

        c = Complexity(self.cdg)
        matrix = c.get_matrix()
        report["complexity"]["before"] = Complexity.get_avg_of_matrix(matrix)
        f = Factory()
        report["cases"] = f.refactor(
            sensitivity,
            self.index_dic,
            self.cd.class_diagram_graph,
            self.base_dirs,
            edit=edit
        )

        if edit:
            self.reload_from_disk()
            c = Complexity(self.cdg)
            matrix = c.get_matrix()
            report["complexity"]["after"] = Complexity.get_avg_of_matrix(matrix)

            code_changes_rate = self.get_code_changes_rate()
            report["code_changes_rate"] = code_changes_rate["insertion"] + code_changes_rate["deletion"]

        return report

    def get_list_of_report(self, save=True, edit=True):
        reports = list()
        for sensitivity in range(2):
            reports.append(self.get_single_report(sensitivity / 10, edit=edit))
            self.restore_java_project()
            self.reload_from_disk()

        if save:
            with open(f"{config.BASE_DIR}/{self.java_project}/factory_report.json", 'w') as f:
                json.dump(reports, f, indent=4)
        return reports

    def get_json_report_fast(self, sensitivity, fast_factory, save=True, edit=True):
        report = {
            "java_project":java_project,
            "sensitivity":sensitivity,
            "cases":None
        }

        report["cases"] = fast_factory.refactor(
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

    def show_avg_no_of_products_vs_sensitivity_chart_fast(self, show=True, save=False):
        sensitivity_list = list()
        avg_of_common_methods_list = list()
        f = FastFactory(self.index_dic)
        for sensitivity in range(10):
            json_report = self.get_json_report_fast(
                sensitivity / 10,
                f,
                edit=False,
                save=False
            )

            sensitivity_list.append(sensitivity / 10)
            print("json_report:", json_report)
            avg_of_common_methods_list.append(self.__get_avg_no_products(json_report))

        plt.plot(sensitivity_list, avg_of_common_methods_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('average number of products')
        if save:
            plt.savefig(self.java_project_address + "/avg_number_of_products_vs_sensitivity_chart_fast.png")
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
    #json_report = fr.get_single_report(0.1, edit=True)
    factory_report = fr.get_list_of_report()
    #pandas_report = fr.get_pandas_report(json_report)
    #fr.show_cases_vs_sensitivity_chart()
    #fr.show_avg_of_common_methods_vs_sensitivity_chart(show=False, save=True)
    #fr.show_avg_no_of_products_vs_sensitivity_chart(show=False, save=True)
    #fr.show_avg_no_of_products_vs_sensitivity_chart_fast(show=False, save=True)
    #subprocess.Popen(["cd", "diff", "--shortstat"], stdout=subprocess.PIPE)
