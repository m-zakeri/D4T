"""
The module extract features for each edge in MDG
to be used in the edge-level learning strategy.

"""

__version__ = '0.1.2'
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


class ModularDependencyGraphEdgeFeature(ModularDependencyGraph):
    def __init__(self, graph_path, test_graph_path, **kwargs):
        super(ModularDependencyGraphEdgeFeature, self).__init__(graph_path, **kwargs)
        self.test_mdg_df = pandas.read_csv(test_graph_path)
        self.test_mdg_df.rename(columns={"From Entities": "From_Entities", "To Entities": "To_Entities"}, inplace=True)
        if self.test_mdg_df is not None and not self.test_mdg_df.empty:
            self.test_mdg_graph: nx.DiGraph() = nx.from_pandas_edgelist(self.test_mdg_df, source='From Class',
                                                                        target='To Class',
                                                                        edge_attr=True, create_using=nx.DiGraph())
        else:
            self.test_mdg_graph: nx.DiGraph() = None

        self.db: understand.Db = kwargs['db']

    def extract_edges_statistics(self, ):
        G = self.mdg_graph
        # G = nx.subgraph(self.mdg_graph, max(nx.weakly_connected_components(self.mdg_graph), key=len))
        df = pd.DataFrame()
        if self.mdg_graph is None:
            return
        if self.test_mdg_graph is None:
            return

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
        print('Finish computing graph attributes.')

        for i, u in enumerate(self.mdg_graph.nodes()):
            entities = self.db.lookup(re.compile(u + r'$'), )
            if entities is None or len(entities) == 0:  # Nested classes
                continue
            if str(entities[0].kind().name()).find('Abstract') != -1:
                continue
            if str(entities[0].kind().name()).find('Interface') != -1:
                continue
            if str(entities[0].kind().name()).find('Enum') != -1:
                continue
            if str(entities[0].kind().name()).find('Unknown') != -1:
                continue
            if str(entities[0].kind().name()).find('Unresolved') != -1:
                continue

            for j, v in enumerate(self.mdg_graph.nodes()):
                if v == u:
                    continue
                if not nx.has_path(self.mdg_graph, u, v):
                    continue
                entities = self.db.lookup(re.compile(v + r'$'), )
                if entities is None or len(entities) == 0:  # Nested classes
                    continue
                if str(entities[0].kind().name()).find('Abstract') != -1:
                    continue
                if str(entities[0].kind().name()).find('Interface') != -1:
                    continue
                if str(entities[0].kind().name()).find('Enum') != -1:
                    continue
                if str(entities[0].kind().name()).find('Unknown') != -1:
                    continue
                if str(entities[0].kind().name()).find('Unresolved') != -1:
                    continue

                df_temp = pd.DataFrame()
                df_temp['FromClass'] = [u]
                df_temp['ToClass'] = [v]

                # Features for source class, u (14)
                df_temp['FromClassInDegree'] = [self.mdg_graph.in_degree(u)]
                df_temp['FromClassOutDegree'] = [self.mdg_graph.out_degree(u)]
                df_temp['FromClassAverageNeighborDegree'] = [average_neighbor_degree_dict[u]]
                # Category: Node centrality features
                df_temp['FromClassDegreeCentrality'] = [degree_centrality_dict[u]]
                df_temp['FromClassInDegreeCentrality'] = [in_degree_centrality_dict[u]]
                df_temp['FromClassOutDegreeCentrality'] = [out_degree_centrality_dict[u]]
                df_temp['FromClassClosenessCentrality'] = [closeness_centrality_dict[u]]
                df_temp['FromClassBetweennessCentrality'] = [betweenness_centrality_dict[u]]
                df_temp['FromClassKatzCentrality'] = [katz_centrality_dict[u]]
                df_temp['FromClassEigenvectorCentrality'] = [eigenvector_centrality_numpy_dict[u]]
                df_temp['FromClassHarmonicCentrality'] = [harmonic_centrality_dict[u]]
                df_temp['FromClassCurrentFlowClosenessCentrality'] = [current_flow_closeness_centrality_dict[u]]
                df_temp['FromClassCurrentFlowBetweennessCentrality'] = [current_flow_betweenness_centrality_dict[u]]
                df_temp['FromClassPageRank'] = [pagerank_dict[u]]

                # Features for destination class, v (14)
                df_temp['ToClassInDegree'] = [self.mdg_graph.in_degree(v)]
                df_temp['ToClassOutDegree'] = [self.mdg_graph.out_degree(v)]
                df_temp['ToClassAverageNeighborDegree'] = [average_neighbor_degree_dict[v]]
                # Category: Node centrality features
                df_temp['ToClassDegreeCentrality'] = [degree_centrality_dict[v]]
                df_temp['ToClassInDegreeCentrality'] = [in_degree_centrality_dict[v]]
                df_temp['ToClassOutDegreeCentrality'] = [out_degree_centrality_dict[v]]
                df_temp['ToClassClosenessCentrality'] = [closeness_centrality_dict[v]]
                df_temp['ToClassBetweennessCentrality'] = [betweenness_centrality_dict[v]]
                df_temp['ToClassKatzCentrality'] = [katz_centrality_dict[v]]
                df_temp['ToClassEigenvectorCentrality'] = [eigenvector_centrality_numpy_dict[v]]
                df_temp['ToClassHarmonicCentrality'] = [harmonic_centrality_dict[v]]
                df_temp['ToClassCurrentFlowClosenessCentrality'] = [current_flow_closeness_centrality_dict[v]]
                df_temp['ToClassCurrentFlowBetweennessCentrality'] = [current_flow_betweenness_centrality_dict[v]]
                df_temp['ToClassPageRank'] = [pagerank_dict[v]]

                # Features for edge
                if self.mdg_graph.has_edge(u, v):
                    df_temp['References'] = [self.mdg_graph[u][v]['References']]
                else:
                    df_temp['References'] = [0]

                # Labels for each possible connection in production mdg
                if not self.test_mdg_graph.has_node(u) or not self.test_mdg_graph.has_node(v):
                    df_temp['IsCovered'] = [-1]
                elif self.test_mdg_graph.has_edge(u, v):
                    df_temp['IsCovered'] = [2]
                elif nx.has_path(self.test_mdg_graph, u, v):
                    df_temp['IsCovered'] = [1]
                else:
                    df_temp['IsCovered'] = [0]

                df = pd.concat([df, df_temp], ignore_index=True)
                # print(df.values)
            nns = nx.number_of_nodes(self.mdg_graph)
            print(f"\rProgress {i}/{nns} ({round(i / nns, 4) * 100}%)", end="")

        print(df)
        return df

    def traverse_edges(self):
        count = 0
        for u, v, d_ in self.mdg_graph.edges.data():
            print(u, v, d_)
            entities = self.db.lookup(re.compile(u + r'$'), )
            print('u', len(entities))
            print('uname', [(e.longname(), e.kind().name()) for e in entities])
            # print(self.test_mdg_graph.has_edge(u, v))
            if self.test_mdg_graph.has_edge(u, v):
                # print(self.test_mdg_graph[u][v]["References"])
                count += 1
        print(nx.number_of_edges(self.mdg_graph), nx.number_of_edges(self.test_mdg_graph), count)


def create_edge_level_dataset():
    benchmark_path = r'E:/LSSDS/QualCode1/SF110-20130704-src/'
    df = pd.read_csv(r'dataset_merged/d4t_ds_sf110_03.csv')
    for project_ in df['Project'][:]:
        print(f'Processing understand db file {project_}:')
        if os.path.exists(f'dataset_edges/{project_}_EDGE.csv'):
            print('Already exist.')
            continue
        db = understand.open(f"{benchmark_path}{project_}.und")
        mdg_ = ModularDependencyGraphEdgeFeature(
            graph_path=f'mdgs/{project_}_UMDG.csv',
            test_graph_path=f'mdgs_only_test_classes2/{project_}_UMDG.csv',
            db=db
        )
        df = mdg_.extract_edges_statistics()
        db.close()
        if df is not None:
            df.to_csv(f'dataset_edges/{project_}_EDGE.csv', index=False)
            # quit()


def merge_dataset_edge():
    csvs_path = 'dataset_edges/'
    files = [f for f in os.listdir(csvs_path) if os.path.isfile(os.path.join(csvs_path, f))]
    df_all = pd.DataFrame()
    for f in files:
        print(f'Processing csv file {f}:')
        try:
            df1 = pd.read_csv(csvs_path + f)
        except:
            continue
        if not df1.empty:
            df_all = df_all.append(df1, ignore_index=True)

    results_path = 'dataset_merged/sf110_edges.csv'

    df_all.to_csv(results_path, index=False)


def preprocess():
    input_path = 'dataset_merged/sf110_edges.csv'
    df = pd.read_csv(input_path)
    print(df.shape)
    df = df[df['IsCovered'] != -1]

    df.loc[df['IsCovered'] == 2, 'IsCovered'] = 1
    print(df)
    print(df[df['IsCovered'] == 1].shape[0])
    print(df[df['IsCovered'] == 0].shape[0])
    result_path = 'dataset_merged/sf110_edges_binary.csv'
    df.to_csv(result_path, index=False)


if __name__ == '__main__':
    # create_edge_level_dataset()
    # merge_dataset_edge()
    preprocess()