from class_diagram import ClassDiagram
from factory import Factory
from utils import File
import config
import networkx as nx
import json

if __name__ == "__main__":
    # java_projects = [
    #     '10_water-simulator',
    #     '61_noen',
    #     '88_jopenchart',
    #     'commons-codec',
    #     'xerces2j'
    # ]
    java_projects = ["javaproject"]
    for java_project in java_projects:
        java_project_address = config.projects_info[java_project]['path']
        base_dirs = config.projects_info[java_project]['base_dirs']
        files = File.find_all_file(java_project_address, 'java')
        index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        cd = ClassDiagram(java_project_address, base_dirs, files, index_dic)
        cd.make_class_diagram()
        #cd.save('class_diagram.gml')
        #cd.load('class_diagram.gml')
        cd.show(cd.class_diagram_graph)
        # g = cd.class_diagram_graph
        # print(len(list(nx.weakly_connected_components(g))))
        # for i in nx.weakly_connected_components(g):
        #     print(i)
        #g = cd.class_diagram_graph
        #print(len(list(nx.weakly_connected_components(g))))
        f = Factory()
        report = f.refactor(0.1, index_dic, cd.class_diagram_graph, base_dirs)

        files = File.find_all_file(java_project_address, 'java')
        index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        cd2 = ClassDiagram(java_project_address, base_dirs, files, index_dic)
        cd2.make_class_diagram()
        cd2.show(cd2.class_diagram_graph)
        CDG = cd2.get_CDG()
        cd.show(CDG)
        # g = cd2.class_diagram_graph
        # print(len(list(nx.weakly_connected_components(g))))
        # for i in nx.weakly_connected_components(g):
        #     print(i)