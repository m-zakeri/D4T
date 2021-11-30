from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import networkx as nx
import matplotlib.pyplot as plt

from utils import File, Path
import config



class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self, methods_information, base_dirs, index_dic):
        self.methods_information = methods_information
        self.current_class = None
        self.current_method = None
        self.class_dic = {}
        self.imports = []
        self.imports_star = []
        self.current_relationship = None
        self.no_implements = 0
        self.has_extends = False
        self.base_dirs = base_dirs
        self.__package = None
        self.index_dic = index_dic

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
            index_dic_dependee = i + '.'.join(splitted_dependee[:-1]) + '-' + class_name
            if index_dic_dependee in self.index_dic.keys():
                print(i)
                return i

        return None


    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.current_class = self.__package + '-' + ctx.IDENTIFIER().getText()
        self.class_dic[self.current_class] = {}

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
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
                self.current_relationship = 'associated'

            dependee = text[:-1]
            dependee_package = self.__find_package_of_dependee(dependee)
            if dependee_package != None:
                self.class_dic[self.current_class][dependee_package + '-' + dependee] = self.current_relationship
            else:
                self.class_dic[self.current_class][self.__package + '-' + dependee] = self.current_relationship


    # this method is for detecting interface relationships
    def enterTypeDeclaration(self, ctx:JavaParserLabeled.TypeDeclarationContext):
        if ctx.classDeclaration() != None:
            if ctx.classDeclaration().IMPLEMENTS() != None:
                self.no_implements = len(ctx.classDeclaration().typeList().typeType())
            if ctx.classDeclaration().EXTENDS() != None:
                self.has_extends = True

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

    def get_file_info(self):
        return self.file_info

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = ctx.IDENTIFIER().getText()
        self.file_info[self.current_class] = {}
        #print('class :', self.current_class)

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.current_class = None
        #print('attributes :', self.attributes)
        self.attributes = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = ctx.IDENTIFIER().getText()
        self.file_info[self.current_class][self.current_method] = {}
        #print('method :', self.current_method)

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.file_info[self.current_class][self.current_method]['is_modify_itself'] = self.is_modify_itself
        self.current_method = None
        self.local_variables = {}
        self.parameters = {}
        self.is_modify_itself = False

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        for vd in ctx.variableDeclarators().variableDeclarator():
            _type = ctx.typeType().getText()
            identifier = vd.variableDeclaratorId().IDENTIFIER().getText()
            self.attributes[identifier] = _type


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
        if 'expression' in dir(ctx.expression(0)):
            variable = ctx.expression(0).expression().getText()
            #print('type1:', variable)
            if variable == 'this' or self.is_class_attribute(variable):
                self.is_modify_itself = True
        else:
            variable = ctx.expression(0).getText()
            if self.is_class_attribute(variable):
                self.is_modify_itself = True
            #print('type2:', variable)


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
        print('fields :', self.field_info)
        self.field_info = {}

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = ctx.IDENTIFIER().getText()
        self.file_info[self.current_class][self.current_method] = {}

    def exitMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.current_method = None
        print('local variable :', self.variable_info)
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
        self.relationship_names = ['implements', 'extends', 'associated']
        self.stereotype_names = ['create', 'use_consult', 'use_def', 'use']

    def make(self, java_project_address, base_dir, index_dic=None):
        files = File.find_all_file(java_project_address, 'java')
        #edges_label = {}
        if index_dic == None:
            index_dic = File.indexing_files_directory(files, 'class_index.json', base_dir)
        #methods_information = self.__find_methods_information(files, index_dic)
        #print(methods_information)
        methods_information = None
        for f in files:
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
            listener = ClassDiagramListener(methods_information, base_dir, index_dic)
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            graph = listener.class_dic
            print(graph)
            for c in graph:
                for i in graph[c]:
                    if i in index_dic.keys():
                        n1 = index_dic[c]
                        n2 = index_dic[i]
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
        #print("start find methods information . . .")
        methods_info = {}
        for file in files:
            try:
                stream = FileStream(file)
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
            for c in file_info:
                if listener.get_package() == None:
                    package = Path.get_default_package(base_dirs, file)
                else:
                    package = listener.get_package()
                class_index = index_dic[package + '-' + c]
                methods_info[class_index] = file_info[c]
        #print("finish finding methods information . . .")
        return methods_info

if __name__ == "__main__":
    java_project_address = config.projects_info['bigJavaProject']['path']
    base_dirs = config.projects_info['bigJavaProject']['base_dirs']
    cd = ClassDiagram()
    cd.make(java_project_address, base_dirs)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    #print(list(cd.dfs()))
    cd.show()
    graphs = []
    g = cd.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
