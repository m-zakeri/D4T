from class_diagram import ClassDiagram
from config import test_class_diagram

import networkx as nx

class Complexity():
    def __init__(self, CDG):
        self.CDG = CDG
        self.inheritance_complexity_dic = {}

    def calculate_complexity(self):
        inheritance_complexity_dic = {}
        candidate_nodes = self.__find_inheritance_candidates()
        for node in candidate_nodes:
            inheritance_complexity_dic[node] = self.__calculate_inheritance_complexity(node)
        print(inheritance_complexity_dic)

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

