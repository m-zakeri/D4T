import json

import signal

from class_diagram import ClassDiagram
from config import test_class_diagram

import networkx as nx
import csv

# class TimeoutException(Exception):   # Custom exception class
#     pass
#
# def timeout_handler(signum, frame):   # Custom signal handler
#     raise TimeoutException
#
# signal.signal(signal.SIGALRM, timeout_handler)

class Complexity():
    def __init__(self, CDG):
        self.CDG = CDG

        # calculate hierarchical inheritance complexity
        self.inheritance_complexity_dic = {}
        candidate_nodes = self.__find_inheritance_candidates()
        for node in candidate_nodes:
            self.inheritance_complexity_dic[node] = self.__calculate_inheritance_complexity(node)

    def calculate_interaction_complexity(self, source, target):
        print("\t in calculate_interaction_complexity")
        complexity = 1
        has_path = False

        # for path in nx.all_simple_paths(self.CDG, source=source, target=target):
        #     print("*****path*****: ", path)
        #print(list(nx.all_simple_paths(self.CDG, source=source, target=target)))
        for path in nx.all_simple_paths(self.CDG, source=source, target=target):
            #print("\t calculate_interaction_complexity")
            print("\t path:", path)
            has_path = True
            print("calculate_interaction_complexity")
            complexity *= self.__calculate_path_complexity(path)
        if not has_path:
            complexity = None
        return complexity

    def __calculate_path_complexity(self, path):
        print("\t in __calculate_path_complexity")
        complexity = 1
        for i in range(len(path) - 1):
            print("__calculate_path_complexity")
            if self.CDG[path[i]][path[i+1]]['relation_type'] == 'use_def':
                if path[i] in self.inheritance_complexity_dic:
                    complexity *= self.inheritance_complexity_dic[path[i]]
                    print("\t __calculate_path_complexity")
        return complexity

    def __calculate_inheritance_complexity(self, node):
        print("\t in __calculate_inheritance_complexity")
        complexity = 0
        stack = []
        stack.append(node)

        depth_dic = {node:1}
        while stack != []:
            print("__calculate_inheritance_complexity")
            #print("\t __calculate_inheritance_complexity")
            current_node = stack.pop()
            is_leave = True
            for neighbor in self.CDG[current_node]:
                if (current_node in self.CDG[neighbor]):
                    if self.CDG[current_node][neighbor]['relation_type'] == 'child' and self.CDG[neighbor][current_node]['relation_type'] == 'parent':
                        is_leave = False
                        stack.append(neighbor)
                        depth_dic[neighbor] = depth_dic[current_node] + 1

            if is_leave:
                complexity += depth_dic[current_node] * (depth_dic[current_node] - 1)
        return complexity

    def __find_inheritance_candidates(self):
        print("\t in __find_inheritance_candidates")
        candidates = set()
        for edge in self.CDG.edges:
            if self.CDG.edges[edge]['relation_type'] == 'parent':
                candidates.add(edge[1])
                print("\t __find_inheritance_candidates")
        return candidates

    def get_matrix(self):
        node_list = list(self.CDG.nodes)
        no_nodes = len(node_list)
        node_list.sort()

        matrix = []
        for s in range(no_nodes):
            matrix.append([])
            for d in range(no_nodes):
                if self.CDG.nodes[node_list[s]]['type'] == "class" and self.CDG.nodes[node_list[d]]['type'] == "class":
                    # Start the timer. Once 5 seconds are over, a SIGALRM signal is sent.
                    signal.alarm(5)

                    try:
                        complexity = self.calculate_interaction_complexity(node_list[s], node_list[d])
                    except TimeoutException:
                        complexity = None
                        print("time out")
                    signal.alarm(0)

                    print("complexity:", node_list[s], node_list[d], complexity)
                    matrix[s].append(complexity)
                else:
                    matrix[s].append(None)
        return matrix

    @staticmethod
    def get_avg_of_matrix(matrix):
        n = 0
        s = 0
        for i in matrix:
            for j in i:
                if j is not None:
                    n += 1
                    s += j
        return s / n

    def save_csv(self, path):
        node_list = list(self.CDG.nodes)
        no_nodes = len(node_list)
        node_list.sort()
        header = ['src', 'dest', 'complexity']

        with open(path, 'w', encoding='UTF8') as f:
            writer = csv.writer(f)

            # write the header
            no_row = 0
            writer.writerow(header)
            for s in range(no_nodes):
                for d in range(no_nodes):
                    print(s, d)
                    if self.CDG.nodes[node_list[s]]['type'] == "class" and self.CDG.nodes[node_list[d]][
                        'type'] == "class":
                        complexity = self.calculate_interaction_complexity(str(node_list[s]), str(node_list[d]))
                        #if complexity != None:
                        #    writer.writerow([s, d, complexity])
                        print([s, d, complexity])
                        no_row += 1
                        print(no_row)

class Complexity2():
    def __init__(self, CDG, class_diagram):
        self.CDG = CDG
        self.class_diagram = class_diagram

        # calculate hierarchical inheritance complexity
        self.inheritance_complexity_dic = {}
        candidate_nodes = self.__find_inheritance_candidates()
        for node in candidate_nodes:
            self.inheritance_complexity_dic[node] = self.__calculate_inheritance_complexity(node)

    def calculate_interaction_complexity(self, source, target):
        print("\t in calculate_interaction_complexity")
        complexity = 1
        has_path = False

        # for path in nx.all_simple_paths(self.CDG, source=source, target=target):
        #     print("*****path*****: ", path)
        #print(list(nx.all_simple_paths(self.CDG, source=source, target=target)))
        for path in nx.all_simple_paths(self.class_diagram, source=source, target=target):
            #print("\t calculate_interaction_complexity")
            print("\t path:", path)
            has_path = True
            print("calculate_interaction_complexity")
            complexity *= self.__calculate_path_complexity(path)
        if not has_path:
            complexity = None
        return complexity

    def __calculate_path_complexity(self, path):
        print("\t in __calculate_path_complexity")
        complexity = 1
        for i in range(len(path) - 1):
            print("__calculate_path_complexity")
            if self.CDG[path[i]][path[i+1]]['relation_type'] == 'use_def':
                if path[i] in self.inheritance_complexity_dic:
                    complexity *= self.inheritance_complexity_dic[path[i]]
                    print("\t __calculate_path_complexity")
        return complexity

    def __calculate_inheritance_complexity(self, node):
        print("\t in __calculate_inheritance_complexity")
        complexity = 0
        stack = []
        stack.append(node)

        depth_dic = {node:1}
        while stack != []:
            print("__calculate_inheritance_complexity")
            #print("\t __calculate_inheritance_complexity")
            current_node = stack.pop()
            is_leave = True
            for neighbor in self.CDG[current_node]:
                if (current_node in self.CDG[neighbor]):
                    if self.CDG[current_node][neighbor]['relation_type'] == 'child' and self.CDG[neighbor][current_node]['relation_type'] == 'parent':
                        is_leave = False
                        stack.append(neighbor)
                        depth_dic[neighbor] = depth_dic[current_node] + 1

            if is_leave:
                complexity += depth_dic[current_node] * (depth_dic[current_node] - 1)
        return complexity

    def __find_inheritance_candidates(self):
        print("\t in __find_inheritance_candidates")
        candidates = set()
        for edge in self.CDG.edges:
            if self.CDG.edges[edge]['relation_type'] == 'parent':
                candidates.add(edge[1])
                print("\t __find_inheritance_candidates")
        return candidates

    def get_matrix(self):
        node_list = list(self.CDG.nodes)
        no_nodes = len(node_list)
        node_list.sort()

        matrix = []
        for s in range(no_nodes):
            matrix.append([])
            for d in range(no_nodes):
                if self.CDG.nodes[node_list[s]]['type'] == "class" and self.CDG.nodes[node_list[d]]['type'] == "class":

                    complexity = self.calculate_interaction_complexity(node_list[s], node_list[d])

                    print("complexity:", node_list[s], node_list[d], complexity)
                    matrix[s].append(complexity)
                else:
                    matrix[s].append(None)
        return matrix

    @staticmethod
    def get_avg_of_matrix(matrix):
        n = 0
        s = 0
        for i in matrix:
            for j in i:
                if j is not None:
                    n += 1
                    s += j
        return s / n

    def save_csv(self, path):
        node_list = list(self.CDG.nodes)
        no_nodes = len(node_list)
        node_list.sort()
        header = ['src', 'dest', 'complexity']

        with open(path, 'w', encoding='UTF8') as f:
            writer = csv.writer(f)

            # write the header
            no_row = 0
            writer.writerow(header)
            for s in range(no_nodes):
                for d in range(no_nodes):
                    print(s, d)
                    if self.CDG.nodes[node_list[s]]['type'] == "class" and self.CDG.nodes[node_list[d]][
                        'type'] == "class":
                        complexity = self.calculate_interaction_complexity(str(node_list[s]), str(node_list[d]))
                        #if complexity != None:
                        #    writer.writerow([s, d, complexity])
                        print([s, d, complexity])
                        no_row += 1
                        print(no_row)




import config
from utils import File

if __name__ == "__main__":
    # cd = ClassDiagram()
    # cd.class_diagram_graph = test_class_diagram
    # cd.show(cd.class_diagram_graph)
    #
    # test_CDG = cd.get_CFG()
    # c = Complexity(test_CDG)
    # matrix = c.get_matrix()
    # for i in matrix:
    #     print(i)


    java_project_address = config.projects_info['xerces2j-impl']['path']
    base_dirs = config.projects_info['xerces2j-impl']['base_dirs']
    # files = File.find_all_file(java_project_address, 'java')
    # index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)

    with open('class_index.json') as f:
        index_dic = json.load(f)

    cd = ClassDiagram(java_project_address, base_dirs, index_dic)
    # cd.make_class_diagram()
    # #cd.save('class_diagram.gml')
    # cd.show(cd.class_diagram_graph)
    #
    #
    # cd.set_stereotypes()
    # cd.save('class_diagram.gml')
    cd.load('class_diagram.gml')
    # cd.show(cd.class_diagram_graph)
    CDG = cd.get_CDG()
    c2 = Complexity2(CDG, cd.class_diagram_graph)
    print(c2.calculate_interaction_complexity("0", "148"))
    # CDG = cd.get_CDG()
    # cd.show(CDG)
    # c = Complexity(CDG)
    # print(c.find_all_paths("22", "0"))
    # c.calculate_interaction_complexity("22", "0")
    # matrix = c.get_matrix()
    # print(Complexity.get_avg_of_matrix(matrix))
    # for i in matrix:
    #    print(i)

    #print(c.calculate_interaction_complexity(2, 3))
    # cd.save(java_project_address + '\\' + 'class_diagram.gml')
    # cd.save_index(java_project_address + '\\' + 'index_dic.json')




    # java_project_address = config.projects_info['rhino-Rhino1_7_12_Release']['path']
    # base_dirs = config.projects_info['rhino-Rhino1_7_12_Release']['base_dirs']
    # with open(java_project_address + '\\' + 'index_dic.json') as f:
    #     index_dic = json.load(f)
    # cd = ClassDiagram(java_project_address, base_dirs, index_dic)
    # cd.load(java_project_address + '\\' + 'class_diagram.gml')
    # CDG = cd.get_CDG()
    # #cd.show(CDG)
    # c = Complexity(CDG)
    # #print((2, 0), c.calculate_interaction_complexity("2", "0"))
    # print(10, 110)
    # x = 0
    # print(len(cd.class_diagram_graph.edges))
    #
    # for path in nx.all_simple_paths(cd.class_diagram_graph, source="10", target="110"):
    #     x += 1
    #     print(x)



    # print((10, 110), c.calculate_interaction_complexity("10", "110"))
    # for i in range(10, 276):
    #     for j in range(276):
    #         print(i, j)
    #         complexity = c.calculate_interaction_complexity(str(i), str(j))
    #         #if complexity != None :
    #             #if complexity > 1:
    #         print((i, j), complexity)
    #c.save_csv(java_project_address + '\\' + 'complexity.csv')




