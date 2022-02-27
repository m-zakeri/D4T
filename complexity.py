from class_diagram import ClassDiagram
from config import test_class_diagram

import networkx as nx

class Complexity():
    def __init__(self, CDG):
        self.CDG = CDG

        # calculate hierarchical inheritance complexity
        self.inheritance_complexity_dic = {}
        candidate_nodes = self.__find_inheritance_candidates()
        for node in candidate_nodes:
            self.inheritance_complexity_dic[node] = self.__calculate_inheritance_complexity(node)

    def calculate_interaction_complexity(self, source, target):
        complexity = 1
        has_path = False
        for path in nx.all_simple_paths(self.CDG, source=source, target=target):
            has_path = True
            complexity *= self.__calculate_path_complexity(path)
        if not has_path:
            complexity = -1
        return complexity

    def __calculate_path_complexity(self, path):
        complexity = 1
        for i in range(len(path) - 1):
            if self.CDG[path[i]][path[i+1]]['relation_type'] == 'use_def':
                if path[i] in self.inheritance_complexity_dic:
                    complexity *= self.inheritance_complexity_dic[path[i]]
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

    def get_matrix(self):
        node_list = list(self.CDG.nodes)
        no_nodes = len(node_list)
        node_list.sort()

        matrix = []
        for s in range(no_nodes):
            matrix.append([])
            for d in range(no_nodes):
                if self.CDG.nodes[node_list[s]]['type'] == "normal" and self.CDG.nodes[node_list[d]]['type'] == "normal":
                    complexity = self.calculate_interaction_complexity(node_list[s], node_list[d])
                    matrix[s].append(complexity)
                else:
                    matrix[s].append(None)
        return matrix


import config
from utils import File

if __name__ == "__main__":
    #cd = ClassDiagram()
    #cd.class_diagram_graph = test_class_diagram
    #cd.show(cd.class_diagram_graph)

    #test_CDG = cd.get_CFG()
    java_project_address = config.projects_info['commons-codec']['path']
    base_dirs = config.projects_info['commons-codec']['base_dirs']
    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd = ClassDiagram()
    cd.make_class_diagram(java_project_address, base_dirs, index_dic)

    cd.show(cd.class_diagram_graph)

    #cd.load('class_diagram.gml')
    cd.set_stereotypes(java_project_address, base_dirs, index_dic)
    cd.save('class_diagram.gml')
    #cd.show(cd.class_diagram_graph)

    CDG = cd.get_CFG()
    cd.show(CDG)

    c = Complexity(CDG)
    #c.calculate_interaction_complexity(8,7)
    matrix = c.get_matrix()
    for i in matrix:
        print(i)

