import json
import networkx as nx

from antlr4 import *
from antlr4.TokenStreamRewriter import TokenStreamRewriter
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled

from interface import InterfaceAdapter, InterfaceCreator
from utils import get_parser, get_parser_and_tokens, Path
import config


class CreatorListener(JavaParserLabeledListener):
    def __init__(self, base_dirs, index_dic, file_name, file, target_classes):
        self.constructors_info = dict()
        self.current_class = None
        self.__package = None
        self.__depth = 0
        self.imports_star = list()
        self.imports = list()

        self.target_classes = target_classes
        self.base_dirs = base_dirs
        self.file_name = file_name
        self.file = file
        self.index_dic = index_dic

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth += 1
        if self.__package is None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            if self.current_class in self.target_classes:
                self.constructors_info[self.current_class] = []

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        if self.__depth == 1 and self.current_class in self.target_classes:
            formal_parameters = []
            if ctx.formalParameters().formalParameterList() is not None:
                for f in ctx.formalParameters().formalParameterList().formalParameter():
                    type_ = f.typeType().getText()
                    type_package = Path.find_package_of_dependee(type_, self.imports, self.imports_star, self.index_dic)
                    if type_package:
                        type_ = f'{type_package}.{type_}'
                    identifier = f.variableDeclaratorId().getText()
                    formal_parameters.append({'type': type_, 'identifier': identifier})
            self.constructors_info[self.current_class].append(formal_parameters)


class InjectorListener(JavaParserLabeledListener):
    def __init__(self, base_dirs, file_name, file):
        self.file_info = {}
        self.current_class = None
        self.__package = None
        self.__depth = 0
        self.base_dirs = base_dirs
        self.file_name = file_name
        self.file = file

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth += 1
        if self.__package is None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            type_declaration = ctx.parentCtx
            _type = 'class'
            if type_declaration.classOrInterfaceModifier() is not None:
                if len(type_declaration.classOrInterfaceModifier()) == 1:
                    if type_declaration.classOrInterfaceModifier()[0].getText() == 'abstract':
                        _type = 'abstract class'

            self.file_info[self.current_class] = {'type': _type}

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterInterfaceDeclaration(self, ctx: JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth += 1
        if self.__package is None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            _type = 'interface'
            self.file_info[self.current_class] = {'type': _type}

    def exitInterfaceDeclaration(self, ctx: JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterEnumDeclaration(self, ctx: JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth += 1
        if self.__package is None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            _type = 'enum'
            self.file_info[self.current_class] = {'type': _type}

    def exitEnumDeclaration(self, ctx: JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None


class Injector:
    def __init__(self, name, path, base_dirs, index_dic):
        self.name = name
        self.path = path
        self.index_dic = index_dic
        self.base_dirs = base_dirs

    # classes parameter is dictionary that keys are long name of class and values are their dependencies
    def create(self, classes: dict):
        classes_info = {}
        for class_ in classes:
            f = self.index_dic[class_]['path']
            file_name = Path.get_file_name_from_path(f)
            parser = get_parser(f)
            tree = parser.compilationUnit()
            listener = CreatorListener(self.base_dirs, self.index_dic, file_name, f, classes)
            walker = ParseTreeWalker()
            walker.walk(listener=listener, t=tree)
            print(listener.constructors_info)
            for c in listener.constructors_info:
                classes_info[c] = []
                for constructor in listener.constructors_info[c]:
                    classes_info[c].append({'params': constructor, 'dependencies': []})

        self.__make_injector_body(classes_info)

    def inject(self, classes: list):
        pass

    def __make_injector_body(self, classes_info):
        text = ''

        # write package statement
        package = Path.get_default_package(self.base_dirs, self.path)
        text += f'package {package};\n'

        class_body = ''
        # write methods statement
        for class_ in classes_info:
            for constructor in classes_info[class_]:
                splitted_class = class_.split('-')
                class_name = class_.replace('-', '_').replace('.', '_')
                method_params_statement = ''
                for param in constructor['params'][:len(constructor['params']) - len(constructor['dependencies'])]:
                    method_params_statement += f'{param["type"]} {param["identifier"]},'
                method_params_statement = method_params_statement[:-1]
                method_body = f'\t public static {splitted_class[0]}.{splitted_class[2]} get_{class_name}({method_params_statement})' + '{' + '\n\t'

                for dependee in constructor['dependencies']:
                    pass

                # write method return statement
                method_body += f'\t{".".join(splitted_class[:-1])} obj'
                method_body += f' = new {splitted_class[0]}.{splitted_class[2]}('
                obj_params = [param['identifier'] for param in constructor['params']]
                method_body += ', '.join(obj_params) + ');\n'
                method_body += '\t\treturn obj;\n\t}'
                # print(method_body)
                class_body += method_body + '\n\n'
        class_body = class_body[:-2]


        text += f'public class {self.name}\n' + '{\n' + class_body + '\n}'

        with open(f'{self.path}{self.name}.java', mode='w', newline='', encoding='utf8', errors='ignore') as f:
            f.write(text)


from utils import File
if __name__ == '__main__':
    java_project = "javaproject"
    java_project_address = config.projects_info[java_project]['path']
    print('java_project_address', java_project_address)
    base_dirs = config.projects_info[java_project]['base_dirs']
    print('base_dirs', base_dirs)
    files = File.find_all_file(java_project_address, 'java')
    print(files)
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)

    injector_path = 'benchmarks/javaproject/com/creator/'
    injector = Injector('Injector', injector_path, base_dirs, index_dic)
    classes = dict()
    for c in index_dic:
        classes[c] = []

    injector.create(classes)
