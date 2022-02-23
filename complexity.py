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
        print(source, target)
        for path in nx.all_simple_paths(self.CDG, source=source, target=target):
            complexity *= self.__calculate_path_complexity(path)
            print('\t' ,path, self.__calculate_path_complexity(path))
        print('\t' ,complexity)
        print('-----------------------')
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



if __name__ == "__main__":
    cd = ClassDiagram()
    cd.class_diagram_graph = test_class_diagram
    #cd.show(cd.class_diagram_graph)

    test_CDG = cd.get_CFG()

    c = Complexity(test_CDG)
    c.calculate_interaction_complexity(8,7)

