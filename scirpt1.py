import json
import pandas as pd


def main(log_path, projects):
    result = {
        'project': projects,
        'code_lines': [5348, 4869, 3100, 3178, 387, 6612],
        'factory_changed_lines': list(),
        'injection_changed_lines': list(),
        'factory_cases': list(),
        'injection_cases': list()
    }
    for project in projects:
        with open(f'{log_path}/{project}/{project}_final_report.json') as f:
            d = json.load(f)
            result['code_lines'].append(0)
            result['factory_changed_lines'].append(d['factory']['code_changes_rate'])
            result['injection_changed_lines'].append(d['injection']['code_changes_rate'] - d['factory']['code_changes_rate'])
            result['factory_cases'].append(len(d['factory']['cases']))
            result['injection_cases'].append(d['injection']['cases']['case1'] + d['injection']['cases']['case2'])

    df = pd.DataFrame(result)
    df.to_csv('/media/sadegh/Data/sadegh/iust/Bachlour/compiler/compiler projects/main_project/reports/evosuit_results/extra_info.csv')



if __name__ == "__main__":
    projects = [
        '85_shop',
        '1_tullibee',
        '25_jni-inchi',
        '68_biblestudy',
        '90_dcparseargs',
        '99_newzgrabber',
    ]
    log_path = '/media/sadegh/Data/sadegh/iust/Bachlour/compiler/compiler projects/main_project/reports/d4t_log'
    main(log_path, projects)