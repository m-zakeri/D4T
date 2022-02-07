from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import networkx as nx
import matplotlib.pyplot as plt

from utils import File, Path
import config



class ClassDiagramListener(JavaParserLabeledListener):
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
        self.in_nest_class = False

    def get_package(self):
        return self.__package

    def __find_package_of_dependee(self, dependee):
        splitted_dependee = dependee.split('.')
        # for normal import
        for i in self.imports:
            splitted_import = i.split('.')
            if splitted_dependee[0] == splitted_import[-1]:
                return '.'.join(i.split('.')[:-1])

        # for import star
        class_name = splitted_dependee[-1]
        for i in self.imports_star:
            index_dic_dependee = i + '.'.join(splitted_dependee[:-1]) + '-' + class_name + '-' + class_name
            if index_dic_dependee in self.index_dic.keys():
                return i

        return None


    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.current_class == None:
            self.class_list.append(ctx.IDENTIFIER().getText())
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            self.class_dic[self.current_class] = {}
        else:
            self.in_nest_class = True

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.current_class == ctx.IDENTIFIER().getText():
            self.in_nest_class = False

        if not self.in_nest_class:
            # detect package and file of each instance
            dependee_dic = {}
            for dependee in self.class_dic[self.current_class].keys():
                #print(self.dependee_dic)
                if dependee in self.dependee_dic.keys():
                    dependee_dic[self.dependee_dic[dependee]] = self.class_dic[self.current_class][dependee]
            self.class_dic[self.current_class] = dependee_dic

            self.current_class = None
            #print('field_variables:', self.field_variables)
            self.field_variables = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = ctx.IDENTIFIER().getText()

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = None
        #print('formal_parameters:', self.formal_parameters)
        #print('local_variables:', self.local_variables)
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
                #print('text:', text)

            if self.has_extends:
                self.current_relationship = 'extends'
                self.has_extends = False
            elif self.no_implements > 0:
                self.no_implements -= 1
                self.current_relationship = 'implements'
            else:
                self.current_relationship = 'create'

            dependee = text[:-1]
            if not (dependee in self.dependee_dic.keys()):
                if dependee in self.class_list:
                    file_name = self.file_name
                    package = self.__package
                else:
                    file_name = dependee
                    package = self.__find_package_of_dependee(dependee)

                if package != None:
                    self.dependee_dic[dependee] = package + '-' + file_name + '-' + dependee
            self.class_dic[self.current_class][dependee] = self.current_relationship


    # this method is for detecting interface relationships
    def enterTypeDeclaration(self, ctx:JavaParserLabeled.TypeDeclarationContext):
        if ctx.classDeclaration() != None:
            if ctx.classDeclaration().IMPLEMENTS() != None:
                self.no_implements = len(ctx.classDeclaration().typeList().typeType())
            if ctx.classDeclaration().EXTENDS() != None:
                self.has_extends = True

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

    #def enterExpression21(self, ctx:JavaParserLabeled.Expression21Context):
    #    print('expression21:', ctx.getText())

    #def enterExpression1(self, ctx:JavaParserLabeled.Expression1Context):
    #    print('expression1:', ctx.getText())

    def enterMethodCall0(self, ctx:JavaParserLabeled.MethodCall0Context):
        method_name = ctx.IDENTIFIER().getText()
        list_of_objects = []
        current_exp1 = ctx.parentCtx
        #print('current_exp1:', current_exp1.getText())
        if "expression" in dir(current_exp1):
            while current_exp1.expression() != None:
                current_exp1 = current_exp1.expression()
                if ('IDENTIFIER' in dir(current_exp1)) and (current_exp1.IDENTIFIER() != None):
                    list_of_objects.append(current_exp1.IDENTIFIER().getText())
                else:
                    break
            list_of_objects.append(current_exp1.getText())
            list_of_objects.reverse()
            #print('method_name:', method_name)
            #print('list_of_objects:', list_of_objects)

    def get_use_type(self, method_name, list_of_objects):
        pass

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
        self.in_sub_class = False

    def get_file_info(self):
        return self.file_info

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.current_class == None:
            self.current_class = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class] = {'attributes':{}, 'methods':{}}
            #print('class :', self.current_class)
        else:
            self.in_sub_class = True

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if not self.in_sub_class:
            if self.current_class == ctx.IDENTIFIER().getText():
                self.in_sub_class = False
            self.current_class = None
            #print('attributes :', self.attributes)
            self.attributes = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if not self.in_sub_class:
            self.current_method = ctx.IDENTIFIER().getText()
            self.file_info[self.current_class]['methods'][self.current_method] = {}
            #print('method :', self.current_method)

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        if not self.in_sub_class:
            self.file_info[self.current_class]['methods'][self.current_method]['is_modify_itself'] = self.is_modify_itself
            self.current_method = None
            self.local_variables = {}
            self.parameters = {}
            self.is_modify_itself = False

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        #print(ctx.getText())
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            #print(_type, identifier)
            self.attributes[identifier] = _type
            self.file_info[self.current_class]['attributes'][identifier] = _type
            #print(self.attributes)


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
        #print('current_method:', self.current_method)
        if self.current_method != None:
            if ('expression' in dir(ctx.expression(0))):
                try:
                    variable = ctx.expression(0).expression().getText()
                except:
                    variable = ctx.expression(0).expression(0).getText()
                #print('variable:', variable)
                if variable == 'this':
                    self.is_modify_itself = True
                elif self.is_class_attribute(variable):
                    self.is_modify_itself = True
            else:
                variable = ctx.expression(0).getText()
                #print('variable:', variable)
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
        return is_attribute and (not(is_parameter or is_local_variable))


class StereotypeListener(JavaParserLabeledListener):
    file_info = {}
    def __init__(self, classes_method_info=None):
        self.current_class = None
        self.current_method = None
        self.field_info = {}
        self.variable_info = {}

    def get_file_info(self):
        return self.file_info

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = ctx.IDENTIFIER().getText()
        self.file_info[self.current_class] = {}

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = None
        self.field_info = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = ctx.IDENTIFIER().getText()
        self.file_info[self.current_class][self.current_method] = {}

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = None
        self.variable_info = {}

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            self.field_info[identifier] = _type


    def enterLocalVariableDeclaration(self, ctx:JavaParserLabeled.LocalVariableDeclarationContext):
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            self.variable_info[identifier] = _type


class ClassDiagram:
    def __init__(self):
        self.class_diagram_graph = nx.DiGraph()
        self.relationship_names = ['implements', 'extends', 'create', 'use_consult', 'use_def']

    def make(self, java_project_address, base_dirs, index_dic=None):
        files = File.find_all_file(java_project_address, 'java')
        if index_dic == None:
            index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
        methods_information = self.__find_methods_information(files, index_dic)
        print(methods_information)
        #methods_information = None
        for f in files:
            file_name = Path.get_file_name_from_path(f)
            print(f)
            try:
                stream = FileStream(f)
            except:
                print(f, 'can not read')
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = ClassDiagramListener(methods_information, base_dirs, index_dic, file_name, f)
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            graph = listener.class_dic
            print('graph:', graph)
            for c in graph:
                for i in graph[c]:
                    if i in index_dic.keys():
                        n1 = index_dic[c]['index']
                        n2 = index_dic[i]['index']
                        weight = self.relationship_names.index(listener.class_dic[c][i])
                        self.class_diagram_graph.add_edge(n1, n2, weight=weight)
                        self.class_diagram_graph[n1][n2]['weight'] = weight

    def save(self, address):
        nx.write_gml(self.class_diagram_graph, address)

    def load(self, address):
        self.class_diagram_graph = nx.read_gml(address)

    def show(self):
        pos = nx.spring_layout(self.class_diagram_graph)  # pos = nx.nx_agraph.graphviz_layout(G)
        nx.draw_networkx(self.class_diagram_graph, pos)
        labels = nx.get_edge_attributes(self.class_diagram_graph, 'weight')
        for i in labels.keys():
            labels[i] = self.relationship_names[labels[i]]
        nx.draw_networkx_edge_labels(self.class_diagram_graph, pos, edge_labels=labels)
        plt.show()

    def dfs(self):
        return nx.dfs_postorder_nodes(self.class_diagram_graph)

    def __find_methods_information(self, files, index_dic):
        print('start finding methods information . . .')
        methods_info = {}
        for file in files:
            try:
                stream = FileStream(file)
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
            #print(listener.get_file_info())
            file_info = listener.get_file_info()
            file_name = Path.get_file_name_from_path(file)
            for c in file_info:
                if listener.get_package() == None:
                    package = Path.get_default_package(base_dirs, file)
                else:
                    package = listener.get_package()
                class_index = index_dic[package + '-' + file_name + '-' + c]['index']
                methods_info[class_index] = file_info[c]
        print("finish finding methods information !")
        return methods_info

if __name__ == "__main__":
    java_project_address = config.projects_info['javaproject']['path']
    base_dirs = config.projects_info['javaproject']['base_dirs']
    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index2.json', base_dirs)
    cd = ClassDiagram()
    cd.make(java_project_address, base_dirs, index_dic)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    #print(list(cd.dfs()))
    cd.show()
    g = cd.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)

