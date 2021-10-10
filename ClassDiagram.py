from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import networkx as nx
import matplotlib.pyplot as plt

from Utils import File, Path



class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self):
        self.currentClass = ''
        self.class_dic = {}
        self.imports = []
        self.imports_star = []
        self.current_relationship = None
        self.no_implements = 0
        self.has_extends = False

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.currentClass = ctx.IDENTIFIER().getText()
        self.class_dic[self.currentClass] = {}

    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassOrInterfaceType(self, ctx:JavaParserLabeled.ClassOrInterfaceTypeContext):
        if len(ctx.IDENTIFIER()) > 0 and self.currentClass != '':
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
                self.current_relationship = 'use'

            self.class_dic[self.currentClass][text[:-1]] = self.current_relationship
            #print(self.current_relationship, text[:-1])


    # this method is for detecting interface relationships
    def enterTypeDeclaration(self, ctx:JavaParserLabeled.TypeDeclarationContext):
        if ctx.classDeclaration() != None:
            if ctx.classDeclaration().IMPLEMENTS() != None:
                self.no_implements = len(ctx.classDeclaration().typeList().typeType())
            if ctx.classDeclaration().EXTENDS() != None:
                self.has_extends = True



class ClassDiagram:
    def __init__(self):
        self.class_diagram_graph = nx.DiGraph()
        self.relationship_names = ['implements', 'extends', 'use']

    def make(self, java_project_address, index_dic=None):
        files = File.find_all_file(java_project_address, 'java')
        edges_label = {}
        if index_dic == None:
            index_dic = File.indexing_files_directory(files, 'class_index.json')
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

    def save(self, address):
        nx.write_gml(self.class_diagram_graph, address)

    def load(self, address):
        self.class_diagram_graph = nx.read_gml(address)

    def show(self):
        #nx.draw(self.class_diagram_graph, with_labels=True)
        #plt.show()
        pos = nx.spring_layout(self.class_diagram_graph)  # pos = nx.nx_agraph.graphviz_layout(G)
        nx.draw_networkx(self.class_diagram_graph, pos)
        labels = nx.get_edge_attributes(self.class_diagram_graph, 'weight')
        for i in labels.keys():
            labels[i] = self.relationship_names[labels[i]]
        nx.draw_networkx_edge_labels(self.class_diagram_graph, pos, edge_labels=labels)
        plt.show()

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

if __name__ == "__main__":
    #java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\java_projects\\javaproject'
    #java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\compiler-factory-method\\refactored\\java_projects\\javaproject'
    java_project_address = 'E:\\sadegh\\iust\\compiler\\compiler projects\\compiler-factory-method\\java_projects\\bigJavaProject\\src\\main\\java'
    cd = ClassDiagram()
    cd.make(java_project_address)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    cd.show()
