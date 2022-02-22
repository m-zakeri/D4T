import json
import networkx as nx

from antlr4 import *
from gen.JavaLexer import JavaLexer
from antlr4.TokenStreamRewriter import TokenStreamRewriter
from gen.JavaParserLabeledListener import JavaParserLabeledListener
from gen.JavaParserLabeled import JavaParserLabeled


class DependeeEditorListener(JavaParserLabeledListener):
    def __init__(self, common_token_stream: CommonTokenStream = None):
        self.is_main_class = None
        self.currentClass = None
        self.class_dic = {}
        self.imports_star = []
        self.imports = []
        self.initiate_constructor()
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def initiate_constructor(self):
        self.current_constructor_info = {'token_index': None,
                                        'formal_parameter_token_index': None,
                                        'assign_token_index': None
                                        }

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.currentClass = ctx.IDENTIFIER().getText()
        self.class_dic[self.currentClass] = {'local_variables':[]}

    def enterClassBody(self, ctx:JavaParserLabeled.ClassBodyContext):
        self.current_constructor_info['token_index'] = ctx.LBRACE().symbol.tokenIndex

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        self.current_constructor_info['token_index'] = ctx.stop.tokenIndex

    def enterConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        self.current_constructor_info['formal_parameter_token_index'] = ctx.formalParameters().stop.tokenIndex
        self.current_constructor_info['assign_token_index'] = ctx.block().stop.tokenIndex


    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def enterLocalVariableDeclaration(self, ctx:JavaParserLabeled.LocalVariableDeclarationContext):
        if ctx.typeType().classOrInterfaceType() != None:
            _type = ctx.typeType().classOrInterfaceType().IDENTIFIER()[0].getText()
            name = ctx.variableDeclarators().variableDeclarator()[0].variableDeclaratorId().IDENTIFIER().getText()
            self.class_dic[self.currentClass]['local_variables'].append({'type':_type, 'name':name})
            # delete local variable instantiation
            self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                              from_idx=ctx.start.tokenIndex,
                                              to_idx=ctx.stop.tokenIndex + 1
                                              )

    def find_import_of_variables(self, _type):
        return None

    def generate_constructor(self):
        text = ''
        formal_variable_text = ''
        assign_text = ''
        for v in self.class_dic[self.currentClass]['local_variables']:
            text += f"\n\tprivate {'I' + v['type']} {v['name']};"
            formal_variable_text += f"{'I' + v['type']} c_{v['name']},"
            assign_text += f"\n\t{v['name']} = c_{v['name']};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = '{' + assign_text + '\n\t}'

        text += f"\n\tpublic {self.currentClass} ({formal_variable_text})\n\t{assign_text}"

        self.token_stream_rewriter.insertAfter(self.current_constructor_info['token_index'],
                                               text,
                                               program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME)

    def edit_constructor(self):
        instantiate_text = ''
        formal_variable_text = ''
        assign_text = ''
        for v in self.class_dic[self.currentClass]['local_variables']:
            instantiate_text += f"\n\tprivate {'I' + v['type']} {v['name']};"
            formal_variable_text += f"{'I' + v['type']} c_{v['name']},"
            assign_text += f"\n\t\t{v['name']} = c_{v['name']};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = assign_text[2:] + '\n\t'


        self.token_stream_rewriter.insertAfter(
            self.current_constructor_info['token_index'],
            instantiate_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

        self.token_stream_rewriter.insertAfter(
            self.current_constructor_info['formal_parameter_token_index'] - 1,
            ', ' + formal_variable_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

        self.token_stream_rewriter.insertAfter(
            self.current_constructor_info['assign_token_index'] - 1,
            assign_text,
            program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
        )

class ConstructorEditorListener(JavaParserLabeledListener):
    def __init__(self, common_token_stream: CommonTokenStream = None):
        self.currentClass = None
        self.class_dic = {}
        self.imports_star = []
        self.imports = []
        self.state = None
        self.initiate_constructor()
        if common_token_stream is not None:
            self.token_stream_rewriter = TokenStreamRewriter(common_token_stream)
        else:
            raise TypeError('common_token_stream is None')

    def initiate_constructor(self):
        self.current_constructor_info = {'token_index': None,
                                        'formal_parameter_token_index': None,
                                        'assign_token_index': None
                                        }

    def enterClassDeclaration(self, ctx: JavaParserLabeled.ClassDeclarationContext):
        self.currentClass = ctx.IDENTIFIER().getText()
        self.class_dic[self.currentClass] = {'field_variables':{}}
        self.state = 'before constructor'

    def exitClassDeclaration(self, ctx:JavaParserLabeled.ClassDeclarationContext):
        if self.class_dic[self.currentClass]['field_variables'] != {}:
            if self.current_constructor_info['formal_parameter_token_index'] == None:
                self.generate_constructor()
            else:
                self.edit_constructor()

        self.currentClass = None
        self.initiate_constructor()
        self.state = None

    def enterClassBody(self, ctx:JavaParserLabeled.ClassBodyContext):
        self.current_constructor_info['token_index'] = ctx.LBRACE().symbol.tokenIndex

    def enterFieldDeclaration(self, ctx:JavaParserLabeled.FieldDeclarationContext):
        self.current_constructor_info['token_index'] = ctx.parentCtx.parentCtx.start.tokenIndex - 1

        if ctx.typeType().classOrInterfaceType() != None:
            _type = ctx.typeType().classOrInterfaceType().IDENTIFIER()[0].getText()
            name = ctx.variableDeclarators().variableDeclarator()[0].variableDeclaratorId().IDENTIFIER().getText()
            self.class_dic[self.currentClass]['field_variables'][name] = {'type':_type,
                                                                          'can_inject':False,
                                                                          'ctx':ctx}
            print(self.class_dic)

    def enterVariableDeclarator(self, ctx:JavaParserLabeled.VariableDeclaratorContext):
        if ctx.ASSIGN() != None:
            name = ctx.variableDeclaratorId().IDENTIFIER().getText()
            if name in self.class_dic[self.currentClass]['field_variables'].keys():
                self.class_dic[self.currentClass]['field_variables'][name]['can_inject'] = True

    def enterConstructorDeclaration(self, ctx:JavaParserLabeled.ConstructorDeclarationContext):
        self.state = 'in constructor'
        self.current_constructor_info['formal_parameter_token_index'] = ctx.formalParameters().stop.tokenIndex
        self.current_constructor_info['assign_token_index'] = ctx.block().stop.tokenIndex

    def enterMethodDeclaration(self, ctx:JavaParserLabeled.MethodDeclarationContext):
        self.state = 'in method'


    def enterImportDeclaration(self, ctx:JavaParserLabeled.ImportDeclarationContext):
        if '*' in ctx.getText():
            self.imports_star.append(ctx.qualifiedName().getText())
        else:
            self.imports.append(ctx.qualifiedName().getText())

    def generate_constructor(self):
        text = ''
        formal_variable_text = ''
        assign_text = ''
        can_make_constructor = False
        for v in self.class_dic[self.currentClass]['field_variables']:
            v_info = self.class_dic[self.currentClass]['field_variables'][v]
            if v_info['can_inject']:
                can_make_constructor = True
                ctx = v_info['ctx']
                # delete field variable instantiation
                self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=ctx.parentCtx.parentCtx.start.tokenIndex,
                                                  to_idx=ctx.parentCtx.parentCtx.stop.tokenIndex + 1
                                                  )
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
        instantiate_text = ''
        formal_variable_text = ''
        assign_text = ''
        can_edit_constructor = False
        for v in self.class_dic[self.currentClass]['field_variables']:
            v_info = self.class_dic[self.currentClass]['field_variables'][v]
            if v_info['can_inject']:
                can_edit_constructor = True
                ctx = v_info['ctx']
                # delete field variable instantiation
                self.token_stream_rewriter.delete(program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME,
                                                  from_idx=ctx.parentCtx.parentCtx.start.tokenIndex,
                                                  to_idx=ctx.parentCtx.parentCtx.stop.tokenIndex + 1
                                                  )
                instantiate_text += f"private {'I' + v_info['type']} {v};\n\t"
                formal_variable_text += f"{'I' + v_info['type']} {v},"
                assign_text += f"\n\t\tthis.{v} = {v};"
        formal_variable_text = formal_variable_text[:-1]
        assign_text = assign_text[2:] + '\n\t'

        if can_edit_constructor:
            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['token_index'],
                instantiate_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['formal_parameter_token_index'] - 1,
                ', ' + formal_variable_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

            self.token_stream_rewriter.insertAfter(
                self.current_constructor_info['assign_token_index'] - 1,
                assign_text,
                program_name=self.token_stream_rewriter.DEFAULT_PROGRAM_NAME
            )

class Injection:
    def detect_and_fix(self, index_dic, class_diagram):
        index_dic_keys = list(index_dic.keys())
        parents = list((v for v, d in class_diagram.in_degree() if d >= 0))
        for p in parents:
            root_dfs = list(nx.bfs_edges(class_diagram, source=p, depth_limit=1))
            if len(root_dfs) > 0:
                for child_index in root_dfs:
                    child = index_dic_keys[int(child_index[1])]
                    child_path = index_dic[child]['path']
                    #child_class_name = child.split('-')[1]
                    try:
                        stream = FileStream(r"" + child_path)
                    except:
                        print(child_path, 'can not read')
                    lexer = JavaLexer(stream)
                    tokens = CommonTokenStream(lexer)
                    parser = JavaParserLabeled(tokens)
                    tree = parser.compilationUnit()
                    listener = ConstructorEditorListener(common_token_stream=tokens)
                    walker = ParseTreeWalker()
                    walker.walk(
                        listener=listener,
                        t=tree
                    )

                    # print(listener.token_stream_rewriter.getDefaultText())
                    with open(r"" + child_path, mode='w', newline='') as f:
                        f.write(listener.token_stream_rewriter.getDefaultText())

            # refactor dependee class
            parent = index_dic_keys[int(p)]
            parent_path = index_dic[parent]['path']
            #parent_class_name = parent.split('-')[1]
            # try:
            stream = FileStream(r"" + parent_path)
            # except:
            #    print(parent_path, 'can not read')
            lexer = JavaLexer(stream)
            tokens = CommonTokenStream(lexer)
            parser = JavaParserLabeled(tokens)
            tree = parser.compilationUnit()
            listener = DependeeEditorListener(
                common_token_stream=tokens
            )
            walker = ParseTreeWalker()
            walker.walk(
                listener=listener,
                t=tree
            )

            #print(listener.token_stream_rewriter.getDefaultText())


from utils import File
import config
from class_diagram import ClassDiagram

if __name__ == "__main__":
    java_project_address = config.projects_info['simple_injection']['path']
    base_dirs = config.projects_info['simple_injection']['base_dirs']
    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd = ClassDiagram()
    cd.make_class_diagram(java_project_address, base_dirs, index_dic)
    #cd.save('class_diagram.gml')
    #cd.load('class_diagram.gml')
    cd.show(cd.class_diagram_graph)
    g = cd.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)
    #g = cd.class_diagram_graph
    #print(len(list(nx.weakly_connected_components(g))))
    f = Injection()
    f.detect_and_fix(index_dic, cd.class_diagram_graph)

    files = File.find_all_file(java_project_address, 'java')
    index_dic = File.indexing_files_directory(files, 'class_index.json', base_dirs)
    cd2 = ClassDiagram()
    cd2.make_class_diagram(java_project_address, base_dirs, index_dic)
    cd2.show(cd2.class_diagram_graph)
    g = cd2.class_diagram_graph
    print(len(list(nx.weakly_connected_components(g))))
    for i in nx.weakly_connected_components(g):
        print(i)