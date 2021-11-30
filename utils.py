from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from antlr4 import *
from gen.JavaLexer import JavaLexer

import os
import json


class ClassDiagramListener(JavaParserLabeledListener):
    def __init__(self):
        self.__classes = []
        self.__interfaces = []
        self.__package = None

    def get_classes(self):
        return self.__classes

    def get_interfaces(self):
        return self.__interfaces

    def get_package(self):
        return self.__package

    def enterPackageDeclaration(self, ctx:JavaParserLabeled.PackageDeclarationContext):
        self.__package = ctx.qualifiedName().getText()

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__classes.append(ctx.IDENTIFIER().getText())

    def enterInterfaceDeclaration(self, ctx:JavaParserLabeled.InterfaceDeclarationContext):
        self.__interfaces.append(ctx.IDENTIFIER().getText())

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
            for c in listener.get_classes() + listener.get_interfaces():
                if listener.get_package() == None:
                    package = None
                    for base_dir in base_java_dirs:
                        if base_dir == f[:len(base_dir)]:
                            target_dir = f[len(base_dir):]
                            splitted_targer_dir = target_dir.split("\\")
                            package = '.'.join(splitted_targer_dir[:-1])
                            class_name = splitted_targer_dir[-1][:-5]
                            index_dic[package + '-' + class_name] = index
                else:
                    index_dic[listener.get_package() + "-" + c] = index
                index += 1

        with open(save_dir, "w") as write_file:
            json.dump(index_dic, write_file, indent=4)
        return index_dic

class Path:
    @staticmethod
    def get_default_package(base_dir, file_path):
        package = file_path[len(base_dir):]
        package = package.split('\\')
        package = '.'.join(package[:-1])
        return package

    @staticmethod
    def get_path_and_className_from_nodeName(nodeName):
        nodeName = nodeName.split('\\')
        className = nodeName[-1]
        path = '\\'.join(nodeName[:-1])
        return path, className

    @staticmethod
    def find_path_of_import(path, _import):
        _import = _import.split('.')
        path = path.split('\\')
        candidate_path = []
        for d in range(len(path) - 1):
            if path[d] == _import[0]:
                candidate_path.append('\\'.join(path[:d] + _import) + '.java')
            elif path[d] == path[-2]:
                candidate_path.append('\\'.join(path[:-1] + _import) + '.java')
        for p in candidate_path:
            try:
                file = open('' + p, 'r')
                file.close()
                return p
            except:
                pass

    @staticmethod
    def find_all_path_of_import_star(path, imports_star):
        path = path.split('\\')
        candidate_path = []
        for import_star in imports_star:
            import_star = import_star.split('.')
            for d in range(len(path)):
                if path[d] == import_star[0]:
                    candidate_path.append('\\'.join(path[:d] + import_star))

        result = []
        for c in candidate_path:
            result += File.find_all_file(c, 'java')
        return result

    @staticmethod
    def find_path_of_class_in_type_of_import(import_list, class_name):
        class_name = class_name.split('.')
        for _import in import_list:
            if _import[:4] != 'java':
                if _import.split('.')[-1] == class_name[0]:
                    result = _import
                    for c in class_name[1:]:
                        result += '.' + c
                    return result

    @staticmethod
    def get_class_name_from_path(path):
        path = path.split('\\')
        class_name = path[-1]
        class_name = class_name.split('.')
        class_name = class_name[0]
        return class_name

    @staticmethod
    def find_path_from_id(result, index_dic):
        json_list = list(index_dic.keys())
        result['factory'] = json_list[int(result['factory'])]
        products_class_list = []
        for product_class in result['products']['classes']:
            products_class_list.append(json_list[int(product_class)])
        result['products']['classes'] = products_class_list
        return result

class List:
    @staticmethod
    def compare_similarity_of_two_list(list1, list2):
        return list(set(list1) & set(list2))

if __name__ == "__main__":
    File.indexing_files_directory(File.find_all_file('E:\\sadegh\\iust\\compiler\\compiler projects\\java_projects\\bigJavaProject', 'java'),
                                  'index.json')
