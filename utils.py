from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import os
import json


class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self):
        self.__classes = []
        self.__package = None
        self.__current_class = None
        self.__in_nest_class = False

    def get_classes(self):
        return self.__classes

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == None:
            self.__current_class = ctx.IDENTIFIER().getText()
            self.__classes.append(ctx.IDENTIFIER().getText())
        else:
            self.__in_nest_class = True

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == ctx.IDENTIFIER().getText():
            self.__in_nest_class = False

        if not self.__in_nest_class:
            self.__current_class = None

    def enterInterfaceDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == None:
            self.__current_class = ctx.IDENTIFIER().getText()
            self.__classes.append(ctx.IDENTIFIER().getText())
        else:
            self.__in_nest_class = True

    def exitInterfaceDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == ctx.IDENTIFIER().getText():
            self.__in_nest_class = False

        if not self.__in_nest_class:
            self.__current_class = None

    def enterEnumDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == None:
            self.__current_class = ctx.IDENTIFIER().getText()
            self.__classes.append(ctx.IDENTIFIER().getText())
        else:
            self.__in_nest_class = True

    def exitEnumDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.__current_class == ctx.IDENTIFIER().getText():
            self.__in_nest_class = False

        if not self.__in_nest_class:
            self.__current_class = None

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
                stream = FileStream(f)
            except Exception as e:
                print(f, 'can not read')
                print(e)
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
            if listener.get_package() == None:
                package = Path.get_default_package(base_java_dirs, f)
            else:
                package = listener.get_package()
            for c in listener.get_classes():
                index_dic[package + "-" + file_name + "-" + c] = {'index':index, 'path':f}
                index += 1

        with open(save_dir, "w") as write_file:
            json.dump(index_dic, write_file, indent=4)
        return index_dic

class Path:
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
        path = path.split('\\')
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
