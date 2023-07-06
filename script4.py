import os
import pandas as pd
import json


def get_relative_improvement(before, after):
    return round((after - before) / before, 5) * 100


def get_final_csv_report(d4t_log_path):
    pandas_report = {
        "project": [],
        "sensitivity": [],
        "no_classes": [],
        "testability_original": [],
        "testability_after_factory": [],
        "testability_after_injection": [],
        "relative_improvement_after_factory": [],
        "relative_improvement_after_injection": [],
        "relative_improvement_total": [],
    }

    projects = [f for f in os.listdir(d4t_log_path) if not os.path.isfile(os.path.join(d4t_log_path, f))]
    for project in projects:
        report_path = os.path.join(d4t_log_path, project, f'{project}_final_report.json')
        if not os.path.exists(report_path):
            continue
        with open(report_path) as f:
            report = json.load(f)

            pandas_report["project"].append(report['factory']["java_project"])
            pandas_report["sensitivity"].append(report['factory']["sensitivity"])
            pandas_report["no_classes"].append(report['factory']["no_classes"]['before'])
            pandas_report["testability_original"].append(report['factory']["testability"]['before'])
            pandas_report["testability_after_factory"].append(report['factory']["testability"]['after'])
            pandas_report["testability_after_injection"].append(report['injection']["testability"]['after'])
            pandas_report["relative_improvement_after_factory"].append(
                get_relative_improvement(
                    pandas_report["testability_original"][-1],
                    pandas_report["testability_after_factory"][-1]
                )
            )
            pandas_report["relative_improvement_after_injection"].append(
                get_relative_improvement(
                    pandas_report["testability_after_factory"][-1],
                    pandas_report["testability_after_injection"][-1]
                )
            )
            pandas_report["relative_improvement_total"].append(
                get_relative_improvement(
                    pandas_report["testability_original"][-1],
                    pandas_report["testability_after_injection"][-1]
                )
            )
    df = pd.DataFrame(pandas_report).sort_values(by=['relative_improvement_total'], ascending=False)
    df.to_csv(f"{d4t_log_path}/final_report.csv", index=False)
    return df


if __name__ == '__main__':
    d4t_log_path = 'reports/d4t_log'
    get_final_csv_report(d4t_log_path)