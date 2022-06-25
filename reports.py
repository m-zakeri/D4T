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

    def get_list_of_report(self, no_of_samples, save=True, edit=True):
        reports = list()
        for sensitivity in range(no_of_samples):
            reports.append(self.get_single_report(sensitivity / (no_of_samples - 1), edit=edit))
            if edit:
                self.restore_java_project()
                self.reload_from_disk()

        if save:
            with open(f"{config.BASE_DIR}/{self.java_project}/factory_report.json", 'w') as f:
                json.dump(reports, f, indent=4)
        return reports

    def get_single_report_fast(self, sensitivity, fast_factory, save=True):
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
            edit=False
        )

        if save:
            with open(f"{config.BASE_DIR}/{self.java_project}/factory_report_fast.json", 'w') as f:
                json.dump(report, f, indent=4)
        return report

    def get_list_of_report_fast(self, no_of_samples, save=True):
        reports = list()
        f = FastFactory(self.index_dic)
        for sensitivity in range(no_of_samples):
            report = self.get_single_report_fast(
                sensitivity / (no_of_samples - 1),
                f,
                save=False
            )
            reports.append(report)

        if save:
            with open(f"{config.BASE_DIR}/{self.java_project}/factory_report_fast.json", 'w') as f:
                json.dump(reports, f, indent=4)
        return reports




    def get_pandas_report(self, json_report, save=True):
        pandas_report = {
            "project":[],
            "package": [],
            "path": [],
            "class": [],
            "sensitivity": []
        }
        for report in json_report:
            for case in report["cases"]:
                factory = case["factory"]
                pandas_report["project"].append(report["java_project"])
                pandas_report["creator_package"].append(factory["package"])
                pandas_report["creator_path"].append(factory["path"])
                pandas_report["creator_class"].append(factory["class_name"])
                pandas_report["sensitivity"].append(report["sensitivity"])

        df = pd.DataFrame(pandas_report)

        if save:
            df.to_csv(f"{config.BASE_DIR}/{self.java_project}/factory_report.csv", index=False)
        return df

    def show_cases_vs_sensitivity_chart(self, json_report, show=True, save=True):
        sensitivity_list = list()
        no_cases_list = list()
        for report in json_report:
            sensitivity_list.append(report["sensitivity"])
            no_cases_list.append(len(report["cases"]))

        plt.plot(sensitivity_list, no_cases_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('number of cases')
        if save:
            plt.savefig(f"{config.BASE_DIR}/{self.java_project}/cases_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_avg_of_common_methods_vs_sensitivity_chart(self, json_report, show=True, save=True):
        sensitivity_list = list()
        avg_of_common_methods_list = list()
        for report in json_report:
            sensitivity_list.append(report["sensitivity"])
            avg_of_common_methods_list.append(self.__get_avg_no_methods(report))

        plt.plot(sensitivity_list, avg_of_common_methods_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('average number of common methods')
        if save:
            plt.savefig(f"{config.BASE_DIR}/{self.java_project}/avg_of_common_methods_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_avg_no_of_products_vs_sensitivity_chart(self, json_report, show=True, save=True):
        sensitivity_list = list()
        avg_of_common_methods_list = list()
        for report in json_report:
            sensitivity_list.append(report["sensitivity"])
            avg_of_common_methods_list.append(self.__get_avg_no_products(report))

        plt.plot(sensitivity_list, avg_of_common_methods_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('average number of products')
        if save:
            plt.savefig(f"{config.BASE_DIR}/{self.java_project}/avg_number_of_products_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_complexity_vs_sensitivity_chart(self, json_report, show=True, save=True):
        sensitivity_list = list()
        complexity_list = list()
        for report in json_report:
            sensitivity_list.append(report["sensitivity"])
            complexity_list.append(report["complexity"]["after"] - report["complexity"]["before"])

        plt.plot(sensitivity_list, complexity_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('The rate of change of complexity')
        if save:
            plt.savefig(f"{config.BASE_DIR}/{self.java_project}/complexity_vs_sensitivity_chart.png")
        if show:
            plt.show()

    def show_code_changed_rate_vs_sensitivity_chart(self, json_report, show=True, save=True):
        sensitivity_list = list()
        complexity_list = list()
        for report in json_report:
            sensitivity_list.append(report["sensitivity"])
            complexity_list.append(report["code_changes_rate"])

        plt.plot(sensitivity_list, complexity_list)
        plt.title(self.java_project)
        plt.xlabel('sensitivity')
        plt.ylabel('code changes rate')
        if save:
            plt.savefig(f"{config.BASE_DIR}/{self.java_project}/code_changed_rate_vs_sensitivity_chart.png")
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
    #factory_report = fr.get_list_of_report(3)
    with open(f"{config.BASE_DIR}/{java_project}/factory_report.json") as f:
        json_report = json.load(f)
    #pandas_report = fr.get_pandas_report(json_report)
    #fr.show_cases_vs_sensitivity_chart(json_report)
    #fr.show_avg_of_common_methods_vs_sensitivity_chart(json_report_fast)
    #fr.show_avg_no_of_products_vs_sensitivity_chart(json_report)
    #fr.get_list_of_report_fast(5)
    #fr.show_complexity_vs_sensitivity_chart(json_report)
    fr.show_code_changed_rate_vs_sensitivity_chart(json_report)