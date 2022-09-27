"""
The module implements dependency injection patterns
"""

__version__ = '0.1.1'
__author__ = 'Sadegh Jafari, Morteza Zakeri'

import json

import networkx as nx
from csv import writer

from antlr4 import *
from antlr4.TokenStreamRewriter import TokenStreamRewriter


from gen.JavaLexer import JavaLexer
from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener

from utils import Path, File, Struct
from interface import InterfaceCreator, InterfaceInfoListener
import config
from class_diagram import ClassDiagram


class ConstructorEditorListener(JavaParserLabeledListener):
    def __init__(self,
                 base_dirs,
                 file,
                 index_dic,
                 roots_long_name,
                 common_token_stream: CommonTokenStream = None
                 ):
        self.base_dirs = base_dirs
        self.file = file
        self.index_dic = index_dic
        self.file_name = Path.get_file_name_from_path(file)
        self.roots_long_name = roots_long_name

        self.dependees = dict()
        self.current_constructor = None
        self.currentClass = None
        self.imports = list()
        self.imports_star = list()
        self.__method_depth = 0
        self.__class_depth = 0
        self.field_variables = dict()
        self.constructors = list()
        self.generate_constructor_location = None
        self.no_constructor_injection_cases = 0
        self.statistics = {}
        self.package = None
        self.dependencies = []
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    @staticmethod
    def __get_field_variable_struct():
        field_variable = {
            'modifiers': list(),
            'identifier': None,
            'type': None,
            'declaration_location': None,
            'initiation_location': None,
            'initiation_place': None,
            'constructors': list(),
            'dependencies': list()
        }
        return Struct(**field_variable)

    @staticmethod
    def __get_dependee_struct():
        dependee = {
            'name': None,
            'package': None,
            'file': None,
            'type': None
        }
        return Struct(**dependee)

    @staticmethod
    def __get_constructor_struct():
        constructor = {
            'formal_parameters': list(),
            'formal_parameters_start_location': None,
            'stop_location': None
        }
        return Struct(**constructor)

    @staticmethod
    def __get_formal_parameter_struct():
        formal_parameter = {
            'identifier': None,
            'type': None,
            'location': None
        }
        return Struct(**formal_parameter)

    def __edit_variable_type(self, variable_type, location):
        self.token_stream_rewriter.replace(
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
            from_idx=location,
            to_idx=location,
            text=variable_type
        )

    def __delete_new(self, start, stop):
        self.token_stream_rewriter.delete(
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
            from_idx=start,
            to_idx=stop
        )

    def __add_formal_parameter(self, text, location):
        self.token_stream_rewriter.insertAfter(
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
            index=location,
            text=text
        )

    def __edit_formal_parameter(self, variable_type, location):
        self.token_stream_rewriter.replace(
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
            from_idx=location,
            to_idx=location,
            text=variable_type
        )

    def __add_assignment(self, text, location):
        self.token_stream_rewriter.insertAfter(
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
            index=location,
            text=text
        )

    def __edit_assignment(self):
        pass

    @staticmethod
    def __check_modifiers(modifiers):
        if 'final' not in modifiers or 'static' not in modifiers:
            return True
        return False

    def __check_constructor_injection_case1(self, field_variable):
        if field_variable.initiation_place == 'fieldDeclaration' and \
            self.__check_modifiers(field_variable.modifiers) and \
            field_variable.type in self.dependees and \
            field_variable.initiation_location:
            return True
        return False

    def __check_constructor_injection_case2(self, field_variable, constructor):
        if field_variable.initiation_place == 'constructor' and \
            self.__check_modifiers(field_variable.modifiers) and \
            field_variable.type in self.dependees and \
            field_variable.initiation_location:
            constructor_params = [formal_param.identifier for formal_param in constructor.formal_parameters]
            for i in range(len(field_variable.constructors)):
                if field_variable.constructors[i] == constructor:
                    for dependency in field_variable.dependencies[i]:
                        if dependency['type'] == 'identifier':
                            if dependency['value'] not in constructor_params:
                                return False
                    break
            return True
        return False

    def __check_constructor_injection_case3(self, field_variable, constructor):
        if field_variable.initiation_place == 'constructor' and \
            self.__check_modifiers(field_variable.modifiers) and \
            field_variable.type in self.dependees and \
            not field_variable.initiation_location:
            for formal_parameter in constructor.formal_parameters:
                if field_variable.type == formal_parameter.type:
                    if self.dependees[field_variable.type].type == 'class':
                        return True
        return False

    def get_statistics(self):
        result = {'total_case': 0, 'case1': 0, 'case2': 0, 'case3': 0}
        for v in self.field_variables:
            if self.__check_constructor_injection_case1(self.field_variables[v]):
                result['total_case'] += 1
                result['case1'] += 1

            for constructor in self.constructors:
                if self.__check_constructor_injection_case2(self.field_variables[v], constructor):
                    result['total_case'] += 1
                    result['case2'] += 1
                    break

            for constructor in self.constructors:
                if self.__check_constructor_injection_case3(self.field_variables[v], constructor):
                    result['total_case'] += 1
                    result['case3'] += 1
                    break
        return result

    def __generate_constructor(self):
        text = ''
        formal_variable_text = ''
        assign_text = ''
        for v in self.field_variables:
            v_info = self.field_variables[v]
            if self.__check_constructor_injection_case1(v_info):
                # delete field variable instantiation
                self.__delete_new(v_info.initiation_location[0], v_info.initiation_location[1])

                variable_type = v_info.type
                if self.dependees[v_info.type].type == 'class':
                    variable_type = 'I' + v_info.type
                    location = v_info.declaration_location[0] + len(v_info.modifiers) - 1
                    self.__edit_variable_type(variable_type, location)

                #text += f"private {'I' + v_info['type']} {v};\n\t"
                formal_variable_text += f"{variable_type} {v}, "
                assign_text += f"\n\t\tthis.{v} = {v};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = '{' + assign_text + '\n\t}'

        text += f"{self.currentClass} ({formal_variable_text})\n\t{assign_text}\n\n\t"
        self.token_stream_rewriter.insertAfter(
            self.generate_constructor_location,
            text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

    def __edit_constructors(self):
        for c in self.constructors:
            assignments_text = ''
            formal_parameters_text = list()

            for v in self.field_variables:
                v_info = self.field_variables[v]
                # print(v_info.__dict__)
                if self.__check_constructor_injection_case1(v_info) and False:
                    self.__delete_new(v_info.initiation_location[0], v_info.initiation_location[1])
                    variable_type = v_info.type
                    if self.dependees[v_info.type].type == 'class':
                        variable_type = 'I' + v_info.type
                        location = v_info.declaration_location[0] + len(v_info.modifiers) - 1
                        self.__edit_variable_type(variable_type, location)

                    assignments_text += f"\n\t\tthis.{v} = {v};"
                    formal_parameters_text.append(f"{variable_type} {v}")

                if self.__check_constructor_injection_case2(v_info, c):
                    print(v_info.__dict__)

            if assignments_text != '':
                assignment_location = c.stop_location - 2
                self.__add_assignment(assignments_text, assignment_location)

            if len(formal_parameters_text) > 0:
                formal_parameter_location = c.formal_parameters_start_location
                formal_parameters_text = ', '.join(formal_parameters_text)
                print(c.__dict__)
                if len(c.formal_parameters) > 0:
                    formal_parameters_text += ', '
                self.__add_formal_parameter(formal_parameters_text, formal_parameter_location)

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.package = ctx.qualifiedName().getText()

    def enterImportDeclaration(self, ctx: JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        if self.package is None:
            self.package = Path.get_default_package(self.base_dirs, self.file)
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.currentClass = ctx.IDENTIFIER().getText()

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            long_name = f"{self.package}-{self.file_name}-{self.currentClass}"
            if long_name not in self.roots_long_name:
                if len(self.constructors) == 0:     # only can case 1 happen
                    self.__generate_constructor()
                else:
                    self.__edit_constructors()

                self.statistics[long_name] = self.get_statistics()

            self.currentClass = None

    def enterClassBody(self, ctx: JavaParserLabeled.ClassBodyContext):
        if self.__class_depth == 1:
            self.generate_constructor_location = ctx.LBRACE().symbol.tokenIndex

    def enterFieldDeclaration(self, ctx: JavaParserLabeled.FieldDeclarationContext):
        if self.__class_depth == 1:
            classBodyDeclaration = ctx.parentCtx.parentCtx
            self.generate_constructor_location = ctx.parentCtx.parentCtx.stop.tokenIndex + 1
            modifiers = []
            _type = ctx.typeType().getText()
            if _type not in self.dependees:
                dependee = self.__get_dependee_struct()
                dependee.name = _type
                dependee.package, dependee.file = Path.find_package_of_dependee(
                    _type,
                    self.imports,
                    self.imports_star,
                    self.index_dic,
                    self.package,
                    self.file_name
                )
                if dependee.package is not None:
                    long_name = f"{dependee.package}-{dependee.file}-{dependee.name}"
                    if long_name in self.index_dic:
                        dependee.type = self.index_dic[long_name]["type"]
                        self.dependees[_type] = dependee
            start_declaration_location = ctx.start.tokenIndex

            for modifier in classBodyDeclaration.modifier():
                modifiers.append(modifier.getText())

            for identifier in ctx.variableDeclarators().variableDeclarator():
                stop_declaration_location = identifier.variableDeclaratorId().stop.tokenIndex
                field_variable = self.__get_field_variable_struct()
                field_variable.modifiers = modifiers
                field_variable.identifier = identifier.variableDeclaratorId().getText()
                field_variable.type = _type
                field_variable.declaration_location = (start_declaration_location, stop_declaration_location)
                self.field_variables[identifier.variableDeclaratorId().getText()] = field_variable

    def enterVariableInitializer1(self, ctx:JavaParserLabeled.VariableInitializer1Context):
        if self.__class_depth == 1:
            if ctx.children[0].children[0].getText() == 'new':
                for child in ctx.parentCtx.parentCtx.children:
                    if type(child).__name__ == 'VariableDeclaratorContext':
                        if child.variableDeclaratorId().getText() in self.field_variables:
                            self.field_variables[child.variableDeclaratorId().getText()].initiation_location = \
                                (ctx.start.tokenIndex - 2, ctx.stop.tokenIndex)
                            self.field_variables[child.variableDeclaratorId().getText()].initiation_place = 'fieldDeclaration'

    def enterExpression4(self, ctx:JavaParserLabeled.Expression4Context):
        if self.__class_depth == 1:
            identifier_list = ctx.parentCtx.children[0].getText()
            identifier_list = identifier_list.split('.')
            if len(identifier_list) == 1:
                identifier = identifier_list[0]
            elif len(identifier_list) == 2 and identifier_list[0] == 'this':
                identifier = identifier_list[1]
            else:
                identifier = None

            if identifier is not None:
                if identifier in self.field_variables:
                    self.field_variables[identifier].initiation_location = \
                        (ctx.start.tokenIndex, ctx.stop.tokenIndex)
                    if self.current_constructor:
                        self.field_variables[identifier].initiation_place = 'constructor'
                        if len(self.field_variables[identifier].constructors) == 0:
                            self.field_variables[identifier].constructors.append(self.current_constructor)
                            self.field_variables[identifier].dependencies.append([])
                        elif self.field_variables[identifier].constructors[-1] != self.current_constructor:
                            self.field_variables[identifier].constructors.append(self.current_constructor)
                            self.field_variables[identifier].dependencies.append([])

                        for dependency in ctx.creator().classCreatorRest().arguments().expressionList().expression():
                            if 'primary' in dir(dependency):
                                if 'IDENTIFIER' in dir(dependency.primary()):
                                    if dependency.primary().IDENTIFIER():
                                        self.field_variables[identifier].dependencies[-1].append(
                                            {'type': 'identifier', 'value': dependency.getText()}
                                        )
                                elif 'literal' in dir(dependency.primary()):
                                    if dependency.primary().literal():
                                        self.field_variables[identifier].dependencies[-1].append(
                                            {'type': 'literal', 'value': dependency.getText()}
                                        )

    def enterConstructorDeclaration(self, ctx: JavaParserLabeled.ConstructorDeclarationContext):
        if self.__class_depth == 1:
            constructor = self.__get_constructor_struct()
            constructor.stop_location = ctx.stop.tokenIndex
            constructor.formal_parameters_start_location = ctx.formalParameters().start.tokenIndex
            if ctx.formalParameters().formalParameterList() is not None:
                for formal_parameter in ctx.formalParameters().formalParameterList().formalParameter():
                    formal_parameter_s = self.__get_formal_parameter_struct()
                    formal_parameter_s.identifier = formal_parameter.variableDeclaratorId().getText()
                    formal_parameter_s.type = formal_parameter.typeType().getText()
                    formal_parameter_s.location = (
                        formal_parameter.start.tokenIndex,
                        formal_parameter.stop.tokenIndex
                    )
                    constructor.formal_parameters.append(formal_parameter_s)
            self.constructors.append(constructor)
            self.current_constructor = constructor

    def exitConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        if self.__class_depth == 1:
            self.current_constructor = None

    def enterVariableDeclarator(self, ctx: JavaParserLabeled.VariableDeclaratorContext):
        if self.__class_depth == 1:
            pass

    def enterExpression21(self, ctx: JavaParserLabeled.Expression21Context):
        if self.__class_depth == 1:
            identifier_list = ctx.children[0].getText()
            identifier_list = identifier_list.split('.')
            if len(identifier_list) == 1:
                identifier = identifier_list[0]
            elif len(identifier_list) == 2 and identifier_list[0] == 'this':
                identifier = identifier_list[1]
            else:
                identifier = None

            right_value_list = ctx.children[-1].getText()
            right_value_list = right_value_list.split('.')
            if len(right_value_list) == 1:
                right_value = right_value_list[0]
            else:
                right_value = None

            if (identifier is not None) and (right_value is not None):
                if identifier in self.field_variables:
                    if self.current_constructor:
                        self.field_variables[identifier].initiation_place = 'constructor'
                        if len(self.field_variables[identifier].constructors) == 0:
                            self.field_variables[identifier].constructors.append(self.current_constructor)
                            self.field_variables[identifier].dependencies.append([])
                        elif self.field_variables[identifier].constructors[-1] != self.current_constructor:
                            self.field_variables[identifier].constructors.append(self.current_constructor)
                            self.field_variables[identifier].dependencies.append([])

    def enterFormalParameter(self, ctx: JavaParserLabeled.FormalParameterContext):
        if self.__class_depth == 1:
            pass

    def enterMethodDeclaration(self, ctx: JavaParserLabeled.MethodDeclarationContext):
        if self.__class_depth == 1:
            pass


class Injection:
    def __init__(self, base_dirs, index_dic, files, class_diagram):
        self.base_dirs = base_dirs
        self.index_dic = index_dic
        self.index_dic_keys = list(index_dic.keys())
        self.files = files
        self.class_diagram = class_diagram

    def refactor(self):
        print('Start injection refactoring . . .')
        reports = []

        roots_long_name = [self.index_dic_keys[n] for n,d in self.class_diagram.in_degree() if d==0]

        for f in files:
            stream = FileStream(f, encoding='utf-8', errors='ignore')
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = ConstructorEditorListener(
                base_dirs,
                f,
                self.index_dic,
                roots_long_name,
                common_token_stream=tokens
            )
            walker = ParseTreeWalker()
            walker.walk(listener=listener, t=tree)
            print("listener.statistics:", listener.statistics)
            with open(r"" + f, mode='w', encoding='utf-8', newline='') as f:
                f.write(listener.token_stream_rewriter.getDefaultText())

        print('End injection refactoring !')

    def __create_injection_interface(self, path):
        stream = FileStream(path, encoding='utf8', errors='ignore')
        lexer = JavaLexer(stream)
        tokens = CommonTokenStream(lexer)
        parser = JavaParserLabeled(tokens)
        tree = parser.compilationUnit()
        listener = InterfaceInfoListener()
        walker = ParseTreeWalker()
        walker.walk(
            listener=listener,
            t=tree
        )
        interface_info = listener.get_interface_info()
        interface_info['name'] = 'I' + interface_info['name']
        path_list = Path.convert_str_paths_to_list_paths([path])
        interface_info['path'] = '/'.join(path_list[0][:-1])
        ic = InterfaceCreator(interface_info)
        ic.save()
        ic.add_implement_statement_to_class(path)


if __name__ == "__main__":
    java_project_address = config.projects_info['javaproject']['path']
    base_dirs = config.projects_info['javaproject']['base_dirs']
    files = File.find_all_file(java_project_address, 'java')
    index_dic_ = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    #with open('class_index.json') as f:
    #    index_dic_ = json.load(f)
    cd = ClassDiagram(java_project_address, base_dirs, files, index_dic_)
    cd.make_class_diagram()
    cd.set_stereotypes()
    # cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    cd.show(cd.class_diagram_graph)
    g = cd.class_diagram_graph
    # g = cd.class_diagram_graph
    injection = Injection(base_dirs, index_dic_, files, cd.class_diagram_graph)
    injection.refactor()

    files = File.find_all_file(java_project_address, 'java')
    index_dic_ = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd2 = ClassDiagram(java_project_address, base_dirs, files, index_dic_)
    cd2.make_class_diagram()
    cd2.set_stereotypes()
    cd2.show(cd2.class_diagram_graph)
