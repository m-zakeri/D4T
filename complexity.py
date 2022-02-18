from class_diagram import ClassDiagram
from config import test_class_diagram

import networkx as nx

class Complexity():
    def __init__(self, CDG):
        self.CDG = CDG
        self.inheritance_complexity_dic = {}

    def calculate_complexity(self):
        # calculate hierarchical inheritance complexity
        inheritance_complexity_dic = {}
        candidate_nodes = self.__find_inheritance_candidates()
        for node in candidate_nodes:
            inheritance_complexity_dic[node] = self.__calculate_inheritance_complexity(node)
        print(inheritance_complexity_dic)

        # calculate complexity between each pair of node
        complexity_matrix = []
        node_list = list(self.CDG.nodes)
        node_list.sort()
        print(node_list)
        no_nodes = len(node_list)
        for i_source in range(no_nodes):
            complexity_matrix.append([])
            for i_target in range(no_nodes):
                complexity = self.__calculate_interaction_complexity(node_list[i_source],
                                                                     node_list[i_target],
                                                                     inheritance_complexity_dic)
                complexity_matrix[i_source].append(complexity)
                print((node_list[i_source], node_list[i_target]), complexity)
                print('--------------')

        #for i in complexity_matrix:
        #    print(i)



    def __calculate_interaction_complexity(self, source, target, inheritance_complexity_dic):
        complexity = 1
        print(source, target)
        for path in nx.all_simple_paths(self.CDG, source=source, target=target):
            complexity *= self.__calculate_path_complexity(path, inheritance_complexity_dic)
            print('\t' ,path, complexity)
        return complexity

    def __calculate_path_complexity(self, path, inheritance_complexity_dic):
        complexity = 1
        for i in range(len(path) - 1):
            if self.CDG[path[i]][path[i+1]]['relation_type'] == 'use_def':
                if path[i] in inheritance_complexity_dic:
                    complexity *= inheritance_complexity_dic[path[i]]
        return complexity

    def __calculate_inheritance_complexity(self, node):
        complexity = 0
        stack = []
        stack.append(node)

        depth_dic = {node:1}
        while stack != []:
            current_node = stack.pop()
            is_leave = True
            for neighbor in self.CDG[current_node]:
                if self.CDG[current_node][neighbor]['relation_type'] == 'child' and self.CDG[neighbor][current_node]['relation_type'] == 'parent':
                    is_leave = False
                    stack.append(neighbor)
                    depth_dic[neighbor] = depth_dic[current_node] + 1

            if is_leave:
                complexity += depth_dic[current_node] * (depth_dic[current_node] - 1)
        return complexity

    def __find_inheritance_candidates(self):
        candidates = set()
        for edge in self.CDG.edges:
            if self.CDG.edges[edge]['relation_type'] == 'parent':
                candidates.add(edge[1])
        return candidates


if __name__ == "__main__":
    cd = ClassDiagram()
    cd.class_diagram_graph = test_class_diagram
    #cd.show(cd.class_diagram_graph)

    test_CDG = cd.get_CFG()

    c = Complexity(test_CDG)
    c.calculate_complexity()

