from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import networkx as nx
import matplotlib.pyplot as plt
import queue
import json

from utils import File, Path
import config


class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self, base_dirs, index_dic, file_name, file):
        self.current_class = None
        self.current_method = None
        self.class_dic = {}
        self.dependee_dic = {}
        self.imports = []
        self.imports_star = []
        self.current_relationship = None
        self.no_implements = 0
        self.has_extends = False
        self.base_dirs = base_dirs
        self.__package = None
        self.index_dic = index_dic
        self.file_name = file_name
        self.class_list = []
        self.file = file
        self.__depth = 0
        self.relations = []

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()
        self.imports_star.append(self.__package)

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
            if self.__package not in self.imports_star:
                self.imports_star.append(self.__package)
        if self.__depth == 1:
            self.class_list.append(ctx.IDENTIFIER().getText())
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            self.class_dic[self.current_class] = {}

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterEnumDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
            if self.__package not in self.imports_star:
                self.imports_star.append(self.__package)
        if self.__depth == 1:
            self.class_list.append(ctx.IDENTIFIER().getText())
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            self.class_dic[self.current_class] = {}

    def exitEnumDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = ctx.IDENTIFIER().getText()

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = None


    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassOrInterfaceType(self, ctx:JavaParserLabeled.ClassOrInterfaceTypeContext):
        if len(ctx.IDENTIFIER()) > 0 and self.current_class != None:
            text = ''
            for t in ctx.IDENTIFIER():
                text += t.getText() + '.'

            if self.has_extends:
                self.current_relationship = 'extends'
                self.has_extends = False
            elif self.no_implements > 0:
                self.no_implements -= 1
                self.current_relationship = 'implements'
            else:
                self.current_relationship = 'create'

            dependee = text[:-1]
            self.relations.append((self.current_class, dependee, self.current_relationship))

    # this method is for detecting interface relationships
    def enterTypeDeclaration(self, ctx:JavaParserLabeled.TypeDeclarationContext):
        if ctx.classDeclaration() != None:
            if ctx.classDeclaration().IMPLEMENTS() != None:
                self.no_implements = len(ctx.classDeclaration().typeList().typeType())
            if ctx.classDeclaration().EXTENDS() != None:
                self.has_extends = True

    def exitCompilationUnit(self, ctx:JavaParserLabeled.CompilationUnitContext):
        for current_class, dependee, current_relationship in self.relations:
            if not (dependee in self.dependee_dic.keys()):
                if dependee in self.class_list:
                    file_name = self.file_name
                    package = self.__package
                else:
                    file_name = dependee
                    package = Path.find_package_of_dependee(dependee, self.imports, self.imports_star, self.index_dic)

                if package != None:
                    self.dependee_dic[dependee] = package + '-' + file_name + '-' + dependee
            self.class_dic[current_class][dependee] = current_relationship

        for current_class in self.class_dic:
            # detect package and file of each instance
            dependee_dic = {}
            for dependee in self.class_dic[current_class].keys():
                if dependee in self.dependee_dic.keys():
                    dependee_dic[self.dependee_dic[dependee]] = self.class_dic[current_class][dependee]
            self.class_dic[current_class] = dependee_dic


class MethodModificationTypeListener(JavaParserLabeledListener):
    #file_info = {}
    def __init__(self):
        self.file_info = {}
        self.current_class = None
        self.current_method = None
        self.attributes = {}
        self.local_variables = {}
        self.parameters = {}
        self.is_modify_itself = False
        self.__package = None
        self.__method_depth = 0
        self.__class_depth = 0

    def get_file_info(self):
        return self.file_info

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterInterfaceDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.current_class = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class] = {'is_class':False, 'attributes':{}, 'methods':{}}

    def exitInterfaceDeclaration(self, ctx:JavaParserLabeled.InterfaceMethodDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            self.current_class = None
            self.attributes = {}

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.current_class = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class] = {'is_class':True, 'attributes':{}, 'methods':{}}


    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            self.current_class = None
            self.attributes = {}

    def enterEnumDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.current_class = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class] = {'is_class':True, 'attributes':{}, 'methods':{}}

    def exitEnumDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            self.current_class = None
            self.attributes = {}

    def enterInterfaceMethodDeclaration(self, ctx:JavaParserLabeled.InterfaceMethodDeclarationContext):
        if self.__class_depth == 1:
            self.__method_depth += 1
            self.current_method = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class]['methods'][self.current_method] = {}

    def exitInterfaceMethodDeclaration(self, ctx:JavaParserLabeled.InterfaceMethodDeclarationContext):
        if self.__class_depth == 1:
            self.__method_depth -= 1
            self.file_info[self.current_class]['methods'][self.current_method]['is_modify_itself'] = None
            self.current_method = None
            self.local_variables = {}
            self.parameters = {}
            self.is_modify_itself = False

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if self.__class_depth == 1:
            self.__method_depth += 1
            if self.__method_depth == 1:
                self.current_method = ctx.IDENTIFIER().getText()
                self.file_info[self.current_class]['methods'][self.current_method] = {}

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if self.__class_depth == 1:
            self.__method_depth -= 1
            if self.__method_depth == 0:
                self.file_info[self.current_class]['methods'][self.current_method]['is_modify_itself'] = self.is_modify_itself
                self.current_method = None
                self.local_variables = {}
                self.parameters = {}
                self.is_modify_itself = False

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            self.attributes[identifier] = _type
            self.file_info[self.current_class]['attributes'][identifier] = _type


    def enterLocalVariableDeclaration(self, ctx:JavaParserLabeled.LocalVariableDeclarationContext):
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            self.local_variables[identifier] = _type

    def enterFormalParameter(self, ctx:JavaParserLabeled.FormalParameterContext):
        _type = ctx.typeType().getText()
        identifier = ctx.variableDeclaratorId().IDENTIFIER().getText()
        self.parameters[identifier] = _type

    def enterExpression21(self, ctx:JavaParserLabeled.Expression21Context):
        if self.current_method != None:
            if ('expression' in dir(ctx.expression(0))):
                try:
                    variable = ctx.expression(0).expression().getText()
                except:
                    variable = ctx.expression(0).expression(0).getText()
                if variable == 'this':
                    self.is_modify_itself = True
                elif self.is_class_attribute(variable):
                    self.is_modify_itself = True
            else:
                variable = ctx.expression(0).getText()
                if self.is_class_attribute(variable):
                    self.is_modify_itself = True


    def exitConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        self.local_variables = {}
        self.parameters = {}
        self.is_use_def = None
        self.is_use_consult = None

    def is_class_attribute(self, variable):
        is_parameter = False
        is_local_variable = False
        is_attribute = False
        if variable in self.parameters:
            is_parameter = True
        if variable in self.local_variables:
            is_local_variable = True
        if variable in self.attributes:
            is_attribute = True

        if not (is_attribute or is_parameter or is_local_variable):
            self.attributes[variable]= None
            self.file_info[self.current_class]['attributes'][variable] = None
        return (is_attribute and (not(is_parameter or is_local_variable))) or \
               (not (is_attribute or is_parameter or is_local_variable))


class StereotypeListener(JavaParserLabeledListener):
    def __init__(self, methods_information, base_dirs, index_dic, file_name, file):
        self.methods_information = methods_information
        self.current_class = None
        self.current_method = None
        self.class_dic = {}
        self.dependee_dic = {}
        self.imports = []
        self.imports_star = []
        self.current_relationship = None
        self.no_implements = 0
        self.has_extends = False
        self.base_dirs = base_dirs
        self.__package = None
        self.index_dic = index_dic
        self.file_name = file_name
        self.class_list = []
        self.file = file
        self.local_variables = {}
        self.field_variables = {}
        self.formal_parameters = {}
        self.__class_depth = 0
        self.__method_depth = 0

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__class_depth == 1:
            self.class_list.append(ctx.IDENTIFIER().getText())
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            self.class_dic[self.current_class] = {}

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            # detect package and file of each instance
            dependee_dic = {}
            for dependee in self.class_dic[self.current_class].keys():
                if dependee in self.dependee_dic.keys():
                    dependee_dic[self.dependee_dic[dependee]] = self.class_dic[self.current_class][dependee]
            self.class_dic[self.current_class] = dependee_dic

            self.current_class = None
            self.field_variables = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.__method_depth += 1
        if self.__method_depth == 1:
            self.current_method = ctx.IDENTIFIER().getText()

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.__method_depth -= 1
        if self.__method_depth == 0:
            self.current_method = None
            self.local_variables = {}
            self.formal_parameters = {}


    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassOrInterfaceType(self, ctx:JavaParserLabeled.ClassOrInterfaceTypeContext):
        if len(ctx.IDENTIFIER()) > 0 and self.current_class != None:
            text = ''
            for t in ctx.IDENTIFIER():
                text += t.getText() + '.'

            dependee = text[:-1]
            if not (dependee in self.dependee_dic.keys()):
                if dependee in self.class_list:
                    file_name = self.file_name
                    package = self.__package
                else:
                    file_name = dependee
                    package = Path.find_package_of_dependee(dependee, self.imports, self.imports_star, self.index_dic)

                if package != None:
                    self.dependee_dic[dependee] = package + '-' + file_name + '-' + dependee


    def enterFormalParameter(self, ctx:JavaParserLabeled.FormalParameterContext):
        if ctx.typeType().classOrInterfaceType() != None:
            _type = ctx.typeType().classOrInterfaceType().getText()
            identifier = ctx.variableDeclaratorId().getText()
            self.formal_parameters[identifier] = _type

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        if ctx.typeType().classOrInterfaceType() != None:
            _type = ctx.typeType().classOrInterfaceType().getText()
            for i in ctx.variableDeclarators().variableDeclarator():
                identifier = i.variableDeclaratorId().getText()
                self.field_variables[identifier] = _type

    def enterLocalVariableDeclaration(self, ctx:JavaParserLabeled.LocalVariableDeclarationContext):
        if ctx.typeType().classOrInterfaceType() != None:
            _type = ctx.typeType().classOrInterfaceType().getText()
            for i in ctx.variableDeclarators().variableDeclarator():
                identifier = i.variableDeclaratorId().getText()
                self.local_variables[identifier] = _type

    def enterExpression21(self, ctx:JavaParserLabeled.Expression21Context):
        current_exp1 = ctx.expression(0)
        while "expression" in dir(current_exp1):
            current_exp1 = current_exp1.getChild(0)
        dependee = self.__get_object_type(current_exp1.getText())
        try:
            self.class_dic[self.current_class][dependee] = 'use_def'
        except Exception as e:
            print(self.current_class, dependee)
            print(e)

    def enterMethodCall0(self, ctx:JavaParserLabeled.MethodCall0Context):
        method_name = ctx.IDENTIFIER().getText()
        list_of_objects = []
        current_exp1 = ctx.parentCtx
        if "expression" in dir(current_exp1):
            while current_exp1.expression() != None:
                current_exp1 = current_exp1.expression()
                if ('IDENTIFIER' in dir(current_exp1)) and (current_exp1.IDENTIFIER() != None):
                    list_of_objects.append(current_exp1.IDENTIFIER().getText())
                else:
                    break
            list_of_objects.append(current_exp1.getText())
            list_of_objects.reverse()
            stereotype = self.__get_use_type(method_name, list_of_objects)

            if stereotype != None:
                dependee = self.__get_object_type(list_of_objects[0])
                if self.current_class in self.class_dic:
                    if dependee in self.class_dic[self.current_class]:
                        if self.class_dic[self.current_class][dependee] == 'use_consult':
                            self.class_dic[self.current_class][dependee] = stereotype
                    else:
                        self.class_dic[self.current_class][dependee] = stereotype


    def __get_use_type(self, method_name, list_of_objects):
        # detect last object type
        current_type = self.__get_object_type(list_of_objects[0])
        if (current_type != None) and (current_type in self.dependee_dic.keys()):
            current_type = self.dependee_dic[current_type]

            if len(list_of_objects) > 1:
                for object in list_of_objects[1:]:
                    if current_type in self.index_dic:
                        # print(current_type, object)
                        current_type = self.methods_information[current_type]['attributes'][object]

            # detect use type
            if current_type in self.index_dic:
                if method_name in self.methods_information[current_type]['methods']:
                    if self.methods_information[current_type]['methods'][method_name]['is_modify_itself']:
                        return 'use_def'
                    else:
                        return 'use_consult'


    def __get_object_type(self, object):
        current_type = None
        if object in self.field_variables.keys():
            current_type = self.field_variables[object]
        elif object in self.local_variables.keys():
            current_type = self.local_variables[object]
        elif object in self.formal_parameters.keys():
            current_type = self.formal_parameters[object]
        return current_type


class ClassDiagram:
    def __init__(self, java_project_address, base_dirs, index_dic=None):
        self.java_project_address = java_project_address
        self.base_dirs = base_dirs
        if index_dic == None:
            files = File.find_all_file(self.java_project_address, 'java')
            self.index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        else:
            self.index_dic = index_dic

        self.class_diagram_graph = nx.DiGraph()
        self.relationships_name = ['implements', 'extends', 'create', 'use_consult', 'use_def']
        nx.set_edge_attributes(self.class_diagram_graph, self.relationships_name, "relation_type")
        nx.set_node_attributes(self.class_diagram_graph, ['normal', 'abstract', 'interface'], "type")

    def make_class_diagram(self):
        files = File.find_all_file(self.java_project_address, 'java')

        # add nodes to class_diagram
        for c in self.index_dic:
            index = self.index_dic[c]['index']
            self.class_diagram_graph.add_node(index)
            self.class_diagram_graph.nodes[index]['type'] = self.index_dic[c]['type']

        print('Start making class diagram . . .')
        for f in files:
            file_name = Path.get_file_name_from_path(f)
            print('\t' + f)
            try:
                stream = FileStream(f, encoding='utf8')
            except:
                print('\t' + f, 'can not read')
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = ClassDiagramListener(self.base_dirs, self.index_dic, file_name, f)
            walker = ParseTreeWalker()

            walker.walk(
                listener=listener,
                t=tree
            )
            graph = listener.class_dic
            # print('graph:', graph)
            # add edges to class_diagram
            for c in graph:
                for i in graph[c]:
                    if i in self.index_dic.keys():
                        n1 = self.index_dic[c]['index']
                        n2 = self.index_dic[i]['index']
                        relation_type = listener.class_dic[c][i]
                        self.class_diagram_graph.add_edge(n1, n2)
                        self.class_diagram_graph[n1][n2]['relation_type'] = relation_type
        print('End making class diagram !')

    def save(self, address):
        nx.write_gml(self.class_diagram_graph, address)

    def save_index(self, address):
        with open(address, 'w', encoding='utf-8') as f:
            json.dump(self.index_dic, f, ensure_ascii=False, indent=4)

    def load(self, address):
        self.class_diagram_graph = nx.read_gml(address)

    def show(self, graph):
        pos = nx.spring_layout(graph)
        nx.draw_networkx(graph, pos)
        labels = nx.get_edge_attributes(graph, 'relation_type')
        nx.draw_networkx_edge_labels(graph, pos, edge_labels=labels)
        plt.show()

    def dfs(self):
        return nx.dfs_postorder_nodes(self.class_diagram_graph)

    def __get_extend_graph(self):
        graph = {}
        for edge in self.class_diagram_graph.edges:
            if self.class_diagram_graph.edges[edge]['relation_type'] == "extends":
                if edge[1] in graph:
                    graph[edge[1]].append(edge[0])
                else:
                    graph[edge[1]] = [edge[0]]
        return graph


    def __find_extend_roots(self, extends_graph):
        roots = list(extends_graph.keys())
        for d in extends_graph:
            for s in extends_graph[d]:
                if s in roots:
                    roots.remove(s)
        return roots

    def __add_extends_attributes_and_methods(self, parent, child, method_info, index_list):
        # print((parent, child))
        for attribute in method_info[index_list[parent]]['attributes']:
            if not(attribute in method_info[index_list[child]]['attributes']):
                method_info[index_list[child]]['attributes'][attribute] = \
                    method_info[index_list[parent]]['attributes'][attribute]
            else:
                if method_info[index_list[child]]['attributes'][attribute] == None:
                    method_info[index_list[child]]['attributes'][attribute] = \
                    method_info[index_list[parent]]['attributes'][attribute]

        for method in method_info[index_list[parent]]['methods']:
            if not(method in method_info[index_list[child]]['methods']):
                method_info[index_list[child]]['methods'][method] = \
                    method_info[index_list[parent]]['methods'][method]
        return method_info

    def __handle_extends_methods_information(self, method_information):
        extends_graph = self.__get_extend_graph()
        roots = self.__find_extend_roots(extends_graph)
        index_list = list(self.index_dic.keys())
        # print(extends_graph)
        # print(roots)
        q = queue.Queue()
        for root in roots:
            q.put(root)

        while not q.empty():
            parent = q.get()
            if parent in extends_graph:
                for child in extends_graph[parent]:
                    method_information = self.__add_extends_attributes_and_methods(parent,
                                                                                   child,
                                                                                   method_information,
                                                                                   index_list)
                    q.put(child)
        return method_information

    def __find_methods_information(self, files):
        print('start finding methods information . . .')
        methods_info = {}
        for file in files:
            try:
                stream = FileStream(file, encoding='utf8')
                print('\t' + file)
            except:
                print(file, 'can not read')
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = MethodModificationTypeListener()
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            file_info = listener.get_file_info()
            # print(file_info)
            file_name = Path.get_file_name_from_path(file)
            for c in file_info:
                if listener.get_package() == None:
                    package = Path.get_default_package(self.base_dirs, file)
                else:
                    package = listener.get_package()
                #class_index = index_dic[]['index']
                methods_info[package + '-' + file_name + '-' + c] = file_info[c]
        # print(methods_info)
        methods_info = self.__handle_extends_methods_information(methods_info)
        methods_info = self.__calculate_interface_modification_type(methods_info)
        # print(methods_info)
        print("finish finding methods information !")
        return methods_info

    def __calculate_interface_modification_type(self, method_info):
        index_list = list(self.index_dic.keys())
        for c in method_info:
            if not method_info[c]['is_class']:
                implemented_classes = []
                all_depended_classes = list(self.class_diagram_graph.predecessors(1))
                # check if relationship between 2 nodes is implements
                for node in all_depended_classes:
                    if (node, self.index_dic[c]['index']) in self.class_diagram_graph.edges:
                        if self.class_diagram_graph.edges[(node, self.index_dic[c]['index'])]['relation_type'] == 'implements':
                            implemented_classes.append(node)

                for m in method_info[c]['methods']:
                    for implemented_class in implemented_classes:
                        class_name = index_list[implemented_class]
                        try:
                            if method_info[class_name]['methods'][m]['is_modify_itself']:
                                method_info[c]['methods'][m]['is_modify_itself'] = True
                                break
                            else:
                                method_info[c]['methods'][m]['is_modify_itself'] = False
                        except:
                            print("-"*20)
                            print(class_name)
                            print(method_info[class_name]['methods'])
                            print(m)
                            print("-"*20)
                            # quit()
        return method_info

    def set_stereotypes(self):
        files = File.find_all_file(self.java_project_address, 'java')
        methods_information = self.__find_methods_information(files)
        print('Start setting stereotype . . .')
        for f in files:
            file_name = Path.get_file_name_from_path(f)
            print('\t' + f)
            try:
                stream = FileStream(f, encoding='utf8')
            except Exception as e:
                print('\t' + f, 'can not read')
                print(e)
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = StereotypeListener(methods_information, self.base_dirs, self.index_dic, file_name, f)
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            graph = listener.class_dic
            for c in graph:
                for i in graph[c]:
                    if i in self.index_dic.keys():
                        n1 = self.index_dic[c]['index']
                        n2 = self.index_dic[i]['index']
                        relation_type = listener.class_dic[c][i]
                        self.class_diagram_graph[n1][n2]['relation_type'] = relation_type
        print('End setting stereotype !')

    def get_CDG(self):
        CDG = nx.DiGraph()
        relationships_name = ['parent', 'child', 'create', 'use_consult', 'use_def']
        nx.set_edge_attributes(CDG, relationships_name, "relation_type")
        nx.set_node_attributes(CDG, ['normal', 'abstract', 'interface'], "type")

        for n in self.class_diagram_graph.nodes:
            CDG.add_node(n)
            CDG.nodes[n]['type'] = self.class_diagram_graph.nodes[n]['type']

        for edge in self.class_diagram_graph.edges:
            edge_info = self.class_diagram_graph.edges[edge]

            if edge_info['relation_type'] == 'extends':
                CDG.add_edge(edge[0], edge[1])
                CDG.add_edge(edge[1], edge[0])
                CDG[edge[0]][edge[1]]['relation_type'] = 'parent'
                CDG[edge[1]][edge[0]]['relation_type'] = 'child'

            elif edge_info['relation_type'] == 'implements':
                CDG.add_edge(edge[1], edge[0])
                CDG[edge[1]][edge[0]]['relation_type'] = 'child'

            else:
                CDG.add_edge(edge[0], edge[1])
                CDG[edge[0]][edge[1]]['relation_type'] = edge_info['relation_type']
        return CDG

if __name__ == "__main__":
    java_project_address = config.projects_info['javaproject']['path']
    print(java_project_address)
    base_dirs = config.projects_info['javaproject']['base_dirs']
    print(base_dirs)
    files = File.find_all_file(java_project_address, 'java')
    print(files)
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd = ClassDiagram(java_project_address, base_dirs, index_dic)
    cd.make_class_diagram()

    cd.show(cd.class_diagram_graph)
    #cd.load('class_diagram.gml')
    cd.save('class_diagram.gml')

    cd.set_stereotypes()
    cd.save('class_diagram.gml')
    cd.show(cd.class_diagram_graph)
    #
    # CDG = cd.get_CFG()
    # for edge in CDG.edges:
    #     print(edge, CDG.edges[edge])
    # cd.show(CDG)
    #
    # g = cd.class_diagram_graph
    # print(len(list(nx.weakly_connected_components(g))))
    # for i in nx.weakly_connected_components(g):
    #     print(i)