"""
The module extract feature form program MDG by graph-embedding techniques.
The extracted feature are then used to predict design test effectiveness.

"""

__version__ = '0.1.1'
__author__ = 'Morteza Zakeri'

import sys
import os
import pandas
import pandas as pd
import networkx as nx
import networkx.algorithms.community as nx_comm


class ModularDependencyGraph:
    def __init__(self, graph_path, **kwargs):
        self.mdg_df = pandas.read_csv(graph_path)
        self.mdg_df.rename(columns={"From Entities": "From_Entities", "To Entities": "To_Entities"}, inplace=True)
        if self.mdg_df is not None and not self.mdg_df.empty:
            self.mdg_graph = nx.from_pandas_edgelist(
                self.mdg_df, source='From Class', target='To Class',
                edge_attr=True, create_using=nx.DiGraph()
            )
        else:
            self.mdg_graph = None

    def remove_test_classes_from_mdg(self, graph_path):
        with open(graph_path, mode='r') as f:
            lines = f.readlines()
        with open(graph_path, mode='w') as f:
            f.write(lines[0])
            for line in lines[1:]:
                if line.find('EvoSuiteTest') == -1:
                    f.write(line)

    def create_evosuite_classes_mdg(self, graph_path):
        with open(graph_path, mode='r') as f:
            lines = f.readlines()
        with open(graph_path, mode='w') as f:
            f.write(lines[0])
            for line in lines[1:]:
                if line.find('EvoSuiteTest') != -1:
                    line_parts = line.split(',')
                    # print(line_parts)
                    if line_parts[0][:-13] != line_parts[1][:-1]:
                        x = line.replace('EvoSuiteTest', '')
                        f.write(x)

    def compute_design_coverage(self, test_graph_path):
        test_mdg_df = pandas.read_csv(test_graph_path)
        if test_mdg_df is not None and not test_mdg_df.empty:
            test_mdg_graph = nx.from_pandas_edgelist(test_mdg_df, source='From Class', target='To Class',
                                                     edge_attr=True, create_using=nx.DiGraph())
        else:
            test_mdg_graph = None

        if test_mdg_graph is None:
            return 0, 0
        else:
            node_coverage = nx.number_of_nodes(test_mdg_graph) / nx.number_of_nodes(self.mdg_graph)
            edge_coverage = nx.number_of_edges(test_mdg_graph) / nx.number_of_edges(self.mdg_graph)
            return 1.0 if node_coverage > 1 else node_coverage, 1.0 if edge_coverage > 1 else edge_coverage

    def extract_components(self, project_name):
        # x = nx.number_connected_components(self.mdg_graph)  # not implemented for directed type
        x = 1
        y = nx.number_strongly_connected_components(self.mdg_graph)
        z = nx.number_weakly_connected_components(self.mdg_graph)
        for wcc in nx.weakly_connected_components(self.mdg_graph):
            print(len(wcc), ':', nx.subgraph(self.mdg_graph, wcc))
        print(project_name, y, z)
        print('-' * 50)
        return z

    def extract_statistics(self, project_name):
        # G = self.mdg_graph
        G = nx.subgraph(self.mdg_graph, max(nx.weakly_connected_components(self.mdg_graph), key=len))

        df = pd.DataFrame()
        df['Project'] = [project_name[:-9]]
        # visualize_graph(graph=G)
        # nx.algorithms.community.asyn_lpa_communities
        df['number_of_nodes'] = [nx.number_of_nodes(G)]
        df['number_of_edges'] = [nx.number_of_edges(G)]
        df['number_of_selfloops'] = [nx.number_of_selfloops(G)]
        df['average_degree'] = [sum([i[1] for i in nx.degree(G)]) / float(len(G))]

        df['average_neighbor_degree'] = [sum(nx.average_neighbor_degree(G).values()) / len(G)]
        df['average_degree_connectivity'] = [sum(nx.average_degree_connectivity(G).values()) / len(G)]
        # df['average_node_connectivity'] = [nx.average_node_connectivity(G)]  # time-consuming
        df['density'] = [nx.density(G)]

        # Just for directed graphs
        # print('condensation', nx.condensation(G))

        # Clustering coefficient 1 (Transitivity)
        df['transitivity'] = [nx.transitivity(G)]
        # Clustering coefficient 2 (CC)
        df['clustering_coefficient_2'] = [nx.average_clustering(G)]

        # print('degree_assortativity_coefficient', nx.degree_assortativity_coefficient(G))
        df['degree_pearson_correlation_coefficient'] = [nx.degree_pearson_correlation_coefficient(G)]

        # Category: Node centrality features
        df['degree_centrality'] = [sum(nx.degree_centrality(G).values()) / len(G)]
        # print('degree_centrality', sorted(nx.degree_centrality(G).items(), key=lambda kv: (kv[1], kv[0]))[:10])
        df['in_degree_centrality'] = [sum(nx.in_degree_centrality(G).values()) / len(G)]
        df['out_degree_centrality'] = [sum(nx.out_degree_centrality(G).values()) / len(G)]

        df['closeness_centrality'] = [sum(nx.closeness_centrality(G).values()) / len(G)]
        df['betweenness_centrality'] = [sum(nx.betweenness_centrality(G).values()) / len(G)]
        df['katz_centrality'] = [sum(nx.katz_centrality(G).values()) / len(G)]
        df['eigenvector_centrality'] = [sum(nx.eigenvector_centrality_numpy(G).values()) / len(G)]
        df['harmonic_centrality'] = [sum(nx.harmonic_centrality(G).values()) / len(G)]
        # df['percolation_centrality'] = sum(nx.percolation_centrality(G, attribute=None).values()) / len(G)
        df['current_flow_closeness_centrality'] = [
            sum(nx.current_flow_closeness_centrality(nx.Graph(G)).values()) / len(G)]
        df['current_flow_betweenness_centrality'] = [
            sum(nx.current_flow_betweenness_centrality(nx.Graph(G)).values()) / len(G)]  # time-consuming

        # Category: Edge centrality features
        df['edge_betweenness_centrality'] = [
            sum(nx.edge_betweenness_centrality(nx.Graph(G)).values()) / nx.number_of_edges(G)]
        df['edge_current_flow_betweenness_centrality'] = [sum(
            nx.edge_current_flow_betweenness_centrality(nx.Graph(G)).values()) / nx.number_of_edges(G)]

        df['page_rank'] = [sum(nx.pagerank(G).values()) / len(G)]

        if not nx.is_directed(G):
            print('is_connected', nx.is_connected(G))
            print('number_connected_components', nx.number_connected_components(G))
            if nx.is_connected(G):
                print('diameter', nx.diameter(G))
                print('average_shortest_path_length', nx.average_shortest_path_length(G))
            else:
                # ccs_asp_list = []
                # ccs_d_list = []
                # print('number_connected_components', nx.number_connected_components(G))
                # for g in nx.connected_component_subgraphs(G):
                #     ccs_d_list.append(nx.average_shortest_path_length(g))
                #     ccs_asp_list.append(nx.average_shortest_path_length(g))

                Gc = max(nx.connected_component_subgraphs(G), key=len)

                print('***wait*** ...')
                print('max_cc_asp', nx.average_shortest_path_length(Gc))
                print('max_cc_diameter', nx.diameter(Gc))

        print(df.shape)
        print(df)
        return df


def concat_dataset_files():
    ds_path = r'dataset/'
    files = [f for f in os.listdir(ds_path) if os.path.isfile(os.path.join(ds_path, f))]
    df1 = pd.DataFrame()
    for f in files:
        df = pd.read_csv(ds_path + f)
        df1 = pd.concat([df1, df], ignore_index=True)

    df2 = pd.read_excel(r'data/evosuite160_with_modularity_and_coverageability2.xlsx', 'Sheet1')
    df_result = df1.merge(df2, how='inner', on='Project')
    print(df_result)
    df_result.to_csv('d4t_ds_sf110_01.csv', index=False, )


def concat_modularity_with_d4t_dataset():
    df1 = pd.read_csv('d4t_ds_sf110_01.csv')
    df2 = pd.read_csv(r'data/data_modularity_QualCode_Understand3.csv')
    df_result = df1.merge(df2, how='inner', on='Project')
    df_result.to_csv('d4t_ds_sf110_02.csv', index=False, )


def create_test_graph():
    mdg_path = 'mdgs_only_test_classes2/'
    files = [f for f in os.listdir(mdg_path) if os.path.isfile(os.path.join(mdg_path, f))]
    for f in files:
        print(f'processing understand db file {f}:')
        mdg = ModularDependencyGraph(mdg_path + f, )
        mdg.create_evosuite_classes_mdg(graph_path=mdg_path + f)


def compute_design_test_effectiveness():
    df = pd.read_csv(r'd4t_ds_sf110_02.csv')
    nc_list = []
    ec_list = []
    avg_list = []
    for project_ in df['Project']:
        print(f'processing understand db file {project_}:')
        mdg_production = ModularDependencyGraph(f'mdgs/{project_}_UMDG.csv')
        nc, ec = mdg_production.compute_design_coverage(test_graph_path=f'mdgs_only_test_classes/{project_}_UMDG.csv')
        nc_list.append(nc)
        ec_list.append(ec)
        avg_list.append((nc + ec) / 2.)

    df['DesignNodeCoverage'] = nc_list
    df['DesignEdgeCoverage'] = ec_list
    df['DesignMeanCoverage'] = avg_list

    df.to_csv(r'd4t_ds_sf110_03.csv', index=False)


def extract_design_metrics():
    mdg_path = 'mdgs/'
    files = [f for f in os.listdir(mdg_path) if os.path.isfile(os.path.join(mdg_path, f))]
    z = 0
    for f in files:
        print(f'processing understand db file {f}:')
        mdg = ModularDependencyGraph(mdg_path + f, )
        # df1 = mdg.extract_statistics(project_name=f)
        # df1.to_csv(f'dataset/{f}', index=False)
        z += mdg.extract_components(project_name=f)
        # quit()

    print('z', z)


if __name__ == '__main__':
    path_ = 'mdgs'
    # concat_modularity_wtih_d4t_dataset()
    extract_design_metrics()
    # create_test_graph()
    # compute_design_test_effectiveness()
    # quit()
