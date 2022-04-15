from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import os
import json


class ClassTypeListener(JavaParserLabeledListener):
    def __init__(self, base_dirs, file_name, file):
        self.file_info = {}
        self.current_class = None
        self.__package = None
        self.__depth = 0
        self.base_dirs = base_dirs
        self.file_name = file_name
        self.file = file

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            type_declaration = ctx.parentCtx
            _type = 'normal'
            if type_declaration.classOrInterfaceModifier() != None:
                if len(type_declaration.classOrInterfaceModifier()) == 1:
                    if type_declaration.classOrInterfaceModifier()[0].getText() == 'abstract':
                        _type = 'abstract'

            self.file_info[self.current_class] = {'type':_type}

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterInterfaceDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            _type = 'interface'
            self.file_info[self.current_class] = {'type':_type}

    def exitInterfaceDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

    def enterEnumDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth += 1
        if self.__package == None:
            self.__package = Path.get_default_package(self.base_dirs, self.file)
        if self.__depth == 1:
            self.current_class = self.__package + '-' + self.file_name + '-' + ctx.IDENTIFIER().getText()
            _type = 'enum'
            self.file_info[self.current_class] = {'type':_type}

    def exitEnumDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__depth -= 1
        if self.__depth == 0:
            self.current_class = None

class File:
    @staticmethod
    def find_all_file(address, type):
        all_files = []
        for root, dirs, files in os.walk(address):
            for file in files:
                if file.endswith('.' + type):
                    all_files.append(os.path.join(root, file))
        return all_files

    @staticmethod
    def indexing_files_directory(files, save_dir, base_java_dirs):
        index = 0
        index_dic = {}
        for f in files:
            file_name = Path.get_file_name_from_path(f)
            try:
                stream = FileStream(f, encoding='utf8')
            except Exception as e:
                print(f, 'can not read')
                print(e)
                continue
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = ClassTypeListener(base_java_dirs, file_name, f)
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )
            for c in listener.file_info:
                index_dic[c] = {'index':index, 'path':f, 'type':listener.file_info[c]['type']}
                index += 1

        with open(save_dir, "w") as write_file:
            json.dump(index_dic, write_file, indent=4)
        return index_dic

class Path:
    @staticmethod
    def find_package_of_dependee(dependee, imports, imports_star, index_dic):
        splitted_dependee = dependee.split('.')
        # for normal import
        for i in imports:
            splitted_import = i.split('.')
            if splitted_dependee[0] == splitted_import[-1]:
                return '.'.join(i.split('.')[:-1])

        # for import star
        class_name = splitted_dependee[-1]
        for i in imports_star:
            index_dic_dependee = i + '.'.join(splitted_dependee[:-1]) + '-' + class_name + '-' + class_name
            if index_dic_dependee in index_dic.keys():
                return i

    @staticmethod
    def get_default_package(base_dirs, file_path):
        for base_dir in base_dirs:
            if base_dir == file_path[:len(base_dir)]:
                target_dir = file_path[len(base_dir):]
                splitted_targer_dir = target_dir.split("\\")
                package = '.'.join(splitted_targer_dir[:-1])
                return package

    @staticmethod
    def get_file_name_from_path(path):
        """
        Use Python built-in functions instead of this
        """
        path = path.split('/')
        class_name = path[-1]
        class_name = class_name.split('.')
        class_name = class_name[0]
        return class_name

    @staticmethod
    def convert_str_paths_to_list_paths(str_paths):
        list_paths = []
        for str_path in str_paths:
            list_paths.append(str_path.split('\\'))
        return list_paths

    @staticmethod
    def detect_path(paths):
        if len(paths) == 1:
            return '\\'.join(paths[0][-2])
        max_path_length = max([len(list_path) for list_path in paths])
        for i in range(max_path_length):
            x = set([j[i] for j in paths])
            if len(x) > 1 :
                return '\\'.join(paths[0][:i])

class List:
    @staticmethod
    def compare_similarity_of_two_list(list1, list2):
        return list(set(list1) & set(list2))

if __name__ == "__main__":
    File.indexing_files_directory(File.find_all_file('E:\\sadegh\\iust\\compiler\\compiler projects\\java_projects\\bigJavaProject', 'java'),
                                  'index.json')
