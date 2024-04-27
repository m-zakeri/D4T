"""
The module provide learning to predict design test effectiveness

"""

__version__ = '0.1.5'
__author__ = 'Morteza Zakeri'

import os
import math
import datetime
from copy import deepcopy

from matplotlib import pyplot as plt

import pandas as pd
import joblib
from joblib import dump, load

from sklearn.metrics import *
from sklearn.preprocessing import QuantileTransformer
from sklearn.inspection import permutation_importance
from sklearn.linear_model import LassoCV, LarsCV
from sklearn.svm import NuSVR, NuSVC
from sklearn.neural_network import MLPRegressor, MLPClassifier
from sklearn.model_selection import train_test_split
from sklearn.model_selection import ShuffleSplit, GridSearchCV
from sklearn import tree, preprocessing
from sklearn.ensemble import GradientBoostingRegressor, RandomForestRegressor, HistGradientBoostingRegressor, \
    VotingRegressor, GradientBoostingClassifier, RandomForestClassifier, HistGradientBoostingClassifier, \
    VotingClassifier

from sklearn import linear_model, feature_selection
from sklearn.gaussian_process import GaussianProcessRegressor, GaussianProcessClassifier
from sklearn.gaussian_process.kernels import DotProduct, WhiteKernel, Matern, RationalQuadratic, Exponentiation

from imblearn.over_sampling import SMOTE, ADASYN
from imblearn.combine import SMOTEENN, SMOTETomek


class EdgeCoverageClassification:
    def __init__(self, df_path: str = None, selection_on=False, skewness=False):
        self.df = pd.read_csv(df_path, delimiter=',', index_col=False)
        # Compelling with NaNs
        number_of_nans = self.df.isnull().sum().sum()
        print('number_of_nans', number_of_nans)
        rows_with_nan = [index for index, row in self.df.iterrows() if row.isnull().any()]
        print('rows_with_nan', rows_with_nan)
        if number_of_nans < 10:
            self.df = self.df.fillna(0)
        else:
            print('Too many NaNs. Cannot continue.')
            return

        # self.dfx = self.df.iloc[:, 1:-14]
        self.X_train1, self.X_test1, self.y_train, self.y_test = train_test_split(self.df.iloc[:, 2:-1],
                                                                                  self.df.iloc[:, -1],
                                                                                  test_size=0.25,
                                                                                  random_state=111,
                                                                                  )

        print('Before resampling')
        print(self.X_train1)
        print('Number of covered edges', self.X_train1[self.y_train == 1].shape[0])
        print('Number of uncovered edges', self.X_train1[self.y_train == 0].shape[0])
        if skewness:
            sampler = SMOTE(random_state=11)
            sampler = ADASYN(random_state=11)
            sampler = SMOTETomek(random_state=11)
            sampler = SMOTEENN(random_state=11, )
            self.X_train1, self.y_train = sampler.fit_resample(self.X_train1, self.y_train)

            df2 = pd.DataFrame(deepcopy(self.X_train1))
            df2['IsCovered'] = deepcopy(self.y_train)
            print('After resampling')
            print('X train 2', self.X_train1)
            print('Number of covered edges', df2[df2['IsCovered'] == 1].shape[0])
            print('Number of uncovered edges', df2[df2['IsCovered'] == 0].shape[0])

        # quit()
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
        # self.scaler = QuantileTransformer(n_quantiles=1000, random_state=111)
        self.scaler.fit(self.X_train1)
        self.X_train = self.scaler.transform(self.X_train1)
        self.X_test = self.scaler.transform(self.X_test1)

    def inference_model(self, model=None, model_path=None, predict_data_path=None):
        if model is None:
            model = joblib.load(model_path)

        df_predict_data = pd.read_csv(predict_data_path, delimiter=',', index_col=False)
        X_test1 = df_predict_data.iloc[:, 2:]
        X_test = self.scaler.transform(X_test1)
        y_pred = model.predict(X_test)
        y_score = model.predict_proba(X_test)

        df_new = pd.DataFrame(df_predict_data.iloc[:, 0:2], columns=['FromClass', 'ToClass'])
        df_new['PredictedDesignEdgeCoverage'] = list(y_pred)
        df_new['PredictedDesignEdgeCoverageProbability'] = list(y_score)
        print(df_new)
        mean_edge_coverage = df_new['PredictedDesignEdgeCoverage'].mean()
        mean_edge_coverage_probability = df_new['PredictedDesignEdgeCoverageProbability'].mean()
        print(f'Design edge coverage (edge test effectiveness) {mean_edge_coverage}')
        print(
            f'Design edge coverage probability (edge test effectiveness probability) {mean_edge_coverage_probability}')
        df_new.to_csv(predict_data_path[:-4] + '_predicted.csv', index=False)

    def evaluate_model(self, model=None, model_path=None, binary_mode=True):
        if model is None:
            model = joblib.load(model_path)
        y_true, y_pred = self.y_test, model.predict(self.X_test)
        y_score = model.predict_proba(self.X_test)

        print('Evaluating classifier ...')
        df = pd.DataFrame()
        df['accuracy_score'] = [accuracy_score(y_true, y_pred)]
        df['balanced_accuracy_score'] = [balanced_accuracy_score(y_true, y_pred)]
        df['precision_score_macro'] = [precision_score(y_true, y_pred, average='macro')]
        df['precision_score_micro'] = [precision_score(y_true, y_pred, average='micro')]
        df['recall_score_macro'] = [recall_score(y_true, y_pred, average='macro')]
        df['recall_score_micro'] = [recall_score(y_true, y_pred, average='micro')]
        df['f1_score_macro'] = [f1_score(y_true, y_pred, average='macro')]
        df['f1_score_micro'] = [f1_score(y_true, y_pred, average='micro')]
        df['fbeta_score_macro'] = [fbeta_score(y_true, y_pred, beta=0.5, average='macro')]
        df['fbeta_score_micro'] = [fbeta_score(y_true, y_pred, beta=0.5, average='micro')]

        df['log_loss'] = [log_loss(y_true, y_score)]

        if binary_mode:
            # y_score = y_score.reshape(-1, 1)
            y_score = y_score[:, 1]
        df['roc_auc_score_ovr_macro'] = [roc_auc_score(y_true, y_score, multi_class='ovr', average='macro')]
        df['roc_auc_score_ovr_micro'] = [roc_auc_score(y_true, y_score, multi_class='ovr', average='weighted')]
        df['roc_auc_score_ovo_macro'] = [roc_auc_score(y_true, y_score, multi_class='ovo', average='macro')]
        df['roc_auc_score_ovo_micro'] = [roc_auc_score(y_true, y_score, multi_class='ovo', average='weighted')]
        # print('roc_curve_:', roc_curve(y_true, y_score))  # multiclass format is not supported

        print(df)
        df.to_csv(model_path[:-7] + '_evaluation_metrics_C1.csv', index=True, index_label='Row')

    def compute_confusion_matrix(self, model=None, model_path=None):
        if model is None:
            model = joblib.load(model_path)
        y_true, y_pred = self.y_test, model.predict(self.X_test)
        y_score = model.predict_proba(self.X_test)
        title = 'Confusion matrix, without normalization'
        disp = plot_confusion_matrix(
            model, self.X_test, self.y_test,
            # y_true_,  # need for using with Binarize the output
            # display_labels=class_names,
            cmap=plt.cm.Blues,
            normalize=None,
            # normalize='true',
        )
        disp.ax_.set_title(title)
        print('Confusion matrix:')
        print(disp.confusion_matrix)
        # plt.show()
        plt.savefig(model_path[:-7] + '_confusion_matrix.png')

    def classify(self, model_path: str = None, model_number: int = None):
        """

        :param model_path:
        :param model_number: 1: DTC, 2: RFC, 3: GBC, 4: HGBC, 5: SGDC, 6: MLPC,
        :return:
        """
        if model_number == 1:
            clf_def = tree.DecisionTreeClassifier(random_state=23, )
            # Set the parameters to be used for tuning by cross-validation
            parameters = {
                'criterion': ['gini', 'entropy'],
                'max_depth': range(3, 50, 5),
                'min_samples_split': range(2, 50, 5)
            }
        elif model_number == 2:
            clf_def = RandomForestClassifier(random_state=19, )
            parameters = {
                'n_estimators': range(50, 500, 50),
                # 'criterion': ['gini', 'entropy'],
                'max_depth': range(5, 50, 5),
                # 'min_samples_split': range(2, 30, 2),
                # 'max_features': ['auto', 'sqrt', 'log2']
            }
        elif model_number == 3:
            clf_def = GradientBoostingClassifier(n_estimators=500, learning_rate=0.05, random_state=17, )
            parameters = {
                'loss': ['ls', 'lad', ],
                'max_depth': range(3, 50, 5),
                'min_samples_split': range(2, 50, 5)
            }
        elif model_number == 4:
            clf_def = HistGradientBoostingClassifier(max_iter=500, learning_rate=0.05, random_state=13, )
            parameters = {
                # 'loss': ['least_squares', 'least_absolute_deviation'],
                'max_depth': range(3, 50, 5),
                'min_samples_leaf': range(2, 50, 5)
            }
        elif model_number == 5:
            clf_def = linear_model.SGDClassifier(early_stopping=True, n_iter_no_change=10, random_state=11, )
            parameters = {
                'loss': ['squared_loss', 'huber', 'epsilon_insensitive'],
                'penalty': ['l2', 'l1', 'elasticnet'],
                'max_iter': range(50, 200, 50),
                'learning_rate': ['invscaling', 'optimal', 'constant', 'adaptive'],
                'eta0': [0.1, 0.01],
                # 'average': [32, ]
            }
        elif model_number == 6:
            clf_def = MLPClassifier(random_state=7, )
            parameters = {
                'hidden_layer_sizes': [(32, 64), (32, 64, 32), ],
                'activation': ['tanh', ],
                'solver': ['adam', ],
                'max_iter': range(50, 200, 50)
            }
        elif model_number == 7:
            clf_def = NuSVC(cache_size=500, max_iter=-1, shrinking=True)
            parameters = {
                'kernel': ['linear', 'rbf', 'poly', 'sigmoid', ],
                'degree': [1, 2, ],
                'nu': [i * 0.1 for i in range(1, 10, 2)],
                'C': [1.0, 2.0, ]
            }
        elif model_number == 8:
            clf_def = GaussianProcessClassifier(random_state=0)
            parameters = {
                'kernel': [DotProduct() + WhiteKernel(), DotProduct(), WhiteKernel(), Matern(),
                           Exponentiation(RationalQuadratic(alpha=1.0), exponent=2),
                           Exponentiation(RationalQuadratic(alpha=0.9), exponent=3),
                           RationalQuadratic(alpha=1.0)],
            }
        else:
            return

        print('Fitting model number', model_number)
        # Set the objectives which must be optimized during parameter tuning
        # scoring = ['balanced_accuracy', 'recall', 'roc_auc', 'f1', 'precision']
        scoring = ['balanced_accuracy', ]
        cv = ShuffleSplit(n_splits=5, test_size=0.20, random_state=101)
        # Find the best model using the grid-search with the cross-validation
        clf = GridSearchCV(clf_def, param_grid=parameters, scoring=scoring, cv=cv, n_jobs=1, refit='balanced_accuracy')
        clf.fit(X=self.X_train, y=self.y_train)

        print('Writing the grid-search results ...')
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
        print('Writing evaluation results ...')
        clf = clf.best_estimator_
        y_true, y_pred = self.y_test, clf.predict(self.X_test)
        # y_score = clf.fit(X_train, y_train).decision_function(X_test)
        # y_score = clf.predict_proba(self.X_test)
        print('Classification report:')
        print(classification_report(y_true, y_pred))
        print('.' * 55)
        print('Precision, recall, F-score, and support micro:')
        print(precision_recall_fscore_support(y_true, y_pred, average='micro'))
        print('Precision, recall, F-score, and support macro:')
        print(precision_recall_fscore_support(y_true, y_pred, average='macro'))

        dump(clf, model_path)
        self.evaluate_model(model=clf, model_path=model_path)

        # Plots
        self.compute_confusion_matrix(model=clf, model_path=model_path)

        print('-' * 25, 'Done!', '-' * 25)

    def vote(self, model_path=None, dataset_number=1):
        print('Load and combine trained classifiers:')
        clf0 = load(f'sklearn_models_edge_classify2/DTC1_DS{dataset_number}.joblib')
        clf1 = load(f'sklearn_models_edge_classify2/RFC1_DS{dataset_number}.joblib')
        clf2 = load(f'sklearn_models_edge_classify2/HGBC1_DS{dataset_number}.joblib')
        clf3 = load(f'sklearn_models_edge_classify2/MLPC1_DS{dataset_number}.joblib')
        # clf4 = load(f'sklearn_models_edge_classify_1/SGDC1_DS{dataset_number}.joblib')

        voting_classifier = VotingClassifier(
            [
                (f'DTC1_DS{dataset_number}', clf0),
                (f'RFC1_DS{dataset_number}', clf1),
                (f'HGBC1_DS{dataset_number}', clf2),
                (f'MLPC1_DS{dataset_number}', clf3)
            ],
            weights=[0.3, 0.4, 0.2, 0.1],
            voting='soft'
        )

        voting_classifier.fit(self.X_train, self.y_train)
        dump(voting_classifier, model_path)
        self.evaluate_model(model=voting_classifier, model_path=model_path)
        self.compute_confusion_matrix(model=voting_classifier, model_path=model_path)


def train(ds_no=2):
    # dataset_path = r'dataset_edges/66_openjms_EDGE.csv'
    dataset_path = r'dataset_merged/sf110_edges_binary.csv'
    model_path = 'sklearn_models_edge_classify2/'
    ecclf = EdgeCoverageClassification(df_path=dataset_path, selection_on=False, skewness=True)
    # ecclf.classify(model_path=f'{model_path}DTC1_DS{ds_no}.joblib', model_number=1)
    # ecclf.classify(model_path=f'{model_path}RFC1_DS{ds_no}.joblib', model_number=2)
    # ecclf.classify(model_path=f'{model_path}GBC1_DS{ds_no}.joblib', model_number=3)
    # ecclf.classify(model_path=f'{model_path}HGBC1_DS{ds_no}.joblib', model_number=4)
    # ecclf.classify(model_path=f'{model_path}SGDC1_DS{ds_no}.joblib', model_number=5)
    # ecclf.classify(model_path=f'{model_path}MLPC1_DS{ds_no}.joblib', model_number=6)
    # ecclf.classify(model_path=f'{model_path}NuSVC1_DS{ds_no}.joblib', model_number=7)
    # ecclf.classify(model_path=f'{model_path}GPR_DS{ds_no}.joblib', model_number=8)
    ecclf.vote(model_path=f'{model_path}VR2_DS{ds_no}.joblib', dataset_number=2)


def compute_permutation_importance(model_path=None, model=None, n_repeats=31, scoring='balanced_accuracy'):
    dataset_path = r'dataset_merged/sf110_edges_binary.csv'
    model_path = 'sklearn_models_edge_classify2/RFC1_DS2.joblib'
    ecclf = EdgeCoverageClassification(df_path=dataset_path, selection_on=False, skewness=True)
    if model is None:
        model = load(model_path)
    result = permutation_importance(model,  ecclf.X_test,  ecclf.y_test,
                                    scoring=scoring,
                                    n_repeats=n_repeats,
                                    random_state=71,
                                    n_jobs=2)
    perm_sorted_idx = result.importances_mean.argsort()
    result_top_features = result.importances[perm_sorted_idx].T
    print('Top 15 metrics:\n', result_top_features[:, -15:])
    df1 = pd.DataFrame(data=result_top_features, columns=ecclf.X_test1.columns[perm_sorted_idx])
    df1.to_csv(f'vc_model_feature_importance_{scoring}_{n_repeats}.csv', index=False)
    print('Finished.')


if __name__ == '__main__':
    # train()
    compute_permutation_importance()
