"""
The utility module

"""

__version__ = '0.1.1'
__author__ = 'Sadegh Jafari, Morteza Zakeri'


import os
import json

from antlr4 import *

from gen.JavaLexer import JavaLexer
from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener

from threading import Thread
import functools


def timeout(timeout):
    def deco(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            res = [Exception('function [%s] timeout [%s seconds] exceeded!' % (func.__name__, timeout))]
            def newFunc():
                try:
                    res[0] = func(*args, **kwargs)
                except Exception as e:
                    res[0] = e
            t = Thread(target=newFunc)
            t.daemon = True
            try:
                t.start()
                t.join(timeout)
            except Exception as je:
                print ('error starting thread')
                raise je
            ret = res[0]
            if isinstance(ret, BaseException):
                raise ret
            return ret
        return wrapper
    return deco


class Struct:
    def __init__(self, **entries):
        self.__dict__.update(entries)


class ClassTypeListener(JavaParserLabeledListener):
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


class File:
    @staticmethod
    def find_all_file(address, type_):
        all_files = []
        for root, dirs, files in os.walk(address):
            for file in files:
                if file.endswith('.' + type_):
                    all_files.append(os.path.join(root, file).replace("\\", "/"))
        return all_files

    @staticmethod
    def indexing_files_directory(files, save_dir, base_java_dirs):
        index = 0
        index_dic = {}
        for f in files:
            file_name = Path.get_file_name_from_path(f)
            parser = get_parser(f)
            tree = parser.compilationUnit()
            listener = ClassTypeListener(base_java_dirs, file_name, f)
            walker = ParseTreeWalker()
            walker.walk(listener=listener, t=tree)
            for c in listener.file_info:
                index_dic[c] = {'index': index, 'path': f, 'type': listener.file_info[c]['type']}
                index += 1

        with open(save_dir, mode="w", encoding='utf-8', errors='ignore') as write_file:
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
                splitted_targer_dir = target_dir.split("/")
                package = '.'.join(splitted_targer_dir[:-1])
                return package

    @staticmethod
    def get_file_name_from_path(path):
        head, tail = os.path.split(path)
        class_name = tail.split('.')[0]
        return class_name

    @staticmethod
    def convert_str_paths_to_list_paths(str_paths):
        list_paths = []
        for str_path in str_paths:
            list_paths.append(str_path.split('/'))
        return list_paths

    @staticmethod
    def detect_path(paths):
        if len(paths) == 1:
            return '/'.join(paths[0][:-1])
        max_path_length = max([len(list_path) for list_path in paths])
        for i in range(max_path_length):
            x = set([j[i] for j in paths])
            if len(x) > 1:
                return '/'.join(paths[0][:i])


class List:
    @staticmethod
    def compare_similarity_of_two_list(list1, list2):
        return list(set(list1) & set(list2))


def get_parser(path):
    stream = FileStream(path, encoding='utf8', errors='ignore')
    lexer = JavaLexer(stream)
    tokens = CommonTokenStream(lexer)
    parser = JavaParserLabeled(tokens)
    return parser


def get_parser_and_tokens(path):
    stream = FileStream(path, encoding='utf8', errors='ignore')
    lexer = JavaLexer(stream)
    tokens = CommonTokenStream(lexer)
    parser = JavaParserLabeled(tokens)
    return parser, tokens


if __name__ == "__main__":
    File.indexing_files_directory(
        File.find_all_file('E:/sadegh/iust/compiler/compiler projects/java_projects/javaproject', 'java'),
        'index.json', ['E:/sadegh/iust/compiler/compiler projects/java_projects/javaproject'])
