from gen.JavaParserLabeled import JavaParserLabeled
from gen.JavaParserLabeledListener import JavaParserLabeledListener

from antlr4 import *
from antlr4.TokenStreamRewriter import TokenStreamRewriter

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
        if 'classOrInterfaceType' in dir(ctx.typeType()):
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
            text += f"\n\tprivate {v['type']} {v['name']};"
            formal_variable_text += f"{v['type']} c_{v['name']},"
            assign_text += f"\n\t\t{v['name']} = c_{v['name']};"
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
            instantiate_text += f"\n\tprivate {v['type']} {v['name']};"
            formal_variable_text += f"{v['type']} c_{v['name']},"
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
