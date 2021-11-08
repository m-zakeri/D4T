from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import networkx as nx
import matplotlib.pyplot as plt

from utils import File, Path



class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self):
        self.current_class = None
        self.current_method = None
        self.class_dic = {}
        self.imports = []
        self.imports_star = []
        self.current_relationship = None
        self.no_implements = 0
        self.has_extends = False

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.current_class = ctx.IDENTIFIER().getText()
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

            self.class_dic[self.current_class][text[:-1]] = self.current_relationship


    # this method is for detecting interface relationships
    def enterTypeDeclaration(self, ctx:JavaParserLabeled.TypeDeclarationContext):
        if ctx.classDeclaration() != None:
            if ctx.classDeclaration().IMPLEMENTS() != None:
                self.no_implements = len(ctx.classDeclaration().typeList().typeType())
            if ctx.classDeclaration().EXTENDS() != None:
                self.has_extends = True

class MethodModificationTypeListener(JavaParserLabeledListener):
    #file_info = {}
    def __init__(self, classes_method_info=None):
        self.file_info = {}
        self.current_class = None
        self.current_method = None
        self.attributes = {}
        self.local_variables = {}
        self.parameters = {}
        self.is_modify_itself = False

    def get_file_info(self):
        return self.file_info

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
        #print('local variable :', self.local_variables)
        #print('parameters :', self.parameters)
        #print('is use_def :', self.is_modify_itself)
        self.file_info[self.current_class][self.current_method]['Modify_itself'] = self.is_modify_itself
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

    def make(self, java_project_address, index_dic=None):
        files = File.find_all_file(java_project_address, 'java')
        #edges_label = {}
        if index_dic == None:
            index_dic = File.indexing_files_directory(files, 'class_index.json')
        print(self.__find_methods_information(files, index_dic))
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
            listener = ClassDiagramListener()
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            graph = self.__make_class_dependency(listener.class_dic, listener.imports, listener.imports_star, f)
            for c in graph['classes']:
                for i in graph['classes'][c]:
                    n1 = index_dic[graph['path'] + '\\' + c]
                    n2_class_name = Path.get_class_name_from_path(i)
                    n2 = index_dic[i + '\\' + n2_class_name]
                    weight = self.relationship_names.index(listener.class_dic[c][n2_class_name])
                    self.class_diagram_graph.add_edge(n1, n2, weight=weight)
                    self.class_diagram_graph[n1][n2]['weight'] = weight

            # test Stereotype listener


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

    def __make_class_dependency(self, class_dic, imports, imports_star, path):
        result = {'path': path, 'classes': {}}

        for c in class_dic.keys():
            data = []
            for i in class_dic[c]:
                path_of_class_in_type_of_import = Path.find_path_of_class_in_type_of_import(imports, i)
                if path_of_class_in_type_of_import != None:
                    path_of_import = Path.find_path_of_import(path, Path.find_path_of_class_in_type_of_import(imports, i))
                    if path_of_import != None:
                        if not path_of_import in data:
                            data.append(path_of_import)
            import_star_paths = Path.find_all_path_of_import_star(path, imports_star)
            for i in class_dic[c]:
                for pp in import_star_paths:
                    if Path.get_class_name_from_path(pp) == i:
                        data.append(pp)
                        break
            result['classes'][c] = data
        return result

    def __find_methods_information(self, files, index_dic):
        #print("start find methods information . . .")
        methods_info = {}
        for file in files:
            #print(file)
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
                class_index = index_dic[file + '\\' + c]
                methods_info[class_index] = file_info[c]
        #print("finish finding methods information . . .")
        return methods_info

if __name__ == "__main__":
    #java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\java_projects\\javaproject'
    java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\compiler-factory-method\\refactored\\java_projects\\javaproject'
    #java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\compiler-factory-method\\java_projects\\bigJavaProject\\src\\main\\java'
    cd = ClassDiagram()
    cd.make(java_project_address)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    #print(list(cd.dfs()))
    #cd.show()

