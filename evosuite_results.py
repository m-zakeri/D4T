import glob
import pandas as pd



def get_results(path, result_path, project_name):
    result = pd.DataFrame()
    # result = pd.read_csv(f'{path}/result.csv')
    tmp_path = glob.glob(path+'tmp*')[0]
    tmp_path = glob.glob(tmp_path+'/reports')[0]

    for index, csv_path in enumerate(glob.glob(tmp_path+'/*/*.csv')):
        df = pd.read_csv(csv_path)
        result = result.append(df)
    result.loc['mean'] = result.mean()
    result.to_csv(f'{result_path}/{project_name}.csv')



def main(SOURCE_DIR, result_path):
    projects = [
        '85_shop',
    ]

    for project in projects:
        get_results(f'{SOURCE_DIR}/{project}/.evosuite/', result_path, project)


if __name__ == '__main__':
    SOURCE_DIR = '/home/sadegh'
    main(SOURCE_DIR, SOURCE_DIR)
