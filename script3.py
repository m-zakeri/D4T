import json
import os

import pandas as pd

import config


def main(log_path, projects):
    injection_only = {
        'project': list(),
        'injection_changed_lines': list(),
        'injection_cases': list(),
        'testability_before': list(),
        'testability_after': list(),
        'relative_improvement': list(),
    }
    factory_only = {
        'project': list(),
        # 'code_lines': [5348, 4869, 3100, 3178, 387, 6612],
        'factory_changed_lines': list(),
        'factory_cases': list(),
        'testability_before': list(),
        'testability_after': list(),
        'relative_improvement': list(),
    }
    for project in projects:
        path = f'{log_path}/{project}/{project}_final_report.json'
        if not os.path.exists(path):
            continue

        with open(path) as f:
            if project == '6_jnfe':
                print('error')
            d = json.load(f)
            # result['code_lines'].append(0)
            factory_only['project'].append(project)
            factory_only['factory_changed_lines'].append(d['factory']['code_changes_rate'])
            factory_only['factory_cases'].append(len(d['factory']['cases']))
            before = d['factory']['testability']['before']
            after = d['factory']['testability']['after']
            relative_improvement = (after - before) / before
            factory_only['testability_before'].append(before)
            factory_only['testability_after'].append(after)
            factory_only['relative_improvement'].append(relative_improvement)

            injection_only['project'].append(project)
            injection_only['injection_changed_lines'].append(d['injection']['code_changes_rate'] - d['factory']['code_changes_rate'])
            injection_only['injection_cases'].append(d['injection']['cases']['case1'] + d['injection']['cases']['case2'])
            diff = d['injection']['testability']['before'] - d['factory']['testability']['before']
            before = d['injection']['testability']['before'] - diff
            after = d['injection']['testability']['after'] - diff
            relative_improvement = (after - before) / before
            injection_only['testability_before'].append(before)
            injection_only['testability_after'].append(after)
            injection_only['relative_improvement'].append(relative_improvement)

    factory_df = pd.DataFrame(factory_only)
    factory_df = factory_df.sort_values('relative_improvement', ascending=False)
    factory_df.to_csv(f'{log_path}/factory_only.csv')

    injection_df = pd.DataFrame(injection_only)
    injection_df = injection_df.sort_values('relative_improvement', ascending=False)
    injection_df.to_csv(f'{log_path}/injection_only.csv')



if __name__ == "__main__":
    log_path = '/media/sadegh/Data/sadegh/iust/Bachlour/compiler/compiler projects/main_project/reports/d4t_log'
    main(log_path, config.SF110_projects)