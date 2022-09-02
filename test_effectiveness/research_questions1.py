"""


"""

import os

import numpy as np
import pandas as pd
import scipy.stats as stats
import joblib

from sklearn.inspection import permutation_importance
from sklearn.preprocessing import LabelEncoder

from matplotlib import pyplot as plt
import seaborn as sns

from test_effectiveness.ml_models import Regression
from test_effectiveness.ml_models_design_metrics import RegressionDesign


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
    print(df.columns[[4, 5]])
    # df.to_excel('learned_model_with_SMOTEENN.xlsx', index=False)


def draw_important_features(n_features=10):
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


def correlation_heatmap():
    dataset_path = r'dataset_merged/sf110_edges_binary.csv'
    df = pd.read_csv(dataset_path)
    df = df.iloc[:, 2:]
    df = df.fillna(0)

    label_encoder = LabelEncoder()
    # df.iloc[:, 0] = label_encoder.fit_transform(df.iloc[:, 0]).astype('float64')

    print(df)

    corr = df.corr(
        method='pearson',  # ‘kendall’, ‘spearman’
        min_periods=1000
    )
    corr = corr.sort_values(by=['IsCovered'], ascending=False)
    print(corr)
    # corr.to_csv('correlation_analysis.csv',)

    for col in df.columns:
        r, p = stats.pearsonr(df[col], df['IsCovered'])
        print(f'col:{col}, correlation coefficient: {r:.4}, p-value: {p:.4}')

    plt.subplots(figsize=(14, 8))
    sns.heatmap(corr)
    plt.tight_layout()
    plt.show()


def merge_trained_model_results():
    trained_models_path = 'sklearn_models_nodes_regress/'
    ds_no = 2
    ds_no = 7
    models = ['DTR1', 'RFR1', 'HGBR1', 'SGDR1', 'MLPR1', 'VoR1', ]
    models = ['DTR1', 'RFR1', 'HGBR1', 'MLPR1', 'VR1', ]
    df_results = pd.DataFrame()
    for model_name in models:
        df1 = pd.read_csv(f'{trained_models_path}{model_name}_DS{ds_no}_evaluation_metrics_R1.csv')
        df1.insert(loc=0, column='Model', value=[model_name])
        df_results = pd.concat([df_results, df1], ignore_index=True)

    print(df_results)
    df_results.to_csv(os.path.join(trained_models_path, f'results_merged_DS{ds_no}.csv'), index=False)


def compute_permutation_importance(model=None, n_repeats=100, scoring='r2'):
    dataset_path = 'dataset_merged/DS07710.csv'
    model_path = 'sklearn_models_nodes_regress/VR1_DS7.joblib'

    if model is None:
        model = joblib.load(model_path)
    reg = RegressionDesign(df_path=dataset_path, selection_on=False)

    result = permutation_importance(
        model, reg.X_test, reg.y_test,
        scoring=scoring,
        n_repeats=n_repeats,
        random_state=42,
        n_jobs=4
    )

    perm_sorted_idx = result.importances_mean.argsort()
    result_top_features = result.importances[perm_sorted_idx].T
    labels_list = []
    for label in reg.X_test1.columns[perm_sorted_idx]:
        labels_list.append(label)
    df1 = pd.DataFrame(data=result_top_features, columns=labels_list)
    print('Top metrics:\n', df1)
    df1.to_csv(f'sklearn_models_nodes_regress/VoR1_DS7_sc_{scoring}_rep{n_repeats}.csv', index=False)


def draw_important_features2(n_features=15):
    df = pd.read_csv(r'sklearn_models_nodes_regress/VoR1_DS7_sc_r2_rep100.csv')  # R2
    df2 = pd.melt(
        df.iloc[:, -1 * n_features:].iloc[:, ::-1], id_vars=None,
        value_name='Importance', var_name='Design metric'
    )

    sns.set_style('ticks', {'xtick.major.size': 0.0005, 'axes.facecolor': '1.0'})
    f, ax = plt.subplots(figsize=(9, 5))
    # ax.set_xscale('logit')
    sns.boxplot(data=df2,
                x='Importance', y='Design metric',
                width=.700,
                linewidth=.70
                )

    # Add in points to show each observation
    sns.stripplot(data=df2,
                  x='Importance', y='Design metric',
                  size=1.55,
                  color='.35',
                  linewidth=0.20, )

    # Tweak the visual presentation
    # ax.set(ylabel="")
    ax.xaxis.grid(b=True, which='both')
    sns.despine(top=True, right=True, left=False, bottom=False, offset=None, trim=False)
    plt.tight_layout()
    plt.show()


def draw_design_metrics_testability_relationship(n_features=10):
    df = pd.read_csv('dataset_merged/sf110_production_nodes.csv')
    df = pd.read_csv('dataset_merged/DS07710.csv')

    df2 = pd.read_csv(r'sklearn_models_nodes_regress/VoR1_DS7_sc_r2_rep100.csv')  # R2
    df3 = df2.iloc[:, -1 * n_features:].iloc[:, ::-1]
    print('DF', df)

    df4 = df[df3.columns]
    df4['Testability'] = df['Testability']
    df4 = df4.fillna(0)

    df4 = df4[(np.abs(stats.zscore(df4)) < 3).all(axis=1)]
    print('DF4', df4)

    for col_ in df4.columns[:-1]:
        df4[col_] = (df4[col_] - df4[col_].min()) / (df4[col_].max() - df4[col_].min())
        df4[col_] = df4[col_] * (1 - 0.0001) + 0.0001
        df4[col_].replace(to_replace=0, value=0.001, inplace=True)

    col_order = ['CBO', 'NMO', 'FANIN', 'FANOUT', 'NIM', 'NOII', 'NOI', 'NOP', 'NOC', 'DIT']

    col_regress_info = []
    for col_ in col_order:
        slope, intercept, r_value, p_value, std_err = stats.linregress(df4[col_], df4['Testability'])
        print(col_)
        r, p = stats.pearsonr(df4[col_], df4['Testability'])
        col_regress_info.append((
            slope,
            intercept,
            r_value,
            p_value,
            std_err,
            r,
            p,
        ))

    print(col_regress_info)
    # quit()

    df4 = df4.sample(frac=0.80, ignore_index=False)
    # df4 = df4.sample(frac=0.40, ignore_index=False)
    # print(df4.columns)

    df4 = df4.melt(id_vars='Testability', var_name='Metric', value_name='Value', )
    print(df4)

    # sns.relplot(data=df4,
    #             x='Value', y='Testability',
    #             col='Metric', col_wrap=5,
    #             height=2.5, aspect=1.15,
    #             kind='line'
    #             )

    sns.set(font_scale=1.05)
    g = sns.FacetGrid(
        data=df4, hue='Metric', col='Metric', col_wrap=5,
        height=2.95, aspect=0.975,
        sharex=False, sharey=False,
        # palette='turbo',
        legend_out=True
    )

    g.map(
        # sns.jointplot,
        sns.regplot,
        "Value", "Testability",
        truncate=True,
        x_bins=500,
        x_ci='sd',
        ci=95,
        # scatter=False,
        n_boot=1000,
        # lw=0.5,
        line_kws={'lw': 1.5,
                  'color': 'm',
                  # 'color': '#4682b4',
                  # 'label': "y={0:.1f}x+{1:.1f}".format(2.5, 3.5)
                  },

    )

    # g.map(
    #     sns.lineplot,
    #     "Value", "Testability",
    #
    # )

    """
    g = sns.lmplot(
        data=df4,
        x='Value', y='Testability',
        hue='Metric', col='Metric', col_wrap=5,

        height=2.5, aspect=1.15,

        fit_reg=True,
        truncate=True,
        # logx=True,
        x_ci='sd',
        ci=95,
        n_boot=1000,
        # x_estimator=np.mean,
        # robust=True, order=1,

        x_bins=1000,
        # common_bins=False,

        scatter_kws={'s': 10,

                     },
        line_kws={'lw': 1.05,
                  'color': 'm',
                  # 'color': '#4682b4',
                  },
        # facet_kws=dict(sharex=False, sharey=False,)
        facet_kws={'sharey': False, 'sharex': False}

    )
    """

    # for species, ax in g.axes_dict.items():
    #     print(ax)

    i = 0
    for ax, title in zip(g.axes.flat, col_order):
        # ax.set_title(title)
        ax.text(
            0.5, 0.15, f'{round(col_regress_info[i][5], 5)}',
            fontsize=12, fontweight='bold',
            horizontalalignment='center', verticalalignment='center', transform=ax.transAxes,
            color='saddlebrown',
        )
        ax.text(
            0.5, 0.05, f'({col_regress_info[i][6]:.4E})',
            fontsize=12, fontweight='bold',
            horizontalalignment='center', verticalalignment='center', transform=ax.transAxes,
            color='saddlebrown',  # 'indigo'
        )
        i += 1

    # g.legend()
    plt.tight_layout()
    plt.show()


def draw_sf110_projects_design_testability_distribution():
    data_path = 'dataset_nodes'
    files = [f for f in os.listdir(data_path) if os.path.isfile(os.path.join(data_path, f))]
    df_all = pd.DataFrame()
    for project_csv_data in files:
        df = pd.read_csv(os.path.join(data_path, project_csv_data), index_col=False)
        df1 = pd.DataFrame()
        df1['Project'] = [project_csv_data[:-9]]
        df1['DesignTestability'] = df['NodeTestability'].mean()
        df_all = pd.concat([df_all, df1])

    df_all.to_csv('dataset_merged/sf110_production_nodes_projects_testability.csv', index=False)


def modularity_testability_relation():
    modularity_path = '../testability/SF110_codart_modularity_production_code.csv'
    testability_path = 'dataset_merged/sf110_production_nodes_projects_testability.csv'
    df_modularity = pd.read_csv(modularity_path, index_col=False)
    df_testability = pd.read_csv(testability_path, index_col=False)
    print(df_modularity)
    print(df_testability)
    df_result = pd.merge(df_modularity, df_testability, how="inner", on=["Project"])
    print(df_result)
    result_path = 'dataset_merged/sf110_production_nodes_projects_testability_modularity.csv'
    # df_result.to_csv(result_path, index=False)
    r, p = stats.pearsonr(df_result['DesignTestability'], df_result['Modularity'])
    print(r, p)


def testability_complexity_relation():
    result_path = r'D:/Users/Morteza/OneDrive/Online2/_04_2o/o2_university/PhD/Project21/a160_design_testability/experiments/'
    result_file = r'interaction_complexity_versus_testability.xlsx'
    df = pd.read_excel(result_path + result_file, sheet_name='report_1')
    # print(df)
    df['1/complexity_avg_ranked'] = df['1/complexity_avg'].rank(ascending=True)
    df['testability_ranked'] = df['testability'].rank(ascending=True)

    print(df)

    # Rank-order correlation
    r, p = stats.spearmanr(df['1/complexity_avg'], df['testability'])
    print('Spearman ranked correlation coefficient:', r)
    print('p-value:', p)

    r2, p2 = stats.kendalltau(df['1/complexity_avg'], df['testability'])
    print('kendalltau ranked correlation coefficient:', r2)
    print('p-value:', p2)

    # Pearson correlation
    r3, p3 = stats.pearsonr(df['1/complexity_avg'], df['testability'])
    print('Pearson correlation coefficient:', r3)
    print('p-value:', p3)

    # sns.relplot( data=df, x="1/complexity_avg_ranked", y="testability_ranked", kind="line",)
    # sns.kdeplot(data=df, x="no_relationships", y='testability', hue="1/complexity_avg_ranked", multiple="stack")
    df.rename(columns={'no_relationships': 'Number of relationships',
                       '1/complexity_avg_ranked': '1/Complexity ranks',
                       'testability_ranked': 'Testability ranks'
                       }, inplace=True)

    df2 = pd.melt(df, id_vars=['Number of relationships', 'no_classes'], var_name='Measure',
                  value_vars=['1/Complexity ranks', 'Testability ranks'], value_name='Rank')


    g = sns.lmplot(data=df2, x='Number of relationships', y='Rank', hue='Measure',
                   truncate=True, markers=['o', 'X'],
                   # line_kws={'ls': ['-', '--']},
                   legend_out=True,
                   )

    g.ax.text(
        0.5, 0.15, f'Spearman correlation: {round(r, 5)}',
        fontsize=12, fontweight='bold',
        horizontalalignment='center', verticalalignment='center', transform=g.ax.transAxes,
        color='saddlebrown',
    )
    g.ax.text(
        0.5, 0.05, f'p-value: ({p:.5E})',
        fontsize=12, fontweight='bold',
        horizontalalignment='center', verticalalignment='center', transform=g.ax.transAxes,
        color='saddlebrown',  # 'indigo'
    )

    plt.show()


if __name__ == '__main__':
    # merge_learning_reports()
    # draw_important_features(15)
    # correlation_heatmap()
    # merge_trained_model_results()
    # compute_permutation_importance()
    # draw_important_features2()
    # draw_design_metrics_testability_relationship()
    # draw_sf110_projects_design_testability_distribution()
    # modularity_testability_relation()
    testability_complexity_relation()
