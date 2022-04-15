import os
import pandas as pd
import seaborn as sns
from matplotlib import pyplot as plt
from sklearn.preprocessing import LabelEncoder


def merge_learning_reports():
    dtc_results_path = 'sklearn_models_edge_classify2/DTC1_DS2_evaluation_metrics_C1.csv'
    rfc_results_path = 'sklearn_models_edge_classify2/RFC1_DS2_evaluation_metrics_C1.csv'
    hgbc_results_path = 'sklearn_models_edge_classify2/HGBC1_DS2_evaluation_metrics_C1.csv'
    mlpc_results_path = 'sklearn_models_edge_classify2/MLPC1_DS2_evaluation_metrics_C1.csv'
    voc_results_path = 'sklearn_models_edge_classify2/VR2_DS2_evaluation_metrics_C1.csv'
    models = ['DTC', 'RFC', 'HGBC', 'MLPC', 'VoC']
    paths = [dtc_results_path, rfc_results_path, hgbc_results_path, mlpc_results_path, voc_results_path]
    df = pd.DataFrame()
    for path_ in paths:
        df1 = pd.read_csv(path_)
        df = pd.concat([df, df1], ignore_index=True)

    df.drop(columns=['Row'], inplace=True)
    df.insert(loc=0, column='Classifier', value=models)
    print(df)
    print(df.columns[[4,5]])
    # df.to_excel('learned_model_with_SMOTEENN.xlsx', index=False)


def draw_important_features(n_features=28):
    df = pd.read_csv(r'sklearn_models_edge_classify2/vc_model_feature_importance_balanced_accuracy_31.csv')

    df.rename(columns={'CSNOST_AVG.1': 'CSNOSTD_AVG'}, inplace=True)
    df2 = pd.melt(df.iloc[:, -1 * n_features:].iloc[:, ::-1], id_vars=None,
                  value_name='Importance', var_name='Source code metric')

    sns.set_style('ticks', {'xtick.major.size': 0.0005, 'axes.facecolor': '1.0'})
    f, ax = plt.subplots(figsize=(8, 4))
    # ax.set_xscale('logit')
    sns.boxplot(data=df2,
                x='Importance', y='Source code metric',
                width=.700,
                linewidth=.70
                )

    # Add in points to show each observation
    sns.stripplot(data=df2,
                  x='Importance', y='Source code metric',
                  size=1.25,
                  color='.55',
                  linewidth=0.20, )

    ax.xaxis.grid(b=True, which='both')
    sns.despine(top=True, right=True, left=False, bottom=False, offset=None, trim=False)
    plt.tight_layout()
    plt.show()


def correlaion_heatmap():
    dataset_path = r'dataset_merged/sf110_edges_binary.csv'
    df = pd.read_csv(dataset_path)
    df = df.iloc[:, 2:]
    df = df.fillna(0)

    label_encoder = LabelEncoder()
    df.iloc[:, 0] = label_encoder.fit_transform(df.iloc[:, 0]).astype('float64')

    corr = df.corr(min_periods=1000)
    print(df)
    print(corr)
    # corr.to_csv('correlation_analysis.csv',)

    plt.subplots(figsize=(14, 8))
    sns.heatmap(corr)
    plt.tight_layout()
    plt.show()


if __name__ == '__main__':
    # merge_learning_reports()
    # draw_important_features(15)
    correlaion_heatmap()