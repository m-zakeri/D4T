"""
The module provide learning to predict design test effectiveness
and design testability

"""

__version__ = '0.1.5'
__author__ = 'Morteza Zakeri'

import os
import math
import datetime

import numpy as np
import pandas as pd
import joblib
from joblib import dump, load
from matplotlib import pyplot as plt
import seaborn as sns
from scipy.stats import stats

from sklearn.linear_model import LassoCV, LarsCV
from sklearn.svm import NuSVR

from sklearn.metrics import *
from sklearn.model_selection import train_test_split
from sklearn.model_selection import ShuffleSplit, GridSearchCV
from sklearn import linear_model, feature_selection
from sklearn import tree, preprocessing

from sklearn.ensemble import (
    GradientBoostingRegressor, RandomForestRegressor, HistGradientBoostingRegressor, VotingRegressor
)
from sklearn.neural_network import MLPRegressor
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import DotProduct, WhiteKernel, Matern, RationalQuadratic, Exponentiation

from sklearn.inspection import permutation_importance

import smogn


class Regression:
    def __init__(self, df_path: str = None, selection_on=False, skewness=False):
        self.df = pd.read_csv(df_path, delimiter=',', index_col=False)
        self.df = self.df.fillna(0)
        self.df = self.df.loc[self.df['AbstractOrInterface'] == 0]
        # Smogn
        # self.dfx = self.df.iloc[:, 1:-14]
        # self.dfx['DesignMeanCoverage'] = self.df.iloc[:, -1]
        # self.dfx.drop(columns=['number_of_selfloops'], inplace=True)
        # print(self.dfx)
        if skewness:
            rg_mtrx = [

                [0.8, 1, 0],  ## over-sample ("minority")
                [0.4, 0, 0],  ## under-sample ("majority")
            ]
            self.dfx = smogn.smoter(
                data=self.dfx,
                y="DesignMeanCoverage",
                under_samp=False,
                samp_method='extreme',
                ## phi relevance arguments
                rel_thres=0.10,  ## real number (0 < R < 1)
                rel_method='manual',  ## string ('auto' or 'manual')
                # rel_xtrm_type = 'both', ## unused (rel_method = 'manual')
                # rel_coef = 1.50,        ## unused (rel_method = 'manual')
                rel_ctrl_pts_rg=rg_mtrx  ## 2d array (format: [x, y])
            )
            print(self.dfx)

        self.X_train1, self.X_test1, self.y_train, self.y_test = train_test_split(
            self.df.iloc[:, 2:-1],  # Features (node metrics)
            self.df.iloc[:, -1],  # Label (Testability in [0, 1])
            test_size=0.25,
            random_state=47,
        )

        if selection_on:
            # -- Feature selection (For DS2)
            selector = feature_selection.SelectKBest(feature_selection.f_regression, k=10)
            # clf = linear_model.LassoCV(eps=1e-3, n_alphas=100, normalize=True, max_iter=5000, tol=1e-4)
            # clf.fit(self.X_train1, self.y_train)
            # importance = np.abs(clf.coef_)
            # print('importance', importance)
            # clf = RandomForestRegressor()
            # selector = feature_selection.SelectFromModel(clf, prefit=False, norm_order=2,
            #                                               max_features=20, threshold=None)
            selector.fit(self.X_train1, self.y_train)

            # Get columns to keep and create new dataframe with only selected features
            cols = selector.get_support(indices=True)
            self.X_train1 = self.X_train1.iloc[:, cols]
            self.X_test1 = self.X_test1.iloc[:, cols]
            print('Selected columns by feature selection:', self.X_train1.columns)
            # -- End of feature selection

        # ---------------------------------------
        # Standardization
        self.scaler = preprocessing.RobustScaler(with_centering=True, with_scaling=True, unit_variance=True)
        self.scaler = preprocessing.StandardScaler()
        # self.scaler = preprocessing.QuantileTransformer(n_quantiles=1000, random_state=111)
        self.scaler.fit(self.X_train1)
        self.X_train = self.scaler.transform(self.X_train1)
        self.X_test = self.scaler.transform(self.X_test1)

        dump(self.scaler, 'scaler.joblib')

        # print(self.df.isnull().sum().sum())
        # rows_with_nan = [index for index, row in self.df.iterrows() if row.isnull().any()]
        # print(rows_with_nan)
        # quit()

    def inference_model(self, model=None, model_path=None, predict_data_path=None):
        if model is None:
            model = joblib.load(model_path)

        df_predict_data = pd.read_csv(predict_data_path, delimiter=',', index_col=False)
        X_test1 = df_predict_data.iloc[:, 1:]
        X_test = self.scaler.transform(X_test1)
        y_pred = model.predict(X_test)

        df_new = pd.DataFrame(df_predict_data.iloc[:, 0], columns=['Class'])
        df_new['PredictedTestability'] = list(y_pred)

        print(df_new)
        # df_new.to_csv(r'dataset/refactored01010_predicted_testability.csv', index=True, index_label='Row')

    def evaluate_model(self, model=None, model_path=None):
        # X = self.data_frame.iloc[:, 1:-4]
        # y = self.data_frame.iloc[:, -4]
        # X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, random_state=0)

        if model is None:
            model = joblib.load(model_path)

        y_true, y_pred = self.y_test, model.predict(self.X_test)
        # y_score = model.predict_proba(X_test)

        # Print all classifier model metrics
        print('Evaluating regressor ...')
        # print('Regressor minimum prediction', min(y_pred), 'Regressor maximum prediction', max(y_pred))
        df = pd.DataFrame()
        df['r2_score_uniform_average'] = [r2_score(y_true, y_pred, multioutput='uniform_average')]
        df['r2_score_variance_weighted'] = [r2_score(y_true, y_pred, multioutput='variance_weighted')]

        df['explained_variance_score_uniform_average'] = [
            explained_variance_score(y_true, y_pred, multioutput='uniform_average')]
        df['explained_variance_score_variance_weighted'] = [
            explained_variance_score(y_true, y_pred, multioutput='variance_weighted')]

        df['mean_absolute_error'] = [mean_absolute_error(y_true, y_pred)]
        df['mean_squared_error_MSE'] = [mean_squared_error(y_true, y_pred)]
        df['mean_squared_error_RMSE'] = [mean_squared_error(y_true, y_pred, squared=False)]
        df['median_absolute_error'] = [median_absolute_error(y_true, y_pred)]

        if min(y_pred) >= 0:
            df['mean_squared_log_error'] = [mean_squared_log_error(y_true, y_pred)]

        # To handle ValueError: Mean Tweedie deviance error with power=2
        # can only be used on strictly positive y and y_pred.
        if min(y_pred > 0) and min(y_true) > 0:
            df['mean_poisson_deviance'] = [mean_poisson_deviance(y_true, y_pred, )]
            df['mean_gamma_deviance'] = [mean_gamma_deviance(y_true, y_pred, )]
        df['max_error'] = [max_error(y_true, y_pred)]

        print(df)
        df.to_csv(model_path[:-7] + '_evaluation_metrics_R1.csv', index=True, index_label='Row')

    def regress(self, model_path: str = None, model_name: str = None):
        """

        :param model_path:
        :param model_number: 1: DTR, 2: RFR, 3: GBR, 4: HGBR, 5: SGDR, 6: MLPR,
        :return:
        """
        if model_name == 'DTR1':
            regressor = tree.DecisionTreeRegressor(random_state=23, )
            # Set the parameters to be used for tuning by cross-validation
            parameters = {
                'criterion': ['squared_error', 'friedman_mse', 'absolute_error'],
                'max_depth': range(3, 50, 1),
                'min_samples_split': range(2, 50, 1)
            }
        elif model_name == 'RFR1':
            regressor = RandomForestRegressor(random_state=19, )
            parameters = {
                'n_estimators': range(5, 50, 5),
                # 'criterion': ['squared_error', 'absolute_error'],
                'max_depth': range(5, 50, 5),
                # 'min_samples_split': range(2, 30, 2),
                # 'max_features': ['auto', 'sqrt', 'log2']
            }
        elif model_name == 'GBR1':
            regressor = GradientBoostingRegressor(n_estimators=500, learning_rate=0.05, random_state=17, )
            parameters = {
                'loss': ['ls', 'lad', ],
                'max_depth': range(3, 50, 1),
                'min_samples_split': range(2, 50, 1)
            }
        elif model_name == 'HGBR1':
            regressor = HistGradientBoostingRegressor(max_iter=500, learning_rate=0.05, random_state=13, )
            parameters = {
                # 'loss': ['least_squares', 'least_absolute_deviation'],
                'max_depth': range(3, 50, 5),
                'min_samples_leaf': range(2, 50, 5)
            }
        elif model_name == 'SGDR1':
            regressor = linear_model.SGDRegressor(early_stopping=True, n_iter_no_change=10, random_state=11, )
            parameters = {
                'loss': ['squared_loss', 'huber', 'epsilon_insensitive'],
                'penalty': ['l2', 'l1', 'elasticnet'],
                'max_iter': range(50, 500, 50),
                'learning_rate': ['invscaling', 'optimal', 'constant', 'adaptive'],
                'eta0': [0.1, 0.01, 0.005],
                # 'average': [32, ]
            }
        elif model_name == 'MLPR1':
            regressor = MLPRegressor(random_state=7, )
            parameters = {
                'hidden_layer_sizes': [(32, 64), (32, 64, 32), ],
                'activation': ['tanh', ],
                'solver': ['adam', ],
                'max_iter': range(10, 200, 10)
            }
        elif model_name == 'NuSVR1':
            regressor = NuSVR(cache_size=1000, max_iter=-1, shrinking=True)
            parameters = {
                'kernel': ['linear', 'rbf', 'poly', ],
                'degree': [1, 2],
                'nu': [i * 0.1 for i in range(1, 10, 2)],
                'C': [1.0, 2.0]
            }
        elif model_name == 'GPR':
            # https://towardsdatascience.com/7-of-the-most-commonly-used-regression-algorithms-and-how-to-choose-the-right-one-fc3c8890f9e3
            regressor = GaussianProcessRegressor(random_state=0)
            parameters = {
                'kernel': [DotProduct() + WhiteKernel(), DotProduct(), WhiteKernel(), Matern(),
                           Exponentiation(RationalQuadratic(alpha=1.0), exponent=2),
                           Exponentiation(RationalQuadratic(alpha=0.9), exponent=3),
                           RationalQuadratic(alpha=1.0)],
            }
        elif model_name == 'LassoCV':
            regressor = LarsCV()
            parameters = {
                "fit_intercept": [True, False],
            }
        else:
            return

        # Set the objectives which must be optimized during parameter tuning
        # scoring = ['r2', 'neg_mean_squared_error', 'neg_root_mean_squared_error', 'neg_mean_absolute_error',]
        scoring = ['neg_root_mean_squared_error', 'r2']
        # CrossValidation iterator object:
        # https://scikit-learn.org/stable/tutorial/statistical_inference/model_selection.html
        cv = ShuffleSplit(n_splits=5, test_size=0.20, random_state=101)
        # Find the best model using gird-search with cross-validation
        clf = GridSearchCV(regressor, param_grid=parameters, scoring=scoring, cv=cv,
                           n_jobs=4, refit='neg_root_mean_squared_error')
        print(f'Fitting {model_name} model')
        clf.fit(X=self.X_train, y=self.y_train)

        print('Writing grid search result ...')
        df = pd.DataFrame(clf.cv_results_, )
        df.to_csv(model_path[:-7] + '_grid_search_cv_results.csv', index=False)
        df = pd.DataFrame()
        print('Best parameters set found on development set:', clf.best_params_)
        df['best_parameters_development_set'] = [clf.best_params_]
        print('Best classifier score on development set:', clf.best_score_)
        df['best_score_development_set'] = [clf.best_score_]
        print('best classifier score on test set:', clf.score(self.X_test, self.y_test))
        df['best_score_test_set:'] = [clf.score(self.X_test, self.y_test)]
        df.to_csv(model_path[:-7] + '_grid_search_cv_results_best.csv', index=False)

        # Save and evaluate the best obtained model
        print('Writing evaluation result ...')
        clf = clf.best_estimator_
        y_true, y_pred = self.y_test, clf.predict(self.X_test)
        dump(clf, model_path)

        self.evaluate_model(model=clf, model_path=model_path)
        print('-' * 50)

    def vote(self, model_path=None, dataset_number=1):
        # Trained regressors
        reg1 = load(r'sklearn_models_nodes_regress/HGBR1_DS{0}.joblib'.format(dataset_number))
        reg2 = load(r'sklearn_models_nodes_regress/RFR1_DS{0}.joblib'.format(dataset_number))
        reg3 = load(r'sklearn_models_nodes_regress/MLPR1_DS{0}.joblib'.format(dataset_number))
        # reg4 = load(r'sklearn_models7/SGDR1_DS1.joblib')

        hgbr_r2 = 0.481711731335663  # 0.624798373109928
        rfr_r2 = 0.473849326241475  # 0.6390467327537
        mlpr_r2 = 0.37217497525279  # 0.569560495202819
        sum_r2 = hgbr_r2 + rfr_r2 + mlpr_r2
        ereg = VotingRegressor([('HGBR1_DS{0}'.format(dataset_number), reg1),
                                ('RFR1_DS{0}'.format(dataset_number), reg2),
                                ('MLPR1_DS{0}'.format(dataset_number), reg3)
                                ],
                               weights=[hgbr_r2 / sum_r2, rfr_r2 / sum_r2, mlpr_r2 / sum_r2])

        ereg.fit(self.X_train, self.y_train)
        dump(ereg, model_path)
        self.evaluate_model(model=ereg, model_path=model_path)


def train():
    # dataset_path = 'dataset_merged/d4t_ds_sf110_03.csv'
    # model_path = 'sklearn_models1/'
    dataset_path = 'dataset_merged/sf110_production_nodes.csv'
    model_path = 'sklearn_models_nodes_regress/'

    # models = ['DTR1', 'RFR1', 'GBR1', 'HGBR1', 'SGDR1', 'MLPR1', 'NuSVR1', 'GPR', 'LassoCV']
    # models = ['DTR1', 'RFR1', 'HGBR1', 'SGDR1', 'MLPR1', 'GPR', 'LassoCV', 'NuSVR1']
    models = ['GPR', 'LassoCV', 'NuSVR1']
    models = ['NuSVR1']
    ds_no = 2
    reg = Regression(df_path=dataset_path, selection_on=False)
    for model_number, model_name in enumerate(models):
        reg.regress(model_path=f'{model_path}{model_name}_DS{ds_no}.joblib', model_name=model_name)

    reg.vote(model_path=f'{model_path}VoR1_DS{ds_no}.joblib', dataset_number=1)


def merge_trained_model_results():
    trained_models_path = 'sklearn_models_nodes_regress/'
    ds_no = 2
    models = ['DTR1', 'RFR1', 'HGBR1', 'SGDR1', 'MLPR1', 'VoR1', ]
    df_results = pd.DataFrame()
    for model_name in models:
        df1 = pd.read_csv(f'{trained_models_path}{model_name}_DS{ds_no}_evaluation_metrics_R1.csv')
        df1.insert(loc=0, column='Model', value=[model_name])
        df_results = pd.concat([df_results, df1], ignore_index=True)

    print(df_results)
    df_results.to_csv(os.path.join(trained_models_path, f'results_merged_DS{ds_no}.csv'), index=False)


def compute_permutation_importance(model=None, n_repeats=100, scoring='r2'):
    dataset_path = 'dataset_merged/sf110_production_nodes.csv'
    model_path = 'sklearn_models_nodes_regress/VoR1_DS2.joblib'

    if model is None:
        model = load(model_path)
    reg = Regression(df_path=dataset_path, selection_on=False)

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
    df1.to_csv(f'sklearn_models_nodes_regress/VoR1_DS2_sc_{scoring}_rep{n_repeats}.csv', index=False)


def draw_important_features(n_features=16):
    df = pd.read_csv(r'sklearn_models_nodes_regress/VoR1_DS2_sc_r2_rep100.csv')  # R2
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


def draw_design_metrics_testability_relationship(n_features=15):
    df = pd.read_csv('dataset_merged/sf110_production_nodes.csv')

    df2 = pd.read_csv(r'sklearn_models_nodes_regress/VoR1_DS2_sc_r2_rep100.csv')  # R2
    df3 = df2.iloc[:, -1 * n_features:].iloc[:, ::-1]
    print(df3)

    df4 = df[df3.columns]
    df4['Testability'] = df['NodeTestability']
    df4 = df4.fillna(0)

    df4 = df4[(np.abs(stats.zscore(df4)) < 3).all(axis=1)]
    print(df4)

    for col_ in df4.columns[:-1]:
        df4[col_] = (df4[col_] - df4[col_].min()) / (df4[col_].max() - df4[col_].min())
        df4[col_] = df4[col_] * (1 - 0.0001) + 0.0001
        # df4[col_].replace(to_replace=0, value=0.001, inplace=True)

    col_order = ['OutDegree', 'KatzCentrality', 'CurrentFlowBetweennessCentrality', 'CurrentFlowClosenessCentrality',
                 'PageRank', 'AverageNeighborDegree', 'ClosenessCentrality', 'AverageDijkstraPathLength',
                 'EigenvectorCentrality', 'OutDegreeCentrality', 'DegreeCentrality', 'InDegree', 'HarmonicCentrality',
                 'InDegreeCentrality', 'BetweennessCentrality']

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
    df4 = df4.sample(frac=0.40, ignore_index=False)
    print(df4.columns)

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
            color='saddlebrown', #'indigo'
        )
        i += 1

    # g.legend()
    plt.tight_layout()
    plt.show()


if __name__ == '__main__':
    # train()
    # merge_trained_model_results()
    # compute_permutation_importance()
    # draw_important_features()
    draw_design_metrics_testability_relationship()

