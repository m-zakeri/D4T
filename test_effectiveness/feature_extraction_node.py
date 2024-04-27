"""
The module extract features for each edge in MDG
to be used in the edge-level learning strategy.

"""

__version__ = '0.1.3'
__author__ = 'Morteza Zakeri'

import sys
import os
import re
import pandas
import pandas as pd
import networkx as nx
import networkx.algorithms.community as nx_comm

import understand

from feature_extraction import ModularDependencyGraph


class ModularDependencyGraphNodeFeature(ModularDependencyGraph):
    def __init__(self, graph_path, **kwargs):
        super(ModularDependencyGraphNodeFeature, self).__init__(graph_path, **kwargs)
        self.db: understand.Db = kwargs['db']
        self.project_name = kwargs['project_name']
        self.df_test = kwargs['df_test']

    def extract_node_statistics(self, ):
        G = self.mdg_graph
        # G = nx.subgraph(self.mdg_graph, max(nx.weakly_connected_components(self.mdg_graph), key=len))
        df = pd.DataFrame()
        if self.mdg_graph is None:
            return

        print('Computing graph attributes for all nodes in the graph components ...')
        average_neighbor_degree_dict = nx.average_neighbor_degree(G)
        # Category: Node centrality features
        degree_centrality_dict = nx.degree_centrality(G)
        in_degree_centrality_dict = nx.in_degree_centrality(G)
        out_degree_centrality_dict = nx.out_degree_centrality(G)
        closeness_centrality_dict = nx.closeness_centrality(G)
        betweenness_centrality_dict = nx.betweenness_centrality(G)
        katz_centrality_dict = nx.katz_centrality(G)
        eigenvector_centrality_numpy_dict = nx.eigenvector_centrality_numpy(G)
        harmonic_centrality_dict = nx.harmonic_centrality(G)

        current_flow_closeness_centrality_dict = dict()
        current_flow_betweenness_centrality_dict = dict()
        for node_set in nx.weakly_connected_components(self.mdg_graph):
            CCG = nx.subgraph(self.mdg_graph, node_set)
            current_flow_closeness_centrality_dict.update(nx.current_flow_closeness_centrality(nx.Graph(CCG)))
            # time-consuming
            current_flow_betweenness_centrality_dict.update(nx.current_flow_betweenness_centrality(nx.Graph(CCG)))

        pagerank_dict = nx.pagerank(G)

        print('Computing feature vector for each node (class) ...')
        for i, u in enumerate(self.mdg_graph.nodes()):
            df_temp = pd.DataFrame()
            df_temp['Class'] = [u]

            entities = self.db.lookup(re.compile(u + r'$'), )
            if entities is None or len(entities) == 0:  # Nested classes
                continue
            if str(entities[0].kind().name()).find('Enum') != -1:
                continue
            if str(entities[0].kind().name()).find('Unknown') != -1:
                continue
            if str(entities[0].kind().name()).find('Unresolved') != -1:
                continue

            df_x = self.df_test[self.df_test['Class'].isin([u])]

            if len(df_x) > 0:
                node_testability = df_x['Coverageability1'].values / 100
                df_temp['AbstractOrInterface'] = [0]
            elif ('Abstract' in entities[0].kind().name()) or ('Interface' in entities[0].kind().name()):
                df_temp['AbstractOrInterface'] = [1]
                node_testability = 1
            else:
                continue

            # Features for source class, u (15)
            df_temp['InDegree'] = [self.mdg_graph.in_degree(u)]
            df_temp['OutDegree'] = [self.mdg_graph.out_degree(u)]
            df_temp['AverageNeighborDegree'] = [average_neighbor_degree_dict[u]]
            # Category: Node centrality features
            df_temp['DegreeCentrality'] = [degree_centrality_dict[u]]
            df_temp['InDegreeCentrality'] = [in_degree_centrality_dict[u]]
            df_temp['OutDegreeCentrality'] = [out_degree_centrality_dict[u]]
            df_temp['ClosenessCentrality'] = [closeness_centrality_dict[u]]
            df_temp['BetweennessCentrality'] = [betweenness_centrality_dict[u]]
            df_temp['KatzCentrality'] = [katz_centrality_dict[u]]
            df_temp['EigenvectorCentrality'] = [eigenvector_centrality_numpy_dict[u]]
            df_temp['HarmonicCentrality'] = [harmonic_centrality_dict[u]]
            df_temp['CurrentFlowClosenessCentrality'] = [current_flow_closeness_centrality_dict[u]]
            df_temp['CurrentFlowBetweennessCentrality'] = [current_flow_betweenness_centrality_dict[u]]

            df_temp['PageRank'] = [pagerank_dict[u]]
            avg_shortest_path = nx.single_source_dijkstra_path_length(G=G, source=u).values()
            df_temp['AverageDijkstraPathLength'] = [sum(avg_shortest_path) / len(avg_shortest_path)]
            df_temp['NodeTestability'] = node_testability

            df = pd.concat([df, df_temp], ignore_index=True)
            # print(df.values)

        print(df)
        return df


def merge_dataset_node():
    csvs_path = 'dataset_nodes/'
    files = [f for f in os.listdir(csvs_path) if os.path.isfile(os.path.join(csvs_path, f))]
    df_all = pd.DataFrame()
    for f in files:
        print(f'Processing csv file {f}:')
        df1 = pd.read_csv(os.path.join(csvs_path, f))
        if not df1.empty:
            df_all = pd.concat([df_all, df1], ignore_index=True)

    results_path = 'dataset_merged/sf110_production_nodes.csv'
    print(df_all.shape)
    df_all.to_csv(results_path, index=False)


def create_dataset_for_each_project():
    udbs_path = 'D:/AnacondaProjects/iust_start/testability/sf110_without_test/'
    mdg_path = '../testability/mdg_production_code/'
    test_path = 'D:/AnacondaProjects/iust_start/testability/dataset06/DS06010Z.csv'

    udb_files = [f for f in os.listdir(udbs_path) if os.path.isdir(os.path.join(udbs_path, f)) and f[-4:] == '.und']
    mdg_files = [f for f in os.listdir(mdg_path) if os.path.isfile(os.path.join(mdg_path, f))]
    df_test = pd.read_csv(test_path)

    for mdg_file in mdg_files:
        project_name = mdg_file[:-8]
        print(f'Processing understand db file {project_name}:')
        # if os.path.exists(f'dataset_nodes/{project_name}_Node.csv'):
        #     print('Already exist.')
        #     continue
        db = understand.open(os.path.join(udbs_path, f"{project_name}.und"))
        mdg_ = ModularDependencyGraphNodeFeature(
            graph_path=os.path.join(mdg_path, mdg_file),
            db=db,
            project_name=project_name,
            df_test=df_test,
        )

        mdg_.draw_mdg()
        continue
        df = mdg_.extract_node_statistics()
        db.close()
        if df is not None:
            df.to_csv(f'dataset_nodes/{project_name}_Node.csv', index=False)
            # quit()
        print('-' * 50)


def draw_an_mdg():
    project_name = '10_water-simulator'
    udbs_path = 'D:/AnacondaProjects/iust_start/testability/sf110_without_test/'
    mdg_path = '../testability/mdg_production_code/'
    mdg_file = '10_water-simulator_MDG.csv'

    db = understand.open(os.path.join(udbs_path, f"{project_name}.und"))
    mdg_ = ModularDependencyGraphNodeFeature(
        graph_path=os.path.join(mdg_path, mdg_file),
        db=db,
        project_name=project_name,
        df_test=None
    )
    mdg_.draw_mdg()


if __name__ == '__main__':
    create_dataset_for_each_project()
    # merge_dataset_node()
    # draw_an_mdg()
