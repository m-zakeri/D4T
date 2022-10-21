import json
import pandas as pd
import matplotlib.pyplot as plt
import os
import logging

import config
from reports import FactoryReport, InjectionReport
from utils import timeout

from testability.design_testability_prediction2 import main as evaluate_testability
from testability.directory_utils import create_understand_database

logging.basicConfig(filename='errors.log', level=logging.DEBUG,
                    format='%(asctime)s %(levelname)s %(name)s %(message)s')
logger = logging.getLogger(__name__)


def get_repost_1(json_reports):
    pandas_report = {
        "project": [],
        "sensitivity": [],
        "complexity_before": [],
        "complexity_after": [],
        "complexity_time": [],
        "testability_before": [],
        "testability_after": [],
        "testability_time": []
    }

    for json_report in json_reports:
        for report in json_report:
            pandas_report["project"].append(report["java_project"])
            pandas_report["sensitivity"].append(report["sensitivity"])
            pandas_report["complexity_before"].append(report["complexity"]['before'])
            pandas_report["complexity_after"].append(report["complexity"]['after'])
            pandas_report["complexity_time"].append(0)
            pandas_report["testability_before"].append(report["testability"]['before'])
            pandas_report["testability_after"].append(report["testability"]['after'])
            pandas_report["testability_time"].append(0)

    df = pd.DataFrame(pandas_report)

    df.to_csv(f"{config.BASE_DIR}/report_1.csv", index=False)
    return df


def f(path, rootdir):
    java_project = path.split('/')[-1]
    config.projects_info[java_project] = dict()
    java_project_address = f'{rootdir}/{java_project}/src/main/java'
    base_dirs = list()
    base_dirs.append(f'{rootdir}/{java_project}/src/main/java/')
    config.projects_info[java_project]['path'] = java_project_address
    config.projects_info[java_project]['base_dirs'] = base_dirs
    config.projects_info[java_project]['db_path'] = f'{rootdir}/{java_project}.und'
    config.projects_info[java_project]['log_path'] = f'{rootdir}/{java_project}/{java_project}_testability_s2.csv'

    fr = FactoryReport(java_project)
    fr.restore_java_project()
    fr.reload_from_disk()
    factory_report = fr.get_list_of_report(10)
    # factory_report = fr.get_single_report(0.5, edit=False)
    with open(f"{rootdir}/{java_project}/{java_project}_factory_report.json", 'w') as f:
        json.dump(factory_report, f, indent=4)


def f2(path, rootdir):
    java_project = path.split('/')[-1]
    config.projects_info[java_project] = dict()
    java_project_address = f'{rootdir}/{java_project}/src/main/java'
    base_dirs = list()
    base_dirs.append(f'{rootdir}/{java_project}/src/main/java/')
    config.projects_info[java_project]['path'] = java_project_address
    config.projects_info[java_project]['base_dirs'] = base_dirs
    config.projects_info[java_project]['db_path'] = f'{rootdir}/{java_project}.und'
    config.projects_info[java_project]['log_path'] = f'{rootdir}/{java_project}/{java_project}_testability_s2.csv'

    sensitivity = 0.1
    fr = FactoryReport(java_project)
    factory_report = fr.get_single_report(sensitivity)
    ir = InjectionReport(java_project)
    injection_report = ir.get_single_report(save=False)
    report = {'factory': factory_report, 'injection': injection_report}
    with open(f"{config.BASE_DIR}/{java_project}/{java_project}_final_report.json", 'w') as f:
        json.dump(report, f, indent=4)


if __name__ == "__main__":
    # java_projects = [
    #     "10_water-simulator",
    #     # "jfreechart",
    #     # "88_jopenchart",
    #     # "tabula-java",
    #     # "61_noen",
    #     # "xerces2j",
    #     "1_tullibee",
    #     "2_a4j"
    # ]
    # json_reports = []
    # for java_project in java_projects:
    #
    #     with open(f"{config.BASE_DIR}/{java_project}/factory_report.json") as f:
    #         json_report = json.load(f)
    #         json_reports.append(json_report)
    # get_repost_1(json_reports)

    # if __name__ == "__main__":
    #     java_projects = [
    #         # "1_tullibee",
    #         # "2_a4j",
    #         # "5_templateit",
    #         "8_gfarcegestionfa",
    #         # "10_water-simulator",
    #         # "13_jdbacl"
    #     ]
    #     for java_project in java_projects:
    #         fr = FactoryReport(java_project, True)
    #         factory_report = fr.get_list_of_report(10)

    files = [f for f in os.listdir(config.BASE_DIR) if not os.path.isfile(os.path.join(config.BASE_DIR, f))]
    for project_name in files:
        print(f'Creating, adding and analyzing UNDs for {config.BASE_DIR}{project_name}')
        # cmd_ = 'und convert -override {0}'.format(project_path)
        cmd_ = f'und create -db {config.UBD_DIR}{project_name}.und -languages java'
        os.system(f'cmd /c "{cmd_}"')
        cmd_ = f'und add "{config.BASE_DIR + project_name}" {config.UBD_DIR}{project_name}.und'
        os.system(f'cmd /c "{cmd_}"')
        cmd_ = f'und analyze -all  {config.UBD_DIR}{project_name}.und'
        os.system(f'cmd /c "{cmd_}"')

        print(f'Creating, adding and committing project files to git for {config.BASE_DIR}{project_name}')
        os.chdir(f'{config.BASE_DIR}{project_name}')
        os.popen("git init").close()
        os.popen("git add .").close()
        os.popen('git commit -m "first commit"').close()
        # path = os.getcwd()
        # path = os.path.abspath(os.path.join(path, os.pardir))
        # path = os.path.abspath(os.path.join(path, os.pardir))
        # os.chdir(path)
        print('*' * 75)

    quit()

    rootdir = config.BASE_DIR
    for it in os.scandir(rootdir):
        if it.is_dir():
            path = str(it.path)
            path = path.replace('\\', '/')
            print(path)
            java_project = '10_water-simulator'
            if path[-4:] != '.und' and java_project in path:
                os.chdir(path)
                os.popen("git init").close()
                os.popen("git add .").close()
                os.popen('git commit -m "first commit"').close()
                path = os.getcwd()
                path = os.path.abspath(os.path.join(path, os.pardir))
                path = os.path.abspath(os.path.join(path, os.pardir))
                os.chdir(path)
                create_understand_database(f'{rootdir}{java_project}', config.UBD_DIR)
    quit()
    rootdir = 'C:/Users/Zakeri/Desktop/SF110_copy/SF110'
    x = 0
    config.BASE_DIR = rootdir + '/'
    paths = []
    for it in os.scandir(rootdir):
        if it.is_dir():
            path = str(it.path)
            path = path.replace('\\', '/')
            paths.append(path)
    for path in paths[::-1]:
        print(path)
    #         if path[-4:] != '.und':
    #             x += 1
    #             if x > 5:
    #                 try:
    #                     print(path)
    #                     f(path, rootdir)
    #                 except Exception as e:
    #                     logger.error(path)
    #                     logger.error(str(e))
    #                     logger.error('-'*20)
    #     break

    # from testability.directory_utils import create_understand_database
    # create_understand_database('benchmarks/javaproject', 'benchmarks/javaproject')

    # rootdir = 'C:/Users/Zakeri/Desktop/SF110'
    # timeout_projects = [
    #     # '105_freemind',
    #     '107_weka'
    #     # '57_hft-bomberman'
    # ]
    #
    # for project in timeout_projects:
    #     db_path = f'{rootdir}/{project}.und'
    #     log_path = f'{rootdir}/{project}/{project}_testability_s2.csv'
    #     print(project)
    #     testability, testability_time = evaluate_testability(
    #         db_path,
    #         initial_value=1.0,
    #         verbose=False,
    #         log_path=log_path
    #     )
    #
    #     print(project, testability, testability_time)
