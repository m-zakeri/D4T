"""
This module contains modularity measurement script
based on Scitools Understand.

The implementation is the same in modularity_rating module
The only difference is that the MDG is created with und (Understand command line tool)

## Reference
[1] https://scitools.com/
"""

__version__ = '0.1.1'
__author__ = 'Morteza Zakeri'

import sys
import os
import pandas
import pandas as pd
import networkx as nx
import networkx.algorithms.community as nx_comm

import understand
from matplotlib import pyplot as plt


class Modularity:
    def __init__(self, graph_path, db, **kwargs):
        try:
            self.mdg_df = pandas.read_csv(graph_path)
        except:
            self.mdg_df = None
        if self.mdg_df is not None and not self.mdg_df.empty:
            self.mdg_graph = nx.from_pandas_edgelist(self.mdg_df, source='From Class', target='To Class',
                                                     edge_attr='References', create_using=nx.DiGraph())
        else:
            self.mdg_graph = None
        self.db = db

        # self.show_mdg()

    def show_mdg(self):
        pos = nx.spring_layout(self.mdg_graph)
        nx.draw(self.mdg_graph, pos=pos, with_labels=True, )
        edge_labels = nx.get_edge_attributes(self.mdg_graph, 'References')
        nx.draw_networkx_edge_labels(self.mdg_graph, pos=pos, edge_labels=edge_labels, font_color='red')
        plt.show()

    def compute_modularity_newman_leicht(self, ):
        """
        https://networkx.org/documentation/stable/reference/algorithms/generated/networkx.algorithms.community.quality.modularity.html

        ## Example:
            communities = [['p1.C1', 'p1.C2', 'p1.C3'], ['p2.C4', 'p2.C5']]
            G = nx.barbell_graph(3, 0)

        :return:
        """
        q = 0
        if self.mdg_graph is not None and self.mdg_graph.number_of_edges() > 0:
            communities = self.__roster_communities()
            q = nx_comm.modularity(self.mdg_graph, communities=communities.values())
        # print(q)
        return q

    def __roster_communities(self):
        """
        # Example:
        communities = self.df_class.groupby(['Parent'])['LongName'].apply(list)
        print(list(communities))
        :return:
        """
        communities_dict = dict()
        for node_ in self.mdg_graph:
            # print('node_:', node_)
            # package_name = self.__get_package_name_by_parsing(node_)
            package_name = self.__get_package_name_by_understand_query(node_)
            if package_name in communities_dict.keys():
                communities_dict[package_name].append(node_)
            else:
                communities_dict[package_name] = [node_]
        # print(communities_dict.values())
        return communities_dict

    def __get_package_name_by_parsing(self, class_longname: str = None):
        if class_longname.find('.') == -1:
            return 'default'
        else:
            package_name, class_short_name = class_longname.rsplit('.', 1)
            return package_name

    def __get_package_name_by_understand_query(self, class_longname: str = None):
        """
        This method can be used instead of `__get_package_name_by_parsing` and it is more accurate

        """
        package_entity, package_longname = self.get_package_of_given_class(self.db, class_longname)
        # print(package_entity.longname())

        return package_longname

    def get_package_of_given_class(self, db, class_name):
        class_entity = self.get_class_entity_by_name(db, class_name)
        # print(class_entity.parent())
        # print('class_name', class_entity.longname())
        # print('class_name', class_name)

        package_list = class_entity.ents('Containin', 'Java Package')
        while not package_list and class_entity.parent() is not None:
            package_list = class_entity.parent().ents('Containin', 'Java Package')
            class_entity = class_entity.parent()
        # print('package_name', package_list)
        if len(package_list) < 1:
            return None, '<root_package>'
        else:
            return package_list[0], package_list[0].longname()

    def get_class_entity_by_name(self, db, class_name):
        # https://docs.python.org/3/library/exceptions.html#exception-hierarchy
        # Find relevant 'class' entity
        entity_list = list()

        # entities = db.ents('Type')  ## Use this for evo-suite SF110 already measured class
        # entities = db.ents('Java Class ~Enum ~Unknown ~Unresolved ~Jar ~Library')
        entities = db.ents('Java Class ~Jar ~Library, Java Interface')
        if entities is not None:
            for entity_ in entities:
                if entity_.longname() == class_name:
                    entity_list.append(entity_)
                    # print('Class entity:', entity_)
                    # print('Class entity kind:', entity_.kind())
        if len(entity_list) == 0:
            # raise UserWarning('Java class with name {0} is not found in project'.format(class_name))
            return None
        if len(entity_list) > 1:
            # raise ValueError('There is more than one Java class with name {0} in the project'.format(class_name))
            return entity_list[0]
        else:
            return entity_list[0]


# Modularity API
def compute_modularity(root_path, project_name=None, make_db=False):
    """
    API for computing modularity with Understand
    """
    if make_db:
        print('Creating, adding and analyzing UNDs ...')
        # cmd_ = 'und convert -override {0}'.format(project_path)
        cmd_ = f'und create -db {root_path}{project_name}.und -languages java'
        os.system(f'cmd /c "{cmd_}"')
        cmd_ = f'und add "{root_path + project_name}" {root_path}{project_name}.und'
        os.system(f'cmd /c "{cmd_}"')
        cmd_ = f'und analyze -all  {root_path}{project_name}.und'
        os.system(f'cmd /c "{cmd_}"')

    print('Compute MDG from UND ...')
    cmd_ = 'und export -format long -dependencies class csv {0} {1}'.format(f'mdgs/{project_name}_UMDG.csv',
                                                                            root_path + project_name + '.und')
    os.system('cmd /c "{0}"'.format(cmd_))

    # db = understand.open(root_path + project_name + '.und')
    # modulo = Modularity(graph_path=f'mdgs/{project_name}_UMDG.csv', db=db)
    # q = modulo.compute_modularity_newman_leicht()
    q = 0
    print('Modularity of project "{0}": Q={1}'.format(project_name, q))
    return q


def make_benchmark():
    benchmark_path = r'E:/LSSDS/QualCode1/SF110-20130704-src/'
    df = pd.read_csv(r'data/data_modularity_QualCode_Understand3.csv')
    modularity_sf110_list = []
    for project_ in df['Project']:
        q = compute_modularity(root_path=benchmark_path, project_name=str(project_))
        # modularity_sf110_list.append(q)
    # df['ModularityRawValueUnderstand'] = modularity_sf110_list
    # df.to_csv(r'../evaluation/data_modularity_QualCode_Understand3.csv', index=False)


if __name__ == '__main__':
    make_benchmark()
