import networkx as nx
from distutils.dir_util import copy_tree

from antlr4 import *
from gen.JavaLexer import JavaLexer
from gen.JavaParserLabeled import JavaParserLabeled

from functions import *
from listeners.GraphMakerListener import GraphMakerListener
from listeners.ConstructorEditorListener import ConstructorEditorListener
from listeners.DependeeEditorListener import DependeeEditorListener
from InterfaceCreator import InterfaceCreator



def make_dependency_graph():
    address = input('please input your java project directory : ')
    new_address = 'refactored\\' + address
    copy_tree(r''+address, r''+new_address)
    files = find_all_file(r''+new_address, 'java')
    index_dic = {}
    g = nx.DiGraph()
    index = 0
    for f in files:
        print(f)
        x = f.split('.')
        if x[-1] == "java" and len(f) > 1:
            file_path = r""+f
            try:
                stream = FileStream(file_path)
            except:
                print(f, 'can not read')
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = GraphMakerListener()
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            graph = make_graph(listener.class_dic, listener.imports,listener.imports_star, f)
            for c in graph['classes']:
                for i in graph['classes'][c]:
                    if not graph['path']+'\\'+c in index_dic.keys():
                        index_dic[graph['path']+'\\'+c] = index
                        index += 1
                    if not i+'\\'+get_class_name_from_path(i) in index_dic.keys():
                        index_dic[i+'\\'+get_class_name_from_path(i)] = index
                        index += 1
                    n1 = index_dic[graph['path']+'\\'+c]
                    n2 = index_dic[i+'\\'+get_class_name_from_path(i)]
                    g.add_edges_from([(n1, n2)])

    nx.write_gml(g, 'dependency_graph.gml')
    with open("class_name_index.json", "w") as write_file:
        json.dump(index_dic, write_file, indent = 4)



def injection_pattern():
    with open("class_name_index.json", "r") as file:
        class_name_index = json.load(file)
    g = nx.read_gml('dependency_graph.gml')
    parents = list((v for v, d in g.in_degree() if d >= 0))
    for p in parents:
        root_dfs = list(nx.bfs_edges(g, source=p, depth_limit=1))
        if len(root_dfs) > 0:
            for child_index in root_dfs:
                child = list(class_name_index.keys())[int(child_index[1])]
                child_path, child_name = get_path_and_className_from_nodeName(child)
                try:
                    stream = FileStream(r"" + child_path)
                except:
                    print(child_path, 'can not read')
                lexer = JavaLexer(stream)
                tokens = CommonTokenStream(lexer)
                parser = JavaParserLabeled(tokens)
                tree = parser.compilationUnit()
                listener = ConstructorEditorListener(common_token_stream=tokens)
                walker = ParseTreeWalker()
                walker.walk(
                    listener=listener,
                    t=tree
                )

                #print(listener.token_stream_rewriter.getDefaultText())
                with open(r"" + child_path, mode='w', newline='') as f:
                    f.write(listener.token_stream_rewriter.getDefaultText())

        # refactor dependee class
        parent = list(class_name_index.keys())[int(p)]
        parent_path, parent_name = get_path_and_className_from_nodeName(parent)
        #try:
        stream = FileStream(r"" + parent_path)
        #except:
        #    print(parent_path, 'can not read')
        lexer = JavaLexer(stream)
        walker = ParseTreeWalker()
        walker.walk(
            listener=listener,
            t=tree
        )
        tokens = CommonTokenStream(lexer)
        parser = JavaParserLabeled(tokens)
        tree = parser.compilationUnit()
        listener = DependeeEditorListener(
            common_token_stream=tokens
        )


        print(listener.token_stream_rewriter.getDefaultText())
        #with open(r"" + parent_path, mode='w', newline='') as f:
            #f.write(listener.token_stream_rewriter.getDefaultText())

def addInjectorInterface():
    with open("class_name_index.json", "r") as file:
        class_name_index = json.load(file)
    g = nx.read_gml('dependency_graph.gml')
    roots = list((v for v, d in g.in_degree() if d >= 0))
    for r in roots:
        root_dfs = list(nx.bfs_edges(g, source=r, depth_limit=1))
        if len(root_dfs) > 1:
            method_class_dic = {}
            package = None
            for child_index in root_dfs:
                child = list(class_name_index.keys())[int(child_index[1])]
                child_path, child_name = get_path_and_className_from_nodeName(child)
                try:
                    print('child path : ', child_path)
                    stream = FileStream(r""+child_path)
                except:
                    print(child_path, 'can not read')
                lexer = JavaLexer(stream)
                tokens = CommonTokenStream(lexer)
                parser = JavaParserLabeled(tokens)
                tree = parser.compilationUnit()
                listener = ProductCreatorDetectorListener(get_class_name_from_path(child_name))
                walker = ParseTreeWalker()
                walker.walk(
                    listener=listener,
                    t=tree
                )
                method_class_dic[int(child_index[1])] = listener.methods
                package = listener.package

            if len(result['products']['classes']) > 1:
                #print(root_dfs[0][0])
                #print(method_class_dic)
                #print(percentage)
                print('--------------------------------------------------')
                print(result)
                interface_name = 'Interface' + str(result['factory_dir'])
                result = find_path_from_id(result)
                print(result)
                #print(result['factory_dir'])
                # make interface for
                interface_creator = InterfaceCreator()
                interface_creator.save(result, interface_name, package)
                #print(interface_creator.get_import_text())
                creator_path, creator_className = get_path_and_className_from_nodeName(result['factory_dir'])
                products_path = []
                products_className = []

if __name__ == "__main__":
    order = input('exit[0] , make_graph[1] , injection_pattern[2] : ')
    while True:
        if order == '0':
            break
        elif order == '1':
            make_dependency_graph()
        elif order == '2':
            injection_pattern()
        print('*******************************************************************************************')
        order = input('exit[0] , make_graph[1] , detect_products_and_creator[2] : ')
