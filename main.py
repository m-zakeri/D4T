from class_diagram import ClassDiagram
from factory import Factory
from utils import File
import config
import networkx as nx

if __name__ == "__main__":
    java_project_address = config.projects_info['javaproject']['path']
    base_dirs = config.projects_info['javaproject']['base_dirs']
    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd = ClassDiagram()
    cd.make(java_project_address, base_dirs, index_dic)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    cd.show()
    g = cd.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)
    #g = cd.class_diagram_graph
    #print(len(list(nx.weakly_connected_components(g))))
    f = Factory()
    f.detect_and_fix(0.1, index_dic, cd.class_diagram_graph, base_dirs)

    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd2 = ClassDiagram()
    cd2.make(java_project_address, base_dirs, index_dic)
    cd2.show()
    g = cd2.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)