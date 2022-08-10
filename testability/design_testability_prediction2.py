"""

## Introduction

This module contains light-weight version of ADAFEST source testability prediction script (with 10 metrics)
to be used to predict design testability

## Changelog
### v0.2.3
- Remove dependency to metrics_jcode_odor
### v0.2.2
- Add scikit-learn 1 compatibility


## Reference
[1] ADAFEST paper
[2] TsDD paper


"""

__version__ = '0.2.3'
__author__ = 'Morteza Zakeri'

import os
import pandas as pd
import joblib
from joblib import Parallel, delayed

import understand as und

from metrics import metrics_names
from metrics.metrics_coverability import UnderstandUtility

scaler1 = joblib.load(
    os.path.join(os.path.dirname(__file__),
                 '../test_effectiveness/sklearn_models_nodes_regress/DS07710_scaler.joblib')
)
model5 = joblib.load(
    os.path.join(os.path.dirname(__file__),
                 '../test_effectiveness/sklearn_models_nodes_regress/VR1_DS7.joblib')
)


class TestabilityMetrics:
    """

    Compute all required metrics for computing Coverageability and testability.

    """

    @classmethod
    def get_package_metrics_names(cls) -> list:
        return ['NOI']

    @classmethod
    def get_class_lexicon_metrics_names(cls) -> list:
        return metrics_names.class_lexicon_metrics_names

    @classmethod
    def get_class_ordinary_metrics_names(cls) -> list:
        return ['AbstractOrInterface', 'DIT', 'NOC', 'NOP', 'NIM', 'NMO', 'NOII', 'FANIN', 'FANOUT', 'CBO']

    @classmethod
    def get_all_metrics_names(cls) -> list:
        metrics = list()
        # print('project_metrics number: ', len(TestabilityMetrics.get_project_metrics_names()))
        # for metric_name in TestabilityMetrics.get_project_metrics_names():
        #     metrics.append('PJ_' + metric_name)

        # print('package_metrics number: ', len(TestabilityMetrics.get_package_metrics_names()))
        for metric_name in TestabilityMetrics.get_package_metrics_names():
            metrics.append('PK_' + metric_name)

        # SOOTI is now corrected.
        # print('class_lexicon_metrics number: ', len(TestabilityMetrics.get_class_lexicon_metrics_names()))
        for metric_name in TestabilityMetrics.get_class_lexicon_metrics_names():
            metrics.append('CSLEX_' + metric_name)

        # print('class_ordinary_metrics number: ', len(TestabilityMetrics.get_class_ordinary_metrics_names()))
        for metric_name in TestabilityMetrics.get_class_ordinary_metrics_names():
            metrics.append('CSORD_' + metric_name)

        # print('All available metrics: {0}'.format(len(metrics)))
        return metrics

    @classmethod
    def get_all_primary_metrics_names(cls) -> list:
        primary_metrics_names = ['AbstractOrInterface', 'DIT', 'NOC', 'NOP', 'NIM', 'NMO', 'NOII', 'FANIN', 'FANOUT', 'CBO', 'NOI', ]
        return primary_metrics_names

    @classmethod
    def compute_java_package_metrics(cls, db=None, entity=None):
        """
        Find package: strategy 2: Dominated strategy

        """
        #
        class_name = entity.longname()
        class_name_list = class_name.split('.')[:-1]
        package_name = '.'.join(class_name_list)
        # print('package_name string', package_name)
        package_list = db.lookup(package_name + '$', 'Package')
        if package_list is None:
            return None
        if len(package_list) == 0:  # if len != 1 return None!
            return None
        package = package_list[0]
        # print('kind:', package.kind())
        # print('Computing package metrics for class: "{0}" in package: "{1}"'.format(class_name, package.longname()))

        package_metrics = dict()
        # classes_and_interfaces_list = package.ents('Contain', 'Java Type ~Unknown ~Unresolved ~Jar ~Library')

        # PKNOMNAMM: Package number of not accessor or mutator methods

        pknoi = len(UnderstandUtility.get_package_interfaces_java(package_entity=package))
        # pknoac = len(UnderstandUtility.get_package_abstract_class_java(package_entity=package))
        package_metrics.update({'NOI': pknoi})
        # package_metrics.update({'PKNOAC': pknoac})

        # print('package metrics', len(package_metrics), package_metrics)
        return package_metrics

    @classmethod
    def compute_java_class_metrics_lexicon(cls, entity=None):
        """
        Args:

            entity (understand.Ent):

        Returns:

             dict: class-level metrics

        """

        class_lexicon_metrics_dict = dict()
        tokens_list = list()
        identifiers_list = list()
        keywords_list = list()
        operators_list = list()

        return_and_print_count = 0
        return_and_print_kw_list = ['return', 'print', 'printf', 'println', 'write', 'writeln']

        condition_count = 0
        condition_kw_list = ['if', 'for', 'while', 'switch', '?', 'assert', ]

        uncondition_count = 0
        uncondition_kw_list = ['break', 'continue', ]

        exception_count = 0
        exception_kw_list = ['try', 'catch', 'throw', 'throws', 'finally', ]

        new_count = 0
        new_count_kw_list = ['new']

        super_count = 0
        super_count_kw_list = ['super']

        dots_count = 0
        # print(entity.longname())
        lexeme = entity.lexer(show_inactive=False).first()
        while lexeme is not None:
            tokens_list.append(lexeme.text())
            if lexeme.token() == 'Identifier':
                identifiers_list.append(lexeme.text())
            if lexeme.token() == 'Keyword':
                keywords_list.append(lexeme.text())
            if lexeme.token() == 'Operator':
                operators_list.append(lexeme.text())
            if lexeme.text() in return_and_print_kw_list:
                return_and_print_count += 1
            if lexeme.text() in condition_kw_list:
                condition_count += 1
            if lexeme.text() in uncondition_kw_list:
                uncondition_count += 1
            if lexeme.text() in exception_kw_list:
                exception_count += 1
            if lexeme.text() in new_count_kw_list:
                new_count += 1
            if lexeme.text() in super_count_kw_list:
                super_count += 1
            if lexeme.text() == '.':
                dots_count += 1
            lexeme = lexeme.next()

        number_of_assignments = operators_list.count('=')
        number_of_operators_without_assignments = len(operators_list) - number_of_assignments
        number_of_unique_operators = len(set(list(filter('='.__ne__, operators_list))))

        class_lexicon_metrics_dict.update({'NumberOfTokens': len(tokens_list)})
        class_lexicon_metrics_dict.update({'NumberOfUniqueTokens': len(set(tokens_list))})

        class_lexicon_metrics_dict.update({'NumberOfIdentifies': len(identifiers_list)})
        class_lexicon_metrics_dict.update({'NumberOfUniqueIdentifiers': len(set(identifiers_list))})

        class_lexicon_metrics_dict.update({'NumberOfKeywords': len(keywords_list)})
        class_lexicon_metrics_dict.update({'NumberOfUniqueKeywords': len(set(keywords_list))})

        class_lexicon_metrics_dict.update(
            {'NumberOfOperatorsWithoutAssignments': number_of_operators_without_assignments})
        class_lexicon_metrics_dict.update({'NumberOfAssignments': number_of_assignments})
        class_lexicon_metrics_dict.update({'NumberOfUniqueOperators': number_of_unique_operators})

        class_lexicon_metrics_dict.update({'NumberOfDots': dots_count})
        class_lexicon_metrics_dict.update({'NumberOfSemicolons': entity.metric(['CountSemicolon'])['CountSemicolon']})

        class_lexicon_metrics_dict.update({'NumberOfReturnAndPrintStatements': return_and_print_count})
        class_lexicon_metrics_dict.update({'NumberOfConditionalJumpStatements': condition_count})
        class_lexicon_metrics_dict.update({'NumberOfUnConditionalJumpStatements': uncondition_count})
        class_lexicon_metrics_dict.update({'NumberOfExceptionStatements': exception_count})
        class_lexicon_metrics_dict.update({'NumberOfNewStatements': new_count})
        class_lexicon_metrics_dict.update({'NumberOfSuperStatements': super_count})

        # print('class lexicon metrics dict', len(class_lexicon_metrics_dict), class_lexicon_metrics_dict)
        return class_lexicon_metrics_dict

    @classmethod
    def compute_java_class_metrics2(cls, db=None, entity=None):
        """
        Strategy #2: Take a list of all classes and search for target class
        Which strategy is used for our final setting? I do not know!

        Args:
            db (understand.Db):

            entity (understand.Ent): Class entity

        Returns:

            dict: Class-level metrics

        """
        class_metrics = dict()
        method_list = UnderstandUtility.get_method_of_class_java2(db=db, class_name=entity.longname())
        if method_list is None:
            # raise TypeError('method_list is none for class "{}"'.format(entity.longname()))
            print('method_list is none for class "{}"'.format(entity.longname()))
            return None

        if 'Interface' in entity.kind().name():
            class_metrics.update({'AbstractOrInterface': 1})
        else:
            class_metrics.update({'AbstractOrInterface': 0})

        class_metrics.update({'DIT': entity.metric(['MaxInheritanceTree'])['MaxInheritanceTree']})
        class_metrics.update({'NOC': entity.metric(['CountClassDerived'])['CountClassDerived']})
        class_metrics.update({'NOP': entity.metric(['CountClassBase'])['CountClassBase']})

        # Inheritance metrics
        class_metrics.update({'NIM': UnderstandUtility.NIM(class_name=entity)})
        class_metrics.update({'NMO': UnderstandUtility.NMO(class_name=entity)})
        class_metrics.update({'NOII': UnderstandUtility.NOII(db=db)})  # Not implemented
        # class_metrics.update({'RFC': UnderstandUtility.RFC(class_name=entity)})
        class_metrics.update({'FANIN': UnderstandUtility.FANIN(db=db, class_entity=entity)})
        class_metrics.update({'FANOUT': UnderstandUtility.FANOUT(db=db, class_entity=entity)})
        class_metrics.update({'CBO': entity.metric(['CountClassCoupled'])['CountClassCoupled']})

        # class_metrics.update({'ATFD': UnderstandUtility.ATFD(db=db, class_entity=entity)})  # Not implement

        # class_metrics.update({'CFNAMM': UnderstandUtility.CFNAMM_Class(class_name=entity)})
        # class_metrics.update({'DAC': UnderstandUtility.get_data_abstraction_coupling(db=db, class_entity=entity)})
        # class_metrics.update({'NumberOfMethodCalls': UnderstandUtility.number_of_method_call(class_entity=entity)})

        # Visibility metrics
        # Understand built-in metrics plus one custom metric.
        # class_metrics.update({'CSNOAMM': UnderstandUtility.NOMAMM(class_entity=entity)})

        # print('class metrics', len(class_metrics), class_metrics)
        return class_metrics


def do(class_entity_long_name, project_db_path):
    import understand as und
    db = und.open(project_db_path)
    class_entity = UnderstandUtility.get_class_entity_by_name(class_name=class_entity_long_name, db=db)
    one_class_metrics_value = [class_entity.longname()]

    # print('Calculating package metrics')
    package_metrics_dict = TestabilityMetrics.compute_java_package_metrics(db=db, entity=class_entity)
    if package_metrics_dict is None or len(package_metrics_dict) == 0:
        return None

    # print('Calculating class lexicon metrics')
    # class_lexicon_metrics_dict = TestabilityMetrics.compute_java_class_metrics_lexicon(entity=class_entity)
    # if class_lexicon_metrics_dict is None or len(class_lexicon_metrics_dict) == 0:
    #     return None

    # print('Calculating class ordinary metrics')
    class_ordinary_metrics_dict = TestabilityMetrics.compute_java_class_metrics2(db=db, entity=class_entity)
    if class_ordinary_metrics_dict is None or len(class_ordinary_metrics_dict) == 0:
        return None

    # one_class_metrics_value.extend([class_lexicon_metrics_dict[metric_name] for
    #                                 metric_name in TestabilityMetrics.get_class_lexicon_metrics_names()])

    one_class_metrics_value.extend([class_ordinary_metrics_dict[metric_name] for
                                    metric_name in TestabilityMetrics.get_class_ordinary_metrics_names()])

    one_class_metrics_value.extend([package_metrics_dict[metric_name] for
                                    metric_name in TestabilityMetrics.get_package_metrics_names()])

    db.close()
    del db
    # print(one_class_metrics_value)
    # quit()
    return one_class_metrics_value


# ------------------------------------------------------------------------
class PreProcess:
    """

    Writes all metrics in a csv file and performs preprocessing

    """

    @classmethod
    def compute_metrics_by_class_list(cls, project_db_path, n_jobs):
        """


        """

        # class_entities = cls.read_project_classes(db=db, classes_names_list=class_list, )
        # print(project_db_path)
        db = und.open(project_db_path)
        class_list = UnderstandUtility.get_project_classes_longnames_java(db=db)
        db.close()
        # del db

        if n_jobs == 0:  # Sequential computing
            res = [do(class_entity_long_name, project_db_path) for class_entity_long_name in class_list]
        else:  # Parallel computing
            res = Parallel(n_jobs=n_jobs, )(
                delayed(do)(class_entity_long_name, project_db_path) for class_entity_long_name in class_list
            )
        res = list(filter(None, res))

        columns = ['Class']
        columns.extend(TestabilityMetrics.get_all_primary_metrics_names())
        # print('*' * 50)
        # print(len(columns), columns)
        # print('*' * 50)

        df = pd.DataFrame(data=res, columns=columns)
        # print('df for class {0} with shape {1}'.format(project_name, df.shape))
        # df.to_csv(csv_path + project_name + '.csv', index=False)
        # print(df)
        return df


class TestabilityModel:
    """

    Testability prediction model

    """

    def __init__(self, ):
        self.scaler = scaler1
        self.model = model5

    def inference(self, df_predict_data=None, verbose=False, log_path=None):
        df_predict_data = df_predict_data.fillna(0)
        df_predict_data1 = df_predict_data.loc[df_predict_data['AbstractOrInterface'] == 1]
        df_predict_data2 = df_predict_data.loc[df_predict_data['AbstractOrInterface'] == 0]

        X_test1 = df_predict_data2.iloc[:, 2:]
        X_test = self.scaler.transform(X_test1)
        y_pred = self.model.predict(X_test)

        df_predict_data2['Testability'] = list(y_pred)

        df_predict_data1['Testability'] = [1] * len(df_predict_data1)
        df_new = pd.concat([df_predict_data2, df_predict_data1], ignore_index=True)
        print(df_new)

        df_new.to_csv(log_path, index=False)
        print(f'Design testability = {df_new["Testability"].mean()}')
        return df_new["Testability"].mean()

# API
def main(project_db_path, initial_value=1.0, verbose=False, log_path=None):
    """

    testability_prediction module API

    """

    df = PreProcess().compute_metrics_by_class_list(
        project_db_path,
        n_jobs=0  # n_job must be set to number of CPU cores, use zero for non-parallel computing of metrics
    )
    testability_ = TestabilityModel().inference(df_predict_data=df, verbose=verbose, log_path=log_path)
    # print('testability=', testability_)
    return round(testability_ / initial_value, 5)


# Test module
if __name__ == '__main__':
    db_path_ = r'E:/LSSDS/CodART/Experimental1/udbs/jvlt-1.3.2.und'  # This path should be replaced for each project
    project_name_ = 'jvlt-1.3.2'

    log_path_ = os.path.join(os.path.dirname(__file__), project_name_ + '_testability_s2.csv')

    print('mean testability2 normalize by 1\t', main(db_path_, initial_value=1.0,
                                                     verbose=False,
                                                     log_path=log_path_))
