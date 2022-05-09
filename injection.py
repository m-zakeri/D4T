"""
The module implements dependency injection patterns
"""

__version__ = '0.1.1'
__author__ = 'Sadegh Jafari, Morteza Zakeri'

import json

import networkx as nx

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
    def __init__(self, index_dic, common_token_stream: CommonTokenStream = None):
        self.currentClass = None
        self.class_dic = dict()
        self.imports_star = list()
        self.imports = list()
        self.state = None
        self.no_constructor_formal_parameter = 0
        self.imports = list()
        self.imports_star = list()
        self.dependee_dic = dict()
        self.__method_depth = 0
        self.__class_depth = 0
        self.index_dic = index_dic
        self.last_import_token_index = None
        self.initiate_constructor()
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

        self.current_constructor_info = None
        self.initiate_constructor()

    def initiate_constructor(self):
        self.current_constructor_info = {'token_index': None,
                                         'formal_parameter_token_index': None,
                                         'assign_token_index': None
                                         }

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        self.last_import_token_index = ctx.stop.tokenIndex

    def enterImportDeclaration(self, ctx: JavaParserLabeled.ImportDeclarationContext):
        self.last_import_token_index = ctx.stop.tokenIndex
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.currentClass = ctx.IDENTIFIER().getText()
            self.class_dic[self.currentClass] = {'field_variables': {}}
            self.state = 'before constructor'

    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            if self.class_dic[self.currentClass]['field_variables'] != {}:
                if self.current_constructor_info['formal_parameter_token_index'] is None:
                    self.generate_constructor()
                else:
                    self.edit_constructor()

            self.currentClass = None
            self.initiate_constructor()
            self.state = None

    def enterClassBody(self, ctx: JavaParserLabeled.ClassBodyContext):
        if self.__class_depth == 1:
            self.current_constructor_info['token_index'] = ctx.LBRACE().symbol.tokenIndex

    def enterFieldDeclaration(self, ctx: JavaParserLabeled.FieldDeclarationContext):
        if self.__class_depth == 1:
            self.current_constructor_info['token_index'] = ctx.parentCtx.parentCtx.start.tokenIndex - 1

            if ctx.typeType().classOrInterfaceType() is not None:
                _type = ctx.typeType().classOrInterfaceType().IDENTIFIER()[0].getText()
                name = ctx.variableDeclarators().variableDeclarator()[0].variableDeclaratorId().IDENTIFIER().getText()
                self.class_dic[self.currentClass]['field_variables'][name] = {'type': _type,
                                                                              'can_inject': False,
                                                                              'ctx': ctx}

                if _type not in self.dependee_dic:
                    package = Path.find_package_of_dependee(_type, self.imports,
                                                            self.imports_star,
                                                            self.index_dic
                                                            )
                    if package is not None:
                        if package + '-' + _type + '-' + _type in self.index_dic:
                            self.dependee_dic[_type] = {}
                            object_type = self.index_dic[package + '-' + _type + '-' + _type]['type']
                            self.dependee_dic[_type]['package'] = package
                            self.dependee_dic[_type]['type'] = object_type

    def enterVariableDeclarator(self, ctx: JavaParserLabeled.VariableDeclaratorContext):
        if self.__class_depth == 1 and (ctx.variableInitializer() is not None):
            if 'expression' in dir(ctx.variableInitializer()):
                if 'NEW' in dir(ctx.variableInitializer().expression()):
                    if (ctx.ASSIGN() is not None) and \
                            (self.currentClass is not None) and \
                            (ctx.variableInitializer().expression().NEW() is not None):
                        name = ctx.variableDeclaratorId().IDENTIFIER().getText()
                        if name in self.class_dic[self.currentClass]['field_variables'].keys():
                            self.class_dic[self.currentClass]['field_variables'][name]['can_inject'] = True

    def enterExpression21(self, ctx: JavaParserLabeled.Expression21Context):
        if self.__class_depth == 1:
            if self.state == 'in constructor':
                if 'NEW' in dir(ctx.expression()[1]):
                    if (ctx.ASSIGN() is not None) and \
                            (ctx.expression()[1].NEW() is not None):
                        name = ctx.expression()[0].getText()
                        print('name:', name)
                        if name[:4] == 'this':
                            name = name[5:]
                        if name in self.class_dic[self.currentClass]['field_variables'].keys():
                            self.class_dic[self.currentClass]['field_variables'][name]['can_inject'] = True
                            self.class_dic[self.currentClass]['field_variables'][name]['ctx2'] = ctx

    def enterFormalParameter(self, ctx: JavaParserLabeled.FormalParameterContext):
        if self.__class_depth == 1:
            if self.state == "in constructor":
                self.no_constructor_formal_parameter += 1

    def enterConstructorDeclaration(self, ctx: JavaParserLabeled.ConstructorDeclarationContext):
        if self.__class_depth == 1:
            self.state = 'in constructor'
            self.current_constructor_info['formal_parameter_token_index'] = ctx.formalParameters().stop.tokenIndex
            self.current_constructor_info['assign_token_index'] = ctx.block().stop.tokenIndex

    def enterMethodDeclaration(self, ctx: JavaParserLabeled.MethodDeclarationContext):
        if self.__class_depth == 1:
            self.state = 'in method'

    def generate_constructor(self):
        text = ''
        formal_variable_text = ''
        assign_text = ''
        can_make_constructor = False
        print("field_variables", self.class_dic[self.currentClass]['field_variables'])
        for v in self.class_dic[self.currentClass]['field_variables']:
            v_info = self.class_dic[self.currentClass]['field_variables'][v]
            if v_info['can_inject']:
                can_make_constructor = True
                ctx = v_info['ctx']
                print('ctx:', ctx.getText())
                # delete field variable instantiation
                self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=ctx.parentCtx.parentCtx.start.tokenIndex,
                                                  to_idx=ctx.parentCtx.parentCtx.stop.tokenIndex + 1
                                                  )
                # print('@@@@@@@@@@@@@@@@@@@@@@')
                # print(self.token_stream_rewriter.getDefaultText())

                text += f"private {'I' + v_info['type']} {v};\n\t"
                formal_variable_text += f"{'I' + v_info['type']} {v},"
                assign_text += f"\n\t\tthis.{v} = {v};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = '{' + assign_text + '\n\t}'

        text += f"public {self.currentClass} ({formal_variable_text})\n\t{assign_text}\n\n\t"

        if can_make_constructor:
            self.token_stream_rewriter.insertAfter(self.current_constructor_info['token_index'],
                                                   text,
                                                   program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME)

    def edit_constructor(self):
        """

        """
        instantiate_text = ''
        formal_variable_text = ''
        assign_text = ''
        can_edit_constructor = False
        print("field_variables", self.class_dic[self.currentClass]['field_variables'])
        for v in self.class_dic[self.currentClass]['field_variables']:
            v_info = self.class_dic[self.currentClass]['field_variables'][v]
            if v_info['can_inject']:
                can_edit_constructor = True
                ctx = v_info['ctx']
                print('ctx:', ctx.getText())
                # delete field variable instantiation
                # print('edit_constructor:', ctx.getText())
                self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=ctx.parentCtx.parentCtx.start.tokenIndex,
                                                  to_idx=ctx.parentCtx.parentCtx.stop.tokenIndex + 1
                                                  )

                #if "PricingPolicyFrame" in self.currentClass:
                    # print('@@@@@ 1')
                    # print(self.token_stream_rewriter.getDefaultText())

                if 'ctx2' in v_info:
                    ctx2 = v_info['ctx2']
                    print('ctx2:', ctx2.getText())
                    self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                      from_idx=ctx2.parentCtx.start.tokenIndex,
                                                      to_idx=ctx2.parentCtx.stop.tokenIndex + 1
                                                      )

                # if "PricingPolicyFrame" in self.currentClass:
                #     print('@@@@@ 2')
                #     print(self.token_stream_rewriter.getDefaultText())

                if v_info['type'] in self.dependee_dic:
                    if self.dependee_dic[v_info['type']]['type'] == 'normal':
                        instantiate_text += f"private {'I' + v_info['type']} {v};\n\t"
                        formal_variable_text += f"{'I' + v_info['type']} {v},"
                    else:
                        instantiate_text += f"private {v_info['type']} {v};\n\t"
                        formal_variable_text += f"{v_info['type']} {v},"

                if 'ctx2' in v_info:
                    assign_text += f"\n\tthis.{v} = {v};"
                else:
                    assign_text += f"\n\t\tthis.{v} = {v};"
        formal_variable_text = formal_variable_text[:-1]
        if self.no_constructor_formal_parameter > 0 and formal_variable_text != '':
            formal_variable_text = ', ' + formal_variable_text
        # print("formal_variable_text", formal_variable_text)
        assign_text = assign_text[2:] + '\n\t'

        if can_edit_constructor:
            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['token_index'],
                instantiate_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['formal_parameter_token_index'] - 1,
                formal_variable_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['assign_token_index'] - 1,
                assign_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

    def exitCompilationUnit(self, ctx: JavaParserLabeled.CompilationUnitContext):
        import_text = ''
        print('self.dependee_dic:', self.dependee_dic)
        for dependee in self.dependee_dic:
            if self.dependee_dic[dependee]['type'] == 'normal':
                import_text += f'\nimport {self.dependee_dic[dependee]["package"]}.I{dependee};'

        self.token_stream_rewriter.insertAfter(
            self.last_import_token_index,
            import_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

class ConstructorEditorListener_v2(JavaParserLabeledListener):
    def __init__(self, index_dic, common_token_stream: CommonTokenStream = None):
        self.currentClass = None
        self.imports = list()
        self.imports_star = list()
        self.__method_depth = 0
        self.__class_depth = 0
        self.field_variables = dict()
        self.dependees = dict()
        self.constructors = list()
        self.index_dic = index_dic
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def __get_field_variable_struct(self):
        field_variable = {
            'modifiers':list(),
            'identifier':None,
            'type':None,
            'declaration_location':None,
            'initiation_location':None,
            'initiation_place':None
        }
        return Struct(**field_variable)

    def __get_dependee_struct(self):
        dependee = {
            'name': None,
            'package': None,
            'type': None
        }
        return Struct(**dependee)

    def __get_constructor_struct(self):
        constructor = {
            'formal_parameters':list(),
            'stop_location':None
        }
        return Struct(**constructor)

    def __get_formal_parameter_struct(self):
        formal_parameter = {
            'identifier':None,
            'type':None,
            'location':None
        }
        return Struct(**formal_parameter)

    def __check_constructor_injection_case1(self, variable):
        pass

    def __generate_constructor(self):
        text = ''
        formal_variable_text = ''
        assign_text = ''
        can_make_constructor = False
        for v in self.field_variables:
            v_info = self.field_variables[v]
            if v_info['can_inject']:
                can_make_constructor = True
                ctx = v_info['ctx']
                print('ctx:', ctx.getText())
                # delete field variable instantiation
                self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=ctx.parentCtx.parentCtx.start.tokenIndex,
                                                  to_idx=ctx.parentCtx.parentCtx.stop.tokenIndex + 1
                                                  )
                # print('@@@@@@@@@@@@@@@@@@@@@@')
                # print(self.token_stream_rewriter.getDefaultText())

                text += f"private {'I' + v_info['type']} {v};\n\t"
                formal_variable_text += f"{'I' + v_info['type']} {v},"
                assign_text += f"\n\t\tthis.{v} = {v};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = '{' + assign_text + '\n\t}'

        text += f"public {self.currentClass} ({formal_variable_text})\n\t{assign_text}\n\n\t"

        if can_make_constructor:
            self.token_stream_rewriter.insertAfter(self.current_constructor_info['token_index'],
                                                   text,
                                                   program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME)

    def __edit_constructors(self):
        pass

    def enterPackageDeclaration(self, ctx: JavaParserLabeled.PackageDeclarationContext):
        pass

    def enterImportDeclaration(self, ctx: JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth += 1
        if self.__class_depth == 1:
            self.currentClass = ctx.IDENTIFIER().getText()


    def exitClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.__class_depth -= 1
        if self.__class_depth == 0:
            if len(self.constructors) == 0: #only can case 1 happen
                self.__generate_constructor()
            else:
                self.__edit_constructors()
            self.currentClass = None

    def enterClassBody(self, ctx: JavaParserLabeled.ClassBodyContext):
        if self.__class_depth == 1:
            pass

    def enterFieldDeclaration(self, ctx: JavaParserLabeled.FieldDeclarationContext):
        if self.__class_depth == 1:
            classBodyDeclaration = ctx.parentCtx.parentCtx
            modifiers = []
            _type = ctx.typeType().getText()
            if _type not in self.dependees:
                dependee = self.__get_dependee_struct()
                dependee.name = _type
                dependee.package = Path.find_package_of_dependee(
                    _type,
                    self.imports,
                    self.imports_star,
                    self.index_dic
                )
                if dependee.package is not None:
                    dependee.type = self.index_dic[f"{dependee.package}-{dependee.name}-{dependee.name}"]["type"]
                    print(dependee.__dict__)
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

                # print(self.token_stream_rewriter.getText(
                #     program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                #     start=start_declaration_location,
                #     stop=stop_declaration_location
                # ))
                # print(field_variable.__dict__)

    def enterVariableInitializer1(self, ctx:JavaParserLabeled.VariableInitializer1Context):
        if self.__class_depth == 1:
            if ctx.children[0].children[0].getText() == 'new':
                for child in ctx.parentCtx.parentCtx.children:
                    # print(dir(child))
                    # print(type(child).__name__)
                    if type(child).__name__ == 'VariableDeclaratorContext':
                        if child.variableDeclaratorId().getText() in self.field_variables:
                            self.field_variables[child.variableDeclaratorId().getText()].initiation_location = \
                                (ctx.start.tokenIndex - 2, ctx.stop.tokenIndex)
                            self.field_variables[child.variableDeclaratorId().getText()].initiation_place = 'fieldDeclaration'
                            print('initializer:')
                            # print(self.token_stream_rewriter.getText(
                            #     program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                            #     start=ctx.start.tokenIndex - 2,
                            #     stop=ctx.stop.tokenIndex
                            # ))
                            print(self.field_variables[child.variableDeclaratorId().getText()].__dict__)

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
                    self.field_variables[identifier].initiation_place = 'constructor'
                    print('initializer2:')
                    # print(self.token_stream_rewriter.getText(
                    #     program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                    #     start=ctx.start.tokenIndex,
                    #     stop=ctx.stop.tokenIndex
                    # ))
                    print(self.field_variables[identifier].__dict__)

    def enterConstructorDeclaration(self, ctx: JavaParserLabeled.ConstructorDeclarationContext):
        if self.__class_depth == 1:
            constructor = self.__get_constructor_struct()
            constructor.stop_location = ctx.stop.tokenIndex
            for formal_parameter in ctx.formalParameters().formalParameterList().formalParameter():
                formal_parameter_s = self.__get_formal_parameter_struct()
                formal_parameter_s.identifier = formal_parameter.variableDeclaratorId().getText()
                formal_parameter_s.type = formal_parameter.typeType().getText()
                formal_parameter_s.location = (
                    formal_parameter.start.tokenIndex,
                    formal_parameter.stop.tokenIndex
                )
                print(formal_parameter_s.__dict__)
                constructor.formal_parameters.append(formal_parameter_s)
            print(constructor.__dict__)



    def enterVariableDeclarator(self, ctx: JavaParserLabeled.VariableDeclaratorContext):
        if self.__class_depth == 1:
            pass

    def enterExpression21(self, ctx: JavaParserLabeled.Expression21Context):
        if self.__class_depth == 1:
            pass

    def enterFormalParameter(self, ctx: JavaParserLabeled.FormalParameterContext):
        if self.__class_depth == 1:
            pass

    def enterMethodDeclaration(self, ctx: JavaParserLabeled.MethodDeclarationContext):
        if self.__class_depth == 1:
            pass



class Injection:
    def detect_and_fix(self, index_dic, class_diagram):
        print('Start injection refactoring . . .')
        index_dic_keys = list(index_dic.keys())
        parents = list((v for v, d in class_diagram.in_degree() if d >= 0))
        interfaces = set()
        for p in parents:
            root_dfs = list(nx.bfs_edges(class_diagram, source=p, depth_limit=1))
            if len(root_dfs) > 0:
                for child_index in root_dfs:
                    child = index_dic_keys[int(child_index[1])]
                    child_path = index_dic[child]['path']
                    print('\t', child_path)
                    # child_class_name = child.split('-')[1]
                    stream = FileStream(child_path, encoding='utf8', errors='ignore')
                    lexer = JavaLexer(stream)
                    tokens = CommonTokenStream(lexer)
                    parser = JavaParserLabeled(tokens)
                    tree = parser.compilationUnit()
                    #listener = ConstructorEditorListener(index_dic, common_token_stream=tokens)
                    listener = ConstructorEditorListener_v2(index_dic, common_token_stream=tokens)
                    walker = ParseTreeWalker()
                    walker.walk(
                        listener=listener,
                        t=tree
                    )

                    # print(listener.token_stream_rewriter.getDefaultText())
                    with open(r"" + child_path, mode='w', encoding='utf-8', newline='') as f:
                        if listener.token_stream_rewriter.getDefaultText() is None:
                            print('this text is None!')
                        f.write(listener.token_stream_rewriter.getDefaultText())

                    # for dependee in listener.dependee_dic:
                    #     if listener.dependee_dic[dependee]['type'] == 'normal':
                    #         key = listener.dependee_dic[dependee]['package'] + '-' + dependee + '-' + dependee
                    #         interfaces.add(index_dic[key]['path'])

        # create interfaces
        # print(interfaces)
        # for path in interfaces:
        #     self.__create_injection_interface(path)
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
        print(interface_info)
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
    print(files)
    index_dic_ = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    #with open('class_index.json') as f:
    #    index_dic_ = json.load(f)
    cd = ClassDiagram(java_project_address, base_dirs, index_dic_)
    cd.make_class_diagram()
    cd.set_stereotypes()
    # cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    cd.show(cd.class_diagram_graph)
    g = cd.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)
    # g = cd.class_diagram_graph
    # print(len(list(nx.weakly_connected_components(g))))
    injection = Injection()
    injection.detect_and_fix(index_dic_, cd.class_diagram_graph)

    files = File.find_all_file(java_project_address, 'java')
    index_dic_ = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd2 = ClassDiagram(java_project_address, base_dirs, index_dic_)
    cd2.make_class_diagram()
    cd2.set_stereotypes()
    cd2.show(cd2.class_diagram_graph)
    g = cd2.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)

