import networkx as nx
from distutils.dir_util import copy_tree
import shutil

from antlr4 import *
from gen.JavaLexer import JavaLexer
from gen.JavaParserLabeled import JavaParserLabeled

from functions import *
from InterfaceCreator import InterfaceCreator

from listeners.FixCreatorListener import FixCreatorListener
from listeners.FixProductsListener import FixProductsListener
from listeners.GraphMakerListener import GraphMakerListener
from listeners.ProductsCreatorDetectorListener import ProductCreatorDetectorListener


def _make_graph(address):

    files = find_all_file(r''+ address, 'java')
    index_dic = {}
    g = nx.DiGraph()
    counter = 0
    for f in files:
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
                        index_dic[graph['path']+'\\'+c] = counter
                        counter += 1
                    if not i+'\\'+get_class_name_from_path(i) in index_dic.keys():
                        index_dic[i+'\\'+get_class_name_from_path(i)] = counter
                        counter += 1
                    n1 = index_dic[graph['path']+'\\'+c]
                    n2 = index_dic[i+'\\'+get_class_name_from_path(i)]
                    g.add_edges_from([(n1, n2)])

    nx.write_gml(g, 'dependency_graph.gml')
    with open("class_name_index.json", "w") as write_file:
        json.dump(index_dic, write_file, indent = 4)

def fix_creator(creator_path, interface_import_text, interface_name, creator_identifier, products_identifier):
    #print(creator_path)
    stream = FileStream(creator_path, encoding='utf8')
    lexer = JavaLexer(stream)
    token_stream = CommonTokenStream(lexer)
    parser = JavaParserLabeled(token_stream)
    parser.getTokenStream()
    parse_tree = parser.compilationUnit()
    my_listener = FixCreatorListener(interfaceName=interface_name,
                                                  interface_import_text=interface_import_text,
                                                  common_token_stream=token_stream,
                                                  creator_identifier=creator_identifier,
                                                  products_identifier=products_identifier)
    walker = ParseTreeWalker()
    walker.walk(t=parse_tree, listener=my_listener)

    with open(creator_path, mode='w', newline='') as f:
        f.write(my_listener.token_stream_rewriter.getDefaultText())

def fix_product(product_path, interface_import_text, interface_name, creator_identifier, products_identifier):
    #print(product_path)
    stream = FileStream(product_path, encoding='utf8')
    lexer = JavaLexer(stream)
    token_stream = CommonTokenStream(lexer)
    parser = JavaParserLabeled(token_stream)
    parser.getTokenStream()
    parse_tree = parser.compilationUnit()
    my_listener = FixProductsListener(interfaceName=interface_name,
                                                  interface_import_text=interface_import_text,
                                                  common_token_stream=token_stream,
                                                  creator_identifier=creator_identifier,
                                                  products_identifier=products_identifier)
    walker = ParseTreeWalker()
    walker.walk(t=parse_tree, listener=my_listener)

    with open(product_path, mode='w', newline='') as f:
        f.write(my_listener.token_stream_rewriter.getDefaultText())


def _detect_procducts_and_creator(sensivity):
    no_detection = 0
    percentage = sensivity / 100
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

            result = find_products(root_dfs[0][0], method_class_dic, percentage)
            if len(result['products']['classes']) > 1:
                #print(root_dfs[0][0])
                #print(method_class_dic)
                #print(percentage)
                print('--------------------------------------------------')
                print(result)
                no_detection += 1
                interface_name = 'Interface' + str(result['factory'])
                result = find_path_from_id(result)
                #print(result['factory'])
                # make interface for
                interface_creator = InterfaceCreator()
                interface_creator.save(result, interface_name, package)
                #print(interface_creator.get_import_text())
                creator_path, creator_className = get_path_and_className_from_nodeName(result['factory'])
                products_path = []
                products_className = []
                for i in result['products']['classes']:
                    product_path, product_className = get_path_and_className_from_nodeName(i)
                    products_path.append(product_path)
                    products_className.append(product_className)

                fix_creator(creator_path, interface_creator.get_import_text(), interface_name, creator_className, products_className)
                for product_path in products_path:
                    fix_product(product_path, interface_creator.get_import_text(), interface_name, creator_className,
                                products_className)
                print('--------------------------------------------------')
    return no_detection

import os
import pathlib
def ignore(path, content_list):
    return [
        content
        for content in content_list
        if os.path.isdir(os.path.join(path, content))
    ]

if __name__ == "__main__":
    base_dir = pathlib.Path().absolute()
    projects = ['java_projects\\javaproject' ]
    project_names = ['javaproject']
    i = 0
    sensitivity = [20, 40, 60, 80, 100]
    for p in projects:
        print(p)
        new_address = 'refactored' + '\\' + p
        copy_tree(p, new_address)
        print(p, new_address)
        _make_graph(new_address)
        for s in sensitivity:
            no_detection = _detect_procducts_and_creator(s)
            print(s, no_detection)
            copy_tree(new_address, 'refactored\\' + project_names[i] + str(s) + p)
            shutil.rmtree(new_address)
            shutil.copytree(p,new_address)
        i += 1



